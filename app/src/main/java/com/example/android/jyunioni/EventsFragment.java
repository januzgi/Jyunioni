package com.example.android.jyunioni;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

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
    public EventsFragment() {
    }

    /**
     * A String array for the different groups event URL's.
     */
    private String[] allEventPageUrls = new String[4];

    /**
     * Create a global boolean to access the internet connection information in the UI and background thread.
     * Create the Handler for stopping the app informatively.
     * For the WIFI_SETTINGS intent an int to request making a connection. Keep it as a '0' so app execution continues.
     */
    public boolean internetConnectionEstablished = true;
    private Handler noConnectionsHandler = new Handler();
    private static final int CONNECT_TO_WIFI_REQUEST = 0;

    /**
     * int for the "no event data" or "no internet connection" messages.
     */
    private int emptyStateTextViewMessage = R.string.empty_message;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_build, container, false);

        // Different groups event's list .txt address in the server.
        String LINKKI_EVENTS_URL = "http://users.jyu.fi/~jatasuor/Jyunioni/linkkiEvents.txt";

        String PORSSI_EVENTS_URL = "http://users.jyu.fi/~jatasuor/Jyunioni/porssiEvents.txt";

        String DUMPPI_EVENTS_URL = "http://users.jyu.fi/~jatasuor/Jyunioni/dumppiEvents.txt";

        String STIMULUS_EVENTS_URL = "http://users.jyu.fi/~jatasuor/Jyunioni/stimulusEvents.txt";

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

        // Get the state of connectivity to the boolean 'internetConnectionEstablished'
        internetConnectionEstablished = isNetworkAvailable(getContext());

        // If there is a network connection, fetch data
        if (internetConnectionEstablished) {

            // Create a toast to keep the user entertained and up-to-date on what's happening.
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.fetching_event_data, Toast.LENGTH_SHORT);
            // Return the default View of the Toast.
            View toastView = toast.getView();

            // Get the TextView of the default View of the Toast.
            TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
            toastMessage.setTextSize(16);
            toastMessage.setTextColor(Color.parseColor("#FFFFFF"));
            toastMessage.setGravity(Gravity.CENTER);
            toast.show();

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
            emptyStateTextViewMessage = R.string.no_internet_connection;
            mEmptyStateTextView.setText(emptyStateTextViewMessage);
        }

    }


    /**
     * Check if there's an internet connection
     */
    public boolean isNetworkAvailable(Context context) {
        // Check using the ConnectivityManager if there's an internet connection or one is just being made.
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // If there's connections
        if (connectivity != null) {
            // Get details on the currently active default data networks
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            // Get the info array and see for any connections in the array
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /*
    @Override
    public void onPause() {

        // TODO: fragment to save data for future use. onPause() method implementation
        // https://stackoverflow.com/questions/22505327/android-save-restore-fragment-state
        // TODO: get loaded events data from local storage

        // The solution is to call dismiss() on the Dialog you created in
        // viewP.java:183 before exiting the Activity, e.g. in onPause().
        // All Windows&Dialogs should be closed before leaving an Activity.
        // https://stackoverflow.com/questions/2850573/activity-has-leaked-window-that-was-originally-added
    }
    */


    @Override
    public void onResume() {
        super.onResume();

        LoaderManager loaderManager = getActivity().getLoaderManager();
        loaderManager.restartLoader(EVENT_LOADER_ID, null, EventsFragment.this).forceLoad();
    }


    @Override
    public Loader<List<Event>> onCreateLoader(int id, Bundle bundle) {
        // Quit the program if there's no internet connection
        if (!internetConnectionEstablished) {
            // Create a Runnable to be used later for finishing the Activity.
            // Give the user 30s of time to connect to a wifi before quitting the app.
            noConnectionsHandler.postDelayed(new Runnable() {
                public void run() {
                    getContext().stopService(new Intent(getContext(), com.example.android.jyunioni.EventsFragment.class));
                    getActivity().finish();
                }
            }, 30000);

            // Dialog if there's no internet connection available
            // From: https://stackoverflow.com/questions/25685755/ask-user-to-connect-to-internet-or-quit-app-android
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.no_internet_connection).setCancelable(false)
                    .setNegativeButton(R.string.connect_to_wifi, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            getActivity().startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), CONNECT_TO_WIFI_REQUEST);
                        }
                    })
                    .setPositiveButton(R.string.quit_app, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            getActivity().finish();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();

            // Show a toast for the user
            noInternetConnectionToast();
            // Create a new loader for the given URLs, with internetConnectionEstablished == false,
            // so the background thread pauses
            return new EventLoader(getContext(), allEventPageUrls, internetConnectionEstablished);
        }

        // Create a new loader for the given URLs
        return new EventLoader(getContext(), allEventPageUrls, internetConnectionEstablished);
    }


    /**
     * Show a toast in the UI thread that there's no internet connection
     */
    private void noInternetConnectionToast() {
        // Create a toast to keep the user entertained and up-to-date on what's happening.
        Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_LONG);
        // Return the default View of the Toast.
        View toastView = toast.getView();

        // Get the TextView of the default View of the Toast.
        TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(16);
        toastMessage.setTextColor(Color.parseColor("#FFFFFF"));
        toastMessage.setGravity(Gravity.CENTER);
        toast.show();
    }


    /**
     * Get the result from WIFI intent and see whether the connection was made or not.
     * From: https://developer.android.com/training/basics/intents/result.html
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CONNECT_TO_WIFI_REQUEST) {

            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                // https://stackoverflow.com/questions/10407159/how-to-manage-startactivityforresult-on-android
                // The user connected to a wifi.
                internetConnectionEstablished = true;
                noConnectionsHandler = null;
            }
        }
    }


    @Override
    public void onLoadFinished(Loader<List<Event>> loader, List<Event> events) {
        mProgressBar.setVisibility(View.GONE);

        // If there is a valid list of Events, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (events != null && !events.isEmpty()) {
            mAdapter.addAll(events);
        } else {
            mEmptyStateTextView.setText(emptyStateTextViewMessage);
        }

        getLoaderManager().destroyLoader(EVENT_LOADER_ID);
    }


    @Override
    public void onLoaderReset(Loader<List<Event>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }


}