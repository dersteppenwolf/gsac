#
# Run the stand-alone GsacServer
#

java -Xmx512m -jar @JARFILE@ -port 8080

#Access this server at:
#http://localhost:8080/gsacws/site/form

#To override database connection properties uncomment and edit the following line:

#java -Dgsac.db.username=<username> -Dgsac.db.password=<password> -Dgsac.db.jdbcurl=<jdbcurl>  -Xmx512m -jar @JARFILE@ -port 8080

# and change: 
# <username>       - database user name
# <password>       - database password
# <jdbcurl>        - The  JDBC URL  e.g.:
# To connect to a mysql server running on port 3306 with database dbname set the above jdbcurl to:
# jdbc:mysql://<databaseserver>:3306/<dbname>

#e.g.:
#java -Dgsac.db.username=root -Dgsac.db.password=password -Dgsac.db.jdbcurl=jdbc:mysql://localhost:3306/cddis  -Xmx512m -jar @JARFILE@ -port 8080
 



