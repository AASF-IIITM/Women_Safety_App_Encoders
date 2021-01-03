package com.aasfencoders.womensafety.Class;

/**
 *  Received connection class when others send connection.
 *
 *  name : Name of the connection.
 *  number : Number of the connection.
 */
public class ReceiveClass {

    private String name;
    private String number;

    public ReceiveClass(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
