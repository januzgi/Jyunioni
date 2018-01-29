package jani.suoranta.android.jyunioni;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Jani Suoranta on 29.7.2017.
 *
 * Class for performing queries to the websites of different group's.
 * Calls methods in parsing classes to form the Event objects.
 * Queries is called from a Loader performing tasks in a background thread.
 *
 * @author Jani Suoranta 25.11.2017
 */
final class Queries {

    /**
     * Private constructor
     */
    private Queries() {
    }


    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url;

        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            return null;
        }
        return url;
    }


    /**
     * Make an HTTPS request to the given URL and return the response as a String.
     */
    private static String makeHttpsRequest(URL url) throws IOException {
        if (url == null) return "";

        String response = "";
        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(2000  /*milliseconds*/);
            urlConnection.setConnectTimeout(5000  /*milliseconds*/);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                // If HTTP request was succesful
                inputStream = urlConnection.getInputStream();
                response = readFromStream(inputStream);
            } else {
                response = "SERVER_ERROR";
            }

        } catch (IOException e) {
            response = "SERVER_ERROR";
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
    private static String readFromStream(InputStream inputStream) throws IOException {
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
     * Query data from the server events .txt files and return a list of Event objects.
     */
    static List<Event> fetchEventData(String[] requestUrl) {

        // Create URL objects and the Strings for URL's. Create the lists in which the event's will be put into.
        List<Event> eventsLinkki = new ArrayList<>();
        URL linkkiUrl;

        List<Event> eventsPorssi = new ArrayList<>();
        URL porssiUrl;

        List<Event> eventsDumppi = new ArrayList<>();
        URL dumppiUrl;

        List<Event> eventsStimulus = new ArrayList<>();
        URL stimulusUrl;

        List<Event> allEventsList = new ArrayList<>();


        /*
         * Check which groups URL's are on the StringArray of URL's.
         * Fetch data from all the URL's in the stringArray.
         */
        for (int i = 0; i < requestUrl.length; i++) {

            /* LINKKI JYVÄSKYLÄ RY */
            if (requestUrl[i].contains("linkkiEvents.txt")) {

                linkkiUrl = createUrl(requestUrl[i]);

                // Extract Event fields from the .txt response and create a list of Linkki's Events.
                // Then add the events to the Linkki's Events list.
                String httpResponse = sendHttpsRequest(linkkiUrl);

                // Check for an error in the server.
                if (httpResponse.equals("SERVER_ERROR")) {
                    allEventsList.add(new Event("Ongelmia", "1.1.",
                            "Ongelmia sovelluksessa tai serverillä. Yritä myöhemmin uudelleen." +
                                    "\nJos ongelma ei ratkea päivän sisään, laita viestiä tekijälle:\n\njanisuoranta@icloud.com",
                            jani.suoranta.android.jyunioni.R.drawable.error_icon, jani.suoranta.android.jyunioni.R.color.error_color, "https://media.giphy.com/media/EUxkZPmTfB7Fe/giphy.gif"));
                    return allEventsList;
                }

                eventsLinkki.addAll(EventDetailsParser.extractEventDetails(httpResponse));


                /* PÖRSSI RY */
            } else if (requestUrl[i].contains("porssiEvents.txt")) {

                porssiUrl = createUrl(requestUrl[i]);

                // Extract Event fields from the .txt response and create a list of Pörssi's Events.
                // Then add the events to the Pörssi's Events list.
                eventsPorssi.addAll(EventDetailsParser.extractEventDetails(sendHttpsRequest(porssiUrl)));


                /* DUMPPI RY */
            } else if (requestUrl[i].contains("dumppiEvents.txt")) {

                dumppiUrl = createUrl(requestUrl[i]);

                // Extract Event fields from the .txt response and create a list of Dumppi's Events.
                // Then add the events to the Dumppi's Events list.
                eventsDumppi.addAll(EventDetailsParser.extractEventDetails(sendHttpsRequest(dumppiUrl)));


                /* STIMULUS RY */
            } else if (requestUrl[i].contains("stimulusEvents.txt")) {

                stimulusUrl = createUrl(requestUrl[i]);

                // Extract Event fields from the .txt response and create a list of Stimulus' Events.
                // Then add the events to the Stimulus' Events list.
                eventsStimulus.addAll(EventDetailsParser.extractEventDetails(sendHttpsRequest(stimulusUrl)));

            }
        }

        // Add all events from different groups list's to the allEventsList
        allEventsList.addAll(eventsLinkki);
        allEventsList.addAll(eventsPorssi);
        allEventsList.addAll(eventsDumppi);
        allEventsList.addAll(eventsStimulus);

        // Organize the events by timestamp in ascending order
        allEventsList = organizeEventsByTimestamp(allEventsList);

        // Events might be passed one's. Loop them through to delete already passed events.
        allEventsList = deletePassedEvents(allEventsList);

        // Return the list of all Events from different groups.
        return allEventsList;
    }


    /**
     * Delete passed events from the event's list
     */
    private static List<Event> deletePassedEvents(List<Event> allEventsList) {
        // Create yesterday's datestamp so that today's events will be shown.
        // Minus one day from today's Date instance
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();

        // Loop through events checking for timestamps that are in the history and deleting those events
        for (int i = 0; i < allEventsList.size(); i++) {
            // If the date is before yesterday then remove the event. Mind the index.
            if (allEventsList.get(i).getEventStartDate().before(yesterday)) {
                allEventsList.remove(i);
                i--;
            }
        }

        return allEventsList;
    }


    /**
     * Organize events by timestamp so that today's event is on top and so list continues
     */
    private static List<Event> organizeEventsByTimestamp(final List<Event> allEventsList) {

        /* Arrange the list by the event's timestamp field using Comparator class. */
        class EventTimeComparator implements Comparator<Event> {
            @Override
            public int compare(Event event1, Event event2) {
                return event1.getEventStartDate().compareTo(event2.getEventStartDate());
            }
        }

        Collections.sort(allEventsList, new EventTimeComparator());

        String eventName1, eventName2, eventName3;

        // If there are two objects of the same event, delete the first one.
        for (int i = 0; i < allEventsList.size() - 2; i++) {
            eventName1 = allEventsList.get(i).getEventName();
            eventName2 = allEventsList.get(i + 1).getEventName();
            eventName3 = allEventsList.get(i + 2).getEventName();

            if (eventName1.equals(eventName2) || eventName1.equals(eventName3)) {

                // Check even further ahead, Linkki's calendar overrides events easily so there can be double's.
                if (eventName1.equals(eventName3)) {
                    // Remember that the index of latter objects changes when deleting.
                    allEventsList.remove(i + 1);
                }

                allEventsList.remove(i);
            }
        }

        return allEventsList;
    }


    /**
     * Do a small method call which all different URL's need.
     */
    private static String sendHttpsRequest(URL url) {
        // Perform HTTP request to the URL and receive a string response back
        String httpsResponse = null;
        try {
            httpsResponse = makeHttpsRequest(url);
        } catch (IOException e) {
            return "SERVER_ERROR";
        }
        return httpsResponse;
    }

}