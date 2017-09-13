package com.example.android.jyunioni;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Created by JaniS on 29.7.2017.
 * <p>
 * Class for performing queries to the websites of different group's.
 * Calls methods in parsing classes to form the Event objects.
 * Queries is called from a Loader performing tasks in a background thread.
 */
public final class Queries {

    /**
     * Private constructor
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
     * Make an HTTP request to the given URL and return the response as a String.
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
                    "\n(This comes from the catch block inside the method makeHttpRequest in Queries.): " + e);
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
     * Convert the InputStream into a String which contains the whole HTTP response from the server.
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
     * Event name, timestamp, general information, image ID, group's color id and event url is needed.
     */
    public static List<Event> extractLinkkiEventDetails(String httpResponseString) {
        // TODO: Menneiden tapahtumien poisjättäminen.

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
                    eventTimeStart = linkkiDetailsParser.extractTime(line);
                }

                // Event's ending time
                else if (line.contains("DTEND;")) {
                    eventTimeEnd = linkkiDetailsParser.extractTime(line);

                    // Get the timestamp from the starting and ending times of the event
                    eventTimestamp = linkkiDetailsParser.checkEventTimestamp(eventTimeStart, eventTimeEnd);
                }


                // Event's name
                else if (line.contains("SUMMARY")) {
                    eventName = linkkiDetailsParser.extractField(line);
                }


                // Event's description / overall information
                else if (line.contains("DESCRIPTION:")) {
                    eventInformation = linkkiDetailsParser.extractDescriptionField(line);
                }


                // Event's URL
                // Skip the first URL, which is the "X-ORIGINAL-URL:" and add only the 'events' to the list
                else if (line.contains("URL") && line.contains("event")) {
                    eventUrl = linkkiDetailsParser.extractUrl(line);

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


    /**
     * Return an Event object by parsing out information from the HTTP response.
     * Event name, timestamp, general information, image ID, group's color id and event url is needed.
     */
    public static Event extractPorssiEventDetails(String url) {
        // Create the Event and List<Event> objects instance
        Event event = null;

        String content = null;

        // Fetch the event's raw html data from the url
        try {
            Document document = Jsoup.connect(url).get();
            content = document.select("div#content").toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException at extractPorssiEventDetails()\n" + e);
        }

        // Parse the data in the porssiDetailsParser class and create an Event object
        Scanner scanner = new Scanner(content).useDelimiter("[\n]");

        // Variables for the different values of an Event object
        String eventName = null;
        String eventInformation = null;

        String eventDate = null;
        String eventTimestamp = null;


        // Helper variable for the scanner loops
        String line;

        while (scanner.hasNext()) {
            line = scanner.next();

            if (line.contains("<h1>")) {
                eventName = porssiDetailsParser.extractEventName(line);

            } else if (line.contains("dashicons-calendar-alt\"></span>")) {
                eventDate = porssiDetailsParser.extractDate(line);

            } else if (line.contains("dashicons-clock\"></span>")) {
                String eventTime = porssiDetailsParser.extractTime(line);
                eventTimestamp = eventDate + " " + eventTime;

            } else if (line.contains("<div class=\"row\" data-equalizer data-equalizer-mq=\"medium-up\">")) {
                // Skip to the first line of the <p> element where the event information is.
                line = scanner.next();
                line = scanner.next();

                // If there's an image, skip over it
                if (line.contains("<p><img src")) {
                    line = scanner.next();
                }

                boolean pElements = true;

                // Make a String out of the <p> content
                while (pElements) {
                    eventInformation = eventInformation + "\n" + line.trim();
                    line = scanner.next();
                    // If the <p> element still continues
                    if (line.contains("<p>") == false) pElements = false;
                }
                eventInformation = porssiDetailsParser.extractEventInformation(eventInformation);

            }
        }

        // Create the Event with the fetched data
        event = new Event(eventName, eventTimestamp, eventInformation, R.drawable.porssi_ry_icon, R.color.color_porssi_ry, url);

        return event;
    }


    /**
     * Query data from different websites and return a list of Event objects.
     */
    public static List<Event> fetchEventData(String[] requestUrl) {

        List<Event> eventsLinkki = new ArrayList<>();
        List<Event> eventsPorssi = new ArrayList<>();
        List<Event> allEventsList = new ArrayList<>();

        List<String> porssiEventUrls = new ArrayList<>();

        // Create URL objects
        URL linkkiUrl;
        String porssiUrl;

        /**
         * Check which groups URL's are on the StringArray of URL's.
         * Fetch data from all the URL's in the stringArray.
         */
        for (int i = 0; i < requestUrl.length; i++) {

            if (requestUrl[i].contains("linkkijkl.fi")) {

                linkkiUrl = createUrl(requestUrl[i]);

                // Extract relevant fields from the HTTP response and create a list of Linkki's Events.
                // Then add the events to the Linkki's Events list.
                eventsLinkki.addAll(extractLinkkiEventDetails(sendHttpRequest(linkkiUrl)));

            } else if (requestUrl[i].contains("porssiry.fi")) {

                // Get just the "css-events-list" HTML div's data from Pörssi's website using jsoup library.
                /** jsoup HTML parser library @ https://jsoup.org */
                try {

                    Document document = Jsoup.connect(requestUrl[i]).get();

                    /** https://jsoup.org/cookbook/extracting-data/selector-syntax */
                    Elements eventUrlElements = document.getElementsByClass("css-events-list").select("[href]");

                    int j = 0;
                    // Put the elements content (the URL's) from href fields to a String List
                    for (Element element : eventUrlElements) {
                        porssiEventUrls.add(element.attr("href"));
                        j++;
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Problem in jsouping.\n" + e);
                }

                Event porssiEvent = null;

                /** Fetch each event's data using the URL array to create the Event objects. */
                for (int j = 0; j < porssiEventUrls.size(); j++) {
                    porssiUrl = porssiEventUrls.get(j);

                    // Extract relevant fields from the HTTP response and create a list of Porssi's Events
                    porssiEvent = (extractPorssiEventDetails(porssiUrl));
                    eventsPorssi.add(porssiEvent);
                }

            }
        }

        // Add all events from different groups list's to the allEventsList
        allEventsList.addAll(eventsPorssi);
        allEventsList.addAll(eventsLinkki);

        allEventsList = organizeEventsByTimestamp(allEventsList);

        // Return the list of all Events from different groups.
        return allEventsList;
    }


    /**
     * Organize events by timestamp so that today's event is on top and so list continues
     */
    public static List<Event> organizeEventsByTimestamp(final List<Event> allEventsList) {

        /** Arrange the list by the event's timestamp field using Comparator class. */
        class EventTimeComparator implements Comparator<Event> {
            @Override
            public int compare(Event event1, Event event2) {

                return event1.getEventStartDate().compareTo(event2.getEventStartDate());
            }
        }

        Collections.sort(allEventsList, new EventTimeComparator());

        return allEventsList;
    }

    /**
     * Do a small method call which all different URL's need.
     */
    private static String sendHttpRequest(URL url) {
        // Perform HTTP request to the URL and receive a string response back
        String httpResponse = null;
        try {
            httpResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException when making the HTTP request in sendHttpRequest at Queries.java ", e);
        }
        return httpResponse;
    }


}
