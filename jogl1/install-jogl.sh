#!/bin/bash
JOGL1_VERSION="1.1.1a"
cd jogl
mvn install:install-file -Dfile=gluegen-rt-${JOGL1_VERSION}.jar -DgroupId=net.java.dev.gluegen -DartifactId=gluegen-rt -Dpackaging=jar -Dversion=${JOGL1_VERSION} 
mvn install:install-file -Dfile=jogl-${JOGL1_VERSION}.jar -DgroupId=net.java.dev.jogl -DartifactId=jogl -Dpackaging=jar -Dversion=${JOGL1_VERSION} 
mvn install:install-file -Dfile=jogl-${JOGL1_VERSION}-linux-amd64.jar -DgroupId=net.java.jogl -DartifactId=jogl-linux-amd64 -Dpackaging=jar -Dversion=${JOGL1_VERSION} 
mvn install:install-file -Dfile=jogl-${JOGL1_VERSION}-linux-i586.jar -DgroupId=net.java.jogl -DartifactId=jogl-linux-i586 -Dpackaging=jar -Dversion=${JOGL1_VERSION} 
mvn install:install-file -Dfile=jogl-${JOGL1_VERSION}-macosx-universal.jar -DgroupId=net.java.jogl -DartifactId=jogl-macosx-universal -Dpackaging=jar -Dversion=${JOGL1_VERSION} 
mvn install:install-file -Dfile=jogl-${JOGL1_VERSION}-windows-amd64.jar -DgroupId=net.java.jogl -DartifactId=jogl-windows-amd64 -Dpackaging=jar -Dversion=${JOGL1_VERSION} 
mvn install:install-file -Dfile=jogl-${JOGL1_VERSION}-windows-i586.jar -DgroupId=net.java.jogl -DartifactId=jogl-windows-i586 -Dpackaging=jar -Dversion=${JOGL1_VERSION} 
