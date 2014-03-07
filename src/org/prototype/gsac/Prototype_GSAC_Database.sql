-- MySQL dump 10.13  Distrib 5.1.69, for debian-linux-gnu (i486)
--
-- Host: localhost    Database: Prototype_GSAC_Database
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
-- Current Database: `Prototype_GSAC_Database`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `Prototype_GSAC_Database` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `Prototype_GSAC_Database`;

--
-- Table structure for table `access_permission`
--

DROP TABLE IF EXISTS `access_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `access_permission` (
  `access_permission_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `access_permission_description` varchar(80) NOT NULL,
  PRIMARY KEY (`access_permission_id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access_permission`
--

LOCK TABLES `access_permission` WRITE;
/*!40000 ALTER TABLE `access_permission` DISABLE KEYS */;
INSERT INTO `access_permission` VALUES (1,'no public access allowed'),(2,'public access allowed for station metadata, instrument metadata, and data files'),(3,'public access allowed for station and instrument metadata only');
/*!40000 ALTER TABLE `access_permission` ENABLE KEYS */;
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
  `mailing_address` varchar(200) DEFAULT NULL,
  `agency_url` varchar(100) DEFAULT NULL,
  `prime_contact_name` varchar(100) DEFAULT NULL,
  `prime_telephone` varchar(50) DEFAULT NULL,
  `prime_fax` varchar(50) DEFAULT NULL,
  `prime_email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`agency_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agency`
--

LOCK TABLES `agency` WRITE;
/*!40000 ALTER TABLE `agency` DISABLE KEYS */;
/*!40000 ALTER TABLE `agency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `antenna_session`
--

DROP TABLE IF EXISTS `antenna_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `antenna_session` (
  `antenna_session_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `station_id` int(6) unsigned NOT NULL,
  `antenna_type_id` int(5) unsigned NOT NULL,
  `antenna_serial_number` varchar(20) NOT NULL,
  `antenna_installed_date` datetime NOT NULL,
  `antenna_removed_date` datetime DEFAULT NULL,
  `antenna_offset_up` float NOT NULL,
  `antenna_offset_north` float NOT NULL,
  `antenna_offset_east` float NOT NULL,
  `antenna_HtCod` char(5) DEFAULT NULL,
  `radome_type_id` int(5) unsigned DEFAULT NULL,
  PRIMARY KEY (`antenna_session_id`)
) ENGINE=MyISAM AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `antenna_session`
--

