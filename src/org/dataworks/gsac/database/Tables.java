/** Tables.java makes classes which represent the tables and fields in your database.      It is created with GSAC code which reads your database and creates an all-new Tables.java file in your local GSAC code area.    Generated by running: java GsacDatabaseManager:writeTables(...)    See the GSAC installation README (part 2) file in your local GSAC code area.**/ 

package org.dataworks.gsac.database;

import org.gsac.gsl.ramadda.sql.SqlUtil;

public abstract class Tables {
    public abstract String getName();
    public abstract String getColumns();


    public static class ACCESS extends Tables {
        public static final String NAME = "access";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ACCESS_ID =  NAME + ".access_id";
        public static final String COL_ACCESS_DESCRIPTION =  NAME + ".access_description";
        public static final String COL_EMBARGO_DURATION_DAYS =  NAME + ".embargo_duration_days";

        public static final String[] ARRAY = new String[] {
            COL_ACCESS_ID,COL_ACCESS_DESCRIPTION,COL_EMBARGO_DURATION_DAYS
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final ACCESS table  = new  ACCESS();
    }



    public static class AGENCY extends Tables {
        public static final String NAME = "agency";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_AGENCY_ID =  NAME + ".agency_id";
        public static final String COL_AGENCY_NAME =  NAME + ".agency_name";
        public static final String COL_AGENCY_SHORT_NAME =  NAME + ".agency_short_name";

        public static final String[] ARRAY = new String[] {
            COL_AGENCY_ID,COL_AGENCY_NAME,COL_AGENCY_SHORT_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final AGENCY table  = new  AGENCY();
    }



    public static class ANTENNA extends Tables {
        public static final String NAME = "antenna";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ANTENNA_ID =  NAME + ".antenna_id";
        public static final String COL_ANTENNA_NAME =  NAME + ".antenna_name";
        public static final String COL_IGS_DEFINED =  NAME + ".igs_defined";

        public static final String[] ARRAY = new String[] {
            COL_ANTENNA_ID,COL_ANTENNA_NAME,COL_IGS_DEFINED
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final ANTENNA table  = new  ANTENNA();
    }



    public static class COUNTRY extends Tables {
        public static final String NAME = "country";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_COUNTRY_ID =  NAME + ".country_id";
        public static final String COL_COUNTRY_NAME =  NAME + ".country_name";

        public static final String[] ARRAY = new String[] {
            COL_COUNTRY_ID,COL_COUNTRY_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final COUNTRY table  = new  COUNTRY();
    }



    public static class DATAFILE extends Tables {
        public static final String NAME = "datafile";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_DATAFILE_ID =  NAME + ".datafile_id";
        public static final String COL_STATION_ID =  NAME + ".station_id";
        public static final String COL_EQUIP_CONFIG_ID =  NAME + ".equip_config_id";
        public static final String COL_DATAFILE_NAME =  NAME + ".datafile_name";
        public static final String COL_ORIGINAL_DATAFILE_NAME =  NAME + ".original_datafile_name";
        public static final String COL_DATAFILE_TYPE_ID =  NAME + ".datafile_type_id";
        public static final String COL_SAMPLE_INTERVAL =  NAME + ".sample_interval";
        public static final String COL_DATAFILE_START_TIME =  NAME + ".datafile_start_time";
        public static final String COL_DATAFILE_STOP_TIME =  NAME + ".datafile_stop_time";
        public static final String COL_YEAR =  NAME + ".year";
        public static final String COL_DAY_OF_YEAR =  NAME + ".day_of_year";
        public static final String COL_PUBLISHED_TIME =  NAME + ".published_time";
        public static final String COL_SIZE_BYTES =  NAME + ".size_bytes";
        public static final String COL_MD5 =  NAME + ".MD5";
        public static final String COL_URL_PATH =  NAME + ".URL_path";

        public static final String[] ARRAY = new String[] {
            COL_DATAFILE_ID,COL_STATION_ID,COL_EQUIP_CONFIG_ID,COL_DATAFILE_NAME,COL_ORIGINAL_DATAFILE_NAME,COL_DATAFILE_TYPE_ID,COL_SAMPLE_INTERVAL,COL_DATAFILE_START_TIME,COL_DATAFILE_STOP_TIME,COL_YEAR,COL_DAY_OF_YEAR,COL_PUBLISHED_TIME,COL_SIZE_BYTES,COL_MD5,COL_URL_PATH
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final DATAFILE table  = new  DATAFILE();
    }



    public static class DATAFILE_TYPE extends Tables {
        public static final String NAME = "datafile_type";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_DATAFILE_TYPE_ID =  NAME + ".datafile_type_id";
        public static final String COL_DATAFILE_TYPE_NAME =  NAME + ".datafile_type_name";
        public static final String COL_DATAFILE_TYPE_VERSION =  NAME + ".datafile_type_version";
        public static final String COL_DATAFILE_TYPE_DESCRIPTION =  NAME + ".datafile_type_description";

