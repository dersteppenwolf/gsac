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


import org.ramadda.repository.*;
import org.ramadda.repository.auth.User;
import org.ramadda.repository.harvester.*;


import org.w3c.dom.Element;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A harvester for the old GSAC MC and DHF files
 *
 */
public class GsacHarvester extends WebHarvester {

    StringBuffer status = new StringBuffer();

    /**
     * ctor
     *
     * @param repository the repository
     * @param id id of this harvester
     *
     * @throws Exception On badness
     */
    public GsacHarvester(Repository repository, String id) throws Exception {
        super(repository, id);
    }

    /**
     * ctor
     *
     * @param repository the repository
     * @param element xml
     *
     * @throws Exception On badness
     */
    public GsacHarvester(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


    /**
     * Get the description of this harveter type
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return "GSAC DHF and MC Files";
    }


    @Override
    public String getExtraInfo() throws Exception {
        return status.toString();
    }

    /**
     * Override base class method to add items to the harvester edit form
     *
     * @param request the request
     * @param entrySB buffer
     * @param urlEntry The URL
     * @param cnt which entry
     *
     * @throws Exception On badness
     */
    @Override
    protected void addEntryToForm(Request request, StringBuffer entrySB,
                                  HarvesterEntry urlEntry, int cnt)
            throws Exception {
        //Just add the parent entry selector widget
        addBaseFolderToForm(request, entrySB, urlEntry, cnt);
    }


    /**
     * This method gets called by the base UrlHarvester class. It either handles the DHF or rhe MC files
     *
     * @param urlEntry Holds the URL to harvest
     * @param entries Add new entries to this list and the base class inserts them into the repository
     *
     *
     * @return Everything is cool
     * @throws Exception On badness
     */
    @Override
    protected boolean processEntry(HarvesterEntry urlEntry,
                                   List<Entry> entries)
            throws Exception {
        status = new StringBuffer();
        String baseGroupId = urlEntry.getBaseGroupId();
        Entry  baseGroup   = ((baseGroupId.length() == 0)
                              ? null
                              : getEntryManager().findGroup(null,
                                  baseGroupId));

        String url         = urlEntry.getUrl();

        //e.g. -        http://www.panga.cwu.edu/data_ftp_pub/GSAC/full/panga.full.mc
        if (IOUtil.hasSuffix(url.toLowerCase(), "mc")) {
            status.append("Processing MC file: " + url+"<br>");
            processMCFile(urlEntry, baseGroup, entries);
        } else if (IOUtil.hasSuffix(url.toLowerCase(), "dhf")) {
            status.append("Processing DHF file: " + url+"<br>");
            processDHFFile(urlEntry, baseGroup);
        } else {
            throw new IllegalArgumentException("Unknown file type:" + url);
        }
        return true;
    }


    /**
     * Process the DHF file. Note - this does not do anything
     *
     * @param urlEntry The URL
     * @param baseGroup The parent entry
     *
     * @throws Exception On badness
     */
    protected void processDHFFile(HarvesterEntry urlEntry, Entry baseGroup)
            throws Exception {
        GsacSiteTypeHandler siteTypeHandler =
            (GsacSiteTypeHandler) getRepository().getTypeHandler(
                GsacSiteTypeHandler.CLASS_SITE, false, false);
        //        GsacFileTypeHandler resourceTypeHandler = 
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
            //# DHF_fields unique_info_id;wholesaler;data_type;unique_site_id;start_time;end_time;dhr_create_time;info_url;file_size;file_create_time;file_checksum;provider;file_grouping;file_compression
            int    col            = 0;
            String uniqueId       = cols.get(col++);
            String wholesaler     = cols.get(col++);
            String dataType       = cols.get(col++);
            String siteId         = cols.get(col++);
            String startTime      = cols.get(col++);
            String endTime        = cols.get(col++);
            String dhrCreateTime  = cols.get(col++);
            String url            = cols.get(col++);
            long   fileSize       = Long.parseLong(cols.get(col++));
            String fileCreateTime = cols.get(col++);
            String checksum       = cols.get(col++);
            String provider       = cols.get(col++);
            String fileGrouping   = cols.get(col++);
            String compression    = cols.get(col++);
        }

    }





    



