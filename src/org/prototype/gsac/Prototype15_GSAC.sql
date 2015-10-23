-- MySQL dump 10.13  Distrib 5.1.69, for debian-linux-gnu (i486)
--
-- Host: localhost    Database: Prototype15_GSAC_with_data
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
-- Current Database: `Prototype15_GSAC_with_data`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `Prototype15_GSAC_with_data` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `Prototype15_GSAC_with_data`;

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
INSERT INTO `access` VALUES (1,'no public access allowed'),(2,'public access allowed'),(3,'public access allowed for metadata only');
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
  `operating_agency_address` varchar(150) DEFAULT NULL,
  `operating_agency_email` varchar(100) DEFAULT NULL,
  `agency_individual_name` varchar(100) DEFAULT NULL,
  `other_contact` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`agency_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agency`
--

LOCK TABLES `agency` WRITE;
/*!40000 ALTER TABLE `agency` DISABLE KEYS */;
INSERT INTO `agency` VALUES (1,'not specified',NULL,NULL,NULL,NULL);
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
INSERT INTO `data_reference_frame` VALUES (1,'WGS84'),(2,'IGS08'),(3,'NA12'),(4,'GTRF09'),(5,'ETRS89'),(6,'FID (UNR)'),(7,'SNARF');
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
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_type`
--

LOCK TABLES `data_type` WRITE;
/*!40000 ALTER TABLE `data_type` DISABLE KEYS */;
INSERT INTO `data_type` VALUES (1,'instrumental data','any type instrumental native, raw, or binary data'),(2,'GNSS observation file','any GNSS obs file'),(3,'GPS navigation file','a GPS          navigation file'),(4,'Galileo navigation file','a Galileo GNSS navigation file'),(5,'GLONASS navigation file','a GLONASS GNSS navigation file'),(6,'GNSS meteorology file','a GNSS meteorology       file'),(7,'QZSS navigation file','a QZSS GNSS navigation file'),(8,'Beidou navigation file','a Beidou GNSS navigation file'),(9,'Final Daily time series','Final Daily time series solution'),(10,'Rapid Daily time series','Rapid Daily time series solution'),(11,'Rapid 5 minute time series','Rapid 5 min time series solution'),(12,'Ultra Rapid 5 minute time series','Ultra Rapid 5 min time    series solution'),(13,'Ultra Rapid 5 minute Combo time series','Ultra Rapid 5 min Combo time series solution'),(14,'Nights Ultra Rapid 5 minute time series','Night Ultra Rapid 5 min time series    solution'),(15,'Time series plot','Time series static plot image'),(16,'Time series cleaned plot','Time series cleaned static plot image '),(17,'Time series Rapid 5 min plot','Time series Rapid 5  minute plot image '),(18,'SINEX product',''),(19,'GNSS sites velocities',''),(20,'GNSS sites positions',''),(21,'strainmeter observations',''),(22,'tidegage observations',''),(23,'tiltmeter observations',''),(24,'DORIS','DORIS'),(25,'SLR','SLR'),(26,'VLBI','VLBI');
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
-- Table structure for table `datafile_format`
--

DROP TABLE IF EXISTS `datafile_format`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datafile_format` (
  `datafile_format_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `datafile_format_name` varchar(50) NOT NULL,
  PRIMARY KEY (`datafile_format_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile_format`
--

LOCK TABLES `datafile_format` WRITE;
/*!40000 ALTER TABLE `datafile_format` DISABLE KEYS */;
INSERT INTO `datafile_format` VALUES (1,'RINEX 2'),(2,'RINEX 3'),(3,'BINEX'),(4,'SINEX'),(5,'UNR tenv3 northings and eastings'),(6,'UNR txyz2 Cartesian xyz'),(7,'UNR tenv traditional NEU'),(8,'plot image'),(9,'UNR station QC estimate .qa file'),(10,'UNR kenv 5 minute products'),(11,'UNR krms RMS products'),(12,'DORIS'),(13,'SLR'),(14,'VLBI'),(15,'BOTTLE'),(16,'SEED'),(17,'PBO GPS Velocity Field Format'),(18,'PBO GPS Station Position Time Series, .pos');
/*!40000 ALTER TABLE `datafile_format` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equip_config`
--

LOCK TABLES `equip_config` WRITE;
/*!40000 ALTER TABLE `equip_config` DISABLE KEYS */;
INSERT INTO `equip_config` VALUES (10,42,'2011-12-02 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 17:50:06',10,'0220368169',0.0083,2,'G3260009',2,NULL,29,'4614207064',NULL,15),(22,47,'2008-09-03 00:00:00','2010-07-08 23:59:45',NULL,NULL,NULL,'2015-10-23 17:50:07',10,'0220366150',0.0083,1,'C5010030',2,NULL,28,'4527253331',NULL,15),(23,47,'2010-07-09 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 17:50:07',10,'0220366150',0.0083,1,'C5010030',2,NULL,29,'4527253331',NULL,15),(45,39,'2008-02-14 23:38:45','2009-03-11 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220382762',0.0083,3,'',2,NULL,28,'4623116489',NULL,15),(46,39,'2009-03-12 00:00:00','2010-10-21 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220377400',0.0083,3,'',2,NULL,28,'4623116489',NULL,15),(47,39,'2010-10-22 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220377400',0.0083,3,'',2,NULL,29,'4623116489',NULL,15),(48,40,'2005-09-23 00:19:00','2010-07-17 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220366152',0.0083,3,'',2,NULL,28,'4518249765',NULL,15),(49,40,'2010-07-18 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220366152',0.0083,3,'',2,NULL,29,'4518249765',NULL,15),(50,41,'2007-06-21 04:04:30','2010-06-12 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220346567',0.0083,3,'',2,NULL,28,'4541260323',NULL,15),(51,41,'2010-12-06 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220346567',0.0083,3,'',2,NULL,29,'4541260323',NULL,15),(52,42,'2006-08-23 00:00:00','2010-07-11 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220368169',0.0083,3,'',2,NULL,28,'4614207064',NULL,15),(53,42,'2010-07-12 00:00:00','2011-12-01 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220368169',0.0083,3,'',2,NULL,29,'4614207064',NULL,15),(54,43,'2005-10-01 00:51:30','2008-09-02 21:19:00',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220365593',0.0083,3,'',2,NULL,28,'4518249763',NULL,15),(55,43,'2008-09-02 21:20:30','2008-09-10 21:56:30',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379553',0.0083,3,'',2,NULL,28,'4518249763',NULL,15),(56,43,'2008-10-09 23:32:45','2010-07-08 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379553',0.0083,3,'',2,NULL,28,'4539259362',NULL,15),(57,43,'2010-07-09 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379553',0.0083,3,'',2,NULL,29,'4539259362',NULL,15),(58,44,'2007-11-26 00:00:00','2010-07-14 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379511',0.0083,3,'',2,NULL,28,'4625209417',NULL,15),(59,44,'2010-07-15 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379511',0.0083,3,'',2,NULL,29,'4625209417',NULL,15),(60,45,'2007-09-13 18:55:00','2010-07-14 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'4623A16401',0.0083,3,'',2,NULL,28,'4625209636',NULL,15),(61,45,'2010-07-15 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'4623A16401',0.0083,3,'',2,NULL,29,'4625209636',NULL,15),(62,46,'2005-09-28 00:16:45','2010-07-11 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220366135',0.0083,3,'',2,NULL,28,'4516248599',NULL,15),(63,46,'2010-07-12 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220366135',0.0083,3,'',2,NULL,29,'4516248599',NULL,15),(64,47,'2005-10-27 21:08:30','2008-09-02 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220366150',0.0083,3,'',2,NULL,28,'4527253331',NULL,15);
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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locale`
--

LOCK TABLES `locale` WRITE;
/*!40000 ALTER TABLE `locale` DISABLE KEYS */;
INSERT INTO `locale` VALUES (1,'not specified'),(2,'Felipe Carrillo Puerto'),(3,'Weaverville'),(4,'Cerrillos'),(5,'Whiskeytown'),(6,'Del Loma'),(7,'Corning'),(8,'Red Bluff'),(9,'Laporte'),(10,'Adin'),(11,'Burney'),(12,'Shasta Lake'),(13,'Potter Valley');
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
INSERT INTO `metpack` VALUES (1,'WXT510'),(2,'WXT520'),(3,'no metpack');
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
INSERT INTO `monument_style` VALUES (1,'not specified'),(2,'building roof'),(3,'deep-drilled braced'),(4,'shallow-drilled braced');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nation`
--

LOCK TABLES `nation` WRITE;
/*!40000 ALTER TABLE `nation` DISABLE KEYS */;
INSERT INTO `nation` VALUES (1,'not specified'),(2,'Mexico'),(3,'United States');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `province_state`
--

LOCK TABLES `province_state` WRITE;
/*!40000 ALTER TABLE `province_state` DISABLE KEYS */;
INSERT INTO `province_state` VALUES (1,'not specified'),(2,'Quintana Roo'),(3,'California'),(4,'Puerto Rico');
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
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receiver_firmware`
--

LOCK TABLES `receiver_firmware` WRITE;
/*!40000 ALTER TABLE `receiver_firmware` DISABLE KEYS */;
INSERT INTO `receiver_firmware` VALUES (1,'not specified','not specified','N'),(2,'TRIMBLE 4000SSI','7.19b','Y'),(3,'TRIMBLE NETRS','1.1-1','Y'),(4,'TRIMBLE NETRS','1.1-2','Y'),(5,'TRIMBLE NETR5','4.03','Y'),(28,'TRIMBLE NETRS','1.1-2 19 Apr 2005','N'),(29,'TRIMBLE NETRS','1.3-0','N'),(30,'TRIMBLE NETR9','4.85','Y');
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
  `published_date` datetime DEFAULT NULL,
  `retired_date` datetime DEFAULT NULL,
  `earliest_data_date` datetime DEFAULT NULL,
  `latest_data_date` datetime DEFAULT NULL,
  `agency_id` int(3) unsigned DEFAULT NULL,
  `access_id` int(3) unsigned DEFAULT NULL,
  `style_id` int(3) unsigned DEFAULT NULL,
  `status_id` int(3) unsigned NOT NULL,
  `monument_style_id` int(3) unsigned DEFAULT NULL,
  `nation_id` int(3) unsigned DEFAULT NULL,
  `province_state_id` int(3) unsigned DEFAULT NULL,
  `locale_id` int(3) unsigned DEFAULT NULL,
  `networks` varchar(2000) DEFAULT NULL,
  `originating_agency_URL` varchar(120) DEFAULT NULL,
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
  KEY `agency_id_idx` (`agency_id`),
  CONSTRAINT `access_id` FOREIGN KEY (`access_id`) REFERENCES `access` (`access_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `agency_id` FOREIGN KEY (`agency_id`) REFERENCES `agency` (`agency_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `locale_id` FOREIGN KEY (`locale_id`) REFERENCES `locale` (`locale_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `monument_style_id` FOREIGN KEY (`monument_style_id`) REFERENCES `monument_style` (`monument_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `nation_id` FOREIGN KEY (`nation_id`) REFERENCES `nation` (`nation_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `status_id` FOREIGN KEY (`status_id`) REFERENCES `station_status` (`station_status_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `style_id` FOREIGN KEY (`style_id`) REFERENCES `station_style` (`station_style_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station`
--

LOCK TABLES `station` WRITE;
/*!40000 ALTER TABLE `station` DISABLE KEYS */;
INSERT INTO `station` VALUES (39,'P340','DashielCrkCN2008',39.4093,-123.0498,865.23,-2691539.1186,-4136726.977,4028080.6731,'2008-02-14 23:38:45',NULL,NULL,NULL,NULL,1,2,1,1,4,3,3,13,'PBO',NULL,'','','http://pboshared.unavco.org/timeseries/P340_timeseries.png',NULL,NULL),(40,'P341','WhiskytownCN2005',40.6507,-122.6069,406.85,NULL,NULL,NULL,'2005-09-23 00:19:00',NULL,NULL,NULL,NULL,1,2,1,1,3,3,3,5,'',NULL,'','','',NULL,NULL),(41,'P343','ChinaPeak_CN2007',40.8871,-123.3342,1617.91,NULL,NULL,NULL,'2007-06-21 04:04:30',NULL,NULL,NULL,NULL,1,2,1,1,4,3,3,6,'',NULL,'','','',NULL,NULL),(42,'P344','VinaHelitkCN2006',39.9291,-122.028,50.29,NULL,NULL,NULL,'2006-08-23 00:00:00',NULL,NULL,NULL,NULL,1,2,1,1,3,3,3,7,'',NULL,'','','',NULL,NULL),(43,'P345','HookerDomeCN2005',40.2712,-122.2708,134.08,NULL,NULL,NULL,'2005-10-01 00:51:30',NULL,NULL,NULL,NULL,1,2,1,1,3,3,3,8,'',NULL,'','','',NULL,NULL),(44,'P346','BuzzardRstCN2007',39.7947,-120.8675,2039.37,NULL,NULL,NULL,'2007-11-26 00:00:00',NULL,NULL,NULL,NULL,1,2,1,1,4,3,3,9,'',NULL,'','','',NULL,NULL),(45,'P347','AdinCTYardCN2007',41.1833,-120.9484,1269.28,NULL,NULL,NULL,'2007-09-13 18:55:00',NULL,NULL,NULL,NULL,1,2,1,1,3,3,3,10,'',NULL,'','','',NULL,NULL),(46,'P348','HatchetMtnCN2005',40.9055,-121.828,1668.07,NULL,NULL,NULL,'2005-09-28 00:16:45',NULL,NULL,NULL,NULL,1,2,1,1,3,3,3,11,'',NULL,'','','',NULL,NULL),(47,'P349','WonderlandCN2005',40.7311,-122.3194,275.4,NULL,NULL,NULL,'2005-10-27 21:08:30',NULL,NULL,NULL,NULL,1,2,1,1,3,3,3,12,'',NULL,'','','',NULL,NULL);
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

-- Dump completed on 2015-10-23 12:33:35
