package com.aasfencoders.womensafety.Class;

/**
 *  Matched connection class.
 *
 *  name : Name of the connection.
 *  number : Number of the connection.
 *  status : Status of the connection. (INVITED / REJECTED / MATCHED)
 */
public class MatchedClass {

    private String name;
    private String number;
    private String status;

    public MatchedClass(String name, String number) {
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
