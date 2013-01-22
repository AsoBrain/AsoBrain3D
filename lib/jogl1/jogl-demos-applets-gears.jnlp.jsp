<%@ page contentType="application/x-java-jnlp-file"
         import="java.util.*"
         import="com.ivenza.platform.web.*"
%><%
	IvenzaServletServices.writeAppletJnlp( out,
		/* name                */ "jogl-demos-applets-gears",
		/* title               */ "JOGL Gears Applet",
		/* codebase            */ String.valueOf( IvenzaServletTools.getUrl( request, "/soda/" ) ),
		/* jarFile             */ Arrays.asList( "jogl1/jogl-demos.jar" ),
		/* mainClass           */ "demos.applets.GearsApplet",
		/* parameters          */ Collections.<String, String>emptyMap(),
		/* width               */ 600,
		/* height              */ 400,
		/* javaVersion         */ "1.6+",
		/* maxHeapSize         */ "64m",
		/* enableJogl          */ true );
%>
