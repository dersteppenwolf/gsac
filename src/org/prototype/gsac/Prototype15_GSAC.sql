-- MySQL dump 10.13  Distrib 5.1.69, for debian-linux-gnu (i486)
--
-- Host: localhost    Database: Prototype15_GSAC
-- ------------------------------------------------------
-- Server version	5.1.69-0ubuntu0.10.04.1

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
-- Current Database: `Prototype15_GSAC`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `Prototype15_GSAC` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `Prototype15_GSAC`;

--
-- Table structure for table `access`
--

DROP TABLE IF EXISTS `access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `access` (
  `access_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `access_description` varchar(80) NOT NULL,
  PRIMARY KEY (`access_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access`
--

LOCK TABLES `access` WRITE;
/*!40000 ALTER TABLE `access` DISABLE KEYS */;
INSERT INTO `access` VALUES (1,'no public access allowed'),(2,'public access allowed for station metadata, instrument metadata, and data files'),(3,'public access allowed for station and instrument metadata only');
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
  PRIMARY KEY (`agency_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agency`
--

LOCK TABLES `agency` WRITE;
/*!40000 ALTER TABLE `agency` DISABLE KEYS */;
INSERT INTO `agency` VALUES (1,'Agency1'),(2,'Agency2');
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `antenna`
--

LOCK TABLES `antenna` WRITE;
/*!40000 ALTER TABLE `antenna` DISABLE KEYS */;
INSERT INTO `antenna` VALUES (1,'TRM57971.00','Y'),(2,'TRM59800.00','Y'),(3,'ASH701945B_M','Y'),(4,'TRM55971.00','Y'),(5,'TRM41249.00','N'),(6,'TRM22020.00+GP','N'),(7,'AOAD/M_T','N'),(8,'TRM14532.00','N'),(9,'ASH700936D_M','N'),(10,'TRM29659.00','N'),(11,'TRM22020.00-GP','N');
/*!40000 ALTER TABLE `antenna` ENABLE KEYS */;
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
  `sample_interval` float DEFAULT NULL,
  `datafile_start_time` datetime NOT NULL,
  `datafile_stop_time` datetime NOT NULL,
  `year` year(4) NOT NULL,
  `day_of_year` int(3) NOT NULL,
  `published_time` datetime NOT NULL,
  `size_bytes` int(10) NOT NULL,
  `MD5` char(32) NOT NULL,
  `URL_path_protocol` varchar(7) DEFAULT NULL,
  `URL_path_domain` varchar(50) DEFAULT NULL,
  `URL_path_dirs` varchar(70) DEFAULT NULL,
  `URL_all` varchar(120) DEFAULT NULL,
  PRIMARY KEY (`datafile_id`),
  KEY `station_id_idx` (`station_id`),
  KEY `equip_config_id_idx` (`equip_config_id`),
  KEY `datafile_type_id_idx` (`datafile_type_id`),
  CONSTRAINT `datafile_type_id` FOREIGN KEY (`datafile_type_id`) REFERENCES `datafile_type` (`datafile_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `equip_config_id` FOREIGN KEY (`equip_config_id`) REFERENCES `equip_config` (`equip_config_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `station_id2` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile`
--

LOCK TABLES `datafile` WRITE;
/*!40000 ALTER TABLE `datafile` DISABLE KEYS */;
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
  `datafile_type_description` varchar(50) NOT NULL,
  PRIMARY KEY (`datafile_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile_type`
--

LOCK TABLES `datafile_type` WRITE;
/*!40000 ALTER TABLE `datafile_type` DISABLE KEYS */;
INSERT INTO `datafile_type` VALUES (1,'instrument data file','Any type or format of native, raw, or binary file '),(2,'RINEX observation file','a RINEX \'o\' obs file; may be compressed'),(3,'RINEX GPS navigation file','a RINEX \'n\' nav file; may be compressed'),(4,'RINEX Galileo navigation file','a RINEX \'e\' nav file; may be compressed'),(5,'RINEX GLONASS navigation file','a RINEX \'g\' nav file; may be compressed'),(6,'RINEX meteorology file','a RINEX \'m\' met file; may be compressed'),(7,'RINEX QZSS navigation file','a RINEX \'j\' nav file; may be compressed'),(8,'RINEX Beidou navigation file','a RINEX \'c\' nav file; may be compressed');
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
  `equip_config_start_time` datetime NOT NULL,
  `equip_config_stop_time` datetime DEFAULT NULL,
  `db_update_time` datetime NOT NULL,
  `antenna_id` int(3) unsigned NOT NULL,
  `antenna_serial_number` varchar(20) DEFAULT NULL,
  `antenna_height` float DEFAULT NULL,
  `metpack_id` int(3) unsigned DEFAULT NULL,
  `metpack_serial_number` varchar(20) DEFAULT NULL,
  `radome_id` int(3) unsigned NOT NULL,
  `radome_serial_number` varchar(20) DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equip_config`
--

LOCK TABLES `equip_config` WRITE;
/*!40000 ALTER TABLE `equip_config` DISABLE KEYS */;
INSERT INTO `equip_config` VALUES (1,2,'2014-09-04 00:00:00','2014-12-17 23:59:45','2014-11-15 00:17:41',2,'5343354885',0.0083,2,'K2630028',2,' ',1,'5250K40670','GPS',15),(2,1,'2014-08-15 14:30:30','2014-12-16 23:59:45','2014-11-15 00:17:41',1,'5000112724',0.5,2,'',1,' ',1,'5137K78333','GPS',15);
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
  `locale_name` varchar(70) NOT NULL,
  PRIMARY KEY (`locale_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locale`
--

LOCK TABLES `locale` WRITE;
/*!40000 ALTER TABLE `locale` DISABLE KEYS */;
INSERT INTO `locale` VALUES (1,'locale1'),(2,'locale2');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metpack`
--

LOCK TABLES `metpack` WRITE;
/*!40000 ALTER TABLE `metpack` DISABLE KEYS */;
INSERT INTO `metpack` VALUES (1,'WXT510'),(2,'WXT520');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monument_style`
--

LOCK TABLES `monument_style` WRITE;
/*!40000 ALTER TABLE `monument_style` DISABLE KEYS */;
INSERT INTO `monument_style` VALUES (1,'shallow foundation pillar'),(2,'building roof'),(3,'deep-drilled braced'),(4,'shallow-drilled braced');
/*!40000 ALTER TABLE `monument_style` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nation`
--

DROP TABLE IF EXISTS `nation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nation` (
  `nation_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `nation_name` varchar(70) NOT NULL,
  PRIMARY KEY (`nation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nation`
--

LOCK TABLES `nation` WRITE;
/*!40000 ALTER TABLE `nation` DISABLE KEYS */;
INSERT INTO `nation` VALUES (1,'nation name');
/*!40000 ALTER TABLE `nation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `province_state`
--

DROP TABLE IF EXISTS `province_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `province_state` (
  `province_state_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `province_state_name` varchar(70) NOT NULL,
  PRIMARY KEY (`province_state_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `province_state`
--

LOCK TABLES `province_state` WRITE;
/*!40000 ALTER TABLE `province_state` DISABLE KEYS */;
INSERT INTO `province_state` VALUES (1,'province name 1');
/*!40000 ALTER TABLE `province_state` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `radome`
--

LOCK TABLES `radome` WRITE;
/*!40000 ALTER TABLE `radome` DISABLE KEYS */;
INSERT INTO `radome` VALUES (1,'NONE','Y'),(2,'SCIT','Y');
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
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receiver_firmware`
--

LOCK TABLES `receiver_firmware` WRITE;
/*!40000 ALTER TABLE `receiver_firmware` DISABLE KEYS */;
INSERT INTO `receiver_firmware` VALUES (1,'TRIMBLE NETR9','4.85','Y'),(2,'TRIMBLE 4000SSI','7.19b','Y'),(3,'TRIMBLE NETRS','1.1-1','Y'),(4,'TRIMBLE NETRS','1.1-2','Y'),(5,'TRIMBLE NETR5','4.03','Y'),(6,'TRIMBLE 5700','1.30','N'),(7,'TPS GB-1000','3.1','N'),(8,'TRIMBLE 4000SSI','7.12','N'),(9,'ROGUE SNR-8000','2.8','N'),(10,'TRIMBLE NETR9','4.60','N'),(11,'TRIMBLE R7','2.21','N'),(12,'TRIMBLE 5700','1.24','N'),(13,'TRIMBLE 4000SST','4.64','N'),(14,'ASHTECH Z-XII3','1E95','N'),(15,'ASHTECH Z-XII3','1E86','N'),(16,'TRIMBLE 4000SSI','7.19','N'),(17,'TRIMBLE NETRS','1.13','N'),(18,'TRIMBLE 4000SSI','7.27','N'),(19,'TRIMBLE 4000SSI','7.28','N'),(20,'TRIMBLE 4000SSI','7.29','N'),(21,'TRIMBLE 4000SSI','7.19A','N'),(22,'TRIMBLE 5700','1.3','N'),(23,'AOA BENCHMARK ACT','3.3.32.5','N'),(24,'TRIMBLE 5700','2.01','N'),(25,'TRIMBLE R7','2.24','N'),(26,'TRIMBLE 5700','2.21','N'),(27,'TRIMBLE 5700','2.10','N');
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
  `height_ellips_elev` float DEFAULT NULL,
  `installed_date` datetime DEFAULT NULL,
  `retired_date` datetime DEFAULT NULL,
  `agency_id` int(3) unsigned DEFAULT NULL,
  `access_id` int(3) unsigned DEFAULT NULL,
  `style_id` int(3) unsigned NOT NULL,
  `status_id` int(3) unsigned NOT NULL,
  `monument_style_id` int(3) unsigned NOT NULL,
  `nation_id` int(3) unsigned NOT NULL,
  `province_state_id` int(3) unsigned DEFAULT NULL,
  `locale_id` int(3) unsigned NOT NULL,
  `ellipsoid_id` int(1) unsigned NOT NULL,
  `networks` varchar(2000) DEFAULT NULL,
  `iers_domes` char(9) DEFAULT NULL,
  `station_photo_URL` varchar(100) DEFAULT NULL,
  `time_series_plot_image_URL` varchar(100) DEFAULT NULL,
  `embargo_duration_hours` int(6) unsigned DEFAULT NULL,
  `embargo_after_date` datetime DEFAULT NULL,
  PRIMARY KEY (`station_id`),
  KEY `style_id_idx` (`style_id`),
  KEY `status_id_idx` (`status_id`),
  KEY `access_id_idx` (`access_id`),
  KEY `monument_style_id_idx` (`monument_style_id`),
  KEY `nation_id_idx` (`nation_id`),
  KEY `locale_id_idx` (`locale_id`),
  KEY `ellipsoid_id_idx` (`ellipsoid_id`),
  KEY `agency_id_idx` (`agency_id`),
  CONSTRAINT `access_id` FOREIGN KEY (`access_id`) REFERENCES `access` (`access_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `agency_id` FOREIGN KEY (`agency_id`) REFERENCES `agency` (`agency_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `nation_id` FOREIGN KEY (`nation_id`) REFERENCES `nation` (`nation_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `ellipsoid_id` FOREIGN KEY (`ellipsoid_id`) REFERENCES `ellipsoid` (`ellipsoid_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `locale_id` FOREIGN KEY (`locale_id`) REFERENCES `locale` (`locale_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `monument_style_id` FOREIGN KEY (`monument_style_id`) REFERENCES `monument_style` (`monument_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `status_id` FOREIGN KEY (`status_id`) REFERENCES `station_status` (`station_status_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `style_id` FOREIGN KEY (`style_id`) REFERENCES `station_style` (`station_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station`
--

LOCK TABLES `station` WRITE;
/*!40000 ALTER TABLE `station` DISABLE KEYS */;
INSERT INTO `station` VALUES (1,'STA1','STA1 name',19.1187,-98.6552,3992,'2014-08-15 14:30:30',NULL,2,2,1,1,1,1,1,1,1,'GeodNet','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/TNAM.jpg',NULL,NULL,NULL),(2,'STA2','STA2 name',20.5357,-103.9668,1226.78,'2014-09-04 00:00:00',NULL,1,2,1,1,2,1,1,2,1,'GeodNet','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/BOGT.jpg',NULL,NULL,NULL);
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

-- Dump completed on 2015-01-06 13:05:14