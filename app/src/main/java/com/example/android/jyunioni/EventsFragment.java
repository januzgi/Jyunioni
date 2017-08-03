package com.example.android.jyunioni;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.net.URL;
import java.util.List;

/**
 * {@link Fragment} that displays a list of events.
 */
public class EventsFragment extends Fragment /*implements LoaderManager.LoaderCallbacks<List<Event>>*/ {

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int EVENT_LOADER_ID = 1;

    /**
     * Adapter for the list of events
     */
    private EventAdapter mAdapter;

    /**
     * To update the UI from onCreateView and updateUi methods.
     */
    private View rootView;

    /**
     * Required empty public constructor
     */
    public EventsFragment() { }


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
     * Update the screen to display information from the given {@link List<Event>}.
     */
    public void updateUi(List<Event> events) {

        // Create an EventAdapter, whose data source is a list of Events.
        // The adapter knows how to create list items for each item in the list.
        mAdapter = new EventAdapter(getActivity(), events);

        // Find the ListView object in the view hierarchy.
        // ListView with the view ID called events_list is declared in the list_build.xml layout file.
        ListView listView = (ListView) rootView.findViewById(R.id.events_list);

        // ListView uses the EventAdapter so ListView will display list items for each Event in the list.
        listView.setAdapter(mAdapter);

        /**
         * Set a click listener to open the event's details via an intent
         */
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
     * <p>
     * Runs multiple times at the moment, which is not good. Should run only in the startup of the app.
     */
    public class EventsFetchingAsyncTask extends AsyncTask<URL, Void, List<Event>> {

        /**
         * Linkki Jyväskylä Ry events page URL.
         */
        private final String LINKKI_EVENTS_URL = "http://linkkijkl.fi/events/2017-09/?ical=1&tribe_display=month";


        /**
         * This is done in a background thread.
         */
        @Override
        protected List<Event> doInBackground(URL... urls) {

            // Create an List<Event> object instance
            List<Event> events;

            // Let the Queries class handle the data fetching
            events = Queries.fetchEventData(LINKKI_EVENTS_URL);

            // Return the Event object as the result for the EventAsyncTask
            return events;
        }


        /**
         * Update the screen with the given event (which was the result of the {@link EventsFetchingAsyncTask}).
         * Runs in the UI thread.
         * Gets the result from the population done in doInBackground().
         */
        @Override
        protected void onPostExecute(List<Event> events) {
            if (events == null) {
                return;
            }

            updateUi(events);
        }
    }
}