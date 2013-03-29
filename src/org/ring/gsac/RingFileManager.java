/*
 * Copyright 2013 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

package org.ring.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;
/* CHANGEME - done for INGV RING - include database package */
import org.ring.gsac.database.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.StringUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


/**
 * Handles all of the resource related repository requests. The main entry point is {@link #handleRequest}
 * Look for the CHANGEME comments
 *
 *  uses the Ring ingv db table:

    public static class CLINIC_GSAC extends Tables {
        public static final String NAME = "clinic_gsac";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_SITO =  NAME + ".sito";
        public static final String COL_FIRST_EPOCH =  NAME + ".first_epoch";
        public static final String COL_LAST_EPOCH =  NAME + ".last_epoch";
        public static final String COL_LINK =  NAME + ".link";

        public static final String[] ARRAY = new String[] {
            COL_SITO,COL_FIRST_EPOCH,COL_LAST_EPOCH,COL_LINK
        };
        public static final String COLUMNS = SqlUtil.comma(ARRAY);
        public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);
    public static final CLINIC_GSAC table  = new  CLINIC_GSAC();
    }
  
   This is a simple schema with site name COL_SITO and a data file at that site, with metadata for start and end times for the data,
   and the complete ftp url (with file name) where you can download the data file from RING servers.

    Database rows are for example
| RSTO | 2002-05-01 00:00:00 | 2002-05-01 23:59:00 | ftp://anonymous@bancadati2.gm.ingv.it/OUTGOING/RINEX30/RING/2002/121/RSTO1210.02d.Z |
| INGR | 2002-11-15 00:00:00 | 2002-11-15 23:59:00 | ftp://anonymous@bancadati2.gm.ingv.it/OUTGOING/RINEX30/RING/2002/319/INGR3190.02d.Z |
| TITO | 2002-11-25 00:00:00 | 2002-11-25 23:59:00 | ftp://anonymous@bancadati2.gm.ingv.it/OUTGOING/RINEX30/RING/2002/329/TITO3290.02d.Z |

 *
 * @author         S K Wier
 */
public class RingFileManager extends FileManager {

    public static final String TYPE_GNSS_OBSERVATION = "gnss.observation";

    private static ResourceType[] GNSS_FILE_TYPES = { new ResourceType(TYPE_GNSS_OBSERVATION, "GNSS - Observation") };

    /*
    public static final String[] GNSS_METADATA_COLUMNS = new String[] {
        Tables.SITI_GSAC.COL_ID_SITO,
        Tables.SITI_GSAC.COL_NOME_SITO,
        Tables.SITI_GSAC.COL_LUOGO,
        Tables.SITI_GSAC.COL_LATITUDINE,
        Tables.SITI_GSAC.COL_LONGITUDINE,
        Tables.SITI_GSAC.COL_IERS_DOMES_NUMBER,
        Tables.SITI_GSAC.COL_ID_RESPONSIBLE_AGENCY,
        Tables.SITI_GSAC.COL_ID_MONUMENTO,
        Tables.SITI_GSAC.COL_NAZIONE,
        Tables.SITI_GSAC.COL_REGIONE,
        Tables.SITI_GSAC.COL_AGENZIA,
    };
    */


