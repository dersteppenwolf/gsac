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
//import java.text.DateFormat;


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
 * @author  Jeff McWhirter 2011  non-functional template file without any code for any database variables.
 * @author  S K Wier Nov. 6;14, 2013
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
              // LLOK could also search on revision_time in gsac prototype database

              // search on file size.  Not now regarded as useful. Nov. 2013.
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

        // make SQL query(ies) to select from the columns (fields) of rows in the database, with  query clauses generated here.

        //  Add entry box for user to select by station 4 character id
        addStringSearch(request, ARG_SITECODE, ARG_SITECODE_SEARCHTYPE, msgBuff, "Site Code", Tables.STATION.COL_CODE_4CHAR_ID, clauses);
        
        // make query clause for the  file type
        if (request.defined(GsacArgs.ARG_FILE_TYPE)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacArgs.ARG_FILE_TYPE);
            clauses.add( Clause.or( Clause.makeStringClauses( Tables.FILE_TYPE.COL_FILE_TYPE_NAME, values)));
        }
        
        // get values of the data date range requested by the user, from the input from web search form / API:
        Date[] dataDateRange = request.getDateRange(ARG_FILE_DATADATE_FROM, ARG_FILE_DATADATE_TO, null, null);

        if (dataDateRange[0] != null) {
            // wrangle the data start time into a format you can use in a SQL query
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataDateRange[0]);
            java.sql.Date sqlStartDate = new java.sql.Date(cal.getTimeInMillis());
            clauses.add(Clause.ge(Tables.GNSS_DATA_FILE.COL_DATA_START_TIME, sqlStartDate));
            // time of data must be inside some one receiver session
            clauses.add(Clause.le(Tables.RECEIVER_SESSION.COL_RECEIVER_INSTALLED_DATE, sqlStartDate));
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
            // time of data must be inside some one receiver session
            clauses.add(Clause.le(Tables.RECEIVER_SESSION.COL_RECEIVER_INSTALLED_DATE, sqlEndDate));
            appendSearchCriteria(msgBuff, "Data date&lt;=", "" + format(dataDateRange[1]));
        }

        // to get file info, join these db tables:
        // sql select needs to join row pairs from these tables, connected by these id values. (search rows in these tables with these shared values):
        clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.GNSS_DATA_FILE.COL_STATION_ID )) ;
        clauses.add(Clause.join(Tables.RECEIVER_SESSION.COL_STATION_ID, Tables.GNSS_DATA_FILE.COL_STATION_ID )) ;
        clauses.add(Clause.join(Tables.FILE_TYPE.COL_FILE_TYPE_ID, Tables.GNSS_DATA_FILE.COL_FILE_TYPE_ID )) ;
 
        Clause mainClause = Clause.and(clauses);

        // for the SQl select clause: WHAT to select (row values returned):
        String cols=SqlUtil.comma(new String[]{
             Tables.GNSS_DATA_FILE.COL_STATION_ID,
             Tables.GNSS_DATA_FILE.COL_FILE_TYPE_ID,
             Tables.GNSS_DATA_FILE.COL_DATA_START_TIME,
             Tables.GNSS_DATA_FILE.COL_DATA_STOP_TIME,
             Tables.GNSS_DATA_FILE.COL_PUBLISHED_DATE,
             Tables.GNSS_DATA_FILE.COL_FILE_URL,
             Tables.GNSS_DATA_FILE.COL_FILE_SIZE,
             Tables.GNSS_DATA_FILE.COL_FILE_MD5,
             Tables.GNSS_DATA_FILE.COL_ACCESS_PERMISSION_ID,
             Tables.GNSS_DATA_FILE.COL_EMBARGO_DURATION_HOURS,
             Tables.GNSS_DATA_FILE.COL_EMBARGO_AFTER_DATE,

             Tables.STATION.COL_CODE_4CHAR_ID,
             Tables.STATION.COL_ACCESS_PERMISSION_ID,
             Tables.STATION.COL_EMBARGO_DURATION_HOURS,
             Tables.STATION.COL_EMBARGO_AFTER_DATE,

             Tables.FILE_TYPE.COL_FILE_TYPE_ID,
             Tables.FILE_TYPE.COL_FILE_TYPE_NAME,             // last item has no final ,
             Tables.RECEIVER_SESSION.COL_RECEIVER_SAMPLE_INTERVAL
             });

        //  for the sql select FROM clause, which tables to select from
        List<String> tables = new ArrayList<String>();
        tables.add(Tables.GNSS_DATA_FILE.NAME);
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.FILE_TYPE.NAME);
        tables.add(Tables.RECEIVER_SESSION.NAME);

        //System.err.println("  FileHandler:handleRequest(): select FROM "+tables+" WHERE "+mainClause);

        // do sql query type "select distinct " columns:
        String distinctCols= getDatabaseManager().distinct(cols);   // adds " distinct " before the list of columns
        Statement statement = getDatabaseManager().select(distinctCols,  tables,  mainClause, " order by " + Tables.GNSS_DATA_FILE.COL_DATA_START_TIME+", "+Tables.STATION.COL_CODE_4CHAR_ID, -1);

        try {
            ResultSet results = null;

            // get each line of values returned from the sql select command
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            //  or SqlUtil.Iterator iter = SqlUtil.getIterator(statement, request.getOffset(), request.getLimit());

            // process each line (row) returned by the select query: 
            while ((results = iter.getNext()) != null) {
               // get an individual file's  values from each single row returned in the array "results"
               String siteID = results.getString(Tables.STATION.COL_CODE_4CHAR_ID);
               int station_id  = results.getInt(Tables.GNSS_DATA_FILE.COL_STATION_ID);
               int file_type_id  = results.getInt(Tables.GNSS_DATA_FILE.COL_FILE_TYPE_ID);

               String start_time  = results.getString(Tables.GNSS_DATA_FILE.COL_DATA_START_TIME);
               String stop_time  = results.getString(Tables.GNSS_DATA_FILE.COL_DATA_STOP_TIME);
               String pub_time  = results.getString(Tables.GNSS_DATA_FILE.COL_PUBLISHED_DATE) ;
               // make sure they are in format "yyyy-MM-dd HH:mm:ss" - LOOK may need more code here
               start_time= start_time.substring(0,19);
               stop_time= stop_time.substring(0,19);
               pub_time = pub_time.substring(0,19);
               Date data_start_time= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(start_time);
               Date data_stop_time= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stop_time);
               Date published_date= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(pub_time);

               String file_url = results.getString       (Tables.GNSS_DATA_FILE.COL_FILE_URL);
               String file_md5 = results.getString       (Tables.GNSS_DATA_FILE.COL_FILE_MD5);
               long file_size= results.getInt          (Tables.GNSS_DATA_FILE.COL_FILE_SIZE);
               String file_type_name = results.getString (Tables.FILE_TYPE.COL_FILE_TYPE_NAME);
               float sample_interval = 9999.0f; // static value for test until get from db 
               sample_interval = results.getFloat (Tables.RECEIVER_SESSION.COL_RECEIVER_SAMPLE_INTERVAL); 

               //System.err.println("  file info for site id " + siteID+" times "+start_time+" to" + stop_time);
               //System.err.println("  FileHandler:handleRequest(): file for site " + siteID+"  url = "+file_url);

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

               // OK this file may be shown to user for downloading

               //int count = (request.getParameter("counter") == null) ? 0 : Integer.parseInt(request.getParameter("counter"));

               ResourceType rt = new ResourceType(TYPE_GNSS_OBSERVATION , " geodesy instrument data");
               if (file_type_name != null) {
                  rt = new ResourceType(TYPE_GNSS_OBSERVATION , file_type_name);
               }

               // make and populate a FileInfo object for this file, used by other parts of GSAC for output handling.
               FileInfo fileinfo = new FileInfo(file_url);
               String sizestr = ""+file_size;
               String sistr = ""+ sample_interval;
               fileinfo.setMd5(file_md5);
               fileinfo.setFileSize(file_size);
               fileinfo.setSampleInterval(sample_interval);

               // make and populate a GsacFile object for this file, used by other parts of GSAC for output handling.
               GsacFile gsacFile = new GsacFile(siteID, fileinfo, null, published_date, data_start_time, data_stop_time, rt);
               // from Gsac File(String          repositoryId,  FileInfo fileInfo, GsacResource relatedResource, Date publishTime, Date startTime, Date endTime, ResourceType type)

               // collect all the GsacFile objects made; this is the array of results from the GSAC file search:
               response.addResource(gsacFile);

            } // end while  loop on sql query rows (file info) returned
            iter.close();
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        setSearchCriteriaMessage(response, msgBuff);

    } // end handleRequest


    /**
     * original comment: "This takes the resource id that is used to identify files and creates a GsacFile object."
     *
     * Composes one particular HTML "resource page"  when a user clicks on a particular item in the table of results, for files in this case, after a search.
     *
     * FIX bug:  input arg is inadequate to find user-selected file by dates etc. Returns a GsacFile object for the station; for first file found for that station in the database.
     *  not the file clicked on. and, how to get file times in here?
     * But note that the 'resource page' has no more information that in the table of results, for files in this case, after a search.  So why bother?
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
               // LOOK the following are defective because java.sql.Date objects "do not have a time component."  Geodesy needs data times to better resolution than 24 hours.
               Date data_start_time  = results.getDate(Tables.GNSS_DATA_FILE.COL_DATA_START_TIME);
               Date data_stop_time  = results.getDate(Tables.GNSS_DATA_FILE.COL_DATA_STOP_TIME);
               Date published_date  = results.getDate(Tables.GNSS_DATA_FILE.COL_PUBLISHED_DATE);

               ResourceType rt = new ResourceType(TYPE_GNSS_OBSERVATION , " geodesy instrument data");
               if (file_type_name != null) {
                  rt = new ResourceType(TYPE_GNSS_OBSERVATION , file_type_name);
               }
               //         Gsac File(String repositoryId, FileInfo fileInfo,      GsacResource relatedResource, Date publishTime, Date startTime, Date endTime, ResourceType type)
               GsacFile gsacFile = new GsacFile(resourceId, new FileInfo(file_url), null,                     published_date,    data_start_time,    data_stop_time, rt);
               return gsacFile;
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
