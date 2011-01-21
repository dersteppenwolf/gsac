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

package org.gsac.gsl.metadata;


import org.gsac.gsl.util.*;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class MetadataGroup extends GsacMetadata {

    /** _more_ */
    public static final String DISPLAY_LIST = "list";

    /** _more_ */
    public static final String DISPLAY_TABS = "tabs";

    /** _more_ */
    public static final String DISPLAY_FORMTABLE = "formtable";

    /** _more_ */
    public static final String DISPLAY_TABLE = "table";

    /** _more_ */
    private List<GsacMetadata> metadataList = new ArrayList<GsacMetadata>();


    /** _more_ */
    private String displayType = DISPLAY_LIST;


    /**
     * _more_
     */
    public MetadataGroup() {}

    /**
     * _more_
     *
     * @param label _more_
     */
    public MetadataGroup(String label) {
        super(label);
    }


    /**
     * _more_
     *
     * @param label _more_
     * @param displayType _more_
     */
    public MetadataGroup(String label, String displayType) {
        super(label);
        this.displayType = displayType;
    }

    /**
     * _more_
     *
     * @param metadata _more_
     */
    public void addMetadata(GsacMetadata metadata) {
        add(metadata);
    }

    /**
     * _more_
     *
     * @param metadata _more_
     */
    public void add(GsacMetadata metadata) {
        metadataList.add(metadata);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<GsacMetadata> getMetadata() {
        return metadataList;
    }

    /**
     * _more_
     *
     * @param metadata _more_
     */
    public void setMetadata(List<GsacMetadata> metadata) {
        metadataList = metadata;
    }

    /**
     *  Set the DislayType property.
     *
     *  @param value The new value for DisplayType
     */
    public void setDisplayType(String value) {
        displayType = value;
    }

    /**
     *  Get the DisplayType property.
     *
     *  @return The DisplayType
     */
    public String getDisplayType() {
        return displayType;
    }

}
