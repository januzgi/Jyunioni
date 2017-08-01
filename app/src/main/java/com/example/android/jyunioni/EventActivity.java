/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.jyunioni;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

public class EventActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        CategoryAdapter adapter = new CategoryAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()
        tabLayout.setupWithViewPager(viewPager);


        // Kick off an {@link AsyncTask} to perform the network request to get the data.
 /*       EventsFetchingAsyncTask task = new EventsFetchingAsyncTask();
        task.execute();*/

    }


    /**
     * Update the screen to display information from the given {@link Event}.
     */
    public void updateUi(Event event) {

        Log.e(LOG_TAG, "Event object contents in updateUi method at AsyncTask class:\n" +
                event.getEventName() + "\n" + event.getEventTimestamp() + "\n" + event.getEventInformation()
                + "\n" + event.getUrl() + "\n" + event.getGroupColorId() + "\n" + event.getImageResourceId());
    }



    /**
     * Created by JaniS on 26.7.2017.
     * <p>
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first event in the response.
     *
     * Runs multiple times at the moment, which is not good. Should run only in the startup of the app.
     */
    private class EventsFetchingAsyncTask extends AsyncTask<URL, Void, Event> {


        /**
         * Linkki Jyväskylä Ry events page URL.
         */
        private final String LINKKI_EVENTS_URL = "http://linkkijkl.fi/events/?ical=1&tribe_display=month";


        /**
         * AsyncTask method call in EventActivity.java
         *
         */
        @Override
        protected Event doInBackground(URL... urls) {

            // Create an Event object instance
            Event event;

            // Create URL object for fetching the Linkki Jyväskylä Ry event's
            URL url = createUrl(LINKKI_EVENTS_URL);

            // Perform HTTP request to the URL and receive a response
            String response = "";
            try {
                response = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException when making the HTTP request in doInBackground at EventActivity" + e);
            }

            // Extract relevant fields from the HTTP response and create an {@link Event} object
            // updateUi gets the result Event object via the onPostExecute().
            event = extractDetails(response);

            // Return the {@link Event} object as the result for the {@link EventAsyncTask}
            return event;
        }



        /**
         * Update the screen with the given event (which was the result of the {@link EventsFetchingAsyncTask}).
         * Runs in the UI thread.
         * Gets the result from the population done in doInBackground().
         */
        @Override
        protected void onPostExecute(Event event) {
            if (event == null) {
                return;
            }

            updateUi(event);
        }


        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url;

            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            if (url == null) return "";

            String response = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000  /*milliseconds*/);
                urlConnection.setConnectTimeout(15000  /*milliseconds*/);
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    // If HTTP request was succesful
                    inputStream = urlConnection.getInputStream();
                    response = readFromStream(inputStream);
                } else {
                    // If the response code was != 200
                    Log.e(LOG_TAG, "HTTP request response code wasn't 200 (OK), but instead: " + Integer.toString(urlConnection.getResponseCode()));
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "IOexception message when making HTTP request." +
                        "\n(This comes from the catch block inside the method makeHttpRequest in EventActivity.): " + e);
            } finally {
                if (urlConnection != null) {
                    // Anyhow disconnect when finished.
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // Close the inputstream from reserving resources
                    inputStream.close();
                }
            }
            return response;
        }


        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole HTTP response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    // Add a newline so the output won't be at one line.
                    output.append(line + "\n");
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return an {@link Event} object by parsing out information from the HTTP response.
         * Event image, name, timestamp, general information url and group's color id is needed.
         */
        private Event extractDetails(String httpResponseString) {

            // TODO: eventit extractDetailsiin arraylistissä, josta sitten päivitetään UI sen mukaan paljon niitä löytyy.

            // Create the Event object instance
            Event currentEvent = new Event("Esmes tapahtuma", "esmes päivämäärä", "esmes lisätietoa",
                    R.drawable.linkki_jkl_icon, R.color.color_linkki_jkl, "http://linkkijkl.fi/");

            // Helper variable for the scanner loops
            String line = null;

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


            // Create string arrays for the different fields that are extracted from
            // the HTTP response string
            String eventTimeStart = "";
            String eventTimeEnd = "";
            String[] eventTimestamp = new String[eventsCount];

            String[] eventName = new String[eventsCount];
            String[] eventInformation = new String[eventsCount];
            String[] eventUrl = new String[eventsCount];


            // Scan through the fields and add the contents to the corresponding
            // String arrays. Use 'newline' as a limiter to go to nextLine().
            Scanner fieldsScanner = new Scanner(httpResponseString).useDelimiter("[\n]");

            // When the for -loop has been done (one event has been extracted, this
            // adds by one)
            int loopCount = 0;

            // When there's still text left in the scanner
            while (fieldsScanner.hasNext()) {

                // Get the different fields information to desired String arrays,
                // from which they can easily be matched up.
                // Add the fields to the "results" string array
                for (int i = 0; i < eventsCount; i++) {

                    // Use the scanner to parse the details of each event
                    line = fieldsScanner.next();

                    // Event's starting time
                    if (line.contains("DTSTART;")) {
                        eventTimeStart = Parser.extractTime(line);

                        // Event's ending time
                    } else if (line.contains("DTEND;")) {
                        eventTimeEnd = Parser.extractTime(line);

                        // Get the timestamp from the starting and ending times of the event
                        eventTimestamp[i] = eventTimeStart + " - " + eventTimeEnd;

                        currentEvent.setEventTimestamp(eventTimestamp[i]);

                        // Event's name
                    } else if (line.contains("SUMMARY")) {
                        eventName[i] = Parser.extractField(line);

                        currentEvent.setEventName(eventName[i]);

                        // Event's description / overall information
                    } else if (line.contains("DESCRIPTION:")) {
                        eventInformation[i] = Parser.extractDescriptionField(line);

                        currentEvent.setEventInformation(eventInformation[i]);

                        // Event's URL
                        // Skip the first URL, which is the "X-ORIGINAL-URL:"
                    } else if (line.contains("URL") && i != 0) {
                        eventUrl[i] = Parser.extractUrl(line);

                        currentEvent.setUrl(eventUrl[i]);

                        // Match up the event's group image and color according to the URL where the info was extracted from
                        if (currentEvent.getUrl().contains("linkkijkl")) {
                            currentEvent.setImageResourceId(R.drawable.linkki_jkl_icon);
                            currentEvent.setGroupColorId(R.color.color_linkki_jkl);
                        }

                    }

                    // If this was the end of the event being extracted
                    if (line.contains("END:VEVENT")) {
                        // If there is the "event end" then exit the for loop back to
                        // the while loop
                        loopCount++;
                        break;
                    }

                }

                // If the loop has gone through all the events
                if (loopCount == eventsCount) break;
            }

            return currentEvent;

        }

    }

}


/**
 * Class in which the parsing methods are for the Linkki Jyväskylä Ry's events.
 */
class Parser {


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




/*    // TODO: Create a duplicate arraylist of events just to see if it works for the eventsfragment.java
    public ArrayList<Event> createFakeList(Event event){
        ArrayList<Event> duplicateEvents = new ArrayList<>();

        for (int i = 0; i < 10; i++){
            duplicateEvents.add(event);
        }


        return duplicateEvents;
    }

    public ArrayList<Event> getFakeList(){
        return duplicateEvents;
    }*/


/*    // Set the according items to the right views.
        eventNameTextView.setText(event.getEventName());
        eventTimestampTextView.setText(event.getEventTimestamp());
        eventsGroupImageView.setBackgroundResource(event.getImageResourceId());

        // Set the theme color for the list item, find id first
        // Find the color that the resource ID maps to and
        // set the background color of the text container View
        textContainer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), event.getGroupColorId()));*/