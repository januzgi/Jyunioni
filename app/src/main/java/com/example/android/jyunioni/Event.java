/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.jyunioni;

/**
 * {@link Event} represents a vocabulary word that the user wants to learn.
 * It contains resource IDs for the default translation, Miwok translation, audio file, and
 * optional image file for that word.
 */
public class Event {

    /**
     * String for the event name
     */
    private String mEventName = "";

    /**
     * String for the event's timestamp in as DD:MM:YY\nMM:HH
     */
    private String mEventTimestamp = "";

    /**
     * Image resource ID for the event's hosting group
     */
    private int mImageResourceId = NO_IMAGE_PROVIDED;

    /**
     * Theme color for the group hosting the event.
     */
    private int mGroupColorId = -1;

    /**
     * The URL of the event's web page.
     */
    private String mUrl = "";

    /**
     * The information, instructions or details of the event.
     */
    private String mEventInformation = "";

    /**
     * Constant value that represents no image was provided for this group
     */
    private static final int NO_IMAGE_PROVIDED = -1;


    /**
     * Create a new Event object.
     *
     * @param eventName       is the string from the HTTP request which is the event's name.
     * @param eventTimestamp  is the string from the HTTP request which is the event's timestamp.
     * @param eventInformation is the overall instructions or description about the event.
     * @param imageResourceId is the drawable resource ID for the group that hosts the event.
     * @param groupColorId    The theme color of the group which hosts the event.
     * @param url             The url of the event's page
     */
    public Event(String eventName, String eventTimestamp, String eventInformation, int imageResourceId, int groupColorId, String url) {
        mEventName = eventName;
        mEventTimestamp = eventTimestamp;
        mEventInformation = eventInformation;
        mImageResourceId = imageResourceId;
        mGroupColorId = groupColorId;
        mUrl = url;
    }

    /**
     * Get the string for the event's name.
     */
    public String getEventName() {
        return mEventName;
    }

    /**
     * Get the string for the event's timestamp.
     */
    public String getEventTimestamp() {
        return mEventTimestamp;
    }

    /**
     * Return the image resource ID of the event hosting group.
     */
    public int getImageResourceId() { return mImageResourceId; }

    /**
     * Return the color ID for the group hosting the event.
     */
    public int getGroupColorId() { return mGroupColorId; }

    /**
     * Get the URL of the event's page wherefrom the data was fetched too
     */
    public String getUrl() { return mUrl; }

    /**
     * Get the event's information or description
     */
    public String getEventInformation() { return mEventInformation; }

    /**
     * Returns whether or not there is an image for this word.
     */
    public boolean hasImage() {
        return mImageResourceId != NO_IMAGE_PROVIDED;
    }
}