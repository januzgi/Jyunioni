package com.example.android.jyunioni;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Created by JaniS on 5.8.2017.
 *
 * Parser class to get the event's data from Pörssi Ry website's HTTP response.
 * Scanner scans through the response and this class gets one line of that response to according method for parsing.
 */
public class porssiDetailsParser {

    /** Extract the event's overview / description information */
    public static String extractEventInformation(String rawInformation){
        String result = null;

        // There is a mystery "null" in the beginning of "rawInformation" so take that off
        result = rawInformation.substring(rawInformation.indexOf("\n") + 1, rawInformation.length()).trim();

        // Information comes in the form where <p> element marks are still there, so get rid of them.
        // e.g. a line from rawInformation: "<p>MISSÄ: Lähtö MaD:n edestä</p>"
        result = result.replaceAll("<p>", "");
        result = result.replaceAll("</p>", "");
        result = result.replaceAll("&nbsp;", "");

        // In case there is no content in the information field.
        if (result.length() < 10 || result.contains(("<")) || result.contains(">")){
            result = "Katso lisätiedot tapahtumasivulta.";
        }

        return result;
    }


    /** Extract the event's name */
    public static String extractEventName(String line){

        String result = line.substring(line.indexOf(">") + 1, line.lastIndexOf("<"));
        result = result.replace("&amp;", "&");

        // If line is longer than 20 then split into two lines
        if (result.length() > 25){
            result = result.substring(0, result.lastIndexOf(" ")) + "\n" + result.substring(result.lastIndexOf(" "), result.length()).trim();
        }

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
