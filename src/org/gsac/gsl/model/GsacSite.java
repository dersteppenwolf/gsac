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

package org.gsac.gsl.model;


import org.gsac.gsl.GsacArgs;


import org.gsac.gsl.GsacConstants;
import org.gsac.gsl.util.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class GsacSite extends GsacResource implements Comparable {

    /** _more_ */
    public static final ResourceClass CLASS_SITE = new ResourceClass("site");

    /**
     * ctor
     */
    public GsacSite() {}

    /**
     * _more_
     *
     * @param siteId _more_
     * @param siteCode _more_
     * @param name _more_
     */
    public GsacSite(String siteId, String siteCode, String name) {
        super(siteId, null, siteCode, name);
    }


    /**
     * ctor
     *
     * @param siteId unique repository specific id
     * @param siteCode site code
     * @param name site name
     * @param latitude location
     * @param longitude location
     * @param elevation location
     */
    public GsacSite(String siteId, String siteCode, String name,
                    double latitude, double longitude, double elevation) {
        this(siteId, siteCode, name, null,
             new EarthLocation(latitude, longitude, elevation));
    }

    /**
     * _more_
     *
     * @param siteId _more_
     * @param siteCode _more_
     * @param name _more_
     * @param type _more_
     * @param location _more_
     */
    public GsacSite(String siteId, String siteCode, String name,
                    ResourceType type, EarthLocation location) {
        super(siteId, type, siteCode, name);
        setEarthLocation(location);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public ResourceClass getResourceClass() {
        return CLASS_SITE;
    }


    /**
     * _more_
     *
     * @param object _more_
     *
     * @return _more_
     */
    public int compareTo(Object object) {
        if ( !(object instanceof GsacSite)) {
            throw new IllegalArgumentException("Cannot compare to:"
                    + object.getClass().getName());
        }
        GsacSite that = (GsacSite) object;
        return this.getShortName().compareTo(that.getShortName());
    }


    /**
     * get the label used to display this site.
     *
     * @return display label
     */
    public String getLabel() {
        return getShortName();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLongLabel() {
        return getLongName() + " " + getLabel();
    }


}
