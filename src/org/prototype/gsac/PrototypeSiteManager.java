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

/* a prototype GSAC SiteManager.java file to use in your GSAC code set. See README part 2.
   You will revise this file, changing two instances of "org.prototype" (in next two lines) to match your package name,
   and changing three instances of "Prototype" near lines 84 to your Java file prefix. */
package org.prototype.gsac;
import  org.prototype.gsac.database.*;
/* CHANGEME -  above, make sure that both lines show your GSAC package name */


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.util.*;
import org.gsac.gsl.metadata.gnss.*;
import org.gsac.gsl.output.HtmlOutputHandler;

import ucar.unidata.util.Misc;

import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;

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


/**
 * The GSAC SiteManager classes handle all of a GSAC repository's site(station)-related requests.  
 * For the Prototype GSAC (Geodesy Seamless Archive). 
 * Uses GSAC prototype database tables and columns. 
 * The base class is in gsac/gsl/SiteManager.java.  Each GSAC application instance also has its own site manager, such as src/org/arepo/gsac/ArepoSiteManager.java.
 *
 * This class is one major part of making a new local GSAC; it allows a local GSAC to query the local GSAC database, and handles the results from queries:
 *
 * - what metadata may be queried on, that is used for searches or selections, in this GSAC repository (see method doGetQueryCapabilities below)
 *   either by the web page forms or via the API URL arguments, 
 *
 * - how to query the database for such request (see method getResourceClauses below), and 
 *
 * - how to package up the results from the query (method makeResource below) into a java object for further use, such as for the HTML pages of
 *   search results on the GSAC web site, and the items in other result formats like SINEX.
 *
 * The base class is gsac/gsl/SiteManager.java.  Each GSAC application instance also has its own site manager, such as src/org/arepo/gsac/ArepoSiteManager.java.
 * Code in the SiteManager class is highly dependent on your particular db schema design and its names for tables and columns in tables.
 * This instance of the SiteManager class uses the GSAC Prototype database schema.
 * 
 * @author  Jeff McWhirter 2011 template without code for any particular database variable names.
 * @author  S K Wier, UNAVCO; PrototypeSiteManager 23 Jan 2014.
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

    /** do we get the data ranges */
    private boolean doDateRanges = true;


    /**
     * Create the site search "capabilities", which are all the items to offer to the user to search for sites(stations), either on web page and/or in API queries.  
     *
     * A GSAC search item (Java object) is a "Capability."  Here is where you implement items to use in site queries from this database, 
     * searches from either web site or url api args)
     *
     * A capability here which for example is tied to the value GsacExtArgs.ARG_ANTENNA has corresponding code in the method getResourceClauses which creates a query with it when user does so.
     *
     * (Perhaps a call  to this method goes before makeCapabilities call, so regular site search form appears before advanced search?)
     *
     * This method is called only once, at GSAC server start-up.  Must restart the GSAC server to find new items only detected here, such as gnss file types.
     *
     * LOOK - this method appears to be called twice at server start-up.
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

            String help = HtmlOutputHandler.stringSearchHelp;  /* some mouse over help text */
            // search on site code, the 4 character ID.  Users may use regular expressions such as AB* or P12*
            //        Capability args:                                          "web page label"
            //                                      (? what for)                           " mouse over help text" + other help
            Capability siteCode =
                initCapability(     new Capability(ARG_SITE_CODE, "Code (4 character ID)", Capability.TYPE_STRING), 
                      CAPABILITY_GROUP_SITE_QUERY, "Code (4 character ID) of the station", "Code (4 character ID) of the station. " + help);
            siteCode.setBrowse(true);  /*  which apparently adds these searches to the GSAC web site Browse form */
            capabilities.add(siteCode);

            // search with site full name or partial name
            help="Full name of the site, such as Marshall, or part or name plus wildcard(*) such as Mar*";
            Capability siteName =
                initCapability(     new Capability(ARG_SITE_NAME, "Site Name",             Capability.TYPE_STRING), 
                       CAPABILITY_GROUP_SITE_QUERY, "Name of the site",                    "Name of site.   " + help);
            siteName.setBrowse(true);  /*  which apparently adds these searches to the GSAC web site Browse form */
            capabilities.add(siteName);

            // site search for latitude-longitude bounding boxi; 4 boxes; not in browse service
            capabilities.add(initCapability(new Capability(ARG_BBOX, "Lat-Lon Bounding Box", Capability.TYPE_SPATIAL_BOUNDS), 
                    CAPABILITY_GROUP_SITE_QUERY, "Spatial bounds within which the site lies"));

            // site search by "Data Date Range" pair of boxes;
            // output of site search is an html table with "Date Range" column , showing station's installed date until now; see gsl/output/HtmlOutputHandler.java.
            Capability sitedateRange =
                               initCapability( new Capability(ARG_SITE_DATE_FROM, "Site Includes Dates in Range", Capability.TYPE_DATERANGE), CAPABILITY_GROUP_SITE_QUERY, 
                         "The site operated between these dates", "Site date");
            capabilities.add(sitedateRange);

            String[] values;

            /*
            more possible search items, from the Unavco-gsac server sitemanager code:
            capabilities.add(initCapability(new Capability(ARG_SITE_MODIFYDATE, "Site Modified Date Range", Capability.TYPE_DATERANGE), CAPABILITY_GROUP_ADVANCED,
                        "The site's metadata was modified between these dates"));
            capabilities.add( initCapability( new Capability( ARG_SITE_CREATEDATE, "Site Created Date Range", Capability.TYPE_DATERANGE), CAPABILITY_GROUP_ADVANCED,
                        "The site was created between these dates"));
            */

            //  Advanced search items: "CAPABILITY_GROUP_ADVANCED" search items appear on the web site search page under the "Advanced Site Query" label:

            ResultSet results;
            ArrayList<String> avalues = new ArrayList<String>();
            List<Clause> clauses = new ArrayList<Clause>();
            List<String> tables = new ArrayList<String>();

            // get network(s) names found in each station:
            //  WHERE
            String cols=SqlUtil.comma(new String[]{Tables.STATION.COL_NETWORKS});
            //  FROM which tables (for a table join)
            tables.add(Tables.STATION.NAME);
            //  LOOK? need no clauses get all networks values in rows: 
            //Statement statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            Statement statement = getDatabaseManager().select(cols,  tables,  null,  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query
               while ((results = iter.getNext()) != null) {
                   String networks= results.getString(Tables.STATION.COL_NETWORKS); // comma sep list of names of networks
                   //String code= results.getString(Tables.STATION.COL_STATION_NAME); 
                   /* for tests; show all network names, from each single site, found when GSAC server starts 
                   if (networks  != null ) {
                     System.err.println("      station as network(s) _"+networks+"_");
                   }
                   else {
                     System.err.println("      station has networks value null in the database.");
                   }
                   */
          
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
            String[] itemArray = new String[avalues.size()];
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacArgs.ARG_SITE_GROUP, "Network", values, true, CAPABILITY_GROUP_ADVANCED));


            /* search on site type; to show all station style or types in the database station_style table which will have more than this data center has:
            values = getDatabaseManager().readDistinctValues( Tables.STATION_STYLE.NAME, Tables.STATION_STYLE.COL_STATION_STYLE_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacArgs.ARG_SITE_TYPE, "Site Type", values, true, CAPABILITY_GROUP_ADVANCED));
            */
            // OR
            // SELECT station_style.station_style_name FROM station_session,station,station_style WHERE ((station_session.station_id =  station.station_id) AND (station.station_style_id = station_style.station_style_id));
            // get only site type names (station_style table values) used by stations in this database, only.
            avalues = new ArrayList<String>();
            clauses = new ArrayList<Clause>();
            //  WHERE 
            clauses.add(Clause.join(Tables.STATION.COL_STATION_STYLE_ID, Tables.STATION_STYLE.COL_STATION_STYLE_ID));
            //  SELECT what column values to find
            cols=SqlUtil.comma(new String[]{Tables.STATION_STYLE.COL_STATION_STYLE_NAME});
            //  FROM   
            tables = new ArrayList<String>();
            tables.add(Tables.STATION.NAME);
            tables.add(Tables.STATION_STYLE.NAME);
            statement = getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query  
               while ((results = iter.getNext()) != null) {
                   String statype= results.getString(Tables.STATION_STYLE.COL_STATION_STYLE_NAME);
                   // save distinct values
                   int notfound=1;
                   for (int vi= 0; vi<avalues.size(); vi+=1 ) {
                      if ( avalues.get(vi).equals(statype) ) {
                         notfound=0;
                         break;
                         }
                   }
                   if (notfound==1) {
                         avalues.add(statype);
                         //System.err.println(" this data center has stations with  site type  " + statype ) ;
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

            // search on antenna types: get antenna type names used by stations in this database, only.
            // Since the protoype db has all IGS antenna names, more than 200, show only the ones at stations in this repository .
            avalues = new ArrayList<String>();
            clauses = new ArrayList<Clause>();
            //  WHERE 
            clauses.add(Clause.join(Tables.ANTENNA_SESSION.COL_ANTENNA_TYPE_ID, Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_ID));
            //  SELECT what to 
            cols=SqlUtil.comma(new String[]{Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME});
            //  FROM   
            tables = new ArrayList<String>();
            tables.add(Tables.ANTENNA_SESSION.NAME);
            tables.add(Tables.ANTENNA_TYPE.NAME);
            statement =
               getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query  
               while ((results = iter.getNext()) != null) {
                   String anttype= results.getString(Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME);
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
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_ANTENNA, "Antenna type", values, true, CAPABILITY_GROUP_ADVANCED));




            /* get all radome type names in the db 
            values = getDatabaseManager().readDistinctValues( Tables.RADOME_TYPE.NAME, Tables.RADOME_TYPE.COL_RADOME_TYPE_NAME);
            Arrays.sort(values); capabilities.add(new Capability(GsacExtArgs.ARG_DOME, "Radome type", values, true, CAPABILITY_GROUP_ADVANCED));
            */
            // better-- get only radome type names used by stations in this database, only.  Show choice of only the radome type names at stations in this repository .
            avalues = new ArrayList<String>();
            clauses =      new ArrayList<Clause>();
            //  WHERE
            clauses.add(Clause.join(Tables.ANTENNA_SESSION.COL_RADOME_TYPE_ID, Tables.RADOME_TYPE.COL_RADOME_TYPE_ID));
            //  SELECT what to
            cols=SqlUtil.comma(new String[]{Tables.RADOME_TYPE.COL_RADOME_TYPE_NAME});
            //  FROM which tables (for a table join)
            tables = new ArrayList<String>();
            tables.add(Tables.ANTENNA_SESSION.NAME);
            tables.add(Tables.RADOME_TYPE.NAME);
            statement =
               getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query
               while ((results = iter.getNext()) != null) {
                   String dometype= results.getString(Tables.RADOME_TYPE.COL_RADOME_TYPE_NAME);
                   int notfound=1;
                   for (int vi= 0; vi<avalues.size(); vi+=1 ) {
                      if ( avalues.get(vi).equals(dometype) ) {
                         notfound=0;
                         break;
                         }
                   }
                   if (notfound==1) {
                         avalues.add(dometype);
                         //System.err.println("      in this network a listed radome type  is " +dometype);
                   }
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }
            itemArray = new String[avalues.size()];
            values = avalues.toArray(itemArray);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_DOME, "Radome type", values, true, CAPABILITY_GROUP_ADVANCED));


            // search on country, province/state, and city

            // add box to choose by country
            values = getDatabaseManager().readDistinctValues( Tables.COUNTRY.NAME, Tables.COUNTRY.COL_COUNTRY_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_COUNTRY, "Nation", values, true, CAPABILITY_GROUP_ADVANCED));

            // add box to choose by state or province 
            values = getDatabaseManager().readDistinctValues( Tables.PROVINCE_REGION_STATE.NAME, Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_STATE, "Province / region / state", values, true, CAPABILITY_GROUP_ADVANCED));

            // add box to choose by city
            values = getDatabaseManager().readDistinctValues( Tables.STATION.NAME, Tables.STATION.COL_CITY);  // get all the city (place) names in GSAC's database.
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_CITY, "Place / City", values, true, CAPABILITY_GROUP_ADVANCED));

            // better move this to data file search:
            //  omit for now LOOK - search on data sampling interval ; float value in seconds per sample as 30 or 0.1 or 0.01
            // get value from receiver session table

            return capabilities;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    } // end doGetQueryCapabilities


    /**
     * Make database search clauses, select statements, from the user's choices specified in the web page input or from the URL request arguments' values.  
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
    public List<Clause> getResourceClauses(GsacRequest request, GsacResponse response, List<String> tableNames, StringBuffer msgBuff) {

        /* which tables in the db to search on; the 'from' part of a db query, in this case the station table in the prototype database. */
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
                        "You have some spaces in your search's site code(name).<br>Did you mean to do that, or did you forget to use a semicolon \";\" with no space to delimit multiple site codes?<br>");
                    break;
                }
            }
           addStringSearch(request, ARG_SITE_CODE, ARG_SITE_CODE_SEARCHTYPE, msgBuff, "Site Code", Tables.STATION.COL_CODE_4CHAR_ID, clauses);
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

        // query for the station's networks; "group" is GSAC jargon for gnss network
        if (request.defined(ARG_SITE_GROUP)) {
            List<String> values = (List<String>) request.get(ARG_SITE_GROUP, new ArrayList());
            clauses.add(Clause.or(getNetworkClauses(values, msgBuff)));  // see method def getNetworkClauses () below
        }

        // query for the station's place name 
        if (request.defined(GsacExtArgs.ARG_CITY)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_CITY);
            clauses.add( Clause.or( Clause.makeStringClauses( Tables.STATION.COL_CITY, values)));
        }
 
        // query for the station's dates in use
        try {
            clauses.addAll(getDateRangeClause(request, msgBuff,
                    ARG_SITE_DATE_FROM, ARG_SITE_DATE_TO, "Site date",
                    Tables.STATION.COL_STATION_INSTALLED_DATE,
                    Tables.STATION.COL_STATION_REMOVED_DATE));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        // LOOK might add code for each case below which has values.get(0), to use a loop over i>1 values.get(i) if present, so can make a selection list

        if (request.defined(GsacExtArgs.ARG_COUNTRY)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_COUNTRY);
            tableNames.add(Tables.COUNTRY.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_COUNTRY_ID, Tables.COUNTRY.COL_COUNTRY_ID));
            clauses.add(Clause.eq(Tables.COUNTRY.COL_COUNTRY_NAME, values.get(0)));
        }
        
        if (request.defined(GsacExtArgs.ARG_STATE)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_STATE);
            tableNames.add(Tables.PROVINCE_REGION_STATE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_PROVINCE_REGION_STATE_ID, Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_ID));
            clauses.add(Clause.eq(Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for province " + values.get(0)) ;
        }
        
        if (request.defined(GsacArgs.ARG_SITE_TYPE)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList(GsacArgs.ARG_SITE_TYPE);
            tableNames.add(Tables.STATION_STYLE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_STYLE_ID, Tables.STATION_STYLE.COL_STATION_STYLE_ID));
            clauses.add(Clause.eq(Tables.STATION_STYLE.COL_STATION_STYLE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for style " + values.get(0)) ;
        }
        
        // FIX next three queries are buggy - return > 1 result per correct result
        if (request.defined(GsacExtArgs.ARG_RECEIVER)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_RECEIVER);
            tableNames.add(Tables.RECEIVER_SESSION.NAME);
            tableNames.add(Tables.RECEIVER_TYPE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.RECEIVER_SESSION.COL_STATION_ID));
            clauses.add(Clause.join(Tables.RECEIVER_SESSION.COL_RECEIVER_TYPE_ID, Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_ID));
            clauses.add(Clause.eq(Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for rcvr " + values.get(0)) ;
        }
        
        if (request.defined(GsacExtArgs.ARG_ANTENNA)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_ANTENNA);
            tableNames.add(Tables.ANTENNA_SESSION.NAME);
            tableNames.add(Tables.ANTENNA_TYPE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.ANTENNA_SESSION.COL_STATION_ID));
            clauses.add(Clause.join(Tables.ANTENNA_SESSION.COL_ANTENNA_TYPE_ID, Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_ID));
            clauses.add(Clause.eq(Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for antenna " + values.get(0)) ;
        }
        
        if (request.defined(GsacExtArgs.ARG_DOME)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_DOME);
            tableNames.add(Tables.ANTENNA_SESSION.NAME);
            tableNames.add(Tables.RADOME_TYPE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.ANTENNA_SESSION.COL_STATION_ID));
            clauses.add(Clause.join(Tables.ANTENNA_SESSION.COL_RADOME_TYPE_ID, Tables.RADOME_TYPE.COL_RADOME_TYPE_ID));
            clauses.add(Clause.eq(Tables.RADOME_TYPE.COL_RADOME_TYPE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for radome " + values.get(0)) ;
        }

        // NOTE: the following shows a line like
        //    SiteManager: getResourceClauses gives [(station.networks = 'BOULDER GNSS' OR station.networks LIKE '%BOULDER GNSS%')]
        // which creates, later, the sql based query or API to GSAC:
        //  new request /prototypegsac/gsacapi/site/search?site.code.searchtype=exact&output=site.html&limit=1000&site.group=BOULDER+GNSS&site.name.searchtype=exact
        // System.err.println("   SiteManager: getResourceClauses gives " + clauses) ;

        return clauses;
    } // end of getResourceClauses



    /**
     * Create and return GSAC's internal "resource" (a "site object") identified by the given resource id in this case the CODE_4CHAR_ID; see Tables.java.
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
        // the SQL search clause: select where a column value COL_CODE_4CHAR_ID  = the "resourceId" which is some site 4 char ID entered by the user in the api or search form
        Clause clause = Clause.eq(Tables.STATION.COL_CODE_4CHAR_ID, resourceId);

        // compose the complete select SQL phrase; apply the select clause to the table(s) given. see select ( ) in gsl/database/GsacDatabaseManager.java
        //                                                 DB  .select( what to find (fields),     from which tables,      where clause, )  
        // works ok: Statement statement = getDatabaseManager().select(getResourceSelectColumns(), clause.getTableNames(), clause);
        // and this also has ordering :
        Statement statement = getDatabaseManager().select(getResourceSelectColumns(), clause.getTableNames(), clause,  " order by " + Tables.STATION.COL_CODE_4CHAR_ID, -1);

        //  to test site or file searches: System.err.println("   SiteManager: station select query is " +statement);

        try {
            // do the SQL query, and get results
            ResultSet results = statement.getResultSet();
            // if no result (row) returned, return null here.
            if ( !results.next()) {
                results.close();
                return null;
            }
            // make a GsacSite object when a query is made, from db query results (row) ( but not yet made a web page or return anything for an API rquest)
            GsacSite site = (GsacSite) makeResource(results);
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
     *   Set this to what you want to sort on   ; station 4 char ID, ASC means ascending ie from A to Z top to bottom.         
     */
    private static final String SITE_ORDER = " ORDER BY  " + Tables.STATION.COL_CODE_4CHAR_ID + " ASC ";


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
     * Create a single 'site':  make a GsacSite object which has site metadata (for display in web page, or to send to user as 'results' in some form determined by an OutputHandler class).
     * input "results" is one row got from the db query, a search on stations.
     * Previous code to this call did a db select clause to get one (or more?) rows in the db station table for one (or more?) site ids
     *
     * @param results db results
     *
     * @return the site
     *
     * @throws Exception on badness
     */
    @Override
    public GsacResource makeResource(ResultSet results) 
        throws Exception {
        // depends on 'station' table in the database

        // access values by name of field in database row: 

        // to fix busted java-jdbc reading of names in Icelandic or other non-latin characters which are correct in the mysql db:
        String    staname  =                  results.getString(Tables.STATION.COL_STATION_NAME);
        if (null!=staname) {
                 staname       =   new String( results.getBytes(Tables.STATION.COL_STATION_NAME), "UTF-8");
        }

        String     city        =     results.getString(Tables.STATION.COL_CITY);
        if (null!= city) {
                    city =  new String( results.getBytes(Tables.STATION.COL_CITY), "UTF-8");
        }

        String iersdomes =     results.getString(Tables.STATION.COL_IERS_DOMES);
        String station_photo_URL = results.getString(Tables.STATION.COL_STATION_PHOTO_URL);
        String networks  =     results.getString(Tables.STATION.COL_NETWORKS);
        String fourCharId    =  results.getString(Tables.STATION.COL_CODE_4CHAR_ID);  // not a var char so does not work 
        double latitude =      results.getDouble(Tables.STATION.COL_LATITUDE_NORTH);
        double longitude =     results.getDouble(Tables.STATION.COL_LONGITUDE_EAST);
        double ellipsoid_hgt = results.getDouble(Tables.STATION.COL_ELLIPSOIDAL_HEIGHT);
        int station_style_id = results.getInt(Tables.STATION.COL_STATION_STYLE_ID);
        int countryid    =     results.getInt(Tables.STATION.COL_COUNTRY_ID);
        int stateid      =     results.getInt(Tables.STATION.COL_PROVINCE_REGION_STATE_ID);
        int agencyid    =      results.getInt(Tables.STATION.COL_AGENCY_ID); // or getLong
        int monument_description_id = results.getInt(Tables.STATION.COL_MONUMENT_DESCRIPTION_ID);
        String ts_image_URL =  results.getString(Tables.STATION.COL_TIME_SERIES_IMAGE_URL);
        int access_permission_id    = results.getInt(Tables.STATION.COL_ACCESS_PERMISSION_ID);
        /* 
        if (1== access_permission_id ) {
            System.err.println("   GSAC found station with no access permission (no public views allowed) " +fourCharId);
            GsacSite site = new GsacSite();
            return site;
        }
        */
         
        /*  Make a site object: GsacSite ctor in src/org/gsac/gsl/model/GsacSite.java is 
         public          GsacSite(String siteId, String siteCode, String name, double latitude, double longitude, double elevation) 
         * the so-called elevation, but actually GSAC like GNSS data, uses height above reference ellipsoid, not elevation which is height above some geoid model surface.
        */
        GsacSite site = new GsacSite(fourCharId, fourCharId, staname, latitude, longitude, ellipsoid_hgt);

        // handle search on date range:
        Date fromDate=readDate(results,  Tables.STATION.COL_STATION_INSTALLED_DATE);
        Date toDate=  readDate(results,  Tables.STATION.COL_STATION_REMOVED_DATE);
        //System.err.println("   SiteManager: station " +fourCharId+ " has installed date from "+fromDate);
        //System.err.println("   SiteManager: station " +fourCharId+ " has installed date to   "+toDate);
        if (toDate != null )
            {
            //System.err.println("   SiteManager: station " +fourCharId+ " has installed date to   "+toDate);
            }
        else
            {
            toDate = new Date(); // "now" ie still operating
            //System.err.println("   SiteManager: station " +fourCharId+ " installed to-date was NULL; now is "+toDate);
            }

        // set these additional values in the site object.
        site.setFromDate(fromDate);  // uses gsl/model/GsacResource.java: public void setFromDate(Date value), probably
        site.setToDate(toDate);

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

        // get names of country, province or state, and agency from their id numbers 
        String country = "";
        String state = "";
        String cols="";
        ResultSet qresults;
        List<Clause> clauses = new ArrayList<Clause>();
        List<String> tables = new ArrayList<String>();
        // get name of country
        //  WHERE the test part in the select statement 
        clauses.add(Clause.join(Tables.STATION.COL_COUNTRY_ID, Tables.COUNTRY.COL_COUNTRY_ID));
        //  SELECT what to get from the db (result in rows returned):
        cols=SqlUtil.comma(new String[]{Tables.COUNTRY.COL_COUNTRY_NAME});
        //  FROM   the select from which tables part 
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.COUNTRY.NAME);
        Statement  statement = //select what    from      where
           getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
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

        // get name of province or state     
        clauses = new ArrayList<Clause>();
        tables = new ArrayList<String>();
        cols="";
        clauses.add(Clause.join(Tables.STATION.COL_PROVINCE_REGION_STATE_ID, Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_ID));
        clauses.add(Clause.eq(Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_ID, stateid));
        cols=SqlUtil.comma(new String[]{Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_NAME});
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.PROVINCE_REGION_STATE.NAME);
        statement = //select            what    from      where
           getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        //System.err.println("   SiteManager: province query is " +statement);
        try {
           SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
           while ((qresults = iter.getNext()) != null) {
               //      System.err.println("   get state name");
               state = new String( qresults.getBytes(Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_NAME), "UTF-8"); // qresults.getString(Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_NAME);
               //      System.err.println("   did get state name"+state);
               break;
           }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }

        // add all three aboce items to site as "PoliticalLocationMetadata":
        site.addMetadata(new PoliticalLocationMetadata(country, state, city));  

        // following code section is in effect readAgencyMetadata(site);
        clauses = new ArrayList<Clause>();
        tables = new ArrayList<String>();
        cols="";
        clauses.add(Clause.join(Tables.STATION.COL_AGENCY_ID, Tables.AGENCY.COL_AGENCY_ID));
        clauses.add(Clause.eq(Tables.AGENCY.COL_AGENCY_ID, agencyid));
        cols=SqlUtil.comma(new String[]{Tables.AGENCY.COL_AGENCY_NAME});
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.AGENCY.NAME);
        statement = //select            what    from      where
           getDatabaseManager().select (cols,  tables,  Clause.and(clauses),  (String) null,  -1);
        //System.err.println("   SiteManager: province query is " +statement);
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
        
        // add URL(s) of image(s) here; which will appear on web page of one station's results, in a tabbed window
        MetadataGroup imagesGroup = null;
        if ( station_photo_URL != null || ts_image_URL.length()>8 ) { //ts_image_URL!=null ) {

            if (imagesGroup == null) {
                site.addMetadata(imagesGroup = new MetadataGroup("Images", MetadataGroup.DISPLAY_TABS));
            }

            if ( station_photo_URL != null ) {
                // add  site photo image to the group:
                imagesGroup.add( new ImageMetadata( station_photo_URL, "Site Photo"));
            }

            if (ts_image_URL.length()>8 ) { //(ts_image_URL!=null) ) {
                // add image of a time series data plot to the images group:
                imagesGroup.add( new ImageMetadata(ts_image_URL, "Time Series Data Plot"));
            }
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
        */
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
        // more station styles ... FIX 
        else if (8  == station_style_id ) {
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
        readEquipmentMetadata(gsacResource);
    }

    /**
     * Get station metadata for the given site; this called when user clicks on a site name in the sites' search results table on the table web page (and when other output demand is made possibly).
     * sets values in some GsacExtArgs.
     * This method adds new items and text to, at least, the HTML page of results.
     *
     * @param gsacResource resource
     *
     * @throws Exception On badness
     */
   private void readIdentificationMetadata(GsacResource gsacResource)
            throws Exception {

        ResultSet results;

        /* make a db query statement to find the site corresponding to the current site or "gsacResource"; the CODE_4CHAR_ID is stored as the resource's Id, from gsacResource.getId()  */
        Statement statement = getDatabaseManager().select( Tables.STATION.COLUMNS, Tables.STATION.NAME,
                Clause.eq( Tables.STATION.COL_CODE_4CHAR_ID, gsacResource.getId()), (String) null, -1);
        //System.err.println("   SiteManager: readIdentificationMetadata select query is " +statement);

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

                // get values from the dq query row returned, and then et for x,y,z, the SITE_TRF_X  etc.
                // Note if you add similar but new and different parameters to your data base, you also need to
                // add to the file gsac/trunk/src/org/gsac/gsl/GsacExtArgs.java to declare similar new variables.
                // The var names "Tables..." comes from the Tables.java file made when you built with ant maketables with your new database.
                String xstr = results.getString(Tables.STATION.COL_X);
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_TRF_X, "X", xstr);

                String ystr = results.getString(Tables.STATION.COL_Y);
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_TRF_Y, "Y", ystr);

                String zstr = results.getString(Tables.STATION.COL_Z);
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_TRF_Z, "Z", zstr);

                /* get, check, and save value for IERS DOMES. */
                String idn= results.getString(Tables.STATION.COL_IERS_DOMES);
                /* trap bad value "(A9)", an artifact of some IGS site logs,  and replace with empty string.  */
                if (idn != null && idn.equals("(A9)") ) 
                   { idn = " " ; }
                else if (idn != null && idn.equals("NULL") ) 
                   { idn = " " ; }
                else if ( idn == null ) 
                   { idn = " " ; }
                // note an empty string idn="" will NOT make a line in the web page output, so use " " so you know there is missing information about iers domes value.
                // add value to results from GSAC searches:
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_METADATA_IERDOMES, "IERS DOMES", idn);

                // only red the first row of db query results returned
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        // db query  to get MONUMENT_DESCRIPTION
        List<Clause> clauses = new ArrayList<Clause>();
        clauses.add(Clause.eq(Tables.STATION.COL_CODE_4CHAR_ID, gsacResource.getId()));
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
                // arg "monument" appears as a label in the HTML page about one station.
                //Only read the first row of db query results returned
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

    }


    /**
     * For the station with the input 4 char ID number gsacResource.getId(), get the metadata for each station session, when
     * generated from the antenna and receiver sessions.
     *
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     */
    private void readEquipmentMetadata(GsacResource gsacResource)
            throws Exception {
               
        int station_sess_id=0;
        int receiverid=0;
        int receiver_firmware_id=0;
        int antennaid=602; // means ' antenna info is missing', in gsac protoytpe db; not especially important
        int radomeid=0;
        float dnorth=0.0f;
        float deast=0.0f;
        float sampInt=0.0f;
        Double zoffset=0.0;
        String antenna_serial=" ";
        String receiver_serial=" ";
        String anttype=" ";
        String rcvrtype=" ";
        String rcvrfw=" ";
        String swver=" ";
        String radometype=" ";
        String satellitesys=" ";
        Date indate=null;
        Date outdate=null;
        Date[] dateRange=null;

        Hashtable<Date, GnssEquipment> equip_sessions = new Hashtable<Date, GnssEquipment>();
        List<GnssEquipment> equipmentList = new ArrayList<GnssEquipment>();
        Statement           statement;
        ResultSet           results;
        List<Clause> clauses = new ArrayList<Clause>();
        List<String> tables = new ArrayList<String>();
        String cols;

        List<Date>  antstartDates= new ArrayList<Date>();
        List<Date>  rcvstartDates=new ArrayList<Date>();
        
        List<Date>  startDates= new ArrayList<Date>();
        List<Date>  stopDates =new ArrayList<Date>();

        List<Date[]>  sessionDates=new ArrayList<Date[]>(); // a list if pairs of dates in dateRange objects; each equipment session start end end time.

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

        //System.err.println("\n  --------------------------------------------------------------- get times of equip sessions at station "+gsacResource.getId());
        //System.err.println("\n  --------------------------------------------------------------- readEquipmentMetadata at "+gsacResource.getId());

        // get antenna session time intervals
        // mysql> select antenna_session.antenna_installed_date,antenna_session.antenna_removed_date from station,antenna_session  where ( (station.code_4char_ID like "%ORID%") and station.   
        //   station_id=antenna_session.station_id and (antenna_installed_date < antenna_session.antenna_removed_date OR antenna_removed_date="0000-00-00 00:00:00" )
        //   and antenna_installed_date is not null ) order by antenna_session.antenna_installed_date;
        //+------------------------+----------------------+
        //| antenna_installed_date | antenna_removed_date |
        //+------------------------+----------------------+
        //| 2000-07-20 00:00:00    | 2002-10-31 12:00:00  |
        //| 2002-10-31 12:00:00    | 2002-12-13 12:00:00  |
        //| 2002-12-13 12:00:00    | 2008-11-04 20:00:00  |
        //| 2008-11-06 07:00:00    | 0000-00-00 00:00:00  |
        //+------------------------+----------------------+
        // WHERE  this station is id-ed by its 4 char id:
        clauses.add(Clause.eq(Tables.STATION.COL_CODE_4CHAR_ID, gsacResource.getId())); 
        // and where the antenna session has the station id number
        clauses.add(Clause.join(Tables.ANTENNA_SESSION.COL_STATION_ID, Tables.STATION.COL_STATION_ID)); 
        // AA mysql select WHAT:  list is matched with line "BB" below.
        cols=SqlUtil.comma(new String[]{ Tables.ANTENNA_SESSION.COL_ANTENNA_INSTALLED_DATE, Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE });
        // FROM these tables
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.ANTENNA_SESSION.NAME);
        statement = getDatabaseManager().select(cols,  tables, Clause.and(clauses), " order by " + Tables.ANTENNA_SESSION.COL_ANTENNA_INSTALLED_DATE, -1);
        int ni=0;
        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {
               // code to get CORRECT times with hours mins and seconds:

               String sdt=null;
               //System.err.println("   get antenna sdt indate string  "); //bbb
               try {
                   sdt = results.getString(Tables.ANTENNA_SESSION.COL_ANTENNA_INSTALLED_DATE);
                } catch (Exception exc) {
                    //System.err.println("   BAD antenna results.getString  "); 
                   System.err.println("   GSAC DB values ERROR:  station "+gsacResource.getId()+" has invalid ANTENNA_INSTALLED_DATE");
                    continue;  //throw new RuntimeException(exc);
                }

               // trap missing installed date
               if  (  sdt == null ) {
                   System.err.println("   GSAC DB values ERROR:  station "+gsacResource.getId()+" has zero ANTENNA_INSTALLED_DATE");
                   continue;
               }
               // if returned time has this precision: 2008-11-04 20:00:00.0
               if (sdt.length() == 21) {sdt = sdt +"00"; } // extend .0 tenth seconds to .000 ms value
               else if (sdt.length() == 19) {sdt = sdt +".000"; } //  if got like 2008-11-04 20:00:00
               else if (sdt.length() == 16) {sdt = sdt +":00.000"; } //  if got like 2008-11-04 20:00
               indate = formatter.parse(sdt); 
               //System.err.println("              sdt indate string = "+sdt); // CORRECT with time of day

               String odt=null;
               //System.err.println("   get antenna sdt outdate string  "); //bbb
               Date test = readDate( results, Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE);
               if (null == test) { 
                   //System.err.println("   get antenna sdt outdate null  "); //bbb
                   outdate = new Date();  // ie now
               } 
               else {
                  odt = results.getString(Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE)+"00";
                  //System.err.println("        not null odt  string = "+odt); // CORRECT with time of day
                  outdate = formatter.parse(odt);
               }
               // these value are CORRECT and include hours minutes and seconds:
               //System.err.println("   antenna session times = "+ sdt +"   | "+ odt );
               //System.err.println("   antenna session times = "+ ft.format(indate) +"   | "+ft.format(outdate) );
               startDates.add(indate);
               stopDates.add(outdate) ;

               antstartDates.add(indate);

               if (null!=indate && null!=outdate && indate.after(outdate)) {
                    System.err.println("   GSAC DB values ERROR:  Dates of antenna session (station "+gsacResource.getId()+")  are reversed: begin time: "+ indate +"  end time: "+ outdate);
                    continue;
                 }
               ni+=1;
            } // end while  ((results = iter.getNext()) != null)
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }


        // get receiver session time intervals
        clauses = new ArrayList<Clause>();
        tables = new ArrayList<String>();
        // WHERE  this station is id-ed by its 4 char id:
        clauses.add(Clause.eq(Tables.STATION.COL_CODE_4CHAR_ID, gsacResource.getId()));
        // and where the row for each receiver session has the station id number
        clauses.add(Clause.join(Tables.RECEIVER_SESSION.COL_STATION_ID, Tables.STATION.COL_STATION_ID));
        // AA mysql select WHAT:  list is matched with line "BB" below.
        cols=SqlUtil.comma(new String[]{ Tables.RECEIVER_SESSION.COL_RECEIVER_INSTALLED_DATE, Tables.RECEIVER_SESSION.COL_RECEIVER_REMOVED_DATE });
        // FROM these tables
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.RECEIVER_SESSION.NAME);
        statement = getDatabaseManager().select(cols,  tables, Clause.and(clauses), " order by " + Tables.RECEIVER_SESSION.COL_RECEIVER_INSTALLED_DATE, -1);
        ni=0;
        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {

               String sdt=null;
               //System.err.println("   get rcvr  sdt indate string  "); //bbb
               try {
                   sdt = results.getString(Tables.RECEIVER_SESSION.COL_RECEIVER_INSTALLED_DATE);
                } catch (Exception exc) {
                    //System.err.println("   BAD rcvr results.getString  "); 
                    System.err.println("   GSAC DB values ERROR:  station "+gsacResource.getId()+" has invalid RECEIVER_INSTALLED_DATE");
                    continue;  //throw new RuntimeException(exc);
                }
               //System.err.println("              sdt indate string = "+sdt); // CORRECT with time of day

               // trap missing installed date
               if  (  sdt == null ) {
                   System.err.println("   GSAC DB values ERROR:  station "+gsacResource.getId()+" has no or zero rcvr INSTALLED_DATE");
                   continue;
               }
               sdt = sdt +"00";// extend .0 tenth seconds to .000 ms value; LOOK check for other strings of time
               String odt = null;
               indate = formatter.parse(sdt); 

               Date test = readDate( results, Tables.RECEIVER_SESSION.COL_RECEIVER_REMOVED_DATE);
               if (null == test) { 
                   // normal end data case of 0, ie now. System.err.println("   rcvr odt null (0) results.getString  "); 
                   outdate = new Date();  // ie now
               } 
               else { 
                  odt = results.getString(Tables.RECEIVER_SESSION.COL_RECEIVER_REMOVED_DATE)+"00";
                  //System.err.println("        odt  string = "+odt); // CORRECT with time of day
                  outdate = formatter.parse(odt); 
               }
               // these value are CORRECT and include hours minutes and seconds:
               //System.err.println("   receiver session times = "+ ft.format(indate) +"   | "+ft.format(outdate) );
               startDates.add(indate);
               stopDates.add(outdate) ;
               rcvstartDates.add(indate);
               if (null!=indate && null!=outdate && indate.after(outdate)) {
                    System.err.println("   GSAC DB values ERROR:  Dates of receiver session (station "+gsacResource.getId()+") are reversed: begin time: "+ indate +"  end time: "+ outdate);
                    continue;
                 }
               ni+=1;
            } // end while 
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        // final error check: if found no ant or rcv session times:
        if ( rcvstartDates.size() == 0 || antstartDates.size()==  0) { 
              // it's OK to return from this method call with empty results:
              GnssEquipmentGroup equipmentGroup = null;
              gsacResource.addMetadata(equipmentGroup = new GnssEquipmentGroup());
              return;
        }
        // FIX above: LOOK if receiver data only, just use that to make an equip session; ditto for antenna only
        // else {      System.err.println("  NO equipment sessions in GSAC database for station "+gsacResource.getId()); 
        // }

        Collections.sort(startDates);
        Collections.sort(stopDates);
        
        // make the equip sessions'  start-stop time pairs
        Date goodstart =startDates.get(0); // the current equip session's start time
        Date stop = null;
        Date start = null;
        int si=0;
        int eqsi=1;
        for (int ai= 1; ai<startDates.size(); ai+=1 ) {
            // start time of the NEXT session...
            start = startDates.get(ai);

            // finish the current session...  startDates often has duplicate times
            if ( start.after(goodstart) ) {
                // find next available stop time after the goodstart time
                for ( si=0; si<stopDates.size(); si+= 1) {
                    stop  = stopDates.get(si);
                    if (stop.after(goodstart)) {
                       if (1==ai) {  // fix odd bug; finds two cases for first session
                         break;
                       }
                       else {
                          dateRange = new Date[] { goodstart, stop };
                          sessionDates.add(dateRange);
                          //System.err.println("    station "+gsacResource.getId()+"  Dates of equip session  "+eqsi+"    "+ ft.format(goodstart) +"   -   "+ ft.format(stop));
                          eqsi+=1;
                          break;
                       }
                    }
                }
                goodstart = start;
            }
            else {
               continue; // continue to look for the next new start time > current goodstart time
            }
        } // end loop on start dates
        // finish the last session times:
        for ( si=0; si<stopDates.size(); si+= 1) {
            stop  = stopDates.get(si);
            if (stop.after(goodstart)) {
               dateRange = new Date[] { goodstart, stop };
               sessionDates.add(dateRange);
               //System.err.println("    station "+gsacResource.getId()+"  Dates of equip session  "+eqsi+"     "+ ft.format(goodstart) +"   -   "+ ft.format(stop));
               break;
            }
        }


        // make equip sessions' metadata data objects for each equip session at this station 
        int antsii=0;
        for ( si=0; si<sessionDates.size(); si+= 1) {
            //System.err.println("\n       make equip session data objects for session "+ (si+1) );

            clauses = new ArrayList<Clause>();
            // WHERE  this station is id-ed by its 4 char id:
            clauses.add(Clause.eq(Tables.STATION.COL_CODE_4CHAR_ID, gsacResource.getId())); 
            // and where the antenna session has the station id number
            clauses.add(Clause.join(Tables.ANTENNA_SESSION.COL_STATION_ID, Tables.STATION.COL_STATION_ID)); 
            Date astartDate= (sessionDates.get(si))[0];
            Date astopDate= (sessionDates.get(si))[1];
            dateRange = sessionDates.get(si);
            // get info for whichever one antenna session spans this equipment session
            clauses.add(Clause.le(Tables.ANTENNA_SESSION.COL_ANTENNA_INSTALLED_DATE, astartDate));
            // adding this next clause this fails sql select  when ANTENNA_REMOVED_DATE is a valid 00 entry = "now" ie not removed; code below handles this case.
            //clauses.add(Clause.ge(Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE, astopDate));
            // removing this clause finds many antenna sessions;
            cols=SqlUtil.comma(new String[]{
                 Tables.ANTENNA_SESSION.COL_ANTENNA_SESSION_ID ,
                 Tables.ANTENNA_SESSION.COL_ANTENNA_INSTALLED_DATE ,
                 Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE ,
                 Tables.ANTENNA_SESSION.COL_ANTENNA_TYPE_ID ,
                 Tables.ANTENNA_SESSION.COL_ANTENNA_SERIAL_NUMBER , // not a number; is a varchar (20) String
                 Tables.ANTENNA_SESSION.COL_ANTENNA_OFFSET_UP ,
                 Tables.ANTENNA_SESSION.COL_ANTENNA_OFFSET_NORTH ,
                 Tables.ANTENNA_SESSION.COL_ANTENNA_OFFSET_EAST ,
                 Tables.ANTENNA_SESSION.COL_RADOME_TYPE_ID 
             });
            // FROM these tables
            tables = new ArrayList<String>();
            tables.add(Tables.STATION.NAME);
            tables.add(Tables.ANTENNA_SESSION.NAME);
            // compose the db query string using GSAC code:
            //                               select what    from      where
            statement = getDatabaseManager().select(cols,  tables, Clause.and(clauses), (String) null, -1);
            //System.err.println("   mysql: "+statement);
            // for the time range constraint, should get only one row!
            /*
            desc antenna_session;
            +------------------------+-----------------+------+-----+---------+----------------+
            | Field                  | Type            | Null | Key | Default | Extra          |
            +------------------------+-----------------+------+-----+---------+----------------+
            | antenna_session_id     | int(5) unsigned | NO   | PRI | NULL    | auto_increment |
            | station_id             | int(6) unsigned | NO   |     | NULL    |                |
            | antenna_type_id        | int(5) unsigned | NO   |     | NULL    |                |
            | antenna_serial_number  | varchar(20)     | NO   |     | NULL    |                |
            | antenna_installed_date | datetime        | NO   |     | NULL    |                |
            | antenna_removed_date   | datetime        | NO   |     | NULL    |                |
            | antenna_offset_up      | float           | NO   |     | NULL    |                |
            | antenna_offset_north   | float           | NO   |     | NULL    |                |
            | antenna_offset_east    | float           | NO   |     | NULL    |                |
            | antenna_HtCod          | char(5)         | YES  |     | NULL    |                |
            | radome_type_id         | int(5) unsigned | YES  |     | NULL    |                |
            +------------------------+-----------------+------+-----+---------+----------------+
            */
            try {
                SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
                while ((results = iter.getNext()) != null) {
                           //System.err.println("         Dates of equip session  "+(si+1)+"     "+ ft.format(astartDate) +"   -   "+ ft.format(astopDate));

                           // code to get CORRECT times with hours mins and seconds:
                           String sdt = results.getString(Tables.ANTENNA_SESSION.COL_ANTENNA_INSTALLED_DATE)+"00";
                           String odt = null;
                           //System.err.println("   sdt antstart string = "+sdt); // CORRECT with time of day
                           Date antstart = formatter.parse(sdt);
                           Date antstop = new Date();  // ie now

                           Date test = readDate( results, Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE);
                           if (null == test) {
                              ;
                           }
                           else {
                              odt = results.getString(Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE)+"00";
                              //System.err.println("        odt  string = "+odt); // CORRECT with time of day
                              antstop = formatter.parse(odt);
                           }
                           // these value are CORRECT and include hours minutes and seconds:
                           //System.err.println("   antenna session times = "+ sdt +"   | "+ odt );
                           //System.err.println("   antenna session times = "+ ft.format(antstart) +"   | "+ft.format(antstop) );

                            //if (ad1 <= astart                                && ad2 >= astopdate ) is ok
                            if  (antstart.compareTo(astartDate) <= 0 && antstop.compareTo(astopDate) >= 0) {
                                ;
                            }
                            else { //System.err.println("           skip it");
                                continue;
                            }
                            //System.err.println("         GOOD     anten session  "+(antsii+1)+"     "+ ft.format(antstart) +"   -   "+ ft.format(antstop));

                            antsii+=1;
                            antennaid = results.getInt(Tables.ANTENNA_SESSION.COL_ANTENNA_TYPE_ID);
                            antenna_serial = results.getString(Tables.ANTENNA_SESSION.COL_ANTENNA_SERIAL_NUMBER);
                            zoffset = results.getDouble(Tables.ANTENNA_SESSION.COL_ANTENNA_OFFSET_UP);
                            dnorth = results.getFloat(Tables.ANTENNA_SESSION.COL_ANTENNA_OFFSET_NORTH);
                            deast= results.getFloat(Tables.ANTENNA_SESSION.COL_ANTENNA_OFFSET_EAST);
                            radomeid = results.getInt(Tables.ANTENNA_SESSION.COL_RADOME_TYPE_ID);
                            //sampInt= results.getFloat(Tables.ANTENNA_SESSION.COL_SAMPLE_INTERVAL);
                            //satellitesys = results.getString(Tables.ANTENNA_SESSION.COL_SATELLITE_SYSTEM);

                            // get value of ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME
                            anttype = null;
                            clauses = new ArrayList<Clause>();
                            //  WHERE  this antenna type id key value is equal to
                            clauses.add(Clause.eq(Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_ID, antennaid) );
                            cols=SqlUtil.comma(new String[]{Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME});
                            tables = new ArrayList<String>();
                            tables.add(Tables.ANTENNA_TYPE.NAME);
                            statement =
                               getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
                            try {
                               SqlUtil.Iterator iter2 = getDatabaseManager().getIterator(statement);
                               while ((results = iter2.getNext()) != null) {
                                   anttype= results.getString(Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME);
                                   //System.err.println("    SiteManager: read equip got antenna type "+ anttype );
                                   //break; // one row only
                               }
                            } finally {
                                   ; //getDatabaseManager().closeAndReleaseConnection(statement);
                            }

                            // get value of RADOME_TYPE.COL_RADOME_TYPE_NAME
                            ArrayList<String> avalues = new ArrayList<String>();
                            clauses =  new ArrayList<Clause>();
                            tables =   new ArrayList<String>();
                            clauses.add(Clause.eq(Tables.RADOME_TYPE.COL_RADOME_TYPE_ID, radomeid) );
                            cols=SqlUtil.comma(new String[]{Tables.RADOME_TYPE.COL_RADOME_TYPE_NAME});
                            tables.add(Tables.RADOME_TYPE.NAME);
                            statement =
                               getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
                            try {
                               SqlUtil.Iterator iter3 = getDatabaseManager().getIterator(statement);
                               while ((results = iter3.getNext()) != null) {
                                   radometype= results.getString(Tables.RADOME_TYPE.COL_RADOME_TYPE_NAME);
                                   if (radometype.equals("unkn"))
                                      { radometype=" "; }
                                   //System.err.println("    SiteManager: read equip got radome type "+ radometype);
                                   //break;
                               }
                            } finally {
                                   getDatabaseManager().closeAndReleaseConnection(statement);
                            }
                       //System.err.println("     ant type id"+antennaid+" ant sn "+antenna_serial+"  delta z nor east "+zoffset+" "+dnorth+" "+deast+"  radome id "+radomeid+"  antenna type "+ anttype);
                       }
               } finally {
                     getDatabaseManager().closeAndReleaseConnection(statement);
               }



            // get receiver metadata: BBB
            /* mysql> desc receiver_session;
            +------------------------------+-----------------+------+-----+---------+----------------+
            | Field                        | Type            | Null | Key | Default | Extra          |
            +------------------------------+-----------------+------+-----+---------+----------------+
            | receiver_session_id          | int(5) unsigned | NO   | PRI | NULL    | auto_increment |
            | station_id                   | int(6) unsigned | NO   |     | NULL    |                |
            | receiver_type_id             | int(5) unsigned | NO   |     | NULL    |                |
            | receiver_firmware_version_id | int(5) unsigned | NO   |     | NULL    |                |
            | receiver_serial_number       | varchar(20)     | NO   |     | NULL    |                |
            | receiver_installed_date      | datetime        | NO   |     | NULL    |                |
            | receiver_removed_date        | datetime        | YES  |     | NULL    |                |
            | receiver_sample_interval     | float           | YES  |     | NULL    |                |
            | satellite_system             | varchar(60)     | YES  |     | NULL    |                |
            +------------------------------+-----------------+------+-----+---------+----------------+
            */
            clauses = new ArrayList<Clause>();
            // WHERE  this station is id-ed by its 4 char id:
            clauses.add(Clause.eq(Tables.STATION.COL_CODE_4CHAR_ID, gsacResource.getId())); 
            // and where the session has the station id number
            clauses.add(Clause.join(Tables.RECEIVER_SESSION.COL_STATION_ID, Tables.STATION.COL_STATION_ID)); 
            astartDate= (sessionDates.get(si))[0];
            astopDate= (sessionDates.get(si))[1];
            dateRange = sessionDates.get(si);
            // get info for whichever one antenna session spans this equipment session
            clauses.add(Clause.le(Tables.RECEIVER_SESSION.COL_RECEIVER_INSTALLED_DATE, astartDate));
            // adding this next clause this fails sql select  when RECEIVER_REMOVED_DATE is a valid 00 entry = "now" ie not removed; code below handles this case.
            //clauses.add(Clause.ge(Tables.RECEIVER_SESSION.COL_RECEIVER_REMOVED_DATE, astopDate));
            // removing this clause finds many antenna sessions;
            cols=SqlUtil.comma(new String[]{
                 Tables.RECEIVER_SESSION.COL_RECEIVER_SESSION_ID ,
                 Tables.RECEIVER_SESSION.COL_RECEIVER_INSTALLED_DATE ,
                 Tables.RECEIVER_SESSION.COL_RECEIVER_REMOVED_DATE ,
                 Tables.RECEIVER_SESSION.COL_RECEIVER_TYPE_ID ,
             Tables.RECEIVER_SESSION.COL_RECEIVER_TYPE_ID , 
             Tables.RECEIVER_SESSION.COL_RECEIVER_FIRMWARE_VERSION_ID , 
             Tables.RECEIVER_SESSION.COL_RECEIVER_SERIAL_NUMBER,   
             Tables.RECEIVER_SESSION.COL_SATELLITE_SYSTEM,       
             Tables.RECEIVER_SESSION.COL_RECEIVER_SAMPLE_INTERVAL       
             });
            // FROM these tables
            tables = new ArrayList<String>();
            tables.add(Tables.STATION.NAME);
            tables.add(Tables.RECEIVER_SESSION.NAME);
            // compose the db query string using GSAC code:
            //                               select what    from      where
            statement = getDatabaseManager().select(cols,  tables, Clause.and(clauses), (String) null, -1);
            //System.err.println("   mysql: "+statement);
            try {
                SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
                while ((results = iter.getNext()) != null) {
                           //System.err.println("         Dates of equip session  "+(si+1)+"     "+ ft.format(astartDate) +"   -   "+ ft.format(astopDate));

                           // code to get CORRECT times with hours mins and seconds:
                           String sdt = results.getString(Tables.RECEIVER_SESSION.COL_RECEIVER_INSTALLED_DATE)+"00";
                           String odt = null;
                           //System.err.println("   sdt antstart string = "+sdt); // CORRECT with time of day
                           Date antstart = formatter.parse(sdt);
                           Date antstop = new Date();  // ie now

                           Date test = readDate( results, Tables.RECEIVER_SESSION.COL_RECEIVER_REMOVED_DATE);
                           if (null == test) {
                              ;
                           }
                           else {
                              odt = results.getString(Tables.RECEIVER_SESSION.COL_RECEIVER_REMOVED_DATE)+"00";
                              //System.err.println("        odt  string = "+odt); // CORRECT with time of day
                              antstop = formatter.parse(odt);
                           }
                           // these value are CORRECT and include hours minutes and seconds:
                           //System.err.println("   antenna session times = "+ sdt +"   | "+ odt );
                           //System.err.println("   antenna session times = "+ ft.format(antstart) +"   | "+ft.format(antstop) );

                            //if (ad1 <= astart                                && ad2 >= astopdate ) is ok
                            if  (antstart.compareTo(astartDate) <= 0 && antstop.compareTo(astopDate) >= 0) {
                                ;
                            }
                            else { //System.err.println("           skip it");
                                continue;
                            }
                            //System.err.println("         GOOD     recvr session  "+(antsii+1)+"     "+ ft.format(antstart) +"   -   "+ ft.format(antstop));

                            antsii+=1;
                            receiverid = results.getInt          (Tables.RECEIVER_SESSION.COL_RECEIVER_TYPE_ID);
                            receiver_firmware_id = results.getInt(Tables.RECEIVER_SESSION.COL_RECEIVER_FIRMWARE_VERSION_ID);
                            receiver_serial = results.getString  (Tables.RECEIVER_SESSION.COL_RECEIVER_SERIAL_NUMBER);
                            satellitesys = results.getString     (Tables.RECEIVER_SESSION.COL_SATELLITE_SYSTEM);

                            // get value of RECEIVER_TYPE_NAME
                            ArrayList<String> avalues =  new ArrayList<String>();
                            clauses =  new ArrayList<Clause>();
                            tables =   new ArrayList<String>();
                            clauses.add(Clause.eq(Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_ID, receiverid) );
                            cols=SqlUtil.comma(new String[]{Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_NAME});
                            tables.add(Tables.RECEIVER_TYPE.NAME);
                            statement =
                               getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
                            try {
                               SqlUtil.Iterator iter2 = getDatabaseManager().getIterator(statement);
                               while ((results = iter2.getNext()) != null) {
                                   rcvrtype= results.getString(Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_NAME);
                                   //System.err.println("    SiteManager: read equip got RECEIVER type "+ rcvrtype );
                                   //break;
                               }
                            } finally {
                                   getDatabaseManager().closeAndReleaseConnection(statement);
                            }

                            // get value of RECEIVER_FIRMWARE VERSION and SwVer aaa
                            avalues =  new ArrayList<String>();
                            clauses =  new ArrayList<Clause>();
                            tables =   new ArrayList<String>();
                            clauses.add(Clause.eq(Tables.RECEIVER_FIRMWARE_VERSION.COL_RECEIVER_FIRMWARE_VERSION_ID, receiver_firmware_id) );
                            cols=SqlUtil.comma(new String[]{Tables.RECEIVER_FIRMWARE_VERSION.COL_RECEIVER_FIRMWARE_VERSION_NAME,
                                                            Tables.RECEIVER_FIRMWARE_VERSION.COL_SWVER});
                            tables.add(Tables.RECEIVER_FIRMWARE_VERSION.NAME);
                            statement =
                               getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
                            try {
                               SqlUtil.Iterator iter2 = getDatabaseManager().getIterator(statement);
                               while ((results = iter2.getNext()) != null) {
                                   rcvrfw = results.getString(Tables.RECEIVER_FIRMWARE_VERSION.COL_RECEIVER_FIRMWARE_VERSION_NAME);
                                   swver = results.getString(Tables.RECEIVER_FIRMWARE_VERSION.COL_SWVER);
                                   //System.err.println("    SiteManager: read equip got RECEIVER FIRMWARE_VERSION "+ rcvrfw );
                                   //break;
                               }
                            } finally {
                                   getDatabaseManager().closeAndReleaseConnection(statement);
                            } // end get value of RECEIVER_FIRMWARE VERSION and SwVer

                        //System.err.println("      rcvr type"+               rcvrtype+"  rcvr sn "+        receiver_serial+ " receiverFirmware "+       rcvrfw );
                       }
               } finally {
                     getDatabaseManager().closeAndReleaseConnection(statement);
               }

            // have gotten all equipment sessions' information for this station:
            //System.err.println("    dateRange "+dateRange[0]+"-"+dateRange[1]+
            // "  ant type "+anttype+" ant sn "+antenna_serial+"  delta z nor east "+zoffset+" "+dnorth+" "+deast+"  radome type "+radometype+"  antenna type "+ anttype);
            //System.err.println("      rcvr type"+               rcvrtype+"  rcvr sn "+        receiver_serial+ " receiverFirmware "+       rcvrfw );

            // construct a "GnssEquipment" object with these values:
            //  public GnssEquipment(Date[] dateRange,     String antenna, String antennaSerial, String dome, String domeSerial, String receiver, String receiverSerial, String receiverFirmware,  double zoffset)  
            GnssEquipment equipment=new GnssEquipment( dateRange, anttype,       antenna_serial, radometype,  " ",               rcvrtype,        receiver_serial,       rcvrfw,                     zoffset);  

            equipment.setSwVer(swver);
            equipment.setSampleInterval(sampInt);
            equipmentList.add(equipment);

            //equip_sessions.put(dateRange[0], equipment);

            equipment.setSatelliteSystem(satellitesys);  

        } // end loop on all equip sessions

        equipmentList = GnssEquipment.sort(equipmentList);
        GnssEquipmentGroup equipmentGroup = null;

        // for every item 'equipment' in the local equipmentList, add it to the equipmentGroup 
        for (GnssEquipment an_equipment : equipmentList) {
            if (equipmentGroup == null) {
                gsacResource.addMetadata(equipmentGroup = new GnssEquipmentGroup());
            }
            equipmentGroup.add(an_equipment);
        }
    }  // end of read equip metadata ()



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
     * @return site group list
     */
    public List<ResourceGroup> doGetResourceGroups() {
        try {
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
                    System.err.println("       doGetResourceGroups(): network _"+tok+"_");
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
        String  col = Tables.STATION.COL_NETWORKS;
        // "Handle the 4 cases to find the argument in the csv list of groups in the DB"
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
     *
     * @param results a row from a qb query which has a datetime field
     * @param column a string name for a db field with  for example a MySQL 'datetime' object,
     *                such as the String held by Tables.ANTENNA_SESSION.COL_SESSION_START_DATE
     *
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
