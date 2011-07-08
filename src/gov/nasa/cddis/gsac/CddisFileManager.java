/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package gov.nasa.cddis.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;


import ucar.unidata.util.DateUtil;
import ucar.unidata.util.StringUtil;

import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;



/**
 * Handles all of the resource related repository requests
 *
 *
 * @author  Jeff McWhirter
 */
public class CddisFileManager extends FileManager implements CddisArgs {

    /** _more_ */
    public static final String TYPE_GNSS_HATANAKA = "gnss.hatanaka";

    /** _more_ */
    public static final String TYPE_GNSS_OBSERVATION = "gnss.observation";

    /** _more_ */
    public static final String TYPE_GNSS_SUMMARY = "gnss.summary";


    /** _more_ */
    private static String[] GNSS_FILE_INFO = { "d", "o", "s" };

    /** _more_ */
    private static ResourceType[] GNSS_FILE_TYPES = { new ResourceType(
                                                        TYPE_GNSS_HATANAKA,
                                                        "GNSS - Hatanaka"),
            new ResourceType(TYPE_GNSS_OBSERVATION, "GNSS - Observation"),
            new ResourceType(TYPE_GNSS_SUMMARY, "GNSS - Summary") };


    /** _more_ */
    public static final String[] GNSS_METADATA_COLUMNS = new String[] {
        Tables.GPS_TRACKING2009.COL_OBSERVATION_INTERVAL,
        Tables.GPS_TRACKING2009.COL_NO_OBSERVATIONS,
        Tables.GPS_TRACKING2009.COL_OBSERVATION_TYPES,
        Tables.GPS_TRACKING2009.COL_RECEIVER_NUMBER,
        Tables.GPS_TRACKING2009.COL_RECEIVER_TYPE,
        Tables.GPS_TRACKING2009.COL_RECEIVER_VERSION,
        Tables.GPS_TRACKING2009.COL_ANTENNA_NUMBER,
        Tables.GPS_TRACKING2009.COL_ANTENNA_TYPE,
        Tables.GPS_TRACKING2009.COL_X_POSITION,
        Tables.GPS_TRACKING2009.COL_Y_POSITION,
        Tables.GPS_TRACKING2009.COL_Z_POSITION,
        Tables.GPS_TRACKING2009.COL_ANTENNA_HEIGHT,
        Tables.GPS_TRACKING2009.COL_ANTENNA_EAST,
        Tables.GPS_TRACKING2009.COL_ANTENNA_NORTH,
        Tables.GPS_TRACKING2009.COL_OBSERVER,
        Tables.GPS_TRACKING2009.COL_AGENCY,
        Tables.GPS_TRACKING2009.COL_PROGRAM,
        Tables.GPS_TRACKING2009.COL_PROGRAM_OPERATOR,
        Tables.GPS_TRACKING2009.COL_MARKER_NAME,
        Tables.GPS_TRACKING2009.COL_DOMES_NUMBER,
        Tables.GPS_TRACKING2009.COL_FILE_NAME,
        Tables.GPS_TRACKING2009.COL_GNSS_TYPE,
        Tables.GPS_TRACKING2009.COL_HOUR_DELAY,
        Tables.GPS_TRACKING2009.COL_NO_OBSEXP,
        Tables.GPS_TRACKING2009.COL_NO_OBS,
        Tables.GPS_TRACKING2009.COL_NO_OBSDEL,
        Tables.GPS_TRACKING2009.COL_DATA_PCT,
        Tables.GPS_TRACKING2009.COL_AVG_MP1,
        Tables.GPS_TRACKING2009.COL_AVG_MP2,
        Tables.GPS_TRACKING2009.COL_POSITION_DIFFERENCE,
        Tables.GPS_TRACKING2009.COL_NO_SLIPS,
        Tables.GPS_TRACKING2009.COL_DATA_VERSION,
    };


    /** _more_ */
    public static final String TYPE_DORIS_RINEX = "doris.rinex";

    /** _more_ */
    public static final String TYPE_DORIS_DORIS = "doris.doris";

    /** _more_ */
    private static String[] DORIS_FILE_INFO = { "d", "o", "s" };

