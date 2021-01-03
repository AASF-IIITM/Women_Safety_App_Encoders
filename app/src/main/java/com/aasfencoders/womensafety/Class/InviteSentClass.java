package com.aasfencoders.womensafety.Class;

/**
 *  Invite Sent connection class when users invite others connection.
 *
 *  name : Name of the connection.
 *  number : Number of the connection.
 *  status : Status of the connection. (INVITED / REJECTED / MATCHED)
 */
public class InviteSentClass {

    private String name;
    private String number;
    private String status;

    public InviteSentClass(String name, String number, String status) {
        this.name = name;
        this.number = number;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getStatus() {
        return status;
    }
}
