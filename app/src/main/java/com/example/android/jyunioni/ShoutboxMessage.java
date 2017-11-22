/*
 Jani Suoranta 22.11.2017
 */
package com.example.android.jyunioni;

/**
 * ShoutboxMessage is the class for shoutbox messages.
 * This class contains constructors for the messages and most important methods.
 */
class ShoutboxMessage {

    /** Different string attributes needed for the messages */
    private String id;
    private String text;
    private String name;
    private String photoUrl;

    // Empty default constructor
    public ShoutboxMessage() { }

    /**
     * ShoutboxMessage constructor.
     *
     * @param text the message string.
     * @param name name of the message sender.
     * @param photoUrl URL to the senders profile picture
     */
    public ShoutboxMessage(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    /** Getter and setter for the message id */
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /** Getter and setter for the message sender's name */
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /** Getter and setter for the message text */
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    /** Getter and setter for the message sender's profile picture */
    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}