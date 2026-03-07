-- ============================================================
-- repair_rooms.sql
-- Run this in phpMyAdmin (http://localhost/phpmyadmin) or MySQL CLI
-- to fix the "No available rooms" issue.
--
-- Root cause: The after_reservation_insert trigger sets every room
-- to 'Occupied' on each reservation INSERT. Since checkout never
-- ran (billing was broken), no room was ever freed.
--
-- This script resets rooms that have NO active reservation
-- (Confirmed or Checked-In) back to 'Available'.
-- Rooms in 'Maintenance' are left untouched.
-- ============================================================

USE resort;

-- Show current state before fix
SELECT room_number, room_type, status FROM rooms ORDER BY room_number;

-- Fix: set Available for rooms with no active reservations
UPDATE rooms
SET status = 'Available'
WHERE status = 'Occupied'
  AND room_number NOT IN (
      SELECT room_number
      FROM reservations
      WHERE status IN ('Confirmed', 'Checked-In')
  );

-- Confirm result
SELECT
    status,
    COUNT(*) AS count
FROM rooms
GROUP BY status;