    /** _more_ */
    private static ResourceType[] DORIS_FILE_TYPES = {
        //        new ResourceType(TYPE_DORIS_RINEX,"DORIS - Rinex"),
        new ResourceType(TYPE_DORIS_DORIS, "DORIS")
    };

    /** _more_ */
    public static final String[] DORIS_METADATA_COLUMNS = new String[] {
        //        Tables.DORIS_2009.COL_NUM_OBS,
        //        Tables.DORIS_2009.COL_SATELLITE
    };

    /** _more_ */
    public static final String DORIS_COLUMNS =
        SqlUtil.comma(new String[] {
            SqlUtil.min(Tables.DORIS_2009.COL_S_DATE),
            SqlUtil.max(Tables.DORIS_2009.COL_E_DATE),
            Tables.DORIS_2009.COL_SATELLITE,
            Tables.DORIS_2009.COL_FILE_NAME });

    /** _more_ */
    public static final String DORIS_GROUP_BY =
        SqlUtil.comma(Tables.DORIS_2009.COL_SATELLITE,
                      Tables.DORIS_2009.COL_FILE_NAME);




    /** _more_ */
    public static final String[][] DORIS_SATELLITE_FILES = {
        { "en1", "ENVISAT" }, { "ja1", "JASON" }, { "ja2", "JASON-2" },
        { "sp2", "SPOT-2" }, { "sp3", "SPOT-4" }, { "sp4", "SPOT-5" },
    };

    /** _more_ */
    public static final String[] DORIS_SATELLITES = {
        "ENVISAT", "JASON", "JASON-2", "SPOT-2", "SPOT-4", "SPOT-5"
    };

    /** _more_ */
    public static final Hashtable<String, String> DORIS_SATELLITE_MAP =
        new Hashtable<String, String>();


    /** _more_ */
    public static final String TYPE_SLR_SLR = "slr.slr";


    /** _more_ */
    private static ResourceType[] SLR_FILE_TYPES = { new ResourceType(
                                                       TYPE_SLR_SLR,
                                                       "SLR"), };

    /** _more_ */
    public static final String[] SLR_METADATA_COLUMNS = new String[] {
        //        Tables.DORIS_2009.COL_NUM_OBS,
        //        Tables.DORIS_2009.COL_SATELLITE
    };

    /** _more_ */
    public static final String SLR_COLUMNS = SqlUtil.comma(new String[] {
        SqlUtil.min(Tables.SATELLITESQL_2009.COL_S_DATE),
        SqlUtil.max(Tables.SATELLITESQL_2009.COL_E_DATE),
        Tables.SATELLITESQL_2009.COL_SATELLITE
        /*,Tables.SATELLITESQL_2009.COL_FILE_NAME*/
    });

    /** _more_ */
    public static final String SLR_GROUP_BY =
        Tables.SATELLITESQL_2009.COL_SATELLITE;






    /** _more_ */
    private static ResourceType[] VLBI_FILE_TYPES = {
        //        new ResourceType(TYPE_VLBI_RINEX,"VLBI - Rinex"),
    };

    /** _more_ */
    private static ResourceType[][] ALL_FILE_TYPES = {
        GNSS_FILE_TYPES, DORIS_FILE_TYPES, SLR_FILE_TYPES, VLBI_FILE_TYPES
    };


    /** _more_ */
    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

    /** _more_ */
    private SimpleDateFormat sdf;

    /** _more_ */
    private SimpleDateFormat yyyyMMDDSdf;

