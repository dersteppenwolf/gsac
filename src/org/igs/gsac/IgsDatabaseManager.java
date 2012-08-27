/*
 * 
 */

package org.igs.gsac;

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
 java your.databasemanager.package.path.YourGsacDatabaseManager
</pre>
 * Then it will generate a Tables.java file 
 *
 * @author     Jeff McWhirter mcwhirter@unavco.org
 */
public class IgsDatabaseManager extends GsacDatabaseManager {

    /** 
        This needs to be the path to your database properties file. 
    */

    public static final String DB_PROPERTIES =
        "/org/igs/gsac/resources/gsacdb.properties";

    /**
     * ctor
     *
     * @param repository the repository
     *
     * @throws Exception On badness
     */
    public IgsDatabaseManager(IgsRepository repository)
            throws Exception {
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

    public String getDriverClassName() {
        return super.getDriverClassName();
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
        IgsDatabaseManager dbm = new IgsDatabaseManager(null);
        dbm.init();
        //Change this package to be your package
        String packageName = dbm.getClass().getPackage().getName();
        dbm.writeTables(packageName);
    }



}
