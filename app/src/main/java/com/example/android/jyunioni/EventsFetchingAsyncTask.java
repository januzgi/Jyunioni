package com.example.android.jyunioni;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Created by JaniS on 26.7.2017.
 * <p>
 * {@link AsyncTask} to perform the network request on a background thread, and then
 * update the UI with the first event in the response.
 */
public class EventsFetchingAsyncTask extends AsyncTask<URL, Void, Event> {


    /**
     * Linkki Jyväskylä Ry events page URL.
     */
    private final String LINKKI_EVENTS_URL = "http://linkkijkl.fi/events/?ical=1&tribe_display=month";

 /*   *
     * TextView that is displayed when the list is empty

    private TextView mEmptyStateTextView;

    *
     * Progressbar to be shown when fetching data.

    private ProgressBar mProgressBar;

    *
     * Adapter for the list of events

    private EventAdapter mAdapter;*/


    /**
     * Private variables for updating the UI with the information from the Event object.
     * Organizing groups image & color and event name and timestamp needed.
     */
    private String eventName = null;
    private String eventTimestamp = null;
    private int groupImageId;
    private int groupColorId;



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
            Log.e(LOG_TAG, "IOException when making the HTTP request in doInBackground at EventActivity" + e);
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
            Log.e(LOG_TAG, "IOException when extracting the HTTP response fields." + e);
        }

        return event;
    }

    /**
     * Update the screen with the given event (which was the result of the {@link EventsFetchingAsyncTask}).
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

        // Prolly needed to use inputstreamreader.

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
