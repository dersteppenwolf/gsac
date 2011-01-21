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
 * Metadata for a url
 *
 */
public class LinkMetadata extends GsacMetadata {

    /** the url */
    private String url;

    /**
     * _more_
     */
    public LinkMetadata() {}

    /**
     * ctor
     *
     * @param url the url
     * @param label the label
     */
    public LinkMetadata(String url, String label) {
        this(TYPE_LINK, url, label);
    }

    /**
     * ctor
     *
     * @param type The type. Normally TYPE_LINK but can be overwritten
     * @param url the url
     * @param label the label
     */
    public LinkMetadata(String type, String url, String label) {
        super(type, label);
        this.url = url;
    }

    /**
     * _more_
     *
     * @param metadata _more_
     *
     * @return _more_
     */
    public static List<LinkMetadata> getMetadata(
            List<GsacMetadata> metadata) {
        return (List<LinkMetadata>) findMetadata(metadata,
                LinkMetadata.class);
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
     *  Get the Url property.
     *
     *  @return The Url
     */
    public String getUrl() {
        return url;
    }




}
