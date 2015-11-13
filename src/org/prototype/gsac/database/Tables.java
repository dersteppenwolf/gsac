/** Tables.java makes classes which represent the tables and fields in your database.      It is created with GSAC code which reads your database and creates an all-new Tables.java file in your local GSAC code area.    Generated by running 'ant tables' which calls the GSAC code  GsacDatabaseManager:writeTables().    See the GSAC installation README (part 2) file in your local GSAC code area.    If you get an error building your local GSAC with 'ant' it may be caused by your Java code calling a for a TABLES variable (db field) which does not exist here or in the db.**/ 

package org.prototype.gsac.database;

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

        public static final String[] ARRAY = new String[] {
            COL_ACCESS_ID,COL_ACCESS_DESCRIPTION
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
        public static final String COL_OPERATING_AGENCY_NAME =  NAME + ".operating_agency_name";
        public static final String COL_OPERATING_AGENCY_ADDRESS =  NAME + ".operating_agency_address";
        public static final String COL_OPERATING_AGENCY_EMAIL =  NAME + ".operating_agency_email";
        public static final String COL_AGENCY_INDIVIDUAL_NAME =  NAME + ".agency_individual_name";
        public static final String COL_OTHER_CONTACT =  NAME + ".other_contact";

        public static final String[] ARRAY = new String[] {
            COL_AGENCY_ID,COL_OPERATING_AGENCY_NAME,COL_OPERATING_AGENCY_ADDRESS,COL_OPERATING_AGENCY_EMAIL,COL_AGENCY_INDIVIDUAL_NAME,COL_OTHER_CONTACT
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



    public static class DATA_REFERENCE_FRAME extends Tables {
        public static final String NAME = "data_reference_frame";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_DATA_REFERENCE_FRAME_ID =  NAME + ".data_reference_frame_id";
        public static final String COL_DATA_REFERENCE_FRAME_NAME =  NAME + ".data_reference_frame_name";

        public static final String[] ARRAY = new String[] {
            COL_DATA_REFERENCE_FRAME_ID,COL_DATA_REFERENCE_FRAME_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final DATA_REFERENCE_FRAME table  = new  DATA_REFERENCE_FRAME();
    }



    public static class DATA_TYPE extends Tables {
        public static final String NAME = "data_type";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_DATA_TYPE_ID =  NAME + ".data_type_id";
        public static final String COL_DATA_TYPE_NAME =  NAME + ".data_type_name";
        public static final String COL_DATA_TYPE_DESCRIPTION =  NAME + ".data_type_description";

        public static final String[] ARRAY = new String[] {
            COL_DATA_TYPE_ID,COL_DATA_TYPE_NAME,COL_DATA_TYPE_DESCRIPTION
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final DATA_TYPE table  = new  DATA_TYPE();
    }



    public static class DATAFILE extends Tables {
        public static final String NAME = "datafile";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_DATAFILE_ID =  NAME + ".datafile_id";
        public static final String COL_STATION_ID =  NAME + ".station_id";
        public static final String COL_EQUIP_CONFIG_ID =  NAME + ".equip_config_id";
        public static final String COL_DATAFILE_NAME =  NAME + ".datafile_name";
        public static final String COL_URL_COMPLETE =  NAME + ".URL_complete";
        public static final String COL_URL_PROTOCOL =  NAME + ".URL_protocol";
        public static final String COL_URL_DOMAIN =  NAME + ".URL_domain";
        public static final String COL_URL_PATH_DIRS =  NAME + ".URL_path_dirs";
        public static final String COL_DATA_TYPE_ID =  NAME + ".data_type_id";
        public static final String COL_DATAFILE_FORMAT_ID =  NAME + ".datafile_format_id";
        public static final String COL_DATA_REFERENCE_FRAME_ID =  NAME + ".data_reference_frame_id";
        public static final String COL_DATAFILE_START_TIME =  NAME + ".datafile_start_time";
        public static final String COL_DATAFILE_STOP_TIME =  NAME + ".datafile_stop_time";
        public static final String COL_DATAFILE_PUBLISHED_DATE =  NAME + ".datafile_published_date";
        public static final String COL_SAMPLE_INTERVAL =  NAME + ".sample_interval";
        public static final String COL_LATENCY_ESTIMATE =  NAME + ".latency_estimate";
        public static final String COL_YEAR =  NAME + ".year";
        public static final String COL_DAY_OF_YEAR =  NAME + ".day_of_year";
        public static final String COL_SIZE_BYTES =  NAME + ".size_bytes";
        public static final String COL_MD5 =  NAME + ".MD5";
        public static final String COL_ORIGINATING_AGENCY_URL =  NAME + ".originating_agency_URL";

        public static final String[] ARRAY = new String[] {
            COL_DATAFILE_ID,COL_STATION_ID,COL_EQUIP_CONFIG_ID,COL_DATAFILE_NAME,COL_URL_COMPLETE,COL_URL_PROTOCOL,COL_URL_DOMAIN,COL_URL_PATH_DIRS,COL_DATA_TYPE_ID,COL_DATAFILE_FORMAT_ID,COL_DATA_REFERENCE_FRAME_ID,COL_DATAFILE_START_TIME,COL_DATAFILE_STOP_TIME,COL_DATAFILE_PUBLISHED_DATE,COL_SAMPLE_INTERVAL,COL_LATENCY_ESTIMATE,COL_YEAR,COL_DAY_OF_YEAR,COL_SIZE_BYTES,COL_MD5,COL_ORIGINATING_AGENCY_URL
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final DATAFILE table  = new  DATAFILE();
    }



    public static class DATAFILE_FORMAT extends Tables {
        public static final String NAME = "datafile_format";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_DATAFILE_FORMAT_ID =  NAME + ".datafile_format_id";
        public static final String COL_DATAFILE_FORMAT_NAME =  NAME + ".datafile_format_name";

        public static final String[] ARRAY = new String[] {
            COL_DATAFILE_FORMAT_ID,COL_DATAFILE_FORMAT_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final DATAFILE_FORMAT table  = new  DATAFILE_FORMAT();
    }



    public static class EQUIP_CONFIG extends Tables {
        public static final String NAME = "equip_config";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_EQUIP_CONFIG_ID =  NAME + ".equip_config_id";
        public static final String COL_STATION_ID =  NAME + ".station_id";
        public static final String COL_EQUIP_CONFIG_START_TIME =  NAME + ".equip_config_start_time";
        public static final String COL_EQUIP_CONFIG_STOP_TIME =  NAME + ".equip_config_stop_time";
        public static final String COL_DATA_LATENCY_HOURS =  NAME + ".data_latency_hours";
        public static final String COL_DATA_LATENCY_DAYS =  NAME + ".data_latency_days";
        public static final String COL_DATA_COMPLETENESS =  NAME + ".data_completeness";
        public static final String COL_DB_UPDATE_TIME =  NAME + ".db_update_time";
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
            COL_EQUIP_CONFIG_ID,COL_STATION_ID,COL_EQUIP_CONFIG_START_TIME,COL_EQUIP_CONFIG_STOP_TIME,COL_DATA_LATENCY_HOURS,COL_DATA_LATENCY_DAYS,COL_DATA_COMPLETENESS,COL_DB_UPDATE_TIME,COL_ANTENNA_ID,COL_ANTENNA_SERIAL_NUMBER,COL_ANTENNA_HEIGHT,COL_METPACK_ID,COL_METPACK_SERIAL_NUMBER,COL_RADOME_ID,COL_RADOME_SERIAL_NUMBER,COL_RECEIVER_FIRMWARE_ID,COL_RECEIVER_SERIAL_NUMBER,COL_SATELLITE_SYSTEM,COL_SAMPLE_INTERVAL
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
        public static final String COL_LOCALE_NAME =  NAME + ".locale_name";

        public static final String[] ARRAY = new String[] {
            COL_LOCALE_ID,COL_LOCALE_NAME
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



    public static class NATION extends Tables {
        public static final String NAME = "nation";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_NATION_ID =  NAME + ".nation_id";
        public static final String COL_NATION_NAME =  NAME + ".nation_name";

        public static final String[] ARRAY = new String[] {
            COL_NATION_ID,COL_NATION_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final NATION table  = new  NATION();
    }



    public static class PROVINCE_STATE extends Tables {
        public static final String NAME = "province_state";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_PROVINCE_STATE_ID =  NAME + ".province_state_id";
        public static final String COL_PROVINCE_STATE_NAME =  NAME + ".province_state_name";

        public static final String[] ARRAY = new String[] {
            COL_PROVINCE_STATE_ID,COL_PROVINCE_STATE_NAME
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final PROVINCE_STATE table  = new  PROVINCE_STATE();
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
        public static final String COL_HEIGHT_ELLIPSOID =  NAME + ".height_ellipsoid";
        public static final String COL_X =  NAME + ".X";
        public static final String COL_Y =  NAME + ".Y";
        public static final String COL_Z =  NAME + ".Z";
        public static final String COL_INSTALLED_DATE =  NAME + ".installed_date";
        public static final String COL_PUBLISHED_DATE =  NAME + ".published_date";
        public static final String COL_RETIRED_DATE =  NAME + ".retired_date";
        public static final String COL_AGENCY_ID =  NAME + ".agency_id";
        public static final String COL_ACCESS_ID =  NAME + ".access_id";
        public static final String COL_STYLE_ID =  NAME + ".style_id";
        public static final String COL_STATUS_ID =  NAME + ".status_id";
        public static final String COL_MONUMENT_STYLE_ID =  NAME + ".monument_style_id";
        public static final String COL_NATION_ID =  NAME + ".nation_id";
        public static final String COL_PROVINCE_STATE_ID =  NAME + ".province_state_id";
        public static final String COL_LOCALE_ID =  NAME + ".locale_id";
        public static final String COL_NETWORKS =  NAME + ".networks";
        public static final String COL_ORIGINATING_AGENCY_URL =  NAME + ".originating_agency_URL";
        public static final String COL_IERS_DOMES =  NAME + ".iers_domes";
        public static final String COL_STATION_PHOTO_URL =  NAME + ".station_photo_URL";
        public static final String COL_TIME_SERIES_PLOT_IMAGE_URL =  NAME + ".time_series_plot_image_URL";
        public static final String COL_EMBARGO_DURATION_HOURS =  NAME + ".embargo_duration_hours";
        public static final String COL_EMBARGO_AFTER_DATE =  NAME + ".embargo_after_date";

        public static final String[] ARRAY = new String[] {
            COL_STATION_ID,COL_FOUR_CHAR_NAME,COL_STATION_NAME,COL_LATITUDE_NORTH,COL_LONGITUDE_EAST,COL_HEIGHT_ELLIPSOID,COL_X,COL_Y,COL_Z,COL_INSTALLED_DATE,COL_PUBLISHED_DATE,COL_RETIRED_DATE,COL_AGENCY_ID,COL_ACCESS_ID,COL_STYLE_ID,COL_STATUS_ID,COL_MONUMENT_STYLE_ID,COL_NATION_ID,COL_PROVINCE_STATE_ID,COL_LOCALE_ID,COL_NETWORKS,COL_ORIGINATING_AGENCY_URL,COL_IERS_DOMES,COL_STATION_PHOTO_URL,COL_TIME_SERIES_PLOT_IMAGE_URL,COL_EMBARGO_DURATION_HOURS,COL_EMBARGO_AFTER_DATE
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
