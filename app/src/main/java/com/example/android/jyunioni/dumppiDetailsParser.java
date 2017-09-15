package com.example.android.jyunioni;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Created by JaniS on 14.9.2017.
 */

class dumppiDetailsParser {



    /**
     * Return an Event object by parsing out information from the HTTP response.
     * Event name, timestamp, general information, image ID, group's color id and event url is needed.
     */
    public static Event extractDumppiEventDetails(String url) {
        // Create the Event and List<Event> objects instance
        Event event = null;

        String content = null;

        /** http://dumppi.fi/events/3miot-22-fuksishower/ */

        // Fetch the event's raw html data from the url
        try {
            Document document = Jsoup.connect(url).get();

            // Select div with class="uutinen"
            content = document.select("div.uutinen").first().toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException at extractPorssiEventDetails()\n" + e);
        }

        Log.e(LOG_TAG, content);

/*
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
                if (line.contains("<p><img ")) {
                    line = scanner.next();
                }

                // Limit the amount of text in the event information to pElementsMax <p> elements.
                int pElementsCount = 0;
                int pElementsMax = 7;

                boolean pElements = true;

                // Make a String out of the <p> content on the event specific site.
                while (pElements) {
                    eventInformation = eventInformation + "\n" + line.trim();
                    line = scanner.next();
                    // If the <p> element still continues
                    if (line.contains("<p>") == false) pElements = false;

                    // If there has been pElementsMax amount of <p> elements.
                    pElementsCount++;
                    if (pElementsCount == pElementsMax) pElements = false;
                }

                eventInformation = porssiDetailsParser.extractEventInformation(eventInformation);

            }
        }

        // Create the Event with the fetched data
        event = new Event(eventName, eventTimestamp, eventInformation, R.drawable.dumppi_ry_icon, R.color.color_dumppi_ry, url);
*/
        return event;
    }


}
