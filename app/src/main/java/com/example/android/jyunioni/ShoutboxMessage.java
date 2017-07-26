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
 * {@link ShoutboxMessage} is the class for shoutbox messages.
 * This class contains String for the message, database where the messages are being saved and
 * the updating mechanism for keeping the shoutbox in real time with the server.
 */
public class ShoutboxMessage {

    /** String for the event name */
    private String mMessage;


    /**
     * Create a new ShoutboxMessage object.
     *
     * @param message is the string from the HTTP request which is the event's name.
     */
    public ShoutboxMessage(String message) {
        mMessage = message;
    }

    /**
     * Get the strings that are in the shoutbox.
     */
    public String getShoutboxMessage() {
        return mMessage;
    }

}