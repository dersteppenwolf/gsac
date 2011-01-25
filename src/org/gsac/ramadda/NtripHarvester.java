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
public class NtripHarvester extends Harvester {

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
        //        addBaseFolderToForm(request, entrySB, urlEntry, cnt);
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
        processSourceTable(urlEntry, baseGroup);
    }



    /**
     * _more_
     *
     * @param urlEntry _more_
     * @param baseGroup _more_
     *
     * @throws Exception _more_
     */
    protected void processSourceTable(HarvesterEntry urlEntry, Entry baseGroup)
            throws Exception {
        GsacSiteTypeHandler siteTypeHandler =
            (GsacSiteTypeHandler) getRepository().getTypeHandler(
                GsacSiteTypeHandler.TYPE_GSACSITE, false, false);
        //        GsacResourceTypeHandler resourceTypeHandler = 
        String url = urlEntry.getUrl();
        logHarvesterInfo("Processing source table:" + url);
        String contents = IOUtil.readContents(url, getClass(),
                                              null);
        if (contents == null) {
            logHarvesterInfo("Could not read source table:" + url);
            return;
        }
        List<String> toks       = StringUtil.split(contents, "\n", false,
                                                   false);
        User         user       = getUser();
        List<Entry>  oldEntries = new ArrayList<Entry>();
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
            List<String> cols = StringUtil.split(line, ";", true, true);
            if(cols.size()==0) continue;
            int          col            = 0;
            String type        = cols.get(col++);
            if(!type.equals(TYPE_STR)) {
                if(!type.equals(TYPE_NET)&&!type.equals(TYPE_CAS)) {
                    logHarvesterInfo("Unknown type:" +  line);
                }
                continue;
            }   
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
            double longitude     = Double.parseDouble(cols.get(col++));
            int nmea        = Integer.parseInt(cols.get(col++));
            int solution      = Integer.parseInt(cols.get(col++));
            String generator     = cols.get(col++);
            String compression     = cols.get(col++);
            String authentication      = cols.get(col++);
            String fee      = cols.get(col++);
            int bitRate      = Integer.parseInt(cols.get(col++));

            System.err.println ("feed url:" + feedUrl);

        }

    }


}
