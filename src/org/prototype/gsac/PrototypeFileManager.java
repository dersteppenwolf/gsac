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

/* CHANGEME - use the correct name of package: */
package org.prototype.gsac;
import  org.prototype.gsac.database.*;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;

// older version (to Oct 16 2013) had 
//     import ucar.unidata.sql.Clause;
//     import ucar.unidata.sql.SqlUtil;
import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.StringUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Calendar;


/**
 * Handles all of the resource related repository requests. The main entry point is {@link #handleRequest}
 * For the Prototype GSAC (Geodesy Seamless Archive). 
 * 
 * For a particular GSAC, code to handle data file searches ands download information, based on the database read by GSAC about data holdings.
 * 
 * A GSAC FileManager class composes what file-related items are provided for SEARCHES for files in the API and web site.
 * A GSAC FileManager class composes what items are provided returned in the RESULTS when a search finds something.
 *
 * This FileManager.java uses the prototype GSCA db table gnss_data_file;
 *
 * @author  Jeff McWhirter 2011
 * @author  S K Wier 30 Oct 2013; UNAVCO  
 */
public class PrototypeFileManager extends FileManager {

    public static final String TYPE_GNSS_OBSERVATION = "geodesy.data";

    /**
     * ctor
     *
     * @param repository the repository
     */
    public PrototypeFileManager(PrototypeRepository repository) {
        super(repository);

    }

    /**
     *  Enable what file-related items are used in searches (database queries) for geoscience data files to download from this particular data repository.  
     *  These items can be used for searches: date range of files; file type.
     *  This also shows the station search itemss on the file search page so the user can, for example, limit files found to one or a few stations.
     *
     *  In GSAC "Capabilities" are the things to search (query) on. 
     *
     * @return  List of GSAC "Capabilities"  objects
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();

        // addDefaultCapabilities(capabilities);

        Capability   cap;

        // get the file type names from the database
        String [] values; 
        try {
             values = getDatabaseManager().readDistinctValues( Tables.FILE_TYPE.NAME, Tables.FILE_TYPE.COL_FILE_TYPE_NAME);
          } catch (Exception exc) {
               throw new RuntimeException(exc);
        }
        Arrays.sort(values);

        Capability[] dflt = { 

              initCapability(new Capability(ARG_FILE_DATADATE, "Data Date Range", Capability.TYPE_DATERANGE),            "File Query", "Date the data was collected"),

              //  capabilities.add(new Capability(GsacArgs.ARG_FILE_TYPE, "File Type", values, true, CAPABILITY_GROUP_SITE_QUERY) );
              initCapability(new Capability(GsacArgs.ARG_FILE_TYPE, "File Type", values, true, Capability.TYPE_FILETYPE ),"File Query", "Data file type" ),

              // search on "Publish Date" is when a repository first made a file available.
              //initCapability(new Capability(ARG_FILE_PUBLISHDATE, "Publish Date",
              //    Capability.TYPE_DATERANGE), "File Query", "Date when this file was first published to the repository"),

              // search on file size.  
              //initCapability(cap = new Capability(ARG_FILE_FILESIZE,  "File Size", Capability .TYPE_NUMBERRANGE), "File Query", "File size") 
        };
        // use with file size searches: cap.setSuffixLabel("&nbsp;(bytes)");

        for (Capability capability : dflt) {
            capabilities.add(capability);
        }

        // Also add all the station-related search choices into the file search web page form, so you can select files from particular sites
        // (gets all the site searches from the related SiteManager class)
        capabilities.addAll(getSiteManager().doGetQueryCapabilities());

        return capabilities;
    }

    /**
     * Handle the search request.   
     * 1. Compose a db query select clause for the requests' values, and make the select query on the GSAC db.
     * 2. Do the database search as specified by the user's search for files in the web site forms or via the API, (contained in input object "request")
     *    and put an array of the results, with one or more GSAC_file objects, into the container object "GsacResponse response."
     *
     * @param request    The request [from the api or web search forms] what to search with
     * @param response   one or more GSACFile objects, in a "GsacResponse"
     *
     * @throws Exception on badness
     */
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        StringBuffer msgBuff = new StringBuffer();
        List<Clause> clauses = new ArrayList<Clause>();

