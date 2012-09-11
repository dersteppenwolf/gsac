/*
 *
 */

package org.igs.gsac;

import org.igs.gsac.database.*;


import org.gsac.gsl.*;
import org.gsac.gsl.output.HtmlOutputHandler;
import org.gsac.gsl.model.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.util.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.util.Arrays;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
        try {
            List<Capability> capabilities = new ArrayList<Capability>();

            String              help = HtmlOutputHandler.stringSearchHelp;
            Capability          siteCode = initCapability(new Capability(ARG_SITE_CODE,
                                                                         "Site Code",
                                                                         Capability.TYPE_STRING), CAPABILITY_GROUP_SITE_QUERY,
                                                          "Short name of the site",
                                                          "Short name of the site. " + help);
            siteCode.setBrowse(true);
            capabilities.add(siteCode);
            capabilities.add(initCapability(new Capability(ARG_BBOX, "Bounds",
                                                           Capability.TYPE_SPATIAL_BOUNDS), CAPABILITY_GROUP_SITE_QUERY,
                                            "Spatial bounds within which the site lies"));
            String[]  values;

            values = getDatabaseManager().readDistinctValues(
                                                             Tables.SITELOG_LOCATION.NAME,
                                                             Tables.SITELOG_LOCATION.COL_COUNTRY);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_COUNTRY, "Country", values, true,
                                            CAPABILITY_GROUP_ADVANCED));

            values = getDatabaseManager().readDistinctValues(
                                                             Tables.SITELOG_LOCATION.NAME,
                                                             Tables.SITELOG_LOCATION.COL_TECTONIC);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_TECTONICPLATE, "Tectonic Plate", values, true,
                                            CAPABILITY_GROUP_ADVANCED));

            return capabilities;
        } catch(Exception exc) {
            throw new RuntimeException(exc);
        }
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
        
        Clause clause = Clause.eq(Tables.SITELOG_LOCATION.COL_FOURID, resourceId);
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
        tableNames.add(Tables.SITELOG_LOCATION.NAME);
        List<Clause> clauses = new ArrayList();
        String latCol = Tables.SITELOG_LOCATION.COL_LATITUDENORTH;
        String lonCol =  Tables.SITELOG_LOCATION.COL_LONGITUDEEAST;
        if (request.defined(ARG_NORTH)) {
            clauses.add(Clause.le(latCol,
                                  convertToStupidFormat(request.get(ARG_NORTH, 0.0))));
            appendSearchCriteria(msgBuff, "north&lt;=",
                                 "" + request.get(ARG_NORTH, 0.0));
        }
        if (request.defined(ARG_SOUTH)) {
            clauses.add(Clause.ge(latCol,
                                  convertToStupidFormat(request.get(ARG_SOUTH, 0.0))));
            appendSearchCriteria(msgBuff, "south&gt;=",
                                 "" + request.get(ARG_SOUTH, 0.0));
        }
        if (request.defined(ARG_EAST)) {
            clauses.add(Clause.le(lonCol,
                                  convertToStupidFormat(request.get(ARG_EAST, 0.0))));
            appendSearchCriteria(msgBuff, "east&lt;=",
                                 "" + request.get(ARG_EAST, 0.0));
        }
        if (request.defined(ARG_WEST)) {
            clauses.add(Clause.ge(lonCol,
                                  convertToStupidFormat(request.get(ARG_WEST, 0.0))));
            appendSearchCriteria(msgBuff, "west&gt;=",
                                 "" + request.get(ARG_WEST, 0.0));
        }

        if (request.defined(ARG_SITE_ID)) {
           addStringSearch(request, ARG_SITE_ID, " ",
                        msgBuff, "Site ID",
                        Tables.SITELOG_LOCATION.COL_FOURID, clauses);
        }


        //Add in the site code 
        addStringSearch(request, ARG_SITECODE, ARG_SITECODE_SEARCHTYPE,
                        msgBuff, "Site Code",
                        Tables.SITELOG_LOCATION.COL_FOURID, clauses);

        if(request.defined(GsacExtArgs.ARG_COUNTRY)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList(GsacExtArgs.ARG_COUNTRY);
            clauses.add(Clause.or(Clause.makeStringClauses(Tables.SITELOG_LOCATION.COL_COUNTRY,
                                                           values)));

        }

        if(request.defined(GsacExtArgs.ARG_TECTONICPLATE)) {
            List<String> values = (List<String>) request.getDelimiterSeparatedList(GsacExtArgs.ARG_TECTONICPLATE);
            clauses.add(Clause.or(Clause.makeStringClauses(Tables.SITELOG_LOCATION.COL_TECTONIC,
                                                           values)));

        }

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
        colCnt+=3;
        //        double x= results.getDouble(colCnt++);
        //        double y= results.getDouble(colCnt++);
        //        double z = results.getDouble(colCnt++);

        String latString = results.getString(colCnt++);
        String lonString = results.getString(colCnt++);
        double latitude   = convertFromStupidFormat(Double.parseDouble(latString.trim()));
        double longitude   = convertFromStupidFormat(Double.parseDouble(lonString.trim()));

        String elevationString = results.getString(colCnt++);
        elevationString = StringUtil.findPattern(elevationString,"([\\d\\.-]+)");
        double elevation  = elevationString!=null?Double.parseDouble(elevationString):0.0;
        //        System.err.println("lat:" + latString +" lon:" + lonString +" elev:" + elevationString);

        GsacSite site = new GsacSite(fourCharId, fourCharId, "",
                                     latitude, longitude, elevation);
        site.setType(new ResourceType("gnss.site.continuous"));
        return site;
        
    }


    public static double convertToStupidFormat(double decimalDegrees) {
        String msg  = "" + decimalDegrees;
        int degrees = (int)decimalDegrees;
        decimalDegrees = decimalDegrees-degrees;
        int  minutes = (int)(decimalDegrees/60);
        double seconds = decimalDegrees-(minutes*60);
        double result = degrees*10000+minutes*1000 + seconds;
        System.err.println("decimal:" + msg +" stupid format:" +  result + " " + degrees +" " + minutes +" " + seconds);
        return result;
    }

    public static double convertFromStupidFormat(double stupidFormat) {
        //505216.68 -1141736.6
        int intValue = (int) stupidFormat;
        double decimalValue = stupidFormat-intValue;
        double seconds = (intValue%100)+decimalValue;
        intValue = intValue/100;
        double minutes = (double)(intValue%100);
        intValue = intValue/100;
        double degrees = (double) intValue;
        //        System.err.println("convert:" + stupidFormat + " to:" + degrees +" " + minutes +" " + seconds);
        return degrees+minutes/60.0+seconds/360.0;
    }




}
