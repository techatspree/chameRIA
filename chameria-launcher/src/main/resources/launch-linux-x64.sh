#! /bin/sh
export CLASSPATH=core/chameria-launcher-${chameria.version}.jar:core/core-${chameleon.version}.jar:core/logback-classic-${logback.version}.jar:core/org.osgi.compendium-4.2.0.jar:core/logback-core-${logback.version}.jar:core/org.apache.felix.framework-${felix.version}.jar:core/slf4j-api-${slf4j.version}.jar:qt/qtjambi-${qt.version}-jnlp.jar:qt/qtjambi-util-${qt.version}.jar

JAVA32=/usr/lib/jvm/ia32-java-6-sun/bin/java 
$JAVA32 -d32  -Dchameleon.log.level=ERROR -Dcom.trolltech.qt.library-path-override=qt/lib de.akquinet.chameria.launcher.Launcher  --config=conf/chameleon.properties  --deploy=deploy "$@"
