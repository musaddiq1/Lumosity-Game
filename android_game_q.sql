-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 01, 2020 at 07:48 PM
-- Server version: 10.4.11-MariaDB
-- PHP Version: 7.4.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `android_game_q`
--

-- --------------------------------------------------------

--
-- Table structure for table `friends`
--

CREATE TABLE `friends` (
  `ID` int(100) NOT NULL,
  `status` varchar(100) DEFAULT NULL,
  `sender` varchar(100) DEFAULT NULL,
  `receiver` varchar(100) DEFAULT NULL,
  `text` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `game_info`
--

CREATE TABLE `game_info` (
  `id` int(100) NOT NULL,
  `userID` int(100) NOT NULL DEFAULT 0,
  `coins` varchar(100) NOT NULL,
  `gameWon` varchar(100) NOT NULL,
  `gameLost` int(100) NOT NULL,
  `currentBalls` varchar(100) DEFAULT 'SILVER_BALL',
  `goldPurhcased` int(100) DEFAULT NULL,
  `diamondPurhcased` int(100) DEFAULT NULL,
  `best_game_won` int(100) DEFAULT 0,
  `rank` int(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `game_info`
--

INSERT INTO `game_info` (`id`, `userID`, `coins`, `gameWon`, `gameLost`, `currentBalls`, `goldPurhcased`, `diamondPurhcased`, `best_game_won`, `rank`) VALUES
(4, 1, '10000', '0', 0, 'SILVER_BALL', NULL, NULL, 0, NULL),
(5, 2, '10000', '0', 0, 'SILVER_BALL', NULL, NULL, 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `profile` varchar(100) NOT NULL,
  `provider` varchar(100) NOT NULL,
  `provider_id` varchar(100) NOT NULL,
  `status` varchar(100) NOT NULL,
  `code` varchar(100) NOT NULL,
  `pings` int(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `email`, `name`, `password`, `profile`, `provider`, `provider_id`, `status`, `code`, `pings`) VALUES
(1, 'hassandanial500@gmail.com', 'Hassan', '12345678', '{\"provider\":\"Qath\",\"image\":\"none\"}', 'USER_PROVIDER_APP_AUTH', '-99', 'approved', '-99', NULL),
(2, 'haris.shahid@gmail.com', 'Haris', '11223344', '{\"provider\":\"Qath\",\"image\":\"none\"}', 'USER_PROVIDER_APP_AUTH', '-99', 'approved', '-99', NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `friends`
--
ALTER TABLE `friends`
  ADD PRIMARY KEY (`ID`);

--
-- Indexes for table `game_info`
--
ALTER TABLE `game_info`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `friends`
--
ALTER TABLE `friends`
  MODIFY `ID` int(100) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `game_info`
--
ALTER TABLE `game_info`
  MODIFY `id` int(100) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(100) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