    private static ResourceType[][] ALL_FILE_TYPES = { GNSS_FILE_TYPES };

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
    public RingFileManager(RingRepository repository) {
        super(repository);

        sdf         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        yyyyMMDDSdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TIMEZONE_UTC);
        yyyyMMDDSdf.setTimeZone(TIMEZONE_UTC);
    }


    /**
     *
     *  Enable what file-related items are used to search for geoscience data files to download
     *  at this particular data repository.  Initially RING will only search on data times in files, from the database "ingv".
     *  You can also search for sites, and files from selected sites.
     * CHANGEME 
     *
     * @return  List of GSAC "Capabilities" which are things to search with
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();

        //  from FileManager class -  add search boxes for file type, date range, publish date, and file size
        //addDefaultCapabilities(capabilities);

        // explicit code for the above search items. For RING only need date range for RINEX files (so far)
        Capability   cap;
        Capability[] dflt = { 
                              // RING provides only RINEX files, so far.
                              // this provides a box with 100-some possible file types, everything in the list in 
                              // src/org/gsac/gsl/resources/vocabulary/file.type.properties
                              //initCapability( new Capability( ARG_FILE_TYPE, "File Type", new ArrayList<IdLabel>(), true), 
                              //  "File Query", "Type of file", null, getRepository().getVocabulary( ARG_FILE_TYPE, true)),

                              initCapability(new Capability(ARG_FILE_DATADATE, "Data Dates",
                                  Capability.TYPE_DATERANGE), "File Query", "Date the data this file holds was collected"),

                              //initCapability(new Capability(ARG_FILE_PUBLISHDATE, "Publish Date",
                              //    Capability.TYPE_DATERANGE), "File Query", "Date when this file was first published to the repository"),

                              //initCapability(cap = new Capability(ARG_FILE_FILESIZE,
                              //        "File Size", Capability .TYPE_NUMBERRANGE), "File Query", "File size") 
        };

        //cap.setSuffixLabel("&nbsp;(bytes)");

        for (Capability capability : dflt) {
            capabilities.add(capability);
        }

        // add the SITE-related search choices into the file search web page form, so you can select files from particular sites
        capabilities.addAll(getSiteManager().doGetQueryCapabilities());

        return capabilities;
    }

    /**
     * CHANGEME
     * "handle the search request"     which means...
     *
     * @param request The request [from the api or web search forms?]
     * @param response The response
     *
     * @throws Exception on badness
     */
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {

        System.err.println("   ring file manager handleRequest ");

        //The msgBuff holds the html that describes what is being searched for
        // (look which is only used for text on the html page?)
        StringBuffer msgBuff = new StringBuffer();

        List<Clause> clauses = new ArrayList<Clause>();

        /*
        // these may have future use at RING:
        if (request.defined(ARG_FILESIZE_MIN)) {
            int size = request.get(ARG_FILESIZE_MIN, 0);
            appendSearchCriteria(msgBuff, "Filesize&gt;=",
                                 "" + request.get(ARG_FILESIZE_MIN, 0));
        }
        if (request.defined(ARG_FILESIZE_MAX)) {
            int size = request.get(ARG_FILESIZE_MAX, 0);
            appendSearchCriteria(msgBuff, "Filesize&lt;=",
                                 "" + request.get(ARG_FILESIZE_MAX, 0));
        }
        if (request.defined(ARG_FILE_TYPE)) {
            List<String> types =
                (List<String>) request.getList(ARG_FILE_TYPE);
            addSearchCriteria(msgBuff, "Resource Type", types, ARG_FILE_TYPE);
        }
        Date[] publishDateRange =
            request.getDateRange(ARG_FILE_PUBLISHDATE_FROM,
                                 ARG_FILE_PUBLISHDATE_TO, null, null);

        if (publishDateRange[0] != null) {
            appendSearchCriteria(msgBuff, "Publish date&gt;=",
                                 "" + format(publishDateRange[0]));
        }
        if (publishDateRange[1] != null) {
            appendSearchCriteria(msgBuff, "Publish date&lt;=",
                                 "" + format(publishDateRange[1]));
        }
        // end of these may have future use at RING.
        */

/*
        // from sopac:
        // start/stop date range ///////////////////////////////////
        Date[] dataDateRange = request.getDateRange(ARG_FILE_DATADATE_FROM,
                ARG_FILE_DATADATE_TO, null, null);
        // TODO: now that we're only using year/doy virtual column, remove
        // appendSearchCriteria method calls here?
        if (dataDateRange[0] != null) {
            //clauses.add(Clause.ge(Tables.DATA_RECORD.COL_START_TIME,
            //      dataDateRange[0]));
            appendSearchCriteria(msgBuff, "Start date&ge;=", ""
                    + format(dataDateRange[0]));

            // need to include year in query of table, this column is indexed,
            // otherwise queries are very slow
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataDateRange[0]);
            int year = cal.get(Calendar.YEAR);
            int doy = cal.get(Calendar.DAY_OF_YEAR);
            int yearDoy = year * 1000 + doy;
            clauses.add(Clause.ge("DATA_RECORD.YEARDOY", // add this col to Tables.java
                    yearDoy));
            cal = null;
        }
        if (dataDateRange[1] != null) {
            //clauses.add(Clause.le(Tables.DATA_RECORD.COL_STOP_TIME,
            //      dataDateRange[1]));
            appendSearchCriteria(msgBuff, "End date&le;=", ""
                    + format(dataDateRange[1]));
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataDateRange[1]);
            int year = cal.get(Calendar.YEAR);
            int doy = cal.get(Calendar.DAY_OF_YEAR);
            int yearDoy = year * 1000 + doy;
            clauses.add(Clause.le("DATA_RECORD.YEARDOY", // add this col to Tables.java
                    yearDoy));
        }
*/

        Date[] dataDateRange = request.getDateRange(ARG_FILE_DATADATE_FROM, ARG_FILE_DATADATE_TO, null, null);

        System.err.println ("   RingFileManager: requested date range " + dataDateRange[0] +" to " + dataDateRange[1]);

        if (dataDateRange[0] != null) {
            clauses.add(Clause.ge(Tables.CLINIC_GSAC.COL_FIRST_EPOCH, dataDateRange[0]));

            appendSearchCriteria(msgBuff, "Data date&gt;=", "" + format(dataDateRange[0]));
        }

        if (dataDateRange[1] != null) {
            clauses.add(Clause.le(Tables.CLINIC_GSAC.COL_LAST_EPOCH, dataDateRange[1]));

            appendSearchCriteria(msgBuff, "Data date&lt;=", "" + format(dataDateRange[1]));
        }
        //clauses.add(Clause.ge(Tables.CLINIC_GSAC.COL_FIRST_EPOCH, starttime));
        //clauses.add(Clause.le(Tables.CLINIC_GSAC.COL_FIRST_EPOCH, stoptime));
        //clauses.add(Clause.eq(Tables.CLINIC_GSAC.COL_SITO, resourceId));

        System.err.println("    FIX: RingFileManager: need site name to complete query clause");

        Clause mainClause = Clause.and(clauses);

        // SQL query to select from the columns (fields) of rows in the database table named CLINIC_GSAC, with  query clauses specified...
        Statement statement =
           getDatabaseManager().select( Tables.CLINIC_GSAC.COLUMNS, Tables.CLINIC_GSAC.NAME, mainClause);

        System.err.println("    RingFileManager: select query is " +statement);

        int col = 1;

        int cnt=0;
        try {
            //ResultSet results = statement.getResultSet();
            ResultSet results = null;

            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            //SqlUtil.Iterator iter = SqlUtil.getIterator(statement, request.getOffset(), request.getLimit());

            // process each line in results of db query  
            while ((results = iter.getNext()) != null) {
               col=0;
               String siteID = results.getString(col++);
               System.err.println("      RingFileManager:  got a file at site " +siteID);
               Date fromTime = results.getDate(col++); // start_date
               Date toTime = results.getDate(col++); // end_date
               String ftpurl = results.getString(col++);

               ResourceType rt = new ResourceType(TYPE_GNSS_OBSERVATION, "GNSS - Observation");

               //                      GsacFile(     resId, FileInfo fileInfo, GsacResource relatedResource, Date startTime, Date endTime, ResourceType type) 
               GsacFile fileItem = new GsacFile(siteID, new FileInfo(ftpurl), null, fromTime, toTime,     rt);

               response.addResource(fileItem);
               cnt++;
               System.err.println("      RingFileManager:  made  a file object " );
                /* if (!iter.countOK()) {
                    response.setExceededLimit();
                    break;
                }*/
            }
            iter.close();
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        setSearchCriteriaMessage(response, msgBuff);

    } // end handleRequest




    /**
     * CHANGEME
     * This takes the resource id that is used to identify files and creates a GsacFile object.
     *
     * (Appears to only be called when you click on a particular item in the table of things found, after a search.
     *  Something to do with composing an HTML page to show about one site?)
     *
     * @param resourceId file id
     *
     * @return GsacFile
     *
     * @throws Exception On badness
     */
    public GsacResource getResource(String resourceId) throws Exception {

        System.err.println("   ring file manager getresource");

        // compose the complete select SQL phrase; select matching sites by name:
        // the SQL search clause select logic, where a column value COL_SITO  = the "resourceId" which is some site name entered by the user in the api or search form
        Clause clause = Clause.eq(Tables.CLINIC_GSAC.COL_SITO, resourceId);

        Statement statement =
            getDatabaseManager().select(Tables.CLINIC_GSAC.COLUMNS, Tables.CLINIC_GSAC.NAME, clause);

        int cnt=0;
        try {
            ResultSet results = statement.getResultSet();
            while (results.next()) {

            // Database rows are for example
            // | RSTO | 2002-05-01 00:00:00 | 2002-05-01 23:59:00 | ftp://anonymous@bancadati2.gm.ingv.it/OUTGOING/RINEX30/RING/2002/121/RSTO1210.02d.Z |
            String baseName = results.getString(1);
            // String monumentID = results.getString( Tables.CLINIC_GSAC.COL_SITO);
            System.err.println("   name?: " + baseName);
            Date fromTime = results.getDate( 2 ); // start_date
            System.err.println("   start time? : " + fromTime);
            Date toTime = results.getDate( 3 ); // end_date
            //Date fromTime = getDate(results, Tables.CLINIC_GSAC.COL_FIRST_EPOCH);
            //Date toTime  =getDate(results, Tables.CLINIC_GSAC.COL_LAST_EPOCH);
            String location = results.getString(4);
            // String location= results.getString( Tables.CLINIC_GSAC.COL_LINK);
            System.err.println("   ftp file location?: " + location );

            /*    Date publishTime = results.getDate( 10 );
            long fileSize = results.getLong( 11 );
            //System.err.println( "FileSize: " + fileSize );
            int dataTypeID = results.getInt( 13 );
            //System.err.println( "dataTypeID: " + dataTypeID );
            */

            //    GsacFile fileinfo = new GsacFile( resourceId, new FileInfo( location, fileSize, checkSum), null, publishTime, fromTime, toTime, toResourceType(type) ); */
            //GsacSite site = new GsacSite(null, monumentID, "");
            ResourceType rt = new ResourceType(TYPE_GNSS_OBSERVATION, "GNSS - Observation");

            //         GsacFile(String        resId, FileInfo fileInfo, GsacResource relatedResource, Date startTime, Date endTime, ResourceType type) 
            GsacFile fileInfo = new GsacFile(resourceId, new FileInfo(location), null,                         fromTime, toTime,     rt);

            return fileInfo;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
        return null;
    }

/*
        List<Clause> clauses = new ArrayList<Clause>();
        clauses.add(Clause.eq(Tables.CLINIC_GSAC.COL_SITO, resourceId));
        //clauses.add(Clause.eq(Tables.CLINIC_GSAC.COL_FIRST_EPOCH, dttm));

        // SQL query to select from the columns (fields) of rows in the database table CLINIC_GSAC, with  query clauses specified...
        Statement statement =
            //getDatabaseManager().select(getResourceSelectColumns(), clause.getTableNames(), clause);
           getDatabaseManager().select( Tables.CLINIC_GSAC.COLUMNS, Tables.CLINIC_GSAC.NAME,
                Clause.and(clauses), (String) null, -1);

          try {
                // do the SQL select query and get the results, one or more 'rows':
                //SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);

                ResultSet results = statement.getResultSet();

                if (results.next()) {
                    List<GsacFile> resources = null; // = makeGnssResources(type, results, resourceTypes, true);

                    if (resources.size() > 0) {
                        return resources.get(0);
                    }
                }
                return null;
            } finally {
                getDatabaseManager().closeAndReleaseConnection(statement);
            }
*/




    /**
     * Create the list of resource types that are shown to the user. This is
     * called by the getDefaultCapabilities  look ?
     *
     * @return resource types
     */
    public List<ResourceType> doGetResourceTypes() {
        List<ResourceType> resourceTypes = new ArrayList<ResourceType>();

        resourceTypes.add(new ResourceType("rinex", "RINEX Files"));

        //resourceTypes.add(new ResourceType("qc", "QC Files"));

        return resourceTypes;
    }


    /**
     * helper method
     *
     * @return sitemanager
     */
    public RingSiteManager getSiteManager() {
        return (RingSiteManager) getRepository().getResourceManager(
            GsacSite.CLASS_SITE);
    }

}
