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
public abstract class GsacResource {

    /** site type */
    private ResourceType type;


    /** _more_ */
    private String id;


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
    public abstract String getViewUrl();

    /**
     * _more_
     *
     * @return _more_
     */
    public abstract String getIdArg();


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



}
