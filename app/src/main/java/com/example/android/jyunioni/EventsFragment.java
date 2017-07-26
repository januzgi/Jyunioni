package com.example.android.jyunioni;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * {@link Fragment} that displays a list of events.
 */
public class EventsFragment extends Fragment {

    /**
     * Adapter for the list of events
     */
    private EventAdapter mAdapter;

    /**
     * Required empty public constructor
     */
    public EventsFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_build, container, false);


        // TODO: Event list should be created during the startup of the app. Data should be fetched when the app has started.
        // TODO: Kaikki mitä tää luokka tarvii on ArrayList Event:tejä

        // TODO: tää lista tulis luoda EventActivityn AsyncTaskilla, koska sen avulla haetaan tiedot netistä.


        // Create a list of events
        final ArrayList<Event> events = new ArrayList<>();

        // Fake events for visualisation
        events.add(new Event("Dumpin syysrieha", "2.12.2017", "Bileet on isot.",
                R.drawable.dumppi_ry_icon, R.color.color_dumppi_ry, "http://linkkijkl.fi/event/baarikierros/"));
        events.add(new Event("Linkin sitsit", "11.11.2017", "Linkiltä löytyy.",
                R.drawable.linkki_jkl_icon, R.color.color_linkki_jkl, "http://linkkijkl.fi/event/baarikierros/"));
        events.add(new Event("Pörssin talvisota", "12.12.2017", "Vieläkin darra",
                R.drawable.porssi_ry_icon, R.color.color_porssi_ry, "http://linkkijkl.fi/event/baarikierros/"));


        // Create an {@link EventAdapter}, whose data source is a list of {@link Event}s.
        // The adapter knows how to create list items for each item in the list.
        mAdapter = new EventAdapter(getActivity(), events);

        // Find the {@link ListView} object in the view hierarchy of the {@link Activity}.
        // There should be a {@link ListView} with the view ID called group_list, which is declared in the
        // list_build.xmll layout file.
        ListView listView = (ListView) rootView.findViewById(R.id.events_list);

        // Make the {@link ListView} use the {@link EventAdapter} we created above, so that the
        // {@link ListView} will display list items for each {@link Event} in the list.
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


        return rootView;

    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
