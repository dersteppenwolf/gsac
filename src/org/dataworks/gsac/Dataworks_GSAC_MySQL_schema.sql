-- MySQL dump 10.13  Distrib 5.1.69, for debian-linux-gnu (i486)
--
-- Host: localhost    Database: Dataworks_GSAC
-- ------------------------------------------------------
-- Server version	5.1.69

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `Dataworks_GSAC`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `Dataworks_GSAC` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `Dataworks_GSAC`;

--
-- Table structure for table `access`
--

DROP TABLE IF EXISTS `access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `access` (
  `access_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `access_description` varchar(80) NOT NULL,
  `embargo_duration_days` int(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`access_id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access`
--

LOCK TABLES `access` WRITE;
/*!40000 ALTER TABLE `access` DISABLE KEYS */;
INSERT INTO `access` VALUES (1,'no public access allowed',0),(2,'public access allowed for station metadata, instrument metadata, and data files',0),(3,'public access allowed for station and  instrument metadata only',0);
/*!40000 ALTER TABLE `access` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `agency`
--

DROP TABLE IF EXISTS `agency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agency` (
  `agency_id` int(4) unsigned NOT NULL AUTO_INCREMENT,
  `agency_name` varchar(100) NOT NULL,
  `agency_short_name` varchar(10) NOT NULL,
  PRIMARY KEY (`agency_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agency`
--

LOCK TABLES `agency` WRITE;
/*!40000 ALTER TABLE `agency` DISABLE KEYS */;
/*!40000 ALTER TABLE `agency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `antenna`
--

DROP TABLE IF EXISTS `antenna`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `antenna` (
  `antenna_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `antenna_name` varchar(15) NOT NULL,
  `igs_defined` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`antenna_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `antenna`
--

LOCK TABLES `antenna` WRITE;
/*!40000 ALTER TABLE `antenna` DISABLE KEYS */;
/*!40000 ALTER TABLE `antenna` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `country`
--

DROP TABLE IF EXISTS `country`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `country` (
  `country_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `country_name` varchar(70) NOT NULL,
  PRIMARY KEY (`country_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `country`
--

LOCK TABLES `country` WRITE;
/*!40000 ALTER TABLE `country` DISABLE KEYS */;
/*!40000 ALTER TABLE `country` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datafile`
--

DROP TABLE IF EXISTS `datafile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datafile` (
  `datafile_id` int(9) NOT NULL AUTO_INCREMENT,
  `station_id` int(6) NOT NULL,
  `equip_config_id` int(6) NOT NULL,
  `datafile_name` varchar(120) NOT NULL,
  `datafile_format_id` int(3) NOT NULL,
  `sample_interval` float NOT NULL,
  `datafile_start_time` datetime NOT NULL,
  `datafile_stop_time` datetime NOT NULL,
  `year` year(4) NOT NULL,
  `day_of_year` int(3) DEFAULT NULL,
  `published_time` datetime NOT NULL,
  `size_bytes` int(10) NOT NULL,
  `MD5` char(32) NOT NULL,
  `url_path` varchar(120) NOT NULL,
  PRIMARY KEY (`datafile_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile`
--

LOCK TABLES `datafile` WRITE;
/*!40000 ALTER TABLE `datafile` DISABLE KEYS */;
/*!40000 ALTER TABLE `datafile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datafile_format`
--

DROP TABLE IF EXISTS `datafile_format`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datafile_format` (
  `datafile_format_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `datafile_format_name` varchar(50) NOT NULL,
  `datafile_format_version` varchar(50) NOT NULL,
  `datafile_format_description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`datafile_format_id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile_format`
--

LOCK TABLES `datafile_format` WRITE;
/*!40000 ALTER TABLE `datafile_format` DISABLE KEYS */;
INSERT INTO `datafile_format` VALUES (1,'instrument data file','','Any type or format of native, raw, or binary file from an instrument.'),(2,'RINEX observation file','','RINEX obs file of any type: o, d, .zip, d.zip, etc.'),(3,'RINEX GPS navigation file','',''),(4,'RINEX Galileo navigation file','',''),(5,'RINEX GLONASS navigation file','',''),(6,'RINEX meteorology file','','');
/*!40000 ALTER TABLE `datafile_format` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ellipsoid`
--

DROP TABLE IF EXISTS `ellipsoid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ellipsoid` (
  `ellipsoid_id` int(4) unsigned NOT NULL AUTO_INCREMENT,
  `ellipsoid_name` varchar(45) NOT NULL,
  `ellipsoid_short_name` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`ellipsoid_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ellipsoid`
--

LOCK TABLES `ellipsoid` WRITE;
/*!40000 ALTER TABLE `ellipsoid` DISABLE KEYS */;
INSERT INTO `ellipsoid` VALUES (1,'WGS 84','WGS 84'),(2,'GRS 80','GRS 80');
/*!40000 ALTER TABLE `ellipsoid` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equip_config`
--

DROP TABLE IF EXISTS `equip_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `equip_config` (
  `equip_config_id` int(6) unsigned NOT NULL AUTO_INCREMENT,
  `station_id` int(6) unsigned NOT NULL,
  `create_time` datetime NOT NULL,
  `equip_config_start_time` datetime NOT NULL,
  `equip_config_stop_time` datetime NOT NULL,
  `antenna_id` int(3) unsigned NOT NULL,
  `antenna_serial_number` varchar(20) NOT NULL,
  `antenna_height` float NOT NULL,
  `metpack_id` int(3) unsigned DEFAULT NULL,
  `metpack_serial_number` varchar(20) DEFAULT NULL,
  `radome_id` int(3) unsigned NOT NULL,
  `radome_serial_number` varchar(20) NOT NULL,
  `receiver_firmware_id` int(3) unsigned NOT NULL,
  `receiver_firmware_vers` varchar(20) NOT NULL,
  `receiver_serial_number` varchar(20) NOT NULL,
  `satellite_system` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`equip_config_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equip_config`
--

LOCK TABLES `equip_config` WRITE;
/*!40000 ALTER TABLE `equip_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `equip_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locale`
--

DROP TABLE IF EXISTS `locale`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `locale` (
  `locale_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `locale_info` varchar(70) NOT NULL,
  PRIMARY KEY (`locale_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locale`
--

LOCK TABLES `locale` WRITE;
/*!40000 ALTER TABLE `locale` DISABLE KEYS */;
/*!40000 ALTER TABLE `locale` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metpack`
--

DROP TABLE IF EXISTS `metpack`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metpack` (
  `metpack_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `metpack_name` varchar(15) NOT NULL,
  PRIMARY KEY (`metpack_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metpack`
--

LOCK TABLES `metpack` WRITE;
/*!40000 ALTER TABLE `metpack` DISABLE KEYS */;
INSERT INTO `metpack` VALUES (1,'no metpack');
/*!40000 ALTER TABLE `metpack` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monument_style`
--

DROP TABLE IF EXISTS `monument_style`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `monument_style` (
  `monument_style_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `monument_style_description` varchar(70) NOT NULL,
  PRIMARY KEY (`monument_style_id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monument_style`
--

LOCK TABLES `monument_style` WRITE;
/*!40000 ALTER TABLE `monument_style` DISABLE KEYS */;
INSERT INTO `monument_style` VALUES (1,'INOX METAL PILLAR'),(2,'METAL PILLAR'),(3,'PILLAR'),(4,'STEEL MAST');
/*!40000 ALTER TABLE `monument_style` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network`
--

DROP TABLE IF EXISTS `network`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `network` (
  `network_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `network_name` varchar(50) NOT NULL,
  PRIMARY KEY (`network_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `network`
--

LOCK TABLES `network` WRITE;
/*!40000 ALTER TABLE `network` DISABLE KEYS */;
/*!40000 ALTER TABLE `network` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `radome`
--

DROP TABLE IF EXISTS `radome`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `radome` (
  `radome_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `radome_name` varchar(15) NOT NULL,
  `igs_defined` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`radome_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `radome`
--

LOCK TABLES `radome` WRITE;
/*!40000 ALTER TABLE `radome` DISABLE KEYS */;
/*!40000 ALTER TABLE `radome` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receiver`
--

DROP TABLE IF EXISTS `receiver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `receiver` (
  `receiver_firmware_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `receiver_name` varchar(20) NOT NULL,
  `igs_defined` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`receiver_firmware_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receiver`
--

LOCK TABLES `receiver` WRITE;
/*!40000 ALTER TABLE `receiver` DISABLE KEYS */;
/*!40000 ALTER TABLE `receiver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `station`
--

DROP TABLE IF EXISTS `station`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `station` (
  `station_id` int(6) unsigned NOT NULL AUTO_INCREMENT,
  `four_char_name` char(4) NOT NULL,
  `station_name` varchar(50) NOT NULL,
  `latitude_north` double NOT NULL,
  `longitude_east` double NOT NULL,
  `height_above_ellipsoid` float NOT NULL,
  `installed_date` datetime NOT NULL,
  `retired_date` datetime DEFAULT NULL,
  `style_id` int(3) unsigned NOT NULL,
  `status_id` int(3) unsigned NOT NULL,
  `access_id` int(3) unsigned NOT NULL,
  `monument_style_id` int(3) unsigned NOT NULL,
  `country_id` int(3) unsigned NOT NULL,
  `locale_id` int(3) unsigned NOT NULL,
  `ellipsoid_id` int(1) unsigned NOT NULL,
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `z` double DEFAULT NULL,
  `iers_domes` char(9) DEFAULT NULL,
  `station_photo_URL` varchar(100) DEFAULT NULL,
  `time_series_plot_photo_URL` varchar(100) DEFAULT NULL,
  `operator_agency_id` int(3) unsigned NOT NULL,
  `data_publisher_agency_id` int(3) unsigned NOT NULL,
  `network_id` int(5) unsigned NOT NULL,
  `networks` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`station_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station`
--

LOCK TABLES `station` WRITE;
/*!40000 ALTER TABLE `station` DISABLE KEYS */;
/*!40000 ALTER TABLE `station` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `station_status`
--

DROP TABLE IF EXISTS `station_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `station_status` (
  `station_status_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `station_status` varchar(80) NOT NULL,
  PRIMARY KEY (`station_status_id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station_status`
--

LOCK TABLES `station_status` WRITE;
/*!40000 ALTER TABLE `station_status` DISABLE KEYS */;
INSERT INTO `station_status` VALUES (1,'Active'),(2,'Inactive/intermittent'),(3,'Retired'),(4,'Pending');
/*!40000 ALTER TABLE `station_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `station_style`
--

DROP TABLE IF EXISTS `station_style`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `station_style` (
  `station_style_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `station_style_description` varchar(80) NOT NULL,
  PRIMARY KEY (`station_style_id`)
) ENGINE=MyISAM AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station_style`
--

LOCK TABLES `station_style` WRITE;
/*!40000 ALTER TABLE `station_style` DISABLE KEYS */;
INSERT INTO `station_style` VALUES (1,'GPS/GNSS Campaign'),(2,'GPS/GNSS Continuous'),(3,'GPS/GNSS Mobile');
/*!40000 ALTER TABLE `station_style` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-08-13  9:02:40