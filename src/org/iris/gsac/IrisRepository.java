/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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
        packageName = packageName.replace(".", "/");
    }


    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public String getExternalVocabulary(String type) {
        if (type.equals(ARG_SITE_GROUP)) {
            return "";
        }
        return super.getExternalVocabulary(type);
    }


    /**
     * Factory method to create the SiteManager
     *
     *
     * @param type _more_
     * @return site manager
     */
    public GsacResourceManager doMakeResourceManager(ResourceClass type) {
        if (type.equals(GsacSite.CLASS_SITE)) {
            return new IrisSiteManager(this);
        }
        if (type.equals(GsacFile.CLASS_FILE)) {
            ucar.unidata.util.Misc.printStack("file");
            return new IrisFileManager(this);
        }
        return null;
    }

    /**
     * _more_
     *
     * @param resourceClass _more_
     */
    public void initOutputHandlers(ResourceClass resourceClass) {
        if (resourceClass.equals(GsacFile.CLASS_FILE)) {
            return;
        }
        super.initOutputHandlers(resourceClass);
    }



    /**
     * _more_
     */
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

    /**
     * _more_
     *
     * @param arg _more_
     *
     * @return _more_
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
