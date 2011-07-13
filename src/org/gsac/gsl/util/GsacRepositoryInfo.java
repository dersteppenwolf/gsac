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

package org.gsac.gsl.util;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;

import java.io.PrintWriter;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;



/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class GsacRepositoryInfo {

    /** _more_ */
    private String url;

    /** _more_ */
    private String name;

    /** _more_ */
    private String description = "";

    /** _more_ */
    private String icon;

    /** _more_ */
    private List<CapabilityCollection> collections =
        new ArrayList<CapabilityCollection>();

    /** _more_ */
    private int errorCnt = 0;

    /** _more_ */
    private int openRequestsCnt = 0;

    /** _more_ */
    private Object REQUEST_MUTEX = new Object();

    /**
     * _more_
     */
    public GsacRepositoryInfo() {}

    /**
     * _more_
     *
     * @param url _more_
     */
    public GsacRepositoryInfo(String url) {
        this(url, url);
    }

    /**
     * _more_
     *
     * @param url _more_
     * @param name _more_
     */
    public GsacRepositoryInfo(String url, String name) {
        this(url, name, null);
    }

    /**
     * _more_
     *
     * @param url _more_
     * @param name _more_
     * @param icon _more_
     */
    public GsacRepositoryInfo(String url, String name, String icon) {
        this.url  = url;
        this.name = name;
        this.icon = icon;
    }

    /**
     * _more_
     *
     * @param pw _more_
     */
    public void printDescription(PrintWriter pw) {
        pw.println("name: " + name);
        pw.println("url: " + url);
        pw.println(description);
        for (CapabilityCollection collection : collections) {
            collection.printDescription(pw);
        }
    }

    /**
     * _more_
     *
     * @param collection _more_
     */
    public void addCollection(CapabilityCollection collection) {
        collections.add(collection);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getErrorCount() {
        return errorCnt;
    }

    /**
     * _more_
     */
    public void resetErrorCount() {
        errorCnt = 0;
    }

    /**
     * _more_
     */
    public void incrementErrorCount() {
        errorCnt++;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getOpenRequestsCount() {
        return openRequestsCnt;
    }

    /**
     * _more_
     */
    public void incrementOpenRequestsCount() {
        synchronized (REQUEST_MUTEX) {
            openRequestsCnt++;
        }
    }

    /**
     * _more_
     */
    public void decrementOpenRequestsCount() {
        synchronized (REQUEST_MUTEX) {
            openRequestsCnt--;
        }
    }

    /**
     * _more_
     *
     * @param that _more_
     */
    public void initWith(GsacRepositoryInfo that) {
        if ((that.name != null) && (that.name.length() > 0)) {
            this.name = that.name;
        }
        if ((that.description != null) && (that.description.length() > 0)) {
            this.description = that.description;
        }
        if ((that.icon != null) && (that.icon.length() > 0)) {
            this.icon = that.icon;
        }
        this.collections = that.collections;

    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return url + " " + name;
    }

    /**
     * _more_
     *
     * @param object _more_
     *
     * @return _more_
     */
    public boolean equals(Object object) {
        if ( !(object instanceof GsacRepositoryInfo)) {
            return false;
        }
        GsacRepositoryInfo that = (GsacRepositoryInfo) object;
        return this.url.equals(that.url);
    }


    /**
     * _more_
     *
     * @param myList _more_
     * @param values _more_
     *
     * @return _more_
     */
    public boolean hasEntries(List myList, List values) {
        if ((myList == null) || (myList.size() == 0)) {
            return false;
        }
        for (String value : (List<String>) values) {
            if (IdLabel.contains((List<IdLabel>) myList, value)) {
                return true;
            }
        }
        return false;
    }


    /**
     *  Set the Collections property.
     *
     *  @param value The new value for Collections
     */
    public void setCollections(List<CapabilityCollection> value) {
        collections = value;
    }

    /**
     *  Get the Collections property.
     *
     *  @return The Collections
     */
    public List<CapabilityCollection> getCollections() {
        return collections;
    }


    /**
     * _more_
     *
     * @param capabilityId _more_
     *
     * @return _more_
     */
    public Capability getCapability(String capabilityId) {
        for (CapabilityCollection collection : collections) {
            for (Capability capability : collection.getCapabilities()) {
                if (capability.getId().equals(capabilityId)) {
                    return capability;
                }
            }
        }
        return null;
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public CapabilityCollection getCollection(ResourceClass resourceClass) {
        for (CapabilityCollection collection : collections) {
            if (collection.getResourceClass().equals(resourceClass)) {
                return collection;
            }
        }
        return null;
    }



    /**
     * _more_
     *
     *
     * @param collectionId _more_
     * @param capability _more_
     *
     * @return _more_
     */
    public boolean isCapabilityUsed(ResourceClass resourceClass,
                                    Capability capability) {
        CapabilityCollection collection = getCollection(resourceClass);
        if (collection == null) {
            return false;
        }
        return collection.isCapabilityUsed(capability);
    }

    /**
     * Set the Url property.
     *
     * @param value The new value for Url
     */
    public void setUrl(String value) {
        url = value;
    }

    /**
     * Get the Url property.
     *
     * @return The Url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the Name property.
     *
     * @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Get the Name property.
     *
     * @return The Name
     */
    public String getName() {
        return name;
    }


    /**
     * Set the Description property.
     *
     * @param value The new value for Description
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     * Get the Description property.
     *
     * @return The Description
     */
    public String getDescription() {
        return description;
    }


    /**
     * Set the Icon property.
     *
     * @param value The new value for Icon
     */
    public void setIcon(String value) {
        icon = value;
    }

    /**
     * Get the Icon property.
     *
     * @return The Icon
     */
    public String getIcon() {
        return icon;
    }


}
