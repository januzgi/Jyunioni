package com.example.android.jyunioni;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Created by JaniS on 1.8.2017.
 * Class in which the parsing methods are for the Linkki Jyväskylä Ry's events.
 */
public class Parser {


    public static String extractTime(String line) {
        String date = "";
        String time = "";
        String result = "Katso tiedot.";

        line = extractField(line);
        // Line is now this format: 20170723T170000

        date = line.substring(0, line.indexOf('T'));
        time = line.substring(line.indexOf('T') + 1, line.indexOf('T') + 5);
        // date + " " + time --- is now this format: 20170723 1700

        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyyMMdd hhmm");
        SimpleDateFormat newFormat = new SimpleDateFormat("d.M. HH:mm");

        try {
            Date timestamp = defaultFormat.parse(date + " " + time);
            result = newFormat.format(timestamp);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Problem in parsing the dates at extractTime method in Parser class.");
        }

        return result;
    }

    public static String extractField(String line) {
        // Get the string after the ':'
        String result = line.substring(line.lastIndexOf(':') + 1);

        return result;
    }

    public static String extractUrl(String line) {
        // Get the string after the ':'
        String result = line.substring(line.lastIndexOf(':') + 3);

        return result;
    }

    public static String extractDescriptionField(String line) {
        // Get the string after the ':'
        String result = line.substring(line.indexOf(':') + 1);
        result = result.replaceAll("\\\\n", "\n");
        result = result.replaceAll("\\\\,", ",");

        return result;
    }

}