        public static final String[] ARRAY = new String[] {
            COL_DATAFILE_TYPE_ID,COL_DATAFILE_TYPE_NAME,COL_DATAFILE_TYPE_VERSION,COL_DATAFILE_TYPE_DESCRIPTION
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final DATAFILE_TYPE table  = new  DATAFILE_TYPE();
    }



    public static class ELLIPSOID extends Tables {
        public static final String NAME = "ellipsoid";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ELLIPSOID_ID =  NAME + ".ellipsoid_id";
        public static final String COL_ELLIPSOID_NAME =  NAME + ".ellipsoid_name";
        public static final String COL_ELLIPSOID_SHORT_NAME =  NAME + ".ellipsoid_short_name";

        public static final String[] ARRAY = new String[] {
            COL_ELLIPSOID_ID,COL_ELLIPSOID_NAME,COL_ELLIPSOID_SHORT_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final ELLIPSOID table  = new  ELLIPSOID();
    }



    public static class EQUIP_CONFIG extends Tables {
        public static final String NAME = "equip_config";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_EQUIP_CONFIG_ID =  NAME + ".equip_config_id";
        public static final String COL_STATION_ID =  NAME + ".station_id";
        public static final String COL_CREATE_TIME =  NAME + ".create_time";
        public static final String COL_EQUIP_CONFIG_START_TIME =  NAME + ".equip_config_start_time";
        public static final String COL_EQUIP_CONFIG_STOP_TIME =  NAME + ".equip_config_stop_time";
        public static final String COL_ANTENNA_ID =  NAME + ".antenna_id";
        public static final String COL_ANTENNA_SERIAL_NUMBER =  NAME + ".antenna_serial_number";
        public static final String COL_ANTENNA_HEIGHT =  NAME + ".antenna_height";
        public static final String COL_METPACK_ID =  NAME + ".metpack_id";
        public static final String COL_METPACK_SERIAL_NUMBER =  NAME + ".metpack_serial_number";
        public static final String COL_RADOME_ID =  NAME + ".radome_id";
        public static final String COL_RADOME_SERIAL_NUMBER =  NAME + ".radome_serial_number";
        public static final String COL_RECEIVER_FIRMWARE_ID =  NAME + ".receiver_firmware_id";
        public static final String COL_RECEIVER_SERIAL_NUMBER =  NAME + ".receiver_serial_number";
        public static final String COL_SATELLITE_SYSTEM =  NAME + ".satellite_system";
        public static final String COL_SAMPLE_INTERVAL =  NAME + ".sample_interval";

        public static final String[] ARRAY = new String[] {
            COL_EQUIP_CONFIG_ID,COL_STATION_ID,COL_CREATE_TIME,COL_EQUIP_CONFIG_START_TIME,COL_EQUIP_CONFIG_STOP_TIME,COL_ANTENNA_ID,COL_ANTENNA_SERIAL_NUMBER,COL_ANTENNA_HEIGHT,COL_METPACK_ID,COL_METPACK_SERIAL_NUMBER,COL_RADOME_ID,COL_RADOME_SERIAL_NUMBER,COL_RECEIVER_FIRMWARE_ID,COL_RECEIVER_SERIAL_NUMBER,COL_SATELLITE_SYSTEM,COL_SAMPLE_INTERVAL
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final EQUIP_CONFIG table  = new  EQUIP_CONFIG();
    }



    public static class LOCALE extends Tables {
        public static final String NAME = "locale";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_LOCALE_ID =  NAME + ".locale_id";
        public static final String COL_LOCALE_INFO =  NAME + ".locale_info";

        public static final String[] ARRAY = new String[] {
            COL_LOCALE_ID,COL_LOCALE_INFO
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final LOCALE table  = new  LOCALE();
    }



    public static class METPACK extends Tables {
        public static final String NAME = "metpack";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_METPACK_ID =  NAME + ".metpack_id";
        public static final String COL_METPACK_NAME =  NAME + ".metpack_name";

        public static final String[] ARRAY = new String[] {
            COL_METPACK_ID,COL_METPACK_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final METPACK table  = new  METPACK();
    }



    public static class MONUMENT_STYLE extends Tables {
        public static final String NAME = "monument_style";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_MONUMENT_STYLE_ID =  NAME + ".monument_style_id";
        public static final String COL_MONUMENT_STYLE_DESCRIPTION =  NAME + ".monument_style_description";

        public static final String[] ARRAY = new String[] {
            COL_MONUMENT_STYLE_ID,COL_MONUMENT_STYLE_DESCRIPTION
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final MONUMENT_STYLE table  = new  MONUMENT_STYLE();
    }



    public static class NETWORK extends Tables {
        public static final String NAME = "network";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_NETWORK_ID =  NAME + ".network_id";
        public static final String COL_NETWORK_NAME =  NAME + ".network_name";

        public static final String[] ARRAY = new String[] {
            COL_NETWORK_ID,COL_NETWORK_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final NETWORK table  = new  NETWORK();
    }



