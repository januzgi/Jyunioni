package com.example.android.jyunioni;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class Queries {

    /**
     * Create a private constructor because no one should ever create a {@link Queries} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private Queries() {
    }

    /**
     * Returns new URL object from the given string URL.
     */
    public static URL createUrl(String stringUrl) {
        URL url;

        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }


    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public static String makeHttpRequest(URL url) throws IOException {
        if (url == null) return "";

        String response = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000  /*milliseconds*/);
            urlConnection.setConnectTimeout(15000  /*milliseconds*/);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                // If HTTP request was succesful
                inputStream = urlConnection.getInputStream();
                response = readFromStream(inputStream);
            } else {
                // If the response code was != 200
                Log.e(LOG_TAG, "HTTP request response code wasn't 200 (OK), but instead: " + Integer.toString(urlConnection.getResponseCode()));
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "IOexception message when making HTTP request." +
                    "\n(This comes from the catch block inside the method makeHttpRequest in EventActivity.): " + e);
        } finally {
            if (urlConnection != null) {
                // Anyhow disconnect when finished.
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Close the inputstream from reserving resources
                inputStream.close();
            }
        }
        return response;
    }


    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole HTTP response from the server.
     */
    public static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                // Add a newline so the output won't be at one line.
                output.append(line + "\n");
                line = reader.readLine();
            }
        }
        return output.toString();
    }



    /**
     * Return an {@link List<Event>} object by parsing out information from the HTTP response.
     * Event image, name, timestamp, general information, url and group's color id is needed.
     */
    public static List<Event> extractDetails(String httpResponseString) {
        // TODO: Entä jos kuussa ei olekaan tapahtumia

        // TODO: kuinka pitkälle tulevaisuuteen näytetään

        // TODO: URL:in muokkaus pitää automatisoida. Eli jos ei ole tässä kuussa tapahtumia, kokeile seuraavaa kuuta,
        // TODO: jos se ei ole validi niin onko parempi vain luoda objekti, joka sanoo, että katso
        // TODO: tulevia tapahtumia Linkin kalenterista ja ohjata Linkin tapahtumien sivulle?


        // Create the Event and List<Event> objects instance
        List<Event> extractedEvents = new ArrayList<>();

        // Helper variable for the scanner loops
        String line;

        // The amount of events is counted using every event's starting symbol
        String eventBegin = "BEGIN:VEVENT";

        // Create a scanner and loop through the string to count the amount of
        // separate events in the string
        Scanner eventsCountScanner = new Scanner(httpResponseString).useDelimiter("[\n]");

        // Create a variable to initialize right size string arrays later
        int eventsCount = 0;

        // If the line contains the beginning of a new event, then add one to
        // the events counter
        while (eventsCountScanner.hasNext()) {
            line = eventsCountScanner.next();
            if (line.contains(eventBegin))
                eventsCount++;
        }


        // Create strings for the different fields that are extracted from
        // the HTTP response string
        String eventTimeStart = "";
        String eventTimeEnd = "";
        String eventTimestamp = "";

        String eventName = "";
        String eventInformation = "";
        String eventUrl = "";

        int eventImageId = -1;
        int eventGroupColorId = -1;

        // Scan through the fields and add the contents to the corresponding
        // String arrays. Use 'newline' as a limiter to go to nextLine().
        Scanner fieldsScanner = new Scanner(httpResponseString).useDelimiter("[\n]");

        // When the for -loop has been done (one event has been extracted, this
        // adds by one)
        int loopCount = 0;


        // When there's still text left in the scanner and there are events in the HTTP response
        while (fieldsScanner.hasNext() && eventsCount != 0) {

            // Get the different fields information to desired String arrays,
            // from which they can easily be matched up.
            // Add the fields to the "results" string array
            for (int i = 0; i < eventsCount; i++) {

                // Use the scanner to parse the details of each event
                line = fieldsScanner.next();

                // Event's starting time
                if (line.contains("DTSTART;")) {
                    eventTimeStart = Parser.extractTime(line);
                }

                // Event's ending time
                else if (line.contains("DTEND;")) {
                    eventTimeEnd = Parser.extractTime(line);
                    // Get the timestamp from the starting and ending times of the event
                    eventTimestamp = Parser.checkEventTimestamp(eventTimeStart, eventTimeEnd);
                }


                // Event's name
                else if (line.contains("SUMMARY")) {
                    eventName = Parser.extractField(line);
                }


                // Event's description / overall information
                else if (line.contains("DESCRIPTION:")) {
                    eventInformation = Parser.extractDescriptionField(line);
                }


                // Event's URL
                // Skip the first URL, which is the "X-ORIGINAL-URL:" and add only the 'events' to the list
                else if (line.contains("URL") && line.contains("event")) {
                    eventUrl = Parser.extractUrl(line);

                    // Match up the event's group image and color according to the URL where the info was extracted from
                    if (eventUrl.contains("linkkijkl")) {
                        eventImageId = R.drawable.linkki_jkl_icon;
                        eventGroupColorId = R.color.color_linkki_jkl;
                    }
                }

                // If this was the end of the event, add the event details to an object in events arraylist
                else if (line.contains("END:VEVENT")) {

                    extractedEvents.add(new Event(eventName, eventTimestamp, eventInformation, eventImageId, eventGroupColorId, eventUrl));

                    // If there is the "event end" then exit the for loop back to
                    // the while loop
                    loopCount++;
                    break;
                }

            }
            // If the loop has gone through all the events
            if (loopCount == eventsCount) break;
        }


        return extractedEvents;

    }


}



/*
    *
     * Query the USGS dataset and return a list of {@link Earthquake} objects.

    public static List<Earthquake> fetchEarthquakeData(String requestUrl) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.e(LOG_TAG, "fetchEarthQuakeData(); at QueryUtils.java");

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        List<Earthquake> earthquakes = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Earthquake}s
        return earthquakes;
    }*/


// Se mitä tehtiin doInBackgroundissa voitaisiin tehdä fetchEventData:ssa? Ilmeisesti se oli ajatus myös Earthquake apissa

  /*  protected List<Event> doInBackground(URL... urls) {

        // Create an List<Event> object instance
        List<Event> events;

        // Create URL object for fetching the Linkki Jyväskylä Ry event's
        URL url = Queries.createUrl(LINKKI_EVENTS_URL);

        // Perform HTTP request to the URL and receive a response
        String response = "";
        try {
            response = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException when making the HTTP request in doInBackground at EventActivity" + e);
        }

        // Extract relevant fields from the HTTP response and create an Event object
        // updateUi gets the result Event object via the onPostExecute().
        events = extractDetails(response);

        // Return the Event object as the result for the EventAsyncTask
        return events;*/
