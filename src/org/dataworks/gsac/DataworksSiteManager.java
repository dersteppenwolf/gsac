/*
 * Copyright 2015-2016 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
import org.gsac.gsl.util.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.metadata.gnss.*;
import org.gsac.gsl.output.HtmlOutputHandler;
import org.gsac.gsl.ramadda.sql.Clause;
import org.gsac.gsl.ramadda.sql.SqlUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.HashSet;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;

/**
 * The GSAC SiteManager classes handle all of a GSAC repository's site(station)-related requests.  
 *
 * The base class is in gsac/gsl/SiteManager.java.  Each GSAC application instance also has its own site manager, such as src/org/dataworks/gsac/DataworksSiteManager.java.
 *
 * For the Dataworks GSAC.
 *
 * Code in this SiteManager class is highly dependent on the database design.  This code is designed to work with the corresponding UNAVCO GSAC Dataworks database for MySQL.
 * This code uses database names for tables and columns in database tables, coded for GSAC Java code in src/org/dataworks/gsac/database/Tables.java
 *
 * The *SiteManager.java is one major part of making a GSAC server instance; it allows a particular GSAC to query its database, and handles the results from queries:
 *
 * - what metadata may be queried on, that is used for searches or selections, in this GSAC repository (see method doGetQueryCapabilities below)
 *   either by the web page forms or via the API URL arguments, 
 *
 * - how to query the database for such request, make SQL phrases, (see method getResourceClauses below), which is called by GSAC/gsac-code/src/org/gsac/gsl/GsacResourceManager.java
 *
 * - how to package up the results from the query (method makeResource below) into a java object for further use, such as for the HTML pages of
 *   search results on the GSAC web site, and the items in other result formats like SINEX.
 *
 * See NETWORKS for alternate code to allow two or more network names per site; this code is commented out to match the initial Dataworks specification of one network per site.
 * The database schema is simpler for more than one netowrk.  More than one network per station may be enable using this code.
 * 
 * @author  Jeff McWhirter, 2011. A short template for any SiteManager.java, without any code for querying a database.
 * @author  S K Wier, UNAVCO; DataworksSiteManager.java, 12 Aug 2014 to (at least) 17 June 2015
 * @author  S K Wier, UNAVCO; DataworksSiteManager.java, 26 Aug 2015 for new core GSAC functions and values, and to suit the NCEDC & PANGA needs too.
 */
public class DataworksSiteManager extends SiteManager {

    /**
     * ctor
     *
     * @param repository the repository
     */
    public DataworksSiteManager(DataworksRepository repository) {
        super(repository);
    }


    /**
     *   This is the main entry point for handling queries.
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        long t1 = System.currentTimeMillis();

        super.handleRequest(request, response);
        // which is in GSAC/gsac-code/src/org/gsac/gsl/GsacResourceManager.java

        int cct= getDatabaseManager().getConnectionCount();
        System.err.println ("GSAC: local DataworksSiteManager; now have " + cct +" db connections" );

        long t2 = System.currentTimeMillis();
        System.err.println ("GSAC: local DataworksSiteManager:handleRequest() took "+ (t2-t1)+ " ms");

    }

    /** do we get the data ranges */
    private boolean doDateRanges = true;


    /**
     * Create the site search "capabilities", which are all the items to offer to the user to search for sites(stations), in web page forms and in API.  
     *
     * A GSAC search item (Java object) is a "Capability."  Here is where you implement items to use in site queries from this database, 
     * searches from either web site or url api args)
     *
     * A capability here which for example is tied to the value GsacExtArgs.ARG_ANTENNA has corresponding code
     * in the method get ResourceClauses which creates a query with it when user does so.
     *
     * (Perhaps the call to this method goes before makeCapabilities call, so regular site search form appears before advanced search?)
     *
     * Must restart the GSAC server to find new field values in the database only read once here at startup, such as gnss file types.
     * This method is called only once, at GSAC server start-up.  
     * LOOK - this method appears to be called twice at server start-up.
     *
     * CHANGEME if you have other things to search on, or different db table or field names for the items to search on.
     *
     * @return site search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        // debug   System.err.println("   SiteManager:  doGetQueryCapabilities ") ;
        try {

            // order of adding to capabilities here specifies order on html site search page
            List<Capability> capabilities = new ArrayList<Capability>();


            // Essential site search items: 

            String help = HtmlOutputHandler.stringSearchHelp;  /* some mouse over help text */
            // search on site code, the 4 character ID.  Users may use regular expressions such as AB* or P12*. Hover cursor on any GSAC entry box.
            // args:( "web page label" capab type ), capab group name,   mouse over help text,  other help text)
            Capability siteCode = initCapability( new Capability(ARG_SITE_CODE, "Code (4 character ID)", Capability.TYPE_STRING), 
                      CAPABILITY_GROUP_SITE_QUERY, "Code (4 character ID) of the station", "Code (4 character ID) of the station. " + help);
            siteCode.setBrowse(true);  /*  which apparently adds these searches to the GSAC web site Browse form */
            capabilities.add(siteCode);

            // cursor hover text: search with site full name or partial name
            help="Full name of the site, such as Marshall, or part or name plus wildcard(*) such as Mar*";
            Capability siteName = initCapability(     new Capability(ARG_SITE_NAME, "Site Name",             Capability.TYPE_STRING), 
                       CAPABILITY_GROUP_SITE_QUERY, "Name of the site",                    "Name of site.   " + help);
            siteName.setBrowse(true); 
            capabilities.add(siteName);


            // site search for latitude-longitude bounding box; 4 boxes; not in browse service
            capabilities.add(initCapability(new Capability(ARG_BBOX, "Lat-Lon Bounding Box", Capability.TYPE_SPATIAL_BOUNDS), 
                    CAPABILITY_GROUP_SITE_QUERY, "Spatial bounds within which the site lies"));


            // Search for sites INSTALLED and overlapping a requested date range; entry box is a "Date Range" pair of boxes;
            // Output of all site searches is an HTML table with "Date Range" column , showing station's installed to retired dates; see gsl/output/HtmlOutputHandler.java.
            // implicitely uses and constructs two values from ARG_SITE_DATE by adding .from , etc.:
            // GsacArgs.java:
            // public static final String ARG_SITE_DATE            = ARG_SITE_PREFIX + "date";
            // public static final String ARG_SITE_DATE_FROM       = ARG_SITE_DATE + ".from";
            // public static final String ARG_SITE_DATE_TO         = ARG_SITE_DATE + ".to";
            Capability sitedateRange =
               initCapability( new Capability(ARG_SITE_DATE,        "Site Occupation Date Range", Capability.TYPE_DATERANGE),
                      CAPABILITY_GROUP_SITE_QUERY, "Site in place", "Site in place");
            capabilities.add(sitedateRange);


            /* not yet used
            Capability pubsitedateRange =
               initCapability( new Capability(ARG_SITE_PUBLISHDATE, "Site Published Date Range",  Capability.TYPE_DATERANGE),
                      CAPABILITY_GROUP_SITE_QUERY, "Site published date", "Site published date");
            capabilities.add(pubsitedateRange);
            */


            capabilities.add( initCapability( new Capability( ARG_SITE_DATADATE, "Site with Data in Date Range", Capability.TYPE_DATERANGE),
                     CAPABILITY_GROUP_SITE_QUERY, "Site data is between these dates", "Site data is between these dates"));


            //  Advanced site search items: 
            //  "CAPABILITY_GROUP_ADVANCED" search items appear on the web site search page under the "Advanced Site Query" label:

            String[] values;
            ResultSet results;
            ArrayList<String> avalues = new ArrayList<String>();
            List<String> tables = new ArrayList<String>();
            List<Clause> clauses = new ArrayList<Clause>();
            String cols="";
            ResultSet qresults;
            Statement statement=null;

            // Note values used in the following are only read once at GSAC start-up time.  If these sort of quasi-static database values are changed, restart GSAC.

