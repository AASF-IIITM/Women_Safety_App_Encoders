package com.aasfencoders.womensafety.Class;

public class InviteSentClass {

    private String Name;
    private String Number;
    private String Status;

    public InviteSentClass(){

    }

    public InviteSentClass(String Name , String Number,  String Status){
        this.Name = Name;
        this.Number = Number;
        this.Status = Status;
    }

    public String getName() {
        return Name;
    }

    public String getNumber() {
        return Number;
    }

    public String getStatus() {
        return Status;
    }


}
