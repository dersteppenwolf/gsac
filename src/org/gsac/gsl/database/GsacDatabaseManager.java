/*
 * Copyright 2015 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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


package org.gsac.gsl.database;

/* ************************************************************
 *
 * To choose a Java 1.7 or Java 1.6 build of GSAC:
 * Find comments in this file with " Java ", and use
 * the Java 1.7 or Java 1.6 code lines for the version needed.
 * There are three sets of such code choices.
 * All changes are caused by changes in method names in Apache commons 
 * from Java 1.6 to Java 1.7.
 * 
 *************************************************************
 */


//            Choose Java version:
//
//  for Java 1.6 version, use this one line 
import org.apache.commons.dbcp.BasicDataSource;
//  for Java 1.7 version, use this one line
//import org.apache.commons.dbcp2.BasicDataSource;


// for either 1.6 or 1.7, use all these imports:
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.ramadda.sql.Clause;
import org.gsac.gsl.ramadda.sql.SqlUtil;

import java.io.*;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;


/**
 * Provides basic database facilities including connection pooling.
 * Overwrite doMakeDataSource to implement
 *
 * 2014: changes to update to org.apache.commons.dbcp2.BasicDataSource
 *
 * @author  Jeff McWhirter mcwhirter@unavco.org
 */

public abstract class GsacDatabaseManager extends GsacManager implements SqlUtil
    .ConnectionManager {

    /** default jdbc driver class name */
    private static final String DB_DRIVER_CLASSNAME =
        "oracle.jdbc.driver.OracleDriver";

    /** default query time out */
    private static final int TIMEOUT = 30000;

    /** property */
    public static final String PROP_GSAC_DB_USERNAME = "gsac.db.username";

    /** property */
    public static final String PROP_GSAC_DB_PASSWORD = "gsac.db.password";

    /** property */
    public static final String PROP_GSAC_DB_JDBCURL = "gsac.db.jdbcurl";

    /** _more_ */
    public static final String PROP_GSAC_DB_DRIVERCLASS =
        "gsac.db.driverclass";


    /** the datasource */
    private BasicDataSource dataSource;


    /** _more_ */
    Properties connectionProps = new Properties();


    /** _more_ */
    private int connectionCnt = 0;

    /** the user name */
    private String userName;

    /** the password */
    private String password;

    /** the jdb url_ */
    private String jdbcUrl;


    /** _more_ */
    Properties properties = new Properties();

    /**
     * ctor.
     *
     * @param repository The repository
     */
    public GsacDatabaseManager(GsacRepository repository) {
        super(repository);
    }

    /**
     * Initialize the database. Should be called after creation.
     *
     * @throws Exception on badness
     */
    public void init() throws Exception {
        // Load the database driver
        try {
            Class.forName(getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(" GSAC: Could not load the jdbc driver:" + getDriverClassName());
        }
        //        System.err.println("DB: Creating data source");
        makeNewDataSource();
        //try to connect
        closeConnection(getConnection());
        SqlUtil.setConnectionManager(this);
    }

    /**
     * _more_
     *
     * @param tableName _more_
     * @param columnName _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String[] readDistinctValues(String tableName, String columnName)
            throws Exception {
        String[] values =
            SqlUtil.readString(getIterator(select(distinct(columnName),
                tableName)), 1);

        return values;
    }

    /**
     * _more_
     *
     * @param what _more_
     *
     * @return _more_
     */
    public String distinct(String what) {
        return " distinct " + what;
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void makeNewDataSource() throws Exception {
        if (dataSource != null) {}
        dataSource = doMakeDataSource();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getPoolStats() {
        return "#active:" + dataSource.getNumActive() + " #idle:"
               + dataSource.getNumIdle()  //+" max active: " // method not missing + dataSource.getMaxActive() i
               + " max idle:"
               + dataSource.getMaxIdle();

    }

    /** _more_          */
    private boolean loadedProperties = false;

    /**
     * _more_
     */
    private void loadDatabaseProperties() {
        if (loadedProperties) {
            return;
        }
        String propertiesFile = getPropertiesFile();
        loadDatabaseProperties(propertiesFile);
        loadedProperties = true;
    }

    public void loadDatabaseProperties(String propertiesFile) {
        if (propertiesFile == null) {
            return;
        }
        try {
            InputStream propertiesIS = IOUtil.getInputStream(propertiesFile,
                                           getClass());
            properties.load(propertiesIS);
            //                System.err.println("properties:" + properties);
        } catch (Exception exc) {
            throw new IllegalArgumentException("GSAC: Could not load properties:" + propertiesFile);
        }
    }

    /**
     * This needs to be overwritten by the derived class to create the data source
     *
     * @return the data source to use
     *
     * @throws Exception _more_
     */
    public BasicDataSource doMakeDataSource() throws Exception {
        //Read the username, password and jdbcurl from the properties file
        loadDatabaseProperties();
        userName = getDatabaseProperty(PROP_GSAC_DB_USERNAME);
        password = getDatabaseProperty(PROP_GSAC_DB_PASSWORD);
        jdbcUrl  = getDatabaseProperty(PROP_GSAC_DB_JDBCURL);

        if (userName == null) {
            throw new IllegalArgumentException("Could not load property:"
                    + PROP_GSAC_DB_USERNAME);
        }
        if (password == null) {
            throw new IllegalArgumentException("Could not load property:"
                    + PROP_GSAC_DB_PASSWORD);
        }
        if (jdbcUrl == null) {
            throw new IllegalArgumentException("Could not load property:"
                    + PROP_GSAC_DB_JDBCURL);
        }

        if (getRepository() != null) {
            getRepository().logInfo("jdbc url: " + jdbcUrl);
        }
        jdbcUrl = jdbcUrl.replace("${username}", userName);
        jdbcUrl = jdbcUrl.replace("${password}", password);

        connectionProps.put("user", userName);
        connectionProps.put("password", password);

        // debug database : log account
        //System.err.println("    jdbcUrl:_" + jdbcUrl+"_");
        //System.err.println("GSAC: user name:" + userName+":");
        //System.err.println("GSAC: password:" + password+":");

        BasicDataSource dataSource = new BasicDataSource();


        // to choose java 1.6 or 1.7:
        //  Java 1.6 version: 
        dataSource.setMaxActive(100);
        //  Java 1.7 version: 
        //dataSource.setMaxTotal(100);


        dataSource.setMaxIdle(100);
        dataSource.setDriverClassName(getDriverClassName());
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        dataSource.setUrl(jdbcUrl);


        // to choose java 1.6 or 1.7:
        //  Java 1.6 version: 
        dataSource.setMaxWait(1L);
        //dataSource.setRemoveAbandoned(true);
        // Java 1.7 version: 
        //dataSource.setMaxWaitMillis(1L);
        //dataSource.setRemoveAbandonedOnBorrow(true); 


        //60 seconds
        dataSource.setRemoveAbandonedTimeout(60);
        dataSource.setLogWriter(new PrintWriter(System.err));

        //2010: LOOK TODO: For now log abandoned but we'll want to turn this off for performance sometime
        dataSource.setLogAbandoned(true);

        return dataSource;
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getDatabaseProperty(String name) {
        loadDatabaseProperties();
        //Let the system properties override the files
        String value = System.getProperty(name);
        //        System.err.println("db prop:" + name);
        if (value != null) {
            //            System.err.println("\tsystem:" + value);
            return value.trim();
        }
        if (getRepository() != null) {
            value = getRepository().getProperty(name);
            if (value != null) {
                //                System.err.println("\trepository:" + value);
                return value.trim();
            }
        }

        String fromProperties = (String) properties.get(name);
        if (fromProperties != null) {
            //            System.err.println("\tmy props:" + fromProperties);
            return fromProperties.trim();
        }

        return null;
    }

    /**
     * a hook so derived classes can provide the path to their properties file
     *
     * @return properties file
     */
    public String getPropertiesFile() {
        return null;
    }

    /**
     * Get the class name of the jdbc driver.  Can be overwritten by derived classes.
     * This looks for the PROP_GSAC_DB_DRIVERCLASS property. If not found then it looks at the jdbc url
     * to figure out the appropriate driver class
     *
     * @return jdbc driver classname
     */
    public String getDriverClassName() {
        String driver =
            (String) getDatabaseProperty(PROP_GSAC_DB_DRIVERCLASS);
        if (driver != null) {
            return driver;
        }
        String jdbcUrl = getDatabaseProperty(PROP_GSAC_DB_JDBCURL);
        //System.err.println("GSAC: JDBC:" + jdbcUrl);
        if (jdbcUrl == null) {
            return null;
        }
        if (jdbcUrl.indexOf("mysql") >= 0) {
            return "com.mysql.jdbc.Driver";
        }
        if (jdbcUrl.indexOf("oracle") >= 0) {
            return "oracle.jdbc.driver.OracleDriver";
        }
        if (jdbcUrl.indexOf("postgres") >= 0) {
            return "org.postgresql.Driver";
        }

        return null;
    }


    /**
     * get number of connections
     *
     * @return number of connections
     */
    public int getConnectionCount() {
        return connectionCnt;
    }


    /**
     * increment number of connections
     */
    public void incrConnectionCount() {
        connectionCnt++;
    }


    /**
     * decrement number of connections
     */
    public void decrConnectionCount() {
        connectionCnt--;
    }


    /**
     * get a connection from the pool
     *
     * @return connection
     *
     * @throws Exception on badness
     */
    public Connection getConnection() throws Exception {
        Connection connection;

        // shows getConnection jdbcUrl:_jdbc:mysql://localhost:3306/IsCGPS_GSAC_database_

        // original JMcW code
        //TODO: lets try out not using the connection pooling
        connection = DriverManager.getConnection(jdbcUrl, connectionProps);  
        //        connection = dataSource.getConnection();
        
        // TRY to get non-latin or non-ascii charcters from a mysql query for station names, want to do
        //      like     DriverManager.getConnection( "jdbc:mysql://"+host+"/"+dbName+"?useUnicode=true&characterEncoding=UTF-8",user,pass);
        //  System.err.println("     getConnection jdbcUrl:_" + jdbcUrl);
        //         gives         getConnection jdbcUrl:_jdbc:mysql://localhost:3306/IsCGPS_GSAC_database
        //connection = DriverManager.getConnection(jdbcUrl+"?useUnicode=true&characterEncoding=UTF-8", connectionProps);  
        // no change!!!!!  still gives bad chars in station names from iceland db, which are correct Icelandic letters in the mysql database

        incrConnectionCount();
        //        System.err.println ("open:" + connectionCnt);
        return connection;
    }




    /**
     * close the connection
     *
     * @param connection on badness
     *
     */
    public void closeConnection(Connection connection) {
        try {
            decrConnectionCount();
            connection.close();
            if (getConnectionCount() > 3) {
                //                Misc.printStack("close:" + connectionCnt,10);
                //                System.err.println("db connection close:" + connectionCnt);
            }
        } catch (Exception ignoreThis) {}
    }


    /**
     * close the statement and release its connection
     *
     * @param statement the statement to close
     *
     * @throws SQLException on badness
     */
    public void closeAndReleaseConnection(Statement statement)
            throws SQLException {
        if (statement == null) {
            return;
        }
        Connection connection = null;
        try {
            connection = statement.getConnection();
            statement.close();
        } catch (Throwable ignore) {}

        if (connection != null) {
            closeConnection(connection);
        }
    }


    /**
     * _more_
     *
     * @param stmt _more_
     */
    public void initSelectStatement(Statement stmt) {}



    /**
     * Close the stmt. Ignores any exceptions
     *
     * @param statement The statement to close
     */
    public void closeStatement(Statement statement) {
        try {
            closeAndReleaseConnection(statement);
            //            statement.close();
        } catch (Exception ignoreThis) {}
    }



    /**
     * Make a string clause  for the given column and value with the given search type.
     *
     * @param searchType one of contains, beginswith, matches, etc.
     * @param column column to search on
     * @param value value to search for
     *
     * @return the clause
     */
    public static Clause getStringSearchClause(String searchType,
            String column, String value) {
        boolean not = value.startsWith("!");
        if (not) {
            value = value.substring(1, value.length());
        }
        if (searchType.equals(SEARCHTYPE_BEGINSWITH)) {
            return Clause.like(column, value + "%", not);
        }
        if (searchType.equals(SEARCHTYPE_ENDSWITH)) {
            return Clause.like(column, "%" + value, not);
        }
        if (searchType.equals(SEARCHTYPE_CONTAINS)) {
            return Clause.like(column, "%" + value + "%", not);
        }
        if (not) {
            return Clause.neq(column, value);
        }

        return Clause.eq(column, value);
    }



    /**
     * Make a select statement
     *
     * @param what What to select
     * @param table Table to select from
     *
     * @return The statement
     *
     * @throws Exception on badness
     */
    public Statement select(String what, String table) throws Exception {
        return select(what, table, (Clause) null);
    }

    /**
     * get an iterator for the given select from the given table
     *
     * @param what what to select
     * @param table the table
     *
     * @return the iterator
     *
     * @throws Exception on badness
     */
    public SqlUtil.Iterator getIterator(String what, String table)
            throws Exception {
        return getIterator(select(what, table));
    }

    /**
     * _more_
     *
     * @param stmt _more_
     * @param offset _more_
     * @param limit _more_
     *
     * @return _more_
     */
    public SqlUtil.Iterator getIterator(Statement stmt, int offset,
                                        int limit) {
        return new Iterator(this, stmt, offset, limit);
    }

    /**
     * _more_
     *
     * @param statement _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public SqlUtil.Iterator getIterator(Statement statement)
            throws Exception {
        return new Iterator(this, statement);
    }


    /**
     * Make a select statement
     *
     * @param what What to select
     * @param table Table to select from
     * @param clause The where clause. May be null
     *
     * @return The statement
     *
     * @throws Exception on badness
     */
    public Statement select(String what, String table, Clause clause)
            throws Exception {
        return select(what, table, clause, "", -1);
    }


    /**
     * Make a select statement
     *
     * @param what What to select
     * @param clause The where clause. May be null
     *
     * @return The statement
     *
     * @throws Exception on badness
     */
    public Statement select(String what, Clause clause) throws Exception {
        return select(what, clause.getTableNames(), clause);
    }


    /**
     * Make a select statement
     *
     * @param what What to select
     * @param tables The tables to select from
     * @param clause The where clause. May be null
     *
     * @return The statement
     *
     * @throws Exception on badness
     */
    public Statement select(String what, List<String> tables, Clause clause)
            throws Exception {
        return select(what, tables, clause, "", -1);
    }

    /**
     * Make a select statement
     *
     * @param what What to select
     * @param table The table to select from
     * @param clause The where clause. May be null
     * @param extra select suffix. May be null,
     * @param max Max count. -1 means all. This may not actuall work on the particular jdbc connection
     *
     * @return The statement
     *
     * @throws Exception on badness
     */
    public Statement select(String what, String table, final Clause clause,
                            String extra, int max)
            throws Exception {

        return select(what, Misc.newList(table), clause, extra, max);
    }

    /**
     * Make a select statement
     *
     * @param what What to select
     * @param tables The tables to select from
     * @param clause The where clause. May be null
     * @param suffixSql select suffix. e.g., order by, group by. May be null.
     * @param max Max count. -1 means all. This may not actually work on the particular jdbc connection
     *
     * @return The statement
     *
     * @throws Exception on badness
     */
    public Statement select(String what, List tables, Clause clause,
                            String suffixSql, int max)
            throws Exception {
        return select(what, tables, clause, null, suffixSql, max);
    }


    /**
     * _more_
     *
     * @param what _more_
     * @param tables _more_
     * @param clause _more_
     * @param sqlBetweenFromAndWhere _more_
     * @param suffixSql _more_
     * @param max _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Statement select(String what, List tables, Clause clause,
                            String sqlBetweenFromAndWhere, String suffixSql,
                            int max)
            throws Exception {
        Connection connection = getConnection();
        try {
            Statement statement = SqlUtil.select(connection, what, tables,
                                      clause, sqlBetweenFromAndWhere,
                                      suffixSql, max, TIMEOUT);

            return statement;
        } catch (Exception exc) {
            closeConnection(connection);

            throw exc;
        }
    }






    /**
     * This writes out the full database table definition to a file called Tables.java
     *
     *
     * @param packageName Tables class package name
     * @throws Exception On badness
     */
    public void writeTables(String packageName) throws Exception {
        writeTables(packageName, new String[] { "TABLE", "VIEW" });
    }

    /**
     * _more_
     *
     * @param packageName _more_
     * @param what _more_
     *
     * @throws Exception On badness
     */
    public void writeTables(String packageName, String[] what)
            throws Exception {
        FileOutputStream fos = new FileOutputStream("Tables.java");
        PrintWriter      pw  = new PrintWriter(fos);
        writeTables(pw, packageName, what);
        pw.close();
        fos.close();
    }

    /**
     * Actually write the tables
     *
     * @param pw What to write to
     * @param packageName Tables.java package name
     * @param what _more_
     *
     * @throws Exception on badness
     */

    private void writeTables(PrintWriter pw, String packageName,
                             String[] what)
            throws Exception {

        String sp1 = "    ";
        String sp2 = sp1 + sp1;
        String sp3 = sp1 + sp1 + sp1;

        // original too brief and wrong: was pw.append( "/**Generated by running: java org.unavco.projects.gsac.repository.UnavcoGsacDatabaseManager**/\n\n");
        pw.append( "/** Tables.java makes classes which represent the tables and fields in your database. ");
        pw.append( " ");
        pw.append( "    It is created with GSAC code which reads your database and creates an all-new Tables.java file in your local GSAC code area.");
        pw.append( "    Generated by running 'ant tables' which calls the GSAC code  GsacDatabaseManager:writeTables().");
        pw.append( "    See the GSAC installation README (part 2) file in your local GSAC code area.");
        pw.append( "    If you get an error building your local GSAC with 'ant' it may be caused by your Java code calling a for a TABLES variable (db field) which does not exist here or in the db.");
        pw.append( "**/ \n\n");

        pw.append("package " + packageName + ";\n\n");
        // old defunct pw.append("import org.ramadda.sql.SqlUtil;\n\n");
        pw.append("import org.gsac.gsl.ramadda.sql.SqlUtil;\n\n");
        pw.append("public abstract class Tables {\n");
        pw.append(sp1 + "public abstract String getName();\n");
        pw.append(sp1 + "public abstract String getColumns();\n");
        Connection       connection = getConnection();
        DatabaseMetaData dbmd       = connection.getMetaData();
        ResultSet        catalogs   = dbmd.getCatalogs();
        ResultSet        tables     = dbmd.getTables(null, null, null, what);


        HashSet          seenTables = new HashSet();
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            //            System.err.println ("NAME:" + tableName);
            String TABLENAME = tableName.toUpperCase();
            if (seenTables.contains(TABLENAME)) {
                continue;
            }
            seenTables.add(TABLENAME);
            String tableType = tables.getString("TABLE_TYPE");
            if (Misc.equals(tableType, "INDEX")) {
                continue;
            }
            if (tableName.indexOf("$") >= 0) {
                continue;
            }

            if (tableType == null) {
                continue;
            }

            if ((tableType != null) && tableType.startsWith("SYSTEM")) {
                continue;
            }

            ResultSet columns  = dbmd.getColumns(null, null, tableName, null);


            List      colNames = new ArrayList();
            pw.append("\n\n");
            pw.append(sp1 + "public static class " + TABLENAME
                      + " extends Tables {\n");

            pw.append(sp2 + "public static final String NAME = \""
                      + tableName + "\";\n");
            pw.append("\n");
            pw.append(sp2 + "public String getName() {return NAME;}\n");
            pw.append(sp2 + "public String getColumns() {return COLUMNS;}\n");
            //System.out.println("processing " + TABLENAME);

            String  tableVar = null;
            List    colVars  = new ArrayList();
            HashSet seen     = new HashSet();
            while (columns.next()) {
                String colName = columns.getString("COLUMN_NAME");
                String colSize = columns.getString("COLUMN_SIZE");
                String COLNAME = colName.toUpperCase();
                if (seen.contains(COLNAME)) {
                    continue;
                }
                seen.add(COLNAME);
                COLNAME = COLNAME.replace("#", "");
                colNames.add("COL_" + COLNAME);
                pw.append(sp2 + "public static final String COL_" + COLNAME
                          + " =  NAME + \"." + colName + "\";\n");
                /*
                pw.append(sp2 + "public static final String ORA_" + COLNAME
                          + " =  \"" + colName + "\";\n");
                */
            }

            pw.append("\n");
            pw.append(
                sp2
                + "public static final String[] ARRAY = new String[] {\n");
            pw.append(sp3 + StringUtil.join(",", colNames));
            pw.append("\n");
            pw.append(sp2 + "};\n");
            pw.append(
                sp2
                + "public static final String COLUMNS = SqlUtil.comma(ARRAY);\n");
            pw.append(
                sp2
                + "public static final String NODOT_COLUMNS = SqlUtil.commaNoDot(ARRAY);\n");
            pw.append(sp1 + "public static final " + TABLENAME
                      + " table  = new  " + TABLENAME + "();\n");
            pw.append(sp1 + "}\n\n");

        }


        pw.append("\n\n}\n");

    }


    /**
     * Class description
     *
     *
     * @version        $version$, Tue, Sep 21, '10
     * @author         Enter your name here...
     */
    public static class Iterator extends SqlUtil.Iterator {

        /** _more_ */
        Statement statement;

        /** _more_ */
        GsacDatabaseManager databaseManager;

        /**
         * _more_
         *
         * @param databaseManager _more_
         * @param statement _more_
         */
        public Iterator(GsacDatabaseManager databaseManager,
                        Statement statement) {
            super(statement);
            this.statement       = statement;
            this.databaseManager = databaseManager;
        }

        /**
         * _more_
         *
         * @param databaseManager _more_
         * @param statement _more_
         * @param offset _more_
         * @param limit _more_
         */
        public Iterator(GsacDatabaseManager databaseManager,
                        Statement statement, int offset, int limit) {
            super(statement, offset, limit);
            this.statement       = statement;
            this.databaseManager = databaseManager;
        }



        /**
         * _more_
         *
         * @param statement _more_
         *
         * @throws SQLException On badness
         */
        protected void close(Statement statement) throws SQLException {
            databaseManager.closeAndReleaseConnection(statement);
        }

    }




}
