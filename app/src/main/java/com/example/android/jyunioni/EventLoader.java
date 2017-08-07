package com.example.android.jyunioni;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by JaniS on 3.8.2017.
 *
 * EventLoader makes it possible to perform heavy HTTP and parsing tasks in a background thread.
 * UI will remain being updated in the main thread so usability won't be compromised during loading data
 * with tasks taking quite long time.
 */
public class EventLoader extends AsyncTaskLoader<List<Event>> {

    /** Query URL */
    private String[] mUrls;


    private List<Event> mEvents;


    public EventLoader(Context context, String[] urls) {
        super(context);
        mUrls = urls;
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
        if (mUrls == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of events.
        mEvents = Queries.fetchEventData(mUrls);
        return mEvents;
    }


}
