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

import ucar.unidata.util.Misc;

import org.gsac.gsl.metadata.*;
import org.gsac.gsl.util.*;

import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;


/**
 * Holds gnss equipment metadata
 *
 */
public class GnssEquipment extends GsacMetadata {

    /** type id */
    public static final String TYPE_GNSS_EQUIPMENT = "gnss.equipment";

    /** date range */
    private Date fromDate;

    /** date range */
    private Date toDate;

    /** _more_ */
    private String antenna;

    /** _more_ */
    private String antennaSerial;

    /** _more_ */
    private String dome;

    /** _more_ */
    private String domeSerial;

    /** _more_ */
    private String receiver;

    /** _more_ */
    private String receiverSerial;

    /** _more_ */
    private String receiverFirmware;

    private String firmwareVersion;

    private String satelliteSystem;

    /** _more_ */
    private double[] xyzOffset = { 0, 0, 0 };

    /**
     * _more_
     */
    public GnssEquipment() {}


    /**
     * _more_
     *
     * @param dateRange _more_
     * @param antenna _more_
     * @param antennaSerial _more_
     * @param dome _more_
     * @param domeSerial _more_
     * @param receiver _more_
     * @param receiverSerial _more_
     * @param receiverFirmware _more_
     * @param zOffset _more_
     */
    public GnssEquipment(Date[] dateRange, String antenna,
                         String antennaSerial, String dome,
                         String domeSerial, String receiver,
                         String receiverSerial, String receiverFirmware,
                         double zOffset) {
        this(dateRange, antenna, antennaSerial, dome, domeSerial, receiver,
             receiverSerial, receiverFirmware, new double[] { 0,
                0, zOffset });
    }

    public static final List<GnssEquipment> sort(List<GnssEquipment> equipment) {
        List<GnssEquipment> sorted = new ArrayList<GnssEquipment>(equipment);
        Comparator comp = new Comparator() {
                public int compare(Object o1,Object o2 ) {
                    GnssEquipment equip1 = (GnssEquipment)o1;
                    GnssEquipment equip2 = (GnssEquipment)o2;
                    if(Misc.equals(equip1.getFromDate(), equip2.getFromDate())) {
                        return compare(equip1.getToDate(), equip2.getToDate());
                    }
                    return compare(equip1.getFromDate(), equip2.getFromDate());
                }
                private int compare(Date d1, Date d2) {
                    if(d1 == null) return (d2==null?0:1);
                    if(d2==null) return -1;
                    return d1.compareTo(d2);
                }
            };
        Collections.sort(sorted, comp);
        return sorted;
    }


    /**
     * _more_
     *
     *
     * @param dateRange _more_
     * @param antenna _more_
     * @param antennaSerial _more_
     * @param dome _more_
     * @param domeSerial _more_
     * @param receiver _more_
     * @param receiverSerial _more_
     * @param receiverFirmware _more_
     * @param xyzOffset _more_
     */
    public GnssEquipment(Date[] dateRange, String antenna,
                         String antennaSerial, String dome,
                         String domeSerial, String receiver,
                         String receiverSerial, String receiverFirmware,
                         double[] xyzOffset) {
        super(TYPE_GNSS_EQUIPMENT);
        this.fromDate         = dateRange[0];
        this.toDate           = dateRange[1];
        this.antenna          = antenna;
        this.antennaSerial    = antennaSerial;
        this.dome             = dome;
        this.domeSerial       = domeSerial;
        this.receiver         = receiver;
        this.receiverSerial   = receiverSerial;
        this.receiverFirmware = receiverFirmware;
        this.xyzOffset[0]     = xyzOffset[0];
        this.xyzOffset[1]     = xyzOffset[1];
        this.xyzOffset[2]     = xyzOffset[2];
    }



    /**
     * _more_
     *
     * @param metadata _more_
     *
     * @return _more_
     */
    public static List<GnssEquipment> getMetadata(
            List<GsacMetadata> metadata) {
        return (List<GnssEquipment>) findMetadata(metadata,
                GnssEquipment.class);
    }


