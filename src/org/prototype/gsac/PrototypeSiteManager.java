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

package org.prototype.gsac;
import  org.prototype.gsac.database.*;
/* CHANGE  above, make sure that both lines show your GSAC package name */

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.util.*;
import org.gsac.gsl.metadata.gnss.*;
import org.gsac.gsl.output.HtmlOutputHandler;
import org.gsac.gsl.ramadda.sql.Clause;
import org.gsac.gsl.ramadda.sql.SqlUtil;

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


/**
 * The GSAC SiteManager classes handle all of a GSAC repository's site(station)-related requests.  
 *
 * This PrototypeSiteManager.java is for the GSAC Prototype15 databasce schema and code, of 2015-2016.
 *
 * The base class is in gsac/gsl/SiteManager.java.  Each GSAC application instance also has its own site manager, such as src/org/myrepo/gsac/MyrepoSiteManager.java.
 *
 * This class is one major part of making a new local GSAC; it allows a local GSAC to query the local GSAC database, and handles the results from queries:
 *
 * - what metadata may be queried on, that is used for searches or selections, in this GSAC repository (see method doGetQueryCapabilities below)
 *   either by the web page forms or via the API URL arguments, 
 *
 * - how to query the database for such request (see method getResourceClauses below), and 
 *
 * - how to package up the results from the query (method makeResource below) into a java object for further use, such as for the HTML pages of
 *   Search Sites results on the GSAC web site, and the items in other result formats like SINEX.
 *
 * The base class is gsac/gsl/SiteManager.java.  Each GSAC application instance also has its own site manager, such as src/org/arepo/gsac/ArepoSiteManager.java.
 * Code in the SiteManager class is highly dependent on your particular db schema design and its names for tables and columns in tables.
 * This instance of the SiteManager class uses the GSAC Prototype database schema.
 * 
 * new variable ARG_SITE_MIRROR_FROM_URL read from the db station table.  
 * @author  S K Wier, 2013 - 2 July 2015
 * @author  S K Wier, October - 9 Nov 2015; many changes to improve this class. Added among others the concept of latest data time at a site.
 */
public class PrototypeSiteManager extends SiteManager {

    /**
     * ctor
     *
     * @param repository the repository
     */
    public PrototypeSiteManager(PrototypeRepository repository) {
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
        super.handleRequest(request, response);
    }

    /** do we get the data ranges: where used? */
    private boolean doDateRanges = true;


    /**
     * Create the site search "capabilities", which are all the items to offer to the user to search for sites(stations), either on web page and/or in API queries.  
     *
     * A GSAC search item (Java object) is a "Capability."  Here is where you implement items to use in site queries from this database, 
     * searches from either web site or url api args)
     *
     * A capability here which for example is tied to the value GsacExtArgs.ARG_ANTENNA has corresponding code in the method getResourceClauses which creates a query with it when user does so.
     *
     * (it appears a call to this method goes before makeCapabilities call, so regular site search form appears before advanced search)
     *
     * This method is called at GSAC server start-up.  Must restart the GSAC server to find new items only detected here, such as gnss file types.
     * This method appears to be called twice at server start-up.
     *
     * CHANGEME if you have other things to search on, or different db table or field names for the items to search on.
     *
     * @return site search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        try {

            // order of adding to capabilities here specifies order on html site search page
            List<Capability> capabilities = new ArrayList<Capability>();

            // Essential search items 

            // To search on site code, the 4 character ID.  Users may use regular expressions such as AB* or P12*
            String help = HtmlOutputHandler.stringSearchHelp;  /* some mouse over help text */
            //            args:(                             "web page label"                     capab type ),      capab group name,       mouse over help text,  other help text)
            Capability siteCode =
                initCapability( new Capability(ARG_SITE_CODE, "Code (4 character ID)", Capability.TYPE_STRING), 
                CAPABILITY_GROUP_SITE_QUERY, "Code (4 character ID) of the station", "Code (4 character ID) of the station. " + help);
            siteCode.setBrowse(true);  /*  which apparently adds these searches to the GSAC web site Browse form */
            capabilities.add(siteCode);

            help="Full name of the site, such as Marshall, or part or name plus wildcard(*) such as Mar*";
            Capability siteName =
                initCapability(     new Capability(ARG_SITE_NAME, "Site Name",             Capability.TYPE_STRING), 
                       CAPABILITY_GROUP_SITE_QUERY, "Name of the site",                    "Name of site.   " + help);
            capabilities.add(siteName);
            //siteName.setBrowse(true);  /*  which apparently adds these searches to the GSAC web site Browse form */

            // site search for latitude-longitude bounding box; 4 boxes; not in browse service
            capabilities.add(initCapability(new Capability(ARG_BBOX, "Lat-Lon Bounding Box", Capability.TYPE_SPATIAL_BOUNDS), 
               CAPABILITY_GROUP_SITE_QUERY, "Spatial bounds within which the site lies"));


            // Search for sites INSTALLED and overlapping a requested date range; entry box is a "Date Range" pair of boxes;
            // Output of all site searches is an html table with "Date Range" column , showing station's installed to retired dates; see gsl/output/HtmlOutputHandler.java.
            // Implicitely uses and constructs two values from ARG_SITE_DATE by adding .from , etc.:
            // GsacArgs.java:
            // public static final String ARG_SITE_DATE            = ARG_SITE_PREFIX + "date";
            // public static final String ARG_SITE_DATE_FROM       = ARG_SITE_DATE + ".from";
            // public static final String ARG_SITE_DATE_TO         = ARG_SITE_DATE + ".to";
            Capability sitedateRange =
               initCapability( new Capability(ARG_SITE_DATE,     "Site occupied in date range", Capability.TYPE_DATERANGE),
                      CAPABILITY_GROUP_SITE_QUERY, "Site installed", "Site installed");
            capabilities.add(sitedateRange);
            // LOOK you can use code like the above for sites' DATA AVAILABLE date ranges if your db station table records start and end times for when data was or is available,
            // in place of site OCCUPATION dates.  In that case use a search box label like "Site Data Date Range."  Code elsewhere will or may need to be changed too. 


            // LOOK NEW
            // Search for sites with real data files in a time range, in this repository (not just INSTALLED in a requested date range); entry box is a "Date Range" pair of boxes;
            // Sites can have data gaps when data was not collected even inside of times they are installed and have equipment completely specified.
            //    implicitly uses  ARG_SITE_EARLIEST_DATA_DATE from GsacArgs.java:
            /* 
            Capability siteDataDateRange = 
               initCapability( new Capability(ARG_SITE_DATADATE, "Site with data files in date range", Capability.TYPE_DATERANGE),
                      CAPABILITY_GROUP_SITE_QUERY, "Site with data in date", "Site  with data in date");
            capabilities.add(siteDataDateRange);
            */

