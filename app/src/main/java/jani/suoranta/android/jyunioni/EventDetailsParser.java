package jani.suoranta.android.jyunioni;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by JaniS on 1.8.2017.
 * Class in which the parsing methods are for different group's events.
 *
 * @author Jani Suoranta 25.11.2017
 */
class EventDetailsParser {


    /**
     * Return an {@link List<Event>} object by parsing out information from the .txt.
     * Event name, timestamp, general information, image ID, group's color id and event url is needed.
     */
    static List<Event> extractEventDetails(String httpResponseString) {
        // Create List<Event> object instance
        List<Event> extractedEvents = new ArrayList<>();

        // If the response is empty or null, then return early.
        if (httpResponseString.equals("")) {
            extractedEvents.add(new Event("Internet yhteys vajaa.",
                    "Tarkista verkon toimivuus.", "Testaa pääsetkö Googleen.",
                    jani.suoranta.android.jyunioni.R.drawable.default_icon,
                    jani.suoranta.android.jyunioni.R.color.primary_color, "https://www.google.fi"));
            return extractedEvents;
        }

        // Helper variable for the scanner loops
        String line;

        // Variables for the different values of an Event object
        String eventName, eventTimestamp, eventUrl, eventInformation;
        int eventIcon, eventGroupColor;

        // Create a scanner and loop through the string to count the amount of separate events in the string
        Scanner eventsCountScanner = new Scanner(httpResponseString).useDelimiter("[\n]");

        // Create a variable to initialize right size string arrays later
        int eventsCount = 0;

        // Check which group's event it is (it's written on the first line of the response) and add according icon and color
        String whichGroupsEvent;
        whichGroupsEvent = eventsCountScanner.next();

        switch (whichGroupsEvent) {
            case "LINKKI":
                eventIcon = jani.suoranta.android.jyunioni.R.drawable.linkki_jkl_icon;
                eventGroupColor = jani.suoranta.android.jyunioni.R.color.color_linkki_jkl;
                break;
            case "PORSSI":
                eventIcon = jani.suoranta.android.jyunioni.R.drawable.porssi_ry_icon;
                eventGroupColor = jani.suoranta.android.jyunioni.R.color.color_porssi_ry;
                break;
            case "DUMPPI":
                eventIcon = jani.suoranta.android.jyunioni.R.drawable.dumppi_ry_icon;
                eventGroupColor = jani.suoranta.android.jyunioni.R.color.color_dumppi_ry;
                break;
            case "STIMULUS":
                eventIcon = jani.suoranta.android.jyunioni.R.drawable.stimulus_ry_icon;
                eventGroupColor = jani.suoranta.android.jyunioni.R.color.color_stimulus_ry;
                break;
            default:
                eventIcon = jani.suoranta.android.jyunioni.R.drawable.default_icon;
                eventGroupColor = jani.suoranta.android.jyunioni.R.color.primary_color;
        }


        // If the line contains the beginning of a new event, then add one to
        // the events counter.
        while (eventsCountScanner.hasNext()) {
            line = eventsCountScanner.next();
            if (line.contains("eventName: "))
                eventsCount++;
        }

        // Create a scanner and loop through the string extracting each event's details
        Scanner scanner = new Scanner(httpResponseString).useDelimiter("[\n]");

        // Loop through all the separate event's in the file.
        // Find Event fields in order: eventName, eventTimestamp, eventUrl, eventInformation
        for (int i = 0; i < eventsCount; i++) {
            line = scanner.next();

            while (true) {
                if (line.contains("eventName: ")) {
                    eventName = extractField(line);

                    while (true) {
                        if (line.contains("eventTimestamp: ")) {
                            eventTimestamp = extractField(line);

                            while (true) {
                                if (line.contains("eventUrl: ")) {
                                    eventUrl = extractField(line);

                                    while (true) {
                                        if (line.contains("eventInformation: ")) {
                                            // Empty the eventInformation from the previous event's information.
                                            eventInformation = "";

                                            while (!line.contains("END_OF_EVENT")) {
                                                // If the event information ends then parse the eventInformation and break the loop
                                                // While the line isn't "END_OF_EVENT", add the information to the eventInformation
                                                eventInformation += line + "\n";
                                                line = scanner.next();
                                            }

                                            // Extract the events information
                                            eventInformation = extractInformation(eventInformation);
                                            break;
                                        }
                                        line = scanner.next();

                                    }
                                    break;
                                }
                                line = scanner.next();

                            }
                            break;

                        }
                        line = scanner.next();

                    }
                    break;
                }
                line = scanner.next();

            }

            // Add event to the list
            extractedEvents.add(new Event(eventName, eventTimestamp, eventInformation, eventIcon, eventGroupColor, eventUrl));
        }

        return extractedEvents;
    }


    /**
     * @param line Line from the scanner.
     * @return Substring of the line after the ':'. Event details from the .txt file.
     */
    private static String extractField(String line) {
        // Return the string after the ':'
        // Example input: "eventName: Chill IoT-workshop"
        // Example output: "Chill IoT-workshop"
        return line.substring(line.indexOf(':') + 2);
    }


    /**
     * Extracts the event information field. Some additional newlines seem to come with the HTTP string so they are cut off.
     *
     * @param rawInformation Line of HTTP response from the scanner.
     * @return Clear text with newlines as they should be.
     */
    private static String extractInformation(String rawInformation) {
        // Get the string after the ':', also trim() the result.
        return rawInformation.substring(rawInformation.indexOf(':') + 1, rawInformation.length()).trim();
    }

}