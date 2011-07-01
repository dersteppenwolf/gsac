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

package org.gsac.gsl.output.site;

import ucar.unidata.xml.XmlUtil;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;

import java.text.SimpleDateFormat;
import java.io.*;

import java.util.ArrayList;
import java.util.Date;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class XmlSiteLogOutputHandler extends GsacOutputHandler {

    /** output id */
    public static final String OUTPUT_SITE_XMLLOG = "site.xmllog";

    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");

    /**
     * ctor
     *
     * @param gsacServlet the servlet
     */
    public XmlSiteLogOutputHandler(GsacRepository gsacServlet) {
        super(gsacServlet);
        getRepository().addOutput(OUTPUT_GROUP_SITE,
                                  new GsacOutput(this, OUTPUT_SITE_XMLLOG,
						 "XML Site Log","/site.xml",true));
    }



    /**
     * handle the request
     *
     * @param request the request
     * @param response the response to write to
     *
     *
     * @throws Exception on badness
     */
    public void handleSiteResult(GsacRequest request, GsacResponse response)
	throws Exception {
        response.startResponse(GsacResponse.MIME_XML);
        PrintWriter pw = response.getPrintWriter();
        pw.append(XmlUtil.XML_HEADER + "\n");
	pw.append(XmlUtil.openTag(XmlSiteLog.TAG_IGSSITELOG,
				  XmlUtil.attrs(new String[]{
					  XmlSiteLog.ATTR_XMLNS_XMLNS,  
					  XmlSiteLog.XMLNS_XMLNS,
					  XmlSiteLog.ATTR_XMLNS_REALTIME,
					  XmlSiteLog.XMLNS_XMLNS_REALTIME,
					  XmlSiteLog.ATTR_XMLNS_XSI, 
					  XmlSiteLog.XMLNS_XMLNS_XSI,
					  XmlSiteLog.ATTR_XMLNS_MI, 
					  XmlSiteLog.XMLNS_XMLNS_MI,
					  XmlSiteLog.ATTR_XMLNS_LI, 
					  XmlSiteLog.XMLNS_XMLNS_LI,
					  XmlSiteLog.ATTR_XMLNS_CONTACT, 
					  XmlSiteLog.XMLNS_XMLNS_CONTACT,
					  XmlSiteLog.ATTR_XSI_SCHEMALOCATION,  
					  XmlSiteLog.VALUE_XSI_SCHEMALOCATION,
				      })));

	/*
	  <formInformation>
	  <mi:preparedBy>Scripps Orbit and Permanent Array</mi:preparedBy>
	  <mi:datePrepared>2011-07-01</mi:datePrepared>
	  <mi:reportType>DYNAMIC</mi:reportType>
	  </formInformation>
	*/

	pw.append(XmlUtil.tag(XmlSiteLog.TAG_FORMINFORMATION, "",
			      XmlUtil.tag(XmlSiteLog.TAG_MI_PREPAREDBY,"",getRepository().getRepositoryName()) +
			      XmlUtil.tag(XmlSiteLog.TAG_MI_DATEPREPARED,"",sdf1.format(new Date())) +
			      XmlUtil.tag(XmlSiteLog.TAG_MI_REPORTTYPE,"","DYNAMIC")));
			      
	List<GsacSite> sites = response.getSites();
	//We can have any number of sites here
	for (GsacSite site : sites) {
	    getRepository().getSiteManager().doGetFullSiteMetadata(site);
	    addSiteIdentification(pw, site);
	    addSiteLocation(pw, site);
	    addSiteEquipment(pw, site);
	}
	pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_IGSSITELOG));
        response.endResponse();
    }


    private void addSiteIdentification(PrintWriter pw, GsacSite site) throws Exception {
	/*
	  <siteIdentification>
	  <mi:siteName>TresPiedraNM2006</mi:siteName>
	  <mi:fourCharacterID>P123</mi:fourCharacterID>
	  <mi:monumentInscription/>
	  <mi:iersDOMESNumber/>
	  <mi:cdpNumber/>
	  <mi:monumentDescription>DEEP-DRILLED BRACED</mi:monumentDescription>
	  <mi:heightOfTheMonument> (m)</mi:heightOfTheMonument>
	  <mi:monumentFoundation/>
	  <mi:foundationDepth> (m)</mi:foundationDepth>
	  <mi:markerDescription>NONE</mi:markerDescription>
	  <mi:dateInstalled>2006-02-27T00:00:00Z</mi:dateInstalled>
	  <mi:geologicCharacteristic/>
	  <mi:bedrockType/>
	  <mi:bedrockCondition/>
	  <mi:fractureSpacing/>
	  <mi:faultZonesNearby/>
	  <mi:distance-Activity/>
	  <mi:notes/>
	  </siteIdentification>
	*/

	pw.append(XmlUtil.openTag(XmlSiteLog.TAG_SITEIDENTIFICATION));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_SITENAME,"",site.getName()));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_FOURCHARACTERID,"",site.getSiteCode()));
	Date date = site.getFromDate();
	if(date!=null) {
	    pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_DATEINSTALLED,"",sdf2.format(date)));
	}
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_MONUMENTINSCRIPTION,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_IERSDOMESNUMBER,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_CDPNUMBER,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_MONUMENTDESCRIPTION,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_HEIGHTOFTHEMONUMENT,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_MONUMENTFOUNDATION,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_FOUNDATIONDEPTH,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_MARKERDESCRIPTION,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_GEOLOGICCHARACTERISTIC,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_BEDROCKTYPE,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_BEDROCKCONDITION,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_FRACTURESPACING,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_FAULTZONESNEARBY,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_DISTANCE_ACTIVITY,"",""));
	pw.append(XmlUtil.tag(XmlSiteLog.TAG_MI_NOTES,"",""));
	pw.append(XmlUtil.closeTag(XmlSiteLog.TAG_SITEIDENTIFICATION));
    }


    private void addSiteLocation(PrintWriter pw, GsacSite site) throws Exception {
	/*
	  <siteLocation>
	  <mi:city>Tres Piedras</mi:city>
	  <mi:state>New Mexico</mi:state>
	  <mi:country>United States</mi:country>
	  <mi:tectonicPlate>North America</mi:tectonicPlate>
	  <mi:approximatePositionITRF>
	  <mi:xCoordinateInMeters>-1405300.44</mi:xCoordinateInMeters>
	  <mi:yCoordinateInMeters>-4929803.00</mi:yCoordinateInMeters>
	  <mi:zCoordinateInMeters>3786420.59</mi:zCoordinateInMeters>
	  <mi:latitude-North>+363813.92</mi:latitude-North>
	  <mi:longitude-East>-1055460.00</mi:longitude-East>
	  <mi:elevation-m_ellips>2411.2</mi:elevation-m_ellips>
	  </mi:approximatePositionITRF>
	  <mi:notes/>
	  </siteLocation>
	*/
    }

    private void addSiteEquipment(PrintWriter pw, GsacSite site) throws Exception {
	/*
	  <gnssReceiver>
	  <equip:receiverType>TRIMBLE NETRS</equip:receiverType>
	  <equip:satelliteSystem>GPS</equip:satelliteSystem>
	  <equip:serialNumber>4545260798</equip:serialNumber>
	  <equip:firmwareVersion>1.1-2 19 Apr 2005</equip:firmwareVersion>
	  <equip:elevationCutoffSetting>0 deg</equip:elevationCutoffSetting>
	  <equip:dateInstalled>2006-02-28T00:00:00.000Z</equip:dateInstalled>
	  <equip:dateRemoved>2010-07-08T20:53:00.000Z</equip:dateRemoved>
	  <equip:temperatureStabilization/>
	  <equip:notes/>
	  </gnssReceiver>
	*/

    }

}
