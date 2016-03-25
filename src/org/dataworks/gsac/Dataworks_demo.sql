-- MySQL dump 10.13  Distrib 5.1.69, for debian-linux-gnu (i486)
--
-- Host: localhost    Database: Dataworks_GSAC_database
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
-- Current Database: `Dataworks_GSAC_database`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `Dataworks_GSAC_database` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `Dataworks_GSAC_database`;

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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agency`
--

LOCK TABLES `agency` WRITE;
/*!40000 ALTER TABLE `agency` DISABLE KEYS */;
INSERT INTO `agency` VALUES (0,'NOT AVAILABLE','NOT AVAILA'),(1,'agency name 1','1');
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `antenna`
--

LOCK TABLES `antenna` WRITE;
/*!40000 ALTER TABLE `antenna` DISABLE KEYS */;
INSERT INTO `antenna` VALUES (1,'NOT AVAILABLE','N'),(2,'TRM55971.00','N'),(3,'TRM57971.00','N'),(4,'TRM29659.00','N'),(5,'TRM41249.00','N'),(6,'ASH701945B_M','N'),(7,'TRM59800.80','N'),(8,'ASH701945E_M','N'),(9,'TRM59800.00','N'),(10,'AOAD/M_T','N'),(11,'ASH701945G_M','N'),(12,'JAVRINGANT_DM','N'),(13,'LEIAR25.R3','N'),(14,'ASH700936C_M','N'),(15,'JAV_RINGANT_G3T','N');
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
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `country`
--

LOCK TABLES `country` WRITE;
/*!40000 ALTER TABLE `country` DISABLE KEYS */;
INSERT INTO `country` VALUES (1,'France'),(2,'British Virgin Islands'),(3,'Panama'),(4,'Montserrat'),(5,'Puerto Rico'),(6,'Dominican Republic'),(7,'Antigua and Barbuda'),(8,'Colombia'),(9,'Cayman Islands'),(10,'Guatemala'),(11,'Haiti'),(12,'Jamaica'),(13,'Bahamas'),(14,'Cuba'),(15,'Honduras'),(16,'Aruba'),(17,'Nicaragua'),(18,'Belize'),(19,'Mexico'),(20,'Venezuela'),(21,'Curacao'),(22,'Trinidad and Tobago'),(23,'Carriacou'),(24,'Saint Lucia'),(25,'Dominica'),(26,'Anguilla'),(27,'Turks and Caicos Islands'),(28,'Virgin Islands'),(29,'Grenada'),(30,'Costa Rica'),(31,'Netherlands Antilles'),(32,'El Salvador'),(33,'Saint Vincent and The Grenadines');
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
  `unique_info_id` int(9) DEFAULT NULL,
  `original_datafile_name` varchar(100) DEFAULT NULL,
  `datafile_type_id` int(3) unsigned NOT NULL,
  `sample_interval` float DEFAULT NULL,
  `datafile_start_time` datetime NOT NULL,
  `datafile_stop_time` datetime NOT NULL,
  `year` year(4) NOT NULL,
  `day_of_year` int(3) NOT NULL,
  `published_time` datetime NOT NULL,
  `size_bytes` int(10) DEFAULT NULL,
  `MD5` char(32) NOT NULL,
  `URL_path` varchar(120) NOT NULL,
  `data_originator_url_domain` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`datafile_id`),
  KEY `station_id_idx` (`station_id`),
  KEY `equip_config_id_idx` (`equip_config_id`),
  KEY `datafile_type_id_idx` (`datafile_type_id`),
  CONSTRAINT `datafile_type_id` FOREIGN KEY (`datafile_type_id`) REFERENCES `datafile_type` (`datafile_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `equip_config_id` FOREIGN KEY (`equip_config_id`) REFERENCES `equip_config` (`equip_config_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `station_id2` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2049 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile`
--

LOCK TABLES `datafile` WRITE;
/*!40000 ALTER TABLE `datafile` DISABLE KEYS */;
INSERT INTO `datafile` VALUES (631,100,470,'ssia0730.16d.Z',NULL,'ssia0730.16d.Z',2,15,'2016-03-13 00:00:00','2016-03-13 23:59:45',2016,73,'2016-03-14 03:11:38',771725,'2dc99fa52c511dc3fd395abc4ee014e7','ftp://myagency.org/gps/rinex/obs/2016/073/ssia0730.16d.Z','data-out.unavco.org'),(632,100,470,'ssia0730.16n.Z',NULL,'ssia0730.16n.Z',3,15,'2016-03-13 00:00:00','2016-03-13 23:59:45',2016,73,'2016-03-14 03:11:41',33819,'4e01d8f6f371b9be87e15425d7d45d67','ftp://myagency.org/gps/rinex/nav/2016/073/ssia0730.16n.Z','data-out.unavco.org'),(633,100,470,'ssia0740.16d.Z',NULL,'ssia0740.16d.Z',2,15,'2016-03-14 00:00:00','2016-03-14 23:59:45',2016,74,'2016-03-15 03:11:41',776289,'77c42838cfa0406167a37dc753e51623','ftp://myagency.org/gps/rinex/obs/2016/074/ssia0740.16d.Z','data-out.unavco.org'),(634,100,470,'ssia0740.16n.Z',NULL,'ssia0740.16n.Z',3,15,'2016-03-14 00:00:00','2016-03-14 23:59:45',2016,74,'2016-03-15 03:11:42',34014,'470c558da0fa8a259627bdfeeb43f87d','ftp://myagency.org/gps/rinex/nav/2016/074/ssia0740.16n.Z','data-out.unavco.org'),(635,100,470,'ssia0750.16n.Z',NULL,'ssia0750.16n.Z',3,15,'2016-03-15 00:00:00','2016-03-15 23:59:45',2016,75,'2016-03-16 03:14:08',34421,'05d2629824f4692f181a0a0959e15fd5','ftp://myagency.org/gps/rinex/nav/2016/075/ssia0750.16n.Z','data-out.unavco.org'),(636,100,470,'ssia0750.16d.Z',NULL,'ssia0750.16d.Z',2,15,'2016-03-15 00:00:00','2016-03-15 23:59:45',2016,75,'2016-03-16 03:14:06',773383,'549ce774ad6e5d3efe02f1e4ab76ff41','ftp://myagency.org/gps/rinex/obs/2016/075/ssia0750.16d.Z','data-out.unavco.org'),(1891,100,470,'ssia0610.16d.Z',NULL,'ssia0610.16d.Z',2,15,'2016-03-01 00:00:00','2016-03-01 23:59:45',2016,61,'2016-03-03 21:18:17',741201,'8096c2a0818cd588466e93d8ebdff153','ftp://myagency.org/gps/rinex/obs/2016/061/ssia0610.16d.Z','data-out.unavco.org'),(1892,100,470,'ssia0610.16n.Z',NULL,'ssia0610.16n.Z',3,15,'2016-03-01 00:00:00','2016-03-01 23:59:45',2016,61,'2016-03-03 21:18:19',35807,'b27f9bb640c5dba01773621b804d9988','ftp://myagency.org/gps/rinex/nav/2016/061/ssia0610.16n.Z','data-out.unavco.org'),(1893,100,470,'ssia0620.16n.Z',NULL,'ssia0620.16n.Z',3,15,'2016-03-02 00:00:00','2016-03-02 23:59:45',2016,62,'2016-03-03 21:18:21',35820,'b01d382a1d70aa910e01c08e1453dd63','ftp://myagency.org/gps/rinex/nav/2016/062/ssia0620.16n.Z','data-out.unavco.org'),(1894,100,470,'ssia0620.16d.Z',NULL,'ssia0620.16d.Z',2,15,'2016-03-02 00:00:00','2016-03-02 23:59:45',2016,62,'2016-03-03 21:18:19',746559,'573ec4c438b7cd7d4a59b64c6ff87cf6','ftp://myagency.org/gps/rinex/obs/2016/062/ssia0620.16d.Z','data-out.unavco.org'),(1895,100,470,'ssia0630.16n.Z',NULL,'ssia0630.16n.Z',3,15,'2016-03-03 00:00:00','2016-03-03 23:59:45',2016,63,'2016-03-04 03:12:05',35795,'8ed1fd325ec5c9cf6df396f4025e5eed','ftp://myagency.org/gps/rinex/nav/2016/063/ssia0630.16n.Z','data-out.unavco.org'),(1896,100,470,'ssia0630.16d.Z',NULL,'ssia0630.16d.Z',2,15,'2016-03-03 00:00:00','2016-03-03 23:59:45',2016,63,'2016-03-04 03:12:03',751139,'e85bf9eaefd63a9ae6779808e441fc6e','ftp://myagency.org/gps/rinex/obs/2016/063/ssia0630.16d.Z','data-out.unavco.org'),(1897,100,470,'ssia0640.16d.Z',NULL,'ssia0640.16d.Z',2,15,'2016-03-04 00:00:00','2016-03-04 23:59:45',2016,64,'2016-03-05 03:12:33',736833,'3d4be87703968859ea119cd63542c7a8','ftp://myagency.org/gps/rinex/obs/2016/064/ssia0640.16d.Z','data-out.unavco.org'),(1898,100,470,'ssia0640.16n.Z',NULL,'ssia0640.16n.Z',3,15,'2016-03-04 00:00:00','2016-03-04 23:59:45',2016,64,'2016-03-05 03:12:35',34931,'80c59b0c636a30fcb52a6629055fb9d2','ftp://myagency.org/gps/rinex/nav/2016/064/ssia0640.16n.Z','data-out.unavco.org'),(1899,100,470,'ssia0650.16n.Z',NULL,'ssia0650.16n.Z',3,15,'2016-03-05 00:00:00','2016-03-05 23:59:45',2016,65,'2016-03-06 03:20:23',33628,'4f8ae7b6aeb028baf055d192889db2c1','ftp://myagency.org/gps/rinex/nav/2016/065/ssia0650.16n.Z','data-out.unavco.org'),(1900,100,470,'ssia0650.16d.Z',NULL,'ssia0650.16d.Z',2,15,'2016-03-05 00:00:00','2016-03-05 23:59:45',2016,65,'2016-03-06 03:20:21',745403,'c05659c2a21fa46af44a6ca19f70ce25','ftp://myagency.org/gps/rinex/obs/2016/065/ssia0650.16d.Z','data-out.unavco.org');
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
) ENGINE=InnoDB AUTO_INCREMENT=523 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equip_config`
--

LOCK TABLES `equip_config` WRITE;
/*!40000 ALTER TABLE `equip_config` DISABLE KEYS */;
INSERT INTO `equip_config` VALUES (461,100,'2016-03-16 19:21:06','2000-09-28 00:00:00','2006-09-17 00:00:00',4,'0220191259',0,NULL,NULL,8,' ',54,'3937A26105','GPS',15),(462,100,'2016-03-16 19:21:06','2007-04-19 15:40:15','2010-07-25 23:59:30',4,'0220356936',0,NULL,NULL,8,' ',72,'4520250849','GPS',15),(463,100,'2016-03-16 19:21:06','2012-03-05 17:32:00','2012-06-07 18:50:45',4,'0220356936',0,NULL,NULL,8,' ',6,'5133K77673','GPS',15),(464,100,'2016-03-16 19:21:06','2012-06-07 19:01:45','2012-12-05 23:59:45',4,'0220356936',0,NULL,NULL,8,' ',7,'5133K77673','GPS',15),(465,100,'2016-03-16 19:21:06','2012-12-06 00:00:00','2013-03-06 23:59:45',4,'0220356936',0,NULL,NULL,8,' ',24,'5133K77673','GPS',15),(466,100,'2016-03-16 19:21:06','2013-03-07 00:00:00','2013-07-08 23:59:45',4,'0220356936',0,NULL,NULL,8,' ',8,'5133K77673','GPS',15),(467,100,'2016-03-16 19:21:06','2013-07-09 00:00:00','2013-08-23 23:59:45',4,'0220356936',0,NULL,NULL,8,' ',9,'5133K77673','GPS',15),(468,100,'2016-03-16 19:21:06','2013-08-24 00:00:00','2014-05-20 23:59:45',4,'0220356936',0,NULL,NULL,8,' ',10,'5133K77673','GPS',15),(469,100,'2016-03-16 19:21:06','2014-05-21 00:00:00','2015-07-09 21:59:45',4,'0220356936',0,NULL,NULL,8,' ',11,'5133K77673','GPS',15),(470,100,'2016-03-16 19:21:06','2015-07-09 22:00:00','2016-03-15 23:59:45',4,'0220356936',0,NULL,NULL,8,' ',25,'5133K77673','GPS',15);
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
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locale`
--

