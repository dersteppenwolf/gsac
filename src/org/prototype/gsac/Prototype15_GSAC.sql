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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agency`
--

LOCK TABLES `agency` WRITE;
/*!40000 ALTER TABLE `agency` DISABLE KEYS */;
INSERT INTO `agency` VALUES (1,'not specified',NULL,NULL,NULL,NULL),(2,'UNAVCO',NULL,NULL,NULL,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_type`
--

LOCK TABLES `data_type` WRITE;
/*!40000 ALTER TABLE `data_type` DISABLE KEYS */;
INSERT INTO `data_type` VALUES (1,'instrumental data','any type instrumental native, raw, or binary data'),(2,'GNSS observation file','any GNSS obs file'),(3,'GPS navigation file','a GPS          navigation file'),(4,'Galileo navigation file','a Galileo GNSS navigation file'),(5,'GLONASS navigation file','a GLONASS GNSS navigation file'),(6,'GNSS meteorology file','a GNSS meteorology       file'),(7,'QZSS navigation file','a QZSS GNSS navigation file'),(8,'Beidou navigation file','a Beidou GNSS navigation file'),(9,'Final Daily time series','Final Daily time series solution'),(10,'Rapid Daily time series','Rapid Daily time series solution'),(11,'Rapid 5 minute time series','Rapid 5 min time series solution'),(12,'Ultra Rapid 5 minute time series','Ultra Rapid 5 min time    series solution'),(13,'Ultra Rapid 5 minute Combo time series','Ultra Rapid 5 min Combo time series solution'),(14,'Nights Ultra Rapid 5 minute time series','Night Ultra Rapid 5 min time series    solution'),(15,'Time series plot','Time series static plot image'),(16,'Time series cleaned plot','Time series cleaned static plot image '),(17,'Time series Rapid 5 min plot','Time series Rapid 5  minute plot image '),(18,'SINEX product',''),(19,'GNSS sites velocities',''),(20,'GNSS sites positions',''),(21,'strainmeter observations',''),(22,'tidegage observations',''),(23,'tiltmeter observations',''),(24,'DORIS','DORIS'),(25,'SLR','SLR'),(26,'VLBI','VLBI'),(27,'teqc qc S file','teqc QC summary S file');
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
INSERT INTO `datafile` VALUES (1,40,49,'p3412840.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/284/p3412840.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/284/',27,19,NULL,'2015-10-11 00:00:00','2015-10-11 23:59:45','2015-10-12 00:00:00',15,NULL,2015,284,20461,'efebbba4b0d6d9be3c93c386bfda3096','UNAVCO'),(2,40,49,'p3412840.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/284/p3412840.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/284/',2,1,NULL,'2015-10-11 00:00:00','2015-10-11 23:59:45','2015-10-12 00:00:00',15,NULL,2015,284,1737879,'dd9e32ca9d96225ed3e511f9e3070c30','UNAVCO'),(3,40,49,'p3412840.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/284/p3412840.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/284/',2,1,NULL,'2015-10-11 00:00:00','2015-10-11 23:59:45','2015-10-12 00:00:00',15,NULL,2015,284,612841,'a056080ef860a9f04dcaef0e35858b22','UNAVCO'),(4,40,49,'p3412840.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/284/p3412840.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/284/',3,1,NULL,'2015-10-11 00:00:00','2015-10-11 23:59:45','2015-10-12 00:00:00',15,NULL,2015,284,30716,'56967d7a5dcfa52f8d9dc60d2bf014cd','UNAVCO'),(5,40,49,'p3412810.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/281/p3412810.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/281/',27,19,NULL,'2015-10-08 00:00:00','2015-10-08 23:59:45','2015-10-09 00:00:00',15,NULL,2015,281,20463,'6670c91ff8ce9bb7f449bc19f60dd86e','UNAVCO'),(6,40,49,'p3412810.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/281/p3412810.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/281/',2,1,NULL,'2015-10-08 00:00:00','2015-10-08 23:59:45','2015-10-09 00:00:00',15,NULL,2015,281,1738779,'7903a3a764f3d93757cf290754491b93','UNAVCO'),(7,40,49,'p3412810.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/281/p3412810.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/281/',2,1,NULL,'2015-10-08 00:00:00','2015-10-08 23:59:45','2015-10-09 00:00:00',15,NULL,2015,281,611229,'191cfaf652c33ddc3bbfb957734d7d34','UNAVCO'),(8,40,49,'p3412810.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/281/p3412810.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/281/',3,1,NULL,'2015-10-08 00:00:00','2015-10-08 23:59:45','2015-10-09 00:00:00',15,NULL,2015,281,31453,'4ccbd8dd55e0093aa4c82d63e85538cd','UNAVCO'),(9,40,49,'p3412830.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/283/p3412830.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/283/',27,19,NULL,'2015-10-10 00:00:00','2015-10-10 23:59:45','2015-10-11 00:00:00',15,NULL,2015,283,20464,'2ae3e2b3c5eb211ac765d63c33bed84d','UNAVCO'),(10,40,49,'p3412830.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/283/p3412830.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/283/',2,1,NULL,'2015-10-10 00:00:00','2015-10-10 23:59:45','2015-10-11 00:00:00',15,NULL,2015,283,1738105,'7b84f80fd18913a509076f5fe013c35a','UNAVCO'),(11,40,49,'p3412830.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/283/p3412830.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/283/',2,1,NULL,'2015-10-10 00:00:00','2015-10-10 23:59:45','2015-10-11 00:00:00',15,NULL,2015,283,617513,'ddb3de0aa17a8b0648438492d17fb75d','UNAVCO'),(12,40,49,'p3412830.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/283/p3412830.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/283/',3,1,NULL,'2015-10-10 00:00:00','2015-10-10 23:59:45','2015-10-11 00:00:00',15,NULL,2015,283,28325,'12eaa4b2d67acd17050c8e116f284674','UNAVCO'),(13,40,49,'p3412800.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/280/p3412800.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/280/',27,19,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,20461,'ce326e8214487d61b8a59f173d53853a','UNAVCO'),(14,40,49,'p3412800.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3412800.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,1739279,'03892e09b2ba1382559aced9d751f886','UNAVCO'),(15,40,49,'p3412800.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3412800.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,613527,'ec5e161f9bb2bd4657338c484cdf8fcc','UNAVCO'),(16,40,49,'p3412800.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/280/p3412800.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/280/',3,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,30476,'786549fda4d4a286ed8d92543eed7f0d','UNAVCO'),(17,40,49,'p3412820.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/282/p3412820.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/282/',27,19,NULL,'2015-10-09 00:00:00','2015-10-09 23:59:45','2015-10-10 00:00:00',15,NULL,2015,282,20466,'e1ca03dacda15b7f5a7a1de424037311','UNAVCO'),(18,40,49,'p3412820.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/282/p3412820.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/282/',2,1,NULL,'2015-10-09 00:00:00','2015-10-09 23:59:45','2015-10-10 00:00:00',15,NULL,2015,282,1739135,'4aea187608724763e323e1e5ca3dd696','UNAVCO'),(19,40,49,'p3412820.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/282/p3412820.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/282/',2,1,NULL,'2015-10-09 00:00:00','2015-10-09 23:59:45','2015-10-10 00:00:00',15,NULL,2015,282,609372,'5e3f47a265f0f91be02a086e755d9d2d','UNAVCO'),(20,40,49,'p3412820.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/282/p3412820.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/282/',3,1,NULL,'2015-10-09 00:00:00','2015-10-09 23:59:45','2015-10-10 00:00:00',15,NULL,2015,282,30812,'bfc79ed2df7c07c86678214e3dfb42d9','UNAVCO'),(21,40,49,'p3412850.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/285/p3412850.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/285/',27,19,NULL,'2015-10-12 00:00:00','2015-10-12 23:59:45','2015-10-13 00:00:00',15,NULL,2015,285,20459,'b175d7690435a8076cee9b4e2738853b','UNAVCO'),(22,40,49,'p3412850.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/285/p3412850.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/285/',2,1,NULL,'2015-10-12 00:00:00','2015-10-12 23:59:45','2015-10-13 00:00:00',15,NULL,2015,285,1739219,'19475f1e75aaac77ba072e2e1f64e56b','UNAVCO'),(23,40,49,'p3412850.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/285/p3412850.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/285/',2,1,NULL,'2015-10-12 00:00:00','2015-10-12 23:59:45','2015-10-13 00:00:00',15,NULL,2015,285,612107,'aacc7b566eb96260914ad2676931fd12','UNAVCO'),(24,40,49,'p3412850.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/285/p3412850.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/285/',3,1,NULL,'2015-10-12 00:00:00','2015-10-12 23:59:45','2015-10-13 00:00:00',15,NULL,2015,285,29940,'3a3537b0d01150c86fd36ff8ba068f79','UNAVCO'),(25,40,49,'p3412870.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/287/p3412870.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/287/',27,19,NULL,'2015-10-14 00:00:00','2015-10-14 23:59:45','2015-10-15 00:00:00',15,NULL,2015,287,20468,'b198ab6f1fd643a2f9cddff91c4e2bd1','UNAVCO'),(26,40,49,'p3412870.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/287/p3412870.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/287/',2,1,NULL,'2015-10-14 00:00:00','2015-10-14 23:59:45','2015-10-15 00:00:00',15,NULL,2015,287,1739579,'f35fec3522236a56bba9d917f030dcd1','UNAVCO'),(27,40,49,'p3412870.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/287/p3412870.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/287/',2,1,NULL,'2015-10-14 00:00:00','2015-10-14 23:59:45','2015-10-15 00:00:00',15,NULL,2015,287,614039,'bf536e9b403b65bbc33f4b001a310f5f','UNAVCO'),(28,40,49,'p3412870.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/287/p3412870.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/287/',3,1,NULL,'2015-10-14 00:00:00','2015-10-14 23:59:45','2015-10-15 00:00:00',15,NULL,2015,287,30427,'a30d9da269d988607c017ac9806cf7c0','UNAVCO'),(29,40,49,'p3412860.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/286/p3412860.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/286/',27,19,NULL,'2015-10-13 00:00:00','2015-10-13 23:59:45','2015-10-14 00:00:00',15,NULL,2015,286,20463,'18cd3603d3193887c405b2098600991c','UNAVCO'),(30,40,49,'p3412860.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/286/p3412860.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/286/',2,1,NULL,'2015-10-13 00:00:00','2015-10-13 23:59:45','2015-10-14 00:00:00',15,NULL,2015,286,1738961,'4aa96f6c67f9c83ecfd161e50fae225b','UNAVCO'),(31,40,49,'p3412860.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/286/p3412860.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/286/',2,1,NULL,'2015-10-13 00:00:00','2015-10-13 23:59:45','2015-10-14 00:00:00',15,NULL,2015,286,609317,'8a2da24e842c1ab573efb8deb096136b','UNAVCO'),(32,40,49,'p3412860.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/286/p3412860.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/286/',3,1,NULL,'2015-10-13 00:00:00','2015-10-13 23:59:45','2015-10-14 00:00:00',15,NULL,2015,286,30825,'5307c1aa95be888ea90f1c267dd35328','UNAVCO'),(33,40,49,'p3412880.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/288/p3412880.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/288/',27,19,NULL,'2015-10-15 00:00:00','2015-10-15 23:59:45','2015-10-16 00:00:00',15,NULL,2015,288,20457,'dcf7936be01ed52af1e5e3721289fc32','UNAVCO'),(34,40,49,'p3412880.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/288/p3412880.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/288/',2,1,NULL,'2015-10-15 00:00:00','2015-10-15 23:59:45','2015-10-16 00:00:00',15,NULL,2015,288,1738517,'3a7da95b10038ad56cd37769ec98b676','UNAVCO'),(35,40,49,'p3412880.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/288/p3412880.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/288/',2,1,NULL,'2015-10-15 00:00:00','2015-10-15 23:59:45','2015-10-16 00:00:00',15,NULL,2015,288,610448,'636ca7e6325d8449abaa0e3a06075360','UNAVCO'),(36,40,49,'p3412880.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/288/p3412880.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/288/',3,1,NULL,'2015-10-15 00:00:00','2015-10-15 23:59:45','2015-10-16 00:00:00',15,NULL,2015,288,30373,'d0e36f9815f9678789880ba74ba98a48','UNAVCO'),(37,40,49,'p3412750.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3412750.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/275/',3,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,30258,'74a71ed96a87df74d6f8ef12a9eec7a2','UNAVCO'),(38,40,49,'p3412750.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3412750.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/275/',27,19,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,20458,'5669b4de1ce3402fae82154b42fed07f','UNAVCO'),(39,40,49,'p3412750.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3412750.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,1739473,'56e733d136f25cc15242110b50edd069','UNAVCO'),(40,40,49,'p3412750.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3412750.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,611045,'a45ebbf82d97892b7c37d1941809e435','UNAVCO'),(41,40,49,'p3412790.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3412790.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/279/',27,19,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,20461,'629c7226a9a550d1913645a6cac873a3','UNAVCO'),(42,40,49,'p3412790.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3412790.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,1738795,'b9536575bf032ab58c33a85ad7d558a8','UNAVCO'),(43,40,49,'p3412790.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3412790.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,609781,'0e24fb6854ca4de38d796b40ed119c20','UNAVCO'),(44,40,49,'p3412790.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/279/p3412790.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/279/',3,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,30898,'770ef024f1f1f1cf58fc3e062ef0b04a','UNAVCO'),(45,40,49,'p3412740.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3412740.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,1739037,'df5c763ce8817666241d8bf65efbab13','UNAVCO'),(46,40,49,'p3412740.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3412740.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,612093,'548f6a992a9449fc172cc3c96627a789','UNAVCO'),(47,40,49,'p3412740.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/274/p3412740.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/274/',3,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,30272,'6bb3ddb8f1a0b2f8397ddc92cc26b4f3','UNAVCO'),(48,40,49,'p3412740.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/274/p3412740.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/274/',27,19,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,20463,'c8015423954d9685d6f9d1c26b03fa39','UNAVCO'),(49,40,49,'p3412770.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/277/p3412770.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/277/',3,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,31052,'0007171e7ec119a3e963ba219805cc56','UNAVCO'),(50,40,49,'p3412770.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/277/p3412770.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/277/',27,19,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,20460,'557a260eeb1bcb454cd85969ad1772e1','UNAVCO'),(51,40,49,'p3412770.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3412770.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,1739001,'c86139fdbbffaf3fad785532f11d059a','UNAVCO'),(52,40,49,'p3412770.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3412770.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,610253,'e12699a53caeed4625062067bf29a6b0','UNAVCO'),(53,40,49,'p3412780.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/278/p3412780.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/278/',3,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,30787,'a300acaf3d50bc9dcda6055a21d7ba45','UNAVCO'),(54,40,49,'p3412780.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/278/p3412780.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/278/',27,19,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,20464,'9b66204858ea7e0f5665ff95f640a1d2','UNAVCO'),(55,40,49,'p3412780.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3412780.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,1738211,'32cb1c17d999411370633b9e66c3144a','UNAVCO'),(56,40,49,'p3412780.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3412780.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,612257,'54625225eaf39a8139e25a62b690e8fc','UNAVCO'),(57,40,49,'p3412760.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/276/p3412760.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/276/',3,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,28408,'aedaa0ed98f6a43df106f53cf0556aa4','UNAVCO'),(58,40,49,'p3412760.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/276/p3412760.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/276/',27,19,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,20462,'8c6f1d8105e8510d38fd809555aadaa1','UNAVCO'),(59,40,49,'p3412760.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3412760.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,1739323,'7d0796ec0046979a92ee84c152b50ecd','UNAVCO'),(60,40,49,'p3412760.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3412760.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,616741,'e5a0c68928727096c11b50b7f8cb94bc','UNAVCO'),(61,46,63,'p3482740.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/274/p3482740.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/274/',27,19,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,20584,'cc2bce900a95156cb20f50bc6e4c4a2d','UNAVCO'),(62,46,63,'p3482740.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3482740.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,1968567,'cf5e89ddb58ec6d3b44c921a00a35125','UNAVCO'),(63,46,63,'p3482740.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3482740.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,701730,'3ab012426256676a3d1c08a7a899a89f','UNAVCO'),(64,46,63,'p3482740.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/274/p3482740.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/274/',3,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,34314,'25a1d865cdd2c2692f754342b07f4d51','UNAVCO'),(65,46,63,'p3482750.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3482750.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/275/',27,19,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,20586,'ac638c4e1e66173a1e828c92d0e3b22f','UNAVCO'),(66,46,63,'p3482750.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3482750.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,1969655,'d65ace7541cae4f3276d6750e6fd0de1','UNAVCO'),(67,46,63,'p3482750.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3482750.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,702518,'7df9b52cd963afde47c682e979a66f36','UNAVCO'),(68,46,63,'p3482750.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3482750.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/275/',3,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,34074,'907eb40ce5e21bb235ba212241d94673','UNAVCO'),(69,46,63,'p3482770.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/277/p3482770.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/277/',27,19,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,20523,'c6e1b30d997f692bea5aac8310ca0779','UNAVCO'),(70,46,63,'p3482770.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3482770.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,1969877,'2a8104d50275c8aeb373a4f28a8d1a41','UNAVCO'),(71,46,63,'p3482770.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3482770.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,702202,'72fef3d2fd9923100f5985b6737d0bce','UNAVCO'),(72,46,63,'p3482770.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/277/p3482770.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/277/',3,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,34284,'0deca422fb6f80fa3ba7072af5fc4b39','UNAVCO'),(73,46,63,'p3482790.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3482790.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/279/',27,19,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,20556,'d52bbe580d61da3ab888a04494d3d8bc','UNAVCO'),(74,46,63,'p3482790.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3482790.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,1965870,'973c3a8432f6690e7c7baff5ac8d3be2','UNAVCO'),(75,46,63,'p3482790.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3482790.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,702041,'6c665a261b5a8ac227231d9f8002f3ea','UNAVCO'),(76,46,63,'p3482790.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/279/p3482790.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/279/',3,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,33988,'a6e84993a78e685f96f6747de8b95f2a','UNAVCO'),(77,46,63,'p3482760.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/276/p3482760.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/276/',27,19,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,20534,'9eb4243ae5b9094da70014bd7a9c8222','UNAVCO'),(78,46,63,'p3482760.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3482760.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,1968875,'d3d06bc5ed729cc2b47480cbfc82250b','UNAVCO'),(79,46,63,'p3482760.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3482760.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,707713,'b587012d4be30091621d04fe2fbde349','UNAVCO'),(80,46,63,'p3482760.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/276/p3482760.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/276/',3,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,31612,'7523c2409ff5437183fe03eb8bce57b5','UNAVCO'),(81,46,63,'p3482780.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/278/p3482780.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/278/',27,19,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,20527,'28560e43faa072101e8453c958ccf3be','UNAVCO'),(82,46,63,'p3482780.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3482780.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,1968644,'ccf087f480cfb92ed420875d7299c646','UNAVCO'),(83,46,63,'p3482780.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3482780.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,702130,'c749a7756c4ef0911522516aab224034','UNAVCO'),(84,46,63,'p3482780.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/278/p3482780.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/278/',3,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,34273,'3bdc5f7f91d54d72ae0a7813c375bed3','UNAVCO'),(85,46,63,'p3482800.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/280/p3482800.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/280/',3,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,33446,'4064ecacdbf79eeff80dbeab7a70f17e','UNAVCO'),(86,46,63,'p3482800.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/280/p3482800.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/280/',27,19,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,20605,'d5a9b0e2fd0d253a0e2cbc3bf32d5102','UNAVCO'),(87,46,63,'p3482800.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3482800.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,1967471,'487c8157a4f97abfdd73e8620d2a47a0','UNAVCO'),(88,46,63,'p3482800.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3482800.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,706930,'469ff86b9d503b1bc44e635a8623b7c4','UNAVCO'),(89,44,59,'p3462780.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/278/p3462780.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/278/',3,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,33455,'c0775d5c863affe08d1e72a3d2959391','UNAVCO'),(90,44,59,'p3462780.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/278/p3462780.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/278/',27,19,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,20546,'2d7bf42614b225c2f5a16dda3fe38c8f','UNAVCO'),(91,44,59,'p3462780.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3462780.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,1918739,'5c7c633970083e836a5ef1bbaa4fbbf9','UNAVCO'),(92,44,59,'p3462780.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3462780.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,685744,'6cf5868dcb9aca220bba175af750cbea','UNAVCO'),(93,44,59,'p3462760.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/276/p3462760.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/276/',27,19,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,20594,'af4de62992115ca64f45363f6502183d','UNAVCO'),(94,44,59,'p3462760.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3462760.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,1919837,'a9896dc7743a4254db792ef9b70a7590','UNAVCO'),(95,44,59,'p3462760.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3462760.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,689016,'1d0b89b2892f959f7b3c43a585f367ce','UNAVCO'),(96,44,59,'p3462760.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/276/p3462760.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/276/',3,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,30579,'7aecf738876f845034b6770a94bcf56c','UNAVCO'),(97,44,59,'p3462790.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/279/p3462790.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/279/',3,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,33073,'13dcbd5c0513f36ae98a3ea0b1a79fdd','UNAVCO'),(98,44,59,'p3462790.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3462790.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/279/',27,19,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,20597,'7de0b68e29bec1f7c7abbe68ac4b69ae','UNAVCO'),(99,44,59,'p3462790.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3462790.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,1918003,'95455e8a9eaff7589a4e2215fb699f61','UNAVCO'),(100,44,59,'p3462790.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3462790.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,686545,'289f7c2694cf2ffb7c99c0cdaa29947f','UNAVCO'),(101,44,59,'p3462800.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/280/p3462800.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/280/',27,19,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,20469,'ebe9ff61264dffbdcacbcd9e944d6c81','UNAVCO'),(102,44,59,'p3462800.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3462800.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,1921204,'d5cf2a1eecb08a53b6ac7710403b3175','UNAVCO'),(103,44,59,'p3462800.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3462800.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,691564,'8bb1e9ac6c7feaf3a431d7464e796918','UNAVCO'),(104,44,59,'p3462800.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/280/p3462800.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/280/',3,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,33273,'69666c54f756dd53d92d6f6a7adbacad','UNAVCO'),(105,44,59,'p3462750.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3462750.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/275/',27,19,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,20452,'257833d889870f6b37e8dad5b0fc7d34','UNAVCO'),(106,44,59,'p3462750.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3462750.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,1919626,'51217da846339a285c42793583687209','UNAVCO'),(107,44,59,'p3462750.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3462750.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,683900,'07adc79282e6352fd2de252a0b62d0ea','UNAVCO'),(108,44,59,'p3462750.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3462750.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/275/',3,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,32765,'41d3f43cb2c1d108b7749fc262b24c42','UNAVCO'),(109,44,59,'p3462770.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/277/p3462770.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/277/',27,19,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,20475,'29a3602dfd648e564ced6da7f05cea61','UNAVCO'),(110,44,59,'p3462770.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3462770.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,1919171,'af78c709da16af84c7f248f31cd5bf90','UNAVCO'),(111,44,59,'p3462770.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3462770.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,685077,'7e780d65bcf41877214013dd28e5b2c4','UNAVCO'),(112,44,59,'p3462770.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/277/p3462770.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/277/',3,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,33416,'cca21379af69616879d340b977762899','UNAVCO'),(113,44,59,'p3462740.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/274/p3462740.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/274/',3,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,33017,'641b10b25addd3094e41cb4ca5ee508a','UNAVCO'),(114,44,59,'p3462740.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/274/p3462740.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/274/',27,19,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,20471,'15df1c05520bb7ab5544e5d3f134efa3','UNAVCO'),(115,44,59,'p3462740.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3462740.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,1918629,'4d3fbc9dc85e1c1798fd3ad4dd7940fb','UNAVCO'),(116,44,59,'p3462740.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3462740.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,684937,'d6472c49f2443c416f1e34f989f12045','UNAVCO'),(117,43,57,'p3452800.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/280/p3452800.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/280/',3,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,32336,'e6b9e75f2ecd8255337429e169c3b8b3','UNAVCO'),(118,43,57,'p3452800.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/280/p3452800.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/280/',27,19,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,20511,'dc76e4517a94734140682a878c52ceb8','UNAVCO'),(119,43,57,'p3452800.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3452800.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,1865447,'b3e8becebd38b7c9637a869b04776995','UNAVCO'),(120,43,57,'p3452800.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3452800.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,688972,'3eec928038ecda4dd3c7fd23507198dd','UNAVCO'),(121,43,57,'p3452760.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/276/p3452760.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/276/',27,19,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,20503,'c41780563ec450c74b207553a7c3cc0a','UNAVCO'),(122,43,57,'p3452760.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3452760.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,1868469,'9116994f54f1831bf32916bfe31c2272','UNAVCO'),(123,43,57,'p3452760.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3452760.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,689196,'48af1577f161df24306ef4d45ed8be6a','UNAVCO'),(124,43,57,'p3452760.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/276/p3452760.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/276/',3,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,30572,'3862bfc90ce9f68f2a1a838b9cdbda9a','UNAVCO'),(125,43,57,'p3452780.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/278/p3452780.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/278/',3,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,33326,'18312c2f66d3d96c7b955b798d9dee9d','UNAVCO'),(126,43,57,'p3452780.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/278/p3452780.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/278/',27,19,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,20508,'2f6cc74ecb05cc2f2dac8ce6259a02e6','UNAVCO'),(127,43,57,'p3452780.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3452780.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,1866481,'661b41018117f0cb77f003569c93ec34','UNAVCO'),(128,43,57,'p3452780.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3452780.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,685278,'606a990f918e5d60248f38d92f301baf','UNAVCO'),(129,43,57,'p3452750.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3452750.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/275/',27,19,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,20507,'f6ee95c13491d5b71f47afd33f4f6617','UNAVCO'),(130,43,57,'p3452750.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3452750.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,1867613,'a6bcac25f3c0648035f46d7781d80db2','UNAVCO'),(131,43,57,'p3452750.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3452750.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,686343,'9f73244ee88c38598c58623135ea2caf','UNAVCO'),(132,43,57,'p3452750.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3452750.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/275/',3,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,33092,'d0302c7b528ccff5c143c38f9fd3165a','UNAVCO'),(133,43,57,'p3452770.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/277/p3452770.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/277/',27,19,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,20510,'1cb26a6e5c169e6d9a81442535810230','UNAVCO'),(134,43,57,'p3452770.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3452770.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,1866995,'ad6da2501652b07ec18c3f56d7f3cb7f','UNAVCO'),(135,43,57,'p3452770.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3452770.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,688324,'136f916e506b959d8e2e6a6b01d1feff','UNAVCO'),(136,43,57,'p3452770.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/277/p3452770.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/277/',3,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,32797,'cffe25e00edcd19344d736c17928ffc1','UNAVCO'),(137,43,57,'p3452740.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/274/p3452740.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/274/',27,19,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,20510,'22b0cc79ff8a3e587d49875a752e6420','UNAVCO'),(138,43,57,'p3452740.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3452740.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,1867479,'14b1cdd0c85285a520b783aadc48b421','UNAVCO'),(139,43,57,'p3452740.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3452740.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,687251,'f532afce2f73aeab9c6811fed2aad84b','UNAVCO'),(140,43,57,'p3452740.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/274/p3452740.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/274/',3,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,32898,'1bfcc4b4077dd1a0fa9411b2f24479ef','UNAVCO'),(141,43,57,'p3452790.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3452790.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/279/',27,19,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,20505,'7b934cd5ed248785c6efb9ff04eb176b','UNAVCO'),(142,43,57,'p3452790.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3452790.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,1866621,'25550f76f734510aa5c2bb2d6a33064f','UNAVCO'),(143,43,57,'p3452790.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3452790.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,687220,'02be52465d300d239757aa905381b0e9','UNAVCO'),(144,43,57,'p3452790.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/279/p3452790.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/279/',3,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,32555,'6c342486ca78a5a38c277a508a984e6e','UNAVCO'),(145,39,47,'p3402760.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/276/p3402760.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/276/',27,19,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,20598,'69a5318ac81c1fabbfb6d131cfa0caf5','UNAVCO'),(146,39,47,'p3402760.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3402760.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,1605081,'3e975722f7ec6e02b796b0efbfa06021','UNAVCO'),(147,39,47,'p3402760.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3402760.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,618927,'fdddc6fb1f33d302f11a0ec67c5e282d','UNAVCO'),(148,39,47,'p3402760.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/276/p3402760.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/276/',3,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,27202,'8aa64541b51057bb69f60d92436a76e1','UNAVCO'),(149,39,47,'p3402740.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/274/p3402740.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/274/',3,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,28942,'bfa02f0b78855881aa3d1422aa9629b7','UNAVCO'),(150,39,47,'p3402740.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/274/p3402740.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/274/',27,19,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,20598,'2f60d4a41713c7705c846dafb174ff42','UNAVCO'),(151,39,47,'p3402740.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3402740.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,1608063,'74815644fa7b20b55926151a80655a5e','UNAVCO'),(152,39,47,'p3402740.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3402740.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,616007,'7a060c5e7ae3c931e2e6cc7f8f080d86','UNAVCO'),(153,39,47,'p3402750.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3402750.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/275/',27,19,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,20598,'fec75aaca76a1e609467033251e47258','UNAVCO'),(154,39,47,'p3402750.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3402750.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,1605623,'65c09f89d9c1958190b7bb75487f9c68','UNAVCO'),(155,39,47,'p3402750.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3402750.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,615331,'7b4046be5008e2b0349c192b607e3fb8','UNAVCO'),(156,39,47,'p3402750.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3402750.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/275/',3,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,28751,'763696e3d6e86ced9e334b6e2f8c0063','UNAVCO'),(157,39,47,'p3402800.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/280/p3402800.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/280/',27,19,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,20604,'9e5f7e79087b57c034d466dac843cc35','UNAVCO'),(158,39,47,'p3402770.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/277/p3402770.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/277/',3,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,29653,'1e4421029a7967fb1a5fcb547c5528ec','UNAVCO'),(159,39,47,'p3402770.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/277/p3402770.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/277/',27,19,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,20594,'5e335eff48494a8d7f603f2a252e0844','UNAVCO'),(160,39,47,'p3402770.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3402770.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,1604607,'f2feb76828c02024fe44200963bdf16d','UNAVCO'),(161,39,47,'p3402770.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3402770.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,618973,'f37d4030216f074f6be2afc5c4f175c2','UNAVCO'),(162,39,47,'p3402800.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3402800.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,1603407,'9d292a1ea3f74b0d0c7f6b0a9a0b7883','UNAVCO'),(163,39,47,'p3402800.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3402800.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,617945,'d5a27afd8d5c946f16a73dc91d20e511','UNAVCO'),(164,39,47,'p3402800.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/280/p3402800.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/280/',3,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,29452,'3195536a0abddb35311d036d00b8ff59','UNAVCO'),(165,39,47,'p3402790.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/279/p3402790.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/279/',3,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,29315,'e473efb4b7eb37e675755c61cb3c62a4','UNAVCO'),(166,39,47,'p3402790.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3402790.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/279/',27,19,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,20601,'b5dd0ff61a564e75fd9f8c72fbe05c5b','UNAVCO'),(167,39,47,'p3402790.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3402790.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,1605197,'714935f108b3a7cd2827f591453aaee3','UNAVCO'),(168,39,47,'p3402790.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3402790.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,617801,'3554df7f44eb5b761b7a2f1dcee525d6','UNAVCO'),(169,39,47,'p3402780.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/278/p3402780.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/278/',3,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,29912,'2c3ec1cb9ddce2dd88ce8e7ffed21a30','UNAVCO'),(170,39,47,'p3402780.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/278/p3402780.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/278/',27,19,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,20603,'33326d600300a824f8cb396cf48694a4','UNAVCO'),(171,39,47,'p3402780.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3402780.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,1606137,'1e9e3d5078cc2c2a679881c8a6dbcaa5','UNAVCO'),(172,39,47,'p3402780.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3402780.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,618929,'61dfa9d534aab214591b3d073f65269a','UNAVCO'),(173,41,51,'p3432750.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3432750.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/275/',27,19,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,20592,'aef6a187a323322ed063acb21311e838','UNAVCO'),(174,41,51,'p3432750.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3432750.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,2021773,'dcc15f31c99064ec4568a4a3f91eff08','UNAVCO'),(175,41,51,'p3432750.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3432750.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,711368,'08a76920a8cdab8a9a900dbf76974d56','UNAVCO'),(176,41,51,'p3432750.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3432750.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/275/',3,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,35113,'5cc3f7219bf24864adcd5ad180137e9a','UNAVCO'),(177,41,51,'p3432780.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/278/p3432780.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/278/',27,19,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,20476,'890c80fd3f0fab0542f9bc278f372e67','UNAVCO'),(178,41,51,'p3432780.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3432780.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,2021289,'a6092609738787f1aa27f5f8bef0a988','UNAVCO'),(179,41,51,'p3432780.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3432780.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,712110,'c0242295eef63de8e8f1ed9e7a32055a','UNAVCO'),(180,41,51,'p3432780.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/278/p3432780.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/278/',3,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,35057,'077db845474b79093d8998920a1e44d8','UNAVCO'),(181,41,51,'p3432790.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3432790.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/279/',27,19,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,20508,'c8e1b4c0f05d3da095ab28ac68ac7666','UNAVCO'),(182,41,51,'p3432790.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3432790.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,2020651,'debf43d0adef3dac8193b2b2094a118b','UNAVCO'),(183,41,51,'p3432790.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3432790.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,714299,'cfe9e412a6d4ff5cfffbdb9bba1aecba','UNAVCO'),(184,41,51,'p3432790.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/279/p3432790.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/279/',3,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,34796,'0dcc21b6d455925a478221cf010fa03a','UNAVCO'),(185,41,51,'p3432760.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/276/p3432760.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/276/',27,19,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,20493,'7b57dcc6a997f16c114f22fe7e03d8ad','UNAVCO'),(186,41,51,'p3432760.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3432760.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,2021579,'6863d94e1d5c82a30fc5a26a1dd7c408','UNAVCO'),(187,41,51,'p3432760.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3432760.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,713554,'5a7bea49f2c4a5810c69660f2a0aa4d2','UNAVCO'),(188,41,51,'p3432760.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/276/p3432760.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/276/',3,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,32643,'ede08fca13faf15b27afb8a2624fb18b','UNAVCO'),(189,41,51,'p3432740.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/274/p3432740.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/274/',27,19,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,20537,'4b5d911967f480eab00f7c861973ab8a','UNAVCO'),(190,41,51,'p3432740.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3432740.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,2021681,'ebf9e90fb2de8025f3cd5c087370b4d4','UNAVCO'),(191,41,51,'p3432740.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3432740.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,710642,'4ab6c468947eba312b41f28454932401','UNAVCO'),(192,41,51,'p3432740.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/274/p3432740.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/274/',3,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,35068,'7fd28d049dabf718463d7614a83bc71a','UNAVCO'),(193,41,51,'p3432770.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/277/p3432770.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/277/',3,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,34987,'404579a98d15589fbd917ef612255710','UNAVCO'),(194,41,51,'p3432770.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/277/p3432770.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/277/',27,19,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,20477,'2381b3e8485f17431da5f701af431d76','UNAVCO'),(195,41,51,'p3432770.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3432770.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,2020281,'78eb97de20a06372d3e7c8511e40a3d0','UNAVCO'),(196,41,51,'p3432770.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3432770.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,711194,'dbdae9f9eec5345fc3067a7c4094b0ac','UNAVCO'),(197,41,51,'p3432800.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/280/p3432800.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/280/',27,19,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,20590,'25942d30656cd8b557e3832734daafba','UNAVCO'),(198,41,51,'p3432800.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3432800.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,2020927,'f35da4c4e7ea01e8bcfa274feea9b729','UNAVCO'),(199,41,51,'p3432800.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3432800.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,717269,'5f9629e39354079fcfb34db3333cc4ba','UNAVCO'),(200,41,51,'p3432800.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/280/p3432800.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/280/',3,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,34314,'de80e4e5b02844634b87e702a7ce11c1','UNAVCO'),(201,42,10,'p3442770.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/277/p3442770.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/277/',27,19,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,20586,'d56bb50e0e324200d68ffb04d62dab34','UNAVCO'),(202,42,10,'p3442770.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3442770.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,2006969,'cc324adad9cc11a2566349ba36c0dfe8','UNAVCO'),(203,42,10,'p3442770.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3442770.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,734557,'ba3ee21625ddd0d7af3f2352a1ef89ed','UNAVCO'),(204,42,10,'p3442770.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/277/p3442770.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/277/',3,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,35342,'2f2d84f27318d8d5522ec891e2a3aa4c','UNAVCO'),(205,42,10,'p3442770.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/277/p3442770.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/277/',6,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,18572,'529976fec44670b70bfb24fc59758d48','UNAVCO'),(206,42,10,'p3442780.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/278/p3442780.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/278/',27,19,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,20578,'256d1f017c4efc89151cdaa154c12d04','UNAVCO'),(207,42,10,'p3442780.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3442780.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,2006163,'fe90078c24b0778d30f1a10703585d3d','UNAVCO'),(208,42,10,'p3442780.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3442780.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,735897,'7b317f7677e11e156ed5845d1d3191b7','UNAVCO'),(209,42,10,'p3442780.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/278/p3442780.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/278/',3,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,35535,'70cf1ed858f8088269af7120055ddcb2','UNAVCO'),(210,42,10,'p3442780.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/278/p3442780.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/278/',6,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,19435,'a976454670db5f7c3db7ee1593c5a912','UNAVCO'),(211,42,10,'p3442800.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/280/p3442800.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/280/',27,19,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,20450,'e0d5588245b807ea81e099387928812f','UNAVCO'),(212,42,10,'p3442800.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3442800.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,2003965,'9b144e7f133a18b04416f1f7401bc3aa','UNAVCO'),(213,42,10,'p3442800.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3442800.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,741993,'d5e5c6ff2ea479de48807571ea37bc16','UNAVCO'),(214,42,10,'p3442800.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/280/p3442800.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/280/',3,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,33988,'109883e83fb859be7374a02c28a683e6','UNAVCO'),(215,42,10,'p3442800.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/280/p3442800.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/280/',6,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,19092,'00962977a6a893b968d5720227b4f8cf','UNAVCO'),(216,42,10,'p3442790.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3442790.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/279/',27,19,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,20483,'60dc589a3bfa49213e303977cc404ed2','UNAVCO'),(217,42,10,'p3442790.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3442790.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,2004537,'3b33223588c60919c4fe9597ee64d70c','UNAVCO'),(218,42,10,'p3442790.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3442790.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,738011,'305a5295c10285a10b30d03f2edfc5d6','UNAVCO'),(219,42,10,'p3442790.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/279/p3442790.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/279/',3,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,34635,'3ed3942279b34f4b63cf12a706b4063f','UNAVCO'),(220,42,10,'p3442790.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/279/p3442790.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/279/',6,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,19346,'2145aff2477c792c2509ba660d0aaef7','UNAVCO'),(221,42,10,'p3442760.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/276/p3442760.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/276/',27,19,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,20588,'62da886fd3eb25152dadba8e17b9f675','UNAVCO'),(222,42,10,'p3442760.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3442760.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,2006389,'98e2ff0c06a62dd8cc1e8efa4b86d714','UNAVCO'),(223,42,10,'p3442760.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3442760.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,739375,'8a5aa193f73fe8ed832e194c491a12c4','UNAVCO'),(224,42,10,'p3442760.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/276/p3442760.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/276/',3,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,32640,'6e032b090d258d4cf204d935aa4e0e21','UNAVCO'),(225,42,10,'p3442760.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/276/p3442760.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/276/',6,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,18950,'927cd2552b81ca79a7815db434d4579e','UNAVCO'),(226,42,10,'p3442740.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/274/p3442740.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/274/',27,19,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,20587,'56b7e8fdc39eeb1a0ab4e63f87ba30f5','UNAVCO'),(227,42,10,'p3442740.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3442740.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,2007255,'c3e0f550d51d84a7e596fc3e601b4e8d','UNAVCO'),(228,42,10,'p3442740.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3442740.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,738697,'97c62953c2da6ec621c1286cd8609471','UNAVCO'),(229,42,10,'p3442740.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/274/p3442740.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/274/',3,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,34968,'08ca4a60679f6a6841c20fdb945f1244','UNAVCO'),(230,42,10,'p3442740.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/274/p3442740.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/274/',6,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,18940,'20d5ccc0af0e98ceb6753d37503d1c85','UNAVCO'),(231,42,10,'p3442750.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3442750.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/275/',27,19,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,20586,'3d9231d2b8802de9cb5f4c0209845c10','UNAVCO'),(232,42,10,'p3442750.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3442750.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,2007281,'7af4be1b9411d4c8ce8f7610d9e94006','UNAVCO'),(233,42,10,'p3442750.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3442750.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,735399,'9460adf30d516354ec41deee927babe8','UNAVCO'),(234,42,10,'p3442750.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3442750.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/275/',3,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,35268,'2467e467b13ee62cec945b2957bdd077','UNAVCO'),(235,42,10,'p3442750.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/275/p3442750.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/275/',6,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,19032,'91ba533b48f8e2a0acb622d009a5e10b','UNAVCO'),(236,47,23,'p3492800.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/280/p3492800.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/280/',27,19,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,20550,'e9f4bf51863e41738c831800bc228760','UNAVCO'),(237,47,23,'p3492800.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3492800.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,1722698,'f536a6ef7eab144a14c01b90ca45262b','UNAVCO'),(238,47,23,'p3492800.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3492800.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,638761,'f50b5438679a805ebb43ed141be15e10','UNAVCO'),(239,47,23,'p3492800.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/280/p3492800.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/280/',3,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,30189,'c483e483627566cf3eb9a91fff3f8375','UNAVCO'),(240,47,23,'p3492800.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/280/p3492800.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/280/',6,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,5023,'fedc90bb2a2572f63c456192089a0024','UNAVCO'),(241,47,23,'p3492740.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/274/p3492740.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/274/',27,19,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,20555,'2ac4b467fe541f0b53467dc50033be3c','UNAVCO'),(242,47,23,'p3492740.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3492740.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,1724111,'7507eb77d732cf9a8ac1cb838b20d3cd','UNAVCO'),(243,47,23,'p3492740.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3492740.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,636693,'9cbb749de76b9b786977c7ff88944adb','UNAVCO'),(244,47,23,'p3492740.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/274/p3492740.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/274/',3,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,30373,'18b4c6dd542a95852dab405a82e543f5','UNAVCO'),(245,47,23,'p3492740.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/274/p3492740.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/274/',6,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,5036,'54ef3e79e350dd6e67fa02bdd15d493f','UNAVCO'),(246,47,23,'p3492750.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3492750.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/275/',27,19,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,20562,'62b0c3ceeb712f5db3ab819120c0147f','UNAVCO'),(247,47,23,'p3492750.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3492750.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,1723511,'eb3b9adc08c52a81a9d3119bf9561179','UNAVCO'),(248,47,23,'p3492750.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3492750.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,635665,'ef8380fe798101f3eeadd4290622e4c8','UNAVCO'),(249,47,23,'p3492750.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3492750.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/275/',3,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,30116,'1bcbee01dfe74cda8d35fce078453869','UNAVCO'),(250,47,23,'p3492750.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/275/p3492750.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/275/',6,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,4991,'77b56ed8deb795c726b5d42a8b8578f7','UNAVCO'),(251,47,23,'p3492770.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/277/p3492770.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/277/',27,19,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,20542,'de4eec1129b40b90e25cae13f1938cf8','UNAVCO'),(252,47,23,'p3492770.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3492770.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,1722511,'3db560c89c9d07d3ff931111b31eb4df','UNAVCO'),(253,47,23,'p3492770.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3492770.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,635641,'33c1ba4af6bf0e3eb324be5f63838ced','UNAVCO'),(254,47,23,'p3492770.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/277/p3492770.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/277/',3,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,30993,'3323bfa08726bdd6fcd22ee2c6696da6','UNAVCO'),(255,47,23,'p3492770.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/277/p3492770.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/277/',6,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,4792,'a8cfd19abe9eafe1585d9d6da83410da','UNAVCO'),(256,47,23,'p3492760.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/276/p3492760.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/276/',27,19,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,20545,'2ccd280dc6054cdac027eb28da5d9ffe','UNAVCO'),(257,47,23,'p3492760.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3492760.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,1721393,'e56b5c423dc432490955eb931192bdb5','UNAVCO'),(258,47,23,'p3492760.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3492760.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,639111,'ad47c5ce8470a99d8214b61576ae3ed6','UNAVCO'),(259,47,23,'p3492760.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/276/p3492760.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/276/',3,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,28972,'f8793bffa904e9c400db816085826976','UNAVCO'),(260,47,23,'p3492760.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/276/p3492760.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/276/',6,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,4904,'ad3d64aeda9905eb1131de09774b65ec','UNAVCO'),(261,47,23,'p3492790.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3492790.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/279/',27,19,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,20547,'0a1f47073a06d8a4c8f2e56ed44512aa','UNAVCO'),(262,47,23,'p3492790.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3492790.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,1722332,'abab5f3d1873f6ae1746489a29266c99','UNAVCO'),(263,47,23,'p3492790.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3492790.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,636577,'5db6ccf0e78f1bb31c007065dec3759f','UNAVCO'),(264,47,23,'p3492790.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/279/p3492790.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/279/',3,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,30686,'ba0c2829d8757f8842a5e1d4afa95f7c','UNAVCO'),(265,47,23,'p3492790.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/279/p3492790.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/279/',6,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,5083,'d2c3a239c76362456ac9c3f7d509d35e','UNAVCO'),(266,47,23,'p3492780.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/278/p3492780.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/278/',27,19,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,20556,'cfad1e941a2a5330f54b9177b607c268','UNAVCO'),(267,47,23,'p3492780.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3492780.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,1721767,'1d3653d1734480048a01967d44182256','UNAVCO'),(268,47,23,'p3492780.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3492780.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,637073,'2cf52c0fce6dfd2f2ac7923f08a42658','UNAVCO'),(269,47,23,'p3492780.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/278/p3492780.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/278/',3,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,31652,'8a24069cce3a0eec3da3f68de83cf30c','UNAVCO'),(270,47,23,'p3492780.15m.Z','ftp://data-out.unavco.org/pub/rinex/met/2015/278/p3492780.15m.Z','ftp','data-out.unavco.org','/pub/rinex/met/2015/278/',6,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,5068,'af9fd976b79fb6447533c921a405c858','UNAVCO'),(271,45,61,'p3472770.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/277/p3472770.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/277/',27,19,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,20454,'d05b1ebcd9001b3075c24308fcf8c140','UNAVCO'),(272,45,61,'p3472770.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3472770.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,1969586,'2e56af5d07f261b85ae3da2c19dc2d06','UNAVCO'),(273,45,61,'p3472770.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/277/p3472770.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/277/',2,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,707547,'2714ba1af79a00416796aee0b0132750','UNAVCO'),(274,45,61,'p3472770.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/277/p3472770.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/277/',3,1,NULL,'2015-10-04 00:00:00','2015-10-04 23:59:45','2015-10-05 00:00:00',15,NULL,2015,277,34987,'cce6fe75c4d29efb1cc67fb7f388d5de','UNAVCO'),(275,45,61,'p3472750.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/275/p3472750.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/275/',27,19,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,20453,'e860ef76eb04dfb9a5949348005d9529','UNAVCO'),(276,45,61,'p3472750.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3472750.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,1969160,'1a742b32a4fb62fcfb0523d1f8d8104f','UNAVCO'),(277,45,61,'p3472750.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/275/p3472750.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/275/',2,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,704641,'17cc6e4c81b2d3ed3face0227a48909f','UNAVCO'),(278,45,61,'p3472750.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/275/p3472750.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/275/',3,1,NULL,'2015-10-02 00:00:00','2015-10-02 23:59:45','2015-10-03 00:00:00',15,NULL,2015,275,34766,'1d1a1dc3109c3ad9bab639736354a767','UNAVCO'),(279,45,61,'p3472790.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/279/p3472790.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/279/',27,19,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,20457,'aa23065d0fef40177129ffc4465d7cbc','UNAVCO'),(280,45,61,'p3472790.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3472790.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,1968768,'1b3ca9ea2ae4ad130650e70235a87ffa','UNAVCO'),(281,45,61,'p3472790.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/279/p3472790.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/279/',2,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,706258,'c71e65b8a24333620944d8550cefbc7a','UNAVCO'),(282,45,61,'p3472790.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/279/p3472790.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/279/',3,1,NULL,'2015-10-06 00:00:00','2015-10-06 23:59:45','2015-10-07 00:00:00',15,NULL,2015,279,33997,'8a0cacc09158b95b77127cfaca6952ca','UNAVCO'),(283,45,61,'p3472800.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/280/p3472800.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/280/',27,19,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,20453,'88a86134f3be496462a834eddb6b92c5','UNAVCO'),(284,45,61,'p3472800.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3472800.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,1965668,'ed58716226ebeb36a72acd0b9f833eb7','UNAVCO'),(285,45,61,'p3472800.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/280/p3472800.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/280/',2,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,713377,'8af3cabc6215070ea21741f06d14630b','UNAVCO'),(286,45,61,'p3472800.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/280/p3472800.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/280/',3,1,NULL,'2015-10-07 00:00:00','2015-10-07 23:59:45','2015-10-08 00:00:00',15,NULL,2015,280,33815,'f60f30b47074c2b0a8db5b9d00055040','UNAVCO'),(287,45,61,'p3472760.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/276/p3472760.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/276/',27,19,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,20452,'d1d6c0f4a4256b3ed5345b51bd9fb069','UNAVCO'),(288,45,61,'p3472760.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3472760.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,1970362,'97205eebb76e2a7aa8c2b4f39e2413b0','UNAVCO'),(289,45,61,'p3472760.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/276/p3472760.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/276/',2,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,710178,'85fecf877fbb394bdb543cd7c0db827c','UNAVCO'),(290,45,61,'p3472760.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/276/p3472760.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/276/',3,1,NULL,'2015-10-03 00:00:00','2015-10-03 23:59:45','2015-10-04 00:00:00',15,NULL,2015,276,32454,'f7a26d9aca951997ea7306380916ee7b','UNAVCO'),(291,45,61,'p3472740.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/274/p3472740.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/274/',27,19,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,20454,'7af7fbb1ddffe34d0863fb0f4022ace6','UNAVCO'),(292,45,61,'p3472740.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3472740.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,1968473,'c487b615937bb3a8975c1715eccb5681','UNAVCO'),(293,45,61,'p3472740.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/274/p3472740.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/274/',2,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,703583,'53cc63173e99db2c996fd7fdf2d6978a','UNAVCO'),(294,45,61,'p3472740.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/274/p3472740.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/274/',3,1,NULL,'2015-10-01 00:00:00','2015-10-01 23:59:45','2015-10-02 00:00:00',15,NULL,2015,274,34633,'88b280c0c7e9fe37ebd052db5886336a','UNAVCO'),(295,45,61,'p3472780.15n.Z','ftp://data-out.unavco.org/pub/rinex/nav/2015/278/p3472780.15n.Z','ftp','data-out.unavco.org','/pub/rinex/nav/2015/278/',3,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,34918,'2d2f91e5c038ced0eecf3fd1d54a7c66','UNAVCO'),(296,45,61,'p3472780.15S','ftp://data-out.unavco.org/pub/rinex/qc/2015/278/p3472780.15S','ftp','data-out.unavco.org','/pub/rinex/qc/2015/278/',27,19,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,20454,'9d1f15e8503701861799aba0bfd1db9b','UNAVCO'),(297,45,61,'p3472780.15o.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3472780.15o.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,1971051,'57fc96b66e0043b1ec5a2c9a2951baf3','UNAVCO'),(298,45,61,'p3472780.15d.Z','ftp://data-out.unavco.org/pub/rinex/obs/2015/278/p3472780.15d.Z','ftp','data-out.unavco.org','/pub/rinex/obs/2015/278/',2,1,NULL,'2015-10-05 00:00:00','2015-10-05 23:59:45','2015-10-06 00:00:00',15,NULL,2015,278,707301,'b1a21d5c8037fc6cc7caa1f58e6cdfaf','UNAVCO');
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafile_format`
--

LOCK TABLES `datafile_format` WRITE;
/*!40000 ALTER TABLE `datafile_format` DISABLE KEYS */;
INSERT INTO `datafile_format` VALUES (1,'RINEX 2'),(2,'RINEX 3'),(3,'BINEX'),(4,'SINEX'),(5,'UNR tenv3 northings and eastings'),(6,'UNR txyz2 Cartesian xyz'),(7,'UNR tenv traditional NEU'),(8,'plot image'),(9,'UNR station QC estimate .qa file'),(10,'UNR kenv 5 minute products'),(11,'UNR krms RMS products'),(12,'DORIS'),(13,'SLR'),(14,'VLBI'),(15,'BOTTLE'),(16,'SEED'),(17,'PBO GPS Velocity Field Format'),(18,'PBO GPS Station Position Time Series, .pos'),(19,'teqc qc summary S file');
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
INSERT INTO `equip_config` VALUES (10,42,'2011-12-02 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 17:50:06',10,'0220368169',0.0083,2,'G3260009',2,NULL,29,'4614207064',NULL,15),(22,47,'2008-09-03 00:00:00','2010-07-08 23:59:45',NULL,NULL,NULL,'2015-10-23 17:50:07',10,'0220366150',0.0083,1,'C5010030',2,NULL,28,'4527253331',NULL,15),(23,47,'2010-07-09 00:00:00',NULL,NULL,NULL,NULL,'2015-10-23 17:50:07',10,'0220366150',0.0083,1,'C5010030',2,NULL,29,'4527253331',NULL,15),(45,39,'2008-02-14 23:38:45','2009-03-11 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220382762',0.0083,3,'',2,NULL,28,'4623116489',NULL,15),(46,39,'2009-03-12 00:00:00','2010-10-21 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220377400',0.0083,3,'',2,NULL,28,'4623116489',NULL,15),(47,39,'2010-10-22 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220377400',0.0083,3,'',2,NULL,29,'4623116489',NULL,15),(48,40,'2005-09-23 00:19:00','2010-07-17 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220366152',0.0083,3,'',2,NULL,28,'4518249765',NULL,15),(49,40,'2010-07-18 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220366152',0.0083,3,'',2,NULL,29,'4518249765',NULL,15),(50,41,'2007-06-21 04:04:30','2010-06-12 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220346567',0.0083,3,'',2,NULL,28,'4541260323',NULL,15),(51,41,'2010-12-06 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220346567',0.0083,3,'',2,NULL,29,'4541260323',NULL,15),(52,42,'2006-08-23 00:00:00','2010-07-11 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220368169',0.0083,3,'',2,NULL,28,'4614207064',NULL,15),(53,42,'2010-07-12 00:00:00','2011-12-01 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220368169',0.0083,3,'',2,NULL,29,'4614207064',NULL,15),(54,43,'2005-10-01 00:51:30','2008-09-02 21:19:00',NULL,NULL,NULL,'2015-10-23 18:01:25',10,'0220365593',0.0083,3,'',2,NULL,28,'4518249763',NULL,15),(55,43,'2008-09-02 21:20:30','2008-09-10 21:56:30',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379553',0.0083,3,'',2,NULL,28,'4518249763',NULL,15),(56,43,'2008-10-09 23:32:45','2010-07-08 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379553',0.0083,3,'',2,NULL,28,'4539259362',NULL,15),(57,43,'2010-07-09 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379553',0.0083,3,'',2,NULL,29,'4539259362',NULL,15),(58,44,'2007-11-26 00:00:00','2010-07-14 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379511',0.0083,3,'',2,NULL,28,'4625209417',NULL,15),(59,44,'2010-07-15 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220379511',0.0083,3,'',2,NULL,29,'4625209417',NULL,15),(60,45,'2007-09-13 18:55:00','2010-07-14 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'4623A16401',0.0083,3,'',2,NULL,28,'4625209636',NULL,15),(61,45,'2010-07-15 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'4623A16401',0.0083,3,'',2,NULL,29,'4625209636',NULL,15),(62,46,'2005-09-28 00:16:45','2010-07-11 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220366135',0.0083,3,'',2,NULL,28,'4516248599',NULL,15),(63,46,'2010-07-12 00:00:00','2015-10-15 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220366135',0.0083,3,'',2,NULL,29,'4516248599',NULL,15),(64,47,'2005-10-27 21:08:30','2008-09-02 23:59:45',NULL,NULL,NULL,'2015-10-23 18:01:26',10,'0220366150',0.0083,3,'',2,NULL,28,'4527253331',NULL,15),(65,48,'2008-08-30 16:35:15','2010-07-14 23:59:45',NULL,NULL,NULL,'2015-11-11 16:02:57',10,'0220382763',0.0083,3,'',2,NULL,28,'4704127081',NULL,15),(66,48,'2010-07-15 00:00:00','2015-11-10 23:59:45',NULL,NULL,NULL,'2015-11-11 16:02:57',10,'0220382763',0.0083,3,'',2,NULL,29,'4704127081',NULL,15);
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locale`
--

LOCK TABLES `locale` WRITE;
/*!40000 ALTER TABLE `locale` DISABLE KEYS */;
INSERT INTO `locale` VALUES (1,'not specified'),(2,'Felipe Carrillo Puerto'),(3,'Weaverville'),(4,'Cerrillos'),(5,'Whiskeytown'),(6,'Del Loma'),(7,'Corning'),(8,'Red Bluff'),(9,'Laporte'),(10,'Adin'),(11,'Burney'),(12,'Shasta Lake'),(13,'Potter Valley'),(15,'Fairfield');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `province_state`
--

LOCK TABLES `province_state` WRITE;
/*!40000 ALTER TABLE `province_state` DISABLE KEYS */;
INSERT INTO `province_state` VALUES (1,'not specified'),(2,'Quintana Roo'),(3,'California'),(4,'Puerto Rico'),(5,'Idaho');
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
  `retired_date` datetime DEFAULT NULL,
  `published_date` datetime DEFAULT NULL,
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
  `ellipsoid_id` int(1) unsigned DEFAULT NULL,
  PRIMARY KEY (`station_id`),
  KEY `style_id_idx` (`style_id`),
  KEY `status_id_idx` (`status_id`),
  KEY `access_id_idx` (`access_id`),
  KEY `monument_style_id_idx` (`monument_style_id`),
  KEY `nation_id_idx` (`nation_id`),
  KEY `locale_id_idx` (`locale_id`),
  KEY `agency_id_idx` (`agency_id`),
  KEY `ellipsoid_id` (`ellipsoid_id`),
  CONSTRAINT `ellipsoid_id` FOREIGN KEY (`ellipsoid_id`) REFERENCES `ellipsoid` (`ellipsoid_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `access_id` FOREIGN KEY (`access_id`) REFERENCES `access` (`access_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `agency_id` FOREIGN KEY (`agency_id`) REFERENCES `agency` (`agency_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
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
INSERT INTO `station` VALUES (39,'P340','DashielCrkCN2008',39.4093,-123.0498,865.23,-2691539.1186,-4136726.977,4028080.6731,'2008-02-14 23:38:45',NULL,NULL,2,2,1,1,4,3,3,13,'PBO;PBO Analysis  Complete;PBO Core Network;','UNAVCO','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/P340.jpg','http://pboshared.unavco.org/timeseries/P340_timeseries_cleaned.png',NULL,NULL,NULL),(40,'P341','WhiskytownCN2005',40.6507,-122.6069,406.85,NULL,NULL,NULL,'2005-09-23 00:19:00',NULL,NULL,2,2,1,1,3,3,3,5,'PBO;PBO Analysis  Complete;PBO Core Network;','UNAVCO','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/P341.jpg','http://pboshared.unavco.org/timeseries/P341_timeseries_cleaned.png',NULL,NULL,NULL),(41,'P343','ChinaPeak_CN2007',40.8871,-123.3342,1617.91,NULL,NULL,NULL,'2007-06-21 04:04:30',NULL,NULL,2,2,1,1,4,3,3,6,'PBO;PBO Analysis  Complete;PBO Core Network;','UNAVCO','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/P343.jpg','http://pboshared.unavco.org/timeseries/P343_timeseries_cleaned.png',NULL,NULL,NULL),(42,'P344','VinaHelitkCN2006',39.9291,-122.028,50.29,NULL,NULL,NULL,'2006-08-23 00:00:00',NULL,NULL,2,2,1,1,3,3,3,7,'PBO;PBO Analysis  Complete;PBO Core Network;','UNAVCO','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/P344.jpg','http://pboshared.unavco.org/timeseries/P344_timeseries_cleaned.png',NULL,NULL,NULL),(43,'P345','HookerDomeCN2005',40.2712,-122.2708,134.08,NULL,NULL,NULL,'2005-10-01 00:51:30',NULL,NULL,2,2,1,1,3,3,3,8,'PBO;PBO Analysis  Complete;PBO Core Network;','UNAVCO','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/P345.jpg','http://pboshared.unavco.org/timeseries/P345_timeseries_cleaned.png',NULL,NULL,NULL),(44,'P346','BuzzardRstCN2007',39.7947,-120.8675,2039.37,NULL,NULL,NULL,'2007-11-26 00:00:00',NULL,NULL,2,2,1,1,4,3,3,9,'PBO;PBO Analysis  Complete;PBO Core Network;','UNAVCO','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/P346.jpg','http://geodesy.unr.edu/tsplots/IGS08/TimeSeries/P346.png',NULL,NULL,NULL),(45,'P347','AdinCTYardCN2007',41.1833,-120.9484,1269.28,NULL,NULL,NULL,'2007-09-13 18:55:00',NULL,NULL,2,2,1,1,3,3,3,10,'PBO;PBO Analysis  Complete;PBO Core Network;','UNAVCO','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/P347.jpg','http://geodesy.unr.edu/tsplots/IGS08/TimeSeries/P347.png',NULL,NULL,NULL),(46,'P348','HatchetMtnCN2005',40.9055,-121.828,1668.07,NULL,NULL,NULL,'2005-09-28 00:16:45',NULL,NULL,2,2,1,1,3,3,3,11,'PBO;PBO Analysis  Complete;PBO Core Network;','UNAVCO','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/P348.jpg','http://geodesy.unr.edu/tsplots/IGS08/TimeSeries/P348.png',NULL,NULL,NULL),(47,'P349','WonderlandCN2005',40.7311,-122.3194,275.4,NULL,NULL,NULL,'2005-10-27 21:08:30',NULL,NULL,2,2,1,1,3,3,3,12,'PBO;PBO Analysis  Complete;PBO Core Network;','UNAVCO','','http://www.unavco.org/data/gps-gnss/lib/images/station_images/P349.jpg','http://geodesy.unr.edu/tsplots/IGS08/TimeSeries/P349.png',NULL,NULL,NULL),(48,'P350','SmokyDome_ID2008',43.5328,-114.8628,2388.3,NULL,NULL,NULL,'2008-08-30 16:35:15',NULL,NULL,1,2,1,1,3,3,5,15,'PBO;PBO Analysis Complete;PBO Core Network;',NULL,'','','http://geodesy.unr.edu/tsplots/IGS08/TimeSeries_cleaned/P350.png',NULL,NULL,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station_style`
--

LOCK TABLES `station_style` WRITE;
/*!40000 ALTER TABLE `station_style` DISABLE KEYS */;
INSERT INTO `station_style` VALUES (1,'GPS/GNSS Continuous'),(2,'GPS/GNSS Campaign'),(3,'GPS/GNSS Mobile'),(4,'GPS/GNSS Episodic');
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

-- Dump completed on 2015-11-19 11:13:59