    /**
     * Process the  MC file.
     *
     * @param urlEntry The URL
     * @param baseGroup Parent entry
     * @param entries Add any new entries to the entries list
     *
     * @throws Exception On badness
     */
    protected void processMCFile(HarvesterEntry urlEntry, Entry baseGroup,
                                 List<Entry> entries)
            throws Exception {

        //Read the MC file
        String contents = IOUtil.readContents(urlEntry.getUrl(), getClass(),
                              null);
        if (contents == null) {
            status.append("Error reading MC file<br>");
            logHarvesterError("GsacHarvester: could not read MC file:"
                              + urlEntry.getUrl(), null);
            return;
        }

        //This is the type of entry we are creating
        GsacSiteTypeHandler typeHandler =
            (GsacSiteTypeHandler) getRepository().getTypeHandler(
                GsacSiteTypeHandler.CLASS_SITE, false, false);

        //The user that will own the entries
        User         user      = getUser();

        List<String> lines     = StringUtil.split(contents, "\n", true, true);
        double[]     latLonAlt = new double[3];

        //Keep a list of the existing entries that we find
        List<Entry> oldEntries = new ArrayList<Entry>();

        int         cnt        = 0;
        System.err.println("# lines:" + lines.size());
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            //For testing limit the number of entries to 100
            if (cnt++ > 150) {
                break;
            }

            //   # MC_fields unique_site_id;wholesaler;char_id;descriptive_id;dhr_create_time;x;y;z;coord_accuracy
            List<String> cols = StringUtil.split(line, ";", true, true);
            if (cols.size() < 8) {
                logHarvesterInfo("Incorrect # columns:" + cols.size()
                                 + " in line:" + line);
                continue;
            }

            int    col        = 0;
            String siteId     = cols.get(col++);
            String wholesaler = cols.get(col++);
            String charId     = cols.get(col++);
            String descriptiveId = cols.get(col++).replaceAll("\\\\",
                                            "").replaceAll("_", " ");
            String createTime = cols.get(col++);
            Date   dttm       = DateUtil.parse(createTime);
            double x          = Double.parseDouble(cols.get(col++));
            double y          = Double.parseDouble(cols.get(col++));
            double z          = Double.parseDouble(cols.get(col++));
            double accuracy   = (col < cols.size())
                                ? Double.parseDouble(cols.get(col++))
                                : 1.0;
            latLonAlt = GeoUtils.wgs84XYZToLatLonAlt(x, y, z, latLonAlt);

            long date = dttm.getTime();

            //Look for an existing entry with the given site id and source
            Entry   entry    = typeHandler.findSiteEntry(siteId, wholesaler);
            boolean newEntry = (entry == null);

            //Create the entry if its a new one
            if (entry == null) {
                entry = typeHandler.createEntry(getRepository().getGUID());
            }

            //The values array holds the extra attributes of the site entry
            //These are defined in the types.xml file
            Object[] values = new Object[] { siteId, wholesaler, "active" };

            //TODO: Date logic
            entry.initEntry(charId, descriptiveId, baseGroup, user, null,
                            null, date, date, date, date, values);
            entry.setLocation(latLonAlt[0], latLonAlt[1], latLonAlt[2]);
            if (newEntry) {
                entries.add(entry);
            } else {
                oldEntries.add(entry);
            }
        }



        //if we had existing entries then they have been updated and we need to update the DB
        if (oldEntries.size() > 0) {
            getRepository().getEntryManager().insertEntries(oldEntries,
                    false, true);
        }
        if(entries.size()>0)
            status.append("Added " + entries.size() +" new sites<br>");
        if(oldEntries.size()>0)
            status.append("Updated " + oldEntries.size() +" existing sites<br>");

    }

}
