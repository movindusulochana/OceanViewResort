-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 05, 2026 at 09:42 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `resort`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `CalculateReservationBill` (IN `res_id` INT)   BEGIN
    DECLARE num_nights INT;
    DECLARE rate DECIMAL(10,2);
    SELECT DATEDIFF(check_out_date, check_in_date), price_per_night
    INTO num_nights, rate
    FROM reservations r
    JOIN rooms rm ON r.room_number = rm.room_number
    WHERE r.reservation_number = res_id;
    UPDATE reservations
    SET total_bill = (num_nights * rate)
    WHERE reservation_number = res_id;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `guests`
--

CREATE TABLE `guests` (
  `guest_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `address` text DEFAULT NULL,
  `contact_number` varchar(15) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `guests`
--

INSERT INTO `guests` (`guest_id`, `name`, `address`, `contact_number`) VALUES
(1, 'Test Guest', '123 Test Ave', '0112233445'),
(2, 'Test Guest', '123 Test Avez', '0112233445'),
(3, 'Test Guest', '123 Test Ave', '0112233445'),
(4, 'sa', '123 Test Aveaa', '0112233445'),
(5, 'asas', 'a', 'a'),
(6, 'a', 'a', 'a'),
(7, 'a', 'a', 'a'),
(8, 'a', 'a', 'a'),
(9, 'Test Guest', '123 Test Ave', '0112233445'),
(10, 'a', 'aa', 'a'),
(11, 'Test Guest', '123 Test Ave', '0112233445'),
(12, 'Test Guest', '123 Test Ave', '0112233445'),
(13, 'Test Guest', '123 Test Ave', '0112233445'),
(14, 'Test Guest', '123 Test Ave', '0112233445'),
(15, 'sa', 'sas', '1651'),
(16, 'Test Guest', '123 Test Ave', '0112233445'),
(17, 'Test Guest', '123 Test Ave', '0112233445'),
(18, 'a', 'a', 'a'),
(19, 'sa', 'sa', 'sa'),
(20, 'sa', 'sa', 'sa'),
(21, 'Test Guest', '123 Test Ave', '0112233445'),
(22, 'Test Guest', '123 Test Ave', '0112233445'),
(23, 'Test Guest', '123 Test Ave', '0112233445'),
(24, 'Test Guest', '123 Test Ave', '0112233445'),
(25, 'Test Guest', '123 Test Ave', '0112233445'),
(26, 'aa', 'a', '0786136346'),
(27, 'aa', 'a', '0786136346'),
(28, 'aa', 'a', '0786136346'),
(29, 'aa', 'a', '0786136346'),
(30, 'Test Guest', '123 Test Ave', '0112233445'),
(31, 'Test Guest', '123 Test Ave', '0112233445'),
(32, 'Test Guest', '123 Test Ave', '0112233445'),
(33, 'Test Guest', '123 Test Ave', '0112233445'),
(34, 'Test Guest', '123 Test Ave', '0112233445'),
(35, 'Test Guest', '123 Test Ave', '0112233445'),
(36, 'Test Guest', '123 Test Ave', '0112233445'),
(37, 'Test Guest', '123 Test Ave', '0112233445'),
(38, 'Test Guest', '123 Test Ave', '0112233445'),
(39, 'Test Guest', '123 Test Ave', '0112233445'),
(40, 'Test Guest', '123 Test Ave', '0112233445'),
(41, 'Test Guest', '123 Test Ave', '0112233445'),
(42, 'Test Guest', '123 Test Ave', '0112233445'),
(43, 'Test Guest', '123 Test Ave', '0112233445'),
(44, 'Test Guest', '123 Test Ave', '0112233445'),
(45, 'Test Guest', '123 Test Ave', '0112233445'),
(46, 'Test Guest', '123 Test Ave', '0112233445'),
(47, 'Test Guest', '123 Test Ave', '0112233445'),
(48, 'Test Guest', '123 Test Ave', '0112233445'),
(49, 'Test Guest', '123 Test Ave', '0112233445'),
(50, 'Test Guest', '123 Test Ave', '0112233445'),
(51, 'Test Guest', '123 Test Ave', '0112233445'),
(52, 'Test Guest', '123 Test Ave', '0112233445'),
(53, 'Test Guest', '123 Test Ave', '0112233445'),
(54, 'John', 'Colombo', '0771234567'),
(55, 'Nimal Perera', '15 Galle Road, Colombo 03', '0771234567'),
(56, 'Sunethra Silva', '42 Dalada Vidiya, Kandy', '0719876543'),
(57, 'Kasun Jayasuriya', '8A Beach Road, Negombo', '0784561230'),
(58, 'Malini Fonseka', '12 Temple Trees, Nuwara Eliya', '0723456789'),
(59, 'Chaminda Fernando', '55 Main Street, Galle', '0765432109'),
(60, 'Ruwan Wijesinghe', '7/2 Lake View, Kurunegala', '0751122334'),
(61, 'Sanduni Bandara', '89 Park Road, Matara', '0709988776'),
(62, 'Kumara Dharmasena', '32 Fort, Jaffna', '0778899001'),
(63, 'Mahela Ratnayake', '11 Sea Street, Trincomalee', '0712233445'),
(64, 'Dinesh Rajapaksha', '104 New Town, Anuradhapura', '0785566778'),
(65, 'Nishanthi Peries', '23 Old Road, Polonnaruwa', '0729900112'),
(66, 'Angelo De Silva', '45 Hill Street, Badulla', '0761122334'),
(67, 'Tharindu Lakmal', '67 Gem City, Ratnapura', '0754455667'),
(68, 'Gayani Senanayake', '120 Kandy Road, Kegalle', '0701239876'),
(69, 'Pradeep Kumara', '14 Matale Road, Dambulla', '0773344556'),
(70, 'Suranga Mendis', '5 Yakkala Road, Gampaha', '0716677889'),
(71, 'Kusal Perera', '9 Panadura Road, Kalutara', '0789900112'),
(72, 'Wanindu Shanaka', '22 Lagoon View, Puttalam', '0721122334'),
(73, 'Dasun Gunaratne', '78 Port Road, Hambantota', '0765566778'),
(74, 'Pathum Nissanka', '34 Airport Road, Katunayake', '0759900112'),
(75, 'a', '226/1 Ihalabiyanwila,kadwatha', '0786136346'),
(76, 'Test Guest', '123 Test Ave', '0112233445');

-- --------------------------------------------------------

--
-- Table structure for table `reservations`
--

CREATE TABLE `reservations` (
  `reservation_number` int(11) NOT NULL,
  `guest_id` int(11) DEFAULT NULL,
  `room_number` int(11) DEFAULT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `total_bill` decimal(10,2) DEFAULT 0.00,
  `status` enum('Confirmed','Checked-In','Checked-Out','Cancelled') DEFAULT 'Confirmed'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reservations`
--

INSERT INTO `reservations` (`reservation_number`, `guest_id`, `room_number`, `check_in_date`, `check_out_date`, `total_bill`, `status`) VALUES
(3, 1, 101, '2026-03-03', '2026-03-06', 0.00, 'Confirmed'),
(4, 2, 101, '2026-03-03', '2026-03-06', 0.00, 'Confirmed'),
(5, 3, 101, '2026-03-04', '2026-03-07', 15000.00, 'Confirmed'),
(6, 4, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(7, 9, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(8, 11, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(9, 12, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(10, 13, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(11, 14, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(12, 16, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(13, 17, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(14, 18, 102, '2026-03-23', '2026-03-15', -68000.00, 'Confirmed'),
(15, 20, 201, '2026-03-03', '2026-03-25', 330000.00, 'Confirmed'),
(16, 21, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(17, 22, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(18, 23, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(19, 24, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(20, 25, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(21, 30, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(22, 31, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(23, 32, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(24, 33, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(25, 34, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(26, 35, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(27, 36, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(28, 37, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(29, 38, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(30, 39, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(31, 40, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(32, 41, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(33, 42, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(34, 43, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(35, 44, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(36, 45, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(37, 46, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(38, 47, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(39, 48, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(40, 49, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(41, 50, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(42, 51, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(43, 52, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(44, 53, 101, '2026-03-04', '2026-03-07', 0.00, 'Confirmed'),
(45, 4, 301, '2026-03-04', '2026-03-05', 0.00, 'Confirmed'),
(46, 1, 103, '2026-04-01', '2026-04-03', 10000.00, 'Checked-Out'),
(47, 2, 104, '2026-04-02', '2026-04-05', 15000.00, 'Confirmed'),
(48, 3, 105, '2026-04-05', '2026-04-10', 42500.00, 'Confirmed'),
(49, 4, 106, '2026-04-10', '2026-04-12', 17000.00, 'Confirmed'),
(50, 5, 107, '2026-04-15', '2026-04-18', 36000.00, 'Confirmed'),
(51, 6, 108, '2026-04-20', '2026-04-25', 60000.00, 'Confirmed'),
(52, 7, 109, '2026-04-01', '2026-04-04', 45000.00, 'Checked-Out'),
(53, 8, 202, '2026-04-05', '2026-04-06', 5000.00, 'Checked-Out'),
(54, 9, 203, '2026-04-08', '2026-04-10', 17000.00, 'Confirmed'),
(55, 10, 204, '2026-04-12', '2026-04-15', 36000.00, 'Confirmed'),
(56, 11, 205, '2026-04-18', '2026-04-20', 30000.00, 'Confirmed'),
(57, 12, 206, '2026-04-22', '2026-04-25', 15000.00, 'Confirmed'),
(58, 13, 207, '2026-05-01', '2026-05-05', 34000.00, 'Confirmed'),
(59, 14, 208, '2026-05-10', '2026-05-12', 24000.00, 'Confirmed'),
(60, 15, 303, '2026-05-15', '2026-05-20', 25000.00, 'Confirmed'),
(61, 16, 304, '2026-05-22', '2026-05-25', 25500.00, 'Confirmed'),
(62, 17, 305, '2026-06-01', '2026-06-05', 48000.00, 'Confirmed'),
(63, 18, 306, '2026-06-10', '2026-06-15', 125000.00, 'Confirmed'),
(64, 19, 103, '2026-06-20', '2026-06-22', 10000.00, 'Cancelled'),
(65, 20, 104, '2026-06-25', '2026-06-30', 25000.00, 'Checked-Out'),
(66, 76, 101, '2026-03-06', '2026-03-09', 0.00, 'Confirmed'),
(67, 2, 103, '2026-03-05', '2026-03-06', 5000.00, 'Checked-Out'),
(68, 1, 109, '2026-03-01', '2026-03-06', 0.00, 'Confirmed'),
(69, 66, 202, '2026-03-05', '2026-03-07', 10000.00, 'Checked-Out'),
(70, 13, 202, '2026-03-05', '2026-03-08', 0.00, 'Confirmed');

--
-- Triggers `reservations`
--
DELIMITER $$
CREATE TRIGGER `after_reservation_insert` AFTER INSERT ON `reservations` FOR EACH ROW BEGIN
    UPDATE rooms SET status = 'Occupied' WHERE room_number = NEW.room_number;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE `rooms` (
  `room_number` int(11) NOT NULL,
  `room_type` enum('Single','Double','Suite','Deluxe') NOT NULL,
  `price_per_night` decimal(10,2) NOT NULL,
  `status` enum('Available','Occupied','Maintenance') DEFAULT 'Available'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rooms`
--

INSERT INTO `rooms` (`room_number`, `room_type`, `price_per_night`, `status`) VALUES
(101, 'Single', 5000.00, 'Occupied'),
(102, 'Double', 8500.00, 'Occupied'),
(103, 'Single', 5000.00, 'Available'),
(104, 'Single', 5000.00, 'Available'),
(105, 'Double', 8500.00, 'Occupied'),
(106, 'Double', 8500.00, 'Occupied'),
(107, 'Deluxe', 12000.00, 'Occupied'),
(108, 'Deluxe', 12000.00, 'Occupied'),
(109, 'Suite', 15000.00, 'Occupied'),
(110, 'Suite', 15000.00, 'Maintenance'),
(201, 'Suite', 15000.00, 'Occupied'),
(202, 'Single', 5000.00, 'Occupied'),
(203, 'Double', 8500.00, 'Occupied'),
(204, 'Deluxe', 12000.00, 'Occupied'),
(205, 'Suite', 15000.00, 'Occupied'),
(206, 'Single', 5000.00, 'Occupied'),
(207, 'Double', 8500.00, 'Occupied'),
(208, 'Deluxe', 12000.00, 'Occupied'),
(301, '', 25000.00, 'Occupied'),
(302, 'Suite', 15000.00, 'Maintenance'),
(303, 'Single', 5000.00, 'Occupied'),
(304, 'Double', 8500.00, 'Occupied'),
(305, 'Deluxe', 12000.00, 'Occupied'),
(306, 'Suite', 25000.00, 'Occupied');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('Admin','Staff') DEFAULT 'Staff'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role`) VALUES
(1, 'admin', 'admin123', 'Admin'),
(2, 'a', 'a', 'Staff'),
(3, 'kamal_staff', 'staff123', 'Staff'),
(4, 'sunil_admin', 'admin123', 'Admin'),
(5, 'ruwini_reception', 'pass123', 'Staff'),
(6, 'nuwan_frontdesk', 'pass123', 'Staff'),
(7, 'ashan_manager', 'admin123', 'Admin'),
(8, 'piyumi_staff', 'pass123', 'Staff'),
(9, 'lahiru_staff', 'pass123', 'Staff'),
(10, 'sanduni_staff', 'pass123', 'Staff'),
(11, 'tharuka_staff', 'pass123', 'Staff'),
(12, 'chathura_staff', 'pass123', 'Staff'),
(13, 'gayan_staff', 'pass123', 'Staff'),
(14, 'dilshan_admin', 'admin123', 'Admin'),
(15, 'ishara_staff', 'pass123', 'Staff'),
(16, 'kavinda_staff', 'pass123', 'Staff'),
(17, 'maleesha_staff', 'pass123', 'Staff'),
(18, 'nadeeka_staff', 'pass123', 'Staff'),
(19, 'oshada_staff', 'pass123', 'Staff'),
(20, 'pramod_staff', 'pass123', 'Staff'),
(21, 'roshan_staff', 'pass123', 'Staff'),
(22, 'samantha_staff', 'pass123', 'Staff');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `guests`
--
ALTER TABLE `guests`
  ADD PRIMARY KEY (`guest_id`);

--
-- Indexes for table `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`reservation_number`),
  ADD KEY `guest_id` (`guest_id`),
  ADD KEY `room_number` (`room_number`);

--
-- Indexes for table `rooms`
--
ALTER TABLE `rooms`
  ADD PRIMARY KEY (`room_number`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `guests`
--
ALTER TABLE `guests`
  MODIFY `guest_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=77;

--
-- AUTO_INCREMENT for table `reservations`
--
ALTER TABLE `reservations`
  MODIFY `reservation_number` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=71;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`guest_id`) REFERENCES `guests` (`guest_id`),
  ADD CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`room_number`) REFERENCES `rooms` (`room_number`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