            // LOOK NEW , could possibly do this:
            // Search for sites with the lastest time of its data files in a requested date range; entry box is a "Date Range" pair of boxes;
            // GsacArgs.java:
            // debusterize this ???
            // public static final String ARG_SITE_LATEST_DATA_DATE =        ARG_SITE_PREFIX + "datadate";
            // public static final String ARG_SITE_LATEST_DATA_TIME_FROM   = ARG_SITE_LATEST_DATA_TIME + ".from";
            // public static final String ARG_SITE_LATEST_DATA_TIME_TO  =    ARG_SITE_LATEST_DATA_TIME + ".to";
            /* 
            Capability siteDataDateRange = 
               initCapability( new Capability(ARG_SITE_LATEST_DATA_TIME, "Site latest data in time  range", Capability.TYPE_DATERANGE),
                      CAPABILITY_GROUP_SITE_QUERY, "Site latest data time", "Site latest data time");
            capabilities.add(siteDataDateRange);
            */


            //  Advanced search items: "CAPABILITY_GROUP_ADVANCED" search items appear on the web site search page under the "Advanced Site Query" label:
            String[] values;
            ResultSet results;
            ArrayList<String> avalues = new ArrayList<String>();
            List<Clause> clauses = new ArrayList<Clause>();
            List<String> tables = new ArrayList<String>();
            String cols="";
            ResultSet qresults;
            Statement statement=null;
            String[] itemArray;

            // Note values used in the following are only read once at GSAC start-up time.  If these sort of quasi-static database values are changed, restart GSAC.

