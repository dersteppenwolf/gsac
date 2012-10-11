/*
 *
 * Copyright 2012 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
 */

package org.igs.gsac;

import org.igs.gsac.database.*;


import org.gsac.gsl.*;
import org.gsac.gsl.output.HtmlOutputHandler;
import org.gsac.gsl.model.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.metadata.gnss.*;
import org.gsac.gsl.util.*;


import ucar.unidata.util.Misc;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.util.Arrays;
import java.util.Hashtable;
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

			/*  args in newCapability() include:
				1. ARG_SITE_CODE, ARG_BBOX, GsacExtArgs.ARG_COUNTRY, GsacExtArgs.ARG_TECTONICPLATE (from gsac/gsl/GsacExtArgs.java)
				2. "Site Code",  "Bounding Box", "Country", "Tectonic Plate",  etc which are labels on the web site form 
				3. Capability.TYPE_STRING, Capability.TYPE_SPATIAL_BOUNDS, values list (Tables.SITELOG_LOCATION.NAME,
                                                             Tables.SITELOG_LOCATION.COL_COUNTRY) (from Tables.java) 
				4. true (optional) 
				5.  CAPABILITY_GROUP_ADVANCED ? optional

				why is initCapability( )  not used in some cases  (the non advanced ones? ) 
			*/

            String              help = HtmlOutputHandler.stringSearchHelp;   /* where from ? */

            Capability          siteCode = initCapability(new Capability(ARG_SITE_CODE,
                                                                         "Site Code",
                                                                         Capability.TYPE_STRING), 
												CAPABILITY_GROUP_SITE_QUERY, "Short name of the site",
                                                          "Short name of the site. " + help);

            siteCode.setBrowse(true); /* which does ? */
            capabilities.add(siteCode);

            capabilities.add(initCapability(              new Capability(ARG_BBOX, 
																		"Lat-Lon Bounding Box",
                                                           Capability.TYPE_SPATIAL_BOUNDS), 
											CAPABILITY_GROUP_SITE_QUERY, "Spatial bounds within which the site lies"));

            String[]  values;

            /* not wanted now for query
            values = getDatabaseManager().readDistinctValues( Tables.SITELOG_LOCATION.NAME,
                                                             Tables.SITELOG_LOCATION.COL_CITY);
            Arrays.sort(values); 
            capabilities.add(                            new Capability(GsacExtArgs.ARG_CITY, 
																	"City",       
																	values, true, CAPABILITY_GROUP_ADVANCED));
            */

            values = getDatabaseManager().readDistinctValues( Tables.SITELOG_LOCATION.NAME,
                                                             Tables.SITELOG_LOCATION.COL_STATE);
            Arrays.sort(values); 
            capabilities.add(                            new Capability(GsacExtArgs.ARG_STATE, 
																	"State",       
																	values, true, CAPABILITY_GROUP_ADVANCED));

            values = getDatabaseManager().readDistinctValues( Tables.SITELOG_LOCATION.NAME,
                                                             Tables.SITELOG_LOCATION.COL_COUNTRY);
            Arrays.sort(values);
            capabilities.add(                            new Capability(GsacExtArgs.ARG_COUNTRY, 
																	"Country",       
																	values, true, CAPABILITY_GROUP_ADVANCED));

            /* not wanted now for query
            values = getDatabaseManager().readDistinctValues( Tables.SITELOG_LOCATION.NAME,
                                                             Tables.SITELOG_LOCATION.COL_TECTONIC);
            Arrays.sort(values);
            capabilities.add(                         new Capability(GsacExtArgs.ARG_TECTONICPLATE, 
																	"Tectonic Plate", 
																	values, true, CAPABILITY_GROUP_ADVANCED));
            */

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
                                  convertToISGSiteLogLatLongFormat(request.get(ARG_NORTH, 0.0))));
            appendSearchCriteria(msgBuff, "north&lt;=",
                                 "" + request.get(ARG_NORTH, 0.0));
        }
        if (request.defined(ARG_SOUTH)) {
            clauses.add(Clause.ge(latCol,
                                  convertToISGSiteLogLatLongFormat(request.get(ARG_SOUTH, 0.0))));
            appendSearchCriteria(msgBuff, "south&gt;=",
                                 "" + request.get(ARG_SOUTH, 0.0));
        }
        if (request.defined(ARG_EAST)) {
            clauses.add(Clause.le(lonCol,
                                  convertToISGSiteLogLatLongFormat(request.get(ARG_EAST, 0.0))));
            appendSearchCriteria(msgBuff, "east&lt;=",
                                 "" + request.get(ARG_EAST, 0.0));
        }
        if (request.defined(ARG_WEST)) {
            clauses.add(Clause.ge(lonCol,
                                  convertToISGSiteLogLatLongFormat(request.get(ARG_WEST, 0.0))));
            appendSearchCriteria(msgBuff, "west&gt;=",
                                 "" + request.get(ARG_WEST, 0.0));
        }

        if (request.defined(ARG_SITE_ID)) {
           addStringSearch(request, ARG_SITE_ID, " ",
                        msgBuff, "Site ID",
                        Tables.SITELOG_LOCATION.COL_FOURID, clauses);
        }

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
        /* order must match column order in e.g. SITELOG_LOCATION.COLUMNS */
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
        double latitude  = convertFromISGSiteLogLatLongFormat(Double.parseDouble(latString.trim()));
        double longitude = convertFromISGSiteLogLatLongFormat(Double.parseDouble(lonString.trim()));

        String elevationString = results.getString(colCnt++);
        elevationString = StringUtil.findPattern(elevationString,"([\\d\\.-]+)");
        double elevation  = elevationString!=null?Double.parseDouble(elevationString):0.0;
        //        System.err.println("lat:" + latString +" lon:" + lonString +" elev:" + elevationString);

        GsacSite site = new GsacSite(fourCharId, fourCharId, "",
                                     latitude, longitude, elevation);

        // Add  items to show in the HTML web page.
        // Note, this sets the order of items on the page.
        readIdentificationMetadata(site);  // name, type, lat, longi, DOMES number.
        site.addMetadata(new PoliticalLocationMetadata(country, state, city));
        readIdentificationMonumentMetadata(site);
        readAgencyMetadata(site);
        // to be fixed readCalibrationMetadata(site);
        readFrequencyStandardMetadata(site); 

        // site.addMetadata(new GnssEquipment(satelliteSystem));

        site.setType(new ResourceType("gnss.site.continuous"));
        return site;
    }


    public static double convertToISGSiteLogLatLongFormat(double decimalDegrees) {
        // Convert TO the IGS site log database lat/longi style
        // such as 505216.68 or -1141736.6, from double degrees values of latitude and longitude.
        // input like -50.253, to give -501510.8
        String msg  = "" + decimalDegrees;
        int degrees = (int)decimalDegrees; // + or minus , like -50
        decimalDegrees = decimalDegrees-degrees; // like 0.253
        int  minutes = (int)(decimalDegrees*60);  // like 15
        double seconds = (decimalDegrees-(minutes/60)) * 3600.0; // like .003 (degrees) * 3600 = 10.8
        double result;
        if (degrees >= 0.0) { 
            result = degrees*10000+minutes*100 + seconds;
        }
        else {
            result = (-1.0*degrees*10000) +minutes*100 + seconds;
            result *= -1.0;
        }
        // original
        //int  minutes = (int)(decimalDegrees/60);
        //double seconds = decimalDegrees-(minutes*60);
        //double result = degrees*10000+minutes*1000 + seconds; // oops 100 not 1000
        // end orig
        //System.err.println("convertToISGSiteLogLatLongFormat :" + msg +"  to igs site log format:" +  result + " " + degrees +" " + minutes +" " + seconds);
        return result;
    }


    public static double convertFromISGSiteLogLatLongFormat(double stupidFormat) {
        // Convert from the igs site log databse values such as 505216.68 or -1141736.6 to double values of latitude and longitude.
        // These input numbers pack into one string all the degrees minutes and seconds of a latitude or longitude
        // eg dddmmss.ff  where dd or ddd or ddd is + or - degrees, mm is minutes, ss.ff is seconds in with 2 or 3 decimal values ff.
        // note the sign in front applies to the final result, not only the degrees.
        int intValue = (int) stupidFormat;
        String ddmmss = String.valueOf(intValue);
        String secs = ddmmss.substring( ddmmss.length( ) - 2, ddmmss.length( )  );
        String mins = ddmmss.substring( ddmmss.length( ) - 4, ddmmss.length( )-2);
        String degs = ddmmss.substring(                    0, ddmmss.length( )-4);
        //System.err.println("convert:" + stupidFormat + " to:" + degs +" " + mins +" " + secs );
        //  convert:-661700.24 to:-66 17 00       convert:1103110.92 to:110 31 10
        int di = Integer.parseInt(degs);
        int mi = Integer.parseInt(mins);
        int si = Integer.parseInt(secs);
        double decimalofsecs = stupidFormat-intValue;
        double dv=0.0;  // the result decimal value
        if (di >= 0) {
            dv = di + (mi/60.0) + ((si + decimalofsecs)/3600.0);
        }
        else if (di <  0) {
            dv = (-1.0* di) + (mi/60.0) + ((si + decimalofsecs)/3600.0);
            dv *= -1.0;
        }
        //System.err.println("convert:" + stupidFormat + " to:" + dv );
        return dv;
    }

    private String readValue(ResultSet results, String column) throws Exception {
        String s  = results.getString(column);
        if(s==null) return "";
        if(s.startsWith("(") && s.endsWith(")")) return "";
        return s;
    }




    /**
     * get all of the metadata for the given site
     *
     * @param gsacResource resource
     *
     * @throws Exception On badness
     */
    @Override
    public void doGetMetadata(int level, GsacResource gsacResource) throws Exception {
        readEquipmentMetadata(gsacResource);
    }


    private  void readEquipmentMetadata(GsacResource gsacResource) throws Exception {
        Hashtable<Date,GnssEquipment> visits  = new Hashtable<Date,GnssEquipment>();
        List<GnssEquipment> equipmentList  = new ArrayList<GnssEquipment>();
        Statement statement;
        ResultSet  results;

        /* get from SITELOG_ANTENNA table */
        statement =
            getDatabaseManager().select(Tables.SITELOG_ANTENNA.COLUMNS,
                                        Tables.SITELOG_ANTENNA.NAME,
                                        Clause.eq(Tables.SITELOG_ANTENNA.COL_FOURID, gsacResource.getId()),
                                        " order by " + Tables.SITELOG_ANTENNA.COL_DATEINSTALLEDANTENNA, -1);
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {
                Date[] dateRange =
                    new Date[] {
                    readDate(results, Tables.SITELOG_ANTENNA.COL_DATEINSTALLEDANTENNA),
                    readDate(results, Tables.SITELOG_ANTENNA.COL_DATEREMOVEDANTENNA)};
                GnssEquipment equipment = 
                    new GnssEquipment(dateRange,
                                      results.getString(Tables.SITELOG_ANTENNA.COL_ANTENNATYPE),
                                      results.getString(Tables.SITELOG_ANTENNA.COL_SERIALNUMBERANTENNA),
                                      results.getString(Tables.SITELOG_ANTENNA.COL_ANTENNARADOMETYPE),
                                      results.getString(Tables.SITELOG_ANTENNA.COL_RADOMESERIALNUMBER),
                                      "", "", "",results.getDouble(Tables.SITELOG_ANTENNA.COL_MARKERUP));

                
                equipmentList.add(equipment);
                visits.put(dateRange[0], equipment); 
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }


        /* get from SITELOG_RECEIVER table */
        statement =
            getDatabaseManager().select(Tables.SITELOG_RECEIVER.COLUMNS,
                                        Tables.SITELOG_RECEIVER.NAME,
                                        Clause.eq(Tables.SITELOG_RECEIVER.COL_FOURID, gsacResource.getId()),
                                        " order by " + Tables.SITELOG_RECEIVER.COL_DATEINSTALLEDRECEIVER, -1);
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {
                Date[] dateRange =
                    new Date[] {
                    readDate(results, Tables.SITELOG_RECEIVER.COL_DATEINSTALLEDRECEIVER),
                    readDate(results, Tables.SITELOG_RECEIVER.COL_DATEREMOVEDRECEIVER)};
                GnssEquipment equipment = visits.get(dateRange[0]);
                if(equipment!=null) {
                    if(!Misc.equals(equipment.getToDate(),dateRange[1])) {
                        equipment = null;
                    }
                }

                if(equipment == null) {
                    //System.err.println ("Could not find corresponding antenna for equipment date:" + dateRange[0]);
                    equipment =  new GnssEquipment(dateRange, "","","","","","","",Double.NaN);
                    equipmentList.add(equipment);
                }
                equipment.setReceiver(results.getString(Tables.SITELOG_RECEIVER.COL_RECEIVERTYPE));
                equipment.setReceiverSerial(results.getString(Tables.SITELOG_RECEIVER.COL_SERIALNUMBERRECEIVER));
                equipment.setReceiverFirmware(results.getString(Tables.SITELOG_RECEIVER.COL_FIRMWAREV));
                equipment.setSatelliteSystem(results.getString(Tables.SITELOG_RECEIVER.COL_SATELLITESYSTEM));
                //System.err.println(dateRange[0] + " " +  equipment.getReceiver());
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }


        equipmentList = GnssEquipment.sort(equipmentList);
        GnssEquipmentGroup equipmentGroup = null;
        /* for every item 'equipment' in the local equipmentList, add it to the equipmentGroup */
        for(GnssEquipment equipment: equipmentList) {
            if (equipmentGroup == null) {
                gsacResource.addMetadata(equipmentGroup =
                                         new GnssEquipmentGroup());
            }
            equipmentGroup.add(equipment);
        }

    }


    // neww
    // get  from db table represented as class SITELOG_FREQUENCYSTANDARD the value of String COL_STANDARDTYPE 
    private  void readFrequencyStandardMetadata(GsacResource gsacResource) throws Exception {
        // compose db query statement; 'order by' phrase is null.
        Statement statement = getDatabaseManager().select(Tables.SITELOG_FREQUENCYSTANDARD.COLUMNS,
                                        Tables.SITELOG_FREQUENCYSTANDARD.NAME,
                                        Clause.eq(Tables.SITELOG_FREQUENCYSTANDARD.COL_FOURID, gsacResource.getId()),
                                        (String)null, -1);
        ResultSet  results;
        try {
            // do db query
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
            // process each line in results of db query; the GsacExtArgs item must have been added to GsacExtArgs.java.
            while ((results = iter.getNext()) != null) {
                addPropertyMetadata(gsacResource,GsacExtArgs.METADATA_FREQUENCYSTANDARD, "Clock", 
                                    results.getString(Tables.SITELOG_FREQUENCYSTANDARD.COL_STANDARDTYPE));
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
    }


    // neww
    /* old: get from SITELOG_RESPONSIBLEAGENCY table, value of AGENCYRESPONSIBLE, which ain't the normal thing so change to use: */
    /*      get from SITELOG_OPERATIONALCONTACT table, value of NAMEAGENCY */
    private  void readAgencyMetadata(GsacResource gsacResource) throws Exception {
        Statement statement =
            getDatabaseManager().select(Tables.SITELOG_OPERATIONALCONTACT.COLUMNS,
                                        Tables.SITELOG_OPERATIONALCONTACT.NAME,
                                        Clause.eq(Tables.SITELOG_OPERATIONALCONTACT.COL_FOURID, gsacResource.getId()),
                                        (String)null,-1);
        ResultSet  results;
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            // process each line in results of db query  
            while ((results = iter.getNext()) != null) {
                addPropertyMetadata(gsacResource,GsacExtArgs.SITE_METADATA_NAMEAGENCY, 
                                    "Agency",                        
                                    results.getString(Tables.SITELOG_OPERATIONALCONTACT.COL_NAMEAGENCY));
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
    }


    // neww
    // get  SITELOG_IDENTIFICATIONMONUMENT.COL_MONUMENTDESCRIPT
    private  void readIdentificationMonumentMetadata(GsacResource gsacResource) throws Exception {
        Statement statement =
            getDatabaseManager().select(Tables.SITELOG_IDENTIFICATIONMONUMENT.COLUMNS,
                                        Tables.SITELOG_IDENTIFICATIONMONUMENT.NAME,
                                        Clause.eq(Tables.SITELOG_IDENTIFICATIONMONUMENT.COL_FOURID, gsacResource.getId()),
                                        (String)null,-1);
        ResultSet  results;
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
                    
            // process each line in results of db query  
            while ((results = iter.getNext()) != null) {
                addPropertyMetadata(gsacResource,GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, 
                                    "Monument Description",                        
                                    results.getString(Tables.SITELOG_IDENTIFICATIONMONUMENT.COL_MONUMENTDESCRIPT));
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
    }


    private  void readIdentificationMetadata(GsacResource gsacResource) throws Exception {
        Statement statement =
            getDatabaseManager().select(Tables.SITELOG_IDENTIFICATION.COLUMNS,
                                        Tables.SITELOG_IDENTIFICATION.NAME,
                                        Clause.eq(Tables.SITELOG_IDENTIFICATION.COL_FOURID, gsacResource.getId()),(String)null,-1);

        ResultSet  results;
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
                    
            // process each line in results of db query  
            while ((results = iter.getNext()) != null) {
                gsacResource.setLongName(results.getString(Tables.SITELOG_IDENTIFICATION.COL_SITENAME));

                // not wanted Oct 5
                //addPropertyMetadata(gsacResource,GsacExtArgs.SITE_METADATA_MONUMENTINSCRIPTION, 
                //                    "Monument Inscription",
                //                    results.getString(Tables.SITELOG_IDENTIFICATION.COL_MONUMENTINSCRI));

                // args to addPropertyMetadata() are:
                // the resource you are adding it to;
                // the
                // the label on the web page or results
                // the db column name 

                addPropertyMetadata(gsacResource, GsacExtArgs.SITE_METADATA_IERDOMES, 
                                    "IERS DOMES",
                                    results.getString(Tables.SITELOG_IDENTIFICATION.COL_IERDOMES));

                // CDP number is not wanted currently as per FB Oct 5 2012
                //addPropertyMetadata(gsacResource,GsacExtArgs.SITE_METADATA_CDPNUM, 
                //                    "CDP Number",
                //                    results.getString(Tables.SITELOG_IDENTIFICATION.COL_CDPNUM));
                //Only read the first row
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }


    }



    private void addPropertyMetadata(GsacResource gsacResource, String id, String label, String value) {
        if(value!=null && value.length()>0) {
            gsacResource.addMetadata(new PropertyMetadata(id, value, label));
        }
    }


    private Date readDate(ResultSet results, String column) {
        try {
            return results.getDate(column);
        } catch(Exception exc) {
            //if the date is undefined we get an error so we just return null to signify the current time
            return null;
        }
    }


}
