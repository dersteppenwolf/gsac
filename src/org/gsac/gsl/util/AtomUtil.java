/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/


package org.gsac.gsl.util;


import ucar.unidata.xml.XmlUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.List;

import java.util.TimeZone;


/**
 * A collection of utilities for atom feeds xml.
 * NOTE: This is copied from the RAMADDA source. I couldn't deal with yet another jar
 *
 */

public class AtomUtil {

    /** _more_ */
    public static final TimeZone TIMEZONE_DEFAULT =
        TimeZone.getTimeZone("UTC");


    /** _more_ */
    public static final SimpleDateFormat atomSdf;

    static {
        atomSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        atomSdf.setTimeZone(TIMEZONE_DEFAULT);
    }

    /** _more_ */
    public static final String XMLNS = "http://www.w3.org/2005/Atom";

    /** _more_ */
    public static final String XMLNS_GEORSS = "http://www.georss.org/georss";



    /** _more_ */
    public static final String REL_SELF = "self";

    /** _more_ */
    public static final String REL_IMAGE = "image";

    /** _more_ */
    public static final String REL_ALTERNATE = "alternate";

    public static final String REL_ESIP_DOCUMENTATION = "http://esipfed.org/ns/discovery/1.1/documentation#";
    public static final String REL_ESIP_BROWSE = "http://esipfed.org/ns/discovery/1.1/browse#";
    public static final String REL_ESIP_METADATA = "http://esipfed.org/ns/discovery/1.1/metadata#";
    public static final String REL_ESIP_DATA = "http://esipfed.org/ns/discovery/1.1/data#";




    public static final String TAG_TIME_START = "time:start";

    public static final String TAG_TIME_END = "time:end";

    /** _more_ */
    public static final String TAG_FEED = "feed";

    /** _more_ */
    public static final String TAG_PUBLISHED = "published";

    /** _more_ */
    public static final String TAG_TITLE = "title";

    /** _more_ */
    public static final String TAG_SUBTITLE = "subtitle";

    /** _more_ */
    public static final String TAG_LINK = "link";

    /** _more_ */
    public static final String TAG_UPDATED = "updated";

    /** _more_ */
    public static final String TAG_AUTHOR = "author";

    /** _more_ */
    public static final String TAG_NAME = "name";

    /** _more_ */
    public static final String TAG_URI = "uri";

    /** _more_ */
    public static final String TAG_ID = "id";

    /** _more_ */
    public static final String TAG_ICON = "icon";

    /** _more_ */
    public static final String TAG_RIGHTS = "rights";

    /** _more_ */
    public static final String TAG_ENTRY = "entry";

    /** _more_ */
    public static final String TAG_SUMMARY = "summary";

    /** _more_ */
    public static final String TAG_CONTENT = "content";


    /** _more_ */
    public static final String ATTR_XMLNS = "xmlns";

    /** _more_ */
    public static final String ATTR_XMLNS_GEORSS = "xmlns:georss";


    /** _more_ */
    public static final String ATTR_HREF = "href";

    /** _more_ */
    public static final String ATTR_REL = "rel";

    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_TITLE = "title";


    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    public static String format(Date date) {
        synchronized (atomSdf) {
            //The sdf produces a time zone that isn't RFC3399 compatible so we just tack on the "Z"
            return atomSdf.format(date) + "Z";
        }
    }

    public static String makeTimeRange(Date date1, Date date2) {
        return XmlUtil.tag(TAG_TIME_START,"",format(date1)) +
            XmlUtil.tag(TAG_TIME_END,"",format(date2));
    }


    /**
     * _more_
     *
     * @param title _more_
     *
     * @return _more_
     */
    public static String makeTitle(String title) {
        return XmlUtil.tag(TAG_TITLE, "", XmlUtil.getCdata(title));
    }


    /**
     * _more_
     *
     * @param href _more_
     *
     * @return _more_
     */
    public static String makeLink(String href) {
        return makeLink(REL_SELF, href);
    }

    /**
     * _more_
     *
     * @param link _more_
     *
     * @return _more_
     */
    public static String makeLink(Link link) {
        StringBuffer attrs = new StringBuffer();
        if(link.mimeType!=null && link.mimeType.length()>0) {
            attrs.append(XmlUtil.attrs(ATTR_TYPE, link.mimeType));
        }
        if(link.rel!=null && link.rel.length()>0) {
            attrs.append(XmlUtil.attrs(ATTR_REL, link.rel));
        }
        if(link.title!=null && link.title.length()>0) {
            attrs.append(XmlUtil.attrs(ATTR_TITLE, link.title));
        }
        return XmlUtil.tag(TAG_LINK,
                           attrs+XmlUtil.attrs(ATTR_HREF, link.url));

    }


    /**
     * _more_
     *
     * @param rel _more_
     *
     * @param type _more_
     * @param href _more_
     *
     * @return _more_
     */
    public static String makeLink(String rel, String href) {
        return XmlUtil.tag(TAG_LINK,
                           XmlUtil.attrs(ATTR_REL, rel, ATTR_HREF, href));
    }


