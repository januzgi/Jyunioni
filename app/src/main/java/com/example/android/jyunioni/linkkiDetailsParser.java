package com.example.android.jyunioni;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by JaniS on 1.8.2017.
 * Class in which the parsing methods are for the Linkki Jyväskylä Ry's events.
 */
public class linkkiDetailsParser {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = EventDetails.class.getSimpleName();


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