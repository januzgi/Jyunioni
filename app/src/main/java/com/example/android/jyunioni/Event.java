package com.example.android.jyunioni;

/**
 * {@link Event} represents a vocabulary word that the user wants to learn.
 * It contains resource IDs for the default translation, Miwok translation, audio file, and
 * optional image file for that word.
 */
public class Event {

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
     * @param eventTimestamp  is the string from the HTTP request which is the event's timestamp.
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
    public String getEventTimestamp() {
        return this.mEventTimestamp;
    }

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


}