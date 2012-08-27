/*
 *
 */

package org.igs.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.util.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.Misc;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


/**
 * Handles all of the site related repository requests
 * The main entry point is the  {@link #handleRequest} method.
 * Look for the CHANGEME comments
 * 
 *
 * @author         Jeff McWhirter
 */
public class IgsSiteManager extends SiteManager {

    
    /**
     * ctor
     *
     * @param repository the repository
     */
    public IgsSiteManager(IgsRepository repository) {
        super(repository);
    }


    /**
     CHANGEME
     * Get the extra site search capabilities. 
     *
     * @return site search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();
        /*
          you can use the default site capabilities:
          addDefaultCapabilities(capabilities);
          or add you own, e.g.:
          Add in an example fruit enumerated query capability
          String[]values = {"banana","apple","orange"};
          Arrays.sort(values);
          capabilities.add(new Capability("fruit", "Fruit Label", values, true));
        */
        addDefaultCapabilities(capabilities);
        return capabilities;
    }


    /** 
        This is the main entry point for handling queries
        If you don't implement this method then the base SiteManager
        class will 
     **/
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        //CHANGEME 
        super.handleRequest(request, response);

    }


    /**
     * CHANGEME
     * create and return the resource (site) identified by the given resource id
     *
     * @param resourceId resource id. This isn't the resource code but actually the monument id
     *
     * @return the resource or null if not found
     *
     * @throws Exception on badness
     */
    public GsacResource getResource(String resourceId) throws Exception {
        /* e.g.:
        //Here is some example code from Unavco for making a query


        Clause clause = Clause.eq(Tables.MV_DAI_PRO.COL_MON_ID,
                                  new Integer(resourceId).intValue());
        Statement statement = getDatabaseManager().select(
                                                          getResourceSelectColumns(), 
                                                          clause.getTableNames(), clause);
        try {
            ResultSet results = statement.getResultSet();
            if (!results.next()) {
                results.close();
                return null;
            }
            GsacSite site = (GsacSite)makeResource(results);
            results.close();
            return site;
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
        */
        return  new GsacSite("xxx", "p123", "xxxpbo place", 40., -107, 123. );
        //return  null;
    }




    /**
     * An example method that shows how to use the Clause class to assemble a set of database
     * search clauses from the URL arguments
     * 
     * @param request the resquest
     * @param response the response
     * @param msgBuff buffer to append search criteria to
     *
     * @return list of clauses for selecting sites
     */
    public List<Clause> getResourceClauses(GsacRequest request,
                                           GsacResponse response,
                                           List<String> tableNames,
                                           StringBuffer msgBuff) {
        List<Clause> clauses = new ArrayList();
        String latCol = Tables.SITELOG_LOCATION.COL_LATITUDENORTH;
        String lonCol =  Tables.SITELOG_LOCATION.COL_LONGITUDEEAST;
        if (request.defined(ARG_NORTH)) {
            clauses.add(Clause.le(latCol,
                                  request.get(ARG_NORTH, 0.0)));
            appendSearchCriteria(msgBuff, "north&lt;=",
                                 "" + request.get(ARG_NORTH, 0.0));
        }
        if (request.defined(ARG_SOUTH)) {
            clauses.add(Clause.ge(latCol,
                                  request.get(ARG_SOUTH, 0.0)));
            appendSearchCriteria(msgBuff, "south&gt;=",
                                 "" + request.get(ARG_SOUTH, 0.0));
        }
        if (request.defined(ARG_EAST)) {
            clauses.add(Clause.le(lonCol,
                                  request.get(ARG_EAST, 0.0)));
            appendSearchCriteria(msgBuff, "east&lt;=",
                                 "" + request.get(ARG_EAST, 0.0));
        }
        if (request.defined(ARG_WEST)) {
            clauses.add(Clause.ge(lonCol,
                                  request.get(ARG_WEST, 0.0)));
            appendSearchCriteria(msgBuff, "west&gt;=",
                                 "" + request.get(ARG_WEST, 0.0));
        }


        List   args         = null;
        if (request.defined(ARG_SITE_ID)) {
           addStringSearch(request, ARG_SITE_ID, " ",
                        msgBuff, "Site ID",
                        Tables.SITELOG_LOCATION.COL_FOURID, clauses);
        }

        //Add in the site type, status, etc
        /*
        String[][] enumArgs = {
            { GsacArgs.ARG_SITE_TYPE, "e.g. Tables.MV_DAI_PRO.COL_SITE_TYPE",
              "Site Type" },
            { GsacArgs.ARG_SITE_STATUS, "e.g. Tables.MV_DAI_PRO.COL_OPERATIONAL",
              "Site Status" }
        };

        for (String[] argValues : enumArgs) {
            if (request.defined(argValues[0])) {
                //There might be more than one argument and also it can be comma separated
                args = (List<String>) request.getDelimiterSeparatedList(
                        argValues[0]);
                clauses.add(Clause.or(Clause.makeStringClauses(argValues[1],
                        args)));
                addSearchCriteria(msgBuff, argValues[2], args);
            }
        }
        */

        //Add in the site code and site name queries
        
        addStringSearch(request, ARG_SITECODE, ARG_SITECODE_SEARCHTYPE,
                        msgBuff, "Site Code",
                        Tables.SITELOG_LOCATION.COL_FOURID, clauses);

        return clauses;
    }


    /**
     * get all of the metadata for the given site
     *
     * @param gsacResource resource
     *
     * @throws Exception On badness
     */
    public void doGetMetadata(int level, GsacResource gsacResource) throws Exception {
        //The Unavco repository adds in GnssEquipment metadata and other things
    }


    /**
     * Get the site group list. This is used by the addDefaultSiteCapabilities 
     *
     * @return resource group list
     */
    public List<ResourceGroup> doGetResourceGroups() {
        List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        /**
           CHANGEME
        groups.add(new ResourceGroup("group1","Group 1"));
        groups.add(new ResourceGroup("group2", "Group 2"));
        groups.add(new ResourceGroup("group3","Group 3"));
        Collections.sort((List) groups);
        */
        return groups;
    }


    /*************************************************************************************************
     * The code below here inherits some of .....
    *************************************************************************************************/


    /** CHANGEME Default query order. 
        Set this to what you want to sort on */
    private static final String SITE_ORDER =
        " ORDER BY  " + Tables.SITELOG_LOCATION.COL_FOURID + " ASC ";


    /**
     * Get the columns that are to be searched on
     *
     * @param request the request
     *
     * @return comma delimited fully qualified column names to select on
     */
    public String getResourceSelectColumns() {
        return Tables.SITELOG_LOCATION.COLUMNS ;
    }


    /**
     * Get the order by clause
     *
     * @param request the request
     *
     * @return order by clause
     */
    public String getResourceOrder(GsacRequest request) {
       // return SITE_ORDER;
	   return null;
    }

    /**
     * CHANGEME
     * Create a single site
     *
     * @param results db results
     *
     * @return the site
     *
     * @throws Exception on badness
     */
    @Override
    public GsacSite makeResource(ResultSet results) throws Exception {
        int    colCnt     = 1;
        /* order must match coulmn order in e.g. SITELOG_LOCATION.COLUMNS */
        String fourCharId = results.getString(colCnt++);
        String city= results.getString(colCnt++);
        String state= results.getString(colCnt++);
        String country= results.getString(colCnt++);
        String tectonic= results.getString(colCnt++);
        double x= results.getDouble(colCnt++);
        double y= results.getDouble(colCnt++);
        double z = results.getDouble(colCnt++);
        double latitude   = results.getDouble(colCnt++);
        double longitude  = results.getDouble(colCnt++);
        double elevation  = 0.0; // FIX pull out number from this weird sting with the height level by name  !!!  results.getDouble(colCnt++);

        System.err.println (x +" "+ y +" "+ z +" " + latitude +" " +longitude); 


        GsacSite site = new GsacSite(fourCharId, fourCharId, "",
                                  latitude, longitude, elevation);
        site.setType(new ResourceType("gnss.site.continuous"));

        return site;
        
    }





}
