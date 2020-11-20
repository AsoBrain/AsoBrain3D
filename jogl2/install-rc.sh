#!/bin/sh
JOGL2_VERSION=2.4.0
JOGL2_URL=https://jogamp.org/deployment/v2.4.0-rc-20200202/jar/
MVN=mvn

rm -rf target/gluegen && mkdir -p target/gluegen && cd target/gluegen || exit $?

wget "$@" -O gluegen-rt.jar ${JOGL2_URL}gluegen-rt.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt.jar || exit $?

wget "$@" -O gluegen-rt-natives-linux-amd64.jar ${JOGL2_URL}gluegen-rt-natives-linux-amd64.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-linux-amd64 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-linux-amd64.jar || exit $?

wget "$@" -O gluegen-rt-natives-linux-i586.jar ${JOGL2_URL}gluegen-rt-natives-linux-i586.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-linux-i586 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-linux-i586.jar || exit $?

wget "$@" -O gluegen-rt-natives-macosx-universal.jar ${JOGL2_URL}gluegen-rt-natives-macosx-universal.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-macosx-universal -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-macosx-universal.jar || exit $?

wget "$@" -O gluegen-rt-natives-windows-amd64.jar ${JOGL2_URL}gluegen-rt-natives-windows-amd64.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-windows-amd64 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-windows-amd64.jar || exit $?

wget "$@" -O gluegen-rt-natives-windows-i586.jar ${JOGL2_URL}gluegen-rt-natives-windows-i586.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-windows-i586 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-windows-i586.jar || exit $?

mkdir gluegen-rt-main && cd gluegen-rt-main || exit $?
# wget "$@" -O pom.xml https://repo1.maven.org/maven2/org/jogamp/gluegen/gluegen-rt-main/2.3.2/gluegen-rt-main-2.3.2.pom || exit $?
cp ../../../gluegen-rt-main.pom pom.xml
${MVN} -N versions:set -DnewVersion=${JOGL2_VERSION}
${MVN} -N install -Dversion=${JOGL2_VERSION} -Dgpg.skip
cd ../../..

rm -rf target/jogl && mkdir -p target/jogl && cd target/jogl || exit $?

wget "$@" -O jogl-all.jar ${JOGL2_URL}jogl-all.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all.jar  || exit $?

wget "$@" -O jogl-all-natives-linux-i586.jar ${JOGL2_URL}jogl-all-natives-linux-i586.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-linux-i586 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-linux-i586.jar || exit $?

wget "$@" -O jogl-all-natives-linux-amd64.jar ${JOGL2_URL}jogl-all-natives-linux-amd64.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-linux-amd64 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-linux-amd64.jar || exit $?

wget "$@" -O jogl-all-natives-windows-i586.jar ${JOGL2_URL}jogl-all-natives-windows-i586.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-windows-i586 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-windows-i586.jar || exit $?

wget "$@" -O jogl-all-natives-windows-amd64.jar ${JOGL2_URL}jogl-all-natives-windows-amd64.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-windows-amd64 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-windows-amd64.jar || exit $?

wget "$@" -O jogl-all-natives-macosx-universal.jar ${JOGL2_URL}jogl-all-natives-macosx-universal.jar || exit $?
${MVN} install:install-file -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-macosx-universal -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-macosx-universal.jar || exit $?

mkdir jogl-all-main && cd jogl-all-main || exit $?
# wget "$@" -O pom.xml https://repo1.maven.org/maven2/org/jogamp/jogl/jogl-all-main/2.3.2/jogl-all-main-2.3.2.pom || exit $?
cp ../../../jogl-all-main.pom pom.xml
${MVN} -N versions:set -DnewVersion=${JOGL2_VERSION}
${MVN} -N install -Dversion=${JOGL2_VERSION} -Dgpg.skip
cd ../../..

