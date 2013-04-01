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
import java.util.Calendar;


/**
 * Handles all of the resource related repository requests. The main entry point is {@link #handleRequest}
 * 
 * A GSAC FileManager class composes what file-related items are provided for file searches in the API and web site.
 * A GSAC FileManager class composes what items are provided to be returned as results when a search finds something.
 *
 * This RingFileManager.java uses the Ring ingv db table CLINIC_GSAC:

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
  
   This is a simple schema with site names COL_SITO, with metadata for start and end times for the data in data files (COL_FIRST_EPOCH, and COL_LAST_EPOCH),
   and the complete ftp url (with file name) COL_LINK where you can download the data file from RING servers.

    Database rows are for example
| RSTO | 2002-05-01 00:00:00 | 2002-05-01 23:59:00 | ftp://anonymous@bancadati2.gm.ingv.it/OUTGOING/RINEX30/RING/2002/121/RSTO1210.02d.Z |
| INGR | 2002-11-15 00:00:00 | 2002-11-15 23:59:00 | ftp://anonymous@bancadati2.gm.ingv.it/OUTGOING/RINEX30/RING/2002/319/INGR3190.02d.Z |
| TITO | 2002-11-25 00:00:00 | 2002-11-25 23:59:00 | ftp://anonymous@bancadati2.gm.ingv.it/OUTGOING/RINEX30/RING/2002/329/TITO3290.02d.Z |

 Note this reposioty only returns RINEX obs files.  This FileManager class lacks file type controls.

 *
 * @author         S K Wier
 */
public class RingFileManager extends FileManager {

    public static final String TYPE_GNSS_OBSERVATION = "gnss.observation";



    /**
     * ctor
     *
     * @param repository the repository
     */
    public RingFileManager(RingRepository repository) {
        super(repository);

    }


