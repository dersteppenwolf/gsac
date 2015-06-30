-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: Dataworks_demo
-- ------------------------------------------------------
-- Server version	5.1.73

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
-- Current Database: `Dataworks_demo`
-- derived from unavco test db DATAWORKS_SCHEMA_3.
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `Dataworks_demo` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `Dataworks_demo`;

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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access`
--

LOCK TABLES `access` WRITE;
/*!40000 ALTER TABLE `access` DISABLE KEYS */;
INSERT INTO `access` VALUES (1,'no public access allowed',0),(2,'public access allowed for station metadata, instrument metadata, and data files',0),(3,'public access allowed for station and instrument metadata only',0);
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
  `agency_short_name` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`agency_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agency`
--

LOCK TABLES `agency` WRITE;
/*!40000 ALTER TABLE `agency` DISABLE KEYS */;
INSERT INTO `agency` VALUES (1,'My Agency name','My Agency');
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
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `antenna`
--

LOCK TABLES `antenna` WRITE;
/*!40000 ALTER TABLE `antenna` DISABLE KEYS */;
INSERT INTO `antenna` VALUES (14,'Zephyr Geodetic','N'),(15,'TRM41249.00','N'),(16,'TRM57971.00','N'),(17,'TRM59800.00','N'),(18,'TRM55971.00','N'),(19,'TRM33429.00+GP','N'),(20,'ASH701945B_M','N'),(21,'TRM14532.00','N'),(22,'TRM22020.00+GP','N'),(23,'TRM33429.20+GP','N');
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `country`
--

LOCK TABLES `country` WRITE;
/*!40000 ALTER TABLE `country` DISABLE KEYS */;
INSERT INTO `country` VALUES (4,'My country');
/*!40000 ALTER TABLE `country` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datafile`
--

