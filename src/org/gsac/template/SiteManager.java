/*
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
 * Example site manager.
 *
 * @author         Jeff McWhirter
 */
public class @MACRO.PREFIX@SiteManager extends SiteManager {

    
    /** CHANGEME  basic site query info.    
        Set this to your table columns    */
    private static final String SITE_WHAT = " distinct "
                                               + SqlUtil.comma(new String[] {
                                                       "YourSiteTable.column1",
                                                       "YourSiteTable.column2",
                                                       "etc"
                                                   });

    /** CHANGEME Default query order. 
        Set this to what you want to sort on */
    private static final String SITE_ORDER =
        " ORDER BY  " + "YourSiteTable.sitecode" + " ASC ";


    /**
     * ctor
     *
     * @param repository the repository
     */
    public @MACRO.PREFIX@SiteManager(@MACRO.PREFIX@Repository repository) {
        super(repository);
    }


    public void handleSiteRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        super.handleSiteRequest(request, response);
    }


    /**
     *
     * @param request the resquest
     * @param response the response
     * @param msgBuff buffer to append search criteria to
     *
     * @return list of clauses for selecting sites
     */
    public List<Clause> getSiteClauses(GsacRequest request,
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
        if (request.defined(ARG_SITEID)) {
            //Here we use makeIntClauses for the site id
            /*            clauses.add(
                Clause.or(
                    Clause.makeIntClauses(
                        Tables.MV_DAI_PRO.COL_MON_ID,
                        args = (List<String>) request.getList(
                            ARG_SITEID))));
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
     * CHANGEME
     * Get the site from the database
     *
     * @param siteId site id. This isn't the site code but actually the monument id
     *
     * @return the site or null if not found
     *
     * @throws Exception on badness
     */
    public GsacSite getSite(String siteId) throws Exception {
        /* e.g.:
        Clause clause = Clause.eq(Tables.MV_DAI_PRO.COL_MON_ID,
                                  new Integer(siteId).intValue());
        Statement statement = getDatabaseManager().select(
                                                          getSiteSelectColumns(), 
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
        site.setType(new SiteType(type));
        if ((groups != null) && (groups.trim().length() > 0)) {
            List<String> toks = new ArrayList<String>();
            for (String tok : groups.split(",")) {
                toks.add(tok.trim());
            }
            Collections.sort(toks);
            for (String tok : (List<String>) toks) {
                site.addSiteGroup(new SiteGroup(tok));
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



    /**
     * get all of the metadata for the given site
     *
     * @param gsacSite site
     *
     * @throws Exception On badness
     */
    public void doGetSiteMetadata(int level, GsacSite gsacSite) throws Exception {
        //The unavcorepository adds in GnssEquipment metadata and other things
    }

    /**
     * Get the extra site search capabilities. 
     *
     * @return site search capabilities
     */
    public List<Capability> doGetSiteQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();
        //CHANGEME
        //Add in an example fruit enumerated query capability
        /*
        String[]values = {"banana","apple","orange"};
        Arrays.sort(values);
        capabilities.add(new Capability("fruit", "Fruit Label",
                                        values, true));
        */
        return capabilities;
    }


    /**
     * Get the site group list
     *
     * @return site group list
     */
    public List<SiteGroup> doGetSiteGroups() {
        List<SiteGroup> groups = new ArrayList<SiteGroup>();
        /**
           CHANGEME
        groups.add(new SiteGroup("group1","Group 1"));
        groups.add(new SiteGroup("group2", "Group 2"));
        groups.add(new SiteGroup("group3","Group 3"));
        Collections.sort((List) groups);
        */
        return groups;
    }


    /**
     * Get the list of site types
     *
     * @return list of site types
     */
    public List<SiteType> doGetSiteTypes() {
        List<SiteType> types = new ArrayList<SiteType>();
        //CHANGEME 
        /*
        types.add(new SiteType("sitetype1", "Type 1"));
        types.add(new SiteType("sitetype2", "Type 2"));
        types.add(new SiteType("sitetype3", "Type 3"));
        Collections.sort((List) types);
        */
        //Here is how the UnavcoRepository does it:
        /*
            Statement statement =
                getDatabaseManager().select(
                    " distinct " + Tables.MV_DAI_PRO.COL_SITE_TYPE,
                    Tables.MV_DAI_PRO.NAME, (Clause) null);
            String[] values =
                SqlUtil.readString(getDatabaseManager().getIterator(statement), 1);
            Arrays.sort(values);
            for (String type : values) {
                types.add(new SiteType(type, type));
            }
        */
        return types;
    }


    /**
     * Get the list of site statuses.
     *
     * @return list of site statuses
     */
    public List<SiteStatus> doGetSiteStatuses() {
        //CHANGEME  -site statuses
        List<SiteStatus> statuses = new ArrayList<SiteStatus>();
        statuses.add(new SiteStatus("active", "Active"));
        statuses.add(new SiteStatus("inactive", "Inactive"));
        return statuses;
    }



    /**
     * Get the columns that are to be searched on
     *
     * @param request the request
     *
     * @return comma delimited fully qualified column names to select on
     */
    public String getSiteSelectColumns() {
        return SITE_WHAT;
    }

    /**
     * Get the order by clause
     *
     * @param request the request
     *
     * @return order by clause
     */
    public String getSiteOrder(GsacRequest request) {
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

    public String getSiteSelectSuffix(GsacRequest request) {
        return super.getSiteSelectSuffix(request);
        //At unavco we end up doing a group by on our site select
        //Something like:
        //	return SITE_GROUP_BY+(request!=null?" " +getSiteOrder(request):"") ;
    }



}
