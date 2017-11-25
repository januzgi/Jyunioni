package com.example.android.jyunioni;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

/**
 * Created by JaniS on 3.8.2017.
 * <p>
 * EventLoader makes it possible to perform heavy HTTP and parsing tasks in a background thread.
 * UI will remain being updated in the main thread so usability won't be compromised during loading data
 * with tasks taking quite long time.
 *
 * @author Jani Suoranta 25.11.2017
 */
class EventLoader extends AsyncTaskLoader<List<Event>> {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = EventDetails.class.getSimpleName();

    /**
     * Query URLs, list of Events and internetConnection boolean
     */
    private String[] mUrls;
    private List<Event> mEvents;
    private boolean internetConnection = false;


    /**
     * Loader constructor
     */
    EventLoader(Context context, String[] urls, boolean connectedToInternet) {
        super(context);
        mUrls = urls;
        internetConnection = connectedToInternet;
    }


    /**
     * Called automatically by LoaderManager when the associated fragment/activity is being started.
     */
    @Override
    protected void onStartLoading() {
        if (mEvents != null) {
            deliverResult(mEvents);
        }

        if (takeContentChanged() || mEvents == null) {
            forceLoad();
        }
    }


    /**
     * Sends the result of the load to the registered listener.
     */
    @Override
    public void deliverResult(List<Event> events) {
        mEvents = events;
        super.deliverResult(mEvents);
    }


    /**
     * This is on a background thread.
     */
    @Override
    public List<Event> loadInBackground() {
        // If there are no URL's in the list
        if (mUrls == null) {
            return null;
        }

        // If there already is event's in the list
        /*if (mEvents != null) {
            Log.e("loadInBackground", "events object contents: " + mEvents.get(0).getEventName());
            return null;
        }*/

        // If there's no internet connection, then end the activity after waiting a little
        if (!internetConnection) {
            // Pause the thread for 60s. Wait for the user to connect to the internet,
            // if that doesn't happen then the app is shut down.
            try {
                Thread.sleep(60000);
                getContext().stopService(new Intent(getContext(), com.example.android.jyunioni.EventLoader.class));
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Problem trying to pause the background thread in EventLoader.java: " + e);
            }
        }

        // Perform the network request, parse the response, and extract a list of events.
        mEvents = Queries.fetchEventData(mUrls);
        return mEvents;
    }

}