            // search on site status (only a few static values possible)
            // get all values of this type from its table
            values = getDatabaseManager().readDistinctValues( Tables.STATION_STATUS.NAME, Tables.STATION_STATUS.COL_STATION_STATUS);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacArgs.ARG_SITE_STATUS, "Site Status", values, true, CAPABILITY_GROUP_ADVANCED));
          

            // search on type of station (only a few static values possible)
            // LOOK: this query choice is disabled due to  "GPS/GNSS Campaign GPS/GNSS Continuous GPS/GNSS Mobile " not applicable to Dataworks (so far).
            //     get all values of this type from its table
            //values = getDatabaseManager().readDistinctValues( Tables.STATION_STYLE.NAME, Tables.STATION_STYLE.COL_STATION_STYLE_DESCRIPTION);
            //Arrays.sort(values);
            //capabilities.add(new Capability(GsacArgs.ARG_SITE_TYPE, "Site Type", values, true, CAPABILITY_GROUP_ADVANCED));

            // LOOK: this query choice is disabled due to  only one network name allowed in the db station table in Dataworks (so far).
            // NETWORKS: code to allow two or more network names per site.
            //   a change is needed for the db schema; see working example in the GSAC Prototype schema.
            //   get all values of this type from its table 
            //values = getDatabaseManager().readDistinctValues( Tables.NETWORK.NAME, Tables.NETWORK.COL_NETWORK_NAME);
            //Arrays.sort(values); // sort names alphabetically
            //capabilities.add(new Capability(GsacArgs.ARG_SITE_GROUP, "Network", values, true, CAPABILITY_GROUP_ADVANCED));
            /* 
            // The Dataworks default db schema only allows one network for each station.  This code for a group of network names for each station is
            // retained for when they discover each station may belong to more than one network.  SW 5 Aug 2014.
            //
            // To provide a list of networks to search on, for all sites in the archive, first get all network(s) names found in each station with this query:
            //  WHERE
            cols=SqlUtil.comma(new String[]{Tables.STATION.COL_NETWORKS});
            //  FROM which tables 
            tables.add(Tables.STATION.NAME);
            statement = getDatabaseManager().select(cols,  tables,  null,  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query
               while ((results = iter.getNext()) != null) {
                   String networks= results.getString(Tables.STATION.COL_NETWORKS); // comma sep list of names of networks
                   // for tests; show all network names, from each single site, found when GSAC server starts 
                   //if (networks  != null ) {
                   //  System.err.println("      station as network(s) _"+networks+"_");
                   //}
                   //else {
                   //  ;// System.err.println("      station has networks value null in the database.");
                   //}
                   if (networks  != null && networks.length()>0) {
                         // split at commas to get each network name
                         String[] parts = networks.split(",");
                         if  (parts.length>0) {
                            //loop on all; make sure not already seen
                            for (int ni= 0; ni<parts.length; ni+=1 ) {
                               String nwname = parts[ni]; 
                               if ( ! avalues.contains(nwname)) {
                                   avalues.add(nwname);
                                   //System.err.println("      new network _"+nwname+"_");
                               }
                            }
                         }
                   }
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }
            itemArray = new String[avalues.size()];
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            // add search on network names:
            capabilities.add(new Capability(GsacArgs.ARG_SITE_GROUP, "Network", values, true, CAPABILITY_GROUP_ADVANCED));
            */


            // to enable the search on antenna types, first get antenna type names used by station equipment sessions
            avalues = new ArrayList<String>();
            clauses = new ArrayList<Clause>();
            //  WHERE 
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_ANTENNA_ID, Tables.ANTENNA.COL_ANTENNA_ID));
            //  SELECT what to 
            cols=SqlUtil.comma(new String[]{Tables.ANTENNA.COL_ANTENNA_NAME});
            //  FROM   
            tables = new ArrayList<String>();
            tables.add(Tables.EQUIP_CONFIG.NAME);
            tables.add(Tables.ANTENNA.NAME);
            statement =
               getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query  
               while ((results = iter.getNext()) != null) {
                   String anttype= results.getString(Tables.ANTENNA.COL_ANTENNA_NAME);
                   // System.err.println("   SiteManager: an allowed antenna type for searches is " + anttype) ;
                   int notfound=1;
                   for (int vi= 0; vi<avalues.size(); vi+=1 ) {
                      if ( avalues.get(vi).equals(anttype) ) { 
                          notfound=0;
                          break;
                          }
                   }
                   if (notfound==1) {
                      avalues.add(anttype);
                   }
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }
            String[] itemArray = new String[avalues.size()];
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_ANTENNA, "Antenna type", values, true, CAPABILITY_GROUP_ADVANCED));


            // to allow a search on receiver NAMES (not firmware version numbers)
            avalues = new ArrayList<String>();
            clauses = new ArrayList<Clause>();
            //  WHERE 
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_RECEIVER_FIRMWARE_ID, Tables.RECEIVER_FIRMWARE.COL_RECEIVER_FIRMWARE_ID));
            //  SELECT what to 
            cols=SqlUtil.comma(new String[]{Tables.RECEIVER_FIRMWARE.COL_RECEIVER_NAME});
            //  FROM   
            tables = new ArrayList<String>();
            tables.add(Tables.EQUIP_CONFIG.NAME);
            tables.add(Tables.RECEIVER_FIRMWARE.NAME);
            statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query  
               while ((results = iter.getNext()) != null) {
                   String type= results.getString(Tables.RECEIVER_FIRMWARE.COL_RECEIVER_NAME);
                   int notfound=1;
                   for (int vi= 0; vi<avalues.size(); vi+=1 ) {
                      if ( avalues.get(vi).equals(type) ) { 
                         notfound=0;
                         break;
                         }
                   }
                   if (notfound==1) {
                         avalues.add(type);
                   }
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }
            itemArray = new String[avalues.size()];
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_RECEIVER, "Receiver type", values, true, CAPABILITY_GROUP_ADVANCED));


            // to allow a search on radome types: get radome type names used by station equipment sessions 
            avalues = new ArrayList<String>();
            clauses = new ArrayList<Clause>();
            //  WHERE 
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_RADOME_ID, Tables.RADOME.COL_RADOME_ID));
            //  SELECT what to 
            cols=SqlUtil.comma(new String[]{Tables.RADOME.COL_RADOME_NAME});
            //  FROM   
            tables = new ArrayList<String>();
            tables.add(Tables.EQUIP_CONFIG.NAME);
            tables.add(Tables.RADOME.NAME);
            statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query  
               while ((results = iter.getNext()) != null) {
                   String type= results.getString(Tables.RADOME.COL_RADOME_NAME);
                   int notfound=1;
                   for (int vi= 0; vi<avalues.size(); vi+=1 ) {
                      if ( avalues.get(vi).equals(type) ) { 
                         notfound=0;
                         break;
                         }
                   }
                   if (notfound==1) {
                         avalues.add(type);
                   }
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }
            itemArray = new String[avalues.size()];
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_DOME, "Radome type", values, true, CAPABILITY_GROUP_ADVANCED));

            // Note values used in the following are only read once at GSAC start-up time.  If these sort of quasi-static database values are changed, restart GSAC.

            // add box to choose by country (reads from the db to get a list to choose from) 
            // NOTE: if no such values in the db, no list is made and shown.
            values = getDatabaseManager().readDistinctValues( Tables.COUNTRY.NAME, Tables.COUNTRY.COL_COUNTRY_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_COUNTRY, "Nation", values, true, CAPABILITY_GROUP_ADVANCED));

            // add box to choose by locale
            // LOOK for dataworks only , in gsac core code change in this file gsl/metadata/PoliticalLocationMetadata.java
            // to               outputHandler.msgLabel("Locale"), city));
            // NOTE: if no such values in the db, none is shown.
            values = getDatabaseManager().readDistinctValues( Tables.LOCALE.NAME, Tables.LOCALE.COL_LOCALE_INFO);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_CITY, "City/Locale", values, true, CAPABILITY_GROUP_ADVANCED));

            // disabled: provide a SEARCH on type of momument
            //values = getDatabaseManager().readDistinctValues( Tables.MONUMENT_STYLE.NAME, Tables.MONUMENT_STYLE.COL_MONUMENT_STYLE_DESCRIPTION);
            //Arrays.sort(values);
            //capabilities.add(new Capability(GsacArgs.ARG_SITE_TYPE, "Monument Type", values, true, CAPABILITY_GROUP_ADVANCED));


            return capabilities;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    } // end doGetQueryCapabilities


    /**
     * Make database search clauses, the db select statement clauses, from the user's choices specified 
     *  in the web page input boxes, or from the URL or API request arguments' values.  
     *
     * Makes and returns item "clauses".
     *
     * @param request the request
     * @param response the response
     * @param tableNames _more_
     * @param msgBuff buffer to append search criteria to
     *
     * @return list of clauses for selecting sites
     */
    public List<Clause> getResourceClauses(GsacRequest request, GsacResponse response, List<String> tableNames, StringBuffer msgBuff) 
        {
        // debug System.err.println("   SiteManager:  getResourceClauses ") ;
        /* which tables in the db to search on; the 'from' part of a db query, in this case the station table in the database. */
        tableNames.add(Tables.STATION.NAME);

        // declare (empty) item to return:
        List<Clause> clauses = new ArrayList();

        String       latCol  = Tables.STATION.COL_LATITUDE_NORTH;
        String       lonCol  = Tables.STATION.COL_LONGITUDE_EAST;

        // query for the station's 4 character ID
        if (request.defined(ARG_SITE_CODE)) {
            for (String code : (List<String>) request.get(ARG_SITE_CODE, new ArrayList())) {
                if (code.indexOf(" ") >= 0) {
                    response.appendMessage(
                        "You have some spaces in your search's site 4 char id.<br>Did you mean to do that, or did you forget to use a semicolon \"; \" with no space to delimit multiple site codes?<br>");
                    break;
                }
            }
           addStringSearch(request, ARG_SITE_CODE, ARG_SITE_CODE_SEARCHTYPE, msgBuff, "Site Code", Tables.STATION.COL_FOUR_CHAR_NAME, clauses);
        }

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

        // query for the dates station operated: (see code above setting ARG_SITE_DATE)
        try {
            clauses.addAll(getDateRangeClause(request, msgBuff,
                    ARG_SITE_DATE_FROM, ARG_SITE_DATE_TO, "Site date",
                    Tables.STATION.COL_INSTALLED_DATE,
                    Tables.STATION.COL_RETIRED_DATE));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        // query for DATA time range at a site NEW 28 Aug 2015
        try {
            clauses.addAll(getDateRangeClause(request, msgBuff,
                    ARG_SITE_DATADATE_FROM, ARG_SITE_DATADATE_TO, "Site data times",
                    Tables.STATION.COL_EARLIEST_DATA_TIME,
                    Tables.STATION.COL_LATEST_DATA_TIME));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }


        // debug System.err.println("   SiteManager: grc gives search clauses so far="+clauses) ;


        // Query for the single network name allowed in the default Dataworks db scheme for this station 
        // When a set of network names is required; see code below.
        if (request.defined(GsacExtArgs.ARG_NETWORK)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_NETWORK);
            tableNames.add(Tables.NETWORK.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_NETWORK_ID, Tables.NETWORK.COL_NETWORK_ID));
            clauses.add(Clause.eq(Tables.NETWORK.COL_NETWORK_NAME, values.get(0)));
        }
        // or for NETWORKS : alternate code to allow two or more network names per site.
        /*
        // query for this station's networks; "group" is GSAC jargon for gnss network name set.
        // The Dataworks schema only allows one network for each station.  This code for a group of network names for each station is
        // retained for when they discover each station may belong to more than one network.  SW 5 Aug 2014.
        if (request.defined(ARG_SITE_GROUP)) {
            List<String> values = (List<String>) request.get(ARG_SITE_GROUP, new ArrayList());
            clauses.add(Clause.or(getNetworkClauses(values, msgBuff)));  // see method def getNetworkClauses () below
        }
        */

        // LOOK might add code for each case below which has values.get(0), to use a loop over i>1 values.get(i) if present, so can make a selection list

        if (request.defined(GsacArgs.ARG_SITE_TYPE)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList(GsacArgs.ARG_SITE_TYPE);
            tableNames.add(Tables.STATION_STYLE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STYLE_ID, Tables.STATION_STYLE.COL_STATION_STYLE_ID));
            clauses.add(Clause.eq(Tables.STATION_STYLE.COL_STATION_STYLE_DESCRIPTION, values.get(0)));
            //System.err.println("   SiteManager: query for station style or type (as tide gage or GPS rcvr) " + values.get(0)) ;
        }
        
        if (request.defined(GsacArgs.ARG_SITE_STATUS)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList(GsacArgs.ARG_SITE_STATUS);
            tableNames.add(Tables.STATION_STATUS.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATUS_ID, Tables.STATION_STATUS.COL_STATION_STATUS_ID));
            clauses.add(Clause.eq(Tables.STATION_STATUS.COL_STATION_STATUS, values.get(0)));
            //System.err.println("   SiteManager: query for stattion status " + values.get(0)) ;
        }
        
        // LOOK FIX return distinct stations, not duplicate stations:
        if (request.defined(GsacExtArgs.ARG_ANTENNA)) {
            //System.err.println("      DW SiteManager: search for sites with antenna "+GsacExtArgs.ARG_ANTENNA);
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_ANTENNA);
            tableNames.add(Tables.EQUIP_CONFIG.NAME);
            tableNames.add(Tables.ANTENNA.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.EQUIP_CONFIG.COL_STATION_ID));
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_ANTENNA_ID, Tables.ANTENNA.COL_ANTENNA_ID));
            clauses.add(Clause.eq(Tables.ANTENNA.COL_ANTENNA_NAME, values.get(0)));
            //System.err.println("   DW SiteManager: query for antenna type name " + values.get(0) + " with where clauses "+clauses) ;
            // query for antenna type name AOAD/M_T with where clauses 
            // [station.station_id join 'equip_config.station_id', equip_config.antenna_id join 'antenna.antenna_id', antenna.antenna_name = 'AOAD/M_T']
            // the sql query is done by GsacResourceManager:handleRequest(GsacRequest request, GsacResponse response);   how called here?  
            request.setsqlWhereSuffix(" GROUP BY "+ Tables.STATION.COL_STATION_ID);
        }
        
        if (request.defined(GsacExtArgs.ARG_DOME)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_DOME);
            tableNames.add(Tables.EQUIP_CONFIG.NAME);
            tableNames.add(Tables.RADOME.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.EQUIP_CONFIG.COL_STATION_ID));
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_RADOME_ID, Tables.RADOME.COL_RADOME_ID));
            clauses.add(Clause.eq(Tables.RADOME.COL_RADOME_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for antenna dome type name " + values.get(0)) ;
            request.setsqlWhereSuffix(" GROUP BY "+ Tables.STATION.COL_STATION_ID);
        }
        
        if (request.defined(GsacExtArgs.ARG_RECEIVER)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_RECEIVER);
            tableNames.add(Tables.EQUIP_CONFIG.NAME);
            tableNames.add(Tables.RECEIVER_FIRMWARE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.EQUIP_CONFIG.COL_STATION_ID));
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_RECEIVER_FIRMWARE_ID, Tables.RECEIVER_FIRMWARE.COL_RECEIVER_FIRMWARE_ID));
            clauses.add(Clause.eq(Tables.RECEIVER_FIRMWARE.COL_RECEIVER_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for receiver type name " + values.get(0)) ;
            request.setsqlWhereSuffix(" GROUP BY "+ Tables.STATION.COL_STATION_ID);
        }
        
        // query for country
        if (request.defined(GsacExtArgs.ARG_COUNTRY)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_COUNTRY);
            tableNames.add(Tables.COUNTRY.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_COUNTRY_ID, Tables.COUNTRY.COL_COUNTRY_ID));
            clauses.add(Clause.eq(Tables.COUNTRY.COL_COUNTRY_NAME, values.get(0)));
        }
        
        // for Dataworks 'locale,' formerly called city
        if (request.defined(GsacExtArgs.ARG_CITY)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_CITY);
            tableNames.add(Tables.LOCALE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_LOCALE_ID, Tables.LOCALE.COL_LOCALE_ID));
            clauses.add(Clause.eq(Tables.LOCALE.COL_LOCALE_INFO, values.get(0)));
            //System.err.println("   SiteManager: query for locale " + values.get(0)) ;
        }

        //     debug / for testing: 
        // System.err.println("   SiteManager: getResourceClauses created clauses="+clauses) ;
        //     to show clauses like
        //       [(station.networks = 'BOULDER GNSS' OR station.networks LIKE '%BOULDER GNSS%')]
        //      which creates, later, the sql based query or API to GSAC:
        //      new request /somegsac/gsacapi/site/search?site.code.searchtype=exact&output=site.html&limit=1000&site.group=BOULDER+GNSS&site.name.searchtype=exact

        return clauses;
    } // end of getResourceClauses



    /**
     * Create and return GSAC's internal "resource" (a "site object") identified by the given resource id in this case the CODE_4CHAR_ID; see Tables.java.
     *
     * This is called only on a SINGLE site search by 4 char ID; not for example by antenna type.  This is NOT the main site search call!
     *
     * do the sql search query and make the 'resource', what is returned as a result from a query
     * Appears to be called when you click on a particular site in the table of sites found, after a search for sites.
     * For composing an HTML page to show about one site.
     *
     * @param resourceId resource id. 
     *
     * @return the resource or null if not found
     *
     * @throws Exception on badness
     */
    public GsacResource getResource(String resourceId) throws Exception {

        // the SQL search clause: select where a column value COL_FOUR_CHAR_NAME  = the "resourceId" which is some site 4 char ID entered by the user in the api or search form
        Clause clause = Clause.eq(Tables.STATION.COL_FOUR_CHAR_NAME, resourceId);

        // compose the complete select SQL phrase; apply the select clause to the table(s) given. see select ( ) in gsl/database/GsacDatabaseManager.java
        //                                                 DB  .select( what to find (fields),     from which tables,      where clause, )  
        // works ok: Statement statement = getDatabaseManager().select(getResourceSelectColumns(), clause.getTableNames(), clause);
        //System.err.println("GSAC:  SiteManager:getResource() Sites Search query is for " + getResourceSelectColumns()  ); // DEBUG
        //System.err.println("GSAC:  SiteManager:getResource() Sites Search query where clause is " + clause  ); //DEBUG

        // and this also has ordering :
        String suffixSql = " order by " + Tables.STATION.COL_FOUR_CHAR_NAME;
        //System.err.println("GSAC:  SiteManager:getResource() Sites Search query suffix is " + suffixSql  );
        Statement statement = getDatabaseManager().select(getResourceSelectColumns(), clause.getTableNames(), clause, suffixSql, -1);

        //System.err.println("   SiteManager: getResource sql statement =_"+statement.toString() +"_") ;  // DEBUG

        try {
            // do the SQL query, and get results
            ResultSet results = statement.getResultSet();
            //System.err.println("GSAC:  SiteManager: Sites Search results are " +results);
            // if no result (row) returned, return null here.
            if ( !results.next()) {
                results.close();
                return null;
            }
            // make a GsacSite object when a query is made, from db query results (row) ( but not yet made a web page or return anything for an API rquest)
            GsacSite site = (GsacSite) makeResource(results);  // aka makeSite
            results.close();


            return site;
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
          
    }  // end of getResource


    /**
     * @param request
     * @param msgBuff
     * @param fromArg
     * @param toArg
     * @param argTxt
     * @param colStart
     * @param colEnd
     * @return
     * @throws Exception on badness
     */
    public List<Clause> getDateRangeClause(GsacRequest request, StringBuffer msgBuff, String fromArg, String toArg,
                                           String argTxt, String colStart, String colEnd) 
                        throws Exception {


        List<Clause> clauses = new ArrayList<Clause>();
        // TODO: check the logic of the date range search
        // debug System.err.println("   SiteManager:  get date range from-name and to-name s: "+fromArg+"   "+toArg) ;

        Date[] dateRange = request.getDateRange(fromArg, toArg, null, null);
        // System.err.println("   SiteManager:  get date range from - to : "+dateRange[0]+" -  "+dateRange[1]) ;

        if (dateRange[0] != null) { 
           appendSearchCriteria(msgBuff,  argTxt + "&gt;=", "" + format(dateRange[0]));
        }
        if (dateRange[1] != null) { 
           appendSearchCriteria(msgBuff, argTxt + "&lt;=", "" + format(dateRange[1]));
        }
        if ((dateRange[0] != null) || (dateRange[1] != null)) { 
           addDateRangeClause(clauses, colStart, colEnd, dateRange); 
           // System.err.println("   SiteManager:  get date range clause = "+ clauses) ;
        }
           // debug System.err.println("   SiteManager:  get date range clause = "+ clauses) ;

        return clauses;
    }


    /**
     * CHANGEME set query order.  This string is SQL.
     *   Set this to what you want to sort on   ; station 4 char ID, ASC means ascending ie from A to Z top to bottom.         
     */
    private static final String SITE_ORDER = " ORDER BY  " + Tables.STATION.COL_FOUR_CHAR_NAME + " ASC ";


    /**
     * Get the columns that are to be searched on              
     *
     * @param request the request
     *
     * @return comma delimited fully qualified column names to select on
     */
    public String getResourceSelectColumns() {
        return Tables.STATION.COLUMNS;
    }


    /**
     * Get the order by clause: the table of sites found is listed alphabetically by site 4 character ID.  
     *
     * @param request the request
     *
     * @return order by clause
     */
    public String getResourceOrder(GsacRequest request) {
        return SITE_ORDER;
        //return null;
    }


    /**
     * Create a single 'site':  make a GsacSite object which has site metadata 
        (for display in web page, or to send to user as 'results' in some form determined by an OutputHandler class).
     * Input "results" is one row got from the db query, a search on stations.
     * "results" is from sql select query on 'station' table in the database.
     * Previous code to this call did a db select clause to get one (or more?) rows in the db station table for one (or more?) site ids
     * cf. makeSite() in UNAVCO GSAC code
     *
     * @param results db results
     *
     * @return the site
     *
     * @throws Exception on badness
     */
    @Override
    public GsacResource makeResource(ResultSet results) throws Exception {
        // debug System.err.println("GSAC:  makeResource: ");

        GsacSite site = new GsacSite();

        // access values by name of field in database row: 
        String fourCharId  =   results.getString(Tables.STATION.COL_FOUR_CHAR_NAME);  
        String    staname  =   results.getString(Tables.STATION.COL_STATION_NAME);
        

        double latitude =      results.getDouble(Tables.STATION.COL_LATITUDE_NORTH);
        double longitude =     results.getDouble(Tables.STATION.COL_LONGITUDE_EAST);
        double ellipsoid_hgt = results.getDouble(Tables.STATION.COL_HEIGHT_ABOVE_ELLIPSOID);

        /* not yet used
        double x = results.getDouble(Tables.STATION.COL_X);
        double y = results.getDouble(Tables.STATION.COL_Y);
        double z = results.getDouble(Tables.STATION.COL_Z);
        */

        int countryid    =     results.getInt(Tables.STATION.COL_COUNTRY_ID);
        int stateid      =     results.getInt(Tables.STATION.COL_LOCALE_ID);
        int networkid    =     results.getInt(Tables.STATION.COL_NETWORK_ID);
        /* for station fields of images of plots of time series: */
        String station_photo_URL          = results.getString(Tables.STATION.COL_STATION_IMAGE_URL);
        String time_series_plot_image_URL = results.getString(Tables.STATION.COL_TIME_SERIES_URL); 

        // new 23 July 
        //String mirrored_from_URL = results.getString(Tables.STATION.COL_MIRRORED_FROM_URL);   // may be null

        //  tricky code to fix the otherwise incorrect reading by Java JDBC                                  (UTF8 UTF-8 utf-8 utf8)
        //  of names in Icelandic or in other non-latin characters, and which are correct in the MySQL db:   (UTF8 UTF-8 utf-8 utf8)
        if (null!=staname) { staname = new String( results.getBytes(Tables.STATION.COL_STATION_NAME), "UTF-8"); }       //    (UTF8 UTF-8 utf-8 utf8)
         
        //  Make a site object: GsacSite ctor in src/org/gsac/gsl/model/GsacSite.java is 
        // public          GsacSite(String siteId, String siteCode, String name, double latitude, double longitude, double elevation) 
        // The so-called "elevation," in GSAC GSL code, is properly the height above reference ellipsoid. 
        //  Not elevation, which is height above some (unknown) geoid model surface.
        site = new GsacSite(fourCharId, fourCharId, staname, latitude, longitude, ellipsoid_hgt);

        // Set additional values in the site object:
 
        // not yet used; set x,y,z LOOK simpler way?
        //EarthLocation el = new EarthLocation(latitude, longitude, ellipsoid_hgt, x, y, z);
        // site.setEarthLocation(el);

        // set the site-was-installed data range at this station:
        // (Not the dates data obd files are available at this site.)
        Date fromDate=readDate(results,  Tables.STATION.COL_INSTALLED_DATE);
        if (fromDate != null )
            {
            site.setFromDate(fromDate);  // uses gsl/model/GsacResource.java: public void setFromDate(Date value). Probably.
            }

        // for site retired date: if no defined, use value of "today" a UNAVCO practice still running, in place of "unknown."
        Date toDate=  readDate(results,  Tables.STATION.COL_RETIRED_DATE );
        if (toDate != null )
            {
            ; // System.err.println("   SiteManager: station " +fourCharId+ " has retired date "+toDate);
            }
        else
            {
            toDate = new Date(); // db value is null, so use "now" ie still operating
            //System.err.println("\n   SiteManager: station " +fourCharId+ " retired date was NULL; now is "+toDate);
            }
        site.setToDate(toDate);

        Date aDate;
        /* not yet used;  set the published date for this  site
        aDate=readDate(results,  Tables.STATION.COL_PUBLISHED_DATE);
        if (aDate != null )
            {
            site.setPublishDate(aDate); 
            }
         */

        // set the latest data date for this  site
        aDate=readDate(results,  Tables.STATION.COL_LATEST_DATA_TIME);
        //if (aDate != null )
            {
            site.setLatestDataDate(aDate);  
            }
 
        // not used yet.   set the published date for this  site
        //Date aDate=readDate(results,  Tables.STATION.COL_EARLIEST_DATA_DATE);
        //if (aDate != null )
        //  {
            //site.setEarliestDataDate(aDate); 
        //}

        //System.err.println("   SiteManager:      makeResource:  station " +fourCharId+ " installed "+ site.getFromDate()+ ";  retired date "+ site.getToDate());

        /* The Dataworks schema only allows one network for each station.  This code for a group of network names for each station is
         * retained for when they discover each station may belong to more than one network.  SW 5 Aug 2014.
        //Add the network(s) for this station, in alphabetical order,  to the resource group
        if ((networks != null) && (networks.trim().length() > 0)) {
            List<String> toks = new ArrayList<String>();
            for (String tok : networks.split(",")) {
                toks.add(tok.trim());
            }
            Collections.sort(toks);
            for (String tok : (List<String>) toks) {
                site.addResourceGroup(new ResourceGroup(tok));  // this method adds the comma-separated list of network names at a site, to a site object
            }
        }
        */

        String cols="";
        List<String> tables = new ArrayList<String>();
        List<Clause> clauses = new ArrayList<Clause>();
        ResultSet qresults;


        // get name of country, province or state, and agency from their id numbers 

        // get name of country
        String country = "";
        cols="";
        tables = new ArrayList<String>();
        clauses = new ArrayList<Clause>();
        //  WHERE the test part in the select statement
        clauses.add(Clause.join(Tables.STATION.COL_COUNTRY_ID, Tables.COUNTRY.COL_COUNTRY_ID));
        clauses.add(Clause.eq(Tables.COUNTRY.COL_COUNTRY_ID, countryid));
        //  SELECT what to get from the db (result in rows returned):
        cols=SqlUtil.comma(new String[]{Tables.COUNTRY.COL_COUNTRY_NAME});
        //  FROM   the select from which tables part
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.COUNTRY.NAME);
        Statement statement = getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        //System.err.println("   SiteManager: country query is " +statement);
        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           // process each line in results of db query
           while ((qresults = iter.getNext()) != null) {
               country = new String( qresults.getBytes(Tables.COUNTRY.COL_COUNTRY_NAME), "UTF-8"); //qresults.getString(Tables.COUNTRY.COL_COUNTRY_NAME);
               // you want Only read the first row of db query results returned
               break;
           }
        } finally {
           getDatabaseManager().closeAndReleaseConnection(statement);
        }

        // get name of locale;  aka city
        String locale = "";
        clauses = new ArrayList<Clause>();
        tables = new ArrayList<String>();
        cols="";
        clauses.add(Clause.join(Tables.STATION.COL_LOCALE_ID, Tables.LOCALE.COL_LOCALE_ID));
        clauses.add(Clause.eq(Tables.LOCALE.COL_LOCALE_ID, stateid));
        cols=SqlUtil.comma(new String[]{Tables.LOCALE.COL_LOCALE_INFO});
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.LOCALE.NAME);
        statement = //select            what    from      where
           getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        //System.err.println("   SiteManager: locale query is " +statement);
        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           while ((qresults = iter.getNext()) != null) {
               //      System.err.println("   get locale name");
               locale = new String( qresults.getBytes(Tables.LOCALE.COL_LOCALE_INFO), "UTF-8"); // qresults.getString(Tables.LOCALE.COL_LOCALE_INFO);
               //      System.err.println("   did get "+locale);
               break;
           }
         } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
         }

        // add above items to site as "PoliticalLocationMetadata":
        String state =null;
        String city = locale;
        site.addMetadata(new PoliticalLocationMetadata(country , state , city ));


        // NEW 23 July 2105
        //site.setMirroredFromURL(mirrored_from_URL);
        // debug System.err.println("   SiteManager:      makeResource:  station " +fourCharId+ " mirror URL="+site.getMirroredFromURL());
        // Look add this value to the site metadata AND make a line showing it on the site HTML web page labeled "Mirrored from" plus ":"
        //if (null!=mirrored_from_URL ) {  site.addMetadata(new PropertyMetadata(GsacArgs.ARG_SITE_MIRROR_FROM_URL,  mirrored_from_URL, "Mirrored from"));}


        // add URL(s) of image(s) here; which will appear on web page of one station's results, in a tabbed window
        MetadataGroup imagesGroup = null;

        if ( station_photo_URL != null  )    {
            if (imagesGroup == null) {
                site.addMetadata(imagesGroup = new MetadataGroup("Images:", MetadataGroup.DISPLAY_TABS));
            }
            if ( station_photo_URL.length()>8 ) {
                   URL u = new URL(station_photo_URL); 
                   HttpURLConnection huc = (HttpURLConnection)u.openConnection(); 
                   huc.setDoOutput(true);
                   huc.setRequestMethod("GET"); 
                   huc.connect() ; 
                   OutputStream os = huc.getOutputStream(); 
                int ucode = huc.getResponseCode(); 
                //System.err.println("       station_photo_URL   get ucode = "+ucode);
                // add  a real site photo image to the group:
                if (200==ucode) {
                   imagesGroup.add( new ImageMetadata( station_photo_URL, "Site Photo"));
                }
            }
        }
        if (time_series_plot_image_URL!=null )    {
            if (imagesGroup == null) {
                site.addMetadata(imagesGroup = new MetadataGroup("Images:", MetadataGroup.DISPLAY_TABS));
            }
            if (time_series_plot_image_URL.length()>8 ) { 
                   URL u = new URL( time_series_plot_image_URL); 
                   HttpURLConnection huc = (HttpURLConnection)u.openConnection(); 
                   huc.setDoOutput(true);
                   huc.setRequestMethod("GET"); 
                   huc.connect() ; 
                   OutputStream os = huc.getOutputStream(); 
                   int ucode = huc.getResponseCode(); 
                   //System.err.println("       time seris plot file get ucode = "+ucode);
                   // add  a real  image to the group:
                   if (200==ucode) {
                      imagesGroup.add( new ImageMetadata(time_series_plot_image_URL, "Position Timeseries"));
                }
            }
        }

        /* following code section is in effect readAgencyMetadata(site);
        clauses = new ArrayList<Clause>();
        tables = new ArrayList<String>();
        cols="";
        clauses.add(Clause.join(Tables.STATION.COL_AGENCY_ID, Tables.AGENCY.COL_AGENCY_ID));
        clauses.add(Clause.eq(Tables.AGENCY.COL_AGENCY_ID, agencyid));
        cols=SqlUtil.comma(new String[]{Tables.AGENCY.COL_AGENCY_NAME});
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.AGENCY.NAME);
        statement = 
           getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        //                      select  what   from     where
        //System.err.println("   SiteManager: select query is " +statement);

        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           while ((qresults = iter.getNext()) != null) {
               String agency = new String( qresults.getBytes(Tables.AGENCY.COL_AGENCY_NAME), "UTF-8"); // qresults.getString( Tables.AGENCY.COL_AGENCY_NAME);
               addPropertyMetadata( site, GsacExtArgs.SITE_METADATA_NAMEAGENCY, "Agency", agency);    
               break;
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }

        //  set site "Type" aka site.type corresponding to "station style" in the database
        // Not clear where or how this is used by GSAC code.
        // CHANGEME if you alter the GSAC prototype db schema.
        // hard coded, using values in the GSAC prototype db:
        /*
        select * from station_style;
        +------------------+---------------------+
        | station_style_id | station_style_name  |
        +------------------+---------------------+
        |                1 | GPS/GNSS Campaign   |
        |                2 | GPS/GNSS Continuous |
        |                3 | GPS/GNSS Mobile     |
        |                4 | DORIS               |
        |                5 | Seismic             |
        |                6 | SLR                 |
        |                7 | Strainmeter         |
        |                8 | Tiltmeter           |
        |                9 | VLBI                |
        |               10 | GPS/GNSS Episodic   |
        |               11 | Tide Gauge          |
        +------------------+---------------------+
        * /

        if (1 == station_style_id ) {
           site.setType(new ResourceType("gnss.site.campaign"));
        }
        else if (2 == station_style_id ) {
           site.setType(new ResourceType("gnss.site.continuous"));
        }
        else if (3 == station_style_id ) {
           site.setType(new ResourceType("gnss.site.mobile"));
        }
        else if (4  == station_style_id ) {
           site.setType(new ResourceType("doris.site"));
        }
        else if (5  == station_style_id ) {
           site.setType(new ResourceType("seismic.site"));
        }
        else if (6 == station_style_id ) {
           site.setType(new ResourceType("slr.site"));
        }
        else if (7 == station_style_id ) {
           site.setType(new ResourceType("strainmeter.site"));
        }
        else if (8  == station_style_id ) {
           site.setType(new ResourceType("tiltmeter.site"));
        }
        else if (9  == station_style_id ) {
           site.setType(new ResourceType("tiltmeter.site"));
        }
        else if (10 == station_style_id ) {
           site.setType(new ResourceType("gnss.site.episodic"));
        }
        else if (11 == station_style_id ) {
           site.setType(new ResourceType("tidegauge.site"));
        }

        // CHANGEME: implement this, if you need this; sample code is below in this file.
        // readFrequencyStandardMetadata(site);
        */

        return site;

    } // end make Resource()


    /**
     * _more_  
     *
     * @param results _more_
     * @param column _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String readValue(ResultSet results, String column)
            throws Exception {
        String s = results.getString(column);
        if (s == null) {
            return "";
        }
        if (s.startsWith("(") && s.endsWith(")")) {
            return "";
        }
        return s;
    }


    /**
     * Get metadata for the given site. 
     * NOTE: who calls this?  What is "level"?
     *
     * @param level _more_
     * @param gsacResource resource
     *
     * @throws Exception On badness
     */
    @Override
    public void doGetMetadata(int level, GsacResource gsacResource)
            throws Exception {
        readIdentificationMetadata(gsacResource);
        //        System.err.println("      calling read EquipmentMetadata");
        readEquipmentMetadata(gsacResource);
    }

    /**
     * Get station metadata for the given site; this called when user clicks on a site name in the 
     * sites' search results table on the table web page (and when other output demand is made possibly).
     * Then sets values in some GsacExtArgs.
     * This method adds new items and text to, at least, the HTML page of results.
     *
     * @param gsacResource resource
     *
     * @throws Exception On badness
     */
   private void readIdentificationMetadata(GsacResource gsacResource)
            throws Exception {

        ResultSet results;

        // make a db query statement to find the site corresponding to the current site or "gsacResource"; the CODE_4CHAR_ID is stored as the resource's Id, from gsacResource.getId()  
        // note that this gets ALL the columns of fields from the table "station" (on the row matching the select Clause.eq item) 
        //System.err.println("GSAC:  SiteManager:getResource() Sites Search query is for " + getResourceSelectColumns()  );
        //System.err.println("GSAC:  SiteManager:getResource() Sites Search query where clause is " + clause  );
        Statement statement = getDatabaseManager().select( Tables.STATION.COLUMNS, Tables.STATION.NAME,
                Clause.eq( Tables.STATION.COL_FOUR_CHAR_NAME, gsacResource.getId()), (String) null, -1);
        
        // a single station query from this is
        //SELECT station.station_id,station.code_4char_ID,station.station_name,station.latitude_north,station.longitude_east,station.ellipsoidal_height,station.station_installed_date,station.station_removed_date,station.station_style_id,station.station_status_id,station.access_permission_id,station.monument_description_id,station.country_id,station.province_region_state_id,station.city,station.x,station.y,station.z,station.iers_domes,station.station_photo_URL,station.time_series_image_URL,station.agency_id,station.networks,station.embargo_duration_hours,station.embargo_after_date FROM station WHERE (station.code_4char_ID = 'ATAL')

        // make the db query to find the row of info about this station
        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            // process each line in results of db query  
            while ((results = iter.getNext()) != null) {
                String    staname  =                  results.getString(Tables.STATION.COL_STATION_NAME);
                if (null!=staname) {
                         staname       =   new String( results.getBytes(Tables.STATION.COL_STATION_NAME), "UTF-8");
                }
                // ok gsacResource.setLongName( new String( results.getBytes(Tables.STATION.COL_STATION_NAME), "UTF-8") ); //results.getString(Tables.STATION.COL_STATION_NAME)  
                gsacResource.setLongName( staname ); 

                // get values from the dq query row returned, and then etc. for x,y,z, the SITE_TRF_X  etc.
                // Note if you add similar but new and different parameters to your data base, you also need to
                // add to the file gsac/trunk/src/org/gsac/gsl/GsacExtArgs.java to declare similar new variables.
                // The var names "Tables..." comes from the Tables.java file made when you built with ant maketables with your new database.

                /*  not yet for dataworks:
                String xstr = results.getString(Tables.STATION.COL_X);
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_TRF_X, "X", xstr);
                String ystr = results.getString(Tables.STATION.COL_Y);
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_TRF_Y, "Y", ystr);
                String zstr = results.getString(Tables.STATION.COL_Z);
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_TRF_Z, "Z", zstr);
                */

                // get and show in site html page the  value for IERS DOMES. 
                String idn= results.getString(Tables.STATION.COL_IERS_DOMES);
                if (idn != null ) { 
                  addPropertyMetadata( gsacResource, GsacExtArgs.SITE_METADATA_IERDOMES, "IERS DOMES", idn);
                }

                // did only the first row of db query results returned
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        // db query  to get MONUMENT_DESCRIPTION
        List<Clause> clauses = new ArrayList<Clause>();
        clauses.add(Clause.eq(Tables.STATION.COL_FOUR_CHAR_NAME, gsacResource.getId()));

        /*
        // join the table with these 2 values:
        clauses.add(Clause.join  (Tables.STATION.COL_MONUMENT_DESCRIPTION_ID, Tables.MONUMENT_DESCRIPTION.COL_MONUMENT_DESCRIPTION_ID));
        String cols=SqlUtil.comma(new String[]{  Tables.MONUMENT_DESCRIPTION.COL_MONUMENT_DESCRIPTION});
        List<String> tables = new ArrayList<String>();
        // FROM BOTH the tables 
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.MONUMENT_DESCRIPTION.NAME);
        statement =
            getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1); 
        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "monument", 
                     results.getString(Tables.MONUMENT_DESCRIPTION.COL_MONUMENT_DESCRIPTION) );
                //System.err.println("\n  --------------------------- set monu desc "+results.getString(Tables.MONUMENT_DESCRIPTION.COL_MONUMENT_DESCRIPTION));
                // arg "monument" appears as a label in the HTML page about one station.
                //Only read the first row of db query results returned
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
        */
    }


    /**
     * For the station with the input 4 char ID number gsacResource.getId(), get the metadata for each equipment  session
     *
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     */
    private void readEquipmentMetadata(GsacResource gsacResource) throws Exception {

        //        System.err.println("      called  read EquipmentMetadata");
        Date indate=null;
        Date outdate=null;
        Date[] dateRange=null;
        List<GnssEquipment>  equipmentList  = new ArrayList<GnssEquipment>();
        List<Date>  startDates = new ArrayList<Date>();
        List<Date>  stopDates =  new ArrayList<Date>();
        List<Clause> clauses = new ArrayList<Clause>();
        List<String> tables = new ArrayList<String>();
        String cols;
        Statement           statement;
        ResultSet           results;
        // access values by order of items returned from query: (use of item name fails for cols with Key = MUL): 
        int colCnt = 1;

        // GSAC uses time formatting in ISO 8601, but without the "T" which helpfully means "this is a time"; what a surprize.
        DateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  

        // to format antenna height (to fix the db may ruin exact original value)
        DecimalFormat fourptForm = new DecimalFormat("#.####");
        // if the value has fewer than 4 decimal points, only the one provided are used.  No adding 0s.
        // Java : DecimalFormat provides rounding modes defined in RoundingMode for formatting. By default, it uses RoundingMode.HALF_EVEN. 

        /* the equip session items in the GSAC dataworks db:
        mysql> desc equip_config;
        +-------------------------+-------------+------+-----+---------+----------------+
        | Field                   | Type        | Null | Key | Default | Extra          |
        +-------------------------+-------------+------+-----+---------+----------------+
        | equip_config_id         | int(6)      | NO   | PRI | NULL    | auto_increment |
        | station_id              | int(6)      | NO   | MUL | NULL    |                |
        | create_time             | datetime    | NO   |     | NULL    |                |
        | equip_config_start_time | datetime    | NO   |     | NULL    |                |
        | equip_config_stop_time  | datetime    | NO   |     | NULL    |                |
        | antenna_id              | int(3)      | NO   | MUL | NULL    |                |
        | antenna_serial_number   | varchar(20) | NO   |     | NULL    |                |
        | antenna_height          | float       | NO   |     | NULL    |                |
        | metpack_id              | int(3)      | YES  | MUL | NULL    |                |
        | metpack_serial_number   | varchar(20) | YES  |     | NULL    |                |
        | radome_id               | int(3)      | NO   | MUL | NULL    |                |
        | radome_serial_number    | varchar(20) | NO   |     | NULL    |                |
        | receiver_firmware_id    | int(3)      | NO   | MUL | NULL    |                |
        | receiver_serial_number  | varchar(20) | NO   |     | NULL    |                |
        | satellite_system        | varchar(60) | YES  |     | NULL    |                |
        +-------------------------+-------------+------+-----+---------+----------------+
        */

        // WHERE  this station is id-ed by its 4 char id:   select ...  WHERE STATION.COL_FOUR_CHAR_NAME="P123"
        clauses.add(Clause.eq(Tables.STATION.COL_FOUR_CHAR_NAME, gsacResource.getId())); 
        // and where  EQUIP_CONFIG.COL_STATION_ID == STATION.COL_STATION_ID
        clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_STATION_ID, Tables.STATION.COL_STATION_ID)); 
        //   mysql select WHAT:  
        // these two values not used in any GSAC output format 
        // Tables.EQUIP_CONFIG.COL_METPACK_ID
        // '' COL_METPACK_SERIAL_NUMBER,
        cols=SqlUtil.comma(new String[]{
         Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_START_TIME,
         Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_STOP_TIME,
         Tables.EQUIP_CONFIG.COL_ANTENNA_ID,
         Tables.EQUIP_CONFIG.COL_ANTENNA_SERIAL_NUMBER,
         Tables.EQUIP_CONFIG.COL_ANTENNA_HEIGHT, 
         Tables.EQUIP_CONFIG.COL_RADOME_ID,
         Tables.EQUIP_CONFIG.COL_RADOME_SERIAL_NUMBER,
         Tables.EQUIP_CONFIG.COL_RECEIVER_FIRMWARE_ID,
         Tables.EQUIP_CONFIG.COL_RECEIVER_SERIAL_NUMBER ,
         Tables.EQUIP_CONFIG.COL_SATELLITE_SYSTEM
         });
        // FROM these tables
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.EQUIP_CONFIG.NAME);
        //System.err.println("GSAC:  SiteManager:getResource() equip sql query is for " + cols  );
        //System.err.println("GSAC:  SiteManager:getResource() equip sql query where clause is " + clauses  );
        statement = getDatabaseManager().select(cols,  tables, Clause.and(clauses), " order by " + Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_START_TIME, -1);
        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {

               String sdt=null;
               Date test = readDate( results, Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_START_TIME);
               if (null == test) { 
                   // System.err.println("   GSAC DB values ERROR:  station "+gsacResource.getId()+" has invalid (null) EQUIP_CONFIG.COL_EQUIP_CONFIG_START_TIME");
                   indate = new Date();  //  better than null?
               } 
               else {
                  sdt = results.getString(Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_START_TIME)+"00";
                  indate = dateformatter.parse(sdt);
               }
               colCnt++;

               String odt=null;
               test = readDate( results, Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_STOP_TIME);
               if (null == test) { 
                   //System.err.println("   the equip session stop time is null, undefined.  "); 
                   outdate = new Date();  // i.e., now: we presume this is correct and the instrument is still active.
               } 
               else {
                  odt = results.getString(Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_STOP_TIME)+"00";
                  outdate = dateformatter.parse(odt);
               }
               colCnt++;

               dateRange = new Date[] { indate, outdate };

               if (null!=indate && null!=outdate && indate.after(outdate)) {
                    // System.err.println("   GSAC DB values ERROR:  Dates of equip config session (station "+gsacResource.getId()+")  are reversed: begin time: "+ indate +" is >  end time: "+ outdate);
                    continue;
                 }

               //System.err.println("\n   GSAC equip config session at station "+gsacResource.getId()+"from start time: "+ indate +"  to stop time: "+ outdate);

                /* some problem with the column-counting way to get values of returned fields:  int antid  = results.getInt(colCnt++);
                  String ant_serial_number  = results.getString(colCnt++);
                  double antenna_height = results.getFloat(colCnt++);
                  antenna_height = Double.valueOf(fourptForm.format(antenna_height));
                  int domeid  = results.getInt(colCnt++);
                */

                int antid  = results.getInt(Tables.EQUIP_CONFIG.COL_ANTENNA_ID);
                //System.err.println("\n   GSAC got antenna key id number "+antid+"  for station "+gsacResource.getId() );

                String ant_serial_number  = results.getString(Tables.EQUIP_CONFIG.COL_ANTENNA_SERIAL_NUMBER);
                double antenna_height = results.getFloat(Tables.EQUIP_CONFIG.COL_ANTENNA_HEIGHT);
                antenna_height = Double.valueOf(fourptForm.format(antenna_height));
                int domeid  = results.getInt(Tables.EQUIP_CONFIG.COL_RADOME_ID);
                String dome_serial_number  = results.getString(Tables.EQUIP_CONFIG.COL_RADOME_SERIAL_NUMBER);
                int recid  = results.getInt(Tables.EQUIP_CONFIG.COL_RECEIVER_FIRMWARE_ID);
                
                String rec_serial_number  = results.getString(Tables.EQUIP_CONFIG.COL_RECEIVER_SERIAL_NUMBER);
                String sat_system  = results.getString(Tables.EQUIP_CONFIG.COL_SATELLITE_SYSTEM);

                ArrayList<String> avalues = new ArrayList<String>();

                /* get value of antenna_name from the db table 'antenna' via key value 'antenna_id'
                mysql> desc antenna;
                +--------------+-------------+------+-----+---------+----------------+
                | Field        | Type        | Null | Key | Default | Extra          |
                +--------------+-------------+------+-----+---------+----------------+
                | antenna_id   | int(3)      | NO   | PRI | NULL    | auto_increment |
                | antenna_name | varchar(15) | NO   |     | NULL    |                |
                | igs_defined  | char(1)     | NO   |     | N       |                |
                +--------------+-------------+------+-----+---------+----------------+
                */
                String ant_type="";
                avalues =  new ArrayList<String>();
                clauses =  new ArrayList<Clause>();
                tables =   new ArrayList<String>();
                //System.err.println("      ant id = "+ antid);
                clauses.add(Clause.eq(Tables.ANTENNA.COL_ANTENNA_ID, antid) );
                cols=SqlUtil.comma(new String[]{Tables.ANTENNA.COL_ANTENNA_NAME});
                tables.add(Tables.ANTENNA.NAME);
                //System.err.println("GSAC:  SiteManager:getResource()  sql query is for " + cols  );
                //System.err.println("GSAC:  SiteManager:getResource()  sql query where clause is " + clauses  );
                statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
                //System.err.println("    get  ant stm = "+ statement);
                try {
                   SqlUtil.Iterator iter2 = getDatabaseManager().getIterator(statement);
                   while ((results = iter2.getNext()) != null) {
                       // System.err.println("      while ant type= "+ ant_type);
                       ant_type = results.getString(Tables.ANTENNA.COL_ANTENNA_NAME);
                   }
                } finally {
                       getDatabaseManager().closeAndReleaseConnection(statement);
                } 
                //System.err.println("      ant type= "+ ant_type);

                /* get dome type name dddd */
                String dome_type="";
                avalues =  new ArrayList<String>();
                clauses =  new ArrayList<Clause>();
                tables =   new ArrayList<String>();
                clauses.add(Clause.eq(Tables.RADOME.COL_RADOME_ID, domeid) );
                cols=SqlUtil.comma(new String[]{Tables.RADOME.COL_RADOME_NAME});
                tables.add(Tables.RADOME.NAME);
                statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
                try {
                   SqlUtil.Iterator iter2 = getDatabaseManager().getIterator(statement);
                   while ((results = iter2.getNext()) != null) {
                       dome_type = results.getString(Tables.RADOME.COL_RADOME_NAME);
                   }
                } finally {
                       getDatabaseManager().closeAndReleaseConnection(statement);
                } 
                //System.err.println("      dome type= "+ dome_type);

                /* get value of receiver_name  and rec_firmware_vers from the db table 'receiver_firmware' via key value 'receiver_firmware_id'
                mysql> desc receiver_firmware; 
                +----------------------+-------------+------+-----+---------+----------------+
                | Field                | Type        | Null | Key | Default | Extra          |
                +----------------------+-------------+------+-----+---------+----------------+
                | receiver_firmware_id | int(5)      | NO   | PRI | NULL    | auto_increment |
                | receiver_name        | varchar(20) | NO   |     | NULL    |                |
                | receiver_firmware    | varchar(20) | NO   |     | NULL    |                |
                | igs_defined          | char(1)     | NO   |     | N       |                |
                +----------------------+-------------+------+-----+---------+----------------+
                */
                String rcvr_type="";
                String rec_firmware_vers = "";
                avalues =  new ArrayList<String>();
                clauses =  new ArrayList<Clause>();
                tables =   new ArrayList<String>();
                cols=SqlUtil.comma(new String[]{     Tables.RECEIVER_FIRMWARE.COL_RECEIVER_NAME, Tables.RECEIVER_FIRMWARE.COL_RECEIVER_FIRMWARE});
                tables.add(Tables.RECEIVER_FIRMWARE.NAME);
                clauses.add(Clause.eq(Tables.RECEIVER_FIRMWARE.COL_RECEIVER_FIRMWARE_ID, recid) );
                statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
                try {
                   SqlUtil.Iterator iter2 = getDatabaseManager().getIterator(statement);
                   while ((results = iter2.getNext()) != null) {
                       rcvr_type = results.getString(Tables.RECEIVER_FIRMWARE.COL_RECEIVER_NAME);
                       rec_firmware_vers = results.getString(                                    Tables.RECEIVER_FIRMWARE.COL_RECEIVER_FIRMWARE);
                   }
                } finally {
                       getDatabaseManager().closeAndReleaseConnection(statement);
                } 

               // construct a GSAC "GnssEquipment" object with these values:
               // public GnssEquipment(Date[] dateRange, String antennatype, String antennaSN, String dometype, String domeSerial, String receiver, String receiverSerial, 
               //                       String receiverFirmware,  double zoffset)  
               GnssEquipment equipment_session =
                   new GnssEquipment(dateRange, ant_type, ant_serial_number, dome_type, dome_serial_number, rcvr_type, rec_serial_number, rec_firmware_vers,antenna_height);  

               // add name of sat systems like "GPS" 
               equipment_session.setSatelliteSystem(sat_system);  

               equipmentList.add(equipment_session);
               // end loop on all equip sessions
            }  // end while  ((results = iter.getNext()) != null)
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        // sort by time?
        equipmentList = GnssEquipment.sort(equipmentList);

        GnssEquipmentGroup equipmentGroup = null;
        // for every item equipment_session in the local equipmentList, add it to the equipmentGroup 
        for (GnssEquipment equipment_session : equipmentList) {
            if (equipmentGroup == null) {
                gsacResource.addMetadata(equipmentGroup = new GnssEquipmentGroup()); // LOOK why add null to this list?
            }
            equipmentGroup.add(equipment_session);
        }

    }  // end of read equip metadata ()


    /* utility
    public boolean checkDouble( String input )  
    {  
       try  
       {  
          Double.parseDouble( input );  
          return true;  
       }  
       catch( Exception e)  
       {  
         return false;  
       }  
    }  
    */


     /* *
     * Get all sites networks. This gets called by the SiteManager.getDefaultCapabilities (base class, not here)
     * Used in query for the station's networks.   
     * 'ResourceGroup' or 'group' is GSAC jargon for gnss networks.
     *
     * NETWORKS : alternate code to allow two or more network names per site.
     *
     * The Dataworks schema only allows one network for each station.  This code for a group of network names for each station is
     * retained for when they discover each station may belong to more than one network.  SW 5 Aug 2014.
     *
     * @return site group list
     * /
    public List<ResourceGroup> doGetResourceGroups() {
        try {
            //System.err.println("       doGetResourceGroups(): ");
            HashSet<String>     seen   = new HashSet<String>();
            List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
            //                       select          what    from      where
            Statement statement =
                getDatabaseManager().select( distinct(Tables.STATION.COL_NETWORKS), Tables.STATION.NAME);

            for (String commaDelimitedList : SqlUtil.readString(getDatabaseManager().getIterator(statement), 1)) {
                if (commaDelimitedList == null) {
                    continue;
                }
                for (String tok : commaDelimitedList.split(",")) {
                    tok = tok.trim();
                    //System.err.println("       doGetResourceGroups(): network _"+tok+"_");
                    if (seen.contains(tok)) {
                        continue;
                    }
                    seen.add(tok);
                    groups.add(new ResourceGroup(tok));
                }
            }
            Collections.sort(groups);
            return groups;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }
     /** 
     * Utility that takes a list of network ids and makes the search clauses for them.
     * Used in query for the station's networks. 
     * 'ResourceGroup' or 'group' is GSAC jargon for gnss networks, sometimes.
     *
     * NETWORKS : alternate code to allow two or more network names per site.
     *
     * The Dataworks schema only allows one network for each station.  This code for a group of network names for each station is
     * retained for when they discover each station may belong to more than one network.  SW 5 Aug 2014.
     *
     * @param groupIds List of group ids
     * @param msgBuff Search criteria buffer
     *
     * @return List of Clauses
     * /
    private List<Clause> getNetworkClauses(List<String> groupIds, StringBuffer msgBuff) {
        List<Clause> groupClauses = new ArrayList<Clause>();

        String  col = Tables.STATION.COL_NETWORKS;

        int cnt = 0;
        for (String group : groupIds) {
            //System.err.println("       getNetworkClauses(): search for network or group name _"+group+"_");  // shows correct result
            appendSearchCriteria(msgBuff, ((cnt++ == 0) ? "Site Group=" : ""), group);
            groupClauses.add(Clause.eq(col, group));
            // need clause where the string 'group' is IN the col result
            groupClauses.add(Clause.like(col, SqlUtil.wildCardBoth(group)));
            // optional other cases which are no help here:
            //groupClauses.add(Clause.like(col, SqlUtil.wildCardBefore("," + group)));
            //groupClauses.add(Clause.like(col, SqlUtil.wildCardAfter(group + ",")));
            //groupClauses.add(Clause.like(col, SqlUtil.wildCardBoth("," + group + ",")));
        }
        return groupClauses;
    }
*/


    /**
     * _more_
     *
     * @param gsacResource _more_
     * @param id _more_
     * @param label _more_
     * @param value _more_
     */
    private void addPropertyMetadata(GsacResource gsacResource, String id, String label, String value) {
        if ((value != null) && (value.length() > 0)) {
            gsacResource.addMetadata(new PropertyMetadata(id, value, label));
        }
    }


    /**
     * Convert a db datetime field to a 'Date' object.
     * NOTE this uses the java sql package ResultSet class and ONLY returns calendar date -- with NO time of day allowed.
            could use results.getTime()
            could use results.getTimeSTAmp() both in java sql
     * @param results a row from a qb query which has a datetime field
     * @param column a string name for a db field with  for example a MySQL 'datetime' object,
     *                such as the String held by Tables.EQUIP_CONFIG.COL_START_DATE
     * @return _more_
     */
    private Date readDate(ResultSet results, String column) {
        try {
            return results.getDate(column);
        } catch (Exception exc) {
            //if the date is undefined we get an error so we just return null 
            return null;
        }
    }

}  // end of class
