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

package org.gsac.gsl.util;

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


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class NtripUtil {

    /** _more_ */
    public static final String TYPE_STR = "STR";

    /** _more_ */
    public static final String TYPE_CAS = "CAS";

    /** _more_ */
    public static final String TYPE_NET = "NET";




    /**
     * _more_
     *
     * @param urlEntry _more_
     * @param baseGroup _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    public static List<List<String>> readSourceTable(String url) 
        throws Exception {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        System.err.println("Processing source table:" + url);
        String contents = IOUtil.readContents(url, NtripUtil.class, null);
        if (contents == null) {
            System.err.println("Could not read url:" + url);
            return null;
        }
        //Don't ask...
        contents = contents.replaceAll("<br>", "");
        List<String> toks = StringUtil.split(contents, "\n", false, false);

        List<List<String>> results = new ArrayList();
        int                      myCnt   = 0;
        int                      lineCnt = 0;
        boolean                  tooMany = toks.size() > 100;
        for (; lineCnt < toks.size(); lineCnt++) {
            String line = toks.get(lineCnt).trim();
            if (lineCnt == 0) {
                if ( !line.startsWith("SOURCETABLE ")) {
                    System.err.println("Bad source table:" + line);
                    return null;
                }
            }
            if (line.length() == 0) {
                lineCnt++;
                break;
            }

        }
        for (; lineCnt < toks.size(); lineCnt++) {
            String line = toks.get(lineCnt).trim();
            if (line.equals("ENDSOURCETABLE")) {
                break;
            }
            List<String> cols = StringUtil.split(line, ";", false, false);
            if (cols.size() == 0) {
                continue;
            }
            int    col  = 0;
            String type = cols.get(col++);
            if ( !type.equals(TYPE_STR)) {
                if ( !type.equals(TYPE_NET) && !type.equals(TYPE_CAS)) {
                    System.err.println("Unknown type:" + line);
                }
                continue;
            }
            results.add(cols);
        }
        return results;
    }


}
