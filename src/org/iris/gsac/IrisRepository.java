/*
 * 
 */

package org.iris.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;


/**
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public class IrisRepository extends GsacRepository implements GsacConstants {

    /**
     * ctor
     */
    public IrisRepository() {
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


    public String getExternalVocabulary(String type) {
        if(type.equals(ARG_SITE_GROUP)) return "";
        return super.getExternalVocabulary(type);
    }


    /**
     * Factory method to create the SiteManager
     *
     * @return site manager
     */
    public GsacResourceManager doMakeResourceManager(ResourceClass type) {
        if(type.equals(GsacSite.CLASS_SITE)) {
            return new IrisSiteManager(this);
        }
        if(type.equals(GsacFile.CLASS_FILE)) {
            return new IrisFileManager(this);
        }
        return null;
    }

    public void initResourceManagers() {
        //Override this to make your own set of resource managers
        //        super.initResourceManagers();
        getResourceManager(GsacSite.CLASS_SITE);
        //        getResourceManager(GsacFile.CLASS_FILE);
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
