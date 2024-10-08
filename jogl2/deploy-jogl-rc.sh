#!/bin/sh
JOGL2_VERSION=2.4.0
JOGL2_URL=https://jogamp.org/deployment/v2.4.0-rc-20200202/jar/
MVN=mvn
DEPLOY_FILE_ARGS="deploy:deploy-file -Durl=https://maven.ivenza.net/nexus/content/repositories/releases -DrepositoryId=maven.ivenza.net"
DEPLOY_ARGS="clean package deploy:deploy -DaltDeploymentRepository=maven.ivenza.net::default::https://maven.ivenza.net/nexus/content/repositories/releases"

rm -rf target/gluegen && mkdir -p target/gluegen && cd target/gluegen || exit $?

curl ${JOGL2_URL}gluegen-rt.jar --output gluegen-rt.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt.jar || exit $?

curl ${JOGL2_URL}gluegen-rt-natives-linux-amd64.jar --output gluegen-rt-natives-linux-amd64.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-linux-amd64 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-linux-amd64.jar || exit $?

curl ${JOGL2_URL}gluegen-rt-natives-linux-i586.jar --output gluegen-rt-natives-linux-i586.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-linux-i586 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-linux-i586.jar || exit $?

curl ${JOGL2_URL}gluegen-rt-natives-macosx-universal.jar --output gluegen-rt-natives-macosx-universal.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-macosx-universal -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-macosx-universal.jar || exit $?

curl ${JOGL2_URL}gluegen-rt-natives-windows-amd64.jar --output gluegen-rt-natives-windows-amd64.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-windows-amd64 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-windows-amd64.jar || exit $?

curl ${JOGL2_URL}gluegen-rt-natives-windows-i586.jar --output gluegen-rt-natives-windows-i586.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dclassifier=natives-windows-i586 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=gluegen-rt-natives-windows-i586.jar || exit $?

mkdir gluegen-rt-main && cd gluegen-rt-main || exit $?
curl https://repo1.maven.org/maven2/org/jogamp/gluegen/gluegen-rt-main/2.3.2/gluegen-rt-main-2.3.2.pom --output pom.xml || exit $?
cp ../../../gluegen-rt-main.pom pom.xml
${MVN} -N versions:set -DnewVersion=${JOGL2_VERSION}
${MVN} -N ${DEPLOY_ARGS} -Dversion=${JOGL2_VERSION} -Dgpg.skip || exit $?
cd ../../..

rm -rf target/jogl && mkdir -p target/jogl && cd target/jogl || exit $?

curl ${JOGL2_URL}jogl-all.jar --output jogl-all.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all.jar  || exit $?

curl ${JOGL2_URL}jogl-all-natives-linux-i586.jar --output jogl-all-natives-linux-i586.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-linux-i586 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-linux-i586.jar || exit $?

curl ${JOGL2_URL}jogl-all-natives-linux-amd64.jar --output jogl-all-natives-linux-amd64.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-linux-amd64 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-linux-amd64.jar || exit $?

curl ${JOGL2_URL}jogl-all-natives-windows-i586.jar --output jogl-all-natives-windows-i586.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-windows-i586 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-windows-i586.jar || exit $?

curl ${JOGL2_URL}jogl-all-natives-windows-amd64.jar --output jogl-all-natives-windows-amd64.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-windows-amd64 -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-windows-amd64.jar || exit $?

curl ${JOGL2_URL}jogl-all-natives-macosx-universal.jar --output jogl-all-natives-macosx-universal.jar || exit $?
${MVN} ${DEPLOY_FILE_ARGS} -DgeneratePom=false -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dclassifier=natives-macosx-universal -Dpackaging=jar -Dversion=${JOGL2_VERSION} -Dfile=jogl-all-natives-macosx-universal.jar || exit $?

mkdir jogl-all-main && cd jogl-all-main || exit $?
curl https://repo1.maven.org/maven2/org/jogamp/jogl/jogl-all-main/2.3.2/jogl-all-main-2.3.2.pom --output pom.xml || exit $?
cp ../../../jogl-all-main.pom pom.xml
${MVN} -N versions:set -DnewVersion=${JOGL2_VERSION}
${MVN} -N ${DEPLOY_ARGS} -Dversion=${JOGL2_VERSION} -Dgpg.skip || exit $?
cd ../../..
