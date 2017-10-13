package com.example.android.jyunioni;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.android.jyunioni.EventDetails.LOG_TAG;

/**
 * Event represents a groups event. Implements Comparable<T> to list the events according their starting date.
 */
class Event implements Comparable<Event> {

    /** String for the event name */
    private String mEventName = "";

    /** String for the event's timestamp */
    private String mEventTimestamp = "";

    /** Image resource ID for the event's hosting group */
    private int mImageResourceId = NO_IMAGE_PROVIDED;

    /** Theme color for the group hosting the event. */
    private int mGroupColorId = -1;

    /** The URL of the event's web page. */
    private String mUrl = "";

    /** The information, instructions or details of the event. */
    private String mEventInformation = "";

    /** Constant value that represents no image was provided for this group */
    private static final int NO_IMAGE_PROVIDED = -1;

    /** Create a new Event object.
     *
     * @param eventName       is the string from the HTTP request which is the event's name.
     * @param eventTimestamp  is the long from the HTTP request which is the event's timestamp.
     * @param eventInformation is the overall instructions or description about the event.
     * @param imageResourceId is the drawable resource ID for the group that hosts the event.
     * @param groupColorId    The theme color of the group which hosts the event.
     * @param url             The url of the event's page
     */
    public Event(String eventName, String eventTimestamp, String eventInformation, int imageResourceId, int groupColorId, String url) {
        this.mEventName = eventName;
        this.mEventTimestamp = eventTimestamp;
        this.mEventInformation = eventInformation;
        this.mImageResourceId = imageResourceId;
        this.mGroupColorId = groupColorId;
        this.mUrl = url;
    }

    /** Getter for event name */
    public String getEventName() {
        return this.mEventName;
    }

    /** Getter for event timestamp */
    public String getEventTimestamp() { return this.mEventTimestamp; }

    /** Getter for the image resource ID of the event hosting group. */
    public int getImageResourceId() { return this.mImageResourceId; }

    /** Getter for the color ID for the group hosting the event. */
    public int getGroupColorId() { return this.mGroupColorId; }

    /** Getter for the URL of the event's page wherefrom the data was fetched too */
    public String getUrl() { return this.mUrl; }

    /** Getter for the event's information or description */
    public String getEventInformation() { return this.mEventInformation; }

    /** Returns whether or not there is an image for this word. */
    public boolean hasImage() {
        return this.mImageResourceId != NO_IMAGE_PROVIDED;
    }

    /** Getter for the event's starting date */
    public Date getEventStartDate() {
        Date result = null;

        // "11.9. 18:00 - 22:00"
        String timestamp = this.getEventTimestamp();

        // If the date doesn't contain spaces, it is only one day event without hours
        if (!timestamp.contains(" ")) {
            // Create a Date object
            try {
                SimpleDateFormat newFormat = new SimpleDateFormat("d.M.");
                result = newFormat.parse(timestamp);
                Log.e(LOG_TAG, result.toString());
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Parsing problem at getEventStartDate().\n" + e);
            }

            return result;
        }

        // Get the starting date (before the HH:MM stamp) e.g. "11.9."
        String startDateString = timestamp.substring(0, timestamp.indexOf(" "));

        // Create a Date object
        try {
            SimpleDateFormat newFormat = new SimpleDateFormat("d.M.");
            result = newFormat.parse(startDateString);
            Log.e(LOG_TAG, result.toString());
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Parsing problem at getEventStartDate().\n" + e);
        }

        return result;
    }

    /** Check for NullPointers, thus override. */
    @Override
    public int compareTo(Event event) {
        if (this.getEventStartDate() == null || event.getEventStartDate() == null)
            return 0;
        return this.getEventStartDate().compareTo(event.getEventStartDate());
    }

}