    /**
     *  Set the FromDate property.
     *
     *  @param value The new value for FromDate
     */
    public void setFromDate(Date value) {
        fromDate = value;
    }

    /**
     *  Get the FromDate property.
     *
     *  @return The FromDate
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     *  Set the ToDate property.
     *
     *  @param value The new value for ToDate
     */
    public void setToDate(Date value) {
        toDate = value;
    }

    /**
     *  Get the ToDate property.
     *
     *  @return The ToDate
     */
    public Date getToDate() {
        return toDate;
    }
    

    public boolean hasAntenna() {
        return antenna!=null && antenna.length()>0;
    }

    public boolean hasReceiver() {
        return receiver!=null && receiver.length()>0;
    }



    /**
     *  Set the Antenna property.
     *
     *  @param value The new value for Antenna
     */
    public void setAntenna(String value) {
        antenna = value;
    }

    /**
     *  Get the Antenna property.
     *
     *  @return The Antenna
     */
    public String getAntenna() {
        return antenna;
    }

    /**
     *  Set the AntennaSerial property.
     *
     *  @param value The new value for AntennaSerial
     */
    public void setAntennaSerial(String value) {
        antennaSerial = value;
    }

    /**
     *  Get the AntennaSerial property.
     *
     *  @return The AntennaSerial
     */
    public String getAntennaSerial() {
        return antennaSerial;
    }

    /**
     *  Set the Dome property.
     *
     *  @param value The new value for Dome
     */
    public void setDome(String value) {
        dome = value;
    }

    /**
     *  Get the Dome property.
     *
     *  @return The Dome
     */
    public String getDome() {
        return dome;
    }

    /**
     *  Set the DomeSerial property.
     *
     *  @param value The new value for DomeSerial
     */
    public void setDomeSerial(String value) {
        domeSerial = value;
    }

    /**
     *  Get the DomeSerial property.
     *
     *  @return The DomeSerial
     */
    public String getDomeSerial() {
        return domeSerial;
    }

    /**
     *  Set the Receiver property.
     *
     *  @param value The new value for Receiver
     */
    public void setReceiver(String value) {
        receiver = value;
    }

    /**
     *  Get the Receiver property.
     *
     *  @return The Receiver
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     *  Set the ReceiverSerial property.
     *
     *  @param value The new value for ReceiverSerial
     */
    public void setReceiverSerial(String value) {
        receiverSerial = value;
    }

    /**
     *  Get the ReceiverSerial property.
     *
     *  @return The ReceiverSerial
     */
    public String getReceiverSerial() {
        return receiverSerial;
    }

    /**
     *  Set the ReceiverFirmware property.
     *
     *  @param value The new value for ReceiverFirmware
     */
    public void setReceiverFirmware(String value) {
        receiverFirmware = value;
    }

    /**
     *  Get the ReceiverFirmware property.
     *
     *  @return The ReceiverFirmware
     */
    public String getReceiverFirmware() {
        return receiverFirmware;
    }

    /**
     *  Set the XyzOffset property.
     *
     *  @param value The new value for XyzOffset
     */
    public void setXyzOffset(double[] value) {
        xyzOffset = value;
    }

    /**
     *  Get the XyzOffset property.
     *
     *  @return The XyzOffset
     */
    public double[] getXyzOffset() {
        return xyzOffset;
    }





    /**
       Set the FirmwareVersion property.

       @param value The new value for FirmwareVersion
    **/
    public void setFirmwareVersion (String value) {
	firmwareVersion = value;
    }

    /**
       Get the FirmwareVersion property.

       @return The FirmwareVersion
    **/
    public String getFirmwareVersion () {
	return firmwareVersion;
    }

    /**
       Set the SatelliteSystem property.

       @param value The new value for SatelliteSystem
    **/
    public void setSatelliteSystem (String value) {
	satelliteSystem = value;
    }

    /**
       Get the SatelliteSystem property.

       @return The SatelliteSystem
    **/
    public String getSatelliteSystem () {
	return satelliteSystem;
    }




}
