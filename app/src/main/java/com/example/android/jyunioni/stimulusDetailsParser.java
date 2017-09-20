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
            // Select div with id="ilmo_content"
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

            while (loopForName) {
                if (line.contains("<p class=\"tapaht_otsikko\"")) {
                    eventName = stimulusDetailsParser.extractEventName(line);
                    loopForName = false;

                    while (loopForTimestamp) {
                        if (line.contains("<h4 class=\"halffloat\">Ajankohta:")) {
                            eventTimestamp = stimulusDetailsParser.extractTimestamp(line);
                            loopForTimestamp = false;

                            while (loopForInformation) {
                                if (line.contains("<br class=\"clear\"")) {
                                    // Limit the amount of text in the event information to pElementsMax <p> elements.
                                    int pElementsCount = 0;
                                    int pElementsMax = 5;

                                    boolean loop = true;

                                    // Make a String out of the <p> content on the event specific site.
                                    while (loop) {
                                        eventInformation = eventInformation + "\n" + line.trim();
                                        line = scanner.next();

                                        // If there has been pElementsMax amount of <p> elements.
                                        pElementsCount++;
                                        if (pElementsCount == pElementsMax) loop = false;

                                        // If the text elements end, then the information we need ends.
                                        if (line.contains("<br class=\"clear\"")) loop = false;
                                    }

                                    eventInformation = stimulusDetailsParser.extractEventInformation(eventInformation);
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

        //There is a mystery "null" in the beginning of "rawInformation" so take that off
        String resultString = rawInformation.substring(rawInformation.indexOf("\n") + 1, rawInformation.length()).trim();

        resultString = resultString.replace("(In English below)", "").trim();

        // Information comes in the form where many HTML element tags are still there, so get rid of them.
        // Using jsoup: https://stackoverflow.com/questions/12943734/jsoup-strip-all-formatting-and-link-tags-keep-text-only
        // e.g. a line from rawInformation: "<p><strong>MISSÄ:</strong> Lähtö MaD:n edestä</p>"
        StringBuilder result = new StringBuilder();
        Document document = Jsoup.parse(resultString);

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
        // <p class="tapaht_otsikko" id="Fuksibondailu">Fuksibondailu</p>

        // + 13 because it counts from the first char's index.
        String result = line.substring(line.indexOf("otsikko\" id=\"") + 13, line.lastIndexOf("\">"));
        // result.equals() == "Fuksibondailu"

        result = result.replace("&amp;", "&");

        return result;
    }


    /** Extract the event's timestamp */
    public static String extractTimestamp(String line){
        // Input example:
        // <h4 class="halffloat">Ajankohta: 20.9.2017 klo. 18:00</h4>

        String result = line.substring(line.indexOf("Ajankohta:") + 11, line.lastIndexOf("</h4>"));
        // result.equals() == "20.9.2017 klo. 18:00"

        result = result.replace("klo. ", "");
        // result.equals() == "20.9.2017 18:00"

        // Replace the year away
        result = result.replaceAll("201\\d+", "");
        // result.equals() == "20.9. 18:00"

        // If there's no specified time for the event
        result = result.replaceAll("00:00", "");

        return result;
    }


}
