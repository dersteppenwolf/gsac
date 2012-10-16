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

package org.gsac.gsl;


import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;

import java.lang.reflect.*;



import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Tue, Oct 5, '10
 * @author         Enter your name here...
 */
public class GsacDocs implements GsacArgs, GsacExtArgs {



    /**
     * _more_
     *
     * @param arg _more_
     * @param c _more_
     *
     * @return _more_
     */
    public String findValue(String arg, Class c) {
        try {
            Field f = c.getDeclaredField(arg);
            if (f == null) {
                return null;
            }

            return "" + f.get(this);
        } catch (IllegalAccessException iae) {
            return null;
        } catch (NoSuchFieldException exc) {
            return null;
        }

    }

    /**
     * _more_
     *
     * @param arg _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public String findValue(String arg) throws Exception {
        String v = findValue(arg, GsacArgs.class);
        if (v == null) {
            v = findValue(arg, GsacExtArgs.class);
        }
        if (v == null) {
            throw new IllegalArgumentException("Unknown argument name:"
                    + arg);
        }

        return v;
    }

    /**
     * _more_
     *
     * @param argName _more_
     * @param type _more_
     * @param desc _more_
     *
     * @throws Exception On badness
     */
    public void process(String argName, String type, String desc)
            throws Exception {
        String argValue = findValue(argName);
        if (type.startsWith("enum")) {
            List<String> toks = StringUtil.split(type, ",", true, true);
            if (toks.size() > 1) {
                toks.remove(0);
                type = "enumeration values=" + StringUtil.join(", ", toks);
            } else {
                type = "enumeration";
            }
        } else if (type.equals("boolean")) {
            type = "boolean: true, false";
        } else if (type.equals("mstring")) {
            type = "Comma delimited set of string values. If a string value has a \"!\" prefix then this is a negation.";
        } else if (type.equals("lat")) {
            type = "Decimal degrees latitude. -90.0 - 90.0";
        } else if (type.equals("lon")) {
            type = "Decimal degrees east longitude. -180 - 180";
        } else if (type.equals("date")) {
            type = "Date string, e.g., yyyy-mm-dd,  now, -1 week, +3 days, etc.";
        }
        System.out.println("<b>Argument: </b>" + argValue + "<br>");
        System.out.println("<b>Variable: </b>" + argName + "<br>");
        System.out.println("<b>Type: </b>" + type + "<br>");
        System.out.println(desc);
        System.out.println("<p>");

    }

    /**
     * _more_
     *
     * @throws Exception On badness
     */
    public void process() throws Exception {
        System.out.println(
            "<html><head><style type=\"text/css\">body {font-family: Arial, Helvetica, sans-serif;}</style><title>GSACWS API Arguments</head><body>");
        //        System.out.println("<table>");
        //        System.out.println("<tr><td><b>Argument</b></td><td><b>Variable</b></td><td><b>Type</b></td></tr>");
        //ARG_SITE_ID;mstring;The unique repository specific site identifier
        for (String line :
                StringUtil.split(
                    IOUtil.readContents(
                        "/org/gsac/gsl/args.txt", getClass()), "\n", true,
                            true)) {
            List<String> toks = StringUtil.split(line, ";", true, true);
            if (toks.get(0).equals("HEADER")) {
                System.out.println("<h2>" + toks.get(1) + "</h2>");

                continue;
            }

            if (toks.size() < 2) {
                throw new IllegalArgumentException("Bad line:" + line);
            }
            String arg  = toks.get(0);
            String type = ((toks.size() >= 2)
                           ? toks.get(1)
                           : "");
            String desc = ((toks.size() >= 3)
                           ? toks.get(2)
                           : "");
            process(arg, type, desc);
        }
        //        System.out.println("</table>");
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception On badness
     */
    public static void main(String[] args) throws Exception {
        GsacDocs docs = new GsacDocs();
        docs.process();
    }

}
