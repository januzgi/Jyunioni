package com.example.android.jyunioni;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * {@link Fragment} that displays a list of events.
 */
public class EventsFragment extends Fragment {

    /**
     * Adapter for the list of events
     */
    private EventAdapter mAdapter;

    // Private class variables to update the UI from onCreateView and updateUi methods.
    private ListView listView;
    private View rootView;

    /**
     * Required empty public constructor
     */
    public EventsFragment() {
    }


    // TODO: implementoi callbacks niin saanee tiedot tallennettua liikuttaessa eri activityjen välillä.

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.list_build, container, false);

        return rootView;

    }

    /**
     * Will be called when the view has been created.
     * Calling the AsyncTask from here.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Kick off an {@link AsyncTask} to perform the network request to get the data.
        EventsFetchingAsyncTask task = new EventsFetchingAsyncTask();
        task.execute();
    }

    @Override
    public void onStop() {
        super.onStop();
    }



    /**
     * Update the screen to display information from the given {@link ArrayList<Event>}.
     */
    public void updateUi(ArrayList<Event> events) {

/*        Log.e(LOG_TAG, "Event object contents in updateUi method at AsyncTask class:\n" +
                event.getEventName() + "\n" + event.getEventTimestamp() + "\n" + event.getEventInformation()
                + "\n" + event.getUrl() + "\n" + event.getGroupColorId() + "\n" + event.getImageResourceId());*/

        // Create an EventAdapter, whose data source is a list of Events.
        // The adapter knows how to create list items for each item in the list.
        mAdapter = new EventAdapter(getActivity(), events);

        // Find the ListView object in the view hierarchy.
        // ListView with the view ID called events_list is declared in the list_build.xml layout file.
        listView = (ListView) rootView.findViewById(R.id.events_list);

        // ListView uses the EventAdapter so ListView will display list items for each Event in the list.
        listView.setAdapter(mAdapter);

        // Set a click listener to open the event's details via an intent
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Find the current event that was clicked on
                Event currentEvent = mAdapter.getItem(position);

                // Create the intent
                Intent intent = new Intent(getContext(), EventDetails.class);

                // Get the URL so the user can be directed to right web page.
                String eventUrl = currentEvent.getUrl();

                // Get the current event's image resource id so the right image can be displayed in the details view.
                int eventImageId = currentEvent.getImageResourceId();

                // Get event's name, timestamp and information
                String eventName = currentEvent.getEventName();
                String eventTimestamp = currentEvent.getEventTimestamp();
                String eventInformation = currentEvent.getEventInformation();

                // Add the data to the intent so it can be used in the activity.
                intent.putExtra("EVENT_NAME", eventName);
                intent.putExtra("EVENT_TIMESTAMP", eventTimestamp);
                intent.putExtra("IMAGE_ID", eventImageId);
                intent.putExtra("EVENT_URL", eventUrl);
                intent.putExtra("EVENT_INFORMATION", eventInformation);

                startActivity(intent);
            }
        });
    }


    /**
     * Created by JaniS on 26.7.2017.
     * <p>
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first event in the response.
     *
     * Runs multiple times at the moment, which is not good. Should run only in the startup of the app.
     */
    private class EventsFetchingAsyncTask extends AsyncTask<URL, Void, ArrayList<Event>> {

        /**
         * Linkki Jyväskylä Ry events page URL.
         */
        private final String LINKKI_EVENTS_URL = "http://linkkijkl.fi/events/2017-09/?ical=1&tribe_display=month";


        /**
         * This is done in a background thread.
         */
        @Override
        protected ArrayList<Event> doInBackground(URL... urls) {

            // Create an ArrayList<Event> object instance
            ArrayList<Event> events;

            // Create URL object for fetching the Linkki Jyväskylä Ry event's
            URL url = createUrl(LINKKI_EVENTS_URL);

            // Perform HTTP request to the URL and receive a response
            String response = "";
            try {
                response = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException when making the HTTP request in doInBackground at EventActivity" + e);
            }

            // Extract relevant fields from the HTTP response and create an Event object
            // updateUi gets the result Event object via the onPostExecute().
            events = extractDetails(response);

            // Return the Event object as the result for the EventAsyncTask
            return events;
        }



        /**
         * Update the screen with the given event (which was the result of the {@link EventsFetchingAsyncTask}).
         * Runs in the UI thread.
         * Gets the result from the population done in doInBackground().
         */
        @Override
        protected void onPostExecute(ArrayList<Event> events) {
            if (events == null) {
                return;
            }

            updateUi(events);
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
         * Return an {@link ArrayList<Event>} object by parsing out information from the HTTP response.
         * Event image, name, timestamp, general information, url and group's color id is needed.
         */
        private ArrayList<Event> extractDetails(String httpResponseString) {
            // TODO: Entä jos kuussa ei olekaan tapahtumia

            // TODO: kuinka pitkälle tulevaisuuteen näytetään

            // TODO: URL:in muokkaus pitää automatisoida. Eli jos ei ole tässä kuussa tapahtumia, kokeile seuraavaa kuuta,
            // TODO: jos se ei ole validi niin onko parempi vain luoda objekti, joka sanoo, että katso
            // TODO: tulevia tapahtumia Linkin kalenterista ja ohjata Linkin tapahtumien sivulle?


            // Create the Event and ArrayList<Event> objects instance
            ArrayList<Event> extractedEvents = new ArrayList<>();

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


            // When there's still text left in the scanner
            while (fieldsScanner.hasNext() && eventsCount > 0) {

                // Get the different fields information to desired String arrays,
                // from which they can easily be matched up.
                // Add the fields to the "results" string array
                for (int i = 0; i < eventsCount; i++) {

                    // Use the scanner to parse the details of each event
                    line = fieldsScanner.next();

                    // Event's starting time
                    if (line.contains("DTSTART;")) { eventTimeStart = Parser.extractTime(line); }

                    // Event's ending time
                    else if (line.contains("DTEND;")) { eventTimeEnd = Parser.extractTime(line);
                        // Get the timestamp from the starting and ending times of the event
                        eventTimestamp = Parser.checkEventTimestamp(eventTimeStart, eventTimeEnd); }


                    // Event's name
                    else if (line.contains("SUMMARY")) { eventName = Parser.extractField(line); }


                    // Event's description / overall information
                    else if (line.contains("DESCRIPTION:")) { eventInformation = Parser.extractDescriptionField(line); }


                    // Event's URL
                    // Skip the first URL, which is the "X-ORIGINAL-URL:" and add only the 'events' to the list
                    else if (line.contains("URL") && line.contains("event")) { eventUrl = Parser.extractUrl(line);

                        // Match up the event's group image and color according to the URL where the info was extracted from
                        if (eventUrl.contains("linkkijkl")) {
                            eventImageId = R.drawable.linkki_jkl_icon;
                            eventGroupColorId = R.color.color_linkki_jkl;
                        }
                    }


                    // If this was the end of the event, add the event details to an object in events arraylist
                    else if (line.contains("END:VEVENT")) {

                        /*Log.e(LOG_TAG, "\nEvent at ArrayList(" + loopCount + ") name: " + eventName +
                                "\nEvent at ArrayList(" + loopCount + ") timestamp: " + eventTimestamp +
                                "\nEvent at ArrayList(" + loopCount + ") information: " + eventInformation +
                                "\nEvent at ArrayList(" + loopCount + ") URL: " + eventUrl +
                                "\nEvent at ArrayList(" + loopCount + ") image id: " + eventImageId +
                                "\nEvent at ArrayList(" + loopCount + ") group color id: " + eventGroupColorId);*/

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

    }


}