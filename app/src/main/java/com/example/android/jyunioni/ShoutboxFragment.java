package com.example.android.jyunioni;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * ShoutboxFragment displays a view in which the messages in the shoutbox are shown.
 */
class ShoutboxFragment extends Fragment {

    /** Required empty public constructor */
    public ShoutboxFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.shoutbox_build, container, false);

        // Create a list of events
        final ArrayList<ShoutboxMessage> events = new ArrayList<ShoutboxMessage>();
        events.add(new ShoutboxMessage("Test"));

        return rootView;
    }

    /** When the activity is stopped, release possible resources */
    @Override
    public void onStop() {
        super.onStop();

    }


}
