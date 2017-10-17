package com.example.android.jyunioni;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This fragment displays a list of events and has LoaderManager implemented to use the once fetched data.
 */
public class EventsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Event>> {


    /**
     * Constant value for the earthquake loader ID.
     */
    private static final int EVENT_LOADER_ID = 1;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = EventDetails.class.getSimpleName();

    /**
     * Adapter for the list of events
     */
    private EventAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    /**
     * Progressbar to be shown when fetching data.
     */
    private ProgressBar mProgressBar;

    /**
     * Required empty public constructor
     */
    public EventsFragment() {}

    /**
     * A String array for the different groups event URL's.
     */
    private String[] allEventPageUrls = new String[4];

    /**
     * Create a global variable to access the internet connection information in the UI and background thread.
     */
    public boolean wifiAndInternet = false;

    /**
     * int for the no event data or no internet connection message.
     */
    private int noEventData = R.string.no_event_data_found;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_build, container, false);

        // TODO: fragment to save data for future use
        // TODO: älä hae tapahtumia uusiksi fragmentsiin tultaessa toista kertaa vaan ota ne vaan muistista

        // TODO: events from the real server address


        // Different groups event's list .txt address in the server.
        String LINKKI_EVENTS_URL = "http://users.jyu.fi/~jatasuor/linkkiEvents.txt";

        String PORSSI_EVENTS_URL = "http://users.jyu.fi/~jatasuor/porssiEvents.txt";

        String DUMPPI_EVENTS_URL = "http://users.jyu.fi/~jatasuor/dumppiEvents.txt";

        String STIMULUS_EVENTS_URL = "http://users.jyu.fi/~jatasuor/stimulusEvents.txt";

        // Add the event URL's to the String array
        allEventPageUrls[0] = LINKKI_EVENTS_URL;
        allEventPageUrls[1] = PORSSI_EVENTS_URL;
        allEventPageUrls[2] = DUMPPI_EVENTS_URL;
        allEventPageUrls[3] = STIMULUS_EVENTS_URL;

        // Find the ListView object in the view hierarchy.
        // ListView with the view ID called events_list is declared in the list_build.xml layout file.
        ListView eventsListView = (ListView) rootView.findViewById(R.id.events_list);

        // Find a reference to the mEmptyStateTextView in the layout
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.emptyView);
        eventsListView.setEmptyView(mEmptyStateTextView);

        // Set the progress bar to be shown when searching for the data
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Create an EventAdapter, whose data source is a list of Events.
        // The adapter knows how to create list items for each item in the list.
        mAdapter = new EventAdapter(getActivity(), new ArrayList<Event>());

        // ListView uses the EventAdapter so ListView will display list items for each Event in the list.
        eventsListView.setAdapter(mAdapter);

        // Create a toast to keep the user entertained and up-to-date on what's happening.
        Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.fetching_event_data, Toast.LENGTH_SHORT);
        // Return the default View of the Toast.
        View toastView = toast.getView();

        // Get the TextView of the default View of the Toast.
        TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(16);
        toastMessage.setTextColor(Color.parseColor("#FFFFFF"));
        toastMessage.setGravity(Gravity.CENTER);
        /*toastMessage.setCompoundDrawablePadding(8);
        toastView.setBackgroundColor(getResources().getColor((R.color.primary_color)));*/
        toast.show();

        /** Set a click listener to open the event's details via an intent */
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Check using the ConnectivityManager if there's an internet connection or one is just being made.
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // Check if WiFi is connected but there's no internet connection
        new Runnable() {
            @Override
            public void run() {
                // Moves the current Thread into the background
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                try {
                    HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.fi").openConnection());
                    urlc.setRequestProperty("User-Agent", "Test");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.connect();
                    wifiAndInternet = (urlc.getResponseCode() == 200);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Problem checking internet connection", e);
                }
            }
        };

        // If there is a network connection, fetch data
        if (activeNetwork != null && activeNetwork.isConnected() && wifiAndInternet) {

            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getActivity().getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(EVENT_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible

            mProgressBar.setVisibility(View.GONE);
            noEventData = R.string.no_internet_connection;
            mEmptyStateTextView.setText(noEventData);
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        // TODO: get events data from local storage

        LoaderManager loaderManager = getActivity().getLoaderManager();
        loaderManager.restartLoader(EVENT_LOADER_ID, null, EventsFragment.this).forceLoad();
    }

    @Override
    public Loader<List<Event>> onCreateLoader(int id, Bundle bundle) {
        // Create a new loader for the given URL
        return new EventLoader(getContext(), allEventPageUrls);
    }

    @Override
    public void onLoadFinished(Loader<List<Event>> loader, List<Event> events) {
        mProgressBar.setVisibility(View.GONE);

        // Clear the adapter of previous events data
        mAdapter.clear();

        // If there is a valid list of Events, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (events != null && !events.isEmpty()) {
            mAdapter.addAll(events);
        }

        mEmptyStateTextView.setText(noEventData);

        getLoaderManager().destroyLoader(EVENT_LOADER_ID);
    }

    @Override
    public void onLoaderReset(Loader<List<Event>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }


}