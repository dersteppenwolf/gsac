/*
 * Copyright 2015 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

package org.dataworks.gsac;
import  org.dataworks.gsac.database.*;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;

// The ramadda imports refer to the rammadda jar file included with GSAC,
// not necessarily the latest thing from rammadda.org
import org.gsac.gsl.ramadda.sql.Clause;
import org.gsac.gsl.ramadda.sql.SqlUtil;

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
 * 
 * For a particular GSAC, code to handle data file searches ands download information, based on the database read by GSAC about data holdings.
 * 
 * For the Dataworks GSAC (Geodesy Seamless Archive). 
 * 
 * A GSAC FileManager class composes what file-related items are provided for SEARCHES for files using a GSAC API or web site forms.
 * A GSAC FileManager class composes what items are provided returned in the RESULTS when a GSAC file search finds something.
 *
 * This FileManager.java uses the Dataworks GSAC database.
 *
 * @author  Jeff McWhirter 2011  minimal function template file (made from gsac/fsl/template/Filemanager.java) without any code for using a database.
 * @author  S K Wier 11 August 2014, a revised and simplified version of the GSAC PrototypeFileManager.java. 
 */
public class DataworksFileManager extends FileManager {

    public static final String TYPE_GNSS_OBSERVATION = "geodesy.data";

    /**
     * ctor
     *
     * @param repository the repository
     */
    public DataworksFileManager(DataworksRepository repository) {
        super(repository);

    }

