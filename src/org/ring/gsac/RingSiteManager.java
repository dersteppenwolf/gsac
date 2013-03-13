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

package org.ring.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.metadata.gnss.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.HtmlOutputHandler;
import org.gsac.gsl.util.*;

/* CHANGEME - done in for INGV - include datahase package for the GSAC installation. */
import org.ring.gsac.database.*;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 * Handles all of the site related repository requests
 * The main entry point is the  {@link #handleRequest} method.
 *
 * @author         Jeff McWhirter
 */
public class RingSiteManager extends SiteManager {


    /**
     * ctor
     *
     * @param repository the repository
     */
    public RingSiteManager(RingRepository repository) {
        super(repository);
    }


    /**
     * CHANGEME - done for INGV RING.   Get the site search capabilities, what to search with.
     * Here is where you implement items to appear in site queries for this database.
     *
     * @return site search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        try {
            List<Capability> capabilities = new ArrayList<Capability>();

            String help = HtmlOutputHandler.stringSearchHelp;  /* where from ? */

            // language
            Capability siteCode =
                initCapability(new Capability(ARG_SITE_CODE, "Nome Sito / Site Code",
                    Capability.TYPE_STRING), CAPABILITY_GROUP_SITE_QUERY,
                     "Short name of the site", "Short name of the site. " + help);

            siteCode.setBrowse(true);  /* which does ? */
            capabilities.add(siteCode);

            // language:   latitudine longitudine
            capabilities
                .add(initCapability(new Capability(ARG_BBOX, "Posizione Limiti / Lat-Lon Bounding Box", Capability.TYPE_SPATIAL_BOUNDS), 
                    CAPABILITY_GROUP_SITE_QUERY, "Spatial bounds within which the site lies"));

            String[] values;

            // language:  luogo
            values = getDatabaseManager().readDistinctValues(
                Tables.SITI.NAME,  // for a db table name
                Tables.SITI.COL_LUOGO);  // for the db table and field name
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_CITY, "Luogo / Place", values, true, CAPABILITY_GROUP_ADVANCED));

            // language: regione 
            values = getDatabaseManager().readDistinctValues(
                Tables.SITI.NAME,
                Tables.SITI.COL_REGIONE);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_STATE, "Regione / Provincia / State", values, true, CAPABILITY_GROUP_ADVANCED));

            // language: regione nazione
            values = getDatabaseManager().readDistinctValues(
                Tables.SITI.NAME,
                Tables.SITI.COL_NAZIONE);
            Arrays.sort(values);
            capabilities.add(new Capability(GsacExtArgs.ARG_COUNTRY, "Nazione / Country", values, true, CAPABILITY_GROUP_ADVANCED));

            return capabilities;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     *   This is the main entry point for handling queries
     *   If you don't implement this method then the base SiteManager class will
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        //CHANGEME  -done for ingv / ring 
        super.handleRequest(request, response);
    }


    /**
     * CHANGEME - done for RING  whatsit
     * create and return GSAC's internal "resource" (site object) [NOT a db object]  identified by the given resource id  in this case NOME_SITO
     *
     * @param resourceId resource id. 
     *
     * @return the resource or null if not found
     *
     * @throws Exception on badness
     */
    public GsacResource getResource(String resourceId) throws Exception {

        Clause clause = Clause.eq(Tables.SITI.COL_NOME_SITO, resourceId);

        Statement statement =
            getDatabaseManager().select(getResourceSelectColumns(),
                                        clause.getTableNames(), clause);
        try {
            ResultSet results = statement.getResultSet();
            if ( !results.next()) {
                results.close();

                return null;
            }
            GsacSite site = (GsacSite) makeResource(results);
            results.close();

            return site;
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
    }



    /**
     * How to use the Clause class to assemble a set of database search clauses from the URL [whatsit or html search form?] argument  
     *
     * @param request the resquest
     * @param response the response
     * @param tableNames _more_
     * @param msgBuff buffer to append search criteria to
     *
     * @return list of clauses for selecting sites
     */
    public List<Clause> getResourceClauses(GsacRequest request,
                                           GsacResponse response,
                                           List<String> tableNames,
                                           StringBuffer msgBuff) {
        tableNames.add(Tables.SITI.NAME);
        List<Clause> clauses = new ArrayList();
        String       latCol  = Tables.SITI.COL_LATITUDINE;
        String       lonCol  = Tables.SITI.COL_LONGITUDINE;
        if (request.defined(ARG_NORTH)) {
            clauses.add(
                Clause.le(
                    latCol,
                    convertToISGSiteLogLatLongFormat(
                        request.get(ARG_NORTH, 0.0))));
            appendSearchCriteria(msgBuff, "north&lt;=",
                                 "" + request.get(ARG_NORTH, 0.0));
        }
        if (request.defined(ARG_SOUTH)) {
            clauses.add(
                Clause.ge(
                    latCol,
                    convertToISGSiteLogLatLongFormat(
                        request.get(ARG_SOUTH, 0.0))));
            appendSearchCriteria(msgBuff, "south&gt;=",
                                 "" + request.get(ARG_SOUTH, 0.0));
        }
        if (request.defined(ARG_EAST)) {
            clauses.add(
                Clause.le(
                    lonCol,
                    convertToISGSiteLogLatLongFormat(
                        request.get(ARG_EAST, 0.0))));
            appendSearchCriteria(msgBuff, "east&lt;=",
                                 "" + request.get(ARG_EAST, 0.0));
        }
        if (request.defined(ARG_WEST)) {
            clauses.add(
                Clause.ge(
                    lonCol,
                    convertToISGSiteLogLatLongFormat(
                        request.get(ARG_WEST, 0.0))));
            appendSearchCriteria(msgBuff, "west&gt;=",
                                 "" + request.get(ARG_WEST, 0.0));
        }

        if (request.defined(ARG_SITE_ID)) {
            addStringSearch(request, ARG_SITE_ID, " ", msgBuff, "Numero Sito / Site ID",
                            Tables.SITI.COL_ID_SITO, clauses);
        }

        addStringSearch(request, ARG_SITECODE, ARG_SITECODE_SEARCHTYPE,
                        msgBuff, "Numero Sito / Site Code",
                        Tables.SITI.COL_NOME_SITO, clauses);

        if (request.defined(GsacExtArgs.ARG_COUNTRY)) {
            List<String> values =
                (List<String>) request.getDelimiterSeparatedList(
                    GsacExtArgs.ARG_COUNTRY);
            clauses.add(
                Clause.or(
                    Clause.makeStringClauses(
                        Tables.SITI.COL_NAZIONE, values)));
        }

        return clauses;
    }





    /**
     * CHANGEME Default query order. done for RING 
     *   Set this to what you want to sort on                  whatsit   order for what?
     */
    private static final String SITE_ORDER =
        " ORDER BY  " + Tables.SITI.COL_NOME_SITO + " ASC ";


    // or by id number: " ORDER BY  " + Tables.SITI.COL_ID_SITO + " ??? see mysql order by syntax ";


    /**
     * Get the columns that are to be searched on              
     *
     * @param request the request
     *
     * @return comma delimited fully qualified column names to select on
     */
    public String getResourceSelectColumns() {
        return Tables.SITI.COLUMNS;
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
     * Create a single site: apparently, read all the values for the GSAC site internal site or 'resource' object
     *
     * @param results db results
     *
     * @return the site
     *
     * @throws Exception on badness
     */
    @Override
    public GsacSite makeResource(ResultSet results) throws Exception {
        int colCnt = 1;
        //              whatsit - hpw does this know which table?
        /*   order must match column order in the table e.g. for the IGS site log db:
        class SITELOG_LOCATION extends Tables {
        public static final String NAME = "SiteLog_Location";
        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
         column 1: public static final String COL_FOURID =  NAME + ".FourID";
        public static final String COL_CITY =  NAME + ".City";
        public static final String COL_STATE =  NAME + ".State";
        public static final String COL_COUNTRY =  NAME + ".Country";
        public static final String COL_TECTONIC =  NAME + ".Tectonic";
        public static final String COL_XCOOR =  NAME + ".XCoor";
        public static final String COL_YCOOR =  NAME + ".YCoor";
        public static final String COL_ZCOOR =  NAME + ".ZCoor";
        public static final String COL_LATITUDENORTH =  NAME + ".LatitudeNorth";
        public static final String COL_LONGITUDEEAST =  NAME + ".LongitudeEast";
        public static final String COL_ELEVATION =  NAME + ".Elevation";
        */
        /* for Ring ingv db:
        public static class SITI extends Tables {
        public static final String NAME = "siti";
        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
         column 1: public static final String COL_ID_SITO =  NAME + ".id_sito";
        public static final String COL_NOME_SITO =  NAME + ".nome_sito";
        public static final String COL_ID_RETE =  NAME + ".id_rete";
        public static final String COL_DATA_MATERIALIZZAZIONE =  NAME + ".data_materializzazione";
        public static final String COL_DATA_DISMISSIONE =  NAME + ".data_dismissione";
        public static final String COL_GEOLOGIA =  NAME + ".geologia";
        public static final String COL_LUOGO =  NAME + ".luogo";
        public static final String COL_LATITUDINE =  NAME + ".latitudine";
        public static final String COL_LONGITUDINE =  NAME + ".longitudine";
        public static final String COL_QUOTA =  NAME + ".quota";
        public static final String COL_X =  NAME + ".x";
        public static final String COL_Y =  NAME + ".y";
        public static final String COL_Z =  NAME + ".z";
        public static final String COL_X_UTMED50 =  NAME + ".x_utmed50";
        public static final String COL_Y_UTMED50 =  NAME + ".y_utmed50";
        public static final String COL_TIPO_MATERIALIZZAZIONE =  NAME + ".tipo_materializzazione";
        public static final String COL_HEIGHT_OF_MONUMENT =  NAME + ".height_of_monument";
        public static final String COL_MONUMENT_FOUNDATION =  NAME + ".monument_foundation";
        public static final String COL_FOUNDATION_DEPTH =  NAME + ".foundation_depth";
        public static final String COL_MONUMENT_INSCRIPTION =  NAME + ".monument_inscription";
        public static final String COL_IERS_DOMES_NUMBER =  NAME + ".iers_domes_number";
        public static final String COL_BEDROCK_TYPES =  NAME + ".bedrock_types";
        public static final String COL_ID_ON_SITE_AGENCY =  NAME + ".id_on_site_agency";
        public static final String COL_ID_RESPONSIBLE_AGENCY =  NAME + ".id_responsible_agency";
        public static final String COL_ID_MONUMENTO =  NAME + ".id_monumento";
        public static final String COL_NAZIONE =  NAME + ".nazione";
        public static final String COL_REGIONE =  NAME + ".regione";
        */

        colCnt += 1;  
        String fourCharId = results.getString(colCnt++);
        colCnt += 4;  
        String city       = results.getString(colCnt++);
        String latString = results.getString(colCnt++);
        String lonString = results.getString(colCnt++);
        colCnt += 11;  
        String iersdomes = results.getString(colCnt++);  // see below    addPropertyMetadata( gsacResource, GsacExtArgs.SITE_METADATA_IERDOMES, "IERS DOMES",
        colCnt += 4;  
        String country    = results.getString(colCnt++);
        String state      = results.getString(colCnt++);

        int monid = results.getInt(Tables.SITI.COL_ID_MONUMENTO);
        int agencyid = results.getInt(Tables.SITI.COL_ID_RESPONSIBLE_AGENCY);

        double latitude  = 0.0;
         if (latString != null) { latitude = convertFromISGSiteLogLatLongFormat( Double.parseDouble(latString.trim())); }
        double longitude  = 0.0;
         if (lonString != null) { longitude = convertFromISGSiteLogLatLongFormat( Double.parseDouble(lonString.trim())); }
        //String elevationString = results.getString(colCnt++);
        //elevationString = StringUtil.findPattern(elevationString, "([\\d\\.-]+)");
        //double elevation = (elevationString != null) ? Double.parseDouble(elevationString) : 0.0; 
        //        System.err.println("lat:" + latString +" lon:" + lonString +" elev:" + elevationString);

        GsacSite site = new GsacSite(fourCharId, fourCharId, "", latitude, longitude, 0.0);

        // Add items to show in the HTML web page.
        // does this set the order of items on the page.
        site.addMetadata(new PoliticalLocationMetadata(country, state, city));

        //if (iersdomes != null ) {
        //   site.addMetadata(new PropertyMetadata("iersdomes", iersdomes, "IERS DOMES"));  } // 3rd arg is web page label

        // readFrequencyStandardMetadata(site);
        // readAgencyMetadata(site);

        site.setType(new ResourceType("gnss.site.continuous"));

        return site;
    }


    /**
     * convert input value in decimal arc degrees to special ISG Site Log Lat Longi Format
     *
     * @param decimalDegrees _more_
     *
     * @return _more_
     */
    public static double convertToISGSiteLogLatLongFormat(
            double decimalDegrees) {
        // Convert TO the IGS site log database lat/longi style
        // such as 505216.68 or -1141736.6, from double degrees values of latitude and longitude.
        // input like -50.253, to give -501510.8
        String msg     = "" + decimalDegrees;
        int    degrees = (int) decimalDegrees;  // + or minus , like -50
        decimalDegrees = decimalDegrees - degrees;  // like 0.253
        int minutes = (int) (decimalDegrees * 60);  // like 15
        double seconds = (decimalDegrees - (minutes / 60)) * 3600.0;  // like .003 (degrees) * 3600 = 10.8
        double result;
        if (degrees >= 0.0) {
            result = degrees * 10000 + minutes * 100 + seconds;
        } else {
            result = (-1.0 * degrees * 10000) + minutes * 100 + seconds;
            result *= -1.0;
        }
        return result;
    }


    /**
     * Convert the weird IGS site log database value format for latitude and longitude, such as 505216.68 or -1141736.6 
     * to correct values of latitude and longitude.
     *
     * @param stupidFormat _more_
     *
     * @return _more_
     */
    public static double convertFromISGSiteLogLatLongFormat(double stupidFormat) {
        // These input numbers pack into one string all the degrees minutes and seconds of a latitude or longitude
        // eg dddmmss.ff  where dd or ddd or ddd is + or - degrees, mm is minutes, ss.ff is seconds in with 2 or 3 decimal values ff.
        // Note the sign in front applies to the final result, not only the degrees.
        
        // check if the value actually is in range of -360 to +360,  so probably is a correct format:
        if (stupidFormat >-361.0 && stupidFormat<361.0) { return stupidFormat; }

        if (Double.isNaN(stupidFormat)) {
            System.err.println(" RingSiteManager:convertFromISGSiteLogLatLongFormat() has bad or NaN 'number' input (lat or longitude):" + stupidFormat );
            // input value is not a number,  such as "" from some IGS SLM  values.  CHECK: perhaps should return an impossible value, 9999, used for similar purpose in GAMIT station.info format. 
            return 9999;
        }
        //System.err.println("convert IGS SLM lat/long formated value: " + stupidFormat );
        int    intValue = (int) stupidFormat;
        String ddmmss   = String.valueOf(intValue);
        String secs = ddmmss.substring(ddmmss.length() - 2, ddmmss.length());
        String mins     = ddmmss.substring(ddmmss.length() - 4,
                                       ddmmss.length() - 2);
        String degs = ddmmss.substring(0, ddmmss.length() - 4);
        //System.err.println(" RingSiteManager:convertFromISGSiteLogLatLongFormat() converted:" + stupidFormat + " to:" + degs +" " + mins +" " + secs );
        //  convert:-661700.24 to:-66 17 00       convert:1103110.92 to:110 31 10
        if (degs.equals("")) { degs="0"; }
        int nq = 1; // flag for a negative value when the degs part has no numbers, just the '-' sign
        if (degs.equals("-")) { degs="0"; nq=-1;}
        if (secs.equals("")) { secs="0"; }
        if (mins.equals("")) { mins="0"; }
        int    di            = Integer.parseInt(degs);
        int    mi            = Integer.parseInt(mins);
        int    si            = Integer.parseInt(secs);
        double decimalofsecs = stupidFormat - intValue;
        double dv            = 0.0;  // the result decimal value
        if (di >= 0) {
            dv = di + (mi / 60.0) + ((si + decimalofsecs) / 3600.0);
        } else if (di < 0 || nq==-1) {
            //add all the pieces as positive numbers to get full value;
            dv = (-1.0 * di) + (mi / 60.0) + ((si + decimalofsecs) / 3600.0);
            // and make  negative of the sum
            dv *= -1.0;
        }
        // CHECK LOOK: new code: could check for out of range latitude and longitude 

        return dv;
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
     * get all of the metadata for the given site     whatsit  who calls this?
     *
     *
     * @param level _more_
     * @param gsacResource resource
     *
     * @throws Exception On badness
     */
    @Override
    public void doGetMetadata(int level, GsacResource gsacResource)
            throws Exception {
        readEquipmentMetadata(gsacResource);
        readIdentificationMetadata(gsacResource);
    }

    /**
     * get id metadata for the given site  
     *
     * @param gsacResource resource
     *
     * @throws Exception On badness
     */
   private void readIdentificationMetadata(GsacResource gsacResource)
            throws Exception {

        ResultSet results;

        /* make a db query state to find the site corresponding to the current site or "gsacResource"; the NOME_SITO is stored as the resource's Id */
        Statement statement =
           getDatabaseManager().select( Tables.SITI.COLUMNS, Tables.SITI.NAME,
                Clause.eq( Tables.SITI.COL_NOME_SITO, gsacResource.getId()), (String) null, -1);

        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);

            // process each line in results of db query  
            while ((results = iter.getNext()) != null) {

                /* for RING which does not have a site long name db value, combine the 4 character site ID with the place name 
                   to make a kind of long name. */
                gsacResource.setLongName(
                    results.getString(Tables.SITI.COL_NOME_SITO) +" "+ results.getString(Tables.SITI.COL_LUOGO));

                /* get and check IERS_DOMES. */
                String idn= results.getString( Tables.SITI.COL_IERS_DOMES_NUMBER);
                /* trap bad value  "(A9)" and fix  */
                if ( idn != null && idn.equals("(A9)") ) { idn = " " ; }

                /* argument 4 in this method's input is the value to save in the GSAC site object */
                /* save the checked IERS DOMES value */
                addPropertyMetadata(
                    gsacResource, GsacExtArgs.SITE_METADATA_IERDOMES, "IERS DOMES", idn);

                addPropertyMetadata(
                    gsacResource, GsacExtArgs.SITE_METADATA_NAMEAGENCY, "Agency", "agency ");

                // CDP number is not in RING database?

                //Only read the first row of db query results returned
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }




        List<Clause> clauses = new ArrayList<Clause>();

        // db query part, to join the tables siti  and MONUMENTI_GPS for this site's ID_MONUMENTO
        clauses.add(Clause.eq(Tables.SITI.COL_NOME_SITO, gsacResource.getId()));
        clauses.add(Clause.join(Tables.SITI.COL_ID_MONUMENTO, Tables.MONUMENTI_GPS.COL_ID_MONUMENTO));
        //clauses.add( Clause.eq( Tables.SITI.COL_NOME_SITO, gsacResource.getId()) );

        String cols=SqlUtil.comma(new String[]{Tables.MONUMENTI_GPS.COL_DESCRIZIONE});

        List<String> tables = new ArrayList<String>();

        // the db query does "from" the  tables siti and MONUMENTI_GPS
        tables.add(Tables.SITI.NAME);
        tables.add(Tables.MONUMENTI_GPS.NAME);

        statement =
            getDatabaseManager().select(cols,  tables,  Clause.and(clauses),  (String) null,  -1); 
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);

            // process each line in results of db query  
            while ((results = iter.getNext()) != null) {

                addPropertyMetadata(
                    gsacResource, GsacExtArgs.SITE_METADATA_MONUMENTDESCRIPTION, "Monument description", 
                     results.getString(Tables.MONUMENTI_GPS.COL_DESCRIZIONE) );

                //Only read the first row of db query results returned
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }



    }


    /**
     *  get equipment metadata from the tables siti , MANUTENZIONE_ANTENNA, and STRUMENTI_ANTENNA
     *
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     */
    private void readEquipmentMetadata(GsacResource gsacResource)
            throws Exception {

        Hashtable<Date, GnssEquipment> visits = new Hashtable<Date,
                                                    GnssEquipment>();
        List<GnssEquipment> equipmentList = new ArrayList<GnssEquipment>();
        Statement           statement;
        ResultSet           results;

        List<Clause> clauses = new ArrayList<Clause>();

        // db query part, to join the tables siti , MANUTENZIONE_ANTENNA, and STRUMENTI_ANTENNA
        clauses.add(Clause.eq(Tables.SITI.COL_NOME_SITO, gsacResource.getId())); 
        clauses.add(Clause.join(Tables.SITI.COL_ID_SITO, Tables.MANUTENZIONE_ANTENNA.COL_ID_SITO)); 
        clauses.add(Clause.join(Tables.MANUTENZIONE_ANTENNA.COL_ID_ANTENNA, Tables.STRUMENTI_ANTENNA.COL_ID_ANTENNA)); 

        String cols=SqlUtil.comma(new String[]{
             Tables.MANUTENZIONE_ANTENNA.COL_DATE_INSTALLED ,Tables.MANUTENZIONE_ANTENNA.COL_DATE_REMOVED , Tables.MANUTENZIONE_ANTENNA.COL_MARKER_ARP_UP ,
             Tables.MANUTENZIONE_ANTENNA.COL_MARKER_ARP_NORTH , Tables.MANUTENZIONE_ANTENNA.COL_MARKER_ARP_EAST , Tables.MANUTENZIONE_ANTENNA.COL_ALIGNMENT ,
             Tables.STRUMENTI_ANTENNA.COL_ANTENNA_TYPE , Tables.STRUMENTI_ANTENNA.COL_SERIAL_NUMBER  , Tables.STRUMENTI_ANTENNA.COL_RADOME_TYPE  , Tables.STRUMENTI_ANTENNA.COL_RADOME_SERIAL_NUMBER  
               });

        List<String> tables = new ArrayList<String>();

        // the db query does "from" the  tables siti , MANUTENZIONE_ANTENNA, and STRUMENTI_ANTENNA
        tables.add(Tables.SITI.NAME);
        tables.add(Tables.MANUTENZIONE_ANTENNA.NAME);
        tables.add(Tables.STRUMENTI_ANTENNA.NAME);

        statement =
            getDatabaseManager()
                .select(cols,  tables, Clause.and(clauses),
                         " order by " + Tables.MANUTENZIONE_ANTENNA.COL_DATE_INSTALLED, -1);
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {
                //    System.err.println(results.getString(Tables.STRUMENTI_ANTENNA.COL_ANTENNA_TYPE));

                Date[] dateRange =
                    new Date[] {
                        readDate( results, Tables.MANUTENZIONE_ANTENNA.COL_DATE_INSTALLED),
                        readDate( results, Tables.MANUTENZIONE_ANTENNA.COL_DATE_REMOVED)       };

                // trap and fix bad non-numerical value got from the db: Tables.SITELOG_ANTENNA.COL_MARKERUP
                double deltahgt = 0.0;
                String sord = results.getString(Tables.MANUTENZIONE_ANTENNA.COL_MARKER_ARP_UP);
                if (checkDouble(sord)) 
                    { deltahgt = Double.parseDouble(sord); }
                else { 
                    // got a bad character string which was supposed to be a float double.
                    // do iterate along the number-as-string and use String.charAt(i).isDigit(); to extract whatever decimal number may be there, if there is one...
                    String snum = "";
                    for (int is = 0; is< sord.length(); is++){
                        char c = sord.charAt(is);        
                        if (Character.isDigit(c) || c=='.' ) { snum =  snum+c; } 
                    }
                    // if that constructed a string representing a number:
                    if (snum.length()==0) { deltahgt = 0.0; }
                    else { deltahgt = Double.parseDouble(snum); }
                    System.err.println("    RingSiteManager: bad 'double' char string from the db for MANUTENZIONE_ANTENNA.COL_MARKER_ARP_UP is " + sord+"';  will use double="+snum);
                }


                // see: src/org/gsac/gsl/metadata/gnss/GsacEquipment.java : 
                /*  public GnssEquipment(Date[] dateRange, String antenna,
                         String antennaSerial, String dome, String domeSerial, 

                         String receiver, String receiverSerial, String receiverFirmware,

                         double zOffset) { */

                GnssEquipment equipment =
                    new GnssEquipment(dateRange,
                       results.getString(Tables.STRUMENTI_ANTENNA.COL_ANTENNA_TYPE),
                       results.getString(Tables.STRUMENTI_ANTENNA.COL_SERIAL_NUMBER),
                       results.getString(Tables.STRUMENTI_ANTENNA.COL_RADOME_TYPE),
                       results.getString(Tables.STRUMENTI_ANTENNA.COL_RADOME_SERIAL_NUMBER),
                        "", "", "", deltahgt);

                equipmentList.add(equipment);
                visits.put(dateRange[0], equipment);
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }


        clauses = new ArrayList<Clause>();
        tables = new ArrayList<String>();

        clauses.add(Clause.eq(Tables.SITI.COL_NOME_SITO, gsacResource.getId())); 
        clauses.add(Clause.join(Tables.SITI.COL_ID_SITO, Tables.MANUTENZIONE_RICEVITORE.COL_ID_SITO)); 
        clauses.add(Clause.join(Tables.MANUTENZIONE_RICEVITORE.COL_ID_RICEVITORE, Tables.STRUMENTI_RICEVITORE.COL_ID_RICEVITORE)); 

        cols=SqlUtil.comma(new String[]{
             Tables.MANUTENZIONE_RICEVITORE.COL_DATE_INSTALLED ,Tables.MANUTENZIONE_RICEVITORE.COL_DATE_REMOVED , Tables.MANUTENZIONE_RICEVITORE.COL_FIRMWARE_VERSION,
             Tables.STRUMENTI_RICEVITORE.COL_RICEVITORE_TYPE , Tables.STRUMENTI_RICEVITORE.COL_SISTEMA_SATELLITE , Tables.STRUMENTI_RICEVITORE.COL_SERIAL_NUMBER  
               });

        tables.add(Tables.SITI.NAME);
        tables.add(Tables.MANUTENZIONE_RICEVITORE.NAME);
        tables.add(Tables.STRUMENTI_RICEVITORE.NAME);

        statement =
            getDatabaseManager()
                .select(cols,  tables, Clause.and(clauses),
                        " order by " + Tables.MANUTENZIONE_RICEVITORE.COL_DATE_INSTALLED, -1);
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            while ((results = iter.getNext()) != null) {
                Date[] dateRange =
                    new Date[] {
                        readDate(results, Tables.MANUTENZIONE_RICEVITORE.COL_DATE_INSTALLED),
                        readDate(results, Tables.MANUTENZIONE_RICEVITORE.COL_DATE_REMOVED)   };
                if (dateRange [0]==null) {
                     //System.err.println( " null date start, end date is  "+dateRange[1]);
                     continue;
                   } 
                //System.err.println( " got date ");
                GnssEquipment equipment = visits.get(dateRange[0]);
                if (equipment != null) {
                    if ( !Misc.equals(equipment.getToDate(), dateRange[1])) {
                        equipment = null;
                    }
                }

                if (equipment == null) {
                    equipment = new GnssEquipment(dateRange, "", "", "", "",
                            "", "", "", Double.NaN);
                    equipmentList.add(equipment);
                }
                equipment.setReceiverFirmware(
                    results.getString(Tables.MANUTENZIONE_RICEVITORE.COL_FIRMWARE_VERSION ));

                equipment.setReceiver(
                    results.getString( Tables.STRUMENTI_RICEVITORE.COL_RICEVITORE_TYPE));
                equipment.setReceiverSerial(
                    results.getString( Tables.STRUMENTI_RICEVITORE.COL_SERIAL_NUMBER));
                equipment.setSatelliteSystem(
                    results.getString( Tables.STRUMENTI_RICEVITORE.COL_SISTEMA_SATELLITE));
                
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        equipmentList = GnssEquipment.sort(equipmentList);
        GnssEquipmentGroup equipmentGroup = null;

        /* for every item 'equipment' in the local equipmentList, add it to the equipmentGroup */
        for (GnssEquipment equipment : equipmentList) {
            if (equipmentGroup == null) {
                gsacResource.addMetadata(equipmentGroup = new GnssEquipmentGroup());
            }
            equipmentGroup.add(equipment);
        }
    }



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
     * from db table represented in Tables.java as class SITELOG_FREQUENCYSTANDARD,
     * get the value of String COL_STANDARDTYPE and add it (with the label "clock") to the GsacResource object "gsacResource".
     * in this case the site is recognized in the db with the getDatabaseManager().select() call.
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
                Clause.eq(
                    Tables.SITELOG_FREQUENCYSTANDARD.COL_FOURID,
                    gsacResource.getId()), (String) null, -1);
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
     * get from SITELOG_OPERATIONALCONTACT table, value of NAMEAGENCY
     *
     * @param gsacResource _more_
     *
     * @throws Exception _more_
     
    private void readAgencyMetadata(GsacResource gsacResource)
            throws Exception {
        Statement statement =
            getDatabaseManager().select(
                Tables.SITELOG_OPERATIONALCONTACT.COLUMNS,
                Tables.SITELOG_OPERATIONALCONTACT.NAME,
                Clause.eq(
                    Tables.SITELOG_OPERATIONALCONTACT.COL_FOURID,
                    gsacResource.getId()), (String) null, -1);
        ResultSet results;
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            // process each line in results of db query  
                // args to addPropertyMetadata() are [see definition of addPropertyMetadata in this file below]:
                // the resource you are adding it to;
                // the label on the web page or results
                // the db column name 
            while ((results = iter.getNext()) != null) {
                addPropertyMetadata(
                    gsacResource, GsacExtArgs.SITE_METADATA_NAMEAGENCY,
                    "Agency",
                    results.getString(
                        Tables.SITELOG_OPERATIONALCONTACT.COL_NAMEAGENCY));
                break;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
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
     * _more_
     *
     * @param results _more_
     * @param column _more_
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


}
