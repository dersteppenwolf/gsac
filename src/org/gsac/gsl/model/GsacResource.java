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


import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
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
public abstract class GsacResource implements GsacArgs, GsacConstants {

    /** _more_ */
    private String id;

    /** site type */
    private ResourceType type;

    /** _more_ */
    private String shortName;

    /** _more_ */
    private String longName;

    /** _more_ */
    private List<GsacMetadata> metadata = new ArrayList();

    /** _more_ */
    private int metadataLevel = 0;

    /** _more_ */
    private GsacRepositoryInfo repositoryInfo;

    /** The groups this site is part of */
    private List<ResourceGroup> resourceGroups = new ArrayList();

    /** site status */
    private ResourceStatus status;

    /** _more_ */
    private List<GsacResource> relatedResources =
        new ArrayList<GsacResource>();

    /** _more_ */
    private EarthLocation earthLocation;

    /** _more_ */
    private Date fromDate;

    /** _more_ */
    private Date toDate;

    /** _more_ */
    private Date publishTime;




    /**
     * ctor
     */
    public GsacResource() {}

    /**
     * _more_
     *
     * @param id _more_
     */
    public GsacResource(String id) {
        this.id = id;
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param type _more_
     */
    public GsacResource(String id, ResourceType type) {
        this(id);
        this.type = type;
    }


    /**
     * _more_
     *
     * @param id _more_
     * @param type _more_
     * @param shortName _more_
     * @param longName _more_
     */
    public GsacResource(String id, ResourceType type, String shortName,
                        String longName) {
        this.id        = id;
        this.type      = type;
        this.shortName = shortName;
        this.longName  = longName;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public abstract ResourceClass getResourceClass();

    /**
     * _more_
     *
     * @param finder _more_
     *
     * @return _more_
     */
    public List<GsacMetadata> findMetadata(
            GsacMetadata.MetadataFinder finder) {
        List<GsacMetadata> result = new ArrayList<GsacMetadata>();
        for (GsacMetadata child : metadata) {
            child.findMetadata(result, finder);
        }
        return result;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getViewUrl() {
        return URL_BASE + "/" + getResourceClass().getName()
               + URL_SUFFIX_VIEW;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getIdArg() {
        return getResourceClass().getName() + "." + ARG_SUFFIX_ID;
    }


    /**
     * Set the Id property.
     *
     * @param value The new value for Id
     */
    public void setId(String value) {
        id = value;
    }

    /**
     * Get the Id property.
     *
     * @return The Id
     */
    public String getId() {
        return id;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        return shortName;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getLongLabel() {
        return getLabel();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getMetadataLevel() {
        return metadataLevel;
    }

    /**
     * _more_
     *
     * @param level _more_
     */
    public void setMetadataLevel(int level) {
        metadataLevel = level;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacMetadata> getMetadata() {
        return metadata;
    }


    /**
     * _more_
     *
     * @param newMetadata _more_
     */
    public void setMetadata(List<GsacMetadata> newMetadata) {
        metadata = newMetadata;
    }

    /**
     * _more_
     *
     * @param m _more_
     */
    public void addMetadata(GsacMetadata m) {
        metadata.add(m);
    }


    /**
     *  Set the RepositoryInfo property.
     *
     *  @param value The new value for RepositoryInfo
     */
    public void setRepositoryInfo(GsacRepositoryInfo value) {
        repositoryInfo = value;
    }

    /**
     *  Get the RepositoryInfo property.
     *
     *  @return The RepositoryInfo
     */
    public GsacRepositoryInfo getRepositoryInfo() {
        return repositoryInfo;
    }


    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(ResourceType value) {
        type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public ResourceType getType() {
        return type;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<ResourceGroup> getResourceGroups() {
        return resourceGroups;
    }

    /**
     * _more_
     *
     * @param groups _more_
     */
    public void setResourceGroups(List<ResourceGroup> groups) {
        resourceGroups = groups;
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public boolean hasGroup(String id) {
        return hasGroup(new ResourceGroup(id));
    }

    /**
     * _more_
     *
     * @param group _more_
     *
     * @return _more_
     */
    public boolean hasGroup(ResourceGroup group) {
        return resourceGroups.contains(group);
    }

    /**
     * _more_
     *
     * @param group _more_
     */
    public void addResourceGroup(ResourceGroup group) {
        resourceGroups.add(group);
    }


    /**
     *  Set the Status property.
     *
     *  @param value The new value for Status
     */
    public void setStatus(ResourceStatus value) {
        status = value;
    }

    /**
     *  Get the Status property.
     *
     *  @return The Status
     */
    public ResourceStatus getStatus() {
        return status;
    }


    /**
     *  Set the RelatedResources property.
     *
     *  @param value The new value for RelatedResources
     */
    public void setRelatedResources(List<GsacResource> value) {
        relatedResources = value;
    }

    /**
     *  Get the RelatedResources property.
     *
     *  @return The RelatedResources
     */
    public List<GsacResource> getRelatedResources() {
        return relatedResources;
    }

    /**
     * _more_
     *
     * @param resource _more_
     */
    public void addRelatedResource(GsacResource resource) {
        relatedResources.add(resource);
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
     * @return _more_
     */
    public boolean hasEarthLocation() {
        return getEarthLocation() != null;
    }


    /**
     * _more_
     *
     * @param makeNewIfNeeded _more_
     *
     * @return _more_
     */
    public EarthLocation getEarthLocation(boolean makeNewIfNeeded) {
        EarthLocation location = earthLocation;
        if (location == null) {
            if (makeNewIfNeeded) {
                earthLocation = location = new EarthLocation();
            }
        }
        if (location == null) {
            for (GsacResource relatedResource : relatedResources) {
                location = relatedResource.getEarthLocation();
                if (location != null) {
                    break;
                }
            }
        }
        return location;
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
     *  Set the PublishTime property.
     *
     *  @param value The new value for PublishTime
     */
    public void setPublishTime(Date value) {
        publishTime = value;
    }

    /**
     *  Get the PublishTime property.
     *
     *  @return The PublishTime
     */
    public Date getPublishTime() {
        return publishTime;
    }



    /**
     *  Set the ShortName property.
     *
     *  @param value The new value for ShortName
     */
    public void setShortName(String value) {
        shortName = value;
    }

    /**
     *  Get the ShortName property.
     *
     *  @return The ShortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     *  Set the LongName property.
     *
     *  @param value The new value for LongName
     */
    public void setLongName(String value) {
        longName = value;
    }

    /**
     *  Get the LongName property.
     *
     *  @return The LongName
     */
    public String getLongName() {
        return longName;
    }



}
