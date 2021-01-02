package com.aasfencoders.womensafety.Class;

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
