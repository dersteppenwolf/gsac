/*
 *
 */

package org.iris.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.util.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.xml.XmlUtil;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.w3c.dom.*;


/**
 * Handles all of the site related repository requests
 * The main entry point is the  {@link #handleRequest} method.
 * Look for the CHANGEME comments
 * 
 *
 * @author         Jeff McWhirter
 */
public class IrisSiteManager extends SiteManager {

    public static final String IRIS_URL = "http://www.iris.edu/ws/station/query";

    public static final String ARG_IRIS_STA = "sta";
    public static final String ARG_IRIS_NET = "net";

    /**
     * ctor
     *
     * @param repository the repository
     */
    public IrisSiteManager(IrisRepository repository) {
        super(repository);
    }


    /** CHANGEME This is the main entry point for handling queries
        If you don't implement this method then the base SiteManager
        class will 
     **/
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        StringBuffer msgBuff    = new StringBuffer();

        /*
        if (request.defined(ARG_NORTH) && 
        if (request.defined(ARG_SOUTH)) {
        if (request.defined(ARG_EAST)) {
        if (request.defined(ARG_WEST)) {
            appendSearchCriteria(msgBuff, "west&gt;=",
                                 "" + request.get(ARG_WEST, 0.0));
        */
        List<String> args = new ArrayList<String>();
        List<String> siteCodes = request.get(ARG_SITE_CODE, new ArrayList<String>());
        for(String siteCode: siteCodes) {
            if(siteCode.length()==0) continue;
            appendSearchCriteria(msgBuff, "station=",
                                 siteCode);
            args.add(HtmlUtil.arg(ARG_IRIS_STA, siteCode));
        }

        
        List<String> groups = request.get(ARG_SITE_GROUP,new ArrayList<String>());
        for(String group: groups) {
            if(group.length()==0) continue;
            appendSearchCriteria(msgBuff, "network=",
                                 group);
            args.add(HtmlUtil.arg(ARG_IRIS_NET, group));
        }

        List<GsacSite> sites = getSites(args);
        for(GsacSite site: sites) {
            response.addResource(site);
        }
        setSearchCriteriaMessage(response, msgBuff);
    }

    private List<GsacSite> getSites(List<String> args) throws Exception {
        List<GsacSite> sites = new ArrayList<GsacSite>();
        String url = IRIS_URL+"?" + StringUtil.join("&",args);
        System.err.println("IRIS: url=" + url);
        Element root   = XmlUtil.getRoot(url, getClass());
        NodeList networkElements = XmlUtil.getElements(root, IrisXml.TAG_NETWORK);
        for (int i = 0; i < networkElements.getLength(); i++) {
            Element networkElement = (Element) networkElements.item(i);
            NodeList stationElements = XmlUtil.getElements(networkElement, IrisXml.TAG_STATION);
            for (int stationIdx = 0; stationIdx < stationElements.getLength(); stationIdx++) {
                Element stationElement = (Element) stationElements.item(stationIdx);
                GsacSite site  = makeSite(stationElement);
                sites.add(site);
            }
        }
        return sites;
    }

    private GsacSite makeSite(Element siteElement) throws Exception {
        String network = XmlUtil.getAttribute(siteElement, IrisXml.ATTR_NET_CODE,"");
        String stationCode = XmlUtil.getAttribute(siteElement, IrisXml.ATTR_STA_CODE,"");
        GsacSite site = new GsacSite(stationCode, stationCode, stationCode);
        site.addMetadata(new IconMetadata("http://www.iris.edu/images/symbols/circle-dot-10x10-FF3333.png"));
        site.addMetadata(new LinkMetadata("http://www.iris.edu/mda/" + network +"/" + stationCode,stationCode +"@IRIS"));

        List<ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>();
        resourceGroups.add(new ResourceGroup(network));
        site.setResourceGroups(resourceGroups);
        NodeList epochElements = XmlUtil.getElements(siteElement, IrisXml.TAG_STATIONEPOCH);
        for (int epochIdx = 0; epochIdx < epochElements.getLength(); epochIdx++) {
            Element epochElement = (Element) epochElements.item(epochIdx);
            String lat = XmlUtil.getGrandChildText(epochElement, IrisXml.TAG_LAT,"0.0");
            String lon = XmlUtil.getGrandChildText(epochElement, IrisXml.TAG_LON,"0.0");
            String elevation = XmlUtil.getGrandChildText(epochElement, IrisXml.TAG_ELEVATION,"0.0");
            site.setLatitude(Double.parseDouble(lat));
            site.setLongitude(Double.parseDouble(lon));
            site.setElevation(Double.parseDouble(elevation));
            site.setLongName(XmlUtil.getGrandChildText(epochElement, IrisXml.TAG_NAME, stationCode));
            break;
        }
        return site;
    }


    /**
     * CHANGEME
     *
     * @param resourceId resource id. This isn't the resource code but actually the monument id
     *
     * @return the resource or null if not found
     *
     * @throws Exception on badness
     */
    public GsacResource getResource(String resourceId) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add(HtmlUtil.arg(ARG_IRIS_STA, resourceId));
        List<GsacSite> sites = getSites(args);
        if(sites.size()==0) return null;
        return sites.get(0);
    }



    /**
     * get all of the metadata for the given site
     *
     * @param gsacResource resource
     *
     * @throws Exception On badness
     */
    public void doGetMetadata(int level, GsacResource gsacResource) throws Exception {
    }

    /**
     * Get the extra site search capabilities. 
     *
     * @return site search capabilities
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();
        String              help = HtmlOutputHandler.stringSearchHelp;
        Capability          siteCode;
        Vocabulary groupVocabulary = getRepository().getVocabularyFromType(ARG_SITE_GROUP);
        List<ResourceGroup> siteGroups = ResourceGroup.convertList(groupVocabulary.getValues());
        Capability[]        dflt       = {
            siteCode = initCapability(new Capability(ARG_SITE_CODE,
                                                     "Site Code",
                                                     Capability.TYPE_STRING), CAPABILITY_GROUP_SITE_QUERY,
                                      "Short name of the site",
                                      "Short name of the site. " + help),
            initCapability(
                           new Capability(
                                          makeUrlArg(ARG_SUFFIX_GROUP), "Network",
                                          IdLabel.toList(siteGroups),
                                          true), CAPABILITY_GROUP_ADVANCED, null),
            initCapability(new Capability(ARG_BBOX, "Bounds",
                                          Capability.TYPE_SPATIAL_BOUNDS), CAPABILITY_GROUP_SITE_QUERY,
                           "Spatial bounds within which the site lies")
        };
        siteCode.setBrowse(true);
        for (Capability capability : dflt) {
            if (capability != null) {
                capabilities.add(capability);
            }
        }
        return capabilities;
    }


    /**
     * Get the site group list. This is used by the addDefaultSiteCapabilities 
     *
     * @return site group list
     */
    public List<ResourceGroup> doGetResourceGroups() {
        List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        /**
           CHANGEME
        groups.add(new ResourceGroup("group1","Group 1"));
        groups.add(new ResourceGroup("group2", "Group 2"));
        groups.add(new ResourceGroup("group3","Group 3"));
        Collections.sort((List) groups);
        */
        return groups;
    }




}
