package com.oceanview.resort.model;

import java.time.LocalDate;

/**
 * Task B: The "Model" layer.
 * This class represents a Reservation entity as required by the scenario.
 */
public class Reservation {
    private int reservationNumber;
    private int guestId;
    private int roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalBill;
    private String status;

    // Default Constructor
    public Reservation() {}

    // Constructor for creating a new reservation (Task 2: Add New Reservation)
    public Reservation(int guestId, int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        this.guestId = guestId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.status = "Confirmed";
    }

    // Full Constructor (used by DAO when retrieving from Database)
    public Reservation(int reservationNumber, int guestId, int roomNumber, 
                       LocalDate checkIn, LocalDate checkOut, double totalBill, String status) {
        this.reservationNumber = reservationNumber;
        this.guestId = guestId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.totalBill = totalBill;
        this.status = status;
    }

    // Getters and Setters
    public int getReservationNumber() { return reservationNumber; }
    public void setReservationNumber(int reservationNumber) { this.reservationNumber = reservationNumber; }

    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId) { this.guestId = guestId; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public double getTotalBill() { return totalBill; }
    public void setTotalBill(double totalBill) { this.totalBill = totalBill; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Reservation #" + reservationNumber + " [Room: " + roomNumber + ", Status: " + status + "]";
    }
}