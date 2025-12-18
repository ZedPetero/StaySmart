-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 18, 2025 at 01:20 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `staysmart_db`
--
CREATE DATABASE IF NOT EXISTS `staysmart_db`;
USE `staysmart_db`;

-- --------------------------------------------------------

--
-- Table structure for table `applications`
--

CREATE TABLE `applications` (
  `id` int(11) NOT NULL,
  `room_id` int(11) NOT NULL,
  `tenant_id` int(11) NOT NULL,
  `property_id` int(11) NOT NULL,
  `application_type` enum('Booking','Tour') NOT NULL,
  `message` text DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `contact_number` varchar(20) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'Pending',
  `apply_date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `applications`
--

INSERT INTO `applications` (`id`, `room_id`, `tenant_id`, `property_id`, `application_type`, `message`, `payment_method`, `contact_number`, `status`, `apply_date`) VALUES
(1, 25, 5, 18, 'Booking', 'Hiii, me interested', 'GCash / E-Wallet', NULL, 'Pending', '2025-12-17 22:49:04'),
(2, 45, 5, 19, 'Tour', 'Please2', NULL, '0998763512', 'Pending', '2025-12-17 22:52:33');

-- --------------------------------------------------------

--
-- Table structure for table `house`
--

CREATE TABLE `house` (
  `id` int(11) NOT NULL,
  `landlord_id` int(11) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `floors` int(11) DEFAULT NULL,
  `rooms_per_floor` int(11) DEFAULT NULL,
  `is_urban` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `house`
--

INSERT INTO `house` (`id`, `landlord_id`, `name`, `floors`, `rooms_per_floor`, `is_urban`) VALUES
(1, 1, 'Opera', 5, 3, 1),
(2, 1, 'UM', 10, 6, 0),
(3, 0, 'please', 3, 0, 1),
(4, 0, 'please', 3, 0, 1),
(5, 0, 'please', 3, 0, 1),
(6, 0, 'Please', 3, 0, 1),
(7, 0, 'Please', 2, 0, 1);

-- --------------------------------------------------------

--
-- Table structure for table `landlord`
--

CREATE TABLE `landlord` (
  `id` int(11) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `properties`
--

CREATE TABLE `properties` (
  `id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `location` varchar(200) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `location_type` varchar(50) DEFAULT NULL,
  `total_floors` int(11) DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `type` varchar(50) DEFAULT 'Apartment',
  `floors` varchar(20) DEFAULT '1',
  `landlord_id` int(11) DEFAULT NULL,
  `amenities` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `properties`
--

INSERT INTO `properties` (`id`, `name`, `location`, `price`, `location_type`, `total_floors`, `image_path`, `type`, `floors`, `landlord_id`, `amenities`) VALUES
(18, 'Sunshine', '12312', 123.00, NULL, NULL, 'C:\\Users\\Admin\\Downloads\\rural1.jpg', 'Rural', '3', NULL, NULL),
(19, 'Urban Pool', 'Davao', 3000.00, NULL, NULL, 'C:\\Users\\Admin\\Downloads\\urban2.jpg', 'Urban', '6', NULL, NULL),
(20, 'Moonlight', 'Mandug', 700.00, NULL, NULL, 'C:\\Users\\Admin\\Downloads\\urban3.jpg', 'Urban', '9', 4, 'Toilet & bath, Laundry area, Emergency exits, Secure main entrance / gate'),
(22, 'Maui Wowie', 'Manila', 1500.00, NULL, NULL, 'C:\\Users\\Admin\\Downloads\\urban2.jpg', 'Urban', '6', 7, NULL),
(23, 'Milo', 'Mandug', 1000.00, NULL, NULL, 'C:\\Users\\Admin\\Downloads\\urban3.jpg', 'Urban', '7', 4, 'Swimming Pool, Gym, Wi-Fi, 24/7 Security, Garden, Pet Friendly'),
(25, 'KapeNate', 'UM', 2000.00, NULL, NULL, 'C:\\Users\\Admin\\Downloads\\urban1.jpg', 'Urban', '1', 4, ''),
(26, 'Lechon', 'dabaw', 1212.00, NULL, NULL, 'C:\\Users\\Admin\\Downloads\\rural2.jpg', 'Rural', '4', 4, 'Toilet & bath, Kitchen / cooking area, Laundry area, Emergency exits, Secure main entrance / gate');

-- --------------------------------------------------------

--
-- Table structure for table `property_floors`
--

CREATE TABLE `property_floors` (
  `id` int(11) NOT NULL,
  `property_id` int(11) DEFAULT NULL,
  `floor_number` int(11) DEFAULT NULL,
  `room_count` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `property_floors`
--

INSERT INTO `property_floors` (`id`, `property_id`, `floor_number`, `room_count`) VALUES
(63, 18, 1, 2),
(64, 18, 2, 1),
(65, 18, 3, 3),
(66, 19, 1, 2),
(67, 19, 2, 3),
(68, 19, 3, 4),
(69, 19, 4, 5),
(70, 19, 5, 1),
(71, 19, 6, 6),
(72, 20, 1, 8),
(73, 20, 2, 1),
(74, 20, 3, 7),
(75, 20, 4, 2),
(76, 20, 5, 6),
(77, 20, 6, 3),
(78, 20, 7, 5),
(79, 20, 8, 4),
(80, 20, 9, 2),
(83, 22, 1, 3),
(84, 22, 2, 4),
(85, 22, 3, 3),
(86, 22, 4, 4),
(87, 22, 5, 3),
(88, 22, 6, 4),
(89, 23, 1, 7),
(90, 23, 2, 6),
(91, 23, 3, 5),
(92, 23, 4, 4),
(93, 23, 5, 3),
(94, 23, 6, 2),
(95, 23, 7, 1),
(98, 25, 1, 3),
(99, 26, 1, 3),
(100, 26, 2, 4),
(101, 26, 3, 2),
(102, 26, 4, 1);

-- --------------------------------------------------------

--
-- Table structure for table `room`
--

CREATE TABLE `room` (
  `id` int(11) NOT NULL,
  `house_id` int(11) DEFAULT NULL,
  `floor` int(11) DEFAULT NULL,
  `room_number` int(11) DEFAULT NULL,
  `photo_path` varchar(255) DEFAULT NULL,
  `rent` decimal(10,2) DEFAULT NULL,
  `facilities` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE `rooms` (
  `id` int(11) NOT NULL,
  `property_id` int(11) NOT NULL,
  `floor_level` int(11) NOT NULL,
  `room_number` varchar(50) NOT NULL,
  `status` varchar(20) DEFAULT 'Available',
  `price` decimal(10,2) NOT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `facilities` text DEFAULT NULL,
  `payment_status` varchar(50) DEFAULT 'Pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rooms`
--

INSERT INTO `rooms` (`id`, `property_id`, `floor_level`, `room_number`, `status`, `price`, `image_path`, `facilities`, `payment_status`) VALUES
(20, 18, 1, '101', 'Occupied', 4542.00, 'C:\\Users\\Admin\\Downloads\\rural1.jpg', 'Electric fan, Study table / Desk, Electrical outlets, Extension cord (fixed)', 'Paid'),
(21, 18, 1, '102', 'Available', 1231231.00, 'C:\\Users\\Admin\\Downloads\\urban3.jpg', 'Noneawdadw1231', 'Paid'),
(22, 18, 2, '201', 'Occupied', 450.00, 'C:\\Users\\Admin\\Downloads\\urban3.jpg', 'Electric fan, Air-conditioned unit, Study table / Desk, Wall hooks / Hangers', 'Paid'),
(23, 18, 3, '301', 'Available', 0.00, NULL, NULL, 'Pending'),
(24, 18, 3, '302', 'Available', 0.00, NULL, NULL, 'Pending'),
(25, 18, 3, '303', 'Available', 0.00, NULL, NULL, 'Pending'),
(26, 18, 1, '103', 'Occupied', 1231.00, 'C:\\Users\\Admin\\Downloads\\urban2.jpg', 'awdad', 'Paid'),
(27, 19, 1, '101', 'Available', 0.00, NULL, NULL, 'Pending'),
(28, 19, 1, '102', 'Available', 123123.00, 'C:\\Users\\Admin\\Downloads\\urban2.jpg', 'None123123', 'Paid'),
(29, 19, 2, '201', 'Available', 0.00, NULL, NULL, 'Pending'),
(30, 19, 2, '202', 'Available', 0.00, NULL, NULL, 'Pending'),
(31, 19, 2, '203', 'Available', 0.00, NULL, NULL, 'Pending'),
(32, 19, 3, '301', 'Available', 0.00, NULL, NULL, 'Pending'),
(33, 19, 3, '302', 'Available', 0.00, NULL, NULL, 'Pending'),
(34, 19, 3, '303', 'Available', 0.00, NULL, NULL, 'Pending'),
(35, 19, 3, '304', 'Available', 0.00, NULL, NULL, 'Pending'),
(36, 19, 4, '401', 'Available', 0.00, NULL, NULL, 'Pending'),
(37, 19, 4, '402', 'Available', 0.00, NULL, NULL, 'Pending'),
(38, 19, 4, '403', 'Available', 5000.00, 'C:\\Users\\Admin\\Downloads\\rural1.jpg', 'Electric fan, Air-conditioned unit, Curtain / Window blinds, Cabinet / Closet, Study table / Desk, Wall hooks / Hangers', 'Paid'),
(39, 19, 4, '404', 'Available', 0.00, NULL, NULL, 'Pending'),
(40, 19, 4, '405', 'Available', 0.00, NULL, NULL, 'Pending'),
(41, 19, 5, '501', 'Available', 0.00, NULL, NULL, 'Pending'),
(42, 19, 6, '601', 'Available', 0.00, NULL, NULL, 'Pending'),
(43, 19, 6, '602', 'Available', 0.00, NULL, NULL, 'Pending'),
(44, 19, 6, '603', 'Available', 0.00, NULL, NULL, 'Pending'),
(45, 19, 6, '604', 'Available', 0.00, NULL, NULL, 'Pending'),
(46, 19, 6, '605', 'Available', 0.00, NULL, NULL, 'Pending'),
(47, 19, 6, '606', 'Available', 0.00, NULL, NULL, 'Pending'),
(48, 18, 3, '304', 'Maintenance', 123123.00, 'C:\\Users\\Admin\\Downloads\\qr_placeholder.jpg', 'Electric fan, Study table / Desk, Electrical outlets, Smoke detector', 'Paid'),
(49, 20, 1, '101', 'Occupied', 5000.00, 'C:\\Users\\Admin\\Downloads\\urban1.jpg', 'Electric fan, Air-conditioned unit, Curtain / Window blinds, Study table / Desk, Wall hooks / Hangers, Electrical outlets, Extension cord (fixed)', 'Paid'),
(50, 20, 1, '102', 'Available', 0.00, NULL, NULL, 'Pending'),
(51, 20, 1, '103', 'Available', 0.00, NULL, NULL, 'Pending'),
(52, 20, 1, '104', 'Available', 0.00, NULL, NULL, 'Pending'),
(53, 20, 1, '105', 'Available', 0.00, NULL, NULL, 'Pending'),
(54, 20, 1, '106', 'Available', 0.00, NULL, NULL, 'Pending'),
(55, 20, 1, '107', 'Available', 0.00, NULL, NULL, 'Pending'),
(56, 20, 1, '108', 'Available', 0.00, NULL, NULL, 'Pending'),
(57, 20, 2, '201', 'Available', 0.00, NULL, NULL, 'Pending'),
(58, 20, 3, '301', 'Available', 0.00, NULL, NULL, 'Pending'),
(59, 20, 3, '302', 'Available', 0.00, NULL, NULL, 'Pending'),
(60, 20, 3, '303', 'Available', 0.00, NULL, NULL, 'Pending'),
(61, 20, 3, '304', 'Available', 0.00, NULL, NULL, 'Pending'),
(62, 20, 3, '305', 'Available', 0.00, NULL, NULL, 'Pending'),
(63, 20, 3, '306', 'Available', 0.00, NULL, NULL, 'Pending'),
(64, 20, 3, '307', 'Available', 0.00, NULL, NULL, 'Pending'),
(65, 20, 4, '401', 'Available', 0.00, NULL, NULL, 'Pending'),
(66, 20, 4, '402', 'Available', 0.00, NULL, NULL, 'Pending'),
(67, 20, 5, '501', 'Available', 0.00, NULL, NULL, 'Pending'),
(68, 20, 5, '502', 'Available', 0.00, NULL, NULL, 'Pending'),
(69, 20, 5, '503', 'Available', 0.00, NULL, NULL, 'Pending'),
(70, 20, 5, '504', 'Available', 0.00, NULL, NULL, 'Pending'),
(71, 20, 5, '505', 'Available', 0.00, NULL, NULL, 'Pending'),
(72, 20, 5, '506', 'Available', 0.00, NULL, NULL, 'Pending'),
(73, 20, 6, '601', 'Available', 0.00, NULL, NULL, 'Pending'),
(74, 20, 6, '602', 'Available', 0.00, NULL, NULL, 'Pending'),
(75, 20, 6, '603', 'Available', 0.00, NULL, NULL, 'Pending'),
(76, 20, 7, '701', 'Available', 0.00, NULL, NULL, 'Pending'),
(77, 20, 7, '702', 'Available', 0.00, NULL, NULL, 'Pending'),
(78, 20, 7, '703', 'Available', 0.00, NULL, NULL, 'Pending'),
(79, 20, 7, '704', 'Available', 0.00, NULL, NULL, 'Pending'),
(80, 20, 7, '705', 'Available', 0.00, NULL, NULL, 'Pending'),
(81, 20, 8, '801', 'Available', 0.00, NULL, NULL, 'Pending'),
(82, 20, 8, '802', 'Available', 0.00, NULL, NULL, 'Pending'),
(83, 20, 8, '803', 'Available', 0.00, NULL, NULL, 'Pending'),
(84, 20, 8, '804', 'Available', 0.00, NULL, NULL, 'Pending'),
(85, 20, 9, '901', 'Available', 0.00, NULL, NULL, 'Pending'),
(86, 20, 9, '902', 'Available', 0.00, NULL, NULL, 'Pending'),
(96, 22, 1, '101', 'Occupied', 0.00, 'C:\\Users\\Admin\\Downloads\\rural1.jpg', 'Electric fan, Curtain / Window blinds, Cabinet / Closet, Smoke detector', 'Paid'),
(97, 22, 1, '102', 'Available', 0.00, NULL, NULL, 'Pending'),
(98, 22, 1, '103', 'Available', 0.00, NULL, NULL, 'Pending'),
(99, 22, 2, '201', 'Available', 0.00, NULL, NULL, 'Pending'),
(100, 22, 2, '202', 'Available', 0.00, NULL, NULL, 'Pending'),
(101, 22, 2, '203', 'Available', 0.00, NULL, NULL, 'Pending'),
(102, 22, 2, '204', 'Available', 0.00, NULL, NULL, 'Pending'),
(103, 22, 3, '301', 'Available', 0.00, NULL, NULL, 'Pending'),
(104, 22, 3, '302', 'Available', 0.00, NULL, NULL, 'Pending'),
(105, 22, 3, '303', 'Available', 0.00, NULL, NULL, 'Pending'),
(106, 22, 4, '401', 'Available', 0.00, NULL, NULL, 'Pending'),
(107, 22, 4, '402', 'Available', 0.00, NULL, NULL, 'Pending'),
(108, 22, 4, '403', 'Available', 0.00, NULL, NULL, 'Pending'),
(109, 22, 4, '404', 'Available', 0.00, NULL, NULL, 'Pending'),
(110, 22, 5, '501', 'Available', 0.00, NULL, NULL, 'Pending'),
(111, 22, 5, '502', 'Available', 0.00, NULL, NULL, 'Pending'),
(112, 22, 5, '503', 'Available', 0.00, NULL, NULL, 'Pending'),
(113, 22, 6, '601', 'Available', 0.00, NULL, NULL, 'Pending'),
(114, 22, 6, '602', 'Available', 0.00, NULL, NULL, 'Pending'),
(115, 22, 6, '603', 'Available', 0.00, NULL, NULL, 'Pending'),
(116, 22, 6, '604', 'Available', 0.00, NULL, NULL, 'Pending'),
(117, 23, 1, '101', 'Available', 0.00, NULL, NULL, 'Pending'),
(118, 23, 1, '102', 'Available', 0.00, NULL, NULL, 'Pending'),
(119, 23, 1, '103', 'Available', 0.00, NULL, NULL, 'Pending'),
(120, 23, 1, '104', 'Available', 0.00, NULL, NULL, 'Pending'),
(121, 23, 1, '105', 'Available', 0.00, NULL, NULL, 'Pending'),
(122, 23, 1, '106', 'Available', 0.00, NULL, NULL, 'Pending'),
(123, 23, 1, '107', 'Available', 0.00, NULL, NULL, 'Pending'),
(124, 23, 2, '201', 'Available', 0.00, NULL, NULL, 'Pending'),
(125, 23, 2, '202', 'Available', 0.00, NULL, NULL, 'Pending'),
(126, 23, 2, '203', 'Available', 0.00, NULL, NULL, 'Pending'),
(127, 23, 2, '204', 'Available', 0.00, NULL, NULL, 'Pending'),
(128, 23, 2, '205', 'Available', 0.00, NULL, NULL, 'Pending'),
(129, 23, 2, '206', 'Available', 0.00, NULL, NULL, 'Pending'),
(130, 23, 3, '301', 'Available', 0.00, NULL, NULL, 'Pending'),
(131, 23, 3, '302', 'Available', 0.00, NULL, NULL, 'Pending'),
(132, 23, 3, '303', 'Available', 0.00, NULL, NULL, 'Pending'),
(133, 23, 3, '304', 'Available', 0.00, NULL, NULL, 'Pending'),
(134, 23, 3, '305', 'Available', 0.00, NULL, NULL, 'Pending'),
(135, 23, 4, '401', 'Available', 0.00, NULL, NULL, 'Pending'),
(136, 23, 4, '402', 'Available', 0.00, NULL, NULL, 'Pending'),
(137, 23, 4, '403', 'Available', 0.00, NULL, NULL, 'Pending'),
(138, 23, 4, '404', 'Available', 0.00, NULL, NULL, 'Pending'),
(139, 23, 5, '501', 'Available', 0.00, NULL, NULL, 'Pending'),
(140, 23, 5, '502', 'Available', 0.00, NULL, NULL, 'Pending'),
(141, 23, 5, '503', 'Available', 0.00, NULL, NULL, 'Pending'),
(142, 23, 6, '601', 'Available', 0.00, NULL, NULL, 'Pending'),
(143, 23, 6, '602', 'Available', 0.00, NULL, NULL, 'Pending'),
(144, 23, 7, '701', 'Available', 0.00, NULL, NULL, 'Pending'),
(149, 25, 1, '101', 'Available', 0.00, NULL, NULL, 'Pending'),
(150, 25, 1, '102', 'Available', 0.00, NULL, NULL, 'Pending'),
(151, 25, 1, '103', 'Available', 0.00, NULL, NULL, 'Pending'),
(152, 26, 1, '101', 'Available', 0.00, NULL, NULL, 'Pending'),
(153, 26, 1, '102', 'Available', 0.00, NULL, NULL, 'Pending'),
(154, 26, 1, '103', 'Available', 0.00, NULL, NULL, 'Pending'),
(155, 26, 2, '201', 'Available', 0.00, NULL, NULL, 'Pending'),
(156, 26, 2, '202', 'Available', 0.00, NULL, NULL, 'Pending'),
(157, 26, 2, '203', 'Available', 0.00, NULL, NULL, 'Pending'),
(158, 26, 2, '204', 'Available', 0.00, NULL, NULL, 'Pending'),
(159, 26, 3, '301', 'Available', 0.00, NULL, NULL, 'Pending'),
(160, 26, 3, '302', 'Available', 0.00, NULL, NULL, 'Pending'),
(161, 26, 4, '401', 'Available', 0.00, NULL, NULL, 'Pending');

-- --------------------------------------------------------

--
-- Table structure for table `saved_properties`
--

CREATE TABLE `saved_properties` (
  `id` int(11) NOT NULL,
  `tenant_id` int(11) NOT NULL,
  `property_id` int(11) NOT NULL,
  `category` enum('Near Campus','Premium Options','Budget Picks') NOT NULL,
  `saved_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `saved_properties`
--

INSERT INTO `saved_properties` (`id`, `tenant_id`, `property_id`, `category`, `saved_at`) VALUES
(1, 5, 18, 'Near Campus', '2025-12-17 23:50:55'),
(3, 5, 19, 'Near Campus', '2025-12-17 23:59:53');

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

CREATE TABLE `messages` (
  `id` int(11) NOT NULL,
  `application_id` int(11) NOT NULL,
  `sender_id` int(11) NOT NULL,
  `receiver_id` int(11) NOT NULL,
  `message_text` text NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `fullname` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `contact_number` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `business_name` varchar(100) DEFAULT NULL,
  `business_type` varchar(50) DEFAULT NULL,
  `profile_image_path` varchar(255) DEFAULT NULL,
  `role` varchar(50) DEFAULT 'user',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `ewallet_provider` varchar(50) DEFAULT NULL,
  `ewallet_number` varchar(20) DEFAULT NULL,
  `qr_image_path` varchar(255) DEFAULT NULL,
  `about_me` text DEFAULT NULL,
  `emergency_contact_name` varchar(100) DEFAULT NULL,
  `emergency_contact_phone` varchar(20) DEFAULT NULL,
  `emergency_contact_relation` varchar(50) DEFAULT NULL,
  `pref_language` varchar(50) DEFAULT 'English',
  `pref_timezone` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `fullname`, `email`, `contact_number`, `address`, `business_name`, `business_type`, `profile_image_path`, `role`, `created_at`, `ewallet_provider`, `ewallet_number`, `qr_image_path`, `about_me`, `emergency_contact_name`, `emergency_contact_phone`, `emergency_contact_relation`, `pref_language`, `pref_timezone`) VALUES
(1, 'admin', '123', 'Admin User', 'admin@staysmart.com', '09123456789', NULL, NULL, NULL, NULL, 'admin', '2025-12-17 13:58:59', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'English', NULL),
(4, 'pete', '123', 'Pete Zedrick A. Petero', 'pete@gmail.com', '09945822724', 'Blk. 2 Lot 19 Makisama Street Mandug Davao City', 'StaySmart', 'Individual Landlord', 'C:\\Users\\Admin\\Pictures\\Screenshots\\Screenshot 2025-12-07 181323.png', 'owner', '2025-12-17 14:55:55', 'GCash', '09945822724', NULL, NULL, NULL, NULL, NULL, 'English', NULL),
(5, 'zed', '123', 'Tenant Petero', 'zed@gmail.com', '09079904440', 'AWdawdwa', NULL, NULL, 'C:\\Users\\Admin\\Downloads\\qr_placeholder.jpg', 'tenant', '2025-12-17 15:47:43', NULL, NULL, NULL, 'Handsome Tall Guys looking for aparties', 'Dhenny Love Petero', '09936738034', 'Mother dearest', 'English', 'Philippines'),
(6, 'test', '123', 'hehey', 'test@gmail.com', '23232', NULL, NULL, NULL, NULL, 'tenant', '2025-12-17 15:56:27', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'English', NULL),
(7, 'testo', '123', 'Test Zedrick', 'testo@gmail.com', '0909', NULL, NULL, NULL, NULL, 'owner', '2025-12-17 19:33:12', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'English', NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `applications`
--
ALTER TABLE `applications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `room_id` (`room_id`),
  ADD KEY `tenant_id` (`tenant_id`),
  ADD KEY `property_id` (`property_id`);

--
-- Indexes for table `house`
--
ALTER TABLE `house`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `landlord`
--
ALTER TABLE `landlord`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `properties`
--
ALTER TABLE `properties`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `property_floors`
--
ALTER TABLE `property_floors`
  ADD PRIMARY KEY (`id`),
  ADD KEY `property_id` (`property_id`);

--
-- Indexes for table `room`
--
ALTER TABLE `room`
  ADD PRIMARY KEY (`id`),
  ADD KEY `house_id` (`house_id`);

--
-- Indexes for table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `application_id` (`application_id`),
  ADD KEY `sender_id` (`sender_id`),
  ADD KEY `receiver_id` (`receiver_id`);

--
-- Indexes for table `rooms`
--
ALTER TABLE `rooms`
  ADD PRIMARY KEY (`id`),
  ADD KEY `property_id` (`property_id`);

--
-- Indexes for table `saved_properties`
--
ALTER TABLE `saved_properties`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_save` (`tenant_id`,`property_id`),
  ADD KEY `property_id` (`property_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `applications`
--
ALTER TABLE `applications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `messages`
--
ALTER TABLE `messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `house`
--
ALTER TABLE `house`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `landlord`
--
ALTER TABLE `landlord`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `properties`
--
ALTER TABLE `properties`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT for table `property_floors`
--
ALTER TABLE `property_floors`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=103;

--
-- AUTO_INCREMENT for table `room`
--
ALTER TABLE `room`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `rooms`
--
ALTER TABLE `rooms`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=162;

--
-- AUTO_INCREMENT for table `saved_properties`
--
ALTER TABLE `saved_properties`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `applications`
--
ALTER TABLE `applications`
  ADD CONSTRAINT `applications_ibfk_1` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `applications_ibfk_2` FOREIGN KEY (`tenant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `applications_ibfk_3` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `messages_ibfk_3` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `property_floors`
--
ALTER TABLE `property_floors`
  ADD CONSTRAINT `property_floors_ibfk_1` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `room`
--
ALTER TABLE `room`
  ADD CONSTRAINT `room_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`);

--
-- Constraints for table `rooms`
--
ALTER TABLE `rooms`
  ADD CONSTRAINT `rooms_ibfk_1` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `saved_properties`
--
ALTER TABLE `saved_properties`
  ADD CONSTRAINT `saved_properties_ibfk_1` FOREIGN KEY (`tenant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `saved_properties_ibfk_2` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
