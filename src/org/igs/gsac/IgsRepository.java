/*
 * Copyright 2012 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

package org.igs.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.database.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.util.*;


/*

"Conforming GSAC Geodesy Repositories" and Standard GSAC Geodesy Parameters  for GPS/GNSS.

Proposed Standard Parameters for GPS/GNSS (sites):
SKW Dec. 5, 2012.

four character ID               ABF3
sitename (long name)                Aberdeen, Fairbanks, unit 3
latitude, north +, in decimal degrees:      64.7749
longitude, east +, in decimal degrees:      -141.9234
receiver type                   ASHTECH UZ-12
receiver firmware version           CQ00
receiver SN                 UC 220 
receiver date time installed            
receiver date time removed
antenna type                    LEIAT504GG  LEIS
antenna serial number               20045
antenna date installed      
antenna date removed        

Proposed Recommended Parameters (sites):

ellipsoidal height, m           31.24 (not elevation above a geoid)
date site installed         1997-04-27  00:00:00
x Coordinate, m      1192672.04
y Coordinate, m     -2450887.66
z Coordinate, m         -5747096.03
TRF or datum name           ITRFYY, WGS 84
ellipsoid name          GRS 80,  WGS 84
antenna offset up, m        0.0083
antenna offset north, m     0.0000
antenna offset east, m      0.0000
iersDOMESNumber     33302M001
Radome type         SCIS
Radome serial number           
 (end of lists)

What makes a GSAC geodesy implementation conforming is that it supports the standard GSAC geodesy parameters.  The database which GSAC reads as part of repository services needs to have fields corresponding to the standard parameters.  These parameters are used in GSAC queries, and / or their values appear in results of GSAC queries.  All standard parameters are required for a Conforming GSAC Repository.   

To implement a conforming GSAC repository, you supply a list of the field names from your database for the standard parameters.  The GSAC standard parameters include, for example, 4-character ID, name, latitude, longitude, and for a geodesy data file for download, time range or span of the observations.

GSAC will also automatically implement some or all of the recommended GSAC geodesy parameters, if you can supply data for them. Installation instructions will tell exactly how to tell GSAC code to read the standard and recommended parameters which are available in your database.
*/

/**
 * Main entry point into the IGS GSAC Repository repository
 *
 * versions: original october 2012, SKW.
 *
 * See the CHANGEME
 */
public class IgsRepository extends GsacRepository implements GsacConstants {

    /**
     * ctor
     */
    public IgsRepository() {}


    /**
     * CHANGEME
     *   Create the resource managers; ie search on sites, files, or both.
     *   Override this to make your own set of resource managers
     */
    public void initResourceManagers() {
        // Use this line to search on BOTH sites and data files in the metadata.
        //super.initResourceManagers();

        // If you only want a query for either sites or files but not both, uncomment one of these
        getResourceManager(GsacSite.CLASS_SITE);
        //  getResourceManager(GsacFile.CLASS_FILE);
    }



    /**
     * Factory method to create the database manager
     *
     * @return database manager
     *
     * @throws Exception on badness
     */
    public GsacDatabaseManager doMakeDatabaseManager() throws Exception {
        IgsDatabaseManager dbm = new IgsDatabaseManager(this);
        dbm.init();

        return dbm;
    }


    /**
     * Factory method to create the resource manager that manages the given ResourceClass
     *
     *
     * @param type _more_
     * @return resource manager
     */
    public GsacResourceManager doMakeResourceManager(ResourceClass type) {
        if (type.equals(GsacSite.CLASS_SITE)) {
            return new IgsSiteManager(this);
        }
        if (type.equals(GsacFile.CLASS_FILE)) {
            return new IgsFileManager(this);
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
