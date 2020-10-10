package com.example.shiftme.dataTypes;

public class User {
    private	String id;
    private String firstname;
    private String lastname;
    private String email;
    private String shift;
    private String group;

    public User(String firstname, String lastname, String email, String group) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.group = group;
    }

    public User(String id, String firstname, String lastname, String shift, String group) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.shift = shift;
        this.group = group;
    }

    public String getFirstName() {
        return firstname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstname) {
        this.firstname = firstname;
    }

    public String getLastName() {
        return lastname;
    }

    public void setLastName(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

}