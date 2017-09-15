package com.example.android.jyunioni;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Created by JaniS on 5.8.2017.
 *
 * Parser class to get the event's data from Pörssi Ry website's HTTP response.
 * Scanner scans through the response and this class gets one line of that response to according method for parsing.
 */
class porssiDetailsParser {


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
        event = new Event(eventName, eventTimestamp, eventInformation, R.drawable.porssi_ry_icon, R.color.color_porssi_ry, url);

        return event;
    }


    /** Extract the event's overview / description information */
    public static String extractEventInformation(String rawInformation){

        // There is a mystery "null" in the beginning of "rawInformation" so take that off
        rawInformation = rawInformation.substring(rawInformation.indexOf("\n") + 1, rawInformation.length()).trim();

        // Information comes in the form where many HTML element tags are still there, so get rid of them.
        // Using jsoup: https://stackoverflow.com/questions/12943734/jsoup-strip-all-formatting-and-link-tags-keep-text-only
        // e.g. a line from rawInformation: "<p>MISSÄ: Lähtö MaD:n edestä</p>"
        StringBuilder result = new StringBuilder();
        Document document = Jsoup.parse(rawInformation);

        for (Element element : document.select("p"))
        {
            result.append(element.text() + "\n");
        }

        // In case there is no content in the information field.
        if (result.length() < 2){
            return result.append("Katso lisää tapahtumasivulta!").toString();

        }

        result.append("..." + "\n"
                + "..." + "\n"
                + "Katso lisää tapahtumasivulta!");

        return result.toString();
    }


    /** Extract the event's name */
    public static String extractEventName(String line){

        String result = line.substring(line.indexOf(">") + 1, line.lastIndexOf("<"));
        result = result.replace("&amp;", "&");

        return result;
    }


    /** Extract the event's date */
    public static String extractDate(String line){
        String result;

        // Trim the line in case of extra spaces
        line = line.trim();

        // TODO: Timestamp korjaten, että alkamisaika menee alkamispäivälle ja loppumisaika loppumispäivälle.

        // If the event is on many days like this: pe 15.09.2017 - su 17.09.2017
        // Then the line will be more than 155 and otherwise less than 149 in length
        if (line.length() > 80) {
            result = line.substring(line.lastIndexOf(">") + 4, line.length()).trim();
            // result now "15.09.2017 - su 17.09.2017"
            String startDay = result.substring(0, result.indexOf(" "));
            String endDay = result.substring(result.lastIndexOf(" "), result.length());
            // startDay + endDay "15.09.2017 17.09.2017"
            startDay = startDay.substring(0, startDay.lastIndexOf(".") + 1);
            endDay = endDay.substring(0, endDay.lastIndexOf(".") + 1);
            // startDay + endDay "15.09. 17.09."

            // Format startDay and endDay to not contain extra 0's.
            try {
                SimpleDateFormat newFormat = new SimpleDateFormat("d.M.");
                Date startDate = newFormat.parse(startDay);
                Date endDate = newFormat.parse(endDay);
                result = newFormat.format(startDate) + " - " + newFormat.format(endDate);
            } catch (ParseException e){
                Log.e(LOG_TAG, "Parsing problem at extractDate().\n" + e);
            }
            // "15.9. - 17.9."

            return result;
        }

        // Default is that it's a one day event.
        result = line.substring(line.lastIndexOf(">"), line.length()).trim();
        // result now "> ti 29.08.2017"
        result = result.substring(result.lastIndexOf(" ") + 1, result.length());
        // result now "29.08.2017"
        // Only the date needed
        result = result.substring(0, result.lastIndexOf(".") + 1);
        // result now "29.08."

        try {
            SimpleDateFormat newFormat = new SimpleDateFormat("d.M.");
            Date startDate = newFormat.parse(result);
            result = newFormat.format(startDate);
        } catch (ParseException e){
            Log.e(LOG_TAG, "Parsing problem at extractDate().\n" + e);
        }
        // result now "29.8."


        return result;
    }

    /** Extract the event's time */
    public static String extractTime(String line) {

        String result = line.substring(line.lastIndexOf(">") + 1, line.length()).trim();
        // If "00:00" then leave the time blank
        if (result.equals("00:00")) result = "";

        return result;
    }

}
