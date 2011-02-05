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

package org.gsac.gsl.output;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;


import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class GsacOutput {

    /** _more_ */
    private GsacOutputHandler outputHandler;

    /** _more_ */
    private String id;

    /** _more_ */
    private String label;

    /** _more_ */
    private boolean forUser = true;

    /** _more_ */
    private String fileSuffix;

    private String toolbarLabel;


    /**
     * _more_
     *
     * @param outputHandler _more_
     * @param id _more_
     */
    public GsacOutput(GsacOutputHandler outputHandler, String id) {
        this(outputHandler, id, id);
    }


    /**
     * _more_
     *
     * @param outputHandler _more_
     * @param id _more_
     * @param label _more_
     */
    public GsacOutput(GsacOutputHandler outputHandler, String id,
                      String label) {
        this(outputHandler, id, label, true);
    }



    /**
     * _more_
     *
     * @param outputHandler _more_
     * @param id _more_
     * @param label _more_
     * @param forUser _more_
     */
    public GsacOutput(GsacOutputHandler outputHandler, String id,
                      String label, boolean forUser) {

        this(outputHandler, id, label, null, forUser);
    }

    /**
     * _more_
     *
     * @param outputHandler _more_
     * @param id _more_
     * @param label _more_
     * @param fileSuffix _more_
     * @param forUser _more_
     */
    public GsacOutput(GsacOutputHandler outputHandler, String id,
                      String label, String fileSuffix, boolean forUser) {
        this(outputHandler, id, label, fileSuffix, forUser, null);
    }

    public GsacOutput(GsacOutputHandler outputHandler, String id,
                      String label, String fileSuffix, boolean forUser, String toolbarLabel) {
        this.outputHandler = outputHandler;
        this.id            = id;
        this.label         = label;
        this.fileSuffix    = fileSuffix;
        this.forUser       = forUser;
        this.toolbarLabel = toolbarLabel;
    }


    /**
       Set the ToolBarLabel property.

       @param value The new value for ToolBarLabel
    **/
    public void setToolbarLabel (String value) {
	toolbarLabel = value;
    }

    /**
       Get the ToolbarLabel property.

       @return The ToolbarLabel
    **/
    public String getToolbarLabel () {
	return toolbarLabel;
    }





    /**
     *  Set the OutputHandler property.
     *
     *  @param value The new value for OutputHandler
     */
    public void setOutputHandler(GsacOutputHandler value) {
        outputHandler = value;
    }

    /**
     *  Get the OutputHandler property.
     *
     *  @return The OutputHandler
     */
    public GsacOutputHandler getOutputHandler() {
        return outputHandler;
    }

    /**
     *  Set the Id property.
     *
     *  @param value The new value for Id
     */
    public void setId(String value) {
        id = value;
    }

    /**
     * _more_
     *
     * @param suffix _more_
     *
     * @return _more_
     */
    public String getProperty(String suffix) {
        return getPropertyPrefix() + suffix;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getPropertyPrefix() {
        return "output." + getId() + ".";
    }

    /**
     *  Get the Id property.
     *
     *  @return The Id
     */
    public String getId() {
        return id;
    }

    /**
     *  Set the Label property.
     *
     *  @param value The new value for Label
     */
    public void setLabel(String value) {
        label = value;
    }

    /**
     *  Get the Label property.
     *
     *  @return The Label
     */
    public String getLabel() {
        return label;
    }

    /**
     *  Set the ForUser property.
     *
     *  @param value The new value for ForUser
     */
    public void setForUser(boolean value) {
        forUser = value;
    }

    /**
     *  Get the ForUser property.
     *
     *  @return The ForUser
     */
    public boolean getForUser() {
        return forUser;
    }

    /**
     *  Set the FileSuffix property.
     *
     *  @param value The new value for FileSuffix
     */
    public void setFileSuffix(String value) {
        fileSuffix = value;
    }

    /**
     *  Get the FileSuffix property.
     *
     *  @return The FileSuffix
     */
    public String getFileSuffix() {
        return fileSuffix;
    }


}
