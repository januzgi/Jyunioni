package com.example.android.jyunioni;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by JaniS on 20.7.2017.
 */

public class EventDetails extends AppCompatActivity {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = EventDetails.class.getSimpleName();

    /**
     * The events URL which is fetched in the EventActivity AsyncTask.
     */
    public static String eventUrl = null;

    /**
     * Private variables for updating the UI with the information from the event page.
     */
    private String eventNameAndTimestamp = null;
    private String eventName = null;
    private String eventInformation = null;
    private int eventImageId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

               /* From EventsFragment.java
               // Add the data to the intent so it can be used in the activity.
        intent.putExtra("EVENT_NAME", eventName);
        intent.putExtra("EVENT_TIMESTAMP", eventTimestamp);
        intent.putExtra("IMAGE_ID", eventImageId);
        intent.putExtra("EVENT_URL", eventUrl);
        intent.putExtra("EVENT_INFORMATION", eventInformation);*/

        Intent intent = getIntent();
        Bundle intentData = intent.getExtras();

        if(intentData != null)
        {
            String j = (String) intentData.get("name");

            // Get the event's name and the timestamp.
            eventName = intent.getStringExtra("EVENT_NAME");
            eventNameAndTimestamp = eventName + "\n" + intent.getStringExtra("EVENT_TIMESTAMP");

            // Set the event name as the title of the activity for better UX
            setTitle(eventName);

            // Get the event's host groups image id. Default -1 == no image.
            eventImageId = intent.getIntExtra("IMAGE_ID", -1);

            // Get the event's URL
            EventDetails.eventUrl = intent.getStringExtra("EVENT_URL");

            // Get the description / other information about the event
            eventInformation = intent.getStringExtra("EVENT_INFORMATION");

        }


        // Set the event's details / information in the event information textview
        TextView eventInformationTextView = (TextView) findViewById(R.id.event_information_textview);
        eventInformationTextView.setText(eventInformation);

        // Find the header where event's name and timestamp is set
        TextView eventDetailsHeaderTextView = (TextView) findViewById(R.id.event_details_header_textview);
        eventDetailsHeaderTextView.setText(eventNameAndTimestamp);

        // Find the imageview by it's id so the correct image can be set
        ImageView eventDetailImageView = (ImageView) findViewById(R.id.event_detail_image);
        eventDetailImageView.setBackgroundResource(eventImageId);



        // Find the button in the event details view that takes the user to the event's web page.
        Button eventDetailsButton = (Button) findViewById(R.id.event_details_button);

        // Set a listener for the button and define actions.
        eventDetailsButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                // Check using the ConnectivityManager if there's an internet connection or one is just being made.
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                // Get details on the currently active default data network
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                // Do this inside a catchblock if something goes wrong.
                try {
                    // If there is a network connection, fetch data
                    if (activeNetwork != null && activeNetwork.isConnected()) {

                        // Show a toast for the user when opening the event's page in a browser.
                        Toast.makeText(getApplicationContext(), R.string.moving_to_event_page, Toast.LENGTH_SHORT).show();

                        // Convert the String URL into a URI object (to pass into the Intent constructor)
                        Uri eventUri = Uri.parse(eventUrl);

                        // Create a new intent to view the event URI
                        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, eventUri);

                        // Send the intent to launch a new activity
                        startActivity(websiteIntent);
                    } else {
                        // Otherwise, display error that there's no internet connection
                        Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // Otherwise, display error that event's webpage wasn't found.
                    Toast.makeText(getApplicationContext(), R.string.webpage_not_found, Toast.LENGTH_SHORT).show();
                    Log.e(LOG_TAG, "Exception when parsing the event's URL for the website intent.");
                }
            }

        });
    }


}


     /* // Get the event's name and the timestamp.
        eventName = event.getEventName();
        eventNameAndTimestamp = event + "\n" + event.getEventTimestamp();

        // Set the event name as the title of the activity for better UX
        setTitle(eventName);

        // Get the event's host groups image id.
        eventImageId = event.getImageResourceId();

        // Get the event's URL
        EventDetails.eventUrl = event.getUrl();

        // Get the description / other information about the event
        eventInformation = event.getEventInformation();*/
