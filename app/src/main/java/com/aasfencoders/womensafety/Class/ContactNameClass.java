package com.aasfencoders.womensafety.Class;

/**
 *  Base class of a Contact Name fetched from the Phone Contact Database.
 *
 *  id: Unique ID of the contact.
 *  name : Name of the contact.
 */
public class ContactNameClass {

    private String id;
    private String name;

    public ContactNameClass(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
