/*
 * Copyright 2010-2016 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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


import org.gsac.gsl.model.*;


import org.gsac.gsl.output.GsacOutputHandler;
import org.gsac.gsl.util.Vocabulary;


import ucar.unidata.util.DateUtil;
import ucar.unidata.util.Misc;

import java.io.IOException;

import java.io.OutputStream;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This class wraps a HttpServletRequest/HttpServletResponse pair
 * and provides a number of higher-level methods for accessing URL arguments,
 * etc.
 *
 *
 * @author  Jeff McWhirter (jeffmc@unavco.org)
 */
public class GsacRequest implements GsacConstants {

    /** _more_ */
    public static final String DELIMITER = ";";

    /** _more_ */
    private static List<SimpleDateFormat> formats;


    /** the original  request */
    private HttpServletRequest httpServletRequest;

    /** the original  response */
    private HttpServletResponse httpServletResponse;

    /** url params */
    private Hashtable parameters = new Hashtable();

    /** extra properties */
    private Hashtable properties = new Hashtable();

    /** _more_ */
    private GsacRepository gsacRepository;

    /** _more_ */
    private String gsacUrlPath;

    /** _more_ */
    private boolean useVocabulary = true;

    /** _more_ */
    private boolean isMobile = false;

    /** _more_ */
    private Hashtable httpHeader = new Hashtable();

    /** _more_ */
    private String sqlWhereSuffix="";


    /**
     * ctor
     */
    public GsacRequest() {}


    /**
     * ctor
     *
     * @param gsacRepository _more_
     * @param parameters _more_
     */
    public GsacRequest(GsacRepository gsacRepository, Hashtable parameters) {
        this.gsacRepository = gsacRepository;
        this.parameters     = parameters;
    }


    /**
     * copy ctor
     *
     * @param that request to copy
     */
    public GsacRequest(GsacRequest that) {
        this.gsacRepository      = that.gsacRepository;
        this.httpServletRequest  = that.httpServletRequest;
        this.httpServletResponse = that.httpServletResponse;
        this.parameters          = new Hashtable(that.parameters);
        this.properties          = new Hashtable(that.properties);
        this.httpHeader          = that.httpHeader;
        this.isMobile            = that.isMobile;
    }

    /**
     * ctor
     *
     *
     * @param gsacRepository _more_
     * @param httpServletRequest the request to wrap
     * @param httpServletResponse the response to wrap
     */
    public GsacRequest(GsacRepository gsacRepository,
                       HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse) {
        this.gsacRepository      = gsacRepository;
        this.httpServletRequest  = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
        initArgs();
    }


    /**
     *  Set the UseVocabulary property.
     *
     *  @param value The new value for UseVocabulary
     */
    public void setUseVocabulary(boolean value) {
        useVocabulary = value;
    }

    /**
     *  Get the UseVocabulary property.
     *
     *  @return The UseVocabulary
     */
    public boolean getUseVocabulary() {
        return useVocabulary;
    }

    /**
     *  Set the sqlWhereSuffix 
     *
     *  @param value set a new value for sqlWhereSuffix
     */
    public void setsqlWhereSuffix(String value) {
        sqlWhereSuffix= value;
    }

