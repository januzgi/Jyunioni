package com.example.android.jyunioni;

/**
 * Created by JaniS on 5.8.2017.
 */

public class porssiDetailsParser {

    public static String extractEventInformation(String rawInformation){
        String result = null;

        // There is a mystery "null" in the beginning of "rawInformation" so take that off
        result = rawInformation.substring(rawInformation.indexOf("\n") + 1, rawInformation.length()).trim();

        // Information comes in the form where <p> element marks are still there, so get rid of them.
        // e.g. a line from rawInformation: "<p>MISSÄ: Lähtö MaD:n edestä</p>"
        result = result.replaceAll("<p>", "");
        result = result.replaceAll("</p>", "");
        result = result.replaceAll("&nbsp;", "");

        return result;
    }


    public static String extractEventName(String line){
        String result = null;

        result = line.substring(line.indexOf(">") + 1, line.lastIndexOf("<"));
        result = result.replace("&amp;", "&");

        return result;
    }


    public static String extractDate(String line){
        String result = null;

        // If the event is on many days like this: pe 15.09.2017 - su 17.09.2017
        // Then the line will be more than 155 and otherwise less than 149 in length
        if (line.length() > 155) {
            result = line.substring(line.lastIndexOf(">") + 4, line.length()).trim();
            // result now "15.09.2017 - su 17.09.2017"
            String startDay = result.substring(0, result.indexOf(" "));
            String endDay = result.substring(result.lastIndexOf(" "), result.length());
            // startDay + endDay "15.09.2017 17.09.2017"
            startDay = startDay.substring(0, startDay.lastIndexOf(".") + 1);
            endDay = endDay.substring(0, endDay.lastIndexOf(".") + 1);
            // startDay + endDay "15.09. 17.09."
            result = startDay + endDay;

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

        return result;
    }

    public static String extractTime(String line) {
        String result = null;

        result = line.substring(line.lastIndexOf(">") + 1, line.length()).trim();

        return result;
    }

}
