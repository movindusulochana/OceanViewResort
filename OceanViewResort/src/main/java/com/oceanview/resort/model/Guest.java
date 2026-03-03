package com.oceanview.resort.model;

/**
 * Task B: The "Model" layer.
 * This class represents a Guest entity in the system.
 */
public class Guest {
    private int guestId;
    private String name;
    private String address;
    private String contactNumber;

    // Default Constructor
    public Guest() {}

    // Parameterized Constructor for creating new guests
    public Guest(String name, String address, String contactNumber) {
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    // Constructor including ID (used when retrieving from Database)
    public Guest(int guestId, String name, String address, String contactNumber) {
        this.guestId = guestId;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters
    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId) { this.guestId = guestId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    @Override
    public String toString() {
        return "Guest [ID=" + guestId + ", Name=" + name + ", Contact=" + contactNumber + "]";
    }
}