LOCK TABLES `locale` WRITE;
/*!40000 ALTER TABLE `locale` DISABLE KEYS */;
INSERT INTO `locale` VALUES (1,'LES ABYMES'),(2,'Anegada'),(3,'Sherman'),(4,'Panama'),(5,'-'),(6,'Arecibo'),(7,'Barahona'),(8,'Bogota'),(9,'Bayamon'),(10,'Major Donald Drive'),(11,'Chisec'),(12,'Codrington'),(13,'Bethesda'),(14,'N/A'),(15,'none'),(16,'Kingston'),(17,'Freeport'),(18,'San Salvador'),(19,'Camaguey'),(20,'Swan Islan'),(21,'Noord'),(22,'Bocas Island'),(23,'San Lorenzo '),(24,'Poneloya'),(25,'Belmopan'),(26,'Felipe Carrillo Puerto'),(27,'Comitan De Dominguez'),(28,'Cabo Frances Viejo'),(29,'Contadora'),(30,'Puerto Cabezas'),(31,'Penonome'),(32,'Meteti'),(33,'Old Town'),(34,'Monteria'),(35,'Galerazamba'),(36,'Cerrejon'),(37,'Quebrada Arriba'),(38,'Williamsted'),(39,'Sauteurs'),(40,'Mount Pleasant'),(41,'Moule A Chique'),(42,'Grand Savanne'),(43,'The Valley'),(44,'Providenciales'),(45,'Cancun'),(46,'Corozal'),(47,'La Romana'),(48,'Christiansted'),(49,'El Seibo'),(50,'Dewey'),(51,'Roseau'),(52,'Santa Elena'),(53,'George Town'),(54,'North Side'),(55,'Guatemala City'),(56,'Carrillo de Hojancha'),(57,'Jacmel'),(58,'Les Cayes'),(59,'Little Cayman'),(60,'LE LAMENTIN'),(61,'La Vega'),(62,'Managua'),(63,'Mayaguez'),(64,'Ponce'),(65,'El Naranjo'),(66,'Nassau'),(67,'Silver Hills'),(68,'Ponc'),(69,'Port Royal'),(70,'Quebrada Seca'),(71,'Soufriere Hills Volcano'),(72,'Bonao'),(73,'Santiago de los Caballeros'),(74,'Higuey'),(75,'Cabrera'),(76,'Miches'),(77,'Redonda'),(78,'Santo Domingo'),(79,'San Francisco de Macoris'),(80,'RDSJ: San Juan de la Maguana'),(81,'Roatan'),(82,'San Andres'),(83,'Santiago de Cuba'),(84,'saint Marten'),(85,'San Pedro de Macoris'),(86,'Santiago Rodriguez'),(87,'Frenchtown'),(88,'St David'),(89,'Taxisco'),(90,'Abaco'),(91,'Tegucigalpa'),(92,'Puerto Morelos'),(93,'Bocas Del Toro'),(94,'San Fernando'),(95,'St. Augustine'),(96,'Cano Negro'),(97,'Kingshill'),(98,'St. Thomas'),(99,'Liverpool');
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metpack`
--

LOCK TABLES `metpack` WRITE;
/*!40000 ALTER TABLE `metpack` DISABLE KEYS */;
INSERT INTO `metpack` VALUES (1,'NOT AVAILABLE'),(2,'WXT520'),(3,'WXT510'),(4,''),(5,'PTU300');
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monument_style`
--

