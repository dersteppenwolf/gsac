/*
 * 
 */

package @MACRO.PACKAGE@;


import org.gsac.gsl.*;
import org.gsac.gsl.database.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;


/**
 * Main entry point into the @MACRO.NAME@ repository
 *
 * See the CHANGEME 
 */
public class @MACRO.PREFIX@Repository extends GsacRepository implements GsacConstants {

    /**
     * ctor
     */
    public @MACRO.PREFIX@Repository() {
    }


    /** CHANGEME
        Create the resource managers
        Override this to make your own set of resource managers
    */
    public void initResourceManagers() {
        //If you only want sites or files then don't call this:
        super.initResourceManagers();

        //But do call one of these:
        //        getResourceManager(GsacSite.CLASS_SITE);
        //        getResourceManager(GsacFile.CLASS_FILE);
    }



    /**
     * Factory method to create the database manager
     *
     * @return database manager
     *
     * @throws Exception on badness 
     */
    public GsacDatabaseManager doMakeDatabaseManager() throws Exception {
        @MACRO.PREFIX@DatabaseManager dbm = new @MACRO.PREFIX@DatabaseManager(this);
        dbm.init();
        return dbm;
    }


    /**
     * Factory method to create the resource manager that manages the given ResourceClass
     *
     * @return resource manager
     */
    public GsacResourceManager doMakeResourceManager(ResourceClass type) {
        if(type.equals(GsacSite.CLASS_SITE)) {
            return new @MACRO.PREFIX@SiteManager(this);
        }
        if(type.equals(GsacFile.CLASS_FILE)) {
            return new @MACRO.PREFIX@FileManager(this);
        }
        return null;
    }


    /**
     * get the html header. This just uses the base class' method which
     * will read the resources/header.html in this package. So, just edit that file
     * to define your own html header
     *
     * @param request the request
     *
     * @return html header
     */
    public String getHtmlHeader(GsacRequest request) {
        return super.getHtmlHeader(request);
    }


    /**
     * get the html footer. This just uses the base class' method which
     * will read the resources/footer.html in this package. So, just edit that file
     * to define your own html footer
     *
     * @param request the request
     *
     * @return html footer
     */
    public String getHtmlFooter(GsacRequest request) {
        return super.getHtmlFooter(request);
    }

}
