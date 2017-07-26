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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

public class EventActivity extends AppCompatActivity {


    /**
     * Linkki Jyväskylä Ry events page URL.
     */
    private final String LINKKI_EVENTS_URL = "http://linkkijkl.fi/events/?ical=1&tribe_display=month";

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    /**
     * Progressbar to be shown when fetching data.
     */
    private ProgressBar mProgressBar;

    /**
     * Adapter for the list of events
     */
    private EventAdapter mAdapter;


    /**
     * Private variables for updating the UI with the information from the Event object.
     * Organizing groups image & color and event name and timestamp needed.
     */
    private String eventName = null;
    private String eventTimestamp = null;
    private int groupImageId;
    private int groupColorId;



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



       /* // Get the instances of the needed views.
        eventNameTextView = (TextView) findViewById(R.id.event_name_text_view);
        eventTimestampTextView = (TextView) findViewById(R.id.event_timestamp_text_view);
        eventsGroupImageView = (ImageView) findViewById(R.id.group_image);


        Event event = new Event("Dumpin syysrieha", "2.12.2017", "Bileet on isot.",
                R.drawable.dumppi_ry_icon, R.color.color_dumppi_ry, "http://linkkijkl.fi/event/baarikierros/");

        updateUi(event);*/


     /*   // Kick off an {@link AsyncTask} to perform the network request to get the data.
        EventAsyncTask task = new EventAsyncTask();
        task.execute();*/
    }


    /**
     * Update the screen to display information from the given {@link Event}.
     */
    public void updateUi(Event event) {

        /*Log.e(LOG_TAG, event.toString() + "Event toString() in EventActivity updateUi method.");*/

        // Get the event's host groups image id.
        groupImageId = event.getImageResourceId();

        // Get the event's name and the timestamp.
        eventName = event.getEventName();
        eventTimestamp = event.getEventTimestamp();

        // Get the organizing group's color id.
        groupColorId = event.getGroupColorId();


/*        // Set the according items to the right views.
        eventNameTextView.setText(eventName);

        eventTimestampTextView.setText(eventTimestamp);

        eventsGroupImageView.setBackgroundResource(groupImageId);*/

        /*// Set the theme color for the list item, find id first
        View textContainer = listItemView.findViewById(R.id.text_container);

        // Find the color that the resource ID maps to and
        // set the background color of the text container View
        textContainer.setBackgroundColor(ContextCompat.getColor(getContext(), currentEvent.getGroupColorId()));*/
    }


    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class EventAsyncTask extends AsyncTask<URL, Void, Event> {

        @Override
        protected Event doInBackground(URL... urls) {

            // Create an Event object instance
            Event event = null;

            // Create URL object
            URL url = createUrl(LINKKI_EVENTS_URL);

            // Perform HTTP request to the URL and receive a response
            String response = "";
            try {
                response = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException when making the HTTP request in doInBackground at EventActivity");
            }

            // Create an InputStream object for the extractor to use.
            InputStream inputStream = null;

            try {
                Log.e(LOG_TAG, "Extracting features soon.");

                // Extract relevant fields from the HTTP response and create an {@link Event} object
                event = extractDetails(response, inputStream);

                // Update the UI with the information
                // updateUi(event);

                // Return the {@link Event} object as the result for the {@link EventAsyncTask}
                return event;
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException when extracting the HTTP response fields.");
            }

            return event;
        }

        /**
         * Update the screen with the given event (which was the result of the {@link EventAsyncTask}).
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
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return an {@link Event} object by parsing out information of the HTTP response.
         */
        private Event extractDetails(String httpResponseString, InputStream inputStream) throws IOException {
            // TODO: parametrit kohdilleen, että täsmää Event konstruktoria ja et toimii eri teksti ja kuvakenttien fetchaus.

            // There is output from the website and it seems to work in that sense.
            Log.e(LOG_TAG, httpResponseString);


            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }


            return null;

            // return output.toString();

/*
            // Extract the wanted information from the fetched .ics file which is a String now.
            try {
                File icsFile = new File(icsFileName);
                Scanner fileScanner = new Scanner(icsFile).useDelimiter("[\n]");
                //skip past meta data
                fileScanner.next();
                fileScanner.next();
                fileScanner.next();

                String summary = null;
                String startTime = null;
                String endTime = null;
                String location = null;

                //Parse all necessary data
                while (fileScanner.hasNext()) {
                    String line = fileScanner.next();

                    if (line.toLowerCase().contains("summary")) {
                        summary = ICSParser.extractText(line);
                    } else if (line.toLowerCase().contains("start")) {
                        startTime = ICSParser.extractTime(line);
                    } else if (line.toLowerCase().contains("end")) {
                        endTime = ICSParser.extractTime(line);
                    } else if (line.toLowerCase().contains("location")) {
                        location = ICSParser.extractText(line);

                        events.add(new IcsEvent(summary, startTime, endTime, location));
                        summary = null;
                        startTime = null;
                        endTime = null;
                        location = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/


            // if empty or null then return
            /*if (TextUtils.isEmpty(earthquakeJSON)) return null;

            try {
                JSONObject baseJsonResponse = new JSONObject(earthquakeJSON);
                JSONArray featureArray = baseJsonResponse.getJSONArray("features");

                // If there are results in the features array
                if (featureArray.length() > 0) {
                    // Extract out the first feature (which is an earthquake)
                    JSONObject firstFeature = featureArray.getJSONObject(0);
                    JSONObject properties = firstFeature.getJSONObject("properties");

                    // Extract out the title, time, and tsunami values
                    String title = properties.getString("title");
                    long time = properties.getLong("time");
                    int tsunamiAlert = properties.getInt("tsunami");

                    // Create a new {@link Event} object
                    return new Event(title, time, tsunamiAlert);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
            }*/
        }
    }
}

/**
 * Extracts dates and text from .ICS file
 *
 * @author Douglas Rudolph
 */
class ICSParser {

    /**
     * @param line: .ics encoded line
     * @return text: return text - either event or name
     */
    public static String extractText(String line) {
        line = line.replaceAll("[ ]+", " ");
        String[] text = line.split("[ ]");

        String str = "";
        for (int i = 1; i < text.length; i++) {
            str += " " + text[i];
        }
        str = str.trim();

        return str;
    }

    /**
     * @param line: .ICS encoded line
     * @return return: Time of event in JSON Date format
     */
    public static String extractTime(String line) {
        line = line.replaceAll("[ ]+", " ");
        String[] text = line.split("[ ]");

        String startTimeStr;
        if (text.length == 3)
            startTimeStr = text[1] + "T" + text[2].trim();
        else
            startTimeStr = text[1].trim();

        return startTimeStr;
    }

}

/**
 * Object to represent each IcsEvent on a Calendar
 *
 * @author Douglas Rudolph
 */
class IcsEvent {
    public String summary;
    public String startTime;
    public String endTime;
    public String location;

    public IcsEvent(String summary, String startTime, String endTime, String location) {
        this.summary = summary;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    @Override
    public String toString() {
        return summary + "|" + startTime + "|" + endTime + "|" + location + "\n";
    }

}
