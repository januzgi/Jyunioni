package com.example.android.jyunioni;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Scanner;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Created by JaniS on 19.9.2017.
 *
 * Class in which the parsing methods are for the Stimulus Ry 's events.
 */
class stimulusDetailsParser {


    /**
     * Return an Event object by parsing out information from the HTTP response.
     * Event name, timestamp, general information, image ID, group's color id and event url is needed.
     *
     * Execution time faster with the while loop -structure than with if else -structure. Tested in Eclipse using the same feed
     * using System.nanoTime(): https://stackoverflow.com/questions/6646467/how-to-find-time-taken-to-run-java-program
     */
    public static Event extractStimulusEventDetails(String url) {
        // Create the Event and List<Event> objects instance
        Event event = null;

        String content = null;

        // Fetch the event's raw html data from the url
        try {
            Document document = Jsoup.connect(url).get();
            // Select div with id="breadcrump"
            content = document.select("div#ilmo_content").toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException at extractStimulusEventDetails()\n" + e);
        }

        // Check that there's content.
        if (content == null) return event;
        // Parse the data in the porssiDetailsParser class and create an Event object
        Scanner scanner = new Scanner(content).useDelimiter("[\n]");

        // Variables for the different values of an Event object
        String eventName = null;
        String eventInformation = null;
        String eventTimestamp = null;

        // Helper variables for the scanner loops
        String line;

        boolean loopForName = true;
        boolean loopForTimestamp = true;
        boolean loopForInformation = true;

        while (scanner.hasNext()) {
            line = scanner.next();
            line = scanner.next();

            while (loopForName) {
                if (line.contains("<h2 class=\"post-title\"><a href=\"")) {
                    eventName = dumppiDetailsParser.extractEventName(line);
                    loopForName = false;

                    while (loopForTimestamp) {
                        if (line.contains("<p> <strong>Päiväys ja aika</strong><br>")) {
                            eventTimestamp = dumppiDetailsParser.extractTimestamp(line);
                            loopForTimestamp = false;

                            while (loopForInformation) {
                                if (line.contains("<br style=\"clear:both\">")) {
                                    // Skip to the first line of the <p> element
                                    // where the event information is.
                                    line = scanner.next();

                                    // Limit the amount of text in the event
                                    // information to pElementsMax <p> elements.
                                    int pElementsCount = 0;
                                    int pElementsMax = 7;

                                    boolean loop = true;

                                    // Make a String out of the <p> content on
                                    // the event specific site.
                                    while (loop) {
                                        eventInformation = eventInformation + "\n" + line.trim();
                                        line = scanner.next();
                                        // If the </ul> element ends, then the information we need ends.
                                        if (line.contains("</ul>")) loop = false;

                                        // If there has been pElementsMax amount
                                        // of <p> elements.
                                        pElementsCount++;
                                        if (pElementsCount == pElementsMax) loop = false;
                                    }

                                    eventInformation = dumppiDetailsParser.extractEventInformation(eventInformation);
                                    loopForInformation = false;
                                }
                                line = scanner.next();
                            }
                        }
                        line = scanner.next();
                    }
                }
                line = scanner.next();
            }
            break;
        }

        // Create the Event with the fetched data
        event = new Event(eventName, eventTimestamp, eventInformation, R.drawable.stimulus_ry_icon, R.color.color_stimulus_ry, url);

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
        // Input example:
        // <h2 class="post-title"><a href="http://dumppi.fi/events/it-tiedekunnan-vaihtoinfoilta/">IT-tiedekunnan Vaihtoinfoilta</a></h2>

        // + 3 because it counts from the first char's index.
        String result = line.substring(line.indexOf("/\">") + 3, line.lastIndexOf("</a>"));
        // result.equals() == "IT-tiedekunnan Vaihtoinfoilta"

        result = result.replace("&amp;", "&");

        return result;
    }


    /** Extract the event's timestamp */
    public static String extractTimestamp(String line){
        // Input example:
        // <p> <strong>Päiväys ja aika</strong><br> 25.10.2017<br><i>00:00</i><br> <a href="http://dumppi.fi/events/it-tiedekunnan-vaihtoinfoilta/ical/">iCal</a> </p>

        String result = line.substring(line.indexOf("</strong><br>") + 14, line.lastIndexOf("</i><br>"));
        // result.equals() == "25.10.2017<br><i>00:00"

        result = result.replace("<br><i>", " ");
        // result.equals() == "25.10.2017 00:00"

        // Replace the year away
        result = result.replaceAll("201\\d+", "");
        // result.equals() == "25.10. 00:00"

        // If there's no specified time for the event
        result = result.replaceAll("00:00", "");

        Log.e(LOG_TAG, result);

        return result;
    }


}
