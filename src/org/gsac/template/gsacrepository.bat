
REM

java -Xmx512m -jar @JARFILE@ -port 8080
jre\bin\java -Xmx512m -Didv.enableStereo=false -jar idv.jar %*

REM Use the line below instead if you want to use the D3D version of Java 3D
REM jre\bin\java -Xmx512m -Dj3d.rend=d3d -jar idv.jar %*


#
# Run the stand-alone GsacServer using the UnavcoRepository
#



#
#You can override database connection properties with:
#java -Dgsac.db.username=root -Dgsac.db.password=password -Dgsac.db.jdbcurl=jdbc:mysql://localhost:3306/dbname  -Xmx512m -jar @JARFILE@ -port 8080