    public static class RADOME extends Tables {
        public static final String NAME = "radome";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_RADOME_ID =  NAME + ".radome_id";
        public static final String COL_RADOME_NAME =  NAME + ".radome_name";
        public static final String COL_IGS_DEFINED =  NAME + ".igs_defined";

        public static final String[] ARRAY = new String[] {
            COL_RADOME_ID,COL_RADOME_NAME,COL_IGS_DEFINED
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final RADOME table  = new  RADOME();
    }



    public static class RECEIVER_FIRMWARE extends Tables {
        public static final String NAME = "receiver_firmware";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_RECEIVER_FIRMWARE_ID =  NAME + ".receiver_firmware_id";
        public static final String COL_RECEIVER_NAME =  NAME + ".receiver_name";
        public static final String COL_RECEIVER_FIRMWARE =  NAME + ".receiver_firmware";
        public static final String COL_IGS_DEFINED =  NAME + ".igs_defined";

        public static final String[] ARRAY = new String[] {
            COL_RECEIVER_FIRMWARE_ID,COL_RECEIVER_NAME,COL_RECEIVER_FIRMWARE,COL_IGS_DEFINED
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final RECEIVER_FIRMWARE table  = new  RECEIVER_FIRMWARE();
    }



    public static class STATION extends Tables {
        public static final String NAME = "station";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_STATION_ID =  NAME + ".station_id";
        public static final String COL_FOUR_CHAR_NAME =  NAME + ".four_char_name";
        public static final String COL_STATION_NAME =  NAME + ".station_name";
        public static final String COL_LATITUDE_NORTH =  NAME + ".latitude_north";
        public static final String COL_LONGITUDE_EAST =  NAME + ".longitude_east";
        public static final String COL_HEIGHT_ABOVE_ELLIPSOID =  NAME + ".height_above_ellipsoid";
        public static final String COL_INSTALLED_DATE =  NAME + ".installed_date";
        public static final String COL_LATEST_DATA_TIME =  NAME + ".latest_data_time";
        public static final String COL_RETIRED_DATE =  NAME + ".retired_date";
        public static final String COL_STYLE_ID =  NAME + ".style_id";
        public static final String COL_STATUS_ID =  NAME + ".status_id";
        public static final String COL_ACCESS_ID =  NAME + ".access_id";
        public static final String COL_MONUMENT_STYLE_ID =  NAME + ".monument_style_id";
        public static final String COL_COUNTRY_ID =  NAME + ".country_id";
        public static final String COL_LOCALE_ID =  NAME + ".locale_id";
        public static final String COL_ELLIPSOID_ID =  NAME + ".ellipsoid_id";
        public static final String COL_IERS_DOMES =  NAME + ".iers_domes";
        public static final String COL_OPERATOR_AGENCY_ID =  NAME + ".operator_agency_id";
        public static final String COL_DATA_PUBLISHER_AGENCY_ID =  NAME + ".data_publisher_agency_id";
        public static final String COL_NETWORK_ID =  NAME + ".network_id";
        public static final String COL_STATION_IMAGE_URL =  NAME + ".station_image_URL";
        public static final String COL_TIME_SERIES_URL =  NAME + ".time_series_URL";

        public static final String[] ARRAY = new String[] {
            COL_STATION_ID,COL_FOUR_CHAR_NAME,COL_STATION_NAME,COL_LATITUDE_NORTH,COL_LONGITUDE_EAST,COL_HEIGHT_ABOVE_ELLIPSOID,COL_INSTALLED_DATE,COL_LATEST_DATA_TIME,COL_RETIRED_DATE,COL_STYLE_ID,COL_STATUS_ID,COL_ACCESS_ID,COL_MONUMENT_STYLE_ID,COL_COUNTRY_ID,COL_LOCALE_ID,COL_ELLIPSOID_ID,COL_IERS_DOMES,COL_OPERATOR_AGENCY_ID,COL_DATA_PUBLISHER_AGENCY_ID,COL_NETWORK_ID,COL_STATION_IMAGE_URL,COL_TIME_SERIES_URL
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final STATION table  = new  STATION();
    }



    public static class STATION_STATUS extends Tables {
        public static final String NAME = "station_status";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_STATION_STATUS_ID =  NAME + ".station_status_id";
        public static final String COL_STATION_STATUS =  NAME + ".station_status";

        public static final String[] ARRAY = new String[] {
            COL_STATION_STATUS_ID,COL_STATION_STATUS
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final STATION_STATUS table  = new  STATION_STATUS();
    }



    public static class STATION_STYLE extends Tables {
        public static final String NAME = "station_style";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_STATION_STYLE_ID =  NAME + ".station_style_id";
        public static final String COL_STATION_STYLE_DESCRIPTION =  NAME + ".station_style_description";

        public static final String[] ARRAY = new String[] {
            COL_STATION_STYLE_ID,COL_STATION_STYLE_DESCRIPTION
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final STATION_STYLE table  = new  STATION_STYLE();
    }



}
