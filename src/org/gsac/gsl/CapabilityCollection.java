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

package org.gsac.gsl;


import org.gsac.gsl.model.ResourceClass;


import org.gsac.gsl.util.*;

import ucar.unidata.xml.XmlUtil;

import java.io.PrintWriter;

import java.net.URL;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;



/**
 * Defines a query capability. Takes an id, query type (e.g., enumeration, boolean, string), a label, etc.
 */
public class CapabilityCollection {

    /** _more_ */
    public static final String TAG_CAPABILITIES = "capabilities";

    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_URL = "url";

    /** _more_ */
    private String name;

    /** _more_ */
    private ResourceClass resourceClass;

    /** _more_ */
    private String url;

    /** _more_ */
    private List<Capability> capabilities;


    /** _more_ */
    private HashSet<String> used;

    /**
     * _more_
     */
    public CapabilityCollection() {}


    /**
     * _more_
     *
     * @param resourceClass _more_
     * @param name _more_
     * @param url _more_
     * @param capabilities _more_
     */
    public CapabilityCollection(ResourceClass resourceClass, String name,
                                String url, List<Capability> capabilities) {
        this.resourceClass = resourceClass;
        this.name          = name;
        this.url           = url;
        setCapabilities(capabilities);
        this.used = getUsedSet(capabilities);
    }

    /**
     * _more_
     *
     * @param pw _more_
     */
    public void printDescription(PrintWriter pw) {
        pw.println("name: " + name);
        pw.println("url: " + url);
        for (Capability capability : capabilities) {
            pw.print("\t");
            capability.printDescription(pw);
        }
    }

    /**
     * _more_
     *
     * @param sb _more_
     *
     * @throws Exception On badness
     */
    public void toXml(Appendable sb) throws Exception {
        sb.append(XmlUtil.openTag(TAG_CAPABILITIES,
                                  XmlUtil.attrs(ATTR_NAME, name, ATTR_ID,
                                      resourceClass.getName(), ATTR_URL,
                                      url)));
        for (Capability capability : capabilities) {
            capability.toXml(sb);
        }
        sb.append(XmlUtil.closeTag(TAG_CAPABILITIES));
    }



    /**
     * _more_
     *
     * @param capability _more_
     *
     * @return _more_
     */
    public boolean isCapabilityUsed(Capability capability) {
        if (used == null) {
            used = getUsedSet(capabilities);
        }
        return used.contains(capability.getId());
    }

    /**
     * _more_
     *
     * @param capabilities _more_
     *
     * @return _more_
     */
    private HashSet getUsedSet(List<Capability> capabilities) {
        HashSet used = new HashSet<String>();
        if (capabilities == null) {
            return used;
        }
        for (Capability capability : capabilities) {
            used.add(capability.getId());
        }
        return used;
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
     * Set the ResourceClass property.
     *
     * @param value The new value for ResourceClass
     */
    public void setResourceClass(ResourceClass value) {
        resourceClass = value;
    }

    /**
     * Get the ResourceClass property.
     *
     * @return The ResourceClass
     */
    public ResourceClass getResourceClass() {
        return resourceClass;
    }



    /**
     *  Set the Url property.
     *
     *  @param value The new value for Url
     */
    public void setUrl(String value) {
        url = value;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public String getRelativeUrl() throws Exception {
        URL tmp = new URL(url);
        return tmp.getPath();
    }

    /**
     *  Get the Url property.
     *
     *  @return The Url
     */
    public String getUrl() {
        return url;
    }

    /**
     *  Set the Capabilities property.
     *
     *  @param value The new value for Capabilities
     */
    public void setCapabilities(List<Capability> value) {
        capabilities = value;
        if (capabilities != null) {
            for (Capability capability : capabilities) {
                capability.setCollection(this);
            }
        }

    }

    /**
     *  Get the Capabilities property.
     *
     *  @return The Capabilities
     */
    public List<Capability> getCapabilities() {
        return capabilities;
    }


}
