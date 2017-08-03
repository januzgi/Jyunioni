package com.example.android.jyunioni;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by JaniS on 3.8.2017.
 */
public class EventLoader extends AsyncTaskLoader<List<Event>> {

    /** Tag for log messages */
    private static final String LOG_TAG = EventLoader.class.getName();

    /** Query URL */
    private String mUrl;


    public EventLoader(Context context, String url) {
        super(context);
        mUrl = url;
        Log.e(LOG_TAG, "EventLoader(); aka onCreateLoader(); callback");
    }

    @Override
    protected void onStartLoading() {
        Log.e(LOG_TAG, "onStartLoading(); at EventLoader.java");
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Event> loadInBackground() {
        Log.e(LOG_TAG, "loadInBackground(); at EventLoader.java");
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of events.
        List<Event> events = Queries.fetchEventData(mUrl);
        return events;
    }
}