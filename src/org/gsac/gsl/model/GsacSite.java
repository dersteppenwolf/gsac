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

    /** This is the site identifier that users are used to. e.g., P123 */
    private String siteCode;

    /** This can be used for a monument ID */
    private String secondarySiteCode;


    /** site name */
    private String name;


    /** _more_ */
    private PoliticalLocation politicalLocation;


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
        super(siteId);
        this.siteCode = siteCode;
        this.name     = name;
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
        super(siteId, type);
        this.siteCode = siteCode;
        this.name     = name;
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
        return this.siteCode.compareTo(that.siteCode);
    }




    /**
     *  Get the SiteId property.
     *
     *  @return The SiteId
     */
    public String getSiteId() {
        return super.getId();
    }

    /**
     * get the label used to display this site. It is either the site code
     * concatenated with the secondarySiteCode or just the site code
     *
     * @return display label
     */
    public String getLabel() {
        if (secondarySiteCode != null) {
            return getSiteCode() + " - " + getSecondarySiteCode();
        }
        return getSiteCode();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLongLabel() {
        return getName() + " " + getLabel() + " (" + getSiteCode() + ")";
    }


    /**
     *  Set the SiteCode property.
     *
     *  @param value The new value for SiteCode
     */
    public void setSiteCode(String value) {
        siteCode = value;
    }

    /**
     *  Get the SiteCode property.
     *
     *  @return The SiteCode
     */
    public String getSiteCode() {
        return siteCode;
    }

    /**
     *  Set the Name property.
     *
     *  @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     *  Get the Name property.
     *
     *  @return The Name
     */
    public String getName() {
        return name;
    }



    /**
     *  Set the PoliticalLocation property.
     *
     *  @param value The new value for PoliticalLocation
     */
    public void setPoliticalLocation(PoliticalLocation value) {
        politicalLocation = value;
    }

    /**
     *  Get the PoliticalLocation property.
     *
     *  @return The PoliticalLocation
     */
    public PoliticalLocation getPoliticalLocation() {
        return politicalLocation;
    }




    /**
     *  Set the SecondarySiteCode property.
     *
     *  @param value The new value for SecondarySiteCode
     */
    public void setSecondarySiteCode(String value) {
        secondarySiteCode = value;
    }

    /**
     *  Get the SecondarySiteCode property.
     *
     *  @return The SecondarySiteCode
     */
    public String getSecondarySiteCode() {
        return secondarySiteCode;
    }


}