LOCK TABLES `antenna_session` WRITE;
/*!40000 ALTER TABLE `antenna_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `antenna_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `antenna_type`
--

DROP TABLE IF EXISTS `antenna_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `antenna_type` (
  `antenna_type_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `antenna_type_name` varchar(15) NOT NULL,
  `igs_defined` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`antenna_type_id`)
) ENGINE=MyISAM AUTO_INCREMENT=644 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `antenna_type`
--

LOCK TABLES `antenna_type` WRITE;
/*!40000 ALTER TABLE `antenna_type` DISABLE KEYS */;
INSERT INTO `antenna_type` VALUES (311,'3S-02-1AERO-CR ','Y'),(312,'3S-02-1AERO+CR ','Y'),(313,'3S-02-2AERO+CR ','Y'),(314,'3S-02-2AERO+GP ','Y'),(315,'3S-02-DM+CR    ','Y'),(316,'3S-02-DM-CR    ','Y'),(317,'3S-02-MACOM+GP ','Y'),(318,'3S-02-MAN      ','Y'),(319,'3S-02-TE+GP    ','Y'),(320,'3S-02-TSADM    ','Y'),(321,'3S-02-TSATE    ','Y'),(322,'APSAPS-3       ','Y'),(323,'APSAPS-3L      ','Y'),(324,'ASH111661      ','Y'),(325,'ASH700228A     ','Y'),(326,'ASH700228A+EX  ','Y'),(327,'ASH700228B     ','Y'),(328,'ASH700228B+EX  ','Y'),(329,'ASH700228C     ','Y'),(330,'ASH700228C+EX  ','Y'),(331,'ASH700228D     ','Y'),(332,'ASH700228E     ','Y'),(333,'ASH700699.L1   ','Y'),(334,'ASH700700.A    ','Y'),(335,'ASH700700.B    ','Y'),(336,'ASH700700.C    ','Y'),(337,'ASH700718A     ','Y'),(338,'ASH700718A1    ','Y'),(339,'ASH700718B     ','Y'),(340,'ASH700829.2    ','Y'),(341,'ASH700829.3    ','Y'),(342,'ASH700829.A    ','Y'),(343,'ASH700829.A1   ','Y'),(344,'ASH700936A_M   ','Y'),(345,'ASH700936B_M   ','Y'),(346,'ASH700936C_M   ','Y'),(347,'ASH700936D_M   ','Y'),(348,'ASH700936E     ','Y'),(349,'ASH700936E_C   ','Y'),(350,'ASH700936F_C   ','Y'),(351,'ASH701008.01B  ','Y'),(352,'ASH701073.1    ','Y'),(353,'ASH701073.3    ','Y'),(354,'ASH701933A_M   ','Y'),(355,'ASH701933B_M   ','Y'),(356,'ASH701933C_M   ','Y'),(357,'ASH701941.1    ','Y'),(358,'ASH701941.2    ','Y'),(359,'ASH701941.A    ','Y'),(360,'ASH701941.B    ','Y'),(361,'ASH701945B_M   ','Y'),(362,'ASH701945C_M   ','Y'),(363,'ASH701945D_M   ','Y'),(364,'ASH701945E_M   ','Y'),(365,'ASH701945G_M   ','Y'),(366,'ASH701946.2    ','Y'),(367,'ASH701946.3    ','Y'),(368,'ASH701975.01A  ','Y'),(369,'ASH701975.01AGP','Y'),(370,'ASH802147_A    ','Y'),(371,'ASHAC_L1       ','Y'),(372,'ASHAC_L1/L2    ','Y'),(373,'ASHMAR/RANGE   ','Y'),(374,'700228 NOTCH   ','Y'),(375,'700228 RINGS   ','Y'),(376,'A-C L1         ','Y'),(377,'A-C L1/L2      ','Y'),(378,'ASH701945.02B  ','Y'),(379,'ASH701946.012  ','Y'),(380,'ASH701946.022  ','Y'),(381,'ASH701975.01Agp','Y'),(382,'ASHTECH MICROZ ','Y'),(383,'DORNE MARGOLIN ','Y'),(384,'GEODETIC III L1','Y'),(385,'GEODETIC L1/L2 ','Y'),(386,'GEODETIC L1/L2 ','Y'),(387,'MARINE/RANGE   ','Y'),(388,'GMXZENITH10    ','Y'),(389,'GMXZENITH20    ','Y'),(390,'ITT3750323     ','Y'),(391,'JAV_GRANT-G3T  ','Y'),(392,'JAV_RINGANT_G3T','Y'),(393,'JAV_TRIUMPH-1  ','Y'),(394,'JAVRINGANT_DM  ','Y'),(395,'JAVTRIANT      ','Y'),(396,'JAVTRIUMPH_VS  ','Y'),(397,'JNSMARANT_GGD  ','Y'),(398,'JPSREGANT_DD_E ','Y'),(399,'JPSREGANT_DD_E1','Y'),(400,'JPSREGANT_DD_E2','Y'),(401,'JPSREGANT_DD_I ','Y'),(402,'JPSREGANT_SD_E ','Y'),(403,'JPSREGANT_SD_E1','Y'),(404,'JPSREGANT_SD_E2','Y'),(405,'JPSREGANT_SD_I ','Y'),(406,'JPSMARANT_GGD  ','Y'),(407,'LEIAR10        ','Y'),(408,'LEIAR20        ','Y'),(409,'LEIAR25        ','Y'),(410,'LEIAR25.R3     ','Y'),(411,'LEIAR25.R4     ','Y'),(412,'LEIAS05        ','Y'),(413,'LEIAS10        ','Y'),(414,'LEIAT201       ','Y'),(415,'LEIAT202+GP    ','Y'),(416,'LEIAT202-GP    ','Y'),(417,'LEIAT302+GP    ','Y'),(418,'LEIAT302-GP    ','Y'),(419,'LEIAT303       ','Y'),(420,'LEIAT501       ','Y'),(421,'LEIAT502       ','Y'),(422,'LEIAT503       ','Y'),(423,'LEIAT504       ','Y'),(424,'LEIAT504GG     ','Y'),(425,'LEIATX1230     ','Y'),(426,'LEIATX1230GG   ','Y'),(427,'LEIATX1230+GNSS','Y'),(428,'LEIAX1201      ','Y'),(429,'LEIAX1202      ','Y'),(430,'LEIAX1202GG    ','Y'),(431,'LEIAX1203+GNSS ','Y'),(432,'LEIGG02PLUS    ','Y'),(433,'LEIGS08        ','Y'),(434,'LEIGS08PLUS    ','Y'),(435,'LEIGS09        ','Y'),(436,'LEIGS12        ','Y'),(437,'LEIGS14        ','Y'),(438,'LEIGS15        ','Y'),(439,'LEIMNA1202GG   ','Y'),(440,'LEIMNA950GG    ','Y'),(441,'LEISR299_INT   ','Y'),(442,'LEISR399_INT   ','Y'),(443,'LEISR399_INTA  ','Y'),(444,'DORNE MARGOLIN ','Y'),(445,'EXTERNAL WITH G','Y'),(446,'EXTERNAL WITHOU','Y'),(447,'INTERNAL       ','Y'),(448,'LEICA AT201    ','Y'),(449,'LEICA AT202/302','Y'),(450,'LEICA AT202/302','Y'),(451,'LEICA AT303    ','Y'),(452,'LEICA AT501    ','Y'),(453,'LEICA AT502    ','Y'),(454,'LEICA AT503    ','Y'),(455,'LEICA INTERNAL ','Y'),(456,'MAGPM-500      ','Y'),(457,'MAGELLAN PM-500','Y'),(458,'MP-1372FW+REGP ','Y'),(459,'MPL_WAAS_2224NW','Y'),(460,'MPL_WAAS_2225NW','Y'),(461,'M-PULSE L1/L2 S','Y'),(462,'MAC4647942     ','Y'),(463,'MACPATCH       ','Y'),(464,'MACROMETER X-DI','Y'),(465,'MINIMAC PATCH  ','Y'),(466,'NAX3G+C        ','Y'),(467,'NOV_WAAS_600   ','Y'),(468,'NOV501         ','Y'),(469,'NOV501+CR      ','Y'),(470,'NOV502         ','Y'),(471,'NOV502+CR      ','Y'),(472,'NOV503+CR      ','Y'),(473,'NOV531         ','Y'),(474,'NOV531+CR      ','Y'),(475,'NOV533+CR      ','Y'),(476,'NOV600LB       ','Y'),(477,'NOV701GG       ','Y'),(478,'NOV701GGL      ','Y'),(479,'NOV702         ','Y'),(480,'NOV702GG       ','Y'),(481,'NOV702GGL      ','Y'),(482,'NOV702L        ','Y'),(483,'NOV703GGG.R2   ','Y'),(484,'NOV704X        ','Y'),(485,'NOV750         ','Y'),(486,'NOV750.R4      ','Y'),(487,'PRXG4          ','Y'),(488,'PRXG5          ','Y'),(489,'AOAD/M_B       ','Y'),(490,'AOAD/M_T       ','Y'),(491,'AOAD/M_TA_NGS  ','Y'),(492,'JPLD/M_R       ','Y'),(493,'JPLD/M_RA_SOP  ','Y'),(494,'DORNE MARGOLIN ','Y'),(495,'DORNE MARGOLIN ','Y'),(496,'DORNE MARGOLIN ','Y'),(497,'RNG80971.00    ','Y'),(498,'SEN67157596+CR ','Y'),(499,'SEPCHOKE_MC    ','Y'),(500,'SOK502         ','Y'),(501,'SOK600         ','Y'),(502,'SOK702         ','Y'),(503,'SOK_GSR2700ISX ','Y'),(504,'SOK_RADIAN_IS  ','Y'),(505,'STHS82_7224V3.0','Y'),(506,'SPECTRA PRECISI','Y'),(507,'SPP571212238+GP','Y'),(508,'SPP571908273   ','Y'),(509,'SPP68410_10    ','Y'),(510,'SPP89823_10    ','Y'),(511,'STXS9SA7224V3.0','Y'),(512,'TI4100_100     ','Y'),(513,'TI4100_2000    ','Y'),(514,'TI4100_4000    ','Y'),(515,'TIAPENG3100R1  ','Y'),(516,'TIAPENSMT8883G ','Y'),(517,'TPS LEGACY     ','Y'),(518,'TPS NET-G3A    ','Y'),(519,'TPS NETG3      ','Y'),(520,'TPS ODYSSEY_E  ','Y'),(521,'TPS ODYSSEY_I  ','Y'),(522,'TOP700337      ','Y'),(523,'TOP700779A     ','Y'),(524,'TOP72110       ','Y'),(525,'TPSCR.G3       ','Y'),(526,'TPSCR.G5       ','Y'),(527,'TPSCR3_GGD     ','Y'),(528,'TPSCR4         ','Y'),(529,'TPSG3_A1       ','Y'),(530,'TPSGR3         ','Y'),(531,'TPSGR5         ','Y'),(532,'TPSHIPER_GD    ','Y'),(533,'TPSLEGANT_G    ','Y'),(534,'TPSLEGANT2     ','Y'),(535,'TPSLEGANT3_UHF ','Y'),(536,'TPSODYSSEY_I   ','Y'),(537,'TPSPG_A1       ','Y'),(538,'TPSPG_A1+GP    ','Y'),(539,'TOPCON GP-R1SD ','Y'),(540,'TOPCR3_GGD     ','Y'),(541,'TRM10877.10_H  ','Y'),(542,'TRM11877.10+SGP','Y'),(543,'TRM12333.00+RGP','Y'),(544,'TRM12562.00+SGP','Y'),(545,'TRM12562.10+RGP','Y'),(546,'TRM14156.00-GP ','Y'),(547,'TRM14177.00    ','Y'),(548,'TRM14532.00    ','Y'),(549,'TRM14532.10    ','Y'),(550,'TRM16741.00    ','Y'),(551,'TRM17200.00    ','Y'),(552,'TRM22020.00+GP ','Y'),(553,'TRM22020.00-GP ','Y'),(554,'TRM23903.00    ','Y'),(555,'TRM23965.00    ','Y'),(556,'TRM26738.00    ','Y'),(557,'TRM27947.00+GP ','Y'),(558,'TRM27947.00-GP ','Y'),(559,'TRM29659.00    ','Y'),(560,'TRM33429.00+GP ','Y'),(561,'TRM33429.00-GP ','Y'),(562,'TRM33429.20+GP ','Y'),(563,'TRM39105.00    ','Y'),(564,'TRM4000ST_INT  ','Y'),(565,'TRM41249.00    ','Y'),(566,'TRM41249USCG   ','Y'),(567,'TRM4800        ','Y'),(568,'TRM55970.00    ','Y'),(569,'TRM55971.00    ','Y'),(570,'TRM57970.00    ','Y'),(571,'TRM57971.00    ','Y'),(572,'TRM59800.00    ','Y'),(573,'TRM59800.80    ','Y'),(574,'TRM59900.00    ','Y'),(575,'TRMR4          ','Y'),(576,'TRMR4-2        ','Y'),(577,'TRMR4-3        ','Y'),(578,'TRMR6          ','Y'),(579,'TRMR6-2        ','Y'),(580,'TRMR6-3        ','Y'),(581,'TRMR6-4        ','Y'),(582,'TRMR8_GNSS     ','Y'),(583,'TRMR8_GNSS3    ','Y'),(584,'TRMR8-4        ','Y'),(585,'TRMR10         ','Y'),(586,'TRMSPS985      ','Y'),(587,'4000SE INTERNAL','Y'),(588,'4000SL MICRO   ','Y'),(589,'4000SLD L1/L2  ','Y'),(590,'4000ST INTERNAL','Y'),(591,'4000ST KINEMATI','Y'),(592,'4000ST L1 GEODE','Y'),(593,'4000ST L1/L2 GE','Y'),(594,'4000SX MICRO   ','Y'),(595,'DORNE MARGOLIN ','Y'),(596,'TR GEOD L1/L2 G','Y'),(597,'TR GEOD L1/L2 W','Y'),(598,'TRM10877.10+RGP','Y'),(599,'TRM10877.10+SGP','Y'),(600,'GPPNULLANTENNA ','Y'),(602,'none given','N'),(637,'NOV53','N'),(638,'LEIAX1202G','N'),(639,'LEIAX1203+GNS','N'),(640,'LEIAR10 NON','N'),(623,'NOV533','N'),(636,'AS10','N'),(635,'LEIAR10 NONE','N'),(634,'AT504 LEIS ','N'),(641,'LEIAX120','N'),(642,'TRM41249.00 NON','N'),(643,'ASH111661 NON','N');
/*!40000 ALTER TABLE `antenna_type` ENABLE KEYS */;
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
) ENGINE=MyISAM AUTO_INCREMENT=65 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `country`
--