            // To provide a list of networks to search on, for all sites in the archive, first get all network(s) names found in each station with this query:
            //  WHERE
            cols=SqlUtil.comma(new String[]{Tables.STATION.COL_NETWORKS});
            int netcount=0;
            //  FROM which tables 
            tables.add(Tables.STATION.NAME);
            statement = getDatabaseManager().select(cols,  tables,  null,  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query
               while ((results = iter.getNext()) != null) {
                   String networks= results.getString(Tables.STATION.COL_NETWORKS); //  the field value should be one string , a SEMI_COLON separated  list of names of networks
                   // for tests; show all network names, from each single site, found when GSAC server starts 
                   //if (networks  != null ) {
                   //  System.err.println("      station as network(s) _"+networks+"_");
                   //}
                   //else {
                   //  ;// System.err.println("      station has networks value null in the database.");
                   //}
                   if (networks  != null && networks.length()>0) {
                         // split at SEMI_COLON to get each network name
                         String[] parts = networks.split(";");
                         if  (parts.length>0) {
                            //loop on all; make sure not already seen
                            for (int ni= 0; ni<parts.length; ni+=1 ) {
                               String nwname = parts[ni]; 
                               if ( ! avalues.contains(nwname)) {
                                   avalues.add(nwname);
                                   netcount += 1;
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
            //System.err.println("GSAC: there are "+netcount+" networks among the stations.");

            /* capabilities.add(initCapability(new Capability(ARG_SITE_MODIFYDATE, "Site Modified Date Range", Capability.TYPE_DATERANGE), CAPABILITY_GROUP_ADVANCED,
                        "The site's metadata was modified between these dates"));

            capabilities.add( initCapability( new Capability( ARG_SITE_CREATEDATE, "Site Created Date Range", Capability.TYPE_DATERANGE), CAPABILITY_GROUP_ADVANCED,
                        "The site was created between these dates"));
            */

            // search on site type; to show all station style or types in the database station_style table which will have more than this data center has:
            // get only site type names (station_style table values) used by stations in this database, only.
            avalues = new ArrayList<String>();
            clauses = new ArrayList<Clause>();
            //  WHERE 
            clauses.add(Clause.join(Tables.STATION.COL_STYLE_ID, Tables.STATION_STYLE.COL_STATION_STYLE_ID));
            //  SELECT what column values to find
            cols=SqlUtil.comma(new String[]{Tables.STATION_STYLE.COL_STATION_STYLE_DESCRIPTION});
            //  FROM   
            tables = new ArrayList<String>();
            tables.add(Tables.STATION.NAME);
            tables.add(Tables.STATION_STYLE.NAME);
            statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query  
               while ((results = iter.getNext()) != null) {
                   String statype= results.getString(Tables.STATION_STYLE.COL_STATION_STYLE_DESCRIPTION);
                   // save Distinct values
                   int notfound=1;
                   for (int vi= 0; vi<avalues.size(); vi+=1 ) {
                      if ( avalues.get(vi).equals(statype) ) {
                         notfound=0;
                         break;
                         }
                   }
                   if (notfound==1) {
                         avalues.add(statype);
                   }
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }
            itemArray = new String[avalues.size()];
            values = avalues.toArray(itemArray);
            // sort by alphabet:
            // Arrays.sort(values);
            capabilities.add(new Capability(GsacArgs.ARG_SITE_TYPE, "Site Type", values, true, CAPABILITY_GROUP_ADVANCED));

            // search on site status (3 static values possible)
            values = getDatabaseManager().readDistinctValues( Tables.STATION_STATUS.NAME, Tables.STATION_STATUS.COL_STATION_STATUS);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacArgs.ARG_SITE_STATUS, "Site Status", values, true, CAPABILITY_GROUP_ADVANCED));


            // Make a choice box for antenna types, to search for site(S) with ONE antenna type choosen from this list of types.
            // First get ALL antenna type names used by ALL stations in all equipment sessions
            avalues = new ArrayList<String>();
            clauses = new ArrayList<Clause>();
            //  WHERE 
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_ANTENNA_ID, Tables.ANTENNA.COL_ANTENNA_ID));
            //  SELECT what to get from the database 
            cols=SqlUtil.comma(new String[]{Tables.ANTENNA.COL_ANTENNA_NAME});
            //  FROM  which tables: 
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
            itemArray = new String[avalues.size()];
            //System.err.println("GSAC: found "+ avalues.size() +  " antenna types ") ;
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            // finally make an entry box with pull-down list of all antenna types, for the user to choose one from:
            capabilities.add(new Capability(GsacExtArgs.ARG_ANTENNA, "Antenna type", values, true, CAPABILITY_GROUP_ADVANCED));
            // also makes the enum list in "API Information" page for API allowed arguments.


            // To allow a search on receiver NAMES (not firmware version numbers).
            // See comments for similar antenna type searches, above.
            avalues = new ArrayList<String>();
            clauses = new ArrayList<Clause>();
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_RECEIVER_FIRMWARE_ID, Tables.RECEIVER_FIRMWARE.COL_RECEIVER_FIRMWARE_ID));
            cols=SqlUtil.comma(new String[]{Tables.RECEIVER_FIRMWARE.COL_RECEIVER_NAME});
            tables = new ArrayList<String>();
            tables.add(Tables.EQUIP_CONFIG.NAME);
            tables.add(Tables.RECEIVER_FIRMWARE.NAME);
            statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
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
            //System.err.println("GSAC: found "+ avalues.size() +  " receiver types ") ;
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_RECEIVER, "Receiver type", values, true, CAPABILITY_GROUP_ADVANCED));


            // to allow a search on radome types: get radome type names used by station equipment sessions 
            // See comments for similar antenna type searches, above.
            avalues = new ArrayList<String>();
            clauses = new ArrayList<Clause>();
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_RADOME_ID, Tables.RADOME.COL_RADOME_ID));
            cols=SqlUtil.comma(new String[]{Tables.RADOME.COL_RADOME_NAME});
            tables = new ArrayList<String>();
            tables.add(Tables.EQUIP_CONFIG.NAME);
            tables.add(Tables.RADOME.NAME);
            statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
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
            //System.err.println("GSAC: found "+ avalues.size() +  " radome types ") ;
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_DOME, "Radome type", values, true, CAPABILITY_GROUP_ADVANCED));


            // search on country, province/state, and city

            // add box to choose by country
            values = getDatabaseManager().readDistinctValues( Tables.NATION.NAME, Tables.NATION.COL_NATION_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_COUNTRY, "Nation", values, true, CAPABILITY_GROUP_ADVANCED));

            // add box to choose by state or province 
            values = getDatabaseManager().readDistinctValues( Tables.PROVINCE_STATE.NAME, Tables.PROVINCE_STATE.COL_PROVINCE_STATE_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_STATE, "Province / state", values, true, CAPABILITY_GROUP_ADVANCED));

            // add box to choose by locale or city
            values = getDatabaseManager().readDistinctValues( Tables.LOCALE.NAME, Tables.LOCALE.COL_LOCALE_NAME);  // get all the city (place) names in GSAC's database.
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_CITY, "Place / City", values, true, CAPABILITY_GROUP_ADVANCED));

            return capabilities;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    } // end do GetQueryCapabilities


    /**
     * Make database search clauses, SQL select statements, from the user's choices specified in the web page input or from the URL request arguments' values.  
     *
     * Makes and returns a  List<Clause> object "clauses", a List of Clause objects.
     *
     * called by GsacResourceManager:handleRequest(GsacRequest request, GsacResponse response)  via ?
     *
     * @param request the request
     * @param response the response
     * @param tableNames _more_
     * @param msgBuff buffer to append search criteria to
     *
     * @return list of clauses for selecting sites
     */
    public List<Clause> getResourceClauses(GsacRequest request, GsacResponse response, List<String> tableNames, StringBuffer msgBuff) {

        /* which tables in the db to search on; the 'from' part of a db query, in this case the station table in the prototype database. */
        tableNames.add(Tables.STATION.NAME);

        // declare (empty) item to return:
        List<Clause> clauses = new ArrayList();

        String  latCol  = Tables.STATION.COL_LATITUDE_NORTH;
        String  lonCol  = Tables.STATION.COL_LONGITUDE_EAST;

        // query for the station's 4 character ID
        if (request.defined(ARG_SITE_CODE)) {
            for (String code : (List<String>) request.get(ARG_SITE_CODE, new ArrayList())) {
                if (code.indexOf(" ") >= 0) {
                    response.appendMessage(
                        "You have some spaces in your search's site code(name).<br>Did you mean to do that, or did you forget to use a semicolon \";\" with no space to delimit multiple site codes?<br>");
                    break;
                }
            }
           addStringSearch(request, ARG_SITE_CODE, ARG_SITE_CODE_SEARCHTYPE, msgBuff, "Site Code", Tables.STATION.COL_FOUR_CHAR_NAME, clauses);
        }

        // query for the station's name string  
        /*
        if (request.defined(ARG_SITE_NAME)) {
            addStringSearch(request, ARG_SITE_NAME, " ", msgBuff, "Site Name", Tables.STATION.COL_STATION_NAME, clauses);  
            System.err.println("   SiteManager: query for name " + ARG_SITE_NAME ) ;
        }
        */
        
        // query for the station's  location inside a latitude-longitude box; see input ARG_BBOX
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

        // query for the dates station was installed, but not necessarily recording data observation values. (see code above setting ARG_SITE_DATE)
        try {
            clauses.addAll(getDateRangeClause(request, msgBuff,
                    ARG_SITE_DATE_FROM, ARG_SITE_DATE_TO, "Site date",
                    Tables.STATION.COL_INSTALLED_DATE,
                    Tables.STATION.COL_RETIRED_DATE));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        /* NEW query by times when DATA is available from stations, in a given date range.
        */
            /* NNN
        try {
            clauses.addAll(getDateRangeClause(request, msgBuff,
                    ARG_SITE_DATADATE_FROM, ARG_SITE_DATADATE_TO, "Site data date",
                    Tables.DATAFILE.COL_DATAFILE_START_TIME,
                    Tables.DATAFILE.COL_DATAFILE_STOP_TIME));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
             */

        // query for the station's place name 
        if (request.defined(GsacExtArgs.ARG_CITY)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_CITY);
            clauses.add(Clause.join(Tables.STATION.COL_LOCALE_ID, Tables.LOCALE.COL_LOCALE_ID));
            clauses.add(Clause.eq(Tables.LOCALE.COL_LOCALE_NAME, values.get(0)));
        }

        if (request.defined(GsacExtArgs.ARG_COUNTRY)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_COUNTRY);
            tableNames.add(Tables.NATION.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_NATION_ID, Tables.NATION.COL_NATION_ID));
            clauses.add(Clause.eq(Tables.NATION.COL_NATION_NAME, values.get(0)));
        }
        
        if (request.defined(GsacExtArgs.ARG_STATE)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_STATE);
            tableNames.add(Tables.PROVINCE_STATE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_PROVINCE_STATE_ID, Tables.PROVINCE_STATE.COL_PROVINCE_STATE_ID));
            clauses.add(Clause.eq(Tables.PROVINCE_STATE.COL_PROVINCE_STATE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for province " + values.get(0)) ;
        }

        // query for the station's networks; "GROUP" or group is GSAC jargon for instrumented networks' names
        if (request.defined(ARG_SITE_GROUP)) {
            List<String> values = (List<String>) request.get(ARG_SITE_GROUP, new ArrayList());
            clauses.add(Clause.or(getNetworkClauses(values, msgBuff)));  // see method def getNetworkClauses () below
        }

        // LOOK might add code for each case below which has values.get(0), to use a loop over i>1 values.get(i) if present, so can make a selection list
        
        if (request.defined(GsacArgs.ARG_SITE_TYPE)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList(GsacArgs.ARG_SITE_TYPE);
            tableNames.add(Tables.STATION_STYLE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STYLE_ID, Tables.STATION_STYLE.COL_STATION_STYLE_ID));
            clauses.add(Clause.eq(Tables.STATION_STYLE.COL_STATION_STYLE_DESCRIPTION, values.get(0)));
            //System.err.println("   SiteManager: query for style " + values.get(0)) ;
        }
        
        if (request.defined(GsacArgs.ARG_SITE_STATUS)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList(GsacArgs.ARG_SITE_STATUS);
            tableNames.add(Tables.STATION_STATUS.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATUS_ID, Tables.STATION_STATUS.COL_STATION_STATUS_ID));
            clauses.add(Clause.eq(Tables.STATION_STATUS.COL_STATION_STATUS, values.get(0)));
            //System.err.println("   SiteManager: query for STATUS " + values.get(0)) ;
        }

        if (request.defined(GsacExtArgs.ARG_ANTENNA)) {
            // debug System.err.println("      PrototypeSiteManager: search for sites with antenna "+GsacExtArgs.ARG_ANTENNA);
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_ANTENNA);
            tableNames.add(Tables.EQUIP_CONFIG.NAME);
            tableNames.add(Tables.ANTENNA.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.EQUIP_CONFIG.COL_STATION_ID));
            clauses.add(Clause.join(Tables.EQUIP_CONFIG.COL_ANTENNA_ID, Tables.ANTENNA.COL_ANTENNA_ID));
            clauses.add(Clause.eq(Tables.ANTENNA.COL_ANTENNA_NAME, values.get(0)));
            // FIX do for select DISTINCT distinct
            // with like  getDatabaseManager().select( distinct(Tables.STATION.COL_NETWORKS), Tables.STATION.NAME);
            //System.err.println("GSAC SiteManager: query for antenna type name " + values.get(0) + " with where clauses "+clauses) ;
            // query for antenna type name AOAD/M_T with where clauses:
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
            //System.err.println("   SiteManager: query for radome type name " + values.get(0)) ;
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
            tableNames.add(Tables.NATION.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_NATION_ID, Tables.NATION.COL_NATION_ID));
            clauses.add(Clause.eq(Tables.NATION.COL_NATION_NAME, values.get(0)));
        }

        // for Dataworks 'locale,' formerly called city
        if (request.defined(GsacExtArgs.ARG_CITY)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_CITY);
            tableNames.add(Tables.LOCALE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_LOCALE_ID, Tables.LOCALE.COL_LOCALE_ID));
            clauses.add(Clause.eq(Tables.LOCALE.COL_LOCALE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for locale " + values.get(0)) ;
        }

        // NOTE: the following shows a line like
        // SiteManager: getResourceClauses gives search clauses=[(station.four_char_name LIKE 'b%' OR station.four_char_name LIKE 'b%' 
        //    OR station.four_char_name LIKE 'B%'), station.latitude_north <= 42.0, station.latitude_north >= 38.0, station.longitude_east <= -100.0, station.longitude_east >= -114.0]
        // or like    SiteManager: getResourceClauses gives [(station.networks = 'BOULDER GNSS' OR station.networks LIKE '%BOULDER GNSS%')]
        // which creates, later, the sql based query or API to GSAC:
        //  new request /prototypegsac/gsacapi/site/search?site.code.searchtype=exact&output=site.html&limit=1000&site.group=BOULDER+GNSS&site.name.searchtype=exact
        // LOOK debug  DEBUG print show search sql 
        //System.err.println("   SiteManager: getResourceClauses gives search clauses=\n      "+clauses) ;

        return clauses;
    } // end of getResourceClauses



    /**
     * Create and return GSAC's internal "resource" (a "site object") identified by the given resource id in this case the FOUR_CHAR_NAME; see Tables.java.
     *
     * What is returned as a result from a query
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
        // works ok: 
        // Statement statement = getDatabaseManager().select(getResourceSelectColumns(), clause.getTableNames(), clause);
        // and this also has ordering (ORDER BY) [sort SORT] :
        Statement    statement = getDatabaseManager().select(getResourceSelectColumns(), clause.getTableNames(), clause,  " order by " + Tables.STATION.COL_FOUR_CHAR_NAME, -1);
        //System.err.println("GSAC:  SiteManager:getResource() Sites Search query is " +statement);

        try {
            // do the SQL query, and get results
            ResultSet results = statement.getResultSet();
            // if no result (row) returned, return null here.
            if ( !results.next()) {
                results.close();
                return null;
            }
            // make a GsacSite object when a query is made, from db query results (rows) but not yet make a web page or return anything for an API request,
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
        Date[] dateRange = request.getDateRange(fromArg, toArg, null, null);
        if (dateRange[0] != null) { appendSearchCriteria(msgBuff,  argTxt + "&gt;=", "" + format(dateRange[0]));
        }
        if (dateRange[1] != null) { appendSearchCriteria(msgBuff, argTxt + "&lt;=", "" + format(dateRange[1]));
        }
        if ((dateRange[0] != null) || (dateRange[1] != null)) { addDateRangeClause(clauses, colStart, colEnd, dateRange); }
        return clauses;
    }


    /**
     * CHANGEME set query order.  This string is SQL.
     *   Set this to what you want to sort on   ; station 4 char ID, ASC means ascending ie from A to z top to bottom.         
     */
    private static final String SITE_ORDER = " ORDER BY  " + Tables.STATION.COL_FOUR_CHAR_NAME + " ASC ";


    /**
     * Get the columns that are to be searched on              
     *
     * value "Tables.STATION.COLUMNS" is all the columns (fields) in the database's station table.
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
     * Create a single 'site':  make a GsacSite object which has site metadata (for display in web page, or to send to user as 'results' in some form determined by an OutputHandler class).
     * input "results" is one row got from the db query, a search on stations.
     * Previous code to this call did a db select clause to get one (or more?) rows in the db station table for one (or more?) site ids
     * cf. makeSite in UNAVCO_GSAC code.
     *
     * @param results db results
     *
     * @return the site
     *
     * @throws Exception on badness
     */
    @Override
    public GsacResource makeResource(ResultSet results) throws Exception {
        // "results" is from sql select query on 'station' table in the database.

        // access values by name of field in database row: 
        String    staname  =    results.getString(Tables.STATION.COL_STATION_NAME);
        // to fix bad java-jdbc reading of names in Icelandic or other non-latin characters which are correct in the mysql db:
        if (null!=staname) {
          staname= new String( results.getBytes(Tables.STATION.COL_STATION_NAME), "UTF-8");
          }
        String fourCharId    = results.getString(Tables.STATION.COL_FOUR_CHAR_NAME);  
        // debug System.err.println("GSAC: SiteManager: site search found station " +fourCharId);
        double latitude =      results.getDouble(Tables.STATION.COL_LATITUDE_NORTH);
        double longitude =     results.getDouble(Tables.STATION.COL_LONGITUDE_EAST);
        double ellipsoid_hgt = results.getDouble(Tables.STATION.COL_HEIGHT_ELLIPSOID);
        String station_photo_URL = results.getString(Tables.STATION.COL_STATION_PHOTO_URL);
        String ts_image_URL =  results.getString(Tables.STATION.COL_TIME_SERIES_PLOT_IMAGE_URL); 
        String iersdomes =     results.getString(Tables.STATION.COL_IERS_DOMES);
        int station_style_id = results.getInt(Tables.STATION.COL_STYLE_ID);
        String station_status_id    = results.getString(Tables.STATION.COL_STATUS_ID);             // may be null; is String of an int
        int countryid    =     results.getInt(Tables.STATION.COL_NATION_ID);
        int stateid      =     results.getInt(Tables.STATION.COL_PROVINCE_STATE_ID);
        int cityid      =     results.getInt(Tables.STATION.COL_LOCALE_ID);
        int agencyid    =      results.getInt(Tables.STATION.COL_OPERATOR_AGENCY_ID); 
        // debug System.err.println("GSAC:  GSAC found site agency id " + agencyid);
        int monument_description_id = results.getInt(Tables.STATION.COL_MONUMENT_STYLE_ID);
        int station_id      =     results.getInt(Tables.STATION.COL_STATION_ID);

        String networks  =     results.getString(Tables.STATION.COL_NETWORKS);
        if (null!= networks) {
           networks =  new String( results.getBytes(Tables.STATION.COL_NETWORKS), "UTF-8");
           }

        /*
        int access_permission_id    = results.getInt(Tables.STATION.COL_ACCESS_PERMISSION_ID);
        if (1== access_permission_id ) {
            System.err.println("GSAC: new request      GSAC found station with no access permission (no public views allowed) " +fourCharId);
            GsacSite site = new GsacSite();
            return site;
        }
        */

        /*  Make a site object: GsacSite ctor in src/org/gsac/gsl/model/GsacSite.java is 
         public          GsacSite(String siteId, String siteCode, String name, double latitude, double longitude, double elevation) 
         * The so-called elevation, in GSAC like GNSS data, should be height above reference ellipsoid.  Not elevation which is height above some (unknown) geoid model surface.
        */
        GsacSite site = new GsacSite(fourCharId, fourCharId, staname, latitude, longitude, ellipsoid_hgt);

        // Set additional values in the site object:

        // add URL(s) of image(s) here; which will appear on web page of one station's results, in a tabbed window
        MetadataGroup imagesGroup = null;
        // a photograph of the site
        if ( station_photo_URL != null  )    {
            if (imagesGroup == null) {
                site.addMetadata(imagesGroup = new MetadataGroup("Images:", MetadataGroup.DISPLAY_TABS));
            }
            if ( station_photo_URL != null ) {
                // add  site photo image to the group:
                imagesGroup.add( new ImageMetadata( station_photo_URL, "Site Photo"));
            }
        }
        // for an image file of a time series solution at this site
        if (ts_image_URL!=null )    {
            if (imagesGroup == null) {
                site.addMetadata(imagesGroup = new MetadataGroup("Images:", MetadataGroup.DISPLAY_TABS));
            }
            if (ts_image_URL.length()>8 ) { 
                // add a valid image URL of a time series data plot to the images group:
                imagesGroup.add( new ImageMetadata(ts_image_URL, "Position Timeseries"));
            }
        }


        /* demo good code
        stop_time      = results.getString (col++); // note : what if stop time is before start time?
        start_time     = results.getString (col++); // from RAW_GPS.START_TIME 
        // NOTE these values from the gps3 database do NOT have the "T" part of the ISO 8601 time format.

        Date     fromTime= null;
        Date     toTime  = null;
        Date     pubTime = null;
        if ( numCols>11 )
            
            // since value may be like  "2015-11-01 23:59:45.0", cut off last two chars:
            toTime=   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stop_time.substring(0,19) );
            fromTime= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(start_time.substring(0,19));
        */

        //  get and set installed date range at this station:
        String startTimeStr     = results.getString (Tables.STATION.COL_INSTALLED_DATE);
        String stopTimeStr      = results.getString (Tables.STATION.COL_RETIRED_DATE); // note : what if stop time is before start time?
        //System.err.println("   SiteManager: station " +fourCharId+ " installed ="+startTimeStr+"_  retired="+stopTimeStr+"_");

        Date startTimeDate=null;  
        if (startTimeStr != null )
            {
                 startTimeDate =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTimeStr.substring(0,19) );
            }

        Date stopTimeDate=  null;  
        if (stopTimeStr != null )
            {
                 stopTimeDate =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stopTimeStr.substring(0,19) );
            }
        
        site.setFromDate(startTimeDate);  // uses gsl/model/GsacResource.java: public void setFromDate(Date value). Probably.
        site.setToDate(stopTimeDate);


        //Add the network(s) for this station, in alphabetical order, to the resource group
        if ((networks != null) && (networks.trim().length() > 0)) {
            List<String> toks = new ArrayList<String>();
            for (String tok : networks.split(";")) {
                toks.add(tok.trim());
            }
            Collections.sort(toks);
            for (String tok : (List<String>) toks) {
                site.addResourceGroup(new ResourceGroup(tok));  // this method adds the comma-separated list of network names at a site, to a site object
            }
        }

        // get names of country, province or state, and agency from their id numbers 
        String country = "";
        String state = "";
        String cols="";
        ResultSet qresults;
        List<Clause> clauses = new ArrayList<Clause>();
        List<String> tables = new ArrayList<String>();

        // get name of country
        //  WHERE the test part in the select statement 
        clauses.add(Clause.join(Tables.STATION.COL_NATION_ID, Tables.NATION.COL_NATION_ID));
        //  SELECT what to get from the db (result in rows returned):
        cols=SqlUtil.comma(new String[]{Tables.NATION.COL_NATION_NAME});
        //  FROM   the select from which tables part 
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.NATION.NAME);
        //                                          select  what    from      where               order-by-clause
        Statement  statement = getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        //System.err.println("   SiteManager: country query is " +statement);
        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           // process each line in results of db query  
           while ((qresults = iter.getNext()) != null) {
               country = new String( qresults.getBytes(Tables.NATION.COL_NATION_NAME), "UTF-8"); //qresults.getString(Tables.NATION.COL_NATION_NAME);
               // you want Only read the first row of db query results returned
               break;
           }
        } finally {
           getDatabaseManager().closeAndReleaseConnection(statement);
        }

        // get name of locale or city from cityid     
        String city = " ";
        clauses = new ArrayList<Clause>();
        tables = new ArrayList<String>();
        cols="";
        clauses.add(Clause.join(Tables.STATION.COL_LOCALE_ID, Tables.LOCALE.COL_LOCALE_ID));
        clauses.add(Clause.eq(Tables.LOCALE.COL_LOCALE_ID, cityid));
        cols=SqlUtil.comma(new String[]{Tables.LOCALE.COL_LOCALE_NAME});
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.LOCALE.NAME);
        statement = //select            what    from      where
           getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        //System.err.println("   SiteManager: province query is " +statement);
        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           while ((qresults = iter.getNext()) != null) {
               city = new String( qresults.getBytes(Tables.LOCALE.COL_LOCALE_NAME), "UTF-8"); // qresults.getString(Tables.LOCALE.COL_LOCALE_NAME);
               break;
           }
         } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
         }

        // get name of province or state     
        clauses = new ArrayList<Clause>();
        tables = new ArrayList<String>();
        cols="";
        clauses.add(Clause.join(Tables.STATION.COL_PROVINCE_STATE_ID, Tables.PROVINCE_STATE.COL_PROVINCE_STATE_ID));
        clauses.add(Clause.eq(Tables.PROVINCE_STATE.COL_PROVINCE_STATE_ID, stateid));
        //System.err.println("   SiteManager: clauses is >>>" +clauses+"<<<");
        cols=SqlUtil.comma(new String[]{Tables.PROVINCE_STATE.COL_PROVINCE_STATE_NAME});
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.PROVINCE_STATE.NAME);
        statement = //select            what    from      where
           getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        //System.err.println("   SiteManager: province query is " +statement);
        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           while ((qresults = iter.getNext()) != null) {
               //      System.err.println("   get state name");
               state = new String( qresults.getBytes(Tables.PROVINCE_STATE.COL_PROVINCE_STATE_NAME), "UTF-8"); // qresults.getString(Tables.PROVINCE_STATE.COL_PROVINCE_STATE_NAME);
               //      System.err.println("   did get state name"+state);
               break;
           }
         } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
         }

        // add all three above items to site as "PoliticalLocationMetadata":
        site.addMetadata(new PoliticalLocationMetadata(country, state, city));  

        //  To set this value in MySiteManager.java:
        // System.err.println("   SiteManager:      makeResource:  iersdomes="+iersdomes+"_");
        if (null!= iersdomes && iersdomes.length() > 4) site.addMetadata(new PropertyMetadata(GsacExtArgs.SITE_METADATA_IERDOMES, iersdomes,  "IERS DOMES" ));

        // CHANGE LOOK if mirroring site info from another GSAC
        //site.setMirroredFromURL(originator);   add an agency's URL web page here; have to go through the agency table, using  agencyid    =      results.getInt(Tables.STATION.COL_AGENCY_ID);
        // debug System.err.println("   SiteManager:      makeResource:  station " +fourCharId+ " mirror URL="+site.getMirroredFromURL());
        // LOOK add this value to the site metadata AND make a line showing it on the site HTML web page
        //if (null!=originator ) {  site.addMetadata(new PropertyMetadata(GsacArgs.ARG_SITE_MIRROR_FROM_URL,  originator, "Originator"));}

        // do a db search now for the most recent data time at this site: "LATEST" code.

        // a working mysql approach is do the query like 
        // select datafile_stop_time from datafile where station_id=40 order by datafile_stop_time DESC;
        // and use the first time returned, of many,

        // get value for, and set, ARG_SITE_LATEST_DATA_DATE, the most recent time of any data file in the GSAC database at this site.
        // LOOK you may need to comment out this code block to speed site searches in repositories with a very large number of data files at many sites.
        // with SQL query per site in the datafile table; get latest data time with variables
        // sample sql:  select max(datafile_stop_time) from datafile where station_id=60;
        // SELECT the most recent time in the table 'datafile'
        cols= " max( " + SqlUtil.comma(new String[]{ Tables.DATAFILE.COL_DATAFILE_STOP_TIME }) +") " ;
        // FROM the table 'datafile'
        tables = new ArrayList<String>();
        tables.add(Tables.DATAFILE.NAME);
        // WHERE only for this site by its station_id
        clauses = new ArrayList<Clause>();
        clauses.add(Clause.eq(Tables.DATAFILE.COL_STATION_ID, station_id));
        //System.err.println("   Prototype SiteManager:   for ldt, select "+cols+"  from "+tables +"   where "+ clauses ) ;
        // like  for ldt, select  max( datafile.datafile_stop_time)   from [datafile]   where [datafile.station_id = 47]
        int col = 1 ;
        statement = getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           while ((qresults = iter.getNext()) != null) {
              // handle the first item (and only item) found:
              String latestTimeStr = qresults.getString (col++);
              // System.err.println("   Prototype SiteManager:  SITE's latest data time string ="+ latestTimeStr+"_" ) ;
              Date latestDateObj=null;
              if ( latestTimeStr != null ) {
                 latestDateObj = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(latestTimeStr.substring(0,19) );
              }
              site.setLatestDataDate(latestDateObj);                                                                // this will appear on the SINGLE SITE  HTML page
              // ok System.err.println("   Prototype SiteManager:  did set ldt ="+ latestTimeStr+"_" ) ;
              // addPropertyMetadata( site, GsacArgs.ARG_SITE_LATEST_DATA_DATE, "Latest data time", latestTimeStr ); // this will appear on the Search Sites HTML TABLE of ALL sites found.   
              // but that ^^^ is already handled by core GSL code.
              break;
              }
            } finally {
               ;
            }

        cols=SqlUtil.comma(new String[]{Tables.AGENCY.COL_AGENCY_NAME});
        tables = new ArrayList<String>();
        clauses = new ArrayList<Clause>();
        cols=SqlUtil.comma(new String[]{Tables.AGENCY.COL_AGENCY_NAME});
        //tables.add(Tables.STATION.NAME);
        tables.add(Tables.AGENCY.NAME);
        //clauses.add(Clause.join(Tables.STATION.COL_OPERATOR_AGENCY_ID, Tables.AGENCY.COL_AGENCY_ID));
        clauses.add(Clause.eq(Tables.AGENCY.COL_AGENCY_ID, agencyid));
        statement = getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        //                               select  fields   from     where
        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           while ((qresults = iter.getNext()) != null) {
               String agency = new String( qresults.getBytes(Tables.AGENCY.COL_AGENCY_NAME), "UTF-8"); // Note trick to get odd letters in UTF-8, NOT qresults.getString( Tables.AGENCY.COL_AGENCY_NAME);
               System.err.println("GSAC:       GSAC found site agency  " +agency);
               addPropertyMetadata( site, GsacExtArgs.SITE_METADATA_NAMEAGENCY, "Agency", agency);    
               break;
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }

        return site;
    }


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
        // For the ONE station with the input 4 char ID number gsacResource.getId(), get the metadata for all of its 'equipment sessions':
        readEquipmentMetadata(gsacResource);
        // LOOK for data times search at this station, next check equip session date ranges in the gsacResource thing.
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

        /* make a db query statement to find the site corresponding to the current site or "gsacResource"; the FOUR_CHAR_NAME is stored as the resource's Id, from gsacResource.getId()  */
        /* note that this gets ALL the columns of fields from the table "station" (on the row matching the select Clause.eq item) */
        Statement statement = getDatabaseManager().select( Tables.STATION.COLUMNS, Tables.STATION.NAME,
                Clause.eq( Tables.STATION.COL_FOUR_CHAR_NAME, gsacResource.getId()), (String) null, -1);
        
        // System.err.println("   SiteManager: readIdentificationMetadata select query is " +statement);
        /* a single station query from this is
        SELECT station.station_id,station.code_4char_ID,station.station_name,station.latitude_north,station.longitude_east,station.ellipsoidal_height,station.station_installed_date,station.station_removed_date,station.station_style_id,station.station_status_id,station.access_permission_id,station.monument_description_id,station.country_id,station.province_region_state_id,station.city,station.x,station.y,station.z,station.iers_domes,station.station_photo_URL,station.time_series_image_URL,station.agency_id,station.networks,station.embargo_duration_hours,station.embargo_after_date FROM station 
        WHERE (station.code_4char_ID = 'ATAL')
        */

        // make the db query to find the row of info about this station
        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            // process each line in results of db query  
            while ((results = iter.getNext()) != null) {
                String    staname  =                  results.getString(Tables.STATION.COL_STATION_NAME);
                if (null!=staname) {
                         staname       =   new String( results.getBytes(Tables.STATION.COL_STATION_NAME), "UTF-8");
                }
                // ok gsacResource.setLongName( new String( results.getBytes(Tables.STATION.COL_STATION_NAME), "UTF-8") ); /*results.getString(Tables.STATION.COL_STATION_NAME)*/  
                gsacResource.setLongName( staname ); 

                // get values from the dq query row returned, and then SET x,y,z, the SITE_TRF_X  etc.
                // Note if you add similar but new and different parameters to your data base, you also need to
                // add to the file gsac/trunk/src/org/gsac/gsl/GsacExtArgs.java to declare similar new variables.

                String xstr = results.getString(Tables.STATION.COL_X);
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_TRF_X, "X", xstr);

                String ystr = results.getString(Tables.STATION.COL_Y);
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_TRF_Y, "Y", ystr);

                String zstr = results.getString(Tables.STATION.COL_Z);
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_TRF_Z, "Z", zstr);
                // System.err.println("   SiteManager: readIdentificationMetadata()  site's x,y,z strings = " +xstr+"  "+ystr+"  "+zstr);

                /* get, check, and save value for IERS DOMES. */
                //String iersdomes= results.getString(Tables.STATION.COL_IERS_DOMES);
                String iersdomes = new String( results.getBytes(Tables.STATION.COL_IERS_DOMES), "UTF-8");
                //System.err.println("   SiteManager: readIdentificationMetadata()  iersdomes=  "+iersdomes);
                if (iersdomes == null ) 
                   {iersdomes =   "" ; }
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_METADATA_IERDOMES, "IERS DOMES", iersdomes);

                /*
                // trap bad value "(A9)", an artifact of some IGS site logs,  and replace with empty string. 
                if (idn != null && idn.equals("(A9)") ) 
                   { idn = " " ; }
                else if (idn != null && idn.equals("NULL") ) 
                   { idn = " " ; }
                else if ( idn == null ) 
                   { idn = " " ; }
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_METADATA_IERDOMES, "IERS DOMES", idn);
                */

                // did only the first row of db query results returned
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        
        // db query  to get MONUMENT_DESCRIPTION
        List<Clause> clauses = new ArrayList<Clause>();
        clauses.add(Clause.eq(Tables.STATION.COL_FOUR_CHAR_NAME, gsacResource.getId()));
        // join the table with these 2 values:
        clauses.add(Clause.join  (Tables.STATION.COL_MONUMENT_STYLE_ID, Tables.MONUMENT_STYLE.COL_MONUMENT_STYLE_ID));
        String cols=SqlUtil.comma(new String[]{  Tables.MONUMENT_STYLE.COL_MONUMENT_STYLE_DESCRIPTION});
        List<String> tables = new ArrayList<String>();
        // FROM BOTH the tables 
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.MONUMENT_STYLE.NAME);
        statement =
            getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1); 
        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {
                // Look add this value to the site metadata AND make a line showing it on the site HTML web page labeled "Monument Style" plus ":"
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "Monument Style", 
                     results.getString(Tables.MONUMENT_STYLE.COL_MONUMENT_STYLE_DESCRIPTION) );
                // debug System.err.println(" site manager: set monu desc "+results.getString(Tables.MONUMENT_STYLE.COL_MONUMENT_STYLE_DESCRIPTION));
                // break to Only read the first row of db query results returned
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
       
    }


    /**
     * For the ONE station with the input 4 char ID number gsacResource.getId(), get the metadata for each station session, when
     * generated from the antenna and receiver sessions.
     *
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     */
    private void readEquipmentMetadata(GsacResource gsacResource) throws Exception {
        //        System.err.println("      called  read EquipmentMetadata");
        Date[] datadateRange=null;
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
        //System.err.println("GSAC:  SiteManager:getResource() equip sql query is  SELECT " + cols + "  FROM + tables + "  WHERE  " + clauses );
        statement = getDatabaseManager().select(cols,  tables, Clause.and(clauses), " order by " + Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_START_TIME, -1);   // ORDER By sites equip session select sql
        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {

/*
 String startTimeStr     = results.getString (Tables.STATION.COL_INSTALLED_DATE);
        String stopTimeStr      = results.getString (Tables.STATION.COL_RETIRED_DATE); // note : what if stop time is before start time?
        System.err.println("   SiteManager: station " +fourCharId+ "  startTimeStr ="+startTimeStr+"_ stop-time="+stopTimeStr+"_");

        Date startTimeDate=null;
        if (startTimeStr != null )
            {
                 startTimeDate =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTimeStr.substring(0,19) );
            }

*/
               String sdt= results.getString ( Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_START_TIME);
               Date indate = null;
               if (sdt != null) {
                   indate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdt.substring(0,19) );
               }
               colCnt++;

               String odt= results.getString( Tables.EQUIP_CONFIG.COL_EQUIP_CONFIG_STOP_TIME);
               Date outdate = null;
               if ( odt != null) {
                   outdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(odt.substring(0,19) );

               }
               colCnt++;
               //System.err.println("   SiteManager: station      equip session starts "+sdt+"_ ends ="+odt+"_");

               datadateRange = new Date[] { indate, outdate };

               if (null!=indate && null!=outdate && indate.after(outdate)) {
                    System.err.println("   GSAC DB values ERROR:  dates of equip config session (station "+gsacResource.getId()+")  are reversed: begin time: "+ indate +" is >  end time: "+ outdate);
                    continue;
                 }


                int antid  = results.getInt(Tables.EQUIP_CONFIG.COL_ANTENNA_ID);

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
               // public GnssEquipment(Date[] datadateRange, String antennatype, String antennaSN, String dometype, String domeSerial, String receiver, String receiverSerial, 
               //                       String receiverFirmware,  double zoffset)  
               GnssEquipment equipment_session =
                   new GnssEquipment(datadateRange, ant_type, ant_serial_number, dome_type, dome_serial_number, rcvr_type, rec_serial_number, rec_firmware_vers,antenna_height);

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
                gsacResource.addMetadata(equipmentGroup = new GnssEquipmentGroup()); //  add null object of correct type
            }
            equipmentGroup.add(equipment_session);
        }

        // LOOK what is 'returned' by this method?  probably has updated the input arg gsacResource.
        // gsacResource has a one or more GnssEquipment equipment_session (s),  which has (have) a Date[] object perhaps called like datadateRange

    }  // end of readEquipmentMetadata



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


    /* *
     * From db table represented in Tables.java as class SITELOG_FREQUENCYSTANDARD,
     * get the value of String COL_STANDARDTYPE and add it (with the label "clock") to the GsacResource object "gsacResource".
     * Note: legacy code not based in the current prototype GSAC database schema.
     * In this case the site is recognized in the db with the getDatabaseManager().select() call.
     *
     * SITE_METADATA_FREQUENCYSTANDARD must be declared in  GsacExtArgs.java.
     *
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     *  /
    private void readFrequencyStandardMetadata(GsacResource gsacResource)
            throws Exception {
        // compose db query statement; 'order by' phrase is null.
        Statement statement =
            getDatabaseManager().select(
                Tables.SITELOG_FREQUENCYSTANDARD.COLUMNS,
                Tables.SITELOG_FREQUENCYSTANDARD.NAME,
                Clause.eq( Tables.SITELOG_FREQUENCYSTANDARD.COL_FOURID, gsacResource.getId()), (String) null, -1);
        ResultSet results;
        try {
            // do db query
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            // process each line in results of db query; the GsacExtArgs item must have been added to GsacExtArgs.java.
                // args to addPropertyMetadata() are [see definition of addPropertyMetadata in this file below]:
                // the resource you are adding it to;
                // the label on the web page or results
                // the db column name 
            while ((results = iter.getNext()) != null) {
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_METADATA_FREQUENCYSTANDARD, "Clock", 
                                      results.getString(Tables.SITELOG_FREQUENCYSTANDARD.COL_STANDARDTYPE));
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
    }
    */


     /**
     * Get (all?) the sites' networks. This gets called by the SiteManager.getDefaultCapabilities (base class, not here)
     * Used in query for the station's networks. 'group' is GSAC jargon for gnss networks, sometimes.
     *
     *
     * @return site group list
     */

    public List<ResourceGroup> doGetResourceGroups() {

        List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        // tests onlyreturn groups;

        try {
            System.err.println("       doGetResourceGroups(): ");
            HashSet<String>     seen   = new HashSet<String>();
            //List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
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
     *  Used in query for the station's networks. 'group' is GSAC jargon for gnss networks, sometimes.
     *
     * @param groupIds List of group ids
     * @param msgBuff Search criteria buffer
     *
     * @return List of Clauses
     */
    private List<Clause> getNetworkClauses(List<String> groupIds, StringBuffer msgBuff) {
        List<Clause> groupClauses = new ArrayList<Clause>();
        // tests only return groupClauses;

        String  col = Tables.STATION.COL_NETWORKS;

        int cnt = 0;
        for (String group : groupIds) {
            //System.err.println("       getNetworkClauses(): search for network or group name _"+group+"_");  // shows correct result
            appendSearchCriteria(msgBuff, ((cnt++ == 0) ? "Site Group=" : ""), group);

            // original simple equality :   which does not work where a station has more than one network names in comma separated list
            //Clause cl_one = new Clause();
            //cl_one = Clause.eq(col, group);

            groupClauses.add(Clause.eq(col, group));

            // need clause where the string 'group' is IN the col result
            groupClauses.add(Clause.like(col, SqlUtil.wildCardBoth(group)));

            // other cases which are no help here:
            //groupClauses.add(Clause.like(col, SqlUtil.wildCardBefore("," + group)));
            //groupClauses.add(Clause.like(col, SqlUtil.wildCardAfter(group + ",")));
            //groupClauses.add(Clause.like(col, SqlUtil.wildCardBoth("," + group + ",")));
        }
        return groupClauses;

    }


    /**
     * add this value to the site metadata AND make a line showing it on the Search Sites results HTML web page table, labeled with 'label'  plus ": ".
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
     * NOTE this uses the  ramadda sql package ResultSet class and ONLY returns calendar date -- with NO time of day allowed.
     *
     * @param results a row from a qb query which has a datetime field
     * @param column a string name for a db field with  for example a MySQL 'datetime' object,
     *                such as the String held by Tables.ANTENNA_SESSION.COL_SESSION_START_DATE
     *
     * @return _more_
     */
    private Date readDate(ResultSet results, String column) {
        try {
            // for DATE only with no time of day:   
            return results.getDate(column);
            //return results.getTime(column); // busted always gives 1970-01-01 23:59
        } catch (Exception exc) {
            //if the date is undefined we get an error so we just return null 
            return null;
        }
    }


}  // end of class