    /**
     * ctor
     *
     * @param repository the repository
     */
    public CddisFileManager(CddisRepository repository) {
        super(repository);

        for (String[] pair : DORIS_SATELLITE_FILES) {
            DORIS_SATELLITE_MAP.put(pair[1], pair[0]);
        }
        sdf         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        yyyyMMDDSdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TIMEZONE_UTC);
        yyyyMMDDSdf.setTimeZone(TIMEZONE_UTC);
    }




    /**
     * CHANGEME
     * handle the request
     *
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badness
     */
    public void handleRequest(GsacRequest request,
                              GsacResponse response)
            throws Exception {


        StringBuffer    msgBuff       = new StringBuffer();
        int             cnt           = 0;
        int             totalCnt      = 0;
        int             offset        = request.getOffset();
        int             limit         = request.getLimit();
        boolean         exceededLimit = false;

        long            t1            = System.currentTimeMillis();
        HashSet<String> resourceTypes = null;

        if (request.defined(ARG_FILE_TYPE)) {
            resourceTypes = new HashSet<String>();
            for (String t :
                    request.getDelimiterSeparatedList(ARG_FILE_TYPE)) {
                resourceTypes.add(t);
            }
        }
        Date[] dateRange = request.getDateRange(ARG_FILE_DATADATE_FROM,
                               ARG_FILE_DATADATE_TO, null, null);
        //        System.err.println ("date:" + dateRange[0] +" " + dateRange[1]);
        boolean[]       addedAnySpecificTypeClauses = { false };


        List<CddisType> cddisTypes = CddisType.getSiteTypes(request);


        //Narrow the types down if we are searching for some type specific metadata
        if (request.defined(ARG_DORIS_SATELLITE)) {
            if (cddisTypes.size() != 4) {
                throw new IllegalArgumentException(
                    "Trying to search for DORIS satellite and other non-DORIS metadata as well");
            }
            cddisTypes = new ArrayList<CddisType>();
            cddisTypes.add(CddisType.TYPE_DORIS);
        }

        if (request.defined(ARG_SLR_SATELLITE)) {
            if (cddisTypes.size() != 4) {
                throw new IllegalArgumentException(
                    "Trying to search for SLR satellite and other non-SLR metadata as well");
            }
            cddisTypes = new ArrayList<CddisType>();
            cddisTypes.add(CddisType.TYPE_SLR);
        }


        for (CddisType type : cddisTypes) {
            //Some aren't implemented.
            if (type.getResourceTable() == null) {
                continue;
            }
            if (exceededLimit) {
                break;
            }
            String       resourceColumns = type.getResourceColumns();
            String       suffix          = "";
            List<Clause> clauses         = new ArrayList<Clause>();

            List<Clause> siteClauses =
                getSiteManager().getSiteClauses(request, response, type,
                    addedAnySpecificTypeClauses, msgBuff);

            if (type.isDoris()) {
                resourceColumns = DORIS_COLUMNS;
                suffix          = SqlUtil.groupBy(DORIS_GROUP_BY);
                if (request.defined(ARG_DORIS_SATELLITE)) {
                    addEnumeratedSearch(request, ARG_DORIS_SATELLITE,
                                        Tables.DORIS_2009.COL_SATELLITE,
                                        "DORIS Satellite", msgBuff, clauses);
                }
            } else if (type.isSlr()) {
                resourceColumns = SLR_COLUMNS;
                suffix          = SqlUtil.groupBy(SLR_GROUP_BY);
                if (request.defined(ARG_SLR_SATELLITE)) {
                    addEnumeratedSearch(
                        request, ARG_SLR_SATELLITE,
                        Tables.SATELLITESQL_2009.COL_SATELLITE,
                        "SLR Satellite", msgBuff, clauses);
                }
            }

            //Only the gnss does the site based file search
            if (siteClauses.size() > 0) {
                Clause siteClause = Clause.and(siteClauses);
                if (type.isGnss()) {
                    clauses.add(Clause
                        .in(Tables.GPS_TRACKING2009.COL_MONUMENT_NAME,
                            " distinct "
                            + Tables.SITE_INFO_GNSS.COL_MONUMENT_NAME, Tables
                                .SITE_INFO_GNSS.NAME, siteClause));
                } else if (type.isDoris()) {}
                else if (type.isSlr()) {}
                else if (type.isVlbi()) {}
            }

            addDateClause(type, clauses, dateRange, msgBuff);

            List<String> tableNames = new ArrayList<String>();
            tableNames.add(type.getResourceTable());
            Clause mainClause = Clause.and(clauses);
            //            SqlUtil.debug = true;
            //            System.err.println("clauses:" + clauses);

            tableNames    = mainClause.getTableNames(tableNames);
            Statement statement =
                getDatabaseManager().select(resourceColumns, tableNames,
                                            mainClause, suffix, -1);
            SqlUtil.Iterator iter = SqlUtil.getIterator(statement);
            //TODO: for efficieny we probably want to take into account
            //when we are skipping to the offset so we don't have to read
            //and create all of the extra resources
            Object[]  extraStuff = new Object[] { null };
            ResultSet results    = null;
            System.err.println("Looping");
            //            while ((results = iter.getNext()) != null) {
            //                System.err.println ("   Loop");
            //            }


            while ((results = iter.getNext()) != null && !exceededLimit) {
                for (GsacFile resource :
                        makeResources(type, results, resourceTypes,
                                      dateRange, extraStuff)) {
                    if (totalCnt >= offset) {
                        response.addObject(resource);
                        cnt++;
                    }
                    totalCnt++;
                }
                if (cnt >= limit) {
                    response.setExceededLimit();
                    exceededLimit = true;
                }
            }
            iter.close();
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        long t2 = System.currentTimeMillis();
        System.err.println("read " + cnt + " resources in " + (t2 - t1)
                           + "ms");
        setSearchCriteriaMessage(response, msgBuff);
    }


    /**
     * CHANGEME
     * Create a resource from the given results
     *
     *
     * @param type _more_
     * @param results result set
     * @param resourceTypes _more_
     * @param dateRange _more_
     * @param extraStuff _more_
     *
     * @return The resource
     *
     * @throws Exception On badness
     */
    public List<GsacFile> makeResources(CddisType type,
                                            ResultSet results,
                                            HashSet<String> resourceTypes,
                                            Date[] dateRange,
                                            Object[] extraStuff)
            throws Exception {
        if (type.isGnss()) {
            return makeGnssResources(type, results, resourceTypes, false);
        } else if (type.isDoris()) {
            return makeDorisResources(type, results, resourceTypes, false);
        } else if (type.isSlr()) {
            return makeSlrResources(type, results, resourceTypes, false,
                                    dateRange, extraStuff);
        } else if (type.isVlbi()) {
            return makeVlbiResources(type, results, resourceTypes, false);
        }
        throw new IllegalArgumentException("Unknown type:" + type.getType());
    }

    /**
     * _more_
     *
     * @param results _more_
     * @param dateColumn _more_
     * @param timeColumn _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Date getDate(ResultSet results, String dateColumn,
                        String timeColumn)
            throws Exception {

        String dateString = results.getString(unDot(dateColumn));
        String timeString = results.getString(unDot(timeColumn));
        if (dateString == null) {
            return null;
        }
        if (timeString == null) {
            return results.getDate(unDot(dateColumn));
        }
        timeString = timeString.trim();
        while(timeString.indexOf(" ")>=0) {
            System.err.println ("CddisFileManager: fixing time string:" + timeString);
            timeString = timeString.replace(" ","0");
        }
        timeString = StringUtil.splitUpTo(timeString, ".", 2).get(0);
        return sdf.parse(dateString + " " + timeString);
    }


    /**
     * _more_
     *
     *
     * @param type _more_
     * @param results _more_
     * @param resourceTypes _more_
     * @param addMetadata _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<GsacFile> makeGnssResources(CddisType type,
            ResultSet results, HashSet<String> resourceTypes,
            boolean addMetadata)
            throws Exception {
        List<GsacFile> resources = new ArrayList<GsacFile>();
        String monumentID =
            results.getString(
                unDot(Tables.GPS_TRACKING2009.COL_MONUMENT_NAME));

        Date startDate = getDate(results,
                                 Tables.GPS_TRACKING2009.COL_START_DATE,
                                 Tables.GPS_TRACKING2009.COL_START_TIME);
        if (startDate == null) {
            return resources;
        }
        Date endDate = getDate(results, Tables.GPS_TRACKING2009.COL_END_DATE,
                               Tables.GPS_TRACKING2009.COL_END_TIME);
        if (endDate == null) {
            endDate = startDate;
        }
        GsacSite site = new GsacSite(null, monumentID.toUpperCase(),
                                     monumentID.toUpperCase());

        Calendar calendar = Calendar.getInstance(TIMEZONE_UTC);
        calendar.setTime(startDate);
        String YYYY = "" + calendar.get(calendar.YEAR);
        String DDD = StringUtil.padZero(calendar.get(calendar.DAY_OF_YEAR),
                                        3);
        String YY = YYYY.substring(2);
        for (int i = 0; i < GNSS_FILE_TYPES.length; i++) {
            ResourceType resourceType = GNSS_FILE_TYPES[i];
            if ((resourceTypes != null)
                    && !resourceTypes.contains(resourceType.getId())) {
                continue;
            }
            String fileTypeLetter = GNSS_FILE_INFO[i];
            String resourceID = type.makeId(new String[] { monumentID,
                    startDate.toString(), resourceType.getId() });

            //Make the path
            //ftp://cddis.gsfc.nasa.gov/pub/gps/data/daily/YYYY/DDD/YYd/MMMMDDD0.YYd.Z
            String path = "";
            path = "ftp://cddis.gsfc.nasa.gov/pub/gps/data/daily/" + YYYY
                   + "/" + DDD + "/" + YY + fileTypeLetter + "/"
                   + monumentID.toLowerCase() + DDD + "0." + YY
                   + fileTypeLetter + ".Z";
            GsacFile resource = new GsacFile(resourceID,
                                        new FileInfo(path), site, startDate,
                                        startDate, endDate, resourceType);

            if (addMetadata) {
                addPropertyMetadata(results, resource, GNSS_METADATA_COLUMNS);
            }
            resources.add(resource);
        }
        return resources;
    }

    /**
     * _more_
     *
     * @param type _more_
     * @param results _more_
     * @param resourceTypes _more_
     * @param addMetadata _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<GsacFile> makeDorisResources(CddisType type,
            ResultSet results, HashSet<String> resourceTypes,
            boolean addMetadata)
            throws Exception {
        List<GsacFile> resources = new ArrayList<GsacFile>();
        String             station   = "N/A";
        //            results.getString(
        //                unDot(Tables.DORIS_2009.COL_STATION));
        Date startDate = results.getDate(1);
        if (startDate == null) {
            return resources;
        }
        Date endDate = results.getDate(2);
        if (endDate == null) {
            endDate = startDate;
        }

        String fileName =
            results.getString(unDot(Tables.DORIS_2009.COL_FILE_NAME));
        String satellite =
            results.getString(unDot(Tables.DORIS_2009.COL_SATELLITE));
        String sat = DORIS_SATELLITE_MAP.get(satellite);

        if (sat == null) {
            throw new IllegalArgumentException(
                "Could not find DORIS satellite:" + satellite);
        }

        GsacSite site = new GsacSite(null, station.toUpperCase(),
                                     station.toUpperCase());

        Calendar calendar = Calendar.getInstance(TIMEZONE_UTC);
        calendar.setTime(startDate);
        String YYYY = "" + calendar.get(calendar.YEAR);
        String DDD = StringUtil.padZero(calendar.get(calendar.DAY_OF_YEAR),
                                        3);
        String YY = YYYY.substring(2);

        for (int i = 0; i < DORIS_FILE_TYPES.length; i++) {
            ResourceType resourceType = DORIS_FILE_TYPES[i];
            if ((resourceTypes != null)
                    && !resourceTypes.contains(resourceType.getId())) {
                continue;
            }
            String resourceID = type.makeId(new String[] { station,
                    resourceType.getId() });
            //Make the path
            String path = "ftp://cddis.gsfc.nasa.gov/pub/doris/data/" + sat
                          + "/" + fileName + ".Z";
            GsacFile resource = new GsacFile(resourceID,
                                        new FileInfo(path), site, startDate,
                                        startDate, endDate, resourceType);

            if (addMetadata) {
                addPropertyMetadata(results, resource,
                                    DORIS_METADATA_COLUMNS);
            }
            resources.add(resource);
        }

        return resources;
    }

    /**
     * _more_
     *
     * @param type _more_
     * @param results _more_
     * @param resourceTypes _more_
     * @param addMetadata _more_
     * @param dateRange _more_
     * @param extraStuff _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<GsacFile> makeSlrResources(CddisType type,
            ResultSet results, HashSet<String> resourceTypes,
            boolean addMetadata, Date[] dateRange, Object[] extraStuff)
            throws Exception {

        if (extraStuff[0] == null) {
            extraStuff[0] = null;
        }
        List<GsacFile> resources = new ArrayList<GsacFile>();
        String             station   = "N/A";
        Date               startDate = results.getDate(1);
        if (startDate == null) {
            return resources;
        }
        Date endDate = results.getDate(2);
        if (endDate == null) {
            endDate = startDate;
        }
        String satellite =
            results.getString(unDot(Tables.SATELLITESQL_2009.COL_SATELLITE));

        //TODO: Put these name changes into a table
        if (satellite.equals("BE-C")) {
            satellite = "BEACONC";
        } else if (satellite.equals("JASON")) {
            satellite = "JASON1";
        }

        String SATNAME = satellite.replace("-", "").toLowerCase();
        //Use the sat name as the site name
        GsacSite site = new GsacSite(null, satellite.toUpperCase(),
                                     satellite.toUpperCase());

        Calendar     calendar     = Calendar.getInstance(TIMEZONE_UTC);
        ResourceType resourceType = SLR_FILE_TYPES[0];
        if ((resourceTypes != null)
                && !resourceTypes.contains(resourceType.getId())) {
            return resources;
        }
        calendar.setTime(startDate);

        while (true) {
            Date calTime = calendar.getTime();
            if (calTime.getTime() > endDate.getTime()) {
                break;
            }
            //The use specified a date range
            if ((dateRange != null) && (dateRange[0] != null)) {
                if (calTime.getTime() < dateRange[0].getTime()) {
                    //Go to the next month and skip this month
                    calendar.add(calendar.MONTH, 1);
                    continue;
                }
            }
            if ((dateRange != null) && (dateRange[1] != null)) {
                //If we're after the time then we're done
                if (calTime.getTime() > dateRange[1].getTime()) {
                    break;
                }
            }

            String YYYY = "" + calendar.get(calendar.YEAR);
            String MM = StringUtil.padZero(calendar.get(calendar.MONTH) + 1,
                                           2);
            String YY   = YYYY.substring(2);
            String YYMM = YY + MM;
            String resourceID = type.makeId(new String[] { satellite,
                    resourceType.getId(), YYMM });
            //Make the path
            //ftp://cddis.gsfc.nasa.gov/pub/slr/data/npt/SATNAME/YYYY/SATNAME.YYMM
            String path = "ftp://cddis.gsfc.nasa.gov/pub/slr/data/npt/"
                          + SATNAME + "/" + YYYY + "/" + SATNAME + "." + YYMM;
            Date     publishDate = calendar.getTime();
            Calendar tmpCalendar = Calendar.getInstance(TIMEZONE_UTC);
            tmpCalendar.setTime(calendar.getTime());

            String startDateString = tmpCalendar.get(Calendar.YEAR) + "-"
                                     + (tmpCalendar.get(Calendar.MONTH) + 1)
                                     + "-" + "1";

            Date dataStartDate = yyyyMMDDSdf.parse(startDateString);
            //            System.err.println ("cal:" + tmpCalendar.getTime() +" " + tmpCalendar.get(Calendar.YEAR));


            //            tmpCalendar.add(Calendar.MONTH,1);
            //            tmpCalendar.add(Calendar.DAY_OF_MONTH,-1);

            String endDateString =
                tmpCalendar.get(Calendar.YEAR) + "-"
                + (tmpCalendar.get(Calendar.MONTH) + 1) + "-"
                + (tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

            Date dataEndDate = yyyyMMDDSdf.parse(endDateString);

            //            System.err.println ("date:" + startDateString +" -- " + endDateString +" " + dataEndDate);
            GsacFile resource = new GsacFile(resourceID,
                                        new FileInfo(path), site,
                                        publishDate, dataStartDate,
                                        dataEndDate, resourceType);
            if (addMetadata) {
                addPropertyMetadata(results, resource, SLR_METADATA_COLUMNS);
            }
            resources.add(resource);




            calendar.add(calendar.MONTH, 1);
        }
        return resources;

    }


    /**
     * _more_
     *
     * @param type _more_
     * @param results _more_
     * @param resourceTypes _more_
     * @param addMetadata _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public List<GsacFile> makeVlbiResources(CddisType type,
            ResultSet results, HashSet<String> resourceTypes,
            boolean addMetadata)
            throws Exception {
        List<GsacFile> resources = new ArrayList<GsacFile>();
        return resources;
    }


    /**
     * _more_
     *
     * @param level _more_
     * @param gsacResource _more_
     *
     * @throws Exception On badness
     */
    public void doGetResourceMetadata(int level, GsacFile gsacResource)
            throws Exception {}


    /**
     * CHANGEME
     * Get the columns to select for resources
     * @return resource columns
     */
    private String getResourceColumns() {
        return "";
    }


    /**
     * _more_
     *
     * @param results _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public GsacFile makeResource(ResultSet results) throws Exception {
        return null;
    }




    /**
     *
     * @param resourceId _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public GsacResource getResource(String resourceId) throws Exception {
        List<String> toks = CddisType.getTypeAndFields(resourceId);
        System.err.println("get resource:" + toks);
        CddisType type = CddisType.getType(toks.get(0));
        if (type.isGnss()) {
            String          monumentId    = toks.get(1);
            String          dateString    = toks.get(2);
            HashSet<String> resourceTypes = new HashSet<String>();
            resourceTypes.add(toks.get(3));
            Date dttm = DateUtil.parse(dateString);
            Clause clause =
                Clause.and(
                    Clause.eq(
                        Tables.GPS_TRACKING2009.COL_MONUMENT_NAME,
                        monumentId), Clause.eq(
                            Tables.GPS_TRACKING2009.COL_START_DATE, dttm));
            Statement statement =
                getDatabaseManager().select(type.getResourceColumns(),
                                            clause.getTableNames(), clause);

            try {
                ResultSet results = statement.getResultSet();
                if (results.next()) {
                    List<GsacFile> resources = makeGnssResources(type,
                                                       results,
                                                       resourceTypes, true);
                    if (resources.size() > 0) {
                        return resources.get(0);
                    }
                }
                return null;
            } finally {
                getDatabaseManager().closeAndReleaseConnection(statement);
            }
        }

        //TODO:
        return null;
    }



    /**
     * Create the list of resource types that are shown to the user
     *
     * @return resource types
     */
    public List<ResourceType> doGetResourceTypes() {
        List<ResourceType> resourceTypes = new ArrayList<ResourceType>();
        for (ResourceType[] types : ALL_FILE_TYPES) {
            for (ResourceType resourceType : types) {
                resourceTypes.add(resourceType);
            }
        }
        return resourceTypes;
    }




    /**
     * helper method
     *
     * @return sitemanager
     */
    public CddisSiteManager getSiteManager() {
        return (CddisSiteManager) getRepository().getResourceManager(GsacSite.CLASS_SITE);
    }



    /**
     * _more_
     *
     * @param type _more_
     * @param clauses _more_
     * @param dateRange _more_
     * @param msgBuff _more_
     */
    private void addDateClause(CddisType type, List<Clause> clauses,
                               Date[] dateRange, StringBuffer msgBuff) {
        String[] cols = type.getResourceDateColumns();
        if (cols == null) {
            return;
        }
        if (dateRange[0] != null) {
            clauses.add(Clause.ge(cols[0], dateRange[0]));
            appendSearchCriteria(msgBuff, "Date&gt;=",
                                 "" + format(dateRange[0]));
        }

        if (dateRange[1] != null) {
            clauses.add(Clause.le(cols[1], dateRange[1]));
            appendSearchCriteria(msgBuff, "Date&lt;=",
                                 "" + format(dateRange[1]));
        }
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public String unDot(String s) {
        return SqlUtil.unDot(s);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<Capability> doGetResourceQueryCapabilities() {
        try {
            List<Capability> capabilities = new ArrayList<Capability>();

            addDefaultCapabilities(capabilities);

            capabilities.add(new Capability(ARG_DORIS_SATELLITE,
                                            "DORIS Satellite",
                                            DORIS_SATELLITES, true));

            CddisSearchInfo slrSatelliteSearchInfo =
                new CddisSearchInfo(
                    CddisType.TYPE_NAME_SLR, ARG_SLR_SATELLITE,
                    Tables.SATELLITESQL_2009.COL_SATELLITE, "SLR Satellite",
                    Capability.TYPE_ENUMERATION, /*"SLR Search"*/ null);


            capabilities.add(makeEnumeratedCapabilty(slrSatelliteSearchInfo));
            return capabilities;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


}
