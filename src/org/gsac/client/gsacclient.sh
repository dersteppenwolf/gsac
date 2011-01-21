#!/bin/sh

dirname=`dirname $0`
command="java -Xmx512m -jar ${dirname}/lib/gsacclient.jar $*"

##echo "Running: $command"

if test  ${JAVA_HOME}; then
##Try using JAVA_HOME
    ${JAVA_HOME}/bin/${command}
else 
##Try just using java
    ${command}
fi
