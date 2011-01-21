/*
 * 
 */

package @MACRO.PACKAGE@;


import org.gsac.gsl.*;
import org.gsac.gsl.database.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;


/**
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public class @MACRO.PREFIX@Repository extends GsacRepositoryImpl implements GsacConstants {

    /**
     * ctor
     */
    public @MACRO.PREFIX@Repository() {
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
     * Factory method to create the SiteManager
     *
     * @return site manager
     */
    public SiteManager doMakeSiteManager() {
        return new @MACRO.PREFIX@SiteManager(this);
    }


    /**
     * Factory method to create the ResourceManager
     *
     * @return resource manager
     */
    public ResourceManager doMakeResourceManager() {
        return new @MACRO.PREFIX@ResourceManager(this);
    }


    /*
     * CHANGEME Is this repository capable of certain things.
     * By default the base repository does  a properties look up.
     * See resources/gsac.properties
     */
    public boolean isCapable(String arg) {
        return super.isCapable(arg);
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
