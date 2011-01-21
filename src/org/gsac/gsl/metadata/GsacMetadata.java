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
 * Generic metadata  class
 *
 */
public class GsacMetadata {

    /** _more_ */
    public static final String TYPE_IMAGE = "imageurl";


    /** _more_ */
    public static final String TYPE_ICON = "icon";

    /** _more_ */
    public static final String TYPE_LINK = "link";

    /** _more_ */
    public static final String TYPE_PROPERTY = "property";

    /** _more_ */
    private String type;

    /** the label */
    private String label;



    /**
     * _more_
     */
    public GsacMetadata() {}


    /**
     * _more_
     *
     * @param type _more_
     *
     * @param label _more_
     */
    public GsacMetadata(String label) {
        this.label = label;
    }

    /**
     * _more_
     *
     * @param type _more_
     * @param label _more_
     */
    public GsacMetadata(String type, String label) {
        this.type  = type;
        this.label = label;
    }


    /**
     * _more_
     *
     * @param metadataList _more_
     * @param c _more_
     *
     * @return _more_
     */
    public static List findMetadata(List<GsacMetadata> metadataList,
                                    Class c) {
        List<GsacMetadata> result = new ArrayList<GsacMetadata>();
        findMetadata(metadataList, c, result);
        return result;
    }


    /**
     * _more_
     *
     * @param metadataList _more_
     * @param c _more_
     * @param result _more_
     */
    public static void findMetadata(List<GsacMetadata> metadataList, Class c,
                                    List<GsacMetadata> result) {
        for (GsacMetadata metadata : metadataList) {
            if (metadata instanceof MetadataGroup) {
                MetadataGroup group = (MetadataGroup) metadata;
                group.findMetadata(group.getMetadata(), c, result);
                continue;
            }
            if (metadata.getClass().equals(c)) {
                result.add(metadata);
            }
        }
    }

    /**
     * _more_
     *
     * @param metadataList _more_
     * @param type _more_
     *
     * @return _more_
     */
    public static List getMetadataByType(List<GsacMetadata> metadataList,
                                         String type) {
        List<GsacMetadata> result = new ArrayList<GsacMetadata>();
        for (GsacMetadata metadata : metadataList) {
            if (metadata.getType().equals(type)) {
                result.add(metadata);
            }
        }
        return result;
    }

    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(String value) {
        type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public String getType() {
        return type;
    }

    /**
     *  Set the Label property.
     *
     *  @param value The new value for Label
     */
    public void setLabel(String value) {
        label = value;
    }

    /**
     *  Get the Label property.
     *
     *  @return The Label
     */
    public String getLabel() {
        return label;
    }




}
