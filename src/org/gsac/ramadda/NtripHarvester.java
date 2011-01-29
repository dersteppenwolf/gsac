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


import ucar.unidata.repository.*;
import ucar.unidata.repository.type.*;
import ucar.unidata.repository.metadata.*;
import ucar.unidata.repository.auth.User;
import ucar.unidata.repository.harvester.*;
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
public class NtripHarvester extends WebHarvester {

    public static final String TYPE_STR = "STR";
    public static final String TYPE_CAS = "CAS";
    public static final String TYPE_NET = "NET";

    private String urls;

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public NtripHarvester(Repository repository, String id) throws Exception {
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
    public NtripHarvester(Repository repository, Element element)
        throws Exception {
        super(repository, element);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "NTRIP Source Table Harvester";
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
    protected void processEntry(HarvesterEntry urlEntry, List<Entry> entries)
        throws Exception {
        String baseGroupId = urlEntry.getBaseGroupId();
        Entry  baseGroup   = ((baseGroupId.length() == 0)
                              ? null
                              : getEntryManager().findGroup(null,
                                                            baseGroupId));
        processSourceTable(urlEntry, baseGroup, entries);
    }



    /**
     * _more_
     *
     * @param urlEntry _more_
     * @param baseGroup _more_
     *
     * @throws Exception _more_
     */
    protected void processSourceTable(HarvesterEntry urlEntry, Entry baseGroup,  List<Entry> entries)
        throws Exception {



        GsacResourceTypeHandler resourceTypeHandler =
            (GsacResourceTypeHandler) getRepository().getTypeHandler(
                                                                     GsacResourceTypeHandler.TYPE_STREAM, false, false);
        GsacSiteTypeHandler siteTypeHandler =
            (GsacSiteTypeHandler) getRepository().getTypeHandler(
                                                                 GsacSiteTypeHandler.TYPE_SITE, false, false);

        User         user       = getUser();
        Entry sitesEntry =  getEntryManager().findEntryFromName(baseGroup.getFullName()+Entry.PATHDELIMITER+"Sites",user, true);
        TypeHandler typeHandler = resourceTypeHandler;
        String url = urlEntry.getUrl();
        if(url.endsWith("/")) url = url.substring(0, url.length()-1);
        System.err.println("url:" + url);
        System.err.println("Processing source table:" + url);

        logHarvesterInfo("Processing source table:" + url);
        String contents = IOUtil.readContents(url, getClass(),
                                              null);


        //Don't ask...
        contents = contents.replaceAll("<br>","");

        if (contents == null) {
            logHarvesterInfo("Could not read source table:" + url);
            return;
        }
        List<String> toks       = StringUtil.split(contents, "\n", false,
                                                   false);

        Hashtable<String,Entry> siteMap = new Hashtable<String,Entry>();
        int myCnt=0;
        int lineCnt=0;
        for(;lineCnt<toks.size();lineCnt++) {
            String line = toks.get(lineCnt).trim();
            if(lineCnt==0) {
                if(!line.startsWith("SOURCETABLE ")) {
                    logHarvesterInfo("Bad source table:" + line);
                    return;
                }
            }
            if (line.length()==0) {
                lineCnt++;
                break;
            }

        }
        for(;lineCnt<toks.size();lineCnt++) {
            String line = toks.get(lineCnt).trim();
            if(line.equals("ENDSOURCETABLE")) break;
            List<String> cols = StringUtil.split(line, ";", false, false);
            if(cols.size()==0) continue;
            int          col            = 0;
            String type        = cols.get(col++);
            if(!type.equals(TYPE_STR)) {
                if(!type.equals(TYPE_NET)&&!type.equals(TYPE_CAS)) {
                    logHarvesterInfo("Unknown type:" +  line);
                }
                continue;
            }   
            try {

                String mountPoint = cols.get(col++);
                String feedUrl = url + "/" +mountPoint;
                String identifier = cols.get(col++);
                String format     = cols.get(col++);
                String formatDetails = cols.get(col++);
        
                String carrier = cols.get(col++);
                String navSystem = cols.get(col++);
                String network = cols.get(col++);
                String country = cols.get(col++);
                double latitude     = Double.parseDouble(cols.get(col++));
                double longitude     = ucar.unidata.geoloc.LatLonPointImpl.lonNormal(Double.parseDouble(cols.get(col++)));
                int nmea        = Integer.parseInt(cols.get(col++));
                int solution      = Integer.parseInt(cols.get(col++));
                String generator     = cols.get(col++);
                String compression     = cols.get(col++);
                String authentication      = cols.get(col++);
                String fee      = cols.get(col++);
                int bitRate      = Integer.parseInt(cols.get(col++));

                String siteId = identifier;
                if(siteId.indexOf("_")>=0) {
                    siteId = siteId.substring(0,siteId.indexOf("_"));
                }

                Date now= new Date();
                String sitePath = sitesEntry.getFullName()+Entry.PATHDELIMITER+siteId;
                Entry siteEntry = siteMap.get(sitePath);
                if(siteEntry==null) {
                    siteEntry = getEntryManager().findEntryFromName(sitePath,user, false);
                } 

                boolean newSite = false;
                if(siteEntry==null) {
                    newSite = true;
                    siteEntry = siteTypeHandler.createEntry(repository.getGUID());
                    siteEntry.initEntry(siteId, "", sitesEntry, getUser(), new Resource(), "",
                                    now.getTime(), now.getTime(), now.getTime(),
                                        now.getTime(), new Object[]{siteId, url});

                }
                siteMap.put(sitePath, siteEntry);
                //Add the site if its new. Else store it if the location has changed
                boolean siteChanged = siteEntry.getNorth()!= latitude || siteEntry.getWest()!=longitude;
                siteEntry.setLocation(latitude, longitude,0);
                if(newSite) {
                    entries.add(siteEntry);
                } else if(siteChanged) {
                    getEntryManager().storeEntry(siteEntry);
                }

                boolean newEntry = false;
                Entry entry = getEntryManager().findEntryFromName(baseGroup.getFullName()+Entry.PATHDELIMITER+identifier,user, false);
                if(entry==null) {
                    newEntry = true;
                    entry = typeHandler.createEntry(repository.getGUID());
                } else {
                    //Use the old date
                    now = new Date(entry.getCreateDate());
                }
                Resource resource = new Resource(feedUrl);
                entry.initEntry(identifier, "", baseGroup, getUser(), resource, "",
                                now.getTime(), now.getTime(), now.getTime(),
                                now.getTime(), new Object[]{siteEntry.getId()});

                entry.setLocation(latitude, longitude,0);
                Metadata formatMetadata= new Metadata(getRepository().getGUID(),
                                                      entry.getId(), GsacMetadataHandler.TYPE_STREAM_FORMAT, false, 
                                                      format, formatDetails,
                                                      "", "", "");
                if(!entry.hasMetadata(formatMetadata)) {
                    entry.addMetadata(formatMetadata);
                }

                Metadata sourceMetadata=  new Metadata(getRepository().getGUID(),
                                                       entry.getId(), GsacMetadataHandler.TYPE_STREAM_SOURCE, false, 
                                                       carrier, navSystem, network, country,"");
                if(!entry.hasMetadata(sourceMetadata)) {
                    entry.addMetadata(sourceMetadata);
                }
                System.err.println ("id:" + identifier +" format:" + format +" url:" + feedUrl);
                if(newEntry) {
                    entries.add(entry);
                } else  {
                    getEntryManager().storeEntry(entry);
                }
                if(myCnt++>50) break;
            } catch(Exception exc) {
                System.err.println("Bad line:" + line);
                System.err.println("cols:" + cols);
                throw exc;
            }
        }

    }


}
