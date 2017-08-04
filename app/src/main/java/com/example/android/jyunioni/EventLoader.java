package com.example.android.jyunioni;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by JaniS on 3.8.2017.
 */
public class EventLoader extends AsyncTaskLoader<List<Event>> {

    /** Tag for log messages */
    /*private static final String LOG_TAG = EventLoader.class.getName();*/

    /** Query URL */
    private String mUrl;


    private List<Event> mEvents;


    public EventLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        if(mEvents != null){
            deliverResult(mEvents);
        }

        if (takeContentChanged() || mEvents == null){
            forceLoad();
        }
    }


    @Override
    public void deliverResult(List<Event> events) {
        mEvents = events;
        super.deliverResult(mEvents);
    }

    /** This is on a background thread. */
    @Override
    public List<Event> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of events.
        mEvents = Queries.fetchEventData(mUrl);
        return mEvents;
    }


}
