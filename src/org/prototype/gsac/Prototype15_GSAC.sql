-- MySQL dump 10.13  Distrib 5.1.69, for debian-linux-gnu (i486)
--
-- Host: localhost    Database: Prototype15_GSAC_Feb29
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
-- Current Database: `Prototype15_GSAC_Feb29`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `Prototype15_GSAC_Feb29` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `Prototype15_GSAC_Feb29`;

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
INSERT INTO `access` VALUES (0,'access unknown'),(1,'no public access allowed'),(2,'public access allowed'),(3,'public access allowed for metadata only');
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
  `operating_agency_name` varchar(100) NOT NULL,
  `operating_agency_short_name` varchar(50) DEFAULT NULL,
  `operating_agency_URL` varchar(150) DEFAULT NULL,
  `operating_agency_address` varchar(150) DEFAULT NULL,
  `operating_agency_email` varchar(100) DEFAULT NULL,
  `agency_individual_name` varchar(100) DEFAULT NULL,
  `other_contact` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`agency_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agency`
--

LOCK TABLES `agency` WRITE;
/*!40000 ALTER TABLE `agency` DISABLE KEYS */;
INSERT INTO `agency` VALUES (0,'agency unknown','agency unknown','agency URL unknown',NULL,NULL,NULL,NULL);
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
INSERT INTO `antenna` VALUES (0,'antenna unknown','N'), (1,'TRM57971.00','Y'),(2,'TRM59800.00','Y'),(3,'ASH701945B_M','Y'),(4,'TRM55971.00','Y'),(5,'TRM41249.00','N'),(6,'TRM22020.00+GP','N'),(7,'AOAD/M_T','N'),(8,'TRM14532.00','N'),(9,'ASH700936D_M','N'),(10,'TRM29659.00','N'),(11,'TRM22020.00-GP','N');
/*!40000 ALTER TABLE `antenna` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data_reference_frame`
--

DROP TABLE IF EXISTS `data_reference_frame`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_reference_frame` (
  `data_reference_frame_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `data_reference_frame_name` varchar(50) NOT NULL,
  PRIMARY KEY (`data_reference_frame_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_reference_frame`
--

LOCK TABLES `data_reference_frame` WRITE;
/*!40000 ALTER TABLE `data_reference_frame` DISABLE KEYS */;
INSERT INTO `data_reference_frame` VALUES (0,'data_reference_frame unknown'), (1,'WGS84'),(2,'IGS08'),(3,'NA12'),(4,'GTRF09'),(5,'ETRS89'),(6,'FID (UNR)'),(7,'SNARF');
/*!40000 ALTER TABLE `data_reference_frame` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data_type`
--

DROP TABLE IF EXISTS `data_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_type` (
  `data_type_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `data_type_name` varchar(50) NOT NULL,
  `data_type_description` varchar(50) NOT NULL,
  PRIMARY KEY (`data_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_type`
--

LOCK TABLES `data_type` WRITE;
/*!40000 ALTER TABLE `data_type` DISABLE KEYS */;
INSERT INTO `data_type` VALUES (0,'data type unknown',''), (1,'instrumental data','any type instrumental native, raw, or binary data'),(2,'GNSS observation file','any GNSS obs file'),(3,'GPS navigation file','a GPS          navigation file, RINEX 2'),(4,'Galileo navigation file','a Galileo GNSS navigation file, RINEX 2'),(5,'GLONASS navigation file','a GLONASS GNSS navigation file, RINEX 2'),(6,'GNSS meteorology file','a GNSS meteorology        file, RINEX 2'),(7,'QZSS navigation file','a QZSS GNSS navigation    file, RINEX 2'),(8,'Beidou navigation file','a Beidou GNSS navigation  file, RINEX 2'),(9,'',''),(10,'',''),(11,'',''),(12,'GNSS nav file','GNSS nav file, any format or system'),(13,'GNSS met file','GNSS met file, any format or system'),(14,'',''),(15,'',''),(16,'Time series solution','time series solution'),(17,'Time series plot','time series plot image file'),(18,'',''),(19,'GNSS sites velocities','GNSS sites velocity solution'),(20,'GNSS sites positions','GNSS sites positions solution'),(21,'strainmeter observations',''),(22,'tidegage observations',''),(23,'tiltmeter observations',''),(24,'DORIS','DORIS'),(25,'SLR','SLR'),(26,'VLBI','VLBI'),(27,'teqc qc S file','teqc QC summary S file');
/*!40000 ALTER TABLE `data_type` ENABLE KEYS */;
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
  `URL_complete` varchar(220) NOT NULL,
  `URL_protocol` varchar(7) DEFAULT NULL,
  `URL_domain` varchar(50) DEFAULT NULL,
  `URL_path_dirs` varchar(70) DEFAULT NULL,
  `data_type_id` int(3) unsigned NOT NULL,
  `datafile_format_id` int(3) unsigned DEFAULT NULL,
  `data_reference_frame_id` int(3) unsigned DEFAULT NULL,
  `datafile_start_time` datetime NOT NULL,
  `datafile_stop_time` datetime NOT NULL,
  `datafile_published_date` datetime DEFAULT NULL,
  `sample_interval` float DEFAULT NULL,
  `latency_estimate` float DEFAULT NULL,
  `year` year(4) DEFAULT NULL,
  `day_of_year` int(3) DEFAULT NULL,
  `size_bytes` int(10) DEFAULT NULL,
  `MD5` char(32) DEFAULT NULL,
  `originating_agency_URL` varchar(220) DEFAULT NULL,
  PRIMARY KEY (`datafile_id`),
  KEY `station_id_idx` (`station_id`),
  KEY `equip_config_id_idx` (`equip_config_id`),
  KEY `data_type_id_idx` (`data_type_id`),
  KEY `datafile_format_id_idx` (`datafile_format_id`),
  KEY `data_reference_frame_id_idx` (`data_reference_frame_id`),
  CONSTRAINT `datafile_format_id` FOREIGN KEY (`datafile_format_id`) REFERENCES `datafile_format` (`datafile_format_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `data_reference_frame_id` FOREIGN KEY (`data_reference_frame_id`) REFERENCES `data_reference_frame` (`data_reference_frame_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `data_type_id` FOREIGN KEY (`data_type_id`) REFERENCES `data_type` (`data_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `equip_config_id` FOREIGN KEY (`equip_config_id`) REFERENCES `equip_config` (`equip_config_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `station_id2` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=299 DEFAULT CHARSET=utf8;
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
  PRIMARY KEY (`datafile_format_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile_format`
--

LOCK TABLES `datafile_format` WRITE;
/*!40000 ALTER TABLE `datafile_format` DISABLE KEYS */;
INSERT INTO `datafile_format` VALUES (0,'data file format unknown'), (1,'RINEX 2'),(2,'RINEX 3'),(3,'BINEX'),(4,'SINEX'),(5,'UNR tenv3 northings and eastings'),(6,'UNR txyz2 Cartesian xyz'),(7,'UNR tenv traditional NEU'),(8,'image file (.png, .jpg, etc.)'),(9,'UNR station QC estimate .qa file'),(10,'UNR kenv 5 minute products'),(11,'UNR krms RMS products'),(12,'DORIS'),(13,'SLR'),(14,'VLBI'),(15,'BOTTLE'),(16,'SEED'),(17,'PBO GPS Velocity Field Format'),(18,'PBO GPS Station Position Time Series, .pos'),(19,'teqc qc summary S file'),(20,'IGS XML site log'),(21,'IGS ASCII site log');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ellipsoid`
--

LOCK TABLES `ellipsoid` WRITE;
/*!40000 ALTER TABLE `ellipsoid` DISABLE KEYS */;
INSERT INTO `ellipsoid` VALUES (0,'ellipsoid unknown', 'unknown'),(1,'WGS 84','WGS 84'),(2,'GRS 80','GRS 80'),(3,'PZ-90','PZ-90');
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
  `data_latency_hours` double DEFAULT NULL,
  `data_latency_days` int(11) DEFAULT NULL,
  `data_completeness` float DEFAULT NULL,
  `db_update_time` datetime DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8;
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
  `locale_name` varchar(70) NOT NULL,
  PRIMARY KEY (`locale_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locale`
--

LOCK TABLES `locale` WRITE;
/*!40000 ALTER TABLE `locale` DISABLE KEYS */;
INSERT INTO `locale` VALUES (0,'locale unknown or not supplied');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metpack`
--

LOCK TABLES `metpack` WRITE;
/*!40000 ALTER TABLE `metpack` DISABLE KEYS */;
INSERT INTO `metpack` VALUES (0,'metpack unknown'), (1,'no metpack'),(2,'WXT520'),(3,'WXT510');
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monument_style`
--

LOCK TABLES `monument_style` WRITE;
/*!40000 ALTER TABLE `monument_style` DISABLE KEYS */;
INSERT INTO `monument_style` VALUES (0,'monument unknown or not supplied');
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nation`
--

LOCK TABLES `nation` WRITE;
/*!40000 ALTER TABLE `nation` DISABLE KEYS */;
INSERT INTO `nation` VALUES (0,'nation unknown or not supplied');
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `province_state`
--

LOCK TABLES `province_state` WRITE;
/*!40000 ALTER TABLE `province_state` DISABLE KEYS */;
INSERT INTO `province_state` VALUES (0,'province unknown or not supplied');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `radome`
--

LOCK TABLES `radome` WRITE;
/*!40000 ALTER TABLE `radome` DISABLE KEYS */;
INSERT INTO `radome` VALUES (0,'radome unknown','N'), (1,'NONE','Y'),(2,'SCIT','Y'),(3,'SCIS','Y');
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receiver_firmware`
--

LOCK TABLES `receiver_firmware` WRITE;
/*!40000 ALTER TABLE `receiver_firmware` DISABLE KEYS */;
INSERT INTO `receiver_firmware` VALUES (0,'receiver unknown','rcv firmware unknown','N');
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
  `height_ellipsoid` float DEFAULT NULL,
  `X` double DEFAULT NULL,
  `Y` double DEFAULT NULL,
  `Z` double DEFAULT NULL,
  `installed_date` datetime DEFAULT NULL,
  `retired_date` datetime DEFAULT NULL,
  `published_date` datetime DEFAULT NULL,
  `operator_agency_id` int(3) unsigned DEFAULT NULL,
  `data_publisher_agency_id` int(3) unsigned DEFAULT NULL,
  `access_id` int(3) unsigned DEFAULT NULL,
  `style_id` int(3) unsigned DEFAULT NULL,
  `status_id` int(3) unsigned NOT NULL,
  `monument_style_id` int(3) unsigned DEFAULT NULL,
  `nation_id` int(3) unsigned DEFAULT NULL,
  `province_state_id` int(3) unsigned DEFAULT NULL,
  `locale_id` int(3) unsigned DEFAULT NULL,
  `networks` varchar(2000) DEFAULT NULL,
  `iers_domes` char(9) DEFAULT NULL,
  `station_photo_URL` varchar(100) DEFAULT NULL,
  `time_series_plot_image_URL` varchar(100) DEFAULT NULL,
  `embargo_duration_hours` int(6) unsigned DEFAULT NULL,
  `embargo_after_date` datetime DEFAULT NULL,
  `ellipsoid_id` int(1) unsigned DEFAULT NULL,
  PRIMARY KEY (`station_id`),
  KEY `style_id_idx` (`style_id`),
  KEY `status_id_idx` (`status_id`),
  KEY `access_id_idx` (`access_id`),
  KEY `monument_style_id_idx` (`monument_style_id`),
  KEY `nation_id_idx` (`nation_id`),
  KEY `locale_id_idx` (`locale_id`),
  KEY `operator_agency_id_idx` (`operator_agency_id`),
  KEY `data_publisher_agency_id_idx` (`data_publisher_agency_id`),
  KEY `ellipsoid_id` (`ellipsoid_id`),
  CONSTRAINT `access_id` FOREIGN KEY (`access_id`) REFERENCES `access` (`access_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `operator_agency_id` FOREIGN KEY (`operator_agency_id`) REFERENCES `agency` (`agency_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `data_publisher_agency_id` FOREIGN KEY (`data_publisher_agency_id`) REFERENCES `agency` (`agency_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `ellipsoid_id` FOREIGN KEY (`ellipsoid_id`) REFERENCES `ellipsoid` (`ellipsoid_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `locale_id` FOREIGN KEY (`locale_id`) REFERENCES `locale` (`locale_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `monument_style_id` FOREIGN KEY (`monument_style_id`) REFERENCES `monument_style` (`monument_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `nation_id` FOREIGN KEY (`nation_id`) REFERENCES `nation` (`nation_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `status_id` FOREIGN KEY (`status_id`) REFERENCES `station_status` (`station_status_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `style_id` FOREIGN KEY (`style_id`) REFERENCES `station_style` (`station_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station_status`
--

LOCK TABLES `station_status` WRITE;
/*!40000 ALTER TABLE `station_status` DISABLE KEYS */;
INSERT INTO `station_status` VALUES (0,'status unknown' ),(1,'Active'),(2,'Inactive/intermittent'),(3,'Retired'),(4,'Pending');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station_style`
--

LOCK TABLES `station_style` WRITE;
/*!40000 ALTER TABLE `station_style` DISABLE KEYS */;
INSERT INTO `station_style` VALUES (0,'style unknown' ),(1,'GPS/GNSS Continuous'),(2,'GPS/GNSS Campaign'),(3,'GPS/GNSS Mobile'),(4,'GPS/GNSS Episodic');
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

-- Dump completed on 2016-02-29 12:37:46
