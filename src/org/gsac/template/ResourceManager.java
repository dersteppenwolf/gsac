/*
 * 
 */

package @MACRO.PACKAGE@;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Handles all of the resource related repository requests. The main
 * entry point is {@link #handleRequest}
 * Look for the CHANGEME comments
 *
 * @author  Jeff McWhirter
 */
public class @MACRO.PREFIX@ResourceManager extends ResourceManager {

    /**
     * ctor
     *
     * @param repository the repository
     */
    public @MACRO.PREFIX@ResourceManager(@MACRO.PREFIX@Repository repository) {
        super(repository);
    }



    /**
     * CHANGEME
     * handle the request
     *
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badness
     */
    public void handleRequest(GsacRequest request,
                                      GsacResponse response)
            throws Exception {
        //Some example code to handle the default url arguments
        //NOTE: This isn't as well organized as the @MACRO.PREFIX@SiteManager 

        long         t1      = System.currentTimeMillis();
        List<Clause> clauses = new ArrayList<Clause>();

        StringBuffer msgBuff = new StringBuffer();
        //Here we get the site clauses
        List<Clause> siteClauses =
            getSiteManager().getSiteClauses(request, response, new ArrayList<String>(), msgBuff);
        if (siteClauses.size() > 0) {
            Clause siteClause = Clause.and(siteClauses);
            //Then we do an inner select
            /* e.g.:
            Clause resourceClause =
                Clause.in(
                    getExportSiteIdColumn(),
                    " distinct " + Tables.MV_DAI_PRO.COL_PERM_SITE_ID,
                    Tables.MV_DAI_PRO.NAME, siteClause);

            clauses.add(resourceClause);
            */
        }

        if (request.defined(ARG_FILESIZE_MIN)) {
            //e.g.:            clauses.add(Clause.ge(getExportFilesizeColumn(), request.get(ARG_FILESIZE_MIN, 0)));
            appendSearchCriteria(msgBuff, "Filesize&gt;=",
                                 "" + request.get(ARG_FILESIZE_MIN, 0));
        }

        if (request.defined(ARG_FILESIZE_MAX)) {
            //e.g.:            clauses.add(Clause.le(getExportFilesizeColumn(), request.get(ARG_FILESIZE_MAX, 0)));
            appendSearchCriteria(msgBuff, "Filesize&lt;=",
                                 "" + request.get(ARG_FILESIZE_MAX, 0));
        }

        if (request.defined(ARG_RESOURCE_TYPE)) {
            List<String> args = null;
            //add in the resource type clause
            //e.g.:
            /*
            clauses.add(
                Clause.or(
                    Clause.makeIntClauses(
                        "file type column",
                        args = (List<String>) request.getList(
                            ARG_RESOURCE_TYPE))));
            */
            addSearchCriteria(msgBuff, "Resource Type", args,
                              ARG_RESOURCE_TYPE);
        }


        Date[] publishDateRange =
            request.getDateRange(ARG_RESOURCE_PUBLISHDATE_FROM,
                                 ARG_RESOURCE_PUBLISHDATE_TO, null, null);

        if (publishDateRange[0] != null) {
            //e.g.:            clauses.add(Clause.ge("date column", publishDateRange[0]));
            appendSearchCriteria(msgBuff, "Publish date&gt;=",
                                 "" + format(publishDateRange[0]));
        }

        if (publishDateRange[1] != null) {
            //            clauses.add(Clause.le("date column", publishDateRange[1]));
            appendSearchCriteria(msgBuff, "Publish date&lt;=",
                                 "" + format(publishDateRange[1]));
        }


        Date[] dataDateRange = request.getDateRange(ARG_RESOURCE_DATADATE_FROM,
                                                    ARG_RESOURCE_DATADATE_TO, null,
                                                    null);

        if (dataDateRange[0] != null) {
            //add in clauses for dataDateRange
            appendSearchCriteria(msgBuff, "Publish date&gt;=",
                                 "" + format(dataDateRange[0]));
        }

        if (dataDateRange[1] != null) {
            //add in clauses for dataDateRange
            appendSearchCriteria(msgBuff, "Publish date&lt;=",
                                 "" + format(dataDateRange[1]));
        }

        List<String> tableNames = new ArrayList<String>();
        tableNames.add("files table name");

        Clause mainClause = Clause.and(clauses);
        Statement statement = getDatabaseManager().select(
                                                          getResourceColumns(),
                                                          mainClause.getTableNames(tableNames),
                                                          mainClause);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement, request.getOffset(), request.getLimit());
        int cnt = 0;
        while (iter.getNext() != null) {
            response.addObject(makeResource(iter.getResults()));
            cnt++;
            if ( !iter.countOK()) {
                response.setExceededLimit();
                break;
            }
        }
        iter.close();
        getDatabaseManager().closeAndReleaseConnection(statement);


        long t2 = System.currentTimeMillis();
        System.err.println("read " + cnt + " resources in " + (t2 - t1)
                           + "ms");

        setSearchCriteriaMessage(response, msgBuff);
    }

    
    public void doGetResourceMetadata(int level, GsacResource gsacResource)
        throws Exception {
    }


    /**
     * CHANGEME
     * Get the columns to select for resources
     * @return resource columns
     */
    private String getResourceColumns() {
        return "files.column1,files.column2, etc";
        //e.g:
        //return Tables.FILES.COLUMNS;
    }


    /**
     * CHANGEME
     * Create a resource from the given results
     *
     * @param results result set
     *
     * @return The resource
     *
     * @throws Exception On badness
     */
    public GsacResource makeResource(ResultSet results) throws Exception {
        return null;
        /* e.g.:
        int    col            = 1;
        String exportID       = results.getString(col++);
        String fileID         = results.getString(col++);
        int    archiveTypeID  = results.getInt(col++);
        int    exportTypeID   = results.getInt(col++);
        int    siteID      = results.getInt(col++);
        long   fileSize       = results.getLong(col++);
        String path           = results.getString(col++);
        Date   publishTime    = results.getDate(col++);
        String md5            = results.getString(col++);
        int    sampleInterval = results.getInt(col++);

        //TODO: select the times from the other tables
        Date fromTime = publishTime;
        Date toTime   = publishTime;

        ExportType type = ExportType.findType(ExportType.GROUP_ALL_TYPES,
                              exportTypeID);
        GsacSite site =
            getSiteManager().getSiteForResource(siteID);

        //Convert the file path to the ftp url
        path = DBUtil.getExportFtpUrl(path);
        GsacResource resource = new GsacResource(exportID,
                                    new FileInfo(path, fileSize, md5),
                                    site, publishTime, fromTime, toTime,
                                    toResourceType(type));

        return resource;
        */

    }

    /**
     * CHANGEME
     * We don't have example code for this
     *
     * @param resourceId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacObject getObject(String resourceId) throws Exception {
        //TODO:
        return null;
    }



    /**
     * Create the list of resource types that are shown to the user
     *
     * @return resource types
     */
    public List<ResourceType> doGetResourceTypes() {
        List<ResourceType> resourceTypes = new ArrayList<ResourceType>();
        resourceTypes.add(new ResourceType("rinex","RINEX Files"));
        resourceTypes.add(new ResourceType("qc","QC Files"));
        return resourceTypes;
    }




    /**
     * helper method
     *
     * @return sitemanager
     */
    public @MACRO.PREFIX@SiteManager getSiteManager() {
        return (@MACRO.PREFIX@SiteManager) getRepository().getObjectManager(GsacSite.TYPE_SITE);
    }




}
