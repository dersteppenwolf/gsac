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

package org.gsac.gsl.metadata.gnss;


import org.gsac.gsl.*;

import org.gsac.gsl.metadata.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.HtmlOutputHandler;
import org.gsac.gsl.util.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.io.IOException;

import java.util.Date;
import java.util.List;


/**
 * Holds a group of gnss equipment metadata. All this does is overrides
 * the addHtml method to format its children GnssEquipment in a table
 *
 */
public class GnssEquipmentGroup extends MetadataGroup {


    /**
     * ctor
     */
    public GnssEquipmentGroup() {}


    /**
     * format the children metatada for html
     *
     * @param request the request
     * @param gsacResource the resource
     * @param outputHandler html handler
     * @param pw appendable to append to
     *
     * @return true
     *
     * @throws IOException On appendable badness
     */
    public boolean addHtml(GsacRequest request, GsacResource gsacResource,
                           HtmlOutputHandler outputHandler, Appendable pw)
            throws IOException {
        GsacResourceManager resourceManager =
            outputHandler.getResourceManager(gsacResource);

        int          cnt  = 0;
        double lastHeight = 0.0;
        StringBuffer buff = new StringBuffer(HtmlUtil.formTable());
        String[]values = {"","","","","",""};
        List<GnssEquipment> equipmentList =  GnssEquipment.getMetadata(getMetadata());


        /*
        //fill in the default values
        for(int i=equipmentList.size()-1;i>=0;i--) {
            GnssEquipment equipment  = equipmentList.get(i);
            getValue(equipment.getAntenna(), values,0);
            getValue(equipment.getAntennaSerial(), values,1);
            getValue(equipment.getDome(), values,2);
            getValue(equipment.getDomeSerial(), values, 3);
            getValue(equipment.getReceiver(), values, 4);
            getValue(equipment.getReceiverSerial(), values,5);
            }*/




         for (GnssEquipment equipment :equipmentList) {
            if (cnt == 0) {
                buff.append(outputHandler.tableHeader(new String[] {
                    outputHandler.msg("Date"),
                    outputHandler.msg("Antenna"), 
                    outputHandler.msg("Dome"),
                    outputHandler.msg("Antenna Height"),
                    outputHandler.msg("Receiver"),
                    outputHandler.msg("Firmware"), }));
            }
            cnt++;

            buff.append("<tr valign=top>");
            buff.append("<td>&nbsp;");
            String dateString =
                outputHandler.formatDateTime(equipment.getFromDate()) + " - "
                + outputHandler.formatDateTime(equipment.getToDate(),"Current");
            //Add link to search for sites
            if (gsacResource instanceof GsacSite) {
                GsacSite site = (GsacSite) gsacResource;
                dateString = HtmlUtil.href(
                    HtmlUtil.url(
                        outputHandler.makeUrl(URL_FILE_FORM), new String[] {
                    resourceManager.getIdUrlArg(), site.getId(),
                    ARG_SITE_CODE, site.getShortName(),
                    ARG_FILE_DATADATE_FROM,
                    outputHandler.formatDateTime(equipment.getFromDate()),
                    ARG_FILE_DATADATE_TO,
                    outputHandler.formatDateTime(equipment.getToDate(), "Current")
                }), dateString);
            }

            buff.append(dateString);
            buff.append("&nbsp;</td>");
            equipmentRow(buff, getValue(equipment.getAntenna(), values,0),
                         getValue(equipment.getAntennaSerial(), values,1));
            equipmentRow(buff, getValue(equipment.getDome(), values,2),
                         getValue(equipment.getDomeSerial(), values, 3));
            buff.append("<td>&nbsp;");
            //                buff.append(equipment.getXyzOffset()[0] + "/"
            //                            + equipment.getXyzOffset()[1] + "/"
            //                            + equipment.getXyzOffset()[2]);

            double height = equipment.getXyzOffset()[2];
            if(Double.isNaN(height)) {
                height = lastHeight;
            } 
            buff.append("" + height);
            lastHeight = height;

            buff.append("&nbsp;</td>");
            equipmentRow(buff, getValue(equipment.getReceiver(), values, 4),
                         getValue(equipment.getReceiverSerial(), values,5));
            equipmentRow(buff, getValue(equipment.getReceiverFirmware(), values, 4),"");

            buff.append("</tr>");
        }
        if (cnt > 0) {
            buff.append(HtmlUtil.formTableClose());
            pw.append(outputHandler.formEntryTop(request,
                    outputHandler.msgLabel("Equipment"),
                    HtmlUtil.makeShowHideBlock("", buff.toString(), false)));
        }



        return true;
    }

    private String getValue(String value, String[]tuple,int index) {
        if(value==null || value.length()==0) {
            return "";
//            return tuple[index];
        }
        tuple[index] = value;
        return value;
    }


    /**
     * _more_
     *
     * @param buff _more_
     * @param name _more_
     * @param serial _more_
     *
     * @throws IOException On badness
     */
    private void equipmentRow(Appendable buff, String name, String serial)
            throws IOException {
        buff.append("<td>&nbsp;");
        if(name == null) name = "-";
        else if(name.length() == 0) name =  "-";
        buff.append(name);
        if (serial != null&& serial.length()>0) {
            buff.append("<br><i>#" + serial + "</i>");
        }
        buff.append("&nbsp;</td>");

    }



}
