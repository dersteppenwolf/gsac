/*
 *
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

            // neww
            /* not wanted for query
            values = getDatabaseManager().readDistinctValues( Tables.SITELOG_LOCATION.NAME,
                                                             Tables.SITELOG_LOCATION.COL_CITY);
            Arrays.sort(values); 
            capabilities.add(                            new Capability(GsacExtArgs.ARG_CITY, 
																	"City",       
																	values, true, CAPABILITY_GROUP_ADVANCED));
            */

            // neww
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

            /* not wanted for query
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
        double latitude   = convertFromStupidFormat(Double.parseDouble(latString.trim()));
        double longitude   = convertFromStupidFormat(Double.parseDouble(lonString.trim()));

        String elevationString = results.getString(colCnt++);
        elevationString = StringUtil.findPattern(elevationString,"([\\d\\.-]+)");
        double elevation  = elevationString!=null?Double.parseDouble(elevationString):0.0;
        //        System.err.println("lat:" + latString +" lon:" + lonString +" elev:" + elevationString);

        GsacSite site = new GsacSite(fourCharId, fourCharId, "",
                                     latitude, longitude, elevation);
        //Add the name, etc
        readIdentificationMetadata(site);
        readIdentificationMonumentMetadata(site);
        site.addMetadata(new PoliticalLocationMetadata(country, state, city));
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
                    //                    System.err.println ("Could not find corresponding antenna for equipment date:" + dateRange[0]);
                    equipment =  new GnssEquipment(dateRange,
                                                   "","","","","","","",Double.NaN);
                    equipmentList.add(equipment);
                }
                equipment.setReceiver(results.getString(Tables.SITELOG_RECEIVER.COL_RECEIVERTYPE));
                equipment.setReceiverSerial(results.getString(Tables.SITELOG_RECEIVER.COL_SERIALNUMBERRECEIVER));
                equipment.setSatelliteSystem(results.getString(Tables.SITELOG_RECEIVER.COL_SATELLITESYSTEM));
                equipment.setSatelliteSystem(results.getString(Tables.SITELOG_RECEIVER.COL_FIRMWAREV));
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }



        equipmentList = GnssEquipment.sort(equipmentList);
        GnssEquipmentGroup equipmentGroup = null;
        for(GnssEquipment equipment: equipmentList) {
            if (equipmentGroup == null) {
                gsacResource.addMetadata(equipmentGroup =
                                         new GnssEquipmentGroup());
            }
            equipmentGroup.add(equipment);
        }


    }



    // neww
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
                addPropertyMetadata(gsacResource,GsacExtArgs.SITE_METADATA_IDENTIFICATIONMONUMENT, 
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
