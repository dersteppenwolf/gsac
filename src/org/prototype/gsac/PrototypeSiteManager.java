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

/* Prototype GSAC SiteManager.java file to use in your GSAC code set. See README part 2.
   You will revise this file, changing two instances of "prototype" (in next two lines) to match your package name,
   and changing three instances of "Prototype" in lines 72-79 to your Java file prefix. */
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

// older version had import ucar.unidata.sql.Clause;
//                   import ucar.unidata.sql.SqlUtil;
import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;

import ucar.unidata.util.StringUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.HashSet;


/**
 * The GSAC SiteManager classes handle all of a GSAC repository's site(station)-related requests.  
 * For the Prototype GSAC (Geodesy Seamless Archive). 
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
 * @author  S K Wier, UNAVCO; version of 24 Oct 2013.
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
     * (Perhaps a call to here needs to go before makeCapabilities call so regular site search form appears  before advanced search?)
     *
     * CHANGEME if you have other things to search on, or different db table or field names for the items to search on.
     *
     * @return site search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        try {
            // order of adding to capabilities here specifies order on html site search page
            List<Capability> capabilities = new ArrayList<Capability>();

            //     Basic search items 

            String help = HtmlOutputHandler.stringSearchHelp;  /* some mouse over help text */
            // search on site code, the 4 character ID.  Users may use regular expressions such as AB* or P12*
            //        Capability args:                                          "web page label"
            //                                      (? what for)                           " mouse over help text" + other help
            Capability siteCode =
                initCapability(     new Capability(ARG_SITE_CODE, "Code (4 character ID)", Capability.TYPE_STRING), 
                      CAPABILITY_GROUP_SITE_QUERY, "Code (4 character ID) of the station", "Code (4 character ID) of the station. " + help);
            siteCode.setBrowse(true);  /*  which apparently adds these searches to the GSAC web site Browse form */
            capabilities.add(siteCode);

            help="Full name of the site, such as Marshall, part plus wildcard * such as Mar*, or ...";
            Capability siteName =
                initCapability(     new Capability(ARG_SITE_NAME, "Site Name",             Capability.TYPE_STRING), 
                       CAPABILITY_GROUP_SITE_QUERY, "Name of the site",                    "Name of site.   " + help);
            siteName.setBrowse(true);  /*  which apparently adds these searches to the GSAC web site Browse form */
            capabilities.add(siteName);

            // code for latitude-longitude bounding box; not in browse service
            capabilities.add(initCapability(new Capability(ARG_BBOX, "Lat-Lon Bounding Box", Capability.TYPE_SPATIAL_BOUNDS), 
                    CAPABILITY_GROUP_SITE_QUERY, "Spatial bounds within which the site lies"));

            // site search page has "Data Date Range"  pair of boxes;
            // output of site search is an html table with "Date Range" column , showing station's installed date until now; see gsl/output/HtmlOutputHandler.java.
            Capability sitedateRange =
                               initCapability( new Capability(ARG_SITE_DATE_FROM, "Site Includes Dates in Range", Capability.TYPE_DATERANGE), CAPABILITY_GROUP_SITE_QUERY, 
                         "The site operated between these dates", "Site date");
            capabilities.add(sitedateRange);

            String[] values;

            /*
            more ideas from the Unavco-gsac server site code:
            capabilities.add( initCapability( new Capability( ARG_SITE_DATE, "Data Date Range", Capability.TYPE_DATERANGE),                CAPABILITY_GROUP_SITE_QUERY, 
                           "Data exists between these dates"));
            capabilities.add(initCapability(new Capability(ARG_SITE_MODIFYDATE, "Site Modified Date Range", Capability.TYPE_DATERANGE), CAPABILITY_GROUP_ADVANCED,
                        "The site's metadata was modified between these dates"));
            capabilities.add( initCapability( new Capability( ARG_SITE_CREATEDATE, "Site Created Date Range", Capability.TYPE_DATERANGE), CAPABILITY_GROUP_ADVANCED,
                        "The site was created between these dates"));
            */

            //  Advanced search items 
            // "CAPABILITY_GROUP_ADVANCED" search items appear on the web site search page under the "Advanced Site Query" label:

            // get receiver type names used by stations in this database, only.
            // Show only the ones at stations in this repository, since the protoype GSAC db has all IGS receiver names, more than 200. 
            //    or simpler?: get all items in row where: from code for RING:
            // Statement statement = getDatabaseManager().select(Tables.SITI_GSAC.COLUMNS, Tables.SITI_GSAC.NAME, Clause.eq( Tables.SITI_GSAC.COL_NOME_SITO, gsacResource.getId()), (String) null, -1);
            ResultSet results;
            ArrayList<String> rvalues = new ArrayList<String>();
            List<Clause> clauses = new ArrayList<Clause>();
            //  for an SQL statement, the WHERE the test part in the select statement 
            clauses.add(Clause.join(Tables.STATION_SESSION.COL_RECEIVER_TYPE_ID, Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_ID));
            //  for the SELECT clause,  which items (fields) to get from the db (result in rows returned):
            String cols=SqlUtil.comma(new String[]{Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_NAME});
            //  for the SQL FROM, the which tables to select from part 
            List<String> tables = new ArrayList<String>();
            tables.add(Tables.STATION_SESSION.NAME);
            tables.add(Tables.RECEIVER_TYPE.NAME);
            // SELECT receiver_type.receiver_type_name FROM station_session,receiver_type WHERE (station_session.receiver_type_id = receiver_type.receiver_type_id);
            Statement  statement =
            //          select          what    from      where
            getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
            //System.err.println("   SiteManager: get resource select query is " +statement);
            try {
               SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
               // process each line in results of db query  
               while ((results = iter.getNext()) != null) {
                   String rcvtype= results.getString(Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_NAME);
                   // the values may have duplicates
                   int notfound=1;
                   for (int rcvi= 0; rcvi<rvalues.size(); rcvi+=1 ) {
                      if ( rvalues.get(rcvi).equals(rcvtype) ) { 
                         notfound=0;
                         break;
                         }
                   }
                   if (notfound==1) {
                         rvalues.add(rcvtype);
                         //System.err.println("        receiver type  is " +rcvtype);
                   }
                   //if you want Only read the first row of db query results returned
                   //break;
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }
            String[] itemArray = new String[rvalues.size()];
            values = rvalues.toArray(itemArray);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_RECEIVER, "Receiver type", values, true, CAPABILITY_GROUP_ADVANCED));

            /* for federated gsac, allow all types with this simple code:
            values = getDatabaseManager().readDistinctValues( Tables.RECEIVER_TYPE.NAME, Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_RECEIVER, "Receiver type", values, true, CAPABILITY_GROUP_ADVANCED));
            */

            // get antenna type names used by stations in this database, only.
            // Since the protoype db has all IGS antenna names, more than 200, show only the ones at stations in this repository .
            //    simpler?: or get all items in row where: example 
            //     Statement statement = getDatabaseManager().select( Tables.SITI_GSAC.COLUMNS, Tables.SITI_GSAC.NAME, Clause.eq( Tables.SITI_GSAC.COL_NOME_SITO, gsacResource.getId()), (String) null, -1);
            ArrayList<String> avalues = new ArrayList<String>();
            clauses =      new ArrayList<Clause>();
            //  WHERE 
            clauses.add(Clause.join(Tables.STATION_SESSION.COL_ANTENNA_TYPE_ID, Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_ID));
            //  SELECT what to 
            cols=SqlUtil.comma(new String[]{Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME});
            //  FROM   
            tables = new ArrayList<String>();
            tables.add(Tables.STATION_SESSION.NAME);
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
            clauses.add(Clause.join(Tables.STATION_SESSION.COL_RADOME_TYPE_ID, Tables.RADOME_TYPE.COL_RADOME_TYPE_ID));
            //  SELECT what to
            cols=SqlUtil.comma(new String[]{Tables.RADOME_TYPE.COL_RADOME_TYPE_NAME});
            //  FROM which tables (for a table join)
            tables = new ArrayList<String>();
            tables.add(Tables.STATION_SESSION.NAME);
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

            // search on station 'style', country, province/state, and city
            values = getDatabaseManager().readDistinctValues( Tables.STATION_STYLE.NAME, Tables.STATION_STYLE.COL_STATION_STYLE_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacArgs.ARG_SITE_TYPE, "Site Type", values, true, CAPABILITY_GROUP_ADVANCED));

            values = getDatabaseManager().readDistinctValues( Tables.COUNTRY.NAME, Tables.COUNTRY.COL_COUNTRY_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_COUNTRY, "Nation", values, true, CAPABILITY_GROUP_ADVANCED));

            values = getDatabaseManager().readDistinctValues( Tables.PROVINCE_REGION_STATE.NAME, Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_NAME);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_STATE, "Province/state", values, true, CAPABILITY_GROUP_ADVANCED));

            values = getDatabaseManager().readDistinctValues( Tables.STATION.NAME, Tables.STATION.COL_CITY);  // get all the city (place) names in GSAC's database.
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_CITY, "Place/city", values, true, CAPABILITY_GROUP_ADVANCED));

            //  omit for now LOOK - search on [data sampling] interval ; use float value in seconds per sample as 30 or 0.1 or 0.01
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
            clauses.add(Clause.or(getNetworkClauses(values, msgBuff)));
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
            tableNames.add(Tables.STATION_SESSION.NAME);
            tableNames.add(Tables.RECEIVER_TYPE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.STATION_SESSION.COL_STATION_ID));
            clauses.add(Clause.join(Tables.STATION_SESSION.COL_RECEIVER_TYPE_ID, Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_ID));
            clauses.add(Clause.eq(Tables.RECEIVER_TYPE.COL_RECEIVER_TYPE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for rcvr " + values.get(0)) ;
        }
        
        if (request.defined(GsacExtArgs.ARG_ANTENNA)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_ANTENNA);
            tableNames.add(Tables.STATION_SESSION.NAME);
            tableNames.add(Tables.ANTENNA_TYPE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.STATION_SESSION.COL_STATION_ID));
            clauses.add(Clause.join(Tables.STATION_SESSION.COL_ANTENNA_TYPE_ID, Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_ID));
            clauses.add(Clause.eq(Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for antenna " + values.get(0)) ;
        }
        
        if (request.defined(GsacExtArgs.ARG_DOME)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList( GsacExtArgs.ARG_DOME);
            tableNames.add(Tables.STATION_SESSION.NAME);
            tableNames.add(Tables.RADOME_TYPE.NAME);
            clauses.add(Clause.join(Tables.STATION.COL_STATION_ID, Tables.STATION_SESSION.COL_STATION_ID));
            clauses.add(Clause.join(Tables.STATION_SESSION.COL_RADOME_TYPE_ID, Tables.RADOME_TYPE.COL_RADOME_TYPE_ID));
            clauses.add(Clause.eq(Tables.RADOME_TYPE.COL_RADOME_TYPE_NAME, values.get(0)));
            //System.err.println("   SiteManager: query for radome " + values.get(0)) ;
        }

        //System.err.println("   SiteManager: getResourceClauses gives " + clauses) ;

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

        // compose the complete select SQL phrase; apply the select clause to the table(s) given.
        //                                                 DB  .select( what to find (fields),     from which tables,      where clause, )  
        // works ok: Statement statement = getDatabaseManager().select(getResourceSelectColumns(), clause.getTableNames(), clause);
        // and this works OK:
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
        String  fourCharId   = results.getString(Tables.STATION.COL_CODE_4CHAR_ID);
        String  staname   =    results.getString(Tables.STATION.COL_STATION_NAME);
        double latitude =      results.getDouble(Tables.STATION.COL_LATITUDE_NORTH);
        double longitude =     results.getDouble(Tables.STATION.COL_LONGITUDE_EAST);
        double ellipsoid_hgt =     results.getDouble(Tables.STATION.COL_ELLIPSOIDAL_HEIGHT);
        int station_style_id  =results.getInt(Tables.STATION.COL_STATION_STYLE_ID);
        int monument_description_id = results.getInt(Tables.STATION.COL_MONUMENT_DESCRIPTION_ID);
        int countryid    =     results.getInt(Tables.STATION.COL_COUNTRY_ID);
        int stateid      =     results.getInt(Tables.STATION.COL_PROVINCE_REGION_STATE_ID);
        String city      =     results.getString(Tables.STATION.COL_CITY);
        String iersdomes =     results.getString(Tables.STATION.COL_IERS_DOMES);
        String station_photo_URL = results.getString(Tables.STATION.COL_STATION_PHOTO_URL);
        int agencyid    =      results.getInt(Tables.STATION.COL_AGENCY_ID); // or getLong
        int access_permission_id    = results.getInt(Tables.STATION.COL_ACCESS_PERMISSION_ID);
        String networks = results.getString(Tables.STATION.COL_NETWORKS);
         
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
               country = qresults.getString(Tables.COUNTRY.COL_COUNTRY_NAME);
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
               state = qresults.getString(Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_NAME);
               break;
           }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }
            //System.err.println("   SiteManager: province is " +state);

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
               //state = qresults.getString(Tables.PROVINCE_REGION_STATE.COL_PROVINCE_REGION_STATE_NAME);
               String agency = qresults.getString( Tables.AGENCY.COL_AGENCY_NAME);
               addPropertyMetadata( site, GsacExtArgs.SITE_METADATA_NAMEAGENCY, "Agency", agency);    
               break;
               }
            } finally {
               getDatabaseManager().closeAndReleaseConnection(statement);
            }
        
        // add URL(s) of image(s) here; will appear on web page of one station results
        MetadataGroup imagesGroup = null;
        if ( station_photo_URL != null) {
            if (imagesGroup == null) {
                site.addMetadata(imagesGroup = new MetadataGroup("Images", MetadataGroup.DISPLAY_TABS));
            }
            // add one image to the group:
            imagesGroup.add( new ImageMetadata( station_photo_URL, "Site Photo"));
            // or in some cases could do like this to make the first arg value: "http://facility.unavco.org/data/images/station_images/" + fourCharId + ".jpg", 
        }

        //  set site "Type" aka site.type also called "station style" in the database
        // hard coded, using values in the GSAC prototype db:
        // CHANGEME if you alter the GSAC prototype db schema.
        if (1 == station_style_id ) {
           site.setType(new ResourceType("gnss.site.campaign"));
        }
        else if (2 == station_style_id ) {
           site.setType(new ResourceType("gnss.site.continuous"));
        }
        else if (3 == station_style_id ) {
           site.setType(new ResourceType("gnss.site.mobile"));
        }

        // CHANGEME if you need this; sample code is below in this file.
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
                gsacResource.setLongName( results.getString(Tables.STATION.COL_STATION_NAME) );

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

                //  this value handeled elsewhere in this class:
                //String station_photo_url= results.getString( Tables.STATION.COL_STATION_PHOTO_URL);

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
                addPropertyMetadata( gsacResource, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "Monument description", 
                     results.getString(Tables.MONUMENT_DESCRIPTION.COL_MONUMENT_DESCRIPTION) );
                //Only read the first row of db query results returned
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

    }


    /**
     * Get metadata for antenna and receiver sessions (implied from site visits).
     * LOOK will be revised extensively to make equipment sessions from only the antenna and receiver session tables, not from the station_session table, in October 2013.
     *
     * Get equipment metadata from the db tables 
     *
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     */
    private void readEquipmentMetadata(GsacResource gsacResource)
            throws Exception {
               
        Hashtable<Date, GnssEquipment> visits = new Hashtable<Date, GnssEquipment>();
        List<GnssEquipment> equipmentList = new ArrayList<GnssEquipment>();
        Statement           statement;
        ResultSet           results;
        List<Clause> clauses = new ArrayList<Clause>();
        List<String> tables = new ArrayList<String>();

        /*
        mysql> describe antenna_type;
        +-------------------+-----------------+------+-----+---------+----------------+
        | Field             | Type            | Null | Key | Default | Extra          |
        +-------------------+-----------------+------+-----+---------+----------------+
        | antenna_type_id   | int(5) unsigned | NO   | PRI | NULL    | auto_increment |
        | antenna_type_name | varchar(15)     | NO   |     | NULL    |                |
        | igs_defined       | char(1)         | NO   |     | N       |                |
        +-------------------+-----------------+------+-----+---------+----------------+

        mysql> describe radome_type;
        +------------------+-----------------+------+-----+---------+----------------+
        | Field            | Type            | Null | Key | Default | Extra          |
        +------------------+-----------------+------+-----+---------+----------------+
        | radome_type_id   | int(5) unsigned | NO   | PRI | NULL    | auto_increment |
        | radome_type_name | varchar(4)      | NO   |     | NULL    |                |
        | igs_defined      | char(1)         | NO   |     | N       |                |
        +------------------+-----------------+------+-----+---------+----------------+

        mysql> describe antenna_session;
        +------------------------+-----------------+------+-----+---------+----------------+
        | Field                  | Type            | Null | Key | Default | Extra          |
        +------------------------+-----------------+------+-----+---------+----------------+
        | antenna_session_id     | int(5) unsigned | NO   | PRI | NULL    | auto_increment |
        | station_id             | int(6) unsigned | NO   |     | NULL    |                |
        | antenna_type_id        | int(5) unsigned | NO   |     | NULL    |                |
        | antenna_serial_number  | varchar(20)     | NO   |     | NULL    |                |
        | antenna_installed_date | datetime        | NO   |     | NULL    |                |
        | antenna_removed_date   | datetime        | NO   |     | NULL    |                |
        | radome_type_id         | int(5) unsigned | NO   |     | NULL    |                |
        | antenna_offset_up      | float           | NO   |     | NULL    |                |
        | antenna_offset_north   | float           | NO   |     | NULL    |                |
        | antenna_offset_east    | float           | NO   |     | NULL    |                |
        | antenna_HtCod          | char(5)         | YES  |     | NULL    |                |
        +------------------------+-----------------+------+-----+---------+----------------+            lll
        */

        // WHERE  this station is ided by its 4 char id:
        clauses.add(Clause.eq(Tables.STATION.COL_CODE_4CHAR_ID, gsacResource.getId())); 
        // and where the antenna session has the station id number
        clauses.add(Clause.join(Tables.STATION_SESSION.COL_STATION_ID, Tables.STATION.COL_STATION_ID)); 
        //clauses.add(Clause.join(Tables.ANTENNA_SESSION.COL_STATION_ID, Tables.STATION.COL_STATION_ID)); 
        //clauses.add(Clause.join(Tables.RECEIVER_SESSION.COL_STATION_ID, Tables.STATION.COL_STATION_ID)); 
        // could return >1 row
  
        // "AA"; SELECT WHAT:  list is matched with line "BB" below.
        String cols=SqlUtil.comma(new String[]{
             Tables.STATION_SESSION.COL_STATION_SESSION_ID , 
             Tables.STATION_SESSION.COL_ANTENNA_SERIAL_NUMBER , 
             Tables.STATION_SESSION.COL_SESSION_START_DATE , 
             Tables.STATION_SESSION.COL_SESSION_END_DATE , 
             Tables.STATION_SESSION.COL_ANTENNA_OFFSET_UP , 
             Tables.STATION_SESSION.COL_ANTENNA_OFFSET_NORTH , 
             Tables.STATION_SESSION.COL_ANTENNA_OFFSET_EAST , 
             Tables.STATION_SESSION.COL_ANTENNA_TYPE_ID , 
             Tables.STATION_SESSION.COL_RADOME_TYPE_ID , 
             Tables.STATION_SESSION.COL_RECEIVER_TYPE_ID , 
             Tables.STATION_SESSION.COL_RECEIVER_FIRMWARE_VERSION_ID , 
             Tables.STATION_SESSION.COL_RECEIVER_SERIAL_NUMBER,   
             Tables.STATION_SESSION.COL_SATELLITE_SYSTEM       
             /*
             Tables.ANTENNA_SESSION.COL_ANTENNA_SESSION_ID ,
             Tables.ANTENNA_SESSION.COL_ANTENNA_INSTALLED_DATE ,
             Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE ,
             Tables.ANTENNA_SESSION.COL_ANTENNA_OFFSET_UP ,
             Tables.ANTENNA_SESSION.COL_ANTENNA_OFFSET_NORTH ,
             Tables.ANTENNA_SESSION.COL_ANTENNA_OFFSET_EAST ,
             Tables.ANTENNA_SESSION.COL_ANTENNA_TYPE_ID ,
             Tables.ANTENNA_SESSION.COL_ANTENNA_SERIAL_NUMBER , // not a number; is a varchar (20) String
             Tables.ANTENNA_SESSION.COL_RADOME_TYPE_ID ,
             Tables.RECEIVER_SESSION.COL_RECEIVER_TYPE_ID ,
             Tables.RECEIVER_SESSION.COL_RECEIVER_FIRMWARE_VERSION_ID ,
             Tables.RECEIVER_SESSION.COL_RECEIVER_SERIAL_NUMBER // not a number; is a varchar (20) String
             */
         });

        // FROM these tables
        tables.add(Tables.STATION.NAME);
        tables.add(Tables.STATION_SESSION.NAME);
        // same info is also in these, but not arranged for this current purpose:
        //tables.add(Tables.ANTENNA_SESSION.NAME);
        //tables.add(Tables.RECEIVER_SESSION.NAME);

        // compose the db query string using GSAC code:
        //                       select          what    from      where
        //          getDatabaseManager().select(cols,  tables, Clause.and(clauses),  (String) null,  -1);
        statement = getDatabaseManager().select(cols,  tables, Clause.and(clauses), " order by " + Tables.STATION_SESSION.COL_SESSION_START_DATE, -1);

        int station_sess_id=0;
        int receiverid=0;
        int receiver_firmware_id=0;
        int antennaid=602; // means missing in gsac protoytpe db; not specially important
        int radomeid=0;
        float dnorth=0.0f;
        float deast=0.0f;
        Double zoffset=0.0;
        String antenna_serial=" ";
        String receiver_serial=" ";
        String anttype=" ";
        String rcvrtype=" ";
        String rcvrfw=" ";
        String radometype=" ";
        String satellitesys=" ";
        Date indate=null;
        Date outdate=null;
        Date[] dateRange=null;

        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {
                //  "BB"  items must match with line "AA" above.
                station_sess_id = results.getInt(Tables.STATION_SESSION.COL_STATION_SESSION_ID);
                antenna_serial = results.getString(Tables.STATION_SESSION.COL_ANTENNA_SERIAL_NUMBER);
                //indate = readDate( results, Tables.ANTENNA_SESSION.COL_ANTENNA_INSTALLED_DATE);
                indate = readDate( results,   Tables.STATION_SESSION.COL_SESSION_START_DATE);
                //outdate = readDate( results, Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE);
                outdate = readDate( results, Tables.STATION_SESSION.COL_SESSION_END_DATE);
                dateRange = new Date[] { indate, outdate  };
                //          new Date[] { readDate( results, Tables.ANTENNA_SESSION.COL_ANTENNA_INSTALLED_DATE), readDate( results, Tables.ANTENNA_SESSION.COL_ANTENNA_REMOVED_DATE)  };
                zoffset = results.getDouble(Tables.STATION_SESSION.COL_ANTENNA_OFFSET_UP);
                dnorth = results.getFloat(Tables.STATION_SESSION.COL_ANTENNA_OFFSET_NORTH);
                deast= results.getFloat(Tables.STATION_SESSION.COL_ANTENNA_OFFSET_EAST);
                antennaid = results.getInt(Tables.STATION_SESSION.COL_ANTENNA_TYPE_ID);
                radomeid = results.getInt(Tables.STATION_SESSION.COL_RADOME_TYPE_ID);
                receiverid = results.getInt(Tables.STATION_SESSION.COL_RECEIVER_TYPE_ID);
                receiver_firmware_id = results.getInt(Tables.STATION_SESSION.COL_RECEIVER_FIRMWARE_VERSION_ID);
                receiver_serial = results.getString(Tables.STATION_SESSION.COL_RECEIVER_SERIAL_NUMBER);
                satellitesys = results.getString(Tables.STATION_SESSION.COL_SATELLITE_SYSTEM);

                //System.err.println("    SiteManager: read equip got session session id "+ station_sess_id );

                // get value of ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME
                ArrayList<String> avalues = new ArrayList<String>();
                clauses =      new ArrayList<Clause>();
                //  WHERE  this antenna type id key value is equal to
                clauses.add(Clause.eq(Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_ID, antennaid) );
                //  SELECT what to return
                cols=SqlUtil.comma(new String[]{Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME});
                //  FROM the table (s)
                tables = new ArrayList<String>();
                tables.add(Tables.ANTENNA_TYPE.NAME);
                statement =
                   getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
                try {
                   SqlUtil.Iterator iter2 = getDatabaseManager().getIterator(statement);
                   while ((results = iter2.getNext()) != null) {
                       anttype= results.getString(Tables.ANTENNA_TYPE.COL_ANTENNA_TYPE_NAME);
                       //System.err.println("    SiteManager: read equip got antenna type "+ anttype );
                       //break;
                   }
                } finally {
                       getDatabaseManager().closeAndReleaseConnection(statement);
                }

                // get value of RECEIVER_TYPE_NAME
                avalues =  new ArrayList<String>();
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

                // get value of RECEIVER_FIRMWARE VERSION
                avalues =  new ArrayList<String>();
                clauses =  new ArrayList<Clause>();
                tables =   new ArrayList<String>();
                clauses.add(Clause.eq(Tables.RECEIVER_FIRMWARE_VERSION.COL_RECEIVER_FIRMWARE_VERSION_ID, receiver_firmware_id) );
                cols=SqlUtil.comma(new String[]{Tables.RECEIVER_FIRMWARE_VERSION.COL_RECEIVER_FIRMWARE_VERSION_NAME});
                tables.add(Tables.RECEIVER_FIRMWARE_VERSION.NAME);
                statement =
                   getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1);
                try {
                   SqlUtil.Iterator iter2 = getDatabaseManager().getIterator(statement);
                   while ((results = iter2.getNext()) != null) {
                       rcvrfw = results.getString(Tables.RECEIVER_FIRMWARE_VERSION.COL_RECEIVER_FIRMWARE_VERSION_NAME);
                       //System.err.println("    SiteManager: read equip got RECEIVER FIRMWARE_VERSION "+ rcvrfw );
                       //break;
                   }
                } finally {
                       getDatabaseManager().closeAndReleaseConnection(statement);
                }

                // get value of RADOME_TYPE.COL_RADOME_TYPE_NAME
                avalues =  new ArrayList<String>();
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

                // construct a "GnssEquipment" object with these values:
                //  public GnssEquipment(Date[] dateRange, String antenna, String antennaSerial, String dome, String domeSerial, String receiver, String receiverSerial, String receiverFirmware,  double zoffset)  
                GnssEquipment equipment =
                    new GnssEquipment(          dateRange,        anttype,       antenna_serial, radometype,  " ",               rcvrtype,        receiver_serial,       rcvrfw,                     zoffset);  
                equipmentList.add(equipment);
                visits.put(dateRange[0], equipment);

                // from ring equipment.setSatelliteSystem( results.getString( Tables.STRUMENTI_RICEVITORE.COL_SISTEMA_SATELLITE)); 
                equipment.setSatelliteSystem(satellitesys);  // test of 

            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        equipmentList = GnssEquipment.sort(equipmentList);
        GnssEquipmentGroup equipmentGroup = null;

        // for every item 'equipment' in the local equipmentList, add it to the equipmentGroup 
        for (GnssEquipment an_equipment : equipmentList) {
            if (equipmentGroup == null) {
                gsacResource.addMetadata(equipmentGroup = new GnssEquipmentGroup());
            }
            equipmentGroup.add(an_equipment);
        }

    }  // end of read equip metadata



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
                addPropertyMetadata(
                    gsacResource, GsacExtArgs.SITE_METADATA_FREQUENCYSTANDARD,
                    "Clock",
                    results.getString(
                        Tables.SITELOG_FREQUENCYSTANDARD.COL_STANDARDTYPE));

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
            appendSearchCriteria(msgBuff, ((cnt++ == 0) ? "Site Group=" : ""), group);
            groupClauses.add(Clause.eq(col, group));
            groupClauses.add(Clause.like(col, SqlUtil.wildCardBefore(", " + group)));
            groupClauses.add(Clause.like(col, SqlUtil.wildCardAfter(group + ",")));
            groupClauses.add(Clause.like(col, SqlUtil.wildCardBoth(", " + group + ",")));
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
     *
     * @param results a row from a qb query which has a datetime field
     * @param column a string name for a db field with  for example a MySQL 'datetime' object,
     *                such as the String held by Tables.STATION_SESSION.COL_SESSION_START_DATE
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
