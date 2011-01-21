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

package gov.nasa.cddis.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.database.*;



/**
 * All this really does is pass to the base class the location of
 * the database properties file. This needs to be of the form:<pre>
 * gsac.db.username=dbuser
 * gsac.db.password=password
 * gsac.db.jdbcurl=jdbc:oracle:thin:${username}/${password}@db0.ap.int.unavco.org:1521:tst3
 * </pre>
 * The jdbcurl is a template with the username and password macros replaced with the given username/password
 *
 * If you implement this then if you run:<pre>
 * java your.databasemanager.package.path.YourGsacDatabaseManager
 * </pre>
 * Then it will generate a Tables.java file
 *
 * @author     Jeff McWhirter mcwhirter@unavco.org
 */
public class CddisDatabaseManager extends GsacDatabaseManager {

    /**
     *   This needs to be the path to your database properties file.
     */

    public static final String DB_PROPERTIES =
        "/gov/nasa/cddis/gsac/resources/gsacdb.properties";

    /**
     * ctor
     *
     * @param repository the repository
     *
     * @throws Exception On badness
     */
    public CddisDatabaseManager(CddisRepository repository) throws Exception {
        super(repository);
    }

    /**
     * return the class path to the properties file.
     *
     * @return properties file
     */
    public String getPropertiesFile() {
        return DB_PROPERTIES;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getDriverClassName() {
        return "com.mysql.jdbc.Driver";
    }

    /**
     * The main writes out to a file, Tables.java, the Java based definition
     * of the database schema.
     *
     * @param args cmd line args
     *
     * @throws Exception On badness
     */
    public static void main(String[] args) throws Exception {
        CddisDatabaseManager dbm = new CddisDatabaseManager(null);
        dbm.init();
        //Change this package to be your package
        String packageName = dbm.getClass().getPackage().getName();
        dbm.writeTables(packageName);
    }



}
