package com.example.android.jyunioni;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * EventAdapter is an ArrayAdapter that provides the layout for each list item
 * based on a data source, which is a list of Event objects.
 */
public class EventAdapter extends ArrayAdapter<Event>  {


    /** Create a new EventAdapter object.
     *
     * @param context is the current context (Activity) that the adapter is being created in.
     * @param events is the list of Events to be displayed.
     */
    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Get the {@link Event} object located at this position in the list
        Event currentEvent = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID event_name_text_view.
        TextView eventNameTextView = (TextView) listItemView.findViewById(R.id.event_name_text_view);
        // Get the name of the currentEvent object and set this text on the event name TextView.
        eventNameTextView.setText(currentEvent.getEventName());


        // Find the TextView in the list_item.xml layout with the ID event_timestamp_text_view.
        TextView timestampTextView = (TextView) listItemView.findViewById(R.id.event_timestamp_text_view);
        // Get the timestamp from the currentEvent object and set this text on the timestamp TextView.
        timestampTextView.setText(currentEvent.getEventTimestamp());


        // Find the ImageView in the list_item.xml layout with the ID image.
        ImageView imageView = (ImageView) listItemView.findViewById(R.id.group_image);

        // Check if an image is provided for this event or not
        if (currentEvent.hasImage()) {
            // If an image is available, display the provided image based on the resource ID
            imageView.setImageResource(currentEvent.getImageResourceId());
            // Make sure the view is visible
            imageView.setVisibility(View.VISIBLE);
        } else {
            // Otherwise hide the ImageView (set visibility to GONE)
            imageView.setVisibility(View.GONE);
        }

        // Set the theme color for the list item, find id first
        View textContainer = listItemView.findViewById(R.id.text_container);

        // Find the color that the resource ID maps to and
        // set the background color of the text container View
        textContainer.setBackgroundColor(ContextCompat.getColor(getContext(), currentEvent.getGroupColorId()));


        // Return the whole list item layout (containing 2 TextViews and the ImageView) so that it can be shown in
        // the ListView.
        return listItemView;
    }


}