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



import org.w3c.dom.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.User;
import org.ramadda.repository.harvester.*;
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
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GsacHarvester extends WebHarvester {

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public GsacHarvester(Repository repository, String id) throws Exception {
        super(repository, id);
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public GsacHarvester(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "GSAC DHF and MC Files";
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entrySB _more_
     * @param urlEntry _more_
     * @param cnt _more_
     *
     * @throws Exception _more_
     */
    protected void addEntryToForm(Request request, StringBuffer entrySB,
                                  HarvesterEntry urlEntry, int cnt)
            throws Exception {
        addBaseFolderToForm(request, entrySB, urlEntry, cnt);
    }


    /**
     * _more_
     *
     * @param urlEntry _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    protected boolean processEntry(HarvesterEntry urlEntry, List<Entry> entries)
            throws Exception {
        String baseGroupId = urlEntry.getBaseGroupId();
        Entry  baseGroup   = ((baseGroupId.length() == 0)
                              ? null
                              : getEntryManager().findGroup(null,
                                  baseGroupId));

        String url         = urlEntry.getUrl();

        //        http://www.panga.cwu.edu/data_ftp_pub/GSAC/full/panga.full.mc
        if (IOUtil.hasSuffix(url.toLowerCase(), "mc")) {
            processMCFile(urlEntry, baseGroup, entries);
        } else if (IOUtil.hasSuffix(url.toLowerCase(), "dhf")) {
            processDHFFile(urlEntry, baseGroup);
        } else {
            throw new IllegalArgumentException("Unknown file type:" + url);
        }
        return true;
    }


    //# DHF_fields unique_info_id;wholesaler;data_type;unique_site_id;start_time;end_time;dhr_create_time;info_url;file_size;file_create_time;file_checksum;provider;file_grouping;file_compression

    /**
     * _more_
     *
     * @param urlEntry _more_
     * @param baseGroup _more_
     *
     * @throws Exception _more_
     */
    protected void processDHFFile(HarvesterEntry urlEntry, Entry baseGroup)
            throws Exception {
        GsacSiteTypeHandler siteTypeHandler =
            (GsacSiteTypeHandler) getRepository().getTypeHandler(
                GsacSiteTypeHandler.TYPE_SITE, false, false);
        //        GsacResourceTypeHandler resourceTypeHandler = 
        String contents = IOUtil.readContents(urlEntry.getUrl(), getClass(),
                              null);
        if (contents == null) {
            getRepository().getLogManager().logError(
                "GsacHarvester: could not read DHF file:"
                + urlEntry.getUrl());
            return;
        }

        List<String> toks       = StringUtil.split(contents, "\n", true,
                                      true);
        User         user       = getUser();
        List<Entry>  oldEntries = new ArrayList<Entry>();
        for (String line : toks) {
            if (line.startsWith("#")) {
                continue;
            }
            List<String> cols = StringUtil.split(line, ";", true, true);
            int          col            = 0;
            String       uniqueId       = cols.get(col++);
            String       wholesaler     = cols.get(col++);
            String       dataType       = cols.get(col++);
            String       siteId         = cols.get(col++);
            String       startTime      = cols.get(col++);
            String       endTime        = cols.get(col++);
            String       dhrCreateTime  = cols.get(col++);
            String       url            = cols.get(col++);
            long         fileSize       = Long.parseLong(cols.get(col++));
            String       fileCreateTime = cols.get(col++);
            String       checksum       = cols.get(col++);
            String       provider       = cols.get(col++);
            String       fileGrouping   = cols.get(col++);
            String       compression    = cols.get(col++);
        }

    }


    //   # MC_fields unique_site_id;wholesaler;char_id;descriptive_id;dhr_create_time;x;y;z;coord_accuracy

    /**
     * _more_
     *
     * @param urlEntry _more_
     * @param baseGroup _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    protected void processMCFile(HarvesterEntry urlEntry, Entry baseGroup,
                                 List<Entry> entries)
            throws Exception {

        GsacSiteTypeHandler typeHandler =
            (GsacSiteTypeHandler) getRepository().getTypeHandler(
                GsacSiteTypeHandler.TYPE_SITE, false, false);
        String contents = IOUtil.readContents(urlEntry.getUrl(), getClass(),
                              null);
        if (contents == null) {
            getRepository().getLogManager().logError(
                "GsacHarvester: could not read MC file:" + urlEntry.getUrl());
            return;
        }
        List<String> toks      = StringUtil.split(contents, "\n", true, true);
        double[]     latLonAlt = new double[3];
        System.out.println("#siteId,latitude,longitude,altitude");
        User        user       = getUser();
        List<Entry> oldEntries = new ArrayList<Entry>();
        for (String line : toks) {
            if (line.startsWith("#")) {
                continue;
            }
            List<String> cols = StringUtil.split(line, ";", true, true);
            int          col           = 0;
            String       siteId        = cols.get(col++);
            String       wholesaler    = cols.get(col++);
            String       charId        = cols.get(col++);
            String       descriptiveId = cols.get(col++).replace("\\", "");
            String       createTime    = cols.get(col++);
            Date         dttm          = DateUtil.parse(createTime);
            double       x             = Double.parseDouble(cols.get(col++));
            double       y             = Double.parseDouble(cols.get(col++));
            double       z             = Double.parseDouble(cols.get(col++));
            double       accuracy      = Double.parseDouble(cols.get(col++));
            //jeffmc comment out so we can build            latLonAlt = GeoUtils.wgs84XYZToLatLonAlt(x, y, z, latLonAlt);
            latLonAlt = null;


            long    date     = dttm.getTime();
            Entry   entry = typeHandler.findSiteEntry(siteId, wholesaler);
            boolean newEntry = (entry == null);
            if (entry == null) {
                entry = typeHandler.createEntry(getRepository().getGUID());
            }

            Object[] values = new Object[] { siteId, wholesaler };
            //TODO: Date logic
            entry.initEntry(charId, descriptiveId, baseGroup, user, null,
                            null, date, date, date, date, values);
            entry.setLocation(latLonAlt[0], latLonAlt[1], latLonAlt[2]);
            //            System.err.println ("adding:" + entry);
            if (newEntry) {
                entries.add(entry);
            } else {
                oldEntries.add(entry);
            }
        }

        if (oldEntries.size() > 0) {
            getRepository().getEntryManager().insertEntries(oldEntries,
                    false, true);
        }
    }

}