    /**
     *  Enable what file-related items are used to search (query) for geoscience data files to download at this particular data repository.  
     *
     * Initially RING will only search on data times in files, from the database "ingv".
     * You can also search for sites, and files from selected sites.
     *
     * "Capabilities" (in this class anyway) are things to search (query) with
     *
     * @return  List of GSAC "Capabilities"  objects
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();

        // from FileManager class -  add search boxes for all of these" file type, date range, publish date, and file size
        // addDefaultCapabilities(capabilities);

        // explicit code for above search items. In this case only date range for RINEX files, since that is all there is data for in the database about files.
        Capability   cap;
        Capability[] dflt = { 
                              // RING provides only RINEX files, so far.
                              // this "File Type" 'capability' provides a box with 100-some possible file types, everything in the list in 
                              // src/org/gsac/gsl/resources/vocabulary/file.type.properties
                              //initCapability( new Capability( ARG_FILE_TYPE, "File Type", new ArrayList<IdLabel>(), true), 
                              //  "File Query", "Type of file", null, getRepository().getVocabulary( ARG_FILE_TYPE, true)),

                              initCapability(new Capability(ARG_FILE_DATADATE, "Data Dates",
                                  Capability.TYPE_DATERANGE), "File Query", "Date the data this file holds was collected"),

                              // "Publish Date" is when a repository first made a file available.
                              //initCapability(new Capability(ARG_FILE_PUBLISHDATE, "Publish Date",
                              //    Capability.TYPE_DATERANGE), "File Query", "Date when this file was first published to the repository"),

                              //initCapability(cap = new Capability(ARG_FILE_FILESIZE,
                              //        "File Size", Capability .TYPE_NUMBERRANGE), "File Query", "File size") 
        };

        //cap.setSuffixLabel("&nbsp;(bytes)");

        for (Capability capability : dflt) {
            capabilities.add(capability);
        }

        // Also add the SITE-related search choices into the file search web page form, so you can select files from particular sites
        // (gets the site search forms from the related SiteManager class)
        capabilities.addAll(getSiteManager().doGetQueryCapabilities());

        return capabilities;
    }

    /**
     * Handle the search request.   
     * CHANGEME
     *
     * Do the database search as specified by the user's search for files in the web site forms or via the API, (contained in arg "request")
     * and put an array of the results, put into one or more GSAC_file objects, into the object "GsacResponse response."
     *
     * @param request The request [from the api or web search forms]
     * @param response  one or more GSACFile objects, put into the container object "GsacResponse response."
     *
     * @throws Exception on badness
     */
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {

        //System.err.println("   ring file manager handleRequest ");

        //The msgBuff holds the html that describes what is being searched for // (look which is only used for text on the html page?)
        StringBuffer msgBuff = new StringBuffer();

        List<Clause> clauses = new ArrayList<Clause>();

        /* file search items not used by RING but of possible interest elsewhere:

        if (request.defined(ARG_FILESIZE_MIN)) {
            int size = request.get(ARG_FILESIZE_MIN, 0);
            // clauses.add(Clause.
            appendSearchCriteria(msgBuff, "Filesize&gt;=", "" + request.get(ARG_FILESIZE_MIN, 0));
        }

        if (request.defined(ARG_FILESIZE_MAX)) {
            int size = request.get(ARG_FILESIZE_MAX, 0);
            appendSearchCriteria(msgBuff, "Filesize&lt;=", "" + request.get(ARG_FILESIZE_MAX, 0));
        }

        if (request.defined(ARG_FILE_TYPE)) {
            List<String> types =
                (List<String>) request.getList(ARG_FILE_TYPE); addSearchCriteria(msgBuff, "Resource Type", types, ARG_FILE_TYPE);
        }

        Date[] publishDateRange =
            request.getDateRange(ARG_FILE_PUBLISHDATE_FROM, ARG_FILE_PUBLISHDATE_TO, null, null);

        if (publishDateRange[0] != null) {
            appendSearchCriteria(msgBuff, "Publish date&gt;=", "" + format(publishDateRange[0]));
        }

        if (publishDateRange[1] != null) {
            appendSearchCriteria(msgBuff, "Publish date&lt;=", "" + format(publishDateRange[1]));
        }

        */


        // get vales of the data date range requested by the user, from the input from web search form / API:
        Date[] dataDateRange = request.getDateRange(ARG_FILE_DATADATE_FROM, ARG_FILE_DATADATE_TO, null, null);

        //System.err.println ("   RingFileManager: handleRequest: requested date range " + dataDateRange[0] +" to " + dataDateRange[1]);

        if (dataDateRange[0] != null) {
            Calendar cal = Calendar.getInstance();
            // wrangle the input time into a format you can use in a SQL query
            cal.setTime(dataDateRange[0]);
            //cal.add(Calendar.HOUR, 23);
            //cal.add(Calendar.MINUTE, 59);
            //cal.add(Calendar.SECOND, 59);
            java.sql.Date sqlStartDate = new java.sql.Date(cal.getTimeInMillis());
            // make the sql query clause for start times of files
            clauses.add(Clause.ge(Tables.CLINIC_GSAC.COL_FIRST_EPOCH, sqlStartDate));
            cal = null;

            appendSearchCriteria(msgBuff, "Data date&gt;=", "" + format(dataDateRange[0]));
        }

        if (dataDateRange[1] != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataDateRange[1]);
            //cal.add(Calendar.HOUR, 23);
            //cal.add(Calendar.MINUTE, 59);
            //cal.add(Calendar.SECOND, 59);
            java.sql.Date sqlEndDate = new java.sql.Date(cal.getTimeInMillis());
            clauses.add(Clause.le(Tables.CLINIC_GSAC.COL_LAST_EPOCH, sqlEndDate));
            cal = null;

            appendSearchCriteria(msgBuff, "Data date&lt;=", "" + format(dataDateRange[1]));
        }


        // add to query, search on site codes (names,  Tables.CLINIC_GSAC.COL_SITO ) using value(s) of name(s) from the sitemanager class search
        List<String> args = null;
        clauses.add(Clause.or(Clause.makeStringClauses( Tables.CLINIC_GSAC.COL_SITO,
                    args = (List<String>) request.getList(ARG_SITE_CODE))));

        addSearchCriteria(msgBuff, "Site code", args);


        Clause mainClause = Clause.and(clauses);

