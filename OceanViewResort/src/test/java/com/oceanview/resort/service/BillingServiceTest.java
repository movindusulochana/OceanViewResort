package com.oceanview.resort.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Task C: Test-Driven Development (TDD) and Test Automation.
 * This class automates the testing of the BillingService logic, 
 * specifically verifying that math calculations and billing processes 
 * function correctly without manual intervention.
 */
public class BillingServiceTest {

    private BillingService billingService;

    /**
     * Initializes the service before each test case.
     */
    @BeforeEach
    public void setUp() {
        billingService = new BillingService();
    }

    /**
     * Test Case: Verifies that the processFinalBill method correctly 
     * executes the stored procedure for a given reservation.
     * Fulfills Task 4: Calculate Bill.
     */
    @Test
    public void testProcessFinalBill() {
        // ID of a reservation that exists in your 'resort' database
        int testReservationId = 1; 

        // The method should return true if the database procedure executes successfully
        boolean result = billingService.processFinalBill(testReservationId);
        
        assertTrue(result, "The billing process should return true upon successful execution of the stored procedure.");
        System.out.println("--> [TEST SUCCESS] testProcessFinalBill passed for Reservation ID: " + testReservationId);
    }

    /**
     * Test Case: Verifies that the total amount retrieved for a 
     * reservation is a valid non-negative number.
     */
    @Test
    public void testGetTotalAmount() {
        int testReservationId = 1;
        
        double amount = billingService.getTotalAmount(testReservationId);
        
        // Assertion: A valid bill should be 0 or more (never negative)
        assertTrue(amount >= 0, "The total bill amount should be a non-negative value.");
        
        if (amount > 0) {
            System.out.println("--> [TEST SUCCESS] testGetTotalAmount passed. Amount: Rs. " + amount);
        } else {
            System.out.println("--> [TEST INFO] Total amount is 0. Ensure the CalculateReservationBill procedure has run.");
        }
    }
}