    /**
     *  Define and enable what file-related items are are offered for search choices (database queries) for geoscience data files to download from this particular data repository.  
     *  This sets the search forms on the web site file search page.
     *
     *  In GSAC, "capabilities" are things to search (query) on. 
     *
     * This method is called only once, at GSAC server start-up.  Must restart the GSAC server to find new items only detected here, such as gnss file types.
     *
     * @return  List of GSAC "Capabilities"  objects
     */
    public List<Capability> doGetQueryCapabilities() {

      // addDefaultCapabilities(capabilities); // why not used here?

      try {

        List<Capability> capabilities = new ArrayList<Capability>();
        Capability   cap;
        String [] values; 

        // Find the types of files in this data archive ( see also "if (request.defined(GsacArgs.ARG_FILE_TYPE)) {" below in another method.)
        // Note this code has to read all the file entries in the database.
        //    (originally, code here found *all* the possible file type names in the database file_type table, many types not in most data centers; which is merely misleading)
        int gpsfcnt=0;
        ResultSet results;
        ArrayList<String> avalues = new ArrayList<String>();
        List<Clause> clauses = new ArrayList<Clause>();
        //  WHERE 
        clauses.add(Clause.join(Tables.DATAFILE.COL_DATAFILE_TYPE_ID, Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_ID));
        //  SELECT what column values to find
        String cols=SqlUtil.comma(new String[]{Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_NAME});
        //  FROM   
        List<String> tables = new ArrayList<String>();
        tables.add(Tables.DATAFILE.NAME);
        tables.add(Tables.DATAFILE_TYPE.NAME);
        Statement statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           //System.err.println("  queried db table DATAFILE for all file type names" ) ;
           // process each line in results of db query  
           while ((results = iter.getNext()) != null) {
               String ftype= results.getString(Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_NAME);
               gpsfcnt +=1;
               // save distinct values
               int notfound=1;
               for (int vi= 0; vi<avalues.size(); vi+=1 ) {
                  if ( avalues.get(vi).equals(ftype) ) {
                         notfound=0;
                         break;
                         }
                   }
                   if (notfound==1) {
                         avalues.add(ftype);
                         //System.err.println("  this data center has GNSS data files of type '" + ftype +"'" ) ;
                   }
               }
        } finally {
           getDatabaseManager().closeAndReleaseConnection(statement);
        }
        //System.err.println("  GSAC: there are "+gpsfcnt+" types of instrumental data files in the database." ) ;
        String[] itemArray = new String[avalues.size()];
        values = avalues.toArray(itemArray);
        // sort by alphabet: Arrays.sort(values); no, just leave them in order found, more likely commom ones show earlier that way, since you scanned all the files.

        // the following search choices do not need or have any pre-defined values or enumerated lists of allowed choices:
        Capability[] dflt = { 

              // parms like "ARG_FILE_..." are declared in core code GsacArgs.java.

              initCapability(new Capability(ARG_FILE_DATADATE,         "Data Date Range",         Capability.TYPE_DATERANGE), "File Query", "Date the data was collected"),

              // search on "Publish Date" is when a repository made a file available *most recently*.  This is used to look for changed / revised/ corrected files.
              initCapability(new Capability(ARG_FILE_PUBLISHDATE,       "Publish Date",           Capability.TYPE_DATERANGE), "File Query", 
                              "Date when this file was first published to the repository"),
              // this loads the user's choices of the range values of ARG_FILE_PUBLISHDATE_FROM, ARG_FILE_PUBLISHDATE_TO used later to seach on


              initCapability(new Capability(GsacArgs.ARG_FILE_TYPE,    "File Type", values, true, Capability.TYPE_FILETYPE ), "File Query", "Data file type" ),

              // Note, for a case using Capability.TYPE_NUMBERRANGE, as for FILESIZE, 
              // the special parm name, ARG_FILE_SAMPLEINT, declared in GsacArgs.java, 
              //    must have two more related corresponding parm names there with magic name extensions .max and .min
              // Note the Capability.TYPE_NUMBERRANGE appears to permit only integer numbers, not real numbers.  FIX this code to allow fraction of seconds:
              initCapability(new Capability(ARG_FILE_SAMPLEINT,  "Data Sampling Interval (s)",     Capability.TYPE_NUMBERRANGE), "File Query", 
                   "instrument data sampling interval") 

              // Could also search on file size.  Not now regarded as useful. No-one has ever asked for this.
              //initCapability(cap = new Capability(ARG_FILE_FILESIZE,  "File Size", Capability.TYPE_NUMBERRANGE), "File Query", "File size") 
              // can use with file size searches: cap.setSuffixLabel("&nbsp;(bytes)");
        };

        for (Capability capability : dflt) {
            capabilities.add(capability);
        }

        // Also add all the station-related search choices into the file search web page form, so you can select files from particular sites
        // (gets choices from the related SiteManager class)
        capabilities.addAll(getSiteManager().doGetQueryCapabilities());

        return capabilities;

      } catch (Exception exc) {
            throw new RuntimeException(exc);
      }

    }

    /**
     * Handle the search request.   
     *  Compose a database query or select clause for the requests' values, and make the select query on the GSAC db.
     *
     *  Does the database search for data files as specified by the user's search for files in the web site forms or via the API, (contained in input object "request")
     *  and puts an array of the results, with one or more GsacFile objects, into the container object "GsacResponse response."
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
        addStringSearch(request, ARG_SITECODE, ARG_SITECODE_SEARCHTYPE, msgBuff, "Site Code", Tables.STATION.COL_FOUR_CHAR_NAME, clauses);
        
        // FROM SiteManager: 
        String latCol  = Tables.STATION.COL_LATITUDE_NORTH;
        String lonCol  = Tables.STATION.COL_LONGITUDE_EAST;
             // query for the station's name string  
        if (request.defined(ARG_SITE_NAME)) {
            addStringSearch(request, ARG_SITE_NAME, " ", msgBuff, "Site Name", Tables.STATION.COL_STATION_NAME, clauses);
            //System.err.println("   SiteManager: query for name " + ARG_SITE_NAME ) ;
        }
             // query for the station's  location inside a latitude-longitude box
        if (request.defined(ARG_NORTH)) {
            clauses.add( Clause.le( latCol, request.get(ARG_NORTH, 0.0)));
            appendSearchCriteria(msgBuff, "north&lt;=", "" + request.get(ARG_NORTH, 0.0));
        }
        if (request.defined(ARG_SOUTH)) {
            clauses.add( Clause.ge( latCol, request.get(ARG_SOUTH, 0.0)));
            appendSearchCriteria(msgBuff, "south&gt;=", "" + request.get(ARG_SOUTH, 0.0));
        }
        if (request.defined(ARG_EAST)) {
            clauses.add( Clause.le( lonCol, request.get(ARG_EAST, 0.0)));
            appendSearchCriteria(msgBuff, "east&lt;=", "" + request.get(ARG_EAST, 0.0));
        }
        if (request.defined(ARG_WEST)) {
            clauses.add( Clause.ge( lonCol, request.get(ARG_WEST, 0.0)));
            appendSearchCriteria(msgBuff, "west&gt;=", "" + request.get(ARG_WEST, 0.0));
        }
        // end FROM SiteManager

        /*
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
        */

        // make query clause for the file type ; search with OR on the list of file type names in 'values':
        if (request.defined(GsacArgs.ARG_FILE_TYPE)) {
             List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacArgs.ARG_FILE_TYPE);
             //System.err.println("  FileHandler:handleRequest(): search on file types "+ values.toString() );
            clauses.add( Clause.or( Clause.makeStringClauses( Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_NAME, values)));
        }

        //float intrange1 = Float.parseFloat(stri); // java atof

        // sample interval code: // FIX handle case if database has null values for DATAFILE.COL_SAMPLE_INTERVAL
        if (request.defined(ARG_FILE_SAMPLEINT_MAX)) {
            float intrange1 = request.get(ARG_FILE_SAMPLEINT_MIN, 0);
            float intrange2 = request.get(ARG_FILE_SAMPLEINT_MAX,0);
            //System.err.println("  FileHandler:handleRequest(): search on sample int from "+ intrange1 +"  to "+ intrange2);
            clauses.add(Clause.ge(Tables.DATAFILE.COL_SAMPLE_INTERVAL, intrange1));
            clauses.add(Clause.le(Tables.DATAFILE.COL_SAMPLE_INTERVAL, intrange2));
        }
            
        // get values of the data date range requested by the user, from the input from web search form / API:
        Date[] dataDateRange = request.getDateRange(ARG_FILE_DATADATE_FROM, ARG_FILE_DATADATE_TO, null, null);
        if (dataDateRange[0] != null) {
            // wrangle the data start time into a format you can use in a SQL query
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataDateRange[0]);
            java.sql.Date sqlStartDate = new java.sql.Date(cal.getTimeInMillis());
            clauses.add(Clause.ge(Tables.DATAFILE.COL_DATAFILE_START_TIME, sqlStartDate));
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
            clauses.add(Clause.le(Tables.DATAFILE.COL_DATAFILE_STOP_TIME, sqlEndDate));
            appendSearchCriteria(msgBuff, "Data date&lt;=", "" + format(dataDateRange[1]));
        }

        // new 6 may
        // use the data range requested by the user, from the input from web search form / API, to search on the "publish time" of data files:
        Date[] usersDateRange = request.getDateRange(ARG_FILE_PUBLISHDATE_FROM, ARG_FILE_PUBLISHDATE_TO, null, null);
        //                                                pubDateRange[0]
        // to compare the pub date to the final date in range of interest: pub date must be >=  [0], the  1st in users date range
        if (usersDateRange[0] != null) {
            // wrangle the final date into a format you can use in a SQL query
            Calendar cal = Calendar.getInstance();
            cal.setTime(usersDateRange[0]);
            java.sql.Date testDate = new java.sql.Date(cal.getTimeInMillis());
            clauses.add(Clause.ge(Tables.DATAFILE.COL_PUBLISHED_TIME, testDate));
            appendSearchCriteria(msgBuff, "Pub. date&gt;=", "" + format(usersDateRange[0]));
        }
        if (usersDateRange[1] != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(usersDateRange[1]);
            // do this to NOT shift one day earlier, both user dates are the same.  do   3 lines:
            cal.add(Calendar.HOUR, 23);
            cal.add(Calendar.MINUTE, 59);
            cal.add(Calendar.SECOND, 59);
            java.sql.Date sqlEndDate = new java.sql.Date(cal.getTimeInMillis());
            clauses.add(Clause.le(Tables.DATAFILE.COL_PUBLISHED_TIME, sqlEndDate));
            appendSearchCriteria(msgBuff, "Pub. date&le;=", "" + format(usersDateRange[1]));
        }
        // end new


        // to get file info, join these db tables:
        // sql select needs to join row pairs from these tables, connected by these id values. (search rows in these tables with these shared values):
        clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.DATAFILE.COL_STATION_ID )) ;
        // // from prototype gsac clauses.add(Clause.join(Tables.RECEIVER_SESSION.COL_STATION_ID, Tables.DATAFILE.COL_STATION_ID )) ;
        clauses.add(Clause.join(Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_ID, Tables.DATAFILE.COL_DATAFILE_TYPE_ID )) ;
 
        Clause mainClause = Clause.and(clauses);

        // for the SQl select clause: WHAT to select (row values returned):
        String cols=SqlUtil.comma(new String[]{
             Tables.DATAFILE.COL_STATION_ID,
             Tables.DATAFILE.COL_DATAFILE_TYPE_ID,
             Tables.DATAFILE.COL_SAMPLE_INTERVAL, 
             Tables.DATAFILE.COL_DATAFILE_START_TIME,
             Tables.DATAFILE.COL_DATAFILE_STOP_TIME,
             Tables.DATAFILE.COL_PUBLISHED_TIME,
             Tables.DATAFILE.COL_SIZE_BYTES,
             Tables.DATAFILE.COL_MD5,
             Tables.DATAFILE.COL_URL_PATH,
             Tables.DATAFILE.COL_DATAFILE_NAME,
             // not yet in dataworks database  Tables.DATAFILE.COL_ACCESS_ID,
             // not yet in dataworks database  Tables.DATAFILE.COL_EMBARGO_DURATION_HOURS,
             // not yet in dataworks database  Tables.DATAFILE.COL_EMBARGO_AFTER_DATE,
             Tables.STATION.COL_FOUR_CHAR_NAME,
             Tables.STATION.COL_ACCESS_ID,
             // not yet in dataworks database  Tables.STATION.COL_EMBARGO_DURATION_HOURS,
             // not yet in dataworks database  Tables.STATION.COL_EMBARGO_AFTER_DATE,
             Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_ID,
             Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_NAME             // last item has no final ,
             });

        //  for the sql select FROM clause, which tables to select from
        List<String> tables = new ArrayList<String>();
        tables.add(Tables.DATAFILE.NAME);
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.DATAFILE_TYPE.NAME);

        //System.err.println("  FileHandler:handleRequest(): select FROM "+tables+" WHERE "+mainClause);

        // do sql query type "select distinct " columns:
        String distinctCols= getDatabaseManager().distinct(cols);   // adds " distinct " before the list of columns

        Statement statement = getDatabaseManager().select(distinctCols,  tables,  mainClause, " order by " + Tables.DATAFILE.COL_DATAFILE_START_TIME+", "+Tables.STATION.COL_FOUR_CHAR_NAME, -1);

        try {
            ResultSet results = null;

            // get each line of values returned from the sql select command
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            //  or SqlUtil.Iterator iter = SqlUtil.getIterator(statement, request.getOffset(), request.getLimit());

            // process each line (row) returned by the select query: 
            while ((results = iter.getNext()) != null) {
               // get an individual file's  values from each single row returned in the array "results"
               String siteID = results.getString     (Tables.STATION.COL_FOUR_CHAR_NAME);
               int station_id  = results.getInt      (Tables.DATAFILE.COL_STATION_ID);
               int file_type_id  = results.getInt    (Tables.DATAFILE.COL_DATAFILE_TYPE_ID);

               String start_time  = results.getString(Tables.DATAFILE.COL_DATAFILE_START_TIME);
               String stop_time  = results.getString (Tables.DATAFILE.COL_DATAFILE_STOP_TIME);
               String pub_time  = results.getString  (Tables.DATAFILE.COL_PUBLISHED_TIME) ;

               // make sure they are in format "yyyy-MM-dd HH:mm:ss" - LOOK may need more code here
               start_time= start_time.substring(0,19);
               stop_time= stop_time.substring(0,19);
               pub_time = pub_time.substring(0,19);
               Date data_start_time= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(start_time);
               Date data_stop_time= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stop_time);
               Date published_date= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(pub_time);

               String file_md5 = results.getString (Tables.DATAFILE.COL_MD5);
               long file_size= results.getInt      (Tables.DATAFILE.COL_SIZE_BYTES);

               String file_url = results.getString       (Tables.DATAFILE.COL_URL_PATH);
               String file_url_filename = results.getString  (Tables.DATAFILE.COL_DATAFILE_NAME);

               /* from more complex prototype GSAC database, here is a  way to create path urls from simple parts, so you need not change all if a part changes:
               // if this database row does not supply the complete FILE_URL, try to compose it from all the parts of a complete url found in the same database row
               if (file_url==null || file_url.length()< 13 )   // error check for say ftp://a.b.c/d has length of 13
               {
                   String file_url_protocol = results.getString  (Tables.DATAFILE.COL_URL_PATH_PROTOCOL);
                   String file_url_ip_domain = results.getString (Tables.DATAFILE.COL_URL_PATH_IP_DOMAIN);
                   String file_url_folders = results.getString   (Tables.DATAFILE.COL_URL_PATH_FOLDERS);
                   String file_url_filename = results.getString  (Tables.DATAFILE.COL_DATAFILE_NAME);

                   file_url =file_url_protocol + "://" + file_url_ip_domain + file_url_folders + file_url_filename;
               }
               */

               String file_type_name = results.getString (Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_NAME);

               String sample_interval = "0.0"; 
               sample_interval = results.getString (Tables.DATAFILE.COL_SAMPLE_INTERVAL);
               if (null == sample_interval) {
                  sample_interval = "0.0"; // some output formats change this to 'unknown'
                  //System.err.println("  file sample interval is null");
                  //sample_interval = results.getString (Tables.RECEIVER_SESSION.COL_RECEIVER_SAMPLE_INTERVAL); 
                  //if (null == sample_interval) {
                  //   sample_interval = "0.0"; // some output formats change this to 'unknown'
                  //}
               }
               //System.err.println("   sample interval ="+sample_interval+"_");

               // Check in the station's data, all types of file access permissions and limits. 
               // If acccess not allowed for this file, do not show in GSAC results (ie do not allow downloading).
               // and do not show this file in GSAC results sent to the user.

               Date now = new Date();

               int  sta_access_permission_id  = results.getInt(Tables.STATION.COL_ACCESS_ID);

               /*
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
               */

               /* from more complex prototype GSAC:
               // Check in the file's gnss_data_file table rows, all types of file access permissions and limits. If fails, do not allow downloading,
               // and do not show this file in GSAC results sent to the user.
               int access_permission_id  = results.getInt(Tables.DATAFILE.COL_ACCESS_ID);
               int embargo_duration_hours  = results.getInt(Tables.DATAFILE.COL_EMBARGO_DURATION_HOURS);
               Date embargo_after_date  = results.getDate(Tables.DATAFILE.COL_EMBARGO_AFTER_DATE);
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
               */

               // OK this file may be shown to user for downloading

               ResourceType rt = new ResourceType(TYPE_GNSS_OBSERVATION , " geodesy instrument data");
               if (file_type_name != null) {
                  rt = new ResourceType(TYPE_GNSS_OBSERVATION , file_type_name);
               }

               // make and populate a FileInfo object for this file, used by other parts of GSAC for output handling.
               FileInfo fileinfo = new FileInfo(file_url);
               String sizestr = ""+file_size;
               fileinfo.setMd5(file_md5);
               fileinfo.setFileSize(file_size);
               //String sampintstr = ""+ sample_interval;
               float sampint = Float.parseFloat(sample_interval);
               fileinfo.setSampleInterval(sampint);

               // make and populate a GsacFile object for this file, used by other parts of GSAC for output handling.
               GsacFile gsacFile = new GsacFile(siteID, fileinfo,                                   null, published_date,  data_start_time, data_stop_time, rt);
               // from Gsac File(String   repositoryId,  FileInfo fileInfo, GsacResource relatedResource, Date publishTime, Date startTime, Date endTime,   ResourceType type)

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
     * original complete documentation for this method: "This takes the resource id that is used to identify files and creates a GsacFile object."
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
        tables.add(Tables.DATAFILE.NAME);
        tables.add(Tables.DATAFILE_TYPE.NAME);
        // clauses: WHERE this station is id-ed by its 4 char id:, and join other tables
        clauses.add(Clause.eq(Tables.STATION.COL_FOUR_CHAR_NAME, resourceId));
        clauses.add(Clause.join(Tables.DATAFILE.COL_STATION_ID, Tables.STATION.COL_STATION_ID));
        clauses.add(Clause.join(Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_ID, Tables.DATAFILE.COL_DATAFILE_TYPE_ID )) ;
        //  and for the mysql SELECT clause: make a list of what to get (row values returned):
        String cols=SqlUtil.comma(new String[]{
             Tables.DATAFILE.COL_STATION_ID,
             Tables.DATAFILE.COL_DATAFILE_START_TIME,
             Tables.DATAFILE.COL_DATAFILE_STOP_TIME,
             Tables.DATAFILE.COL_PUBLISHED_TIME,
             Tables.DATAFILE.COL_URL_PATH,
             Tables.DATAFILE.COL_DATAFILE_TYPE_ID,
             Tables.STATION.COL_FOUR_CHAR_NAME,
             Tables.STATION.COL_STATION_ID,
             Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_NAME
             });
        Statement statement =  getDatabaseManager().select(cols,  tables, Clause.and(clauses));
        try {
            ResultSet results = statement.getResultSet();
            while (results.next()) {
               int station_id  = results.getInt(Tables.DATAFILE.COL_STATION_ID);
               int file_type_id  = results.getInt(Tables.DATAFILE.COL_DATAFILE_TYPE_ID);
               String file_type_name  = results.getString(Tables.DATAFILE_TYPE.COL_DATAFILE_TYPE_NAME);
               String file_url = results.getString(Tables.DATAFILE.COL_URL_PATH);
               String siteID = ""+station_id;
               // LOOK the following are perhaps somewhat defective because java.sql.Date objects 
               // "do not have a time component."  Geodesy needs data times to better resolution than 24 hours.
               Date data_start_time  = results.getDate(Tables.DATAFILE.COL_DATAFILE_START_TIME);
               Date data_stop_time  = results.getDate(Tables.DATAFILE.COL_DATAFILE_STOP_TIME);
               Date published_date  = results.getDate(Tables.DATAFILE.COL_PUBLISHED_TIME);

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
     * Get the related Sitemanager for this FileManager, used for added site queries (searches) into the file search page, so
     * the user can select files by indicating one or more sites.
     *
     * @return sitemanager
     */
    public DataworksSiteManager getSiteManager() {
        return (DataworksSiteManager) getRepository().getResourceManager( GsacSite.CLASS_SITE);
    }


}
