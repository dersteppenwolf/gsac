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

package org.gsac.ramadda;

import org.gsac.gsl.util.NtripLine;
import org.ramadda.repository.*;
import org.ramadda.repository.auth.User;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;



import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;



import java.io.File;


import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 * This harvester fetches GNSS stream metadata from NTRIP servers. 
 * It parses the Source Table and creates RAMADDA Metadata elements 
 * define in the gsacmetadata.xml file
 *
 */
public class NtripHarvester extends WebHarvester {

    /** source table type for stream */
    public static final String TYPE_STR = "STR";

    /** other source table type */
    public static final String TYPE_CAS = "CAS";

    /** other source table type */
    public static final String TYPE_NET = "NET";


    /**
     * ctor
     *
     * @param repository ramadda
     * @param id harvester id
     *
     * @throws Exception On badness
     */
    public NtripHarvester(Repository repository, String id) throws Exception {
        super(repository, id);
    }

    /**
     * ctor
     *
     * @param repository ramadda
     * @param element xml node
     * @param id harvester id
     *
     * @throws Exception On badness
     */
    public NtripHarvester(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


    /**
     * Get the description of this harvester type
     *
     * @return description
     */
    public String getDescription() {
        return "NTRIP Source Table Harvester";
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception On badness
     */
    public void addToEditForm(Request request, StringBuffer sb)
            throws Exception {
        addBaseGroupSelect(ATTR_BASEGROUP, sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entrySB _more_
     * @param urlEntry _more_
     * @param cnt _more_
     *
     * @throws Exception On badness
     */
    protected void addEntryToForm(Request request, StringBuffer entrySB,
                                  HarvesterEntry urlEntry, int cnt)
            throws Exception {
        entrySB.append(
            HtmlUtil.formEntry(
                msgLabel("Parent Folder Name"),
                HtmlUtil.input(
                    ATTR_NAME + cnt, urlEntry.getName(),
                    HtmlUtil.SIZE_80 + HtmlUtil.title(templateHelp))));
    }


    /**
     * _more_
     *
     * @param urlEntry _more_
     * @param entries _more_
     *
     *
     * @return _more_
     * @throws Exception On badness
     */
    protected boolean processEntry(HarvesterEntry urlEntry,
                                   List<Entry> entries)
            throws Exception {
        String baseGroupName = urlEntry.getName();
        Entry  rootGroup     = getBaseGroup();
        User   user          = getUser();
        Entry baseGroup =
            getEntryManager().findEntryFromName(rootGroup.getFullName()
                + Entry.PATHDELIMITER + baseGroupName, user, true);
        processSourceTable(urlEntry, baseGroup, entries);
        return true;
    }



    /**
     * _more_
     *
     * @param urlEntry _more_
     * @param baseGroup _more_
     * @param entries _more_
     *
     * @throws Exception On badness
     */
    protected void processSourceTable(HarvesterEntry urlEntry,
                                      Entry baseGroup, List<Entry> entries)
        throws Exception {

        GsacFileTypeHandler streamTypeHandler =
            (GsacFileTypeHandler) getRepository().getTypeHandler(
                                                                 GsacSiteTypeHandler.TYPE_STREAM, false, false);
        /*        GsacSiteTypeHandler siteTypeHandler =
                  (GsacSiteTypeHandler) getRepository().getTypeHandler(
                  GsacSiteTypeHandler.CLASS_SITE, false, false);
        */

        int myCnt=0;
        User        user        = getUser();
        String      url         = urlEntry.getUrl();

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        System.err.println("url:" + url);
        System.err.println("Processing source table:" + url);
        StringBuffer errorBuff = new StringBuffer();
        //Use NtripLine (from the GSL)  to read and parse the source table
        List<NtripLine> lines = NtripLine.readSourceTable(url, errorBuff);
        if (lines == null) {
            logHarvesterError("UnavcoSiteManager.initPboSourceTable: Unable to read source table:"
                             + url + " " + errorBuff, null);
            return;
        }

        Hashtable<String, Entry> siteMap = new Hashtable<String, Entry>();
        for(NtripLine line: lines) {
            String siteId         = line.getIdentifier();
            if (siteId.indexOf("_") >= 0) {
                siteId = siteId.substring(0, siteId.indexOf("_"));
            }
            if (siteId.trim().length() == 0) {
                continue;
            }
            Date now = new Date();
            String sitePath = baseGroup.getFullName()
                + Entry.PATHDELIMITER + siteId;
            boolean haveISeenThisSite = true;
            Entry siteEntry = siteMap.get(sitePath);
            if (siteEntry == null) {
                siteEntry = getEntryManager().findEntryFromName(sitePath,
                                                                user, false);
                haveISeenThisSite = false;
            }

            boolean newSite = false;
            if (siteEntry == null) {
                newSite = true;
                siteEntry =
                    streamTypeHandler.createEntry(repository.getGUID());
                siteEntry.initEntry(siteId, "", baseGroup, getUser(),
                                    new Resource(), "", now.getTime(),
                                    now.getTime(), now.getTime(),
                                    now.getTime(), new Object[] { siteId,
                                                                  "active", url });

                System.err.println("\t" + (newSite
                                           ? "new site:"
                                           : "old site:") + siteEntry.getFullName());
            } else if(!haveISeenThisSite) {
                //TODO: Delete all of the stream metadata
            }
            siteMap.put(sitePath, siteEntry);



            //Add the site if it is new. Else store it if the location has changed
            boolean siteChanged = (siteEntry.getNorth() != line.getLatitude())
                || (siteEntry.getWest() != line.getLongitude());
            siteEntry.setLocation(line.getLatitude(), line.getLongitude(), 0);
            if (newSite) {
                entries.add(siteEntry);
            } else if (siteChanged) {
                getEntryManager().storeEntry(siteEntry);
            }

            Metadata ntripMetadata =
                new Metadata(getRepository().getGUID(),
                             siteEntry.getId(),
                             GsacMetadataHandler.TYPE_NTRIP,
                             new String[]{
                                 url,
                                 line.getMountPoint(),
                                 line.getIdentifier(),
                                 line.getFormat(),
                                 line.getFormatDetails(),
                                 line.getCarrier(),
                                 line.getNavSystem(),
                                 line.getNetwork(),
                                 line.getCountry(),
                                 ""+ line.getNmea(),
                                 ""+  line.getSolution(),
                                 line.getGenerator(),
                                 line.getCompression(),
                                 line.getAuthentication(),
                                 line.getFee(),
                                 ""+ line.getBitRate()
                             });
            if ( !siteEntry.hasMetadata(ntripMetadata)) {
                siteEntry.addMetadata(ntripMetadata);
            }

            if (myCnt++ > 5) {
                break;
            }
        }
    }
}