        // SQL query to select from the columns (fields) of rows in the database table named CLINIC_GSAC, with  query clauses specified...
        Statement statement = getDatabaseManager().select( Tables.CLINIC_GSAC.COLUMNS, Tables.CLINIC_GSAC.NAME, mainClause);

        //System.err.println("   RingFileManager: select query is " +statement);

        int col;
        int cnt=0;

        try {
            //ResultSet results = statement.getResultSet();
            ResultSet results = null;

            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            //SqlUtil.Iterator iter = SqlUtil.getIterator(statement, request.getOffset(), request.getLimit());

            // process each line in results of db query  
            while ((results = iter.getNext()) != null) {
               //System.err.println("      RingFileManager:  got a file  match in db ");

               col=1;
               String siteID = results.getString(col++);
               // alternate String site_name = results.getString( Tables.CLINIC_GSAC.COL_SITO);
               // System.err.println("      RingFileManager:  got a file at site " +siteID);

               Date fromTime = results.getDate(col++); // start_date
               Date toTime = results.getDate(col++); // end_date
               String ftpurl = results.getString(col++);

               ResourceType rt = new ResourceType(TYPE_GNSS_OBSERVATION, "GNSS - Observation");

               //                      GsacFile(     resId, FileInfo fileInfo, GsacResource relatedResource, Date startTime, Date endTime, ResourceType type) 
               GsacFile fileItem = new GsacFile(siteID, new FileInfo(ftpurl), null, fromTime, toTime,     rt);

               // collect all the GsacFile objects made; this is the array of results from the GSAC file seach:
               response.addResource(fileItem);

               cnt++;

               // code to check for exceeding max of how many results allowed
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
     * This takes the resource id that is used to identify files and creates a Gsac object.
     *
     * Composes one particular result provided to user, called when  a user clicks on a particular item in the table of things found, after a search.
     * For example, used to make an HTML page to show about one file. 
     *
     * @param resourceId file id
     *
     * @return GsacFile
     *
     * @throws Exception On badness
     */
    public GsacResource getResource(String resourceId) throws Exception {

        //System.err.println("   ringfilemanager: GsacResource, resourceId: " + resourceId );
        // eg                  ringfilemanager: GsacResource, resourceId: GROT

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
            // alternate: String monumentID = results.getString( Tables.CLINIC_GSAC.COL_SITO);
            //System.err.println("   site name: " + baseName);

            Date fromTime = results.getDate( 2 ); // start_date
            //       System.err.println("   files time range start time : " + fromTime);
            Date toTime = results.getDate( 3 ); // end_date
            // possible alternates:
            //Date fromTime = getDate(results, Tables.CLINIC_GSAC.COL_FIRST_EPOCH);
            //Date toTime  =getDate(results, Tables.CLINIC_GSAC.COL_LAST_EPOCH);

            // get the full URL to use ftp to get one file:
            String location = results.getString(4);
            // possible alternate:
            // String location= results.getString( Tables.CLINIC_GSAC.COL_LINK);
            //System.err.println("   ftp URL file location: " + location );

            /*    Date publishTime = results.getDate( 10 );
            long fileSize = results.getLong( 11 );
            //System.err.println( "FileSize: " + fileSize );
            int dataTypeID = results.getInt( 13 );
            //System.err.println( "dataTypeID: " + dataTypeID );
            */

            ResourceType rt = new ResourceType(TYPE_GNSS_OBSERVATION, "GNSS - Observation");

            //         GsacFile(String        resId, FileInfo fileInfo, GsacResource relatedResource, Date startTime, Date endTime, ResourceType type) 
            GsacFile fileInfo = new GsacFile(resourceId, new FileInfo(location), null,  fromTime, toTime, rt);

            return fileInfo;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
        return null;
    }



    /**
     * helper method
     * get the related Sitemanager for this FileManager, used for added site queries (searches) into the file search page, so
     * the user can select files from one or more sites.
     *
     * @return sitemanager
     */
    public RingSiteManager getSiteManager() {
        return (RingSiteManager) getRepository().getResourceManager( GsacSite.CLASS_SITE);
    }


}