    /**
     * _more_
     *
     * @param type _more_
     * @param content _more_
     *
     * @return _more_
     */
    public static String makeContent(String type, String content) {
        return XmlUtil.tag(TAG_LINK, XmlUtil.attrs(ATTR_TYPE, type),
                           XmlUtil.getCdata(content));
    }

    /**
     * _more_
     *
     *
     * @param id _more_
     * @return _more_
     */
    public static String openFeed(String id) {
        String blobOfNamespaces =
            " xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:time=\"http://a9.com/-/opensearch/extensions/time/1.0/\"  xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gmi=\"http://www.isotc211.org/2005/gmi\" xmlns:gml=\"http://www.opengis.net/gml\" ";
        return XmlUtil.openTag(TAG_FEED,
                               XmlUtil.attrs(ATTR_XMLNS, XMLNS,
                                             ATTR_XMLNS_GEORSS,
                                             XMLNS_GEORSS) +
                               blobOfNamespaces) +
            XmlUtil.tag(TAG_ID, "", id) + 
            XmlUtil.tag(TAG_UPDATED, "",format(new Date()));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static String closeFeed() {
        return XmlUtil.closeTag(TAG_FEED);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param uri _more_
     *
     * @return _more_
     */
    public static String makeAuthor(String name, String uri) {
        // <author>   <name>Xah Lee</name>   <uri>http://xahlee.org/</uri> </author>
        return XmlUtil.tag(TAG_AUTHOR, "",
                           XmlUtil.tag(TAG_NAME, "", name)
                           + XmlUtil.tag(TAG_URI, "", uri));
    }

    /**
     * _more_
     *
     * @param title _more_
     * @param id _more_
     * @param published _more_
     * @param updated _more_
     * @param summary _more_
     * @param content _more_
     * @param author _more_
     * @param authorUrl _more_
     * @param links _more_
     * @param extraStuff _more_
     *
     * @return _more_
     */
    public static String makeEntry(String title, String id, Date published,
                                   Date updated, Date fromDate, Date toDate,
                                   String summary,
                                   String content, String author,
                                   String authorUrl, List<Link> links,
                                   String extraStuff) {
        StringBuffer sb = new StringBuffer();
        if (updated == null) {
            updated = published;
        }
        /* <entry>
   <title>Batman thoughts</title>
   <id>tag:xahlee.org,2006-09-09:015218</id>
   <updated>2006-09-08T18:52:18-07:00</updated>
   <summary>Some notes after watching movie Batman.</summary>
   <content type="xhtml">
      <div xmlns="http://www.w3.org/1999/xhtml">
      <p>I watched Batman today ...</p>
      <!-- more xhtml here -->
      </div>
   </content>
  <link rel="alternate" href="pd.html"/>
  </entry>*/

        sb.append(XmlUtil.openTag(TAG_ENTRY));
        sb.append(XmlUtil.tag(TAG_TITLE, "", title));
        sb.append(XmlUtil.tag(TAG_ID, "", id));


        if (published != null) {
            sb.append(XmlUtil.tag(TAG_PUBLISHED, "", format(published)));
        }
        if (updated != null) {
            sb.append(XmlUtil.tag(TAG_UPDATED, "", format(updated)));
        }

        if(fromDate!=null && toDate!=null) {
            sb.append(makeTimeRange(fromDate, toDate));
        }
        sb.append(makeAuthor(author, authorUrl));


        if ((summary != null) && (summary.length() > 0)) {
            sb.append(XmlUtil.tag(TAG_SUMMARY, XmlUtil.attrs(ATTR_TYPE,"html"),
                                  XmlUtil.getCdata(summary)));
        }
        if ((content != null) && (content.length() > 0)) {
            sb.append(XmlUtil.tag(TAG_CONTENT, "",
                                  XmlUtil.getCdata(content)));
        }

        if (extraStuff != null) {
            sb.append(extraStuff);
        }

        if (links != null) {
            for (Link link : links) {
                sb.append(makeLink(link));
                sb.append("\n");
            }
        }
        sb.append(XmlUtil.closeTag(TAG_ENTRY));
        sb.append("\n");
        return sb.toString();
    }



    /**
     * Class description
     *
     *
     * @version        $version$, Wed, Feb 2, '11
     * @author         Enter your name here...
     */
    public static class Link {

        /** _more_ */
        private String rel;

        /** _more_ */
        private String url;

        /** _more_ */
        private String title;

        private String  mimeType;

        /**
         * _more_
         *
         * @param url _more_
         */
        public Link(String url) {
            this(REL_SELF, url);
        }

        /**
         * _more_
         *
         * @param rel _more_
         * @param url _more_
         */
        public Link(String rel, String url) {
            this(rel, url, null);
        }

        /**
         * _more_
         *
         * @param rel _more_
         * @param url _more_
         * @param title _more_
         */
        public Link(String rel, String url, String title) {
            this(rel, url, title, null);
        }

        public Link(String rel, String url, String title, String mimeType) {
            this.rel   = rel;
            this.url   = url;
            this.title = title;
            this.mimeType = mimeType;
        }
    }
}
