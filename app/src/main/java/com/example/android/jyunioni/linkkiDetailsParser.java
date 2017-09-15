package com.example.android.jyunioni;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by JaniS on 1.8.2017.
 * Class in which the parsing methods are for the Linkki Jyväskylä Ry's events.
 */
class linkkiDetailsParser {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = EventDetails.class.getSimpleName();


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
     * Extract timestamps from the HTTP response.
     *
     * @param line Line of HTTP response from the scanner.
     * @return Formatted timestamp.
     */
    public static String extractTime(String line) {

        String date;
        String time;
        String result = "";

        line = extractField(line);
        // Line is now this format: "20170723T170000" or "20170829"

        // If the line contains only the date of the event and no hours, for example: "20170723T170000"
        if (line.contains("T")){
            date = line.substring(0, line.indexOf('T'));
            time = line.substring(line.indexOf('T') + 1, line.indexOf('T') + 5);
            // date + " " + time --- is now this format: 20170723 1700

            SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyyMMdd HHmm", Locale.ENGLISH);
            SimpleDateFormat newFormat = new SimpleDateFormat("d.M. HH:mm", Locale.ENGLISH);
            try {
                Date timestamp = defaultFormat.parse(date + " " + time);
                result = newFormat.format(timestamp);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Problem in parsing the dates at extractTime method in linkkiDetailsParser class.");
            }

            return result;
        }

        // If the line is without the 'T', for example: "20170829"
        // then format the date without the time as follows.
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        SimpleDateFormat newFormat = new SimpleDateFormat("d.M.", Locale.ENGLISH);
        try {
            Date timestamp = defaultFormat.parse(line);
            result = newFormat.format(timestamp);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Problem in parsing the dates at extractTime method in linkkiDetailsParser class.");
        }

        return result;
    }

    /**
     * Check if the event happens only on one day. If, then use the date on the startTime only.
     *
     * Example input: "7.9. 17:00", "7.9. 23:30" , Example output: "7.9. 17:00 - 23:30"
     *
     * @param startTime Events starting date and time.
     * @param endTime Events ending date and time.
     * @return Timestamp of the event without multiple same date.
     */
    public static String checkEventTimestamp(String startTime, String endTime){
        String result = startTime + " - " + endTime;

        String startDate = startTime.substring(0, startTime.lastIndexOf("."));
        String endDate = endTime.substring(0, endTime.lastIndexOf("."));

        // Check if it's only one day event.
        if (startDate.equals(endDate)){
            // Check if the event doesn't have starting or ending hours. Then just return one date.
            if (!startTime.contains(":") || !endTime.contains(":")) return startTime + " ";

            result = startTime + " - " + endTime.substring(endTime.lastIndexOf(".") + 2, endTime.length());
        }

        return result;
    }

    /**
     * Extract a field from HTTP response with the simplest "after ':'" rule.
     *
     * @param line Line of HTTP response from the scanner.
     * @return Substring of the line after the ':'
     */
    public static String extractField(String line) {
        // Return the string after the ':'
        return line.substring(line.lastIndexOf(':') + 1);
    }

    /**
     * Parses the URL.
     *
     * @param line Line of HTTP response from the scanner.
     * @return Substring with the rightly formatted URL to be used for web intent later.
     */
    public static String extractUrl(String line) {
        // Get the string after the first ':'
        return line.substring(line.indexOf(':') + 1);
    }

    /**
     * Extracts the event information field. Some additional newlines seem to come with the HTTP string so they are cut off.
     *
     * @param line Line of HTTP response from the scanner.
     * @return Clear text with newlines as they should be.
     */
    public static String extractDescriptionField(String line) {
        // Get the string after the ':'
        String result = line.substring(line.indexOf(':') + 1);
        result = result.replaceAll("\\\\n", "\n");
        result = result.replaceAll("\\\\,", ",");

        return result;
    }


}