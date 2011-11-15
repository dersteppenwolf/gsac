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

package org.gsac.gsl.metadata.gnss;


import org.gsac.gsl.metadata.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.XmlSiteLog;


import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.net.URL;
import java.io.*;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class StreamMetadata extends GsacMetadata {


    public static final String TYPE_PUBLISHED = "published";
    public static final String TYPE_SITE = "site";


    /** _more_ */
    private String url;

    private String type = TYPE_PUBLISHED;


    /** _more_ */
    private String format;

    /** _more_ */
    private int bitRate;



    /**
     * _more_
     */
    public StreamMetadata() {
        //        super(TYPE_NTRIP);
    }

    public StreamMetadata(String type) {
        super(type);
    }

    public void encode(PrintWriter pw, GsacOutputHandler outputHandler, String type) throws Exception {
            String mainTag = XmlSiteLog.TAG_REALTIME_PUBLISHEDSTREAM;
            if(this.getType().equals(StreamMetadata.TYPE_SITE)) {
                mainTag = XmlSiteLog.TAG_REALTIME_SITESTREAM;
            }

            pw.append(XmlUtil.openTag(mainTag));

            String ntripUrl = this.getUrl();
            if(ntripUrl.indexOf(":")<0) {
                ntripUrl = "http://" + ntripUrl;
            }
            try {
                URL url = new URL(ntripUrl);
                pw.append(tag(XmlSiteLog.TAG_REALTIME_IPADDRESS, url.getHost()));
                pw.append(tag(XmlSiteLog.TAG_REALTIME_PORT, "" + url.getPort()));
            } catch(java.net.MalformedURLException mue) {
                pw.append(tag(XmlSiteLog.TAG_REALTIME_IPADDRESS, "Bad url: " + ntripUrl));
            }
            pw.append(tag(XmlSiteLog.TAG_REALTIME_SAMPINTERVAL,
                          "" + this.getBitRate()));
            pw.append(tag(XmlSiteLog.TAG_REALTIME_DATAFORMAT,

                          this.getFormat()));

            encodeInner(pw, outputHandler, type);
            pw.append(
                      XmlUtil.closeTag(mainTag));
    }


    /**
     * _more_
     *
     * @param tag _more_
     * @param contents _more_
     *
     * @return _more_
     */
    public static String tag(String tag, String contents) {
        return XmlUtil.tag(tag, "", contents);
    }


    public void encodeInner(PrintWriter pw, GsacOutputHandler outputHandler, String type) throws Exception {}



    /**
     * _more_
     *
     * @param metadata _more_
     *
     * @return _more_
     */
    public static List<StreamMetadata> getStreamMetadata(
            List<GsacMetadata> metadata) {
        return (List<StreamMetadata>) findMetadata(metadata,
                StreamMetadata.class);
    }

    /**
       Set the Url property.

       @param value The new value for Url
    **/
    public void setUrl (String value) {
	url = value;
    }

    /**
       Get the Url property.

       @return The Url
    **/
    public String getUrl () {
	return url;
    }


/**
Set the Type property.

@param value The new value for Type
**/
public void setType (String value) {
	type = value;
}

/**
Get the Type property.

@return The Type
**/
public String getType () {
	return type;
}

    /**
     * Set the Format property.
     *
     * @param value The new value for Format
     */
    public void setFormat(String value) {
        format = value;
    }

    /**
     * Get the Format property.
     *
     * @return The Format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Set the BitRate property.
     *
     * @param value The new value for BitRate
     */
    public void setBitRate(int value) {
        bitRate = value;
    }

    /**
     * Get the BitRate property.
     *
     * @return The BitRate
     */
    public int getBitRate() {
        return bitRate;
    }





}