DROP TABLE IF EXISTS `datafile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datafile` (
  `datafile_id` int(9) unsigned NOT NULL AUTO_INCREMENT,
  `station_id` int(6) unsigned NOT NULL,
  `equip_config_id` int(6) unsigned DEFAULT NULL,
  `datafile_name` varchar(120) NOT NULL,
  `original_datafile_name` varchar(100) DEFAULT NULL,
  `datafile_type_id` int(3) unsigned NOT NULL,
  `sample_interval` float NOT NULL,
  `datafile_start_time` datetime NOT NULL,
  `datafile_stop_time` datetime NOT NULL,
  `year` year(4) NOT NULL,
  `day_of_year` int(3) NOT NULL,
  `published_time` datetime NOT NULL,
  `size_bytes` int(10) NOT NULL,
  `MD5` char(32) NOT NULL,
  `URL_path` varchar(120) NOT NULL,
  PRIMARY KEY (`datafile_id`),
  KEY `station_id_idx` (`station_id`),
  KEY `equip_config_id_idx` (`equip_config_id`),
  KEY `datafile_type_id_idx` (`datafile_type_id`),
  CONSTRAINT `datafile_type_id` FOREIGN KEY (`datafile_type_id`) REFERENCES `datafile_type` (`datafile_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `equip_config_id` FOREIGN KEY (`equip_config_id`) REFERENCES `equip_config` (`equip_config_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `station_id2` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=50070 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile`
--

LOCK TABLES `datafile` WRITE;
/*!40000 ALTER TABLE `datafile` DISABLE KEYS */;
INSERT INTO `datafile` VALUES (49625,54,145,'IRI31620.15d.Z','5024K68287201506110000a.CRX',2,30,'2015-06-11 00:00:00','2015-06-11 23:59:30',2015,162,'2015-06-23 17:09:25',1142167,'84e87c45246a67a23c2a4100d4dfe285','ftp://dataworks1/rinex/obs/2015/162/IRI31620.15d.Z'),(49626,54,145,'IRI31600.15d.Z','5024K68287201506090000a.CRX',2,30,'2015-06-09 00:00:00','2015-06-09 23:59:30',2015,160,'2015-06-23 17:09:25',1179692,'f2e29f339ddbb3514f38a4e95ff8869b','ftp://dataworks1/rinex/obs/2015/160/IRI31600.15d.Z'),(49628,54,145,'IRI31640.15d.Z','5024K68287201506130000a.CRX',2,30,'2015-06-13 00:00:00','2015-06-13 23:59:30',2015,164,'2015-06-23 17:11:30',1218951,'1584fbdc5df934a78c87649ed4f8f8ce','ftp://dataworks1/rinex/obs/2015/164/IRI31640.15d.Z'),(49630,54,145,'IRI31560.15d.Z','5024K68287201506050000a.CRX',2,30,'2015-06-05 00:00:00','2015-06-05 23:59:30',2015,156,'2015-06-23 17:05:17',1162189,'464c1621bcb0e647a4f77b1e357bdc28','ftp://dataworks1/rinex/obs/2015/156/IRI31560.15d.Z'),(49631,54,145,'IRI31550.15d.Z','5024K68287201506040000a.CRX',2,30,'2015-06-04 00:00:00','2015-06-04 23:59:30',2015,155,'2015-06-23 17:05:17',1134544,'1750cc8c287873e403556398c25dea89','ftp://dataworks1/rinex/obs/2015/155/IRI31550.15d.Z'),(49632,54,145,'IRI31590.15d.Z','5024K68287201506080000a.CRX',2,30,'2015-06-08 00:00:00','2015-06-08 23:59:30',2015,159,'2015-06-23 17:07:21',1150483,'a7f987d3015b06eb81654e978ba43048','ftp://dataworks1/rinex/obs/2015/159/IRI31590.15d.Z'),(49633,54,145,'IRI31470.15d.Z','5024K68287201505270000a.CRX',2,30,'2015-05-27 00:00:00','2015-05-27 23:59:30',2015,147,'2015-06-23 16:59:05',1188363,'13262b398adf041c3a3591a710d12b93','ftp://dataworks1/rinex/obs/2015/147/IRI31470.15d.Z'),(49634,54,145,'IRI31530.15d.Z','5024K68287201506020000a.CRX',2,30,'2015-06-02 00:00:00','2015-06-02 23:59:30',2015,153,'2015-06-23 17:03:13',1188015,'bbf07e3c8f7c33afc4d866ab97c238ec','ftp://dataworks1/rinex/obs/2015/153/IRI31530.15d.Z'),(49635,54,145,'IRI31650.15d.Z','5024K68287201506140000a.CRX',2,30,'2015-06-14 00:00:00','2015-06-14 23:59:30',2015,165,'2015-06-23 17:11:30',1178533,'c07e0ef6ae7f238d3eb9399b694581d2','ftp://dataworks1/rinex/obs/2015/165/IRI31650.15d.Z'),(49637,54,145,'IRI31480.15d.Z','5024K68287201505280000a.CRX',2,30,'2015-05-28 00:00:00','2015-05-28 23:59:30',2015,148,'2015-06-23 17:01:09',1123937,'f63748e8dc95d871a754bbe0572beae4','ftp://dataworks1/rinex/obs/2015/148/IRI31480.15d.Z'),(49639,54,145,'IRI31580.15d.Z','5024K68287201506070000a.CRX',2,30,'2015-06-07 00:00:00','2015-06-07 23:59:30',2015,158,'2015-06-23 17:07:21',1153315,'55e978711b93ea74febb7db6cdd5791f','ftp://dataworks1/rinex/obs/2015/158/IRI31580.15d.Z'),(49640,54,145,'IRI31630.15d.Z','5024K68287201506120000a.CRX',2,30,'2015-06-12 00:00:00','2015-06-12 23:59:30',2015,163,'2015-06-23 17:09:25',1215645,'2618faa986f8800feb8079767f024092','ftp://dataworks1/rinex/obs/2015/163/IRI31630.15d.Z'),(49644,54,145,'IRI31570.15d.Z','5024K68287201506060000a.CRX',2,30,'2015-06-06 00:00:00','2015-06-06 23:59:30',2015,157,'2015-06-23 17:07:21',1194251,'e50e1aade258568fa63bf9fe83de6a08','ftp://dataworks1/rinex/obs/2015/157/IRI31570.15d.Z'),(49646,54,145,'IRI31610.15d.Z','5024K68287201506100000a.CRX',2,30,'2015-06-10 00:00:00','2015-06-10 23:59:30',2015,161,'2015-06-23 17:09:25',1178099,'01e17449675a4ca8801148481e0e4571','ftp://dataworks1/rinex/obs/2015/161/IRI31610.15d.Z');
/*!40000 ALTER TABLE `datafile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datafile_type`
--

DROP TABLE IF EXISTS `datafile_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datafile_type` (
  `datafile_type_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `datafile_type_name` varchar(50) NOT NULL,
  `datafile_type_version` varchar(50) NOT NULL,
  `datafile_type_description` varchar(50) NOT NULL,
  PRIMARY KEY (`datafile_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile_type`
--

LOCK TABLES `datafile_type` WRITE;
/*!40000 ALTER TABLE `datafile_type` DISABLE KEYS */;
INSERT INTO `datafile_type` VALUES (1,'instrument data file','','Any type or format of native, raw, or binary file '),(2,'RINEX observation file','','a RINEX \'o\' obs file; may be compressed'),(3,'RINEX GPS navigation file','','a RINEX \'n\' nav file; may be compressed'),(4,'RINEX Galileo navigation file','','a RINEX \'e\' nav file; may be compressed'),(5,'RINEX GLONASS navigation file','','a RINEX \'g\' nav file; may be compressed'),(6,'RINEX meteorology file','','a RINEX \'m\' met file; may be compressed'),(7,'RINEX QZSS navigation file','','a RINEX \'j\' nav file; may be compressed'),(8,'RINEX Beidou navigation file','','a RINEX \'c\' nav file; may be compressed');
/*!40000 ALTER TABLE `datafile_type` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ellipsoid`
--

LOCK TABLES `ellipsoid` WRITE;
/*!40000 ALTER TABLE `ellipsoid` DISABLE KEYS */;
INSERT INTO `ellipsoid` VALUES (1,'WGS 84','WGS 84'),(2,'GRS 80','GRS 80'),(3,'PZ-90','PZ-90');
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
  `equip_config_stop_time` datetime DEFAULT NULL,
  `antenna_id` int(3) unsigned NOT NULL,
  `antenna_serial_number` varchar(20) NOT NULL,
  `antenna_height` float NOT NULL,
  `metpack_id` int(3) unsigned DEFAULT NULL,
  `metpack_serial_number` varchar(20) DEFAULT NULL,
  `radome_id` int(3) unsigned NOT NULL,
  `radome_serial_number` varchar(20) NOT NULL,
  `receiver_firmware_id` int(3) unsigned NOT NULL,
  `receiver_serial_number` varchar(20) NOT NULL,
  `satellite_system` varchar(20) DEFAULT NULL,
  `sample_interval` float DEFAULT NULL,
  PRIMARY KEY (`equip_config_id`),
  KEY `station_id_idx` (`station_id`),
  KEY `antenna_id_idx` (`antenna_id`),
  KEY `metpack_id_idx` (`metpack_id`),
  KEY `receiver_firmware_id_idx` (`receiver_firmware_id`),
  KEY `radome_id_idx` (`radome_id`),
  CONSTRAINT `antenna_id` FOREIGN KEY (`antenna_id`) REFERENCES `antenna` (`antenna_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `metpack_id` FOREIGN KEY (`metpack_id`) REFERENCES `metpack` (`metpack_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `radome_id` FOREIGN KEY (`radome_id`) REFERENCES `radome` (`radome_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `receiver_firmware_id` FOREIGN KEY (`receiver_firmware_id`) REFERENCES `receiver_firmware` (`receiver_firmware_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `station_id` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=264 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equip_config`
--

LOCK TABLES `equip_config` WRITE;
/*!40000 ALTER TABLE `equip_config` DISABLE KEYS */;
INSERT INTO `equip_config` VALUES (145,54,'2012-02-12 00:00:00','2012-02-12 00:00:00','0000-00-00 00:00:00',14,'60142329',0,6,'\n',4,'unspecified',43,'5024K68287','GPS',NULL),(205,115,'2015-06-26 16:17:57','2005-04-27 22:40:30','2005-10-07 23:59:30',15,'60100911',0.077,NULL,NULL,1,' ',44,'153273569','GPS',15);
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
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locale`
--

LOCK TABLES `locale` WRITE;
/*!40000 ALTER TABLE `locale` DISABLE KEYS */;
INSERT INTO `locale` VALUES (33,'Boulder Colorado');
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metpack`
--

LOCK TABLES `metpack` WRITE;
/*!40000 ALTER TABLE `metpack` DISABLE KEYS */;
INSERT INTO `metpack` VALUES (1,'no metpack'),(2,'WXT520'),(4,''),(5,'WXT510'),(6,'None');
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monument_style`
--

LOCK TABLES `monument_style` WRITE;
/*!40000 ALTER TABLE `monument_style` DISABLE KEYS */;
INSERT INTO `monument_style` VALUES (1,'shallow foundation pillar'),(2,'building roof'),(3,'deep-drilled braced'),(4,'shallow-drilled braced'),(5,'rock-pin'),(6,'Building Roof Top'),(7,'permanent station unspecified');
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `network`
--

LOCK TABLES `network` WRITE;
/*!40000 ALTER TABLE `network` DISABLE KEYS */;
INSERT INTO `network` VALUES (4,'(test)'),(5,'my_network');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `radome`
--

LOCK TABLES `radome` WRITE;
/*!40000 ALTER TABLE `radome` DISABLE KEYS */;
INSERT INTO `radome` VALUES (1,'NONE','N'),(2,'SCIT','N'),(3,'SCIS','N'),(4,'Unknown','N');
/*!40000 ALTER TABLE `radome` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receiver_firmware`
--

DROP TABLE IF EXISTS `receiver_firmware`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `receiver_firmware` (
  `receiver_firmware_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `receiver_name` varchar(20) NOT NULL,
  `receiver_firmware` varchar(20) NOT NULL,
  `igs_defined` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`receiver_firmware_id`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receiver_firmware`
--

LOCK TABLES `receiver_firmware` WRITE;
/*!40000 ALTER TABLE `receiver_firmware` DISABLE KEYS */;
INSERT INTO `receiver_firmware` VALUES (43,'NetR9','4.85','N'),(44,'TRIMBLE NETRS','1.0-3','N'),(45,'TRIMBLE NETRS','1.1-3','N'),(46,'TRIMBLE NETRS','1.1-5','N'),(47,'TRIMBLE NETRS','1.3-2','N'),(48,'TRIMBLE NETR9','4.85','N'),(49,'TRIMBLE NETRS','1.1-2','N'),(50,'TRIMBLE NETRS','1.2-5','N'),(51,'TRIMBLE NETR9','4.17','N'),(52,'TRIMBLE NETR9','4.62','N'),(53,'TRIMBLE NETR9','4.70','N'),(54,'TRIMBLE 4000SSE','7.29/3.07','N'),(55,'TRIMBLE 4700','1.37','N'),(56,'TRIMBLE 4000SSI','7.19b','N'),(57,'TRIMBLE NETRS','1.1-1','N'),(58,'TRIMBLE 4000SSE','7.19/3.04','N');
/*!40000 ALTER TABLE `receiver_firmware` ENABLE KEYS */;
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
  `retired_date` datetime DEFAULT NULL,
  `style_id` int(3) unsigned NOT NULL,
  `status_id` int(3) unsigned NOT NULL,
  `access_id` int(3) unsigned NOT NULL,
  `monument_style_id` int(3) unsigned NOT NULL,
  `country_id` int(3) unsigned NOT NULL,
  `locale_id` int(3) unsigned NOT NULL,
  `ellipsoid_id` int(1) unsigned NOT NULL,
  `iers_domes` char(9) DEFAULT NULL,
  `operator_agency_id` int(3) unsigned DEFAULT NULL,
  `data_publisher_agency_id` int(3) unsigned DEFAULT NULL,
  `network_id` int(5) unsigned NOT NULL,
  `mirrored_from_URL` varchar(120) DEFAULT NULL,
  `station_image_URL` varchar(120) DEFAULT NULL,
  `time_series_URL` varchar(120) DEFAULT NULL,
  PRIMARY KEY (`station_id`),
  KEY `style_id_idx` (`style_id`),
  KEY `status_id_idx` (`status_id`),
  KEY `access_id_idx` (`access_id`),
  KEY `monument_style_id_idx` (`monument_style_id`),
  KEY `country_id_idx` (`country_id`),
  KEY `locale_id_idx` (`locale_id`),
  KEY `ellipsoid_id_idx` (`ellipsoid_id`),
  KEY `operator_agency_id_idx` (`operator_agency_id`),
  KEY `data_publisher_agency_id_idx` (`data_publisher_agency_id`),
  KEY `network_id_idx` (`network_id`),
  CONSTRAINT `access_id` FOREIGN KEY (`access_id`) REFERENCES `access` (`access_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `country_id` FOREIGN KEY (`country_id`) REFERENCES `country` (`country_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `data_publisher_agency_id` FOREIGN KEY (`data_publisher_agency_id`) REFERENCES `agency` (`agency_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `ellipsoid_id` FOREIGN KEY (`ellipsoid_id`) REFERENCES `ellipsoid` (`ellipsoid_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `locale_id` FOREIGN KEY (`locale_id`) REFERENCES `locale` (`locale_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `monument_style_id` FOREIGN KEY (`monument_style_id`) REFERENCES `monument_style` (`monument_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `network_id` FOREIGN KEY (`network_id`) REFERENCES `network` (`network_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `operator_agency_id` FOREIGN KEY (`operator_agency_id`) REFERENCES `agency` (`agency_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `status_id` FOREIGN KEY (`status_id`) REFERENCES `station_status` (`station_status_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `style_id` FOREIGN KEY (`style_id`) REFERENCES `station_style` (`station_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station`
--

LOCK TABLES `station` WRITE;
/*!40000 ALTER TABLE `station` DISABLE KEYS */;
INSERT INTO `station` VALUES (54,'IRI3','IRI3 Test Station',40.061253,-105.206251,1571.56,'2012-02-12 00:00:00','2015-06-21 23:59:30',NULL,1,1,2,6,4,33,1,'',1,1,4,NULL,'http://www.unavco.org/data/gps-gnss/lib/images/station_images/IRI3.jpg','http://pboshared.unavco.org/timeseries/IRI3_timeseries_cleaned.png');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station_style`
--

LOCK TABLES `station_style` WRITE;
/*!40000 ALTER TABLE `station_style` DISABLE KEYS */;
INSERT INTO `station_style` VALUES (1,'GPS/GNSS Continuous'),(2,'GPS/GNSS Campaign'),(3,'GPS/GNSS Mobile');
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

-- Dump completed on 2015-06-30 18:40:24