    /**
     *  Get the sqlWhereSuffix
     *
     *  @return  sqlWhereSuffix
     */
    public String getsqlWhereSuffix() {
        return sqlWhereSuffix;
    }



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws IOException On badness
     */
    public OutputStream getOutputStream() throws IOException {
        return httpServletResponse.getOutputStream();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isFederatedRequest() {
        return defined(ARG_REMOTEREPOSITORY);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getOriginatingIP() {
        if (defined(ARG_REQUEST_IP)) {
            return get(ARG_REQUEST_IP, "");
        }

        return getRequestIP();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getRequestIP() {
        return httpServletRequest.getRemoteAddr();

    }

    /**
     * _more_
     *
     * @param code _more_
     * @param message _more_
     *
     * @throws IOException On badness
     */
    public void sendError(int code, String message) throws IOException {
        httpServletResponse.sendError(code, message);
    }

    /**
     * read the url args
     */
    private void initArgs() {
        Map      p  = httpServletRequest.getParameterMap();
        Iterator it = p.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String    key   = (String) pairs.getKey();
            String[]  vals  = (String[]) pairs.getValue();
            if (vals.length == 1) {
                parameters.put(key,
                               gsacRepository.toRepositoryNamespace(key,
                                   vals[0]));
            } else if (vals.length > 1) {
                List values = new ArrayList();
                for (int i = 0; i < vals.length; i++) {
                    values.add(gsacRepository.toRepositoryNamespace(key,
                            vals[i]));
                }
                parameters.put(key, values);
            }
        }

        for (Enumeration headerNames = httpServletRequest.getHeaderNames();
                headerNames.hasMoreElements(); ) {
            String name  = (String) headerNames.nextElement();
            String value = httpServletRequest.getHeader(name);
            httpHeader.put(name, value);
        }


        String ua = getUserAgent("").toLowerCase();
        isMobile = (ua.indexOf("iphone") >= 0)
                   || (ua.indexOf("android") >= 0);

    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSpider() {
        String userAgent = getUserAgent(null);
        if (userAgent == null) {
            return false;
        }
        userAgent = userAgent.toLowerCase();

        return ((userAgent.indexOf("googlebot") >= 0)
                || (userAgent.indexOf("slurp") >= 0)
                || (userAgent.indexOf("spider") >= 0)
                || (userAgent.indexOf("crawler") >= 0)
                || (userAgent.indexOf("bot") >= 0));

    }


    /**
     * _more_
     *
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getUserAgent(String dflt) {
        String value = getHeaderArg("User-Agent");
        if (value == null) {
            System.err.println("no user agent");

            return dflt;
        }

        return value;
    }


    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getHeaderArg(String name) {
        if (httpHeader == null) {
            return null;
        }
        String arg = (String) httpHeader.get(name);
        if (arg == null) {
            arg = (String) httpHeader.get(name.toLowerCase());
        }

        return arg;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isMobile() {
        //        if(true) return true;
        return isMobile;
    }


    /**
     * put an extra property. This is different from the
     * put and get methods which access the url arguments.
     * this can be used to carry around extra stuff on the request
     *
     * @param key property key
     * @param value property value
     */
    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    /**
     * get the extra property. Note: this is not the url arguments. Rather,
     * this is for accessing extra interal properties
     *
     * @param key property key
     *
     * @return extra property value
     */
    public Object getProperty(Object key) {
        return properties.get(key);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getMethod() {
        return httpServletRequest.getMethod();
    }

    /**
     * get the uri path
     *
     * @return uri path
     */
    public String getRequestURI() {
        return httpServletRequest.getRequestURI();
    }

    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public boolean isGsacUrl(String url) {
        return getGsacUrlPath().startsWith(url);
    }

    /**
     * get the /gsacws/... part of the url path
     *
     * @return _more_
     */
    public String getGsacUrlPath() {
        if (gsacUrlPath == null) {
            String uri   = getRequestURI();
            int    index = uri.lastIndexOf(URL_BASE);
            if (index >= 0) {
                gsacUrlPath = uri.substring(index);
            }
        }

        return gsacUrlPath;
    }

    /**
     * add in a new url argument to the parameters
     *
     * @param key url argument
     * @param value value_
     */
    public void put(String key, Object value) {
        parameters.put(key, value);
    }

    /**
     * _more_
     *
     * @param key _more_
     */
    public void remove(String key) {
        parameters.remove(key);
    }

    /**
     * _more_
     *
     * @param nameOverride _more_
     * @param valueOverride _more_
     *
     * @return _more_
     */
    public String getUrlArgs(String nameOverride, String valueOverride) {
        Hashtable<String, String> override = new Hashtable<String, String>();
        override.put(nameOverride, valueOverride);

        return getUrlArgs(override);
    }


    /**
     * stringify the url arguments of the form:<pre>
     * arg1=value1&arg2=value2...
     * </pre>
     *
     * @return url args stringified
     */
    public String getUrlArgs() {
        return getUrlArgs(null);
    }

    /**
     * _more_
     *
     * @param overrideArgs _more_
     *
     * @return _more_
     */
    public String getUrl(Hashtable<String, String> overrideArgs) {
        String args = getUrlArgs(overrideArgs);
        if (args.length() == 0) {
            return getRequestURI();
        } else {
            return getRequestURI() + "?" + args;
        }
    }


    /**
     * stringify the url arguments of the form:<pre>
     * arg1=value1&arg2=value2...
     * </pre>
     *
     * @param overrideArgs if non-null then if there is a url argument
     *  that is also in overrideArgs then use the value in overrideArgs
     *
     * @return url args stringified
     */
    public String getUrlArgs(Hashtable<String, String> overrideArgs) {
        return getUrlArgs(overrideArgs, new HashSet());
    }

    /**
     * _more_
     *
     * @param overrideArgs _more_
     * @param exceptArgs _more_
     *
     * @return _more_
     */
    public String getUrlArgs(Hashtable<String, String> overrideArgs,
                             HashSet exceptArgs) {
        exceptArgs.add(ARG_SEARCH);
        Hashtable    exceptValues      = null;
        String       exceptArgsPattern = null;

        StringBuffer sb                = new StringBuffer();
        int          cnt               = 0;
        Hashtable[]  tables = new Hashtable[] { overrideArgs, parameters };

        for (Hashtable table : tables) {
            if (table == null) {
                continue;
            }
            for (Enumeration keys = table.keys(); keys.hasMoreElements(); ) {
                String arg = (String) keys.nextElement();

                if (exceptArgs.contains(arg)) {
                    continue;
                }

                if ((exceptArgsPattern != null)
                        && arg.matches(exceptArgsPattern)) {
                    continue;
                }
                Object value = null;
                if ((overrideArgs != null) && (table != overrideArgs)) {
                    if (overrideArgs.get(arg) != null) {
                        continue;
                    }
                }
                if (value == null) {
                    value = table.get(arg);
                }
                if ((exceptValues != null)
                        && (exceptValues.get(value) != null)) {
                    continue;
                }
                if (value instanceof List) {
                    List l = (List) value;
                    if (l.size() == 0) {
                        continue;
                    }
                    for (int i = 0; i < l.size(); i++) {
                        String svalue = (String) l.get(i);
                        if (svalue.length() == 0) {
                            continue;
                        }
                        if (cnt++ > 0) {
                            sb.append("&");
                        }
                        sb.append(arg + "=" + svalue);
                    }

                    continue;
                }
                String svalue = value.toString();
                if (svalue.length() == 0) {
                    continue;
                }
                if (cnt++ > 0) {
                    sb.append("&");
                }
                try {
                    svalue = java.net.URLEncoder.encode(svalue, "UTF-8");
                } catch (Exception exc) {  /*noop*/
                }
                sb.append(arg + "=" + svalue);
            }
        }

        return sb.toString();
    }



    /**
     * Return the url argument specified ny key. If not defined then return dflt
     *
     * @param key url argument
     * @param dflt the default value
     *
     * @return argument value of key or dflt if not defined
     */
    private Object getValue(String key, Object dflt) {
        Object result = parameters.get(key);
        if (result == null) {
            return dflt;
        }

        Vocabulary vocabulary = (useVocabulary
                                 ? gsacRepository.getVocabulary(key)
                                 : null);
        if (vocabulary != null) {
            if (result instanceof List) {
                return gsacRepository.convertToInternal(vocabulary, key,
                        (List<String>) result);
            } else if (result instanceof String) {
                List<String> tmp = new ArrayList<String>();
                tmp.add((String) result);
                List<String> tmpResult =
                    gsacRepository.convertToInternal(vocabulary, key, tmp);
                if (tmpResult.size() == 1) {
                    return tmpResult.get(0);
                }

                return tmpResult;
            }
        }


        return result;
    }



    /**
     * Is there a non zero length url argument
     *
     * @param key url argument
     *
     * @return is argument defined
     */
    public boolean defined(String key) {
        if (key == null) {
            return false;
        }
        Object result = getValue(key, (Object) null);
        if (result == null) {
            return false;
        }
        if (result instanceof List) {
            return ((List) result).size() > 0;
        }
        String sresult = (String) result;
        if (sresult.length() == 0) {
            return false;
        }

        return true;
    }


    /**
     * Get the url arg value if defined. If not defined return dflt
     *
     * @param key url argument
     * @param dflt the default value
     *
     * @return argument value of key or dflt if not defined
     */
    public String get(String key, String dflt) {
        Object o = getValue(key, (String) null);
        if (o == null) {
            return dflt;
        }
        String result;
        if (o instanceof List) {
            List l = (List) o;
            if (l.size() == 0) {
                return dflt;
            }
            result = l.get(0).toString();
        } else {
            result = o.toString();
        }
        if ((result == null) || (result.length() == 0)) {
            return dflt;
        }

        return result;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return getUrl(null);
    }


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public List<String> getDelimiterSeparatedList(String key) {
        List<String> values = (List<String>) getList(key);
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            for (String subValue : value.split(DELIMITER)) {
                //                subValue = subValue.trim();
                if (subValue.length() > 0) {
                    result.add(subValue);
                }
            }
        }

        return gsacRepository.convertToInternal(key, result);
    }





    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public List<String> getUpperAndLowerCaseDelimiterSeparatedList(
            String key) {
        List<String> values = (List<String>) getList(key);
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            for (String subValue : value.split(DELIMITER)) {
                //NOTE: Should we trim here?
                //                subValue = subValue.trim();
                if (subValue.length() > 0) {
                    result.add(subValue);
                    result.add(subValue.toLowerCase());
                    result.add(subValue.toUpperCase());
                }
            }
        }

        return result;
    }


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public List getList(String key) {
        return get(key, new ArrayList());
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public double getLatLon(String key, double dflt) {
        String result = (String) getValue(key, (String) null);
        if ((result == null) || (result.trim().length() == 0)) {
            return dflt;
        }

        double v = Misc.decodeLatLon(result);
        if (Double.isNaN(v)) {
            throw new IllegalArgumentException("Bad argument value:" + result
                    + " for arg:" + key);
        }

        return v;
    }


    /**
     * Get the url arg value if defined. If not defined return dflt
     *
     * @param key url argument
     * @param dflt the default value
     *
     * @return argument value of key or dflt if not defined
     */
    public double get(String key, double dflt) {
        String result = (String) getValue(key, (String) null);
        if ((result == null) || (result.trim().length() == 0)) {
            return dflt;
        }
        double multiplier = 1.0;
        if (result.endsWith("K")) {
            multiplier = 1000;
            result     = result.substring(0, result.length() - 1).trim();
        }

        return new Double(result).doubleValue() * multiplier;
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean get(String key, boolean dflt) {
        String result = (String) getValue(key, (String) null);
        if ((result == null) || (result.trim().length() == 0)) {
            return dflt;
        }

        return result.equals("true");
    }

    /**
     * Get the url arg value if defined. If not defined return dflt
     *
     * @param key url argument
     * @param dflt the default value
     *
     * @return argument value of key or dflt if not defined
     */
    public int get(String key, int dflt) {
        String result = (String) getValue(key, (String) null);
        if ((result == null) || (result.trim().length() == 0)) {
            return dflt;
        }
        result = result.toLowerCase().trim();
        int multiplier = 1;
        if (result.endsWith("k")) {
            multiplier = 1000;
            result     = result.substring(0, result.length() - 1).trim();
        } else if (result.endsWith("mb")) {
            multiplier = 1000 * 1000;
            result     = result.substring(0, result.length() - 2).trim();
        } else if (result.endsWith("gb")) {
            multiplier = 1000 * 1000 * 1000;
            result     = result.substring(0, result.length() - 2).trim();
        }

        return new Integer(result).intValue() * multiplier;
    }

    /**
     * get a list of url argument values
     *
     * @param key url argument
     * @param dflt the default value
     *
     * @return list of argument values
     */
    public List get(String key, List dflt) {
        Object o = getValue(key, null);
        if (o == null) {
            return dflt;
        }
        if (o instanceof List) {
            return (List) o;
        }
        List l = new ArrayList();
        l.add(o);

        return l;
    }




    /**
     *  Get the HttpServletRequest property  that this request wraps
     *
     *  @return The HttpServletRequest
     */
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    /**
     * Get the HttpServletResponse that this request wraps
     *
     * @return the response
     */
    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getServerName() {
        return getHttpServletRequest().getServerName();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getServerPort() {
        return getHttpServletRequest().getServerPort();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getSiteAscending() {
        return get(ARG_SITE_SORT_ORDER,
                   SORT_ORDER_ASCENDING).equals(SORT_ORDER_ASCENDING);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getResourceAscending() {
        return get(ARG_FILE_SORT_ORDER,
                   SORT_ORDER_ASCENDING).equals(SORT_ORDER_ASCENDING);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getLimit() {
        int limit = get(ARG_LIMIT, DEFAULT_LIMIT);
        // debug: shows actual limit to how many items returned from a query:
        // System.err.println("     GsacRequest:getLimit() : limit=" + limit+"."); //  MAX_LIMIT, ARG_LIMIT, DEFAULT_LIMIT
        // or, what for?:
        // return Math.min(limit, MAX_LIMIT);

        return limit;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getOffset() {
        int offset = get(ARG_OFFSET, 0);

        return offset;
    }



    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getDateSelect(String name, String dflt) {
        String v = get(name, (String) null);
        if (v == null) {
            return dflt;
        }
        if (defined(name + ".time")) {
            v = v + " " + get(name + ".time", "");
        }

        //TODO:Check value
        return v;
    }


    /**
     * _more_
     *
     * @param from _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Date getDate(String from, Date dflt) throws Exception {
        if ( !defined(from)) {
            return dflt;
        }
        String dateString = (String) getDateSelect(from, "").trim();

        return parseDate(dateString);
    }


    /**
     * _more_
     *
     * @param from _more_
     * @param to _more_
     * @param relativeArg _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws java.text.ParseException On badness
     */
    public Date[] getDateRange(String from, String to, String relativeArg,
                               Date dflt)
            throws java.text.ParseException {
        String fromDate = "";
        String toDate   = "";
        if (defined(from) || defined(to)) {
            fromDate = (String) getDateSelect(from, "").trim();
            toDate   = (String) getDateSelect(to, "").trim();
        } else if ((relativeArg != null) && defined(relativeArg)) {
            fromDate = (String) getDateSelect(relativeArg, "").trim();
            if (fromDate.equals("none")) {
                return new Date[] { null, null };
            }
            toDate = "now";
        } else if (dflt == null) {
            return new Date[] { null, null };
        }

        //        System.err.println("from:" + fromDate);
        //        System.err.println("to:" + toDate);


        if (dflt == null) {
            dflt = new Date();
        }
        Date[] range = DateUtil.getDateRange(fromDate, toDate, dflt);

        //        System.err.println("dateRange:" + fromDate + " date:" + range[0]);
        return range;
    }



    /**
     * _more_
     *
     * @param dttm _more_
     *
     * @return _more_
     *
     * @throws java.text.ParseException On badness
     */
    public Date parseDate(String dttm) throws java.text.ParseException {
        //Check for yyyy-DDD. DateUtil.parse does not support day of year
        return DateUtil.parse(dttm);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getSiteId() {
        return get(ARG_SITE_ID, (String) null);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean siteIdDefined() {
        return defined(ARG_SITE_ID);
    }





}
