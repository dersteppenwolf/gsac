/*
 * Copyright 2010-2014 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

package org.gsac.gsl.output.site;

import org.gsac.gsl.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;

import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Class description
 *
 * Make a KMZ file for Google Earth, of stations.
 *
 * @version        Wed, May 19, '10
 * @author         J McWhirter
 *
 * @version        4 June 2014 
 * @author         S K Wier; revise contents of the site balloons in Google Earth.  And begin to add some comments.
 */
public class KmlSiteOutputHandler extends HtmlOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_KML = "site.kml";

    /** _more_ */
    public static final String OUTPUT_SITE_KMZ = "site.kmz";

    /**
     * _more_
     *
     * @param gsacRepository _more_
     * @param resourceClass _more_
     */
    public KmlSiteOutputHandler(GsacRepository gsacRepository,
                                ResourceClass resourceClass) {
        super(gsacRepository, resourceClass);

        getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_SITE_KMZ, "Google Earth KMZ", "/sites.kmz", true));

        // 2014 usage survey of UNAVCO GSAC shows this output choice, for a KML file, is never used, so do not offer it.
        //getRepository().addOutput(getResourceClass(), new GsacOutput(this, OUTPUT_SITE_KML, "Google Earth KML", "/sites.kml", true));
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean shouldUrlsBeAbsolute() {
        return true;
    }


    /**
     * _more_
     *
     *
     * @param type _more_
     * @param request the request
     * @param response the response to write to
     *
     *
     * @throws Exception on badness
     */
    public void handleRequest(ResourceClass type, GsacRequest request,
                              GsacResponse response)
            throws Exception {
        String path = request.getRequestURI();

        //If the path does not end with .kmz then send a redirect

        boolean kmz = request.get(ARG_OUTPUT, "").equals(OUTPUT_SITE_KMZ);

        if (kmz && !path.endsWith(".kmz")) {
            path = path + "/sites.kmz";
            String redirectUrl = path + "?" + request.getUrlArgs();
            response.sendRedirect(redirectUrl);
            response.endResponse();

            return;
        }

        if ( !path.endsWith(".kml") && !path.endsWith(".kmz")) {
            path = path + "/sites.kml";
            String redirectUrl = path + "?" + request.getUrlArgs();
            response.sendRedirect(redirectUrl);
            response.endResponse();
            return;
        }

        boolean      kml = path.endsWith(".kml");

        StringBuffer sb  = new StringBuffer();
        response.startResponse(kml ? GsacResponse.MIME_KML : GsacResponse.MIME_KMZ);
        getRepository().processRequest(getResourceClass(), request, response);
        Element root   = KmlUtil.kml("Site Search");
        Element doc    = KmlUtil.document(root, "GSAC Google Earth Stations", true);
        Element folder = doc;
        //        Element folder = KmlUtil.folder(doc, "Site Groups", false);
        List<GsacSite>             sites    = response.getSites();
        Hashtable<String, Element> groupMap = new Hashtable<String, Element>();
        Hashtable<String, Element> iconMap = new Hashtable<String, Element>();
        for (GsacSite site : sites) {
            String       href = makeResourceViewHref(site);
            StringBuffer html = new StringBuffer();
            html.append(href);
            getResourceHtml(request, html, site, false, false, true);
            Element             groupElement = folder;
            List<ResourceGroup> groups       = site.getResourceGroups();
            if (groups.size() > 0) {
                ResourceGroup firstGroup = groups.get(0);
                groupElement = groupMap.get(firstGroup.getId());
                if (groupElement == null) {
                    groupElement = KmlUtil.folder(folder, firstGroup.getName(), false);
                    groupMap.put(firstGroup.getId(), groupElement);
                }
            }

            /* to set an icon replacing the fat yellow pushpin as the point placemark icon:
            

The <href> element in KML takes a URL. The URL can be an absolute or relative location.

To get it working suggest you first move the KML file and the image to the same folder then refer to the image by its filename.

<Style id="icon">
        <IconStyle>
          <Icon>
            <href>etnasss.jpg</href>
          </Icon>
        </IconStyle>
 </Style>

Source: https://developers.google.com/kml/documentation/kmlreference#href

Next you could refer to the image by its absolute location (e.g. file:///C:/etnasss.jpg) but Google Earth has security policy regarding access to local files on the file system outside the context of the KML file. You'd have to allow access to local files which generally is not recommended.

Alternatively you could create a KMZ file (aka ZIP file) and include the image within the KMZ archive file and reference it in the KML file.

see also http://dagik.org/kml_intro/E/point.html:
Change the icon of the marker<Style>, <IconStyle>, <styleUrl>

<?xml version="1.0" encoding="UTF-8"?> 
<kml xmlns="http://earth.google.com/kml/2.0"> <Document>

<Style id="My_Style">
 <IconStyle> <Icon> <href>ball.png</href> </Icon></IconStyle>
</Style> 

<Placemark>
 <name> New point</name> 
 <description>This is a new point to learn KML format.</description>
 <styleUrl> #My_Style</styleUrl> 
 <Point>
  <coordinates>
   135.2, 35.4, 0.
  </coordinates>
 </Point> 
</Placemark>

</Document> </kml>

Save "ball.png" in the same folder with the above kml file.
Image file can be a file on WWW, such as "http://www...../ball.png" .
"styleUrl" can also be a style file on WWW.
To make one file with image files and kml file, combine the kml file and image files with "zip" command, and set the extension ".kmz", such as "test.kmz". 

            */

            String  url          = getIconUrl(request, site);
            Element styleElement = iconMap.get(url);
            String  styleId;
            if (styleElement == null) {
                styleId      = "style" + iconMap.size();
                styleElement = KmlUtil.iconstyle(doc, styleId, url, 0.5);
                iconMap.put(url, styleElement);
            } else {
                styleId = XmlUtil.getAttribute(styleElement, "id", "");
            }

            Element placemark = KmlUtil.placemark(groupElement,
                                    site.getLabel(), html.toString(),
                                    site.getLatitude(), site.getLongitude(),
                                    0, styleId);
            KmlUtil.snippet(placemark, site.getShortName());
        }
        //      System.err.println("xml:" +  XmlUtil.toString(root));

        if (kml) {
            PrintWriter pw = response.getPrintWriter();
            XmlUtil.toString(root, pw);
        } else {
            StringBuffer kmlBuffer = new StringBuffer();
            XmlUtil.toString(root, kmlBuffer);
            OutputStream    os  = request.getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(os);
            zos.setLevel(0);
            zos.putNextEntry(new ZipEntry("sites.kml"));
            byte[] bytes = kmlBuffer.toString().getBytes();
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            IOUtil.close(zos);
        }
        response.endResponse();
    }

}