LOCK TABLES `country` WRITE;
/*!40000 ALTER TABLE `country` DISABLE KEYS */;
/*!40000 ALTER TABLE `country` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_type`
--

DROP TABLE IF EXISTS `file_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file_type` (
  `file_type_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `file_type_name` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`file_type_id`)
) ENGINE=MyISAM AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_type`
--

LOCK TABLES `file_type` WRITE;
/*!40000 ALTER TABLE `file_type` DISABLE KEYS */;
INSERT INTO `file_type` VALUES (1,'instrument raw binary file'),(2,'RINEX GPS navigation file'),(3,'RINEX meteorological file'),(4,'RINEX observation file'),(5,'RINEX GLONASS navigation file'),(6,'RINEX Galileo navigation file'),(7,'RINEX QZSS navigation file'),(8,'RINEX COMPASS navigation file'),(9,'position time series PBO cvs format'),(10,'position time series PBO pos format'),(11,'SOPAC XML site log'),(12,'SINEX'),(13,'station.info GAMIT'),(14,'IGS format site log'),(15,'BSM (borehole strainmeter) Raw'),(16,'BSM (borehole strainmeter) Processed'),(17,'BSM (borehole strainmeter) Notes'),(18,'BSM (borehole strainmeter) time series plots'),(19,'Tiltmeter Raw ASCII Data'),(20,'Tiltmeter Plots'),(21,'Seismometer data'),(22,'time series plot image'),(23,'SLR data'),(24,'VLBI data'),(25,'DORIS data'),(26,'BINEX'),(27,'GNSS observation QC file (teqc \'S\' file)'),(28,'tide gauge data');
/*!40000 ALTER TABLE `file_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gnss_data_file`
--

DROP TABLE IF EXISTS `gnss_data_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gnss_data_file` (
  `file_id` int(6) unsigned NOT NULL AUTO_INCREMENT,
  `station_id` int(6) unsigned NOT NULL,
  `file_type_id` int(3) unsigned NOT NULL,
  `file_sample_interval` float DEFAULT NULL,
  `data_start_time` datetime NOT NULL,
  `data_stop_time` datetime NOT NULL,
  `data_year` int(4) unsigned DEFAULT NULL,
  `data_day_of_year` int(3) unsigned DEFAULT NULL,
  `published_date` datetime NOT NULL,
  `revision_time` datetime DEFAULT NULL,
  `file_size` int(10) unsigned DEFAULT NULL,
  `file_MD5` char(32) DEFAULT NULL,
  `file_url` varchar(120) DEFAULT NULL,
  `file_url_protocol` varchar(5) DEFAULT NULL,
  `file_url_ip_domain` varchar(120) DEFAULT NULL,
  `file_url_folders` varchar(120) DEFAULT NULL,
  `file_url_filename` varchar(120) DEFAULT NULL,
  `access_permission_id` int(3) unsigned DEFAULT NULL,
  `embargo_after_date` datetime DEFAULT NULL,
  `embargo_duration_hours` int(6) unsigned DEFAULT NULL,
  PRIMARY KEY (`file_id`)
) ENGINE=MyISAM AUTO_INCREMENT=74919 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gnss_data_file`
--

LOCK TABLES `gnss_data_file` WRITE;
/*!40000 ALTER TABLE `gnss_data_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `gnss_data_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monument_description`
--

DROP TABLE IF EXISTS `monument_description`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `monument_description` (
  `monument_description_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `monument_description` varchar(70) NOT NULL,
  PRIMARY KEY (`monument_description_id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monument_description`
--

LOCK TABLES `monument_description` WRITE;
/*!40000 ALTER TABLE `monument_description` DISABLE KEYS */;
/*!40000 ALTER TABLE `monument_description` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `province_region_state`
--

DROP TABLE IF EXISTS `province_region_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `province_region_state` (
  `province_region_state_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `province_region_state_name` varchar(70) NOT NULL,
  PRIMARY KEY (`province_region_state_id`)
) ENGINE=MyISAM AUTO_INCREMENT=60 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `province_region_state`
--

LOCK TABLES `province_region_state` WRITE;
/*!40000 ALTER TABLE `province_region_state` DISABLE KEYS */;
/*!40000 ALTER TABLE `province_region_state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `radome_type`
--

DROP TABLE IF EXISTS `radome_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `radome_type` (
  `radome_type_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `radome_type_name` varchar(4) NOT NULL,
  `igs_defined` char(1) NOT NULL DEFAULT 'N',
  `description_English` varchar(150) DEFAULT NULL,
  `description_RENAG` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`radome_type_id`)
) ENGINE=MyISAM AUTO_INCREMENT=37 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `radome_type`
--

LOCK TABLES `radome_type` WRITE;
/*!40000 ALTER TABLE `radome_type` DISABLE KEYS */;
INSERT INTO `radome_type` VALUES (1,'SNOW','Y','ASHTECH conical \'snow\' antenna dome','RadÃ´me conique d\'ASHTECH'),(2,'JAVC','Y','Conical dome for JAV_RINGANT_G3T chokering antenna','RadÃ´me conique pour l\'antenne chokering      JAVAD JAV_RINGANT_G3T'),(3,'JVDM','Y','Conical dome for JAVRINGANT_DM chokering antenna','RadÃ´me conique pour l\'antenne chokering JAVAD JAVRINGANT_DM'),(4,'LEIC','Y','Dome for LEICA AT303 and AT503 conical shape','RadÃ´me conique pour les antennes LEICA AT303 et AT503'),(5,'LEIM','Y','Dome for LEICA AR20','[RadÃ´me pour l\'antenne LEICA AR20]'),(6,'LEIS','Y','Dome for DORNE MARGOLIN LEICA spherical shape','RadÃ´me sphÃ©rique pour l\'antenne LEICA de type    DORNE MARGOLIN'),(7,'LEIT','Y','Dome for LEICA AR25','[RadÃ´me pour l\'antenne LEICA AR25]'),(8,'MMAC','Y','Clear spherical dome covering antenna element with MiniMac MAC4647942 antenna','RadÃ´me sphÃ©rique couvrant les Ã©lÃ©ments  de l\'antenne Macrometer MiniMac MAC4647942'),(9,'NOVC','Y','NOVATEL conical dome used with the NOV533+CR antenna','[RadÃ´me conique NOVATEL pour l\'antenne NOV533+CR]'),(10,'NOVS','Y','NOVATEL spherical dome used with NOV750       NOV750.R4 NOV750.R5','[RadÃ´me sphÃ©rique NOVATEL pour les antennes NOV750, NOV750.R4, NOV750.R5]'),(11,'SPKE','Y','SPECTRA PRECISION conical dome with spike used with the SPP571908273 antenna','RadÃ´me conique avec   pointe de     SPECTRA PRECISION utiliÃ© avec l\'antenne SPP571908273'),(12,'CONE','Y','Topcon part no. 10-031401-01 conical dome','RadÃ´me conique pour l\'antenne Topcon p/n 10-031401-01'),(13,'TPSD','Y','Topcon hemispherical dome for PG-A1 and  G3-A1','RadÃ´me hÃ©misphÃ©rique pour l\'antenne Topcon PG-A1 et G3-A1'),(14,'TPSH','Y','Topcon hemispherical dome for choke ring antenna','RadÃ´me hÃ©misphÃ©rique pour l\'antenne choke ring de Topcon'),(15,'RPTR','Y','Regal Plastic 19 inch dome for TRM33429.00+GP','RadÃ´me ancien de 19 pouces en plastique pour l\'antenne Trimble TRM33429.00+GP'),(16,'TCWD','Y','Dome for Trimble 24490-00','RadÃ´me      conique pour l\'antenne Trimble 24490-00'),(17,'TZGD','Y','hemispheric for Zephyr Geodetic 41249.00 antenna','RadÃ´me hÃ©misphÃ©rique pour l\'antenne Zephyr Geodetic 41249.00'),(18,'NONE','Y','No antenna dome','Pas de radÃ´me'),(19,'DOME','Y','Miscellaneous antenna dome','D\'autres types  de           radÃ´mes'),(20,'SCIS','Y','SCIGN short antenna dome','RadÃ´me de SCIGN de petite taille (dite short pour moins de hauteur)'),(21,'AUST','Y','Australian radome: hemispherical High Impact Clear Perspex','RadÃ´me          australien hÃ©misphÃ©rique en Perspex clair'),(22,'BEVA','Y','Austrian dome with cylinders and spherical cap elongated design','RadÃ´me autrichien de type cylindrique alongÃ© avec dÃ´me sphÃ©rique'),(23,'CAFG','Y','Kearney-         Powerglass fiberglass enclosure used in California by USGS&early SCIGN sites(PVEP TRAK HOLC)','Coffre en fibre de verre dite Kearney-Powerglass, utilisÃ© en Californie par USGS pour les vieilles stations SCIGN'),(24,'DUTD','Y','Delft University of Technology Design','RadÃ´me hollandais     fabriquÃ© par Delft University of Technology'),(25,'EMRA','Y','EMR clear spherical acrylic dome','RadÃ´me sphÃ©rique acrylique clair fabriquÃ© par NRCan, utilisÃ© dans le rÃ©seau WCDA (Western Canada Deformation Array)'),(26,'ENCL','Y','Miscellaneous antenna enclosure','RadÃ´mes type inconnus'),(27,'GRAZ','Y','see BEVA and OLGA. Moved dome GRAZ to *Previously Valid*','RadÃ´me autrichien de type   cylindrique avec dÃ´me sphÃ©rique'),(28,'JPLA','Y','JPL acrylic dome','RadÃ´me acrylique fabriquÃ© par JPL'),(29,'OLGA','Y','Austrian dome with cylinders and spherical cap standard design','RadÃ´me autrichien de type standard       cylindrique    avec dÃ´me sphÃ©rique'),(30,'OSOD','Y','Dome used in Swedish array','RadÃ´me utilisÃ© dans le rÃ©seau suÃ©dois'),(31,'PFAN','Y','Austrian dome with cylinders and spherical cap big size design','Gros radÃ´me           autrichien de type cylindrique avec dÃ´me sphÃ©rique'),(32,'SCIT','Y','SCIGN tall antenna dome','RadÃ´me de SCIGN de grande taille (dite tall pour plus de hauteur)'),(33,'SCPL','Y','Polycarbonate enclosures 24-48 diameter used at   early SCIGN sites (CHIL CMP9 CLAR LEEP)','RadÃ´me en polycarbonate de 24-48 pouces de diamÃ¨tre utilisÃ© dans les vieux sites SCIGN (CHIL, CMP9, CLAR, LEEP)'),(34,'UNAV','Y','UNAVCO clear spherical antenna dome','RadÃ´me sphÃ©rique clair de l\'UNAVCO. Ring Cap'),(35,'unkn','Y','Use for historic data periods only when radome status was not accurately recorded',NULL),(36,'CHCD','Y','CHC hemispherical  long life dome for CHCC220GR',NULL);
/*!40000 ALTER TABLE `radome_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receiver_firmware_version`
--

DROP TABLE IF EXISTS `receiver_firmware_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `receiver_firmware_version` (
  `receiver_firmware_version_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `receiver_firmware_version_name` varchar(20) NOT NULL,
  `SwVer` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`receiver_firmware_version_id`)
) ENGINE=MyISAM AUTO_INCREMENT=55 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receiver_firmware_version`
--

LOCK TABLES `receiver_firmware_version` WRITE;
/*!40000 ALTER TABLE `receiver_firmware_version` DISABLE KEYS */;
INSERT INTO `receiver_firmware_version` VALUES (1,'1.52/2.120',' 1.52'),(2,'3.00/2.121',' 3.00'),(3,'3.00/2.127',' 3.00'),(4,'6.00/3.015',' 6.00'),(5,'7.80/3.018',' 7.80'),(6,'7.80/3.019',' 7.80'),(7,'8.00/4.005',' 8.00'),(8,'1K00-1D04',' 8.80'),(9,'3.3.32.5',' 3.30'),(10,'5.60',' 5.60'),(11,'5.62',' 5.62'),(12,'5.97',' 5.97'),(43,'2.62/6.112',' 2.62'),(42,'1.1-3',' 1.10'),(15,'7.01/3.017',' 7.01'),(16,'7.50/3.017',' 7.50'),(17,'7.50/3.018',' 7.50'),(18,'8.10/3.019',' 8.10'),(19,'3.1 Jun,28,2007 p3',' 3.10'),(20,'3.3 Dec,22,2008 p6',' 3.30'),(21,'4.2','-----'),(22,'5.62/3.014',' 5.62'),(23,'8.00/3.019',' 8.00'),(24,'8.00/4.004',' 8.00'),(25,'8.10/4.004',' 8.10'),(41,'none given','-----'),(27,'V 5.10',' 5.10'),(28,'3.17',' 3.17'),(29,'4.03',' 4.03'),(30,'5.00',' 5.00'),(31,'7.53/2.125',' 7.53'),(32,'V 5.62',' 5.62'),(33,'5.10',' 5.10'),(34,'1.00',' 1.00'),(35,'2.00 (642)',' 2.00'),(36,'1.35',' 1.35'),(37,'7.80',' 7.80'),(38,'3.00',' 3.00'),(39,'2.14',' 2.14'),(44,'2.62',' 2.62'),(45,'2.00',' 2.00'),(46,'4.1','-----'),(47,'5.62/3.019',' 5.62');
/*!40000 ALTER TABLE `receiver_firmware_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receiver_session`
--

DROP TABLE IF EXISTS `receiver_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `receiver_session` (
  `receiver_session_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `station_id` int(6) unsigned NOT NULL,
  `receiver_type_id` int(5) unsigned NOT NULL,
  `receiver_firmware_version_id` int(5) unsigned NOT NULL,
  `receiver_serial_number` varchar(20) NOT NULL,
  `receiver_installed_date` datetime NOT NULL,
  `receiver_removed_date` datetime DEFAULT NULL,
  `receiver_sample_interval` float DEFAULT NULL,
  `satellite_system` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`receiver_session_id`)
) ENGINE=MyISAM AUTO_INCREMENT=276 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receiver_session`
--

LOCK TABLES `receiver_session` WRITE;
/*!40000 ALTER TABLE `receiver_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `receiver_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receiver_type`
--

DROP TABLE IF EXISTS `receiver_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `receiver_type` (
  `receiver_type_id` int(5) unsigned NOT NULL AUTO_INCREMENT,
  `receiver_type_name` varchar(20) NOT NULL,
  `igs_defined` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`receiver_type_id`)
) ENGINE=MyISAM AUTO_INCREMENT=613 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receiver_type`
--

LOCK TABLES `receiver_type` WRITE;
/*!40000 ALTER TABLE `receiver_type` DISABLE KEYS */;
INSERT INTO `receiver_type` VALUES (306,'3SNAV GNSS-300      ','Y'),(307,'3SNAV GNSS-300T     ','Y'),(308,'3SNAV R100 OLD      ','Y'),(309,'3SNAV R101 OLD      ','Y'),(310,'3SNAV R100-30       ','Y'),(311,'3SNAV R100-30T 2    ','Y'),(312,'3SNAV R100-30T 12   ','Y'),(313,'3SNAV R100-40       ','Y'),(314,'3SNAV R100-40T 4    ','Y'),(315,'3SNAV R100-40T 12   ','Y'),(316,'ALTUS APS-3         ','Y'),(317,'ALTUS APS-3L        ','Y'),(318,'ASHTECH 3DF-XXIV    ','Y'),(319,'ASHTECH 802147_A    ','Y'),(320,'ASHTECH D-XII       ','Y'),(321,'ASHTECH G-XII       ','Y'),(322,'ASHTECH GG24C       ','Y'),(323,'ASHTECH L-XII       ','Y'),(324,'ASHTECH LCS-XII     ','Y'),(325,'ASHTECH LM-XII3     ','Y'),(326,'ASHTECH M-XII       ','Y'),(327,'ASHTECH MS-XII      ','Y'),(328,'ASHTECH P-XII3      ','Y'),(329,'ASHTECH PF500       ','Y'),(330,'ASHTECH PF800       ','Y'),(331,'ASHTECH RANGER      ','Y'),(332,'ASHTECH S-XII       ','Y'),(333,'ASHTECH SUPER-CA    ','Y'),(334,'ASHTECH UZ-12       ','Y'),(335,'ASHTECH Z-X         ','Y'),(336,'ASHTECH Z-XII3      ','Y'),(337,'ASHTECH Z-XII3GETT  ','Y'),(338,'ASHTECH Z-XII3T     ','Y'),(339,'ASHTECH Z18         ','Y'),(340,'GEOMAX ZENITH10     ','Y'),(341,'GEOMAX ZENITH20     ','Y'),(342,'IFEN NX_NTR_303_D   ','Y'),(343,'IFEN NX_NTR_500_D   ','Y'),(344,'IFEN NX_NTR_600_D   ','Y'),(345,'IFEN SX_NSR_RT_200  ','Y'),(346,'IFEN SX_NSR_RT_300  ','Y'),(347,'IFEN SX_NSR_RT_400  ','Y'),(348,'IFEN SX_NSR_RT_401  ','Y'),(349,'IFEN SX_NSR_RT_402  ','Y'),(350,'IFEN SX_NSR_RT_700  ','Y'),(351,'IFEN SX_NSR_RT_800  ','Y'),(352,'ITT 3750300         ','Y'),(353,'JAVAD DUO_G2        ','Y'),(354,'JAVAD DUO_G2 DELTA  ','Y'),(355,'JAVAD DUO_G2 SIGMA  ','Y'),(356,'JAVAD DUO_G2D       ','Y'),(357,'JAVAD DUO_G2D DELTA ','Y'),(358,'JAVAD DUO_G2D SIGMA ','Y'),(359,'JAVAD DUO_G3D       ','Y'),(360,'JAVAD DUO_G3D DELTA ','Y'),(361,'JAVAD DUO_G3D SIGMA ','Y'),(362,'JAVAD GISMORE       ','Y'),(363,'JAVAD QUA_G3D       ','Y'),(364,'JAVAD QUA_G3D DELTA ','Y'),(365,'JAVAD QUA_G3D SIGMA ','Y'),(366,'JAVAD TR_G2         ','Y'),(367,'JAVAD TR_G2T        ','Y'),(368,'JAVAD TR_G2T ALPHA  ','Y'),(369,'JAVAD TR_G2TH       ','Y'),(370,'JAVAD TR_G2TH ALPHA ','Y'),(371,'JAVAD TR_G3         ','Y'),(372,'JAVAD TR_G3 ALPHA   ','Y'),(373,'JAVAD TR_G3H        ','Y'),(374,'JAVAD TR_G3H ALPHA  ','Y'),(375,'JAVAD TR_G3T        ','Y'),(376,'JAVAD TR_G3T ALPHA  ','Y'),(377,'JAVAD TR_G3TH       ','Y'),(378,'JAVAD TR_G3TH ALPHA ','Y'),(379,'JAVAD TR_VS         ','Y'),(380,'JAVAD TRE_G2T       ','Y'),(381,'JAVAD TRE_G2T DELTA ','Y'),(382,'JAVAD TRE_G2T SIGMA ','Y'),(383,'JAVAD TRE_G2TH      ','Y'),(384,'JAVAD TRE_G2TH DELTA','Y'),(385,'JAVAD TRE_G2TH SIGMA','Y'),(386,'JAVAD TRE_G3T       ','Y'),(387,'JAVAD TRE_G3T DELTA ','Y'),(388,'JAVAD TRE_G3T SIGMA ','Y'),(389,'JAVAD TRE_G3TAJ     ','Y'),(390,'JAVAD TRE_G3TAJ DELT','Y'),(391,'JAVAD TRE_G3TAJ SIGM','Y'),(392,'JAVAD TRE_G3TH      ','Y'),(393,'JAVAD TRE_G3TH DELTA','Y'),(394,'JAVAD TRE_G3TH SIGMA','Y'),(395,'JAVAD TRIUMPH1      ','Y'),(396,'JAVAD TRIUMPH4      ','Y'),(397,'JPS E_GGD           ','Y'),(398,'JPS EGGDT           ','Y'),(399,'JPS EUROCARD        ','Y'),(400,'JPS LEGACY          ','Y'),(401,'JPS ODYSSEY         ','Y'),(402,'JPS REGENCY         ','Y'),(403,'LEICA ATX1230       ','Y'),(404,'LEICA ATX1230+GNSS  ','Y'),(405,'LEICA CRS1000       ','Y'),(406,'LEICA GG02PLUS      ','Y'),(407,'LEICA GMX901        ','Y'),(408,'LEICA GMX902        ','Y'),(409,'LEICA GMX902GG      ','Y'),(410,'LEICA GR10          ','Y'),(411,'LEICA GR25          ','Y'),(412,'LEICA GRX1200       ','Y'),(413,'LEICA GRX1200+      ','Y'),(414,'LEICA GRX1200+GNSS  ','Y'),(415,'LEICA GRX1200GGPRO  ','Y'),(416,'LEICA GRX1200LITE   ','Y'),(417,'LEICA GRX1200PRO    ','Y'),(418,'LEICA GS08          ','Y'),(419,'LEICA GS08PLUS      ','Y'),(420,'LEICA GS09          ','Y'),(421,'LEICA GS10          ','Y'),(422,'LEICA GS12          ','Y'),(423,'LEICA GS14          ','Y'),(424,'LEICA GS15          ','Y'),(425,'LEICA GS25          ','Y'),(426,'LEICA GX1210        ','Y'),(427,'LEICA GX1210+       ','Y'),(428,'LEICA GX1220        ','Y'),(429,'LEICA GX1220+       ','Y'),(430,'LEICA GX1220+GNSS   ','Y'),(431,'LEICA GX1230        ','Y'),(432,'LEICA GX1230+       ','Y'),(433,'LEICA GX1230+GNSS   ','Y'),(434,'LEICA GX1230GG      ','Y'),(435,'LEICA MC1000        ','Y'),(436,'LEICA MC500         ','Y'),(437,'LEICA MNA950GG      ','Y'),(438,'LEICA MNS1250GG     ','Y'),(439,'LEICA RS500         ','Y'),(440,'LEICA SR260         ','Y'),(441,'LEICA SR261         ','Y'),(442,'LEICA SR299         ','Y'),(443,'LEICA SR299E        ','Y'),(444,'LEICA SR399         ','Y'),(445,'LEICA SR399E        ','Y'),(446,'LEICA SR510         ','Y'),(447,'LEICA SR520         ','Y'),(448,'LEICA SR530         ','Y'),(449,'LEICA SR9400        ','Y'),(450,'LEICA SR9500        ','Y'),(451,'LEICA SR9600        ','Y'),(452,'MAGELLAN PM-500     ','Y'),(453,'MINIMAC 2816        ','Y'),(454,'MINIMAC 2816AT      ','Y'),(455,'NAVCOM NCT-2000D    ','Y'),(456,'NAVCOM NCT-2030M    ','Y'),(457,'NAVCOM RT-3010S     ','Y'),(458,'NAVCOM RT-3020M     ','Y'),(459,'NAVCOM RT-3020S     ','Y'),(460,'NAVCOM SF-2000      ','Y'),(461,'NAVCOM SF-2040G     ','Y'),(462,'NAVCOM SF-2050G     ','Y'),(463,'NAVCOM SF-2050M     ','Y'),(464,'NAVCOM SF-2050R     ','Y'),(465,'NOV 15A             ','Y'),(466,'NOV EURO4-1.00-222  ','Y'),(467,'NOV MILLEN-RT2      ','Y'),(468,'NOV MILLEN-RT2OS    ','Y'),(469,'NOV MILLEN-STD      ','Y'),(470,'NOV MILLEN-STDW     ','Y'),(471,'NOV OEM4-G2         ','Y'),(472,'NOV OEM6            ','Y'),(473,'NOV OEMV1           ','Y'),(474,'NOV OEMV1G          ','Y'),(475,'NOV OEMV2           ','Y'),(476,'NOV OEMV3           ','Y'),(477,'NOV OEMV3-RT2       ','Y'),(478,'NOV WAASGII         ','Y'),(479,'PREXISO G4          ','Y'),(480,'PREXISO G5          ','Y'),(481,'AOA BENCHMARK ACT   ','Y'),(482,'AOA ICS-4000Z       ','Y'),(483,'AOA ICS-4000Z ACT   ','Y'),(484,'AOA RASCAL-12       ','Y'),(485,'AOA RASCAL-8        ','Y'),(486,'AOA SNR-12 ACT      ','Y'),(487,'AOA SNR-8000 ACT    ','Y'),(488,'AOA SNR-8100 ACT    ','Y'),(489,'AOA TTR-12          ','Y'),(490,'AOA TTR-4P          ','Y'),(491,'ROGUE SNR-12        ','Y'),(492,'ROGUE SNR-12 RM     ','Y'),(493,'ROGUE SNR-8         ','Y'),(494,'ROGUE SNR-800       ','Y'),(495,'ROGUE SNR-8000      ','Y'),(496,'ROGUE SNR-8100      ','Y'),(497,'ROGUE SNR-8A        ','Y'),(498,'ROGUE SNR-8C        ','Y'),(499,'RNG FASA+           ','Y'),(500,'SEPT ASTERX1        ','Y'),(501,'SEPT ASTERX2        ','Y'),(502,'SEPT ASTERX2E       ','Y'),(503,'SEPT ASTERX2EH      ','Y'),(504,'SEPT ASTERX2EL      ','Y'),(505,'SEPT ASTERX3        ','Y'),(506,'SEPT POLARX2        ','Y'),(507,'SEPT POLARX2C       ','Y'),(508,'SEPT POLARX2E       ','Y'),(509,'SEPT POLARX3        ','Y'),(510,'SEPT POLARX3E       ','Y'),(511,'SEPT POLARX3EG      ','Y'),(512,'SEPT POLARX3ETR     ','Y'),(513,'SEPT POLARX3G       ','Y'),(514,'SEPT POLARX3TR      ','Y'),(515,'SEPT POLARX4        ','Y'),(516,'SEPT POLARX4TR      ','Y'),(517,'SEPT POLARXS        ','Y'),(518,'SOK GSR2600         ','Y'),(519,'SOK GSR2700 RS      ','Y'),(520,'SOK GSR2700 RSX     ','Y'),(521,'SOK RADIAN          ','Y'),(522,'SOK RADIAN_IS       ','Y'),(523,'SOUTH S82T GNSS     ','Y'),(524,'SOUTH S82V GNSS     ','Y'),(525,'SPP 68410_10        ','Y'),(526,'SPP GEODIMETER-L1   ','Y'),(527,'SPP GEOTRACER100    ','Y'),(528,'SPP GEOTRACER3220   ','Y'),(529,'SPP GEOTRACER3320   ','Y'),(530,'SPP PROMARK700      ','Y'),(531,'GEO DUAL FREQ GPS   ','Y'),(532,'GEO GEODIMETER L1   ','Y'),(533,'GEO GPS-GLONASS     ','Y'),(534,'SPECTRA PRECISION   ','Y'),(535,'STONEX S9II GNSS    ','Y'),(536,'TI4100              ','Y'),(537,'TIASAHI PENG3100-R1 ','Y'),(538,'TIASAHI PENSMT888-3G','Y'),(539,'TOPCON GP-DX1       ','Y'),(540,'TOPCON GP-R1        ','Y'),(541,'TOPCON GP-R1D       ','Y'),(542,'TOPCON GP-R1DP      ','Y'),(543,'TOPCON GP-R1DY      ','Y'),(544,'TOPCON GP-S1        ','Y'),(545,'TOPCON GP-SX1       ','Y'),(546,'TOPCON TT4000SSI    ','Y'),(547,'TOPCON TURBO-SII    ','Y'),(548,'TPS E_GGD           ','Y'),(549,'TPS EUROCARD        ','Y'),(550,'TPS GB-1000         ','Y'),(551,'TPS GR3             ','Y'),(552,'TPS GR5             ','Y'),(553,'TPS HIPER_GD        ','Y'),(554,'TPS HIPER_GGD       ','Y'),(555,'TPS HIPER_LITE      ','Y'),(556,'TPS HIPER_PLUS      ','Y'),(557,'TPS LEGACY          ','Y'),(558,'TPS NET-G3A         ','Y'),(559,'TPS NETG3           ','Y'),(560,'TPS ODYSSEY_E       ','Y'),(561,'TPS ODYSSEY_I       ','Y'),(562,'TRIMBLE 4000S       ','Y'),(563,'TRIMBLE 4000SE      ','Y'),(564,'TRIMBLE 4000SL      ','Y'),(565,'TRIMBLE 4000SLD     ','Y'),(566,'TRIMBLE 4000SSE     ','Y'),(567,'TRIMBLE 4000SSI     ','Y'),(568,'TRIMBLE 4000SSI-SS  ','Y'),(569,'TRIMBLE 4000SST     ','Y'),(570,'TRIMBLE 4000ST      ','Y'),(571,'TRIMBLE 4000ST S    ','Y'),(572,'TRIMBLE 4000SX      ','Y'),(573,'TRIMBLE 4400        ','Y'),(574,'TRIMBLE 4600        ','Y'),(575,'TRIMBLE 4700        ','Y'),(576,'TRIMBLE 4800        ','Y'),(577,'TRIMBLE 5700        ','Y'),(578,'TRIMBLE 5800        ','Y'),(579,'TRIMBLE 7400MSI     ','Y'),(580,'TRIMBLE GEODESIST P ','Y'),(581,'TRIMBLE MS750       ','Y'),(582,'TRIMBLE NETR3       ','Y'),(583,'TRIMBLE NETR5       ','Y'),(584,'TRIMBLE NETR8       ','Y'),(585,'TRIMBLE NETR9       ','Y'),(586,'TRIMBLE NETRS       ','Y'),(587,'TRIMBLE R4          ','Y'),(588,'TRIMBLE R4-2        ','Y'),(589,'TRIMBLE R4-3        ','Y'),(590,'TRIMBLE R5          ','Y'),(591,'TRIMBLE R6          ','Y'),(592,'TRIMBLE R6-2        ','Y'),(593,'TRIMBLE R6-3        ','Y'),(594,'TRIMBLE R6-4        ','Y'),(595,'TRIMBLE R7          ','Y'),(596,'TRIMBLE R7 GNSS     ','Y'),(597,'TRIMBLE R8          ','Y'),(598,'TRIMBLE R8 GNSS     ','Y'),(599,'TRIMBLE R8 GNSS3    ','Y'),(600,'TRIMBLE R8-4        ','Y'),(601,'TRIMBLE R10         ','Y'),(602,'TRIMBLE SPS855      ','Y'),(603,'TRIMBLE SPS985      ','Y'),(604,'CMC ALLSTAR 12      ','Y'),(605,'CMC ALLSTAR OEM     ','Y'),(606,'ESA/ISN GNSS        ','Y'),(607,'ROCKWELL ZODIAC OEM ','Y'),(609,'LEICA GRX1200PROGG','N'),(610,'GR10','N'),(611,'none given','N');
/*!40000 ALTER TABLE `receiver_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `station`
--

DROP TABLE IF EXISTS `station`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `station` (
  `station_id` int(6) unsigned NOT NULL AUTO_INCREMENT,
  `code_4char_ID` char(4) NOT NULL,
  `station_name` varchar(50) NOT NULL,
  `latitude_north` double NOT NULL,
  `longitude_east` double NOT NULL,
  `ellipsoidal_height` float DEFAULT NULL,
  `station_installed_date` datetime NOT NULL,
  `station_style_id` int(3) unsigned NOT NULL,
  `access_permission_id` int(3) unsigned DEFAULT NULL,
  `monument_description_id` int(5) unsigned NOT NULL,
  `country_id` int(5) unsigned NOT NULL,
  `province_region_state_id` int(5) unsigned DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `z` double DEFAULT NULL,
  `station_removed_date` datetime DEFAULT NULL,
  `iers_domes` varchar(9) DEFAULT NULL,
  `station_photo_URL` varchar(100) DEFAULT NULL,
  `time_series_image_URL` varchar(100) DEFAULT NULL,
  `agency_id` int(3) unsigned DEFAULT NULL,
  `networks` varchar(2000) DEFAULT NULL,
  `embargo_duration_hours` int(6) unsigned DEFAULT NULL,
  `embargo_after_date` datetime DEFAULT NULL,
  `nominal_sample_interval` float DEFAULT NULL,
  PRIMARY KEY (`station_id`)
) ENGINE=MyISAM AUTO_INCREMENT=46 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station`
--

LOCK TABLES `station` WRITE;
/*!40000 ALTER TABLE `station` DISABLE KEYS */;
/*!40000 ALTER TABLE `station` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `station_style`
--

DROP TABLE IF EXISTS `station_style`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `station_style` (
  `station_style_id` int(3) unsigned NOT NULL AUTO_INCREMENT,
  `station_style_name` varchar(20) NOT NULL,
  `station_style_test_int` int(3) DEFAULT NULL,
  PRIMARY KEY (`station_style_id`)
) ENGINE=MyISAM AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station_style`
--

LOCK TABLES `station_style` WRITE;
/*!40000 ALTER TABLE `station_style` DISABLE KEYS */;
INSERT INTO `station_style` VALUES (1,'GPS/GNSS Campaign',NULL),(2,'GPS/GNSS Continuous',NULL),(3,'GPS/GNSS Mobile',NULL),(4,'DORIS',NULL),(5,'Seismic',NULL),(6,'SLR',NULL),(7,'Strainmeter',NULL),(8,'Tiltmeter',NULL),(9,'VLBI',NULL),(10,'GPS/GNSS Episodic',NULL),(11,'tide gauge',NULL);
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

-- Dump completed on 2014-03-07 11:01:42
