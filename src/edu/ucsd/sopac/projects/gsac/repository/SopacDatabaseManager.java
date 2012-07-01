/*
 *  
 */

package edu.ucsd.sopac.projects.gsac.repository;

import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import javax.naming.Context;
import javax.sql.DataSource;
import javax.naming.InitialContext;

import ucar.unidata.sql.SqlUtil;

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
public class SopacDatabaseManager extends GsacDatabaseManager {

	private DataSource dataSource;
	private int maxActive;
	

    /** 
        This needs to be the path to your database properties file. 
    */

    public static final String DB_PROPERTIES =
        "/edu/ucsd/sopac/projects/gsac/repository/resources/gsacdb.properties";

    /**
     * ctor
     *
     * @param repository the repository
     *
     * @throws Exception On badness
     */
    public SopacDatabaseManager(SopacRepository repository)
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
     * Initialize the database. Should be called after creation.
     *
     * @throws Exception on badness
     */
    public void skipThis_init() throws Exception {
        // Load the database driver
/*
    	try {
            Class.forName(getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load the jdbc driver:"
                                       + getDriverClassName());
        }
*/
        //        System.err.println("DB: Creating data source");
    	if (dataSource==null) {
    		dataSource = doMakePooledDataSource();
    	}
        //try to connect
        closeConnection(getConnection());
        
        SqlUtil.setConnectionManager(this);
    }
    
    /**
     * Obtains the datasource from a JNDI context
     * 
     * NOTE: Creating a pooled data source requires configuration at 
     *       the JNDI source (context.xml) and web.xml as well as 
     *       here.  Currently, it only creates a regular data source.
     *
     * @param none
     *
     * @throws Exception On badness
     */
    public DataSource doMakePooledDataSource() throws Exception {

    	Context initContext = new InitialContext();
    	Context envContext  = (Context)initContext.lookup("java:/comp/env");
    	DataSource ds = (DataSource) envContext.lookup("jdbc/geod");
    	
    	// Following requires GsacDatabaseManager change, so avoid.
    	//
        //if (this.repository != null) {
        //    this.repository.logInfo("jndi context: " + "jdbc/geod");
        //}
        ds.setLogWriter(new PrintWriter(System.err));

        //TODO: For now log abandoned but we'll want to turn this off for performance sometime
        //ds.setLogAbandoned(true);

        return ds;
    }

    public Connection skipThis_getConnection() throws Exception {
        Connection connection;
        //TODO: lets try out not using the connection pooling
        connection = dataSource.getConnection();
        incrConnectionCount();
        DatabaseMetaData dmd = connection.getMetaData();
        maxActive = dmd.getMaxConnections();
        return connection;
    }

    
    /**
     * _more_
     *
     * @return _more_
     */
    public String skipThis_getPoolStats() {
        return "#active:1 #idle:1" + maxActive + " maxIdle:1";
/*        
        "#active:" + dataSource.getNumActive() + " #idle:"
               + dataSource.getNumIdle() + " max active: "
               + dataSource.getMaxActive() + " max idle:"
               + dataSource.getMaxIdle();
*/
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
        SopacDatabaseManager dbm = new SopacDatabaseManager(null);
        dbm.init();
        //Change this package to be your package
        String packageName = dbm.getClass().getPackage().getName();
        dbm.writeTables(packageName);
    }



}
