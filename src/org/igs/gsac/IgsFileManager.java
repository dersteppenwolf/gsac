/*
 * 
 */

package org.igs.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;


import ucar.unidata.util.StringUtil;
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
public class IgsFileManager extends FileManager {

    /**
     * ctor
     *
     * @param repository the repository
     */
    public IgsFileManager(IgsRepository repository) {
        super(repository);
    }



    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();
        //CHANGEME
        /*
          you can use the default file capabilities:
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
     * CHANGEME
     * handle the search request
     *
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badness
     */
    public void handleRequest(GsacRequest request,
                              GsacResponse response)
            throws Exception {
        //Some example code

        //The msgBuff holds the html that describes what is being searched for
        StringBuffer msgBuff = new StringBuffer();
        if (request.defined(ARG_FILESIZE_MIN)) {
            int size = request.get(ARG_FILESIZE_MIN, 0);
            appendSearchCriteria(msgBuff, "Filesize&gt;=",
                                 "" + request.get(ARG_FILESIZE_MIN, 0));
        }

        if (request.defined(ARG_FILESIZE_MAX)) {
            int size = request.get(ARG_FILESIZE_MAX, 0);
            appendSearchCriteria(msgBuff, "Filesize&lt;=",
                                 "" + request.get(ARG_FILESIZE_MAX, 0));
        }

        if (request.defined(ARG_FILE_TYPE)) {
            List<String> types = (List<String>) request.getList(ARG_FILE_TYPE);
            addSearchCriteria(msgBuff, "Resource Type", types,
                              ARG_FILE_TYPE);
        }

        Date[] publishDateRange =
            request.getDateRange(ARG_FILE_PUBLISHDATE_FROM,
                                 ARG_FILE_PUBLISHDATE_TO, null, null);

        if (publishDateRange[0] != null) {
            appendSearchCriteria(msgBuff, "Publish date&gt;=",
                                 "" + format(publishDateRange[0]));
        }

        if (publishDateRange[1] != null) {
            appendSearchCriteria(msgBuff, "Publish date&lt;=",
                                 "" + format(publishDateRange[1]));
        }


        Date[] dataDateRange = request.getDateRange(ARG_FILE_DATADATE_FROM,
                                                    ARG_FILE_DATADATE_TO, null,
                                                    null);

        if (dataDateRange[0] != null) {
            appendSearchCriteria(msgBuff, "Publish date&gt;=",
                                 "" + format(dataDateRange[0]));
        }

        if (dataDateRange[1] != null) {
            appendSearchCriteria(msgBuff, "Publish date&lt;=",
                                 "" + format(dataDateRange[1]));
        }

        //find and create the files
        /** 
            e.g.:
        GsacResource site = theSiteForThisFile; may be null
        String type = someType;
        GsacFile resource = new GsacFile(resourceId,
                                    new FileInfo(filePath, fileSize, md5),
                                    site,
                                    publishTime, fromTime, toTime,
                                    toResourceType(type));

                                    response.addResource(resource);
        **/
        setSearchCriteriaMessage(response, msgBuff);
    }

    

    /**
     * CHANGEME
     * This takes the resource id that is used to identify files and
     * creates a GsacFile object
     *
     * @param resourceId file id
     *
     * @return GsacFile
     *
     * @throws Exception On badness
     */
    public GsacResource getResource(String resourceId) throws Exception {
        return null;
    }



    /**
     * Create the list of resource types that are shown to the user. This is
     * called by the getDefaultCapabilities
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
    public IgsSiteManager getSiteManager() {
        return (IgsSiteManager) getRepository().getResourceManager(GsacSite.CLASS_SITE);
    }


}