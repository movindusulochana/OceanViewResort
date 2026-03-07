package com.oceanview.resort.model;

/**
 * Task B: The "Model" layer.
 * This class represents a Room entity as defined in the 'resort' database.
 */
public class Room {
    private int roomNumber;
    private String roomType;
    private double pricePerNight;
    private String status;

    // Default Constructor
    public Room() {}

    // Constructor for creating a new room entry.
    // BUG FIX: default roomType to "Standard" if the DB column is NULL (e.g. Room 301).
    public Room(int roomNumber, String roomType, double pricePerNight, String status) {
        this.roomNumber    = roomNumber;
        this.roomType      = (roomType != null && !roomType.trim().isEmpty()) ? roomType : "Standard";
        this.pricePerNight = pricePerNight;
        this.status        = status;
    }

    // Getters and Setters
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " [" + roomType + " - Rs." + pricePerNight + "/night]";
    }
}