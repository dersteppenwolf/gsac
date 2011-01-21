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
public class GsacAttrMetadata extends GsacMetadata {


    /** attribute */
    private String attr1;

    /** attribute */
    private String attr2;

    /** attribute */
    private String attr3;

    /** attribute */
    private String attr4;

    /**
     * _more_
     */
    public GsacAttrMetadata() {}

    /**
     * _more_
     *
     * @param type _more_
     */
    public GsacAttrMetadata(String type) {
        super(type);
    }

    /**
     * _more_
     *
     * @param metadataList _more_
     *
     * @return _more_
     */
    public static List<GsacAttrMetadata> findMetadata(
            List<GsacMetadata> metadataList) {
        return (List<GsacAttrMetadata>) GsacMetadata.findMetadata(
            metadataList, GsacAttrMetadata.class);
    }


    /**
     *  Set the Attr1 property.
     *
     *  @param value The new value for Attr1
     */
    public void setAttr1(String value) {
        attr1 = value;
    }

    /**
     *  Get the Attr1 property.
     *
     *  @return The Attr1
     */
    public String getAttr1() {
        return attr1;
    }

    /**
     *  Set the Attr2 property.
     *
     *  @param value The new value for Attr2
     */
    public void setAttr2(String value) {
        attr2 = value;
    }

    /**
     *  Get the Attr2 property.
     *
     *  @return The Attr2
     */
    public String getAttr2() {
        return attr2;
    }

    /**
     *  Set the Attr3 property.
     *
     *  @param value The new value for Attr3
     */
    public void setAttr3(String value) {
        attr3 = value;
    }

    /**
     *  Get the Attr3 property.
     *
     *  @return The Attr3
     */
    public String getAttr3() {
        return attr3;
    }

    /**
     *  Set the Attr4 property.
     *
     *  @param value The new value for Attr4
     */
    public void setAttr4(String value) {
        attr4 = value;
    }

    /**
     *  Get the Attr4 property.
     *
     *  @return The Attr4
     */
    public String getAttr4() {
        return attr4;
    }



}
