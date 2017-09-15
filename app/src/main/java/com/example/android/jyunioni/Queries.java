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
     * Query data from different websites and return a list of Event objects.
     */
    public static List<Event> fetchEventData(String[] requestUrl) {

        // Create URL objects and the Strings for URL's. Create the lists in which the event's will be put into.
/*
        List<Event> eventsLinkki = new ArrayList<>();
        URL linkkiUrl;

        List<Event> eventsPorssi = new ArrayList<>();
        List<String> porssiEventUrls = new ArrayList<>();
        String porssiUrl;
*/

        List<Event> eventsDumppi = new ArrayList<>();
        List<String> dumppiEventUrls = new ArrayList<>();
        String dumppiUrl;

        List<Event> allEventsList = new ArrayList<>();


        // else if (requestUrl[i].contains("dumppi.fi")) {

            // Get just the "css-events-list" HTML div's data from Dumppi's website using jsoup library.
            /** jsoup HTML parser library @ https://jsoup.org */
            try {

                Document document = Jsoup.connect(requestUrl[0]).get();

                /** https://jsoup.org/cookbook/extracting-data/selector-syntax */
                Elements eventUrlElements = document.getElementsByClass("css-events-list").select("[href]");

                // Put the elements content (the URL's) from href fields to a String List
                // The first URL is a solid one, "ilmoittautumisen pelisäännöt", so we skip over it.
                for (Element element : eventUrlElements.subList(1, eventUrlElements.size())) {
                    dumppiEventUrls.add(element.attr("href"));
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem in jsouping Dumppi Ry's events.\n" + e);
            }

            Event dumppiEvent;

            /** Fetch each event's data using the URL array to create the Event objects. */
            for (int j = 0; j < dumppiEventUrls.size(); j++) {
                dumppiUrl = dumppiEventUrls.get(j);

                // Extract relevant fields from the HTTP response and create a list of Porssi's Events
                dumppiEvent = dumppiDetailsParser.extractDumppiEventDetails(dumppiUrl);
                eventsDumppi.add(dumppiEvent);
            }


        /**
         * Check which groups URL's are on the StringArray of URL's.
         * Fetch data from all the URL's in the stringArray.
         */
       /* for (int i = 0; i < requestUrl.length; i++) {

            * LINKKI JYVÄSKYLÄ RY
            if (requestUrl[i].contains("linkkijkl.fi")) {

                linkkiUrl = createUrl(requestUrl[i]);

                // Extract relevant fields from the HTTP response and create a list of Linkki's Events.
                // Then add the events to the Linkki's Events list.
                eventsLinkki.addAll(linkkiDetailsParser.extractLinkkiEventDetails(sendHttpRequest(linkkiUrl)));

                * PÖRSSI RY
            } else if (requestUrl[i].contains("porssiry.fi")) {

                // Get just the "css-events-list" HTML div's data from Pörssi's website using jsoup library.
                * jsoup HTML parser library @ https://jsoup.org
                try {

                    Document document = Jsoup.connect(requestUrl[i]).get();

                    * https://jsoup.org/cookbook/extracting-data/selector-syntax
                    Elements eventUrlElements = document.getElementsByClass("css-events-list").select("[href]");

                    // Put the elements content (the URL's) from href fields to a String List
                    for (Element element : eventUrlElements) {
                        porssiEventUrls.add(element.attr("href"));
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Problem in jsouping Pörssi Ry's events.\n" + e);
                }

                Event porssiEvent = null;

                * Fetch each event's data using the URL array to create the Event objects.
                for (int j = 0; j < porssiEventUrls.size(); j++) {
                    porssiUrl = porssiEventUrls.get(j);

                    // Extract relevant fields from the HTTP response and create a list of Porssi's Events
                    porssiEvent = porssiDetailsParser.extractPorssiEventDetails(porssiUrl);
                    eventsPorssi.add(porssiEvent);
                }

                * DUMPPI RY
            }*/ /*else if (requestUrl[i].contains("dumppi.fi")) {

                // Get just the "css-events-list" HTML div's data from Dumppi's website using jsoup library.
                * jsoup HTML parser library @ https://jsoup.org
                try {

                    Document document = Jsoup.connect(requestUrl[i]).get();

                    * https://jsoup.org/cookbook/extracting-data/selector-syntax
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

                Event dumppiEvent = null;

                * Fetch each event's data using the URL array to create the Event objects.
                for (int j = 0; j < dumppiEventUrls.size(); j++) {
                    dumppiUrl = dumppiEventUrls.get(j);

                    // Extract relevant fields from the HTTP response and create a list of Porssi's Events
                    dumppiEvent = (extractDumppiEventDetails(porssiUrl));
                    eventsDumppi.add(dumppiEvent);
                }

            }*/

        //}

        // Add all events from different groups list's to the allEventsList
     /*   allEventsList.addAll(eventsPorssi);
        allEventsList.addAll(eventsLinkki);*/
        allEventsList.addAll(eventsDumppi);

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
