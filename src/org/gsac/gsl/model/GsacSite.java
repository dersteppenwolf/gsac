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

    /** _more_          */
    public static final ResourceClass TYPE_SITE = new ResourceClass("site");

    /** This is the site identifier that users are used to. e.g., P123 */
    private String siteCode;

    /** This can be used for a monument ID */
    private String secondarySiteCode;

    /** site type */
    private SiteType type;

    /** site status */
    private SiteStatus status;

    /** site name */
    private String name;

    /** _more_ */
    private EarthLocation earthLocation;

    /** _more_ */
    private PoliticalLocation politicalLocation;

    /** _more_ */
    private Date fromDate;

    /** _more_ */
    private Date toDate;

    /** The groups this site is part of */
    private List<SiteGroup> siteGroups = new ArrayList();


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
                    SiteType type, EarthLocation location) {
        super(siteId);
        this.type          = type;
        this.siteCode      = siteCode;
        this.name          = name;
        this.earthLocation = location;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public ResourceClass getResourceClass() {
        return TYPE_SITE;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getViewUrl() {
        return GsacConstants.URL_SITE_VIEW;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getIdArg() {
        return GsacArgs.ARG_SITE_ID;
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
     * _more_
     *
     * @return _more_
     */
    public List<SiteGroup> getSiteGroups() {
        return siteGroups;
    }

    /**
     * _more_
     *
     * @param groups _more_
     */
    public void setSiteGroups(List<SiteGroup> groups) {
        siteGroups = groups;
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public boolean hasGroup(String id) {
        return hasGroup(new SiteGroup(id));
    }

    /**
     * _more_
     *
     * @param group _more_
     *
     * @return _more_
     */
    public boolean hasGroup(SiteGroup group) {
        return siteGroups.contains(group);
    }

    /**
     * _more_
     *
     * @param group _more_
     */
    public void addSiteGroup(SiteGroup group) {
        siteGroups.add(group);
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
     *  Set the EarthLocation property.
     *
     *  @param value The new value for EarthLocation
     */
    public void setEarthLocation(EarthLocation value) {
        earthLocation = value;
    }

    /**
     *  Get the EarthLocation property.
     *
     *  @return The EarthLocation
     */
    public EarthLocation getEarthLocation() {
        return getEarthLocation(false);
    }

    /**
     * _more_
     *
     * @param makeNewIfNeeded _more_
     *
     * @return _more_
     */
    public EarthLocation getEarthLocation(boolean makeNewIfNeeded) {
        if (makeNewIfNeeded && (earthLocation == null)) {
            earthLocation = new EarthLocation();
        }
        return earthLocation;
    }




    /**
     *  Set the Latitude property.
     *
     *  @param value The new value for Latitude
     */
    public void setLatitude(double value) {
        getEarthLocation(true).setLatitude(value);
    }

    /**
     *  Get the Latitude property.
     *
     *  @return The Latitude
     */
    public double getLatitude() {
        return getEarthLocation(true).getLatitude();
    }

    /**
     *  Set the Longitude property.
     *
     *  @param value The new value for Longitude
     */
    public void setLongitude(double value) {
        getEarthLocation(true).setLongitude(value);
    }

    /**
     *  Get the Longitude property.
     *
     *  @return The Longitude
     */
    public double getLongitude() {
        return getEarthLocation(true).getLongitude();

    }

    /**
     *  Set the Elevation property.
     *
     *  @param value The new value for Elevation
     */
    public void setElevation(double value) {
        getEarthLocation(true).setElevation(value);
    }

    /**
     *  Get the Elevation property.
     *
     *  @return The Elevation
     */
    public double getElevation() {
        return getEarthLocation(true).getElevation();
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
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(SiteType value) {
        type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public SiteType getType() {
        return type;
    }

    /**
     *  Set the Status property.
     *
     *  @param value The new value for Status
     */
    public void setStatus(SiteStatus value) {
        status = value;
    }

    /**
     *  Get the Status property.
     *
     *  @return The Status
     */
    public SiteStatus getStatus() {
        return status;
    }



    /**
     *  Set the FromDate property.
     *
     *  @param value The new value for FromDate
     */
    public void setFromDate(Date value) {
        fromDate = value;
    }

    /**
     *  Get the FromDate property.
     *
     *  @return The FromDate
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     *  Set the ToDate property.
     *
     *  @param value The new value for ToDate
     */
    public void setToDate(Date value) {
        toDate = value;
    }

    /**
     *  Get the ToDate property.
     *
     *  @return The ToDate
     */
    public Date getToDate() {
        return toDate;
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
