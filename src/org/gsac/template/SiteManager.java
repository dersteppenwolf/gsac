/*
 *
 */

package @MACRO.PACKAGE@;


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
public class @MACRO.PREFIX@SiteManager extends SiteManager {

    
    /**
     * ctor
     *
     * @param repository the repository
     */
    public @MACRO.PREFIX@SiteManager(@MACRO.PREFIX@Repository repository) {
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

        /**
           Here's how to access the arguments
           if (request.defined(ARG_NORTH)) {
                request.get(ARG_NORTH, 0.0);
           }
           ARG_SOUTH,ARG_EAST, ARG_WEST
        */

        StringBuffer msgBuff = new StringBuffer();
        List<GsacSite> sites = new ArrayList<GsacSite>();
        //Create the sites ...
        //sites.add(new GsacSite(...));
        for (GsacSite site : sites) {
            response.addResource(site);
        }
        setSearchCriteriaMessage(response, msgBuff);
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
            GsacSite site = makeSite(results);
            results.close();
            return site;
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }
        */
        return  null;
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
        String latCol = "replace me with correct column name";
        String lonCol = "replace me with correct column name";
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
            //Here we use makeIntClauses for the site id
            /*            clauses.add(
                Clause.or(
                    Clause.makeIntClauses(
                        Tables.MV_DAI_PRO.COL_MON_ID,
                        args = (List<String>) request.getList(
                            ARG_SITE_ID))));
            */
            addSearchCriteria(msgBuff, "Site ID", args);
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
        /*
        addStringSearch(request, ARG_SITECODE, ARG_SITECODE_SEARCHTYPE,
                        msgBuff, "Site Code",
                        "e.g. Tables.MV_DAI_PRO.COL_MON_SITE_CODE", clauses);
        addStringSearch(request, ARG_SITENAME, ARG_SITENAME_SEARCHTYPE,
                        msgBuff, "Site Name",
                        Tables.SITE_INFORMATION.COL_SITE_NAME, clauses);

        if (request.defined(ARG_SITE_GROUP)) {
            //...
        }
        */
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
        " ORDER BY  " + "YourSiteTable.sitecode" + " ASC ";


    /**
     * Get the columns that are to be searched on
     *
     * @param request the request
     *
     * @return comma delimited fully qualified column names to select on
     */
    public String getResourceSelectColumns() {
        return " distinct "
            + SqlUtil.comma(new String[] {
                    "YourSiteTable.column1",
                    "YourSiteTable.column2",
                    "etc"
                });
    }


    /**
     * Get the order by clause
     *
     * @param request the request
     *
     * @return order by clause
     */
    public String getResourceOrder(GsacRequest request) {
        if(request  == null || !request.defined(ARG_SITE_SORT_VALUE)) {
            return SITE_ORDER;
        } 
        boolean ascending = request.getSiteAscending();
        StringBuffer cols = new StringBuffer();
        //CHANGEME: set this to use your column names for sorting
        for(String sort: request.getDelimiterSeparatedList(ARG_SITE_SORT_VALUE)) {
            String col = null;
            if(sort.equals(SORT_SITE_CODE)) {
                //                col = Tables.MV_DAI_PRO.COL_MON_SITE_CODE;
            } else if(sort.equals(SORT_SITE_NAME)) {
                //                col = Tables.MV_DAI_PRO.COL_MON_SITE_NAME;
            } else  if(sort.equals(SORT_SITE_TYPE)) {
                //                col = Tables.MV_DAI_PRO.COL_SITE_TYPE;
            } 
            if(col!=null) {
                if(cols.length()!=0) cols.append(",");
                //Oracle has a UPPER operator. We use this to sort on upper case
                cols.append("UPPER(" +col+")");
            }
        }
        if(cols.length()>0) {
            return orderBy(cols.toString(), ascending);
        }
        return SITE_ORDER;
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
    public GsacSite makeSite(ResultSet results) throws Exception {
        return null;
        /** e.g.:
        int    colCnt     = 1;
        int    monId      = results.getInt(colCnt++);
        String fourCharId = results.getString(colCnt++);
        String name       = results.getString(colCnt++);
        double latitude   = results.getDouble(colCnt++);
        double longitude  = results.getDouble(colCnt++);
        double elevation  = results.getDouble(colCnt++);
        String type       = results.getString(colCnt++);
        if (type == null) {
            type = "";
        }
        String  status = results.getString(colCnt++);
        String  groups = results.getString(colCnt++);

        GsacSite site = new GsacSite("" + monId, fourCharId, name,
                                  latitude, longitude, elevation);
        site.setType(new ResourceType(type));
        if ((groups != null) && (groups.trim().length() > 0)) {
            List<String> toks = new ArrayList<String>();
            for (String tok : groups.split(",")) {
                toks.add(tok.trim());
            }
            Collections.sort(toks);
            for (String tok : (List<String>) toks) {
                site.addResourceGroup(new ResourceGroup(tok));
            }
        }

        //Add icons based on type
        if (type.toLowerCase().equals(SITETYPE_CAMPAIGN)) {
            site.addMetadata(
            new IconMetadata(
                    "http://facility.unavco.org/data/gnss/lib/DAI/images/icon1.png"));
        } else {
            site.addMetadata(
            new IconMetadata(
                    "http://facility.unavco.org/data/gnss/lib/DAI/images/icon1.png"));
        }
        return site;
        */
    }





}
