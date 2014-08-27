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

package org.gsac.gsl.metadata;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.output.*;

import java.io.IOException;



/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class PoliticalLocationMetadata extends GsacMetadata {

    /** _more_ */
    public static final String TYPE_POLITICALLOCATION = "politicallocation";

    /** _more_ */
    private String country;

    /** _more_ */
    private String state;

    /** _more_ */
    private String city;



    /**
     * _more_
     */
    public PoliticalLocationMetadata() {
        super(TYPE_POLITICALLOCATION);
    }


    /**
     *  shows nation, region or state, and city or place in html output pages
     *
     * changed country -> nation and  state -> region/state, which are all cognates in Italian, French, German, Spanish and Portuguese
     *
     * @param request _more_
     * @param gsacResource _more_
     * @param outputHandler _more_
     * @param pw _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public boolean addHtml(GsacRequest request, GsacResource gsacResource,
                           HtmlOutputHandler outputHandler, Appendable pw)
            throws IOException {
        if (country != null) {
            pw.append(outputHandler.formEntry(request,
                    outputHandler.msgLabel("Nation"), country));
        }

        if (state != null) {
            pw.append(outputHandler.formEntry(request,
                    //outputHandler.msgLabel("Province/State"), state)); // or LOOK for other forms of GSAC
                    outputHandler.msgLabel("Locale"), state)); // LOOK:  for dataworks GSAC, use locale as name for province / state
        }

        if (city != null) {
            pw.append(outputHandler.formEntry(request,
                    outputHandler.msgLabel("Place/city"), city)); 
        }

        return true;
    }

    /**
     * _more_
     *
     * @param country _more_
     * @param state _more_
     * @param city _more_
     */
    public PoliticalLocationMetadata(String country, String state,
                                     String city) {
        super(TYPE_POLITICALLOCATION);
        this.city    = city;
        this.state   = state;
        this.country = country;
    }


    /**
     *  Set the Country property.
     *
     *  @param value The new value for Country
     */
    public void setCountry(String value) {
        country = value;
    }

    /**
     *  Get the Country property.
     *
     *  @return The Country
     */
    public String getCountry() {
        return country;
    }

    /**
     *  Set the State property.
     *
     *  @param value The new value for State
     */
    public void setState(String value) {
        state = value;
    }

    /**
     *  Get the State property.
     *
     *  @return The State
     */
    public String getState() {
        return state;
    }

    /**
     *  Set the City property.
     *
     *  @param value The new value for City
     */
    public void setCity(String value) {
        city = value;
    }

    /**
     *  Get the City property.
     *
     *  @return The City
     */
    public String getCity() {
        return city;
    }


}