LOCK TABLES `monument_style` WRITE;
/*!40000 ALTER TABLE `monument_style` DISABLE KEYS */;
INSERT INTO `monument_style` VALUES (1,'NOT AVAILABLE'),(2,'building roof'),(3,'deep foundation pillar'),(4,'deep-drilled braced'),(5,'shallow-drilled braced'),(6,'building wall'),(7,'rock-pin'),(8,'shallow foundation pillar'),(9,'permanent station unspecified'),(10,'shallow foundation mast');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `network`
--

LOCK TABLES `network` WRITE;
/*!40000 ALTER TABLE `network` DISABLE KEYS */;
INSERT INTO `network` VALUES (0,'NOT AVAILABLE'),(1,'network 2'),(2,'network 3');
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `radome`
--

LOCK TABLES `radome` WRITE;
/*!40000 ALTER TABLE `radome` DISABLE KEYS */;
INSERT INTO `radome` VALUES (1,'NONE','Y'),(2,'SCIT','Y'),(3,'SCIS','Y'),(4,'NOT AVAILABLE','N'),(5,'SNOW','N'),(6,'LEIT','N'),(7,'JPLA','N'),(8,'UNAV','N'),(9,'TZGD','N');
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
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receiver_firmware`
--

LOCK TABLES `receiver_firmware` WRITE;
/*!40000 ALTER TABLE `receiver_firmware` DISABLE KEYS */;
INSERT INTO `receiver_firmware` VALUES (1,'NOT AVAILABLE','NOT AVAILABLE','N'),(2,'TRIMBLE NETR5','4.03','N'),(3,'TRIMBLE NETR5','4.17','N'),(4,'TRIMBLE NETR5','4.22','N'),(5,'TRIMBLE NETR5','4.42','N'),(6,'TRIMBLE NETR9','4.42','N'),(7,'TRIMBLE NETR9','4.60','N'),(8,'TRIMBLE NETR9','4.70','N'),(9,'TRIMBLE NETR9','4.80','N'),(10,'TRIMBLE NETR9','4.81','N'),(11,'TRIMBLE NETR9','4.85','N'),(12,'LEICA GR25','3.11','N'),(13,'TRIMBLE NETRS','1.3-0  05 Apr 2010','N'),(14,'TRIMBLE NETRS','1.3-1 10/DEC/2010','N'),(15,'TRIMBLE NETRS','1.3-2','N'),(16,'TRIMBLE NETRS','1.1-3','N'),(17,'TRIMBLE NETRS','1.3-1','N'),(18,'ASHTECH UZ-12','CJ00','N'),(19,'TRIMBLE NETRS','1.1-3   28 Apr 2005','N'),(20,'TRIMBLE NETRS','1.2-5','N'),(21,'TRIMBLE NETR9','5.03','N'),(22,'TRIMBLE NETRS','0.3-3','N'),(23,'TRIMBLE NETRS','1.3-2 30/OCT/2012','N'),(24,'TRIMBLE NETR9','4.62','N'),(25,'TRIMBLE NETR9','5.01','N'),(26,'TRIMBLE NETR9','5.10','N'),(27,'ROGUE SNR-8000','2.8.32.1','N'),(28,'ROGUE SNR-8000','3.2','N'),(29,'ROGUE SNR-8000','3.2.32.8','N'),(30,'ASHTECH Z-XII3','CC00','N'),(31,'ASHTECH UZ-12','CJ12','N'),(32,'ASHTECH UZ-12','CQ00','N'),(33,'TRIMBLE NETRS','1.2-0 26 Apr 2007','N'),(34,'JAVAD TRE_3 DELTA','3.6.2','N'),(35,'JAVAD TRE_3 DELTA','3.6.3','N'),(36,'TRIMBLE NETR5','4.48','N'),(37,'LEICA GRX1200GGPRO','8.20/3.019','N'),(38,'TRIMBLE NETR9','4.93','N'),(39,'TRIMBLE NETR9','4.42 beta build 82','N'),(40,'TRIMBLE NETR9','4.43','N'),(41,'TRIMBLE NETRS','1.1-3  28 Apr 2005','N'),(42,'TRIMBLE NETRS','1.3-0   05 Apr 2010','N'),(43,'TRIMBLE NETRS','1.2-0','N'),(44,'ROGUE SNR-8000','3.2 link 03/09/95','N'),(45,'AOA SNR-8100 ACT','3.3.32.2','N'),(46,'AOA SNR-8100 ACT','3.3.32.2 1s soc2rnx','N'),(47,'ASHTECH Z-XII3','CD00 1s soc2rnx','N'),(48,'ASHTECH Z-XII3','CD00-1D02','N'),(49,'TRIMBLE NETR9','4.17','N'),(50,'TRIMBLE 4700','1.30','N'),(51,'LEICA GRX1200+','8.20/4.005','N'),(52,'TRIMBLE NETR5','3.84','N'),(53,'TRIMBLE NETR5','4.85','N'),(54,'TRIMBLE 4000SSI','7.29','N'),(55,'LEICA GRX1200PRO','5.62','N'),(56,'LEICA GRX1200PRO','7.01','N'),(57,'LEICA GRX1200PRO','7.01/3.016','N'),(58,'LEICA GRX1200GGPRO','8.20/3.055','N'),(59,'TRIMBLE 5700','1.04','N'),(60,'TRIMBLE NETRS','1.1-2','N'),(61,'TRIMBLE R7','2.27','N'),(62,'TRIMBLE NETRS','1.1-2 19 Apr 2005','N'),(63,'TRIMBLE NETRS','0.3-8   10 Dec 2009','N'),(64,'TRIMBLE NETRS','1.3-1   10 Dec 2010','N'),(65,'TRIMBLE NETRS','1.3-0','N'),(66,'TRIMBLE NETR5','4.87','N'),(67,'TRIMBLE NETR8','4.48','N'),(68,'TRIMBLE NETR9','4.61','N'),(69,'TRIMBLE NETR9','4.82','N'),(70,'ASHTECH Z-XII3','1D02-CD00','N'),(71,'JAVAD TRE_G3TH DELTA','3.5.7','N'),(72,'TRIMBLE NETRS','1.13','N'),(73,'TRIMBLE NETRS','1.1-5','N'),(74,'TRIMBLE NETRS','1.15','N'),(75,'TRIMBLE NETRS','1.20','N');
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
  `unique_site_id` varchar(50) DEFAULT NULL,
  `latitude_north` double NOT NULL,
  `longitude_east` double NOT NULL,
  `height_above_ellipsoid` float NOT NULL,
  `installed_date` datetime NOT NULL,
  `latest_data_time` datetime DEFAULT NULL,
  `retired_date` datetime DEFAULT NULL,
  `style_id` int(3) unsigned NOT NULL,
  `status_id` int(3) unsigned NOT NULL,
  `access_id` int(3) unsigned NOT NULL,
  `monument_style_id` int(3) unsigned NOT NULL,
  `country_id` int(3) unsigned DEFAULT NULL,
  `locale_id` int(3) unsigned DEFAULT NULL,
  `ellipsoid_id` int(1) unsigned DEFAULT NULL,
  `iers_domes` char(9) DEFAULT NULL,
  `operator_agency_id` int(3) unsigned DEFAULT NULL,
  `data_publisher_agency_id` int(3) unsigned DEFAULT NULL,
  `network_id` int(5) unsigned DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=117 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station`
--

LOCK TABLES `station` WRITE;
/*!40000 ALTER TABLE `station` DISABLE KEYS */;
INSERT INTO `station` VALUES (100,'SSIA','Ilopang__SLV2007',NULL,13.6973,-89.1162,664.43,'2000-09-28 00:00:00',NULL,NULL,1,1,2,9,32,18,1,'41401S001',NULL,NULL,1,NULL,NULL);
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

-- Dump completed on 2016-03-25 10:02:50
