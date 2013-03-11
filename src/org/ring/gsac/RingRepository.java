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
import org.gsac.gsl.database.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;


/*
 * Main entry point into the IGS GSAC Repository repository
 *
 * versions: original 11 Mar 2013 SKW.
 *
 */
public class RingRepository extends GsacRepository implements GsacConstants {

    /**
     * ctor
     */
    public RingRepository() {}


    /**
     * CHANGEME done for ring ingv
     *   Create the resource managers; ie search on sites, files, or both.
     *
     *   Override this to make your own set of resource managers
     */
    public void initResourceManagers() {
        // Use this line to search on BOTH sites and data files in the metadata.
        //super.initResourceManagers();

        // If you only want a query for either sites or files but not both, uncomment one of these

        getResourceManager(GsacSite.CLASS_SITE);

        // getResourceManager(GsacFile.CLASS_FILE);
    }



    /**
     * Factory method to create the database manager
     *
     * @return database manager
     *
     * @throws Exception on badness
     */
    public GsacDatabaseManager doMakeDatabaseManager() throws Exception {
        RingDatabaseManager dbm = new RingDatabaseManager(this);
        dbm.init();

        return dbm;
    }


    /**
     * Factory method to create the resource manager that manages the given ResourceClass
     *
     * @param type _more_
     * @return resource manager
     */
    public GsacResourceManager doMakeResourceManager(ResourceClass type) {
        if (type.equals(GsacSite.CLASS_SITE)) {
            return new RingSiteManager(this);
        }
        if (type.equals(GsacFile.CLASS_FILE)) {
            return new RingFileManager(this);
        }
        return null;
    }


    /**
     * get the html header. This just uses the base class' method which
     * will read the resources/header.html in this package. So, just edit that file
     * to define your own html header
     * see resources/header.html file
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
     * see resources/footer.html file
     *
     * @param request the request
     *
     * @return html footer
     */
    public String getHtmlFooter(GsacRequest request) {
        return super.getHtmlFooter(request);
    }

}
