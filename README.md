# GSAC 

Mirror of https://svn.code.sf.net/p/gsac/code/ 

######################################################################
GSAC SourceForge tree
######################################################################

To build GSAC, look at at src/org/gsac/README for instructions.

The top-level build.xml script builds:

package: src/org/gsac/gsl
This builds the  core GSAC code (the 'GSL' layer), which is the GSAC core code used in all GSAC implementations. 
The build makes the library file lib/gsacws.jar
 
optional:
package: src/org/gsac/federated
This is the federated GSAC repository implementation. The build results in the file gsacfederated.jar

