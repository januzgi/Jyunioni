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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Created by JaniS on 29.7.2017.
 * <p>
 * Class for performing queries to the websites of different group's.
 * Calls methods in parsing classes to form the Event objects.
 * Queries is called from a Loader performing tasks in a background thread.
 */
final class Queries {

    /**
     * Private constructor
     */
    private Queries() { }


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
            urlConnection.setReadTimeout(20000  /*milliseconds*/);
            urlConnection.setConnectTimeout(55000  /*milliseconds*/);
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
     * Query data from different websites and return a list of Event objects.
     */
    public static List<Event> fetchEventData(String[] requestUrl) {

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


        /**
         * Check which groups URL's are on the StringArray of URL's.
         * Fetch data from all the URL's in the stringArray.
         */
        for (int i = 0; i < requestUrl.length; i++) {

            /** LINKKI JYVÄSKYLÄ RY */
            if (requestUrl[i].contains("linkkiEvents.txt")) {

                linkkiUrl = createUrl(requestUrl[i]);

                // Extract Event fields from the .txt response and create a list of Linkki's Events.
                // Then add the events to the Linkki's Events list.
                eventsLinkki.addAll(EventDetailsParser.extractEventDetails(sendHttpRequest(linkkiUrl)));


                /** PÖRSSI RY */
            } else if (requestUrl[i].contains("porssiEvents.txt")) {

                porssiUrl = createUrl(requestUrl[i]);

                // Extract Event fields from the .txt response and create a list of Pörssi's Events.
                // Then add the events to the Pörssi's Events list.
                eventsPorssi.addAll(EventDetailsParser.extractEventDetails(sendHttpRequest(porssiUrl)));


                /** DUMPPI RY */
            } else if (requestUrl[i].contains("dumppiEvents.txt")) {

                dumppiUrl = createUrl(requestUrl[i]);

                // Extract Event fields from the .txt response and create a list of Dumppi's Events.
                // Then add the events to the Dumppi's Events list.
                eventsDumppi.addAll(EventDetailsParser.extractEventDetails(sendHttpRequest(dumppiUrl)));


                /** STIMULUS RY */
             }  else if (requestUrl[i].contains("stimulusEvents.txt")) {

                stimulusUrl = createUrl(requestUrl[i]);

                // Extract Event fields from the .txt response and create a list of Stimulus' Events.
                // Then add the events to the Stimulus' Events list.
                eventsDumppi.addAll(EventDetailsParser.extractEventDetails(sendHttpRequest(stimulusUrl)));

            }
        }
        // Only Linkki's events might be passed one's. Loop them through to delete already passed events.
        eventsLinkki = deletePassedEvents(eventsLinkki);

        // Add all events from different groups list's to the allEventsList
        allEventsList.addAll(eventsLinkki);
        allEventsList.addAll(eventsPorssi);
        allEventsList.addAll(eventsDumppi);
        allEventsList.addAll(eventsStimulus);

        allEventsList = organizeEventsByTimestamp(allEventsList);

        // Return the list of all Events from different groups.
        return allEventsList;
    }


    /**
     * Delete passed events from Linkki's event's list
     */
    public static List<Event> deletePassedEvents(List<Event> eventsLinkki) {
        // Create today's datestamp
        Date today = new Date();

        // Loop through events checking for timestamps that are in the history and deleting those events
        for (int i = 0; i < eventsLinkki.size(); i++) {
            if (eventsLinkki.get(i).getEventStartDate().before(today)){
                eventsLinkki.remove(i);
            }
        }

        return eventsLinkki;
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

        String eventName1, eventName2, eventName3;

        // If there are two objects of the same event, delete the first one.
        for (int i = 0; i < allEventsList.size() - 2; i++) {
            eventName1 = allEventsList.get(i).getEventName();
            eventName2 = allEventsList.get(i + 1).getEventName();
            eventName3 = allEventsList.get(i + 2).getEventName();

            if (eventName1.equals(eventName2) || eventName1.equals(eventName3)){

                // Check even further ahead, Linkki's calendar overrides events easily so there can be double's.
                if (eventName1.equals(eventName3)){
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