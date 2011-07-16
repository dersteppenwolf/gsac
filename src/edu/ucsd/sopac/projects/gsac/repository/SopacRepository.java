/*
 *
 */

package edu.ucsd.sopac.projects.gsac.repository;


import org.gsac.gsl.*;
import org.gsac.gsl.database.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;


/**
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public class SopacRepository extends GsacRepository implements GsacConstants {

    /** url path before the /gsacws/...  */
    private String urlBase = "";

    /** html header */
    private String htmlHeader;

    /** html footer */
    private String htmlFooter;

    /**
     * ctor
     */
    public SopacRepository() {
        try {
            initResources();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * initialize resources
     * CHANGME: Change the header.html and footer.html
     *
     * @throws Exception on badness
     */
    private void initResources() throws Exception {
        String packageName = getClass().getPackage().getName();
        packageName = packageName.replace(".","/");
        htmlHeader = ucar.unidata.util.IOUtil.readContents("/"+packageName+"/resources/header.html",
                                                           getClass());
        htmlFooter = ucar.unidata.util.IOUtil.readContents("/"+packageName +"/resources/footer.html",
                                                           getClass());
    }


    /**
     * Factory method to create the database manager
     *
     * @return database manager
     *
     * @throws Exception on badness
     */
    public GsacDatabaseManager doMakeDatabaseManager() throws Exception {
        SopacDatabaseManager dbm = new SopacDatabaseManager(this);
        dbm.init();
        return dbm;
    }


    public GsacResourceManager doMakeResourceManager(ResourceClass type) {
        if (type.equals(GsacSite.CLASS_SITE)) {
            return new SopacSiteManager(this);
        }
        if (type.equals(GsacFile.CLASS_FILE)) {
            return new SopacFileManager(this);
        }
        return null;
    }



    /*
     * CHANGEME Is this repository capable of certain things
     */
    public boolean isCapable(String arg) {
        //          Can we order searches for sites and resources
        /*
        if(arg.equals(ARG_SITE_SORT_VALUE)) return true;
        if(arg.equals(ARG_SITE_SORT_ORDER)) return true;
        if(arg.equals(ARG_FILE_SORT_VALUE)) return true;
        if(arg.equals(ARG_FILE_SORT_ORDER)) return true;
        */
        return super.isCapable(arg);
    }


    /**
     * Get the url base that this repository uses.
     * e.g., all gsac urls begin with:
     * http://server/urlBase/gsacws
     * the default is blank, e.g.:
     * http://server/gsacws
     *
     * @return url base
     */
    // pj, 11/2/2010, per jeff: remove the getUrlBase as this is now implemented in the base class with the property file (where urlbase defaults to /gsacws).
/*
    public String getUrlBase() {
        return urlBase;
    }
*/


    /**
     * get the html header
     *
     * @param request the request
     *
     * @return html header
     */
    public String getHtmlHeader(GsacRequest request) {
        return htmlHeader;
    }


    /**
     * get the html footer
     *
     * @param request the request
     *
     * @return html footer
     */
    public String getHtmlFooter(GsacRequest request) {
        return htmlFooter;
    }

}