        // make the SQL query to select from the columns (fields) of rows in the database, with  query clauses generated here.

        /* file search items not used yet, but of possible interest
        if (request.defined(ARG_FILESIZE_MIN)) {
            int size = request.get(ARG_FILESIZE_MIN, 0);
            clauses.add(Clause.appendSearchCriteria(msgBuff, "Filesize&gt;=", "" + request.get(ARG_FILESIZE_MIN, 0));
        }
        if (request.defined(ARG_FILESIZE_MAX)) {
            int size = request.get(ARG_FILESIZE_MAX, 0);
            appendSearchCriteria(msgBuff, "Filesize&lt;=", "" + request.get(ARG_FILESIZE_MAX, 0));
        }
        if (request.defined(ARG_FILE_TYPE)) {
            List<String> types = (List<String>) request.getList(ARG_FILE_TYPE); 
            addSearchCriteria(msgBuff, "Resource Type", types, ARG_FILE_TYPE);
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

        // LOOK how this works
        addStringSearch(request, ARG_SITECODE, ARG_SITECODE_SEARCHTYPE, msgBuff, "Site Code", Tables.STATION.COL_CODE_4CHAR_ID, clauses);
        //System.err.println("   FileManager:handleRequest clause at 1 " + clauses) ;
        
        // make query clause for the  file type
        if (request.defined(GsacArgs.ARG_FILE_TYPE)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacArgs.ARG_FILE_TYPE);
            clauses.add( Clause.or( Clause.makeStringClauses( Tables.FILE_TYPE.COL_FILE_TYPE_NAME, values)));
            // if (request.defined(ARG_FILE_TYPE)) {
            // arg File type is label for html page "Search Criteria" section.
            //List<String> types = (List<String>) request.getList(ARG_FILE_TYPE); 
            //addSearchCriteria(msgBuff, "Ffile Type", types, ARG_FILE_TYPE);
            //System.err.println (" file type(s) to find :"+types);
            //System.err.println (" msgBuff :"+msgBuff);
        }
        //System.err.println("   FileManager:handleRequest clause at 2 " + clauses) ;

        
        // get values of the data date range requested by the user, from the input from web search form / API:
        Date[] dataDateRange = request.getDateRange(ARG_FILE_DATADATE_FROM, ARG_FILE_DATADATE_TO, null, null);

        if (dataDateRange[0] != null) {
            Calendar cal = Calendar.getInstance();
            // wrangle the input time into a format you can use in a SQL query
            cal.setTime(dataDateRange[0]);
            // to shift one day earlier:   3 lines:
            //cal.add(Calendar.HOUR, 23);
            //cal.add(Calendar.MINUTE, 59);
            //cal.add(Calendar.SECOND, 59);
            java.sql.Date sqlStartDate = new java.sql.Date(cal.getTimeInMillis());
            // make the sql query clause for start times of files
            clauses.add(Clause.ge(Tables.GNSS_DATA_FILE.COL_DATA_START_TIME, sqlStartDate));
            cal = null;

            appendSearchCriteria(msgBuff, "Data date&gt;=", "" + format(dataDateRange[0]));
        }

        if (dataDateRange[1] != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataDateRange[1]);
            // do this to NOT shift one day earlier:   3 lines:
            cal.add(Calendar.HOUR, 23);
            cal.add(Calendar.MINUTE, 59);
            cal.add(Calendar.SECOND, 59);
            java.sql.Date sqlEndDate = new java.sql.Date(cal.getTimeInMillis());
            clauses.add(Clause.le(Tables.GNSS_DATA_FILE.COL_DATA_STOP_TIME, sqlEndDate));
            cal = null;

            appendSearchCriteria(msgBuff, "Data date&lt;=", "" + format(dataDateRange[1]));
        }

        // sql select needs to join row pairs from these tables ie use both, connected by the station_id values.
        clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.GNSS_DATA_FILE.COL_STATION_ID )) ;
 
        // sql select needs to join row pairs from these tables ie use both, connected by the station_id values.
        clauses.add(Clause.join(Tables.FILE_TYPE.COL_FILE_TYPE_ID, Tables.GNSS_DATA_FILE.COL_FILE_TYPE_ID )) ;
 
        Clause mainClause = Clause.and(clauses);
        //System.err.println("   FileManager:handleRequest select where clause " + mainClause) ;

        //  the sql select FROM clause, ie which tables  
        List<String> tables = new ArrayList<String>();
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.GNSS_DATA_FILE.NAME);
        tables.add(Tables.FILE_TYPE.NAME);
        
        //  and for the mysql SELECT clause: make a list of what to get (row values returned):
        String cols=SqlUtil.comma(new String[]{
             Tables.GNSS_DATA_FILE.COL_STATION_ID,
             Tables.GNSS_DATA_FILE.COL_FILE_TYPE_ID,
             Tables.GNSS_DATA_FILE.COL_DATA_START_TIME,
             Tables.GNSS_DATA_FILE.COL_DATA_STOP_TIME,
             Tables.GNSS_DATA_FILE.COL_PUBLISHED_DATE,
             Tables.GNSS_DATA_FILE.COL_FILE_URL,
             Tables.STATION.COL_CODE_4CHAR_ID,
             Tables.GNSS_DATA_FILE.COL_ACCESS_PERMISSION_ID,
             Tables.GNSS_DATA_FILE.COL_EMBARGO_DURATION_HOURS,
             Tables.GNSS_DATA_FILE.COL_EMBARGO_AFTER_DATE,
             Tables.STATION.COL_ACCESS_PERMISSION_ID,
             Tables.STATION.COL_EMBARGO_DURATION_HOURS,
             Tables.STATION.COL_EMBARGO_AFTER_DATE,
             Tables.FILE_TYPE.COL_FILE_TYPE_ID,
             Tables.FILE_TYPE.COL_FILE_TYPE_NAME
             });


        //Statement statement = getDatabaseManager().select( cols,  tables,  mainClause);
        Statement statement = getDatabaseManager().select( cols,  tables,  mainClause, " order by " + Tables.GNSS_DATA_FILE.COL_DATA_START_TIME+", "+Tables.STATION.COL_CODE_4CHAR_ID, -1);

        //System.err.println("       sql statmnt obj    " +statement);

        int col;

        try {
            ResultSet results = null;

            // get each line of values returned from the sql select command
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            //  or SqlUtil.Iterator iter = SqlUtil.getIterator(statement, request.getOffset(), request.getLimit());

            // process each line 
            while ((results = iter.getNext()) != null) {
               
               // get an individual file's  values from each single row returned in the array "results"
               String siteID = results.getString(Tables.STATION.COL_CODE_4CHAR_ID);
               int station_id  = results.getInt(Tables.GNSS_DATA_FILE.COL_STATION_ID);
               int file_type_id  = results.getInt(Tables.GNSS_DATA_FILE.COL_FILE_TYPE_ID);
               Date data_start_time  = results.getDate(Tables.GNSS_DATA_FILE.COL_DATA_START_TIME);
               Date data_stop_time  = results.getDate(Tables.GNSS_DATA_FILE.COL_DATA_STOP_TIME);
               Date published_date  = results.getDate(Tables.GNSS_DATA_FILE.COL_PUBLISHED_DATE);
               String file_url = results.getString       (Tables.GNSS_DATA_FILE.COL_FILE_URL);
               String file_type_name = results.getString (Tables.FILE_TYPE.COL_FILE_TYPE_NAME);

               // Check in the station's data, all types of file access permissions and limits. If accces not allowed for this file, do not show in GSAC reults (ie do not allow downloading).
               // and do not show this file in GSAC results sent to the user.
               Date now = new Date();
               int  sta_access_permission_id  = results.getInt(Tables.STATION.COL_ACCESS_PERMISSION_ID);
               int  sta_embargo_duration_hours  = results.getInt(Tables.STATION.COL_EMBARGO_DURATION_HOURS);
               Date sta_embargo_after_date  = results.getDate(Tables.STATION.COL_EMBARGO_AFTER_DATE);
               //    where access permission_id is  1 |  no public access
               if (1== sta_access_permission_id ) {
                  //System.err.println("       do not show this file (no access permission) : " + file_url);
                  continue;
                  }
               // b. if the time interval from data start time to 'now' (the value now = new Date()) is less than sta_embargo_duration_hours, bypass this file
               if (now.getTime() - data_start_time.getTime() < (sta_embargo_duration_hours * 3600.0 * 1000.0))  // getTime returns the time in milliseconds
                     { 
                     //System.err.println("  station restriction: do not show this file (inside station  embargo time) : " + file_url);
                     continue;
                     }
               // c. if the data start time is later than (after) the sta embargo_after_date, bypass this file 
               if  (sta_embargo_after_date != null && data_start_time.getTime() >= sta_embargo_after_date.getTime() )  
                     { 
                     //System.err.println("  station restriction: do not show this file (file is more recent that station's embargo date) : " + file_url);
                     continue;
                     }

               // Check in the file's gnss_data_file table rows, all types of file access permissions and limits. If fails, do not allow downloading,
               // and do not show this file in GSAC results sent to the user.
               int access_permission_id  = results.getInt(Tables.GNSS_DATA_FILE.COL_ACCESS_PERMISSION_ID);
               int embargo_duration_hours  = results.getInt(Tables.GNSS_DATA_FILE.COL_EMBARGO_DURATION_HOURS);
               Date embargo_after_date  = results.getDate(Tables.GNSS_DATA_FILE.COL_EMBARGO_AFTER_DATE);
               // 1.    where access_permission_id is  1 |  no public access
               if (1== access_permission_id ) {
                  //System.err.println("       do not show this file (no access permission) : " + file_url);
                  continue;
                  }
               // 2. if the time interval from data start time to 'now' (the value now = new Date()) is less than embargo_duration_hours, bypass this file
               if (now.getTime() - data_start_time.getTime() < (embargo_duration_hours * 3600.0 * 1000.0))  // getTime returns the time in milliseconds
                     { 
                     //System.err.println("       do not show this file (inside embargo time) : " + file_url);
                     continue;
                     }
               // 3. if the data start time is later than (after) the embargo_after_date, bypass this file 
               if  (embargo_after_date != null && data_start_time.getTime() >= embargo_after_date.getTime() ) 
                     { 
                     //System.err.println("       do not show this file (file is more recent that its embargo date) : " + file_url);
                     continue;
                     }

               // int  count = (request.getParameter("counter") == null) ? 0 : Integer.parseInt(request.getParameter("counter"));

               //System.err.println("   got site code, file id, and its file url         " + siteID +"  file type="+file_type_id+ "  url="+file_url ) ;
               //System.err.println("  FileManager:handleRequest(): got file site id " + siteID+"  file time range start " 
               //                        + data_start_time+" file time range end " + data_stop_time+"  file url : " + file_url);

               ResourceType rt = new ResourceType(TYPE_GNSS_OBSERVATION , " geodesy instrument data");
               if (file_type_name != null) {
                  rt = new ResourceType(TYPE_GNSS_OBSERVATION , file_type_name);
               }

               //GsacFile(String          repositoryId,  FileInfo fileInfo, GsacResource relatedResource, Date publishTime, Date startTime, Date endTime, ResourceType type)
               GsacFile fileInfo = new GsacFile(siteID, new FileInfo(file_url), null,                     published_date,   data_start_time, data_stop_time, rt);

               // collect all the GsacFile objects made; this is the array of results from the GSAC file seach:
               response.addResource(fileInfo);

               /*  this  busts file search big time // FIX code to check for exceeding max of how many results allowed
               if (!iter.countOK()) {
                    response.setExceededLimit();
                    break;
               }
               */

            } // end while 
            iter.close();
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        /* new from unavco gsac code 
        //If we have any site types specified then check if the user selected CAMPAIGN
        boolean lookForCampaignData = true;
        if (request.defined(ARG_SITE_TYPE)) {
            if ( !request.get(ARG_SITE_TYPE, new ArrayList()).contains(
                    UnavcoSiteManager.SITETYPE_CAMPAIGN)) {
                lookForCampaignData = false;
            }
        }
        if (lookForCampaignData) {
            // uggh What to do here? 
        }
        response.appendMessage(
            "Note: This GSAC service does not currently support access to campaign data<br>");
        */

        setSearchCriteriaMessage(response, msgBuff);

    } // end handleRequest


    /**
     * This takes the resource id that is used to identify files and creates a GsacFile object.
     * FIX bug: inadequate to find user-selected file by dates etc. Returns a GsacFile object for the station; for first file found for that station in the database.
     *  not the file clicked on.  and, How to get file times in here?
     *
     * Composes one particular "resource page"  when a user clicks on a particular item in the table of results, for files in this case, after a search.
     *  used to make an HTML page about one file. 
     *
     * But also note that the 'resource page' has no more information that in the table of results, for files in this case, after a search.  So why bother?
     *
     * @param resourceId file id
     *
     * @return GsacFile
     *
     * @throws Exception On badness
     */
    public GsacResource getResource(String resourceId) throws Exception {
        //System.err.println("   filemanager: GsacResource, resourceId=_" + resourceId +"_");

        List<String> tables = new ArrayList<String>();
        List<Clause> clauses = new ArrayList<Clause>();
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.GNSS_DATA_FILE.NAME);
        tables.add(Tables.FILE_TYPE.NAME);
        // clauses: WHERE this station is id-ed by its 4 char id:, and join other tables
        clauses.add(Clause.eq(Tables.STATION.COL_CODE_4CHAR_ID, resourceId));
        clauses.add(Clause.join(Tables.GNSS_DATA_FILE.COL_STATION_ID, Tables.STATION.COL_STATION_ID));
        clauses.add(Clause.join(Tables.FILE_TYPE.COL_FILE_TYPE_ID, Tables.GNSS_DATA_FILE.COL_FILE_TYPE_ID )) ;
        //  and for the mysql SELECT clause: make a list of what to get (row values returned):
        String cols=SqlUtil.comma(new String[]{
             Tables.GNSS_DATA_FILE.COL_STATION_ID,
             Tables.GNSS_DATA_FILE.COL_DATA_START_TIME,
             Tables.GNSS_DATA_FILE.COL_DATA_STOP_TIME,
             Tables.GNSS_DATA_FILE.COL_PUBLISHED_DATE,
             Tables.GNSS_DATA_FILE.COL_FILE_URL,
             Tables.GNSS_DATA_FILE.COL_FILE_TYPE_ID,
             Tables.STATION.COL_CODE_4CHAR_ID,
             Tables.STATION.COL_STATION_ID,
             Tables.FILE_TYPE.COL_FILE_TYPE_NAME
             });
        Statement statement =  getDatabaseManager().select(cols,  tables, Clause.and(clauses));
        try {
            ResultSet results = statement.getResultSet();
            while (results.next()) {
               int station_id  = results.getInt(Tables.GNSS_DATA_FILE.COL_STATION_ID);
               int file_type_id  = results.getInt(Tables.GNSS_DATA_FILE.COL_FILE_TYPE_ID);
               String file_type_name  = results.getString(Tables.FILE_TYPE.COL_FILE_TYPE_NAME);
               String file_url = results.getString(Tables.GNSS_DATA_FILE.COL_FILE_URL);
               String siteID = ""+station_id;
               Date data_start_time  = results.getDate(Tables.GNSS_DATA_FILE.COL_DATA_START_TIME);
               Date data_stop_time  = results.getDate(Tables.GNSS_DATA_FILE.COL_DATA_STOP_TIME);
               Date published_date  = results.getDate(Tables.GNSS_DATA_FILE.COL_PUBLISHED_DATE);

               ResourceType rt = new ResourceType(TYPE_GNSS_OBSERVATION , " geodesy instrument data");
               if (file_type_name != null) {
                  rt = new ResourceType(TYPE_GNSS_OBSERVATION , file_type_name);
               }
               //         GsacFile(String repositoryId, FileInfo fileInfo,      GsacResource relatedResource, Date publishTime, Date startTime, Date endTime, ResourceType type)
               GsacFile fileInfo = new GsacFile(resourceId, new FileInfo(file_url), null,                     published_date,    data_start_time,    data_stop_time, rt);
               return fileInfo;
            } // end while
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
    public PrototypeSiteManager getSiteManager() {
        return (PrototypeSiteManager) getRepository().getResourceManager( GsacSite.CLASS_SITE);
    }


}
