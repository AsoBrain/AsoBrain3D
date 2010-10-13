#!/bin/bash
cd "`dirname "$0"`"/java/ab/j3d/geom/tessellator || exit $?
###############################################################################

JOGL_SOURCES="/numdata/install/java/APIs/jogl2/nightly-20100322/jogl-2.0-src/jogl"

echo "Copy source files"
rm -f *.java
cp ${JOGL_SOURCES}/build/jogl/gensrc/classes/javax/media/opengl/GL.java . || exit $?
cp ${JOGL_SOURCES}/build/jogl/gensrc/classes/javax/media/opengl/glu/GLU.java . || exit $?
cp ${JOGL_SOURCES}/src/jogl/classes/javax/media/opengl/glu/GLUtessellator*.java . || exit $?
cp ${JOGL_SOURCES}/src/jogl/classes/com/sun/opengl/impl/glu/tessellator/* . || exit $?

###############################################################################

GLOBAL_FIXES="\"
\"
\"Verwijder JOGL imports
\"
g/^import com.sun/d
g/^import javax.media/d
\"
\" Vervang alle package referenties
\"
%s/com.sun.opengl.impl.glu.tessellator/ab.j3d.geom.tessellator/g
%s/javax.media.opengl.glu/ab.j3d.geom.tessellator/g
%s/javax.media.opengl/ab.j3d.geom.tessellator/g
\"
\" Vervang 'GL' en 'GLU' referenties door 'GLUtessellator'
\"
%s/GLU\([\.#]\)/GLUtessellator\1/g
%s/GL\([\.#]\)/GLUtessellator\1/g
\"
\" Javadoc reference fixes
\"
%s/ab.j3d.geom.tessellator.GLUtessellator#/javax.media.opengl.GL#/g
%s/ #gluErrorString/ javax.media.opengl.glu.GLU#gluErrorString/g
%s/ GLUtessellator#gluErrorString/ javax.media.opengl.glu.GLU#gluErrorString/g
\"
\" Verwijder trialing whitespace en maak Unix file
\"
%s/[ 	]*$//
set fileformat=unix
"


SAVEQUIT="wq
"

###############################################################################

echo "Getting definitions from GL.java"
echo "g!/GL_LINE_LOOP\|GL_TRIANGLES\|GL_TRIANGLE_FAN\|GL_TRIANGLE_STRIP/d
${GLOBAL_FIXES}${SAVEQUIT}"|ex GL.java

###############################################################################

echo "Getting interface methods / constants from GLU.java"
echo "\"
\"
\" Laat alleen tessellator-deel over
\"
1,/tess.gluDeleteTess();/+2d
/-------/,\$d
\"
\" Verwijder uitgecommentarieerd deel
\"
g/^ *\/\//d
\"
\" Vertaal static utility methods naar interface definitie
\"
%s/public static final //
%s/(GLUtessellator tessellator, /(/
%s/(GLUtessellator tessellator)/()/
g/) {\$/+1,/^  }/d
%s/) {\$/);/
%g/@param tessellator/,+2d
g/#gluNewTess/d
\"
\" Misc
\"
${GLOBAL_FIXES}
${SAVEQUIT}"|ex GLU.java

###############################################################################

echo "Adding contents of 'GL.java' and 'GLU.java' to GLUtessellator.java"
echo "${GLOBAL_FIXES}
\"
\" Kopieer interface uit GLU.java
\"
:%s/{}/{/
\$r GLU.java
a

.
\$r GL.java
a

}
.
${SAVEQUIT}"|ex GLUtessellator.java
rm -f GL.java GLU.java

###############################################################################

for file in *.java
do
	case "$file" in
		GL.java|GLU.java|GLUtessellator.java)
			;;
		*)
			echo "Applying global fixes to $file"
			echo "${GLOBAL_FIXES}${SAVEQUIT}"|ex $file
			;;
	esac
done

cd ../../../../..;ant build
