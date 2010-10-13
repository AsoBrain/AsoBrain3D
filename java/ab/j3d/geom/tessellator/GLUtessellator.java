/*
* Portions Copyright (C) 2003-2006 Sun Microsystems, Inc.
* All rights reserved.
*/

/*
** License Applicability. Except to the extent portions of this file are
** made subject to an alternative license as permitted in the SGI Free
** Software License B, Version 1.1 (the "License"), the contents of this
** file are subject only to the provisions of the License. You may not use
** this file except in compliance with the License. You may obtain a copy
** of the License at Silicon Graphics, Inc., attn: Legal Services, 1600
** Amphitheatre Parkway, Mountain View, CA 94043-1351, or at:
**
** http://oss.sgi.com/projects/FreeB
**
** Note that, as provided in the License, the Software is distributed on an
** "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
** DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
** CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
** PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
**
** NOTE:  The Original Code (as defined below) has been licensed to Sun
** Microsystems, Inc. ("Sun") under the SGI Free Software License B
** (Version 1.1), shown above ("SGI License").   Pursuant to Section
** 3.2(3) of the SGI License, Sun is distributing the Covered Code to
** you under an alternative license ("Alternative License").  This
** Alternative License includes all of the provisions of the SGI License
** except that Section 2.2 and 11 are omitted.  Any differences between
** the Alternative License and the SGI License are offered solely by Sun
** and not by SGI.
**
** Original Code. The Original Code is: OpenGL Sample Implementation,
** Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
** Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
** Copyright in any portions created by third parties is as indicated
** elsewhere herein. All Rights Reserved.
**
** Additional Notice Provisions: The application programming interfaces
** established by SGI in conjunction with the Original Code are The
** OpenGL(R) Graphics System: A Specification (Version 1.2.1), released
** April 1, 1999; The OpenGL(R) Graphics System Utility Library (Version
** 1.3), released November 4, 1998; and OpenGL(R) Graphics with the X
** Window System(R) (Version 1.3), released October 19, 1998. This software
** was created using the OpenGL(R) version 1.2.1 Sample Implementation
** published by SGI, but has not been independently verified as being
** compliant with the OpenGL(R) version 1.2.1 Specification.
**
** Author: Eric Veach, July 1994
** Java Port: Pepijn Van Eeckhoudt, July 2003
** Java Port: Nathan Parker Burg, August 2003
*/
package ab.j3d.geom.tessellator;

/**
 * The <b>GLUtessellator</b> object is used to hold the data, such as the
 * vertices, edges and callback objects, to describe and tessellate complex
 * polygons.  A <b>GLUtessellator</b> object is used with the
 * {@link GLU GLU} tessellator methods and
 * {@link GLUtessellatorCallback GLU callbacks}.
 *
 * @author Eric Veach, July 1994
 * @author Java Port: Pepijn Van Eechhoudt, July 2003
 * @author Java Port: Nathan Parker Burg, August 2003
 */
public interface GLUtessellator {
  /*****************************************************************************
   * <b>gluTessProperty</b> is used to control properites stored in a
   * tessellation object.  These properties affect the way that the polygons are
   * interpreted and rendered.  The legal value for <i>which</i> are as
   * follows:<P>
   *
   * Optional, throws GLException if not available in profile
   *
   * <b>GLU_TESS_WINDING_RULE</b>
   * <UL>
   *   Determines which parts of the polygon are on the "interior".
   *   <em>value</em> may be set to one of
   *   <BR><b>GLU_TESS_WINDING_ODD</b>,
   *   <BR><b>GLU_TESS_WINDING_NONZERO</b>,
   *   <BR><b>GLU_TESS_WINDING_POSITIVE</b>, or
   *   <BR><b>GLU_TESS_WINDING_NEGATIVE</b>, or
   *   <BR><b>GLU_TESS_WINDING_ABS_GEQ_TWO</b>.<P>
   *
   *   To understand how the winding rule works, consider that the input
   *   contours partition the plane into regions.  The winding rule determines
   *   which of these regions are inside the polygon.<P>
   *
   *   For a single contour C, the winding number of a point x is simply the
   *   signed number of revolutions we make around x as we travel once around C
   *   (where CCW is positive).  When there are several contours, the individual
   *   winding numbers are summed.  This procedure associates a signed integer
   *   value with each point x in the plane.  Note that the winding number is
   *   the same for all points in a single region.<P>
   *
   *   The winding rule classifies a region as "inside" if its winding number
   *   belongs to the chosen category (odd, nonzero, positive, negative, or
   *   absolute value of at least two).  The previous GLU tessellator (prior to
   *   GLU 1.2) used the "odd" rule.  The "nonzero" rule is another common way
   *   to define the interior.  The other three rules are useful for polygon CSG
   *   operations.
   * </UL>
   * <BR><b>GLU_TESS_BOUNDARY_ONLY</b>
   * <UL>
   *   Is a boolean value ("value" should be set to GL_TRUE or GL_FALSE). When
   *   set to GL_TRUE, a set of closed contours separating the polygon interior
   *   and exterior are returned instead of a tessellation.  Exterior contours
   *   are oriented CCW with respect to the normal; interior contours are
   *   oriented CW. The <b>GLU_TESS_BEGIN</b> and <b>GLU_TESS_BEGIN_DATA</b>
   *   callbacks use the type GL_LINE_LOOP for each contour.
   * </UL>
   * <BR><b>GLU_TESS_TOLERANCE</b>
   * <UL>
   *   Specifies a tolerance for merging features to reduce the size of the
   *   output. For example, two vertices that are very close to each other
   *   might be replaced by a single vertex.  The tolerance is multiplied by the
   *   largest coordinate magnitude of any input vertex; this specifies the
   *   maximum distance that any feature can move as the result of a single
   *   merge operation.  If a single feature takes part in several merge
   *   operations, the toal distance moved could be larger.<P>
   *
   *   Feature merging is completely optional; the tolerance is only a hint.
   *   The implementation is free to merge in some cases and not in others, or
   *   to never merge features at all.  The initial tolerance is 0.<P>
   *
   *   The current implementation merges vertices only if they are exactly
   *   coincident, regardless of the current tolerance.  A vertex is spliced
   *   into an edge only if the implementation is unable to distinguish which
   *   side of the edge the vertex lies on.  Two edges are merged only when both
   *   endpoints are identical.
   * </UL>
   *
   * @param which
   *        Specifies the property to be set.  Valid values are
   *        <b>GLU_TESS_WINDING_RULE</b>, <b>GLU_TESS_BOUNDARDY_ONLY</b>,
   *        <b>GLU_TESS_TOLERANCE</b>.
   * @param value
   *        Specifices the value of the indicated property.
   *
   * @see #gluGetTessProperty gluGetTessProperty
   ****************************************************************************/
  void gluTessProperty(int which, double value);

  /*****************************************************************************
   * <b>gluGetTessProperty</b> retrieves properties stored in a tessellation
   * object.  These properties affect the way that tessellation objects are
   * interpreted and rendered.  See the
   * {@link #gluTessProperty gluTessProperty} reference
   * page for information about the properties and what they do.
   *
   * Optional, throws GLException if not available in profile
   *
   * @param which
   *        Specifies the property whose value is to be fetched. Valid values
   *        are <b>GLU_TESS_WINDING_RULE</b>, <b>GLU_TESS_BOUNDARY_ONLY</b>,
   *        and <b>GLU_TESS_TOLERANCES</b>.
   * @param value
   *        Specifices an array into which the value of the named property is
   *        written.
   *
   * @see #gluTessProperty gluTessProperty
   ****************************************************************************/
  void gluGetTessProperty(int which, double[] value, int value_offset);

  /*****************************************************************************
   * <b>gluTessNormal</b> describes a normal for a polygon that the program is
   * defining. All input data will be projected onto a plane perpendicular to
   * the one of the three coordinate axes before tessellation and all output
   * triangles will be oriented CCW with repsect to the normal (CW orientation
   * can be obtained by reversing the sign of the supplied normal).  For
   * example, if you know that all polygons lie in the x-y plane, call
   * <b>gluTessNormal</b>(tess, 0.0, 0.0, 0.0) before rendering any polygons.<P>
   *
   * If the supplied normal is (0.0, 0.0, 0.0)(the initial value), the normal
   * is determined as follows.  The direction of the normal, up to its sign, is
   * found by fitting a plane to the vertices, without regard to how the
   * vertices are connected.  It is expected that the input data lies
   * approximately in the plane; otherwise, projection perpendicular to one of
   * the three coordinate axes may substantially change the geometry.  The sign
   * of the normal is chosen so that the sum of the signed areas of all input
   * contours is nonnegative (where a CCW contour has positive area).<P>
   *
   * The supplied normal persists until it is changed by another call to
   * <b>gluTessNormal</b>.
   *
   * Optional, throws GLException if not available in profile
   *
   * @param x
   *        Specifies the first component of the normal.
   * @param y
   *        Specifies the second component of the normal.
   * @param z
   *        Specifies the third component of the normal.
   *
   * @see #gluTessBeginPolygon gluTessBeginPolygon
   * @see #gluTessEndPolygon   gluTessEndPolygon
   ****************************************************************************/
  void gluTessNormal(double x, double y, double z);

  /*****************************************************************************
   * <b>gluTessCallback</b> is used to indicate a callback to be used by a
   * tessellation object. If the specified callback is already defined, then it
   * is replaced. If <i>aCallback</i> is null, then the existing callback
   * becomes undefined.<P>
   *
   * Optional, throws GLException if not available in profile
   *
   * These callbacks are used by the tessellation object to describe how a
   * polygon specified by the user is broken into triangles. Note that there are
   * two versions of each callback: one with user-specified polygon data and one
   * without. If both versions of a particular callback are specified, then the
   * callback with user-specified polygon data will be used. Note that the
   * polygonData parameter used by some of the methods is a copy of the
   * reference that was specified when
   * {@link #gluTessBeginPolygon gluTessBeginPolygon}
   * was called. The legal callbacks are as follows:<P>
   *
   * <b>GLU_TESS_BEGIN</b>
   * <UL>
   *   The begin callback is invoked like {@link javax.media.opengl.GL#glBegin
   *   glBegin} to indicate the start of a (triangle) primitive. The method
   *   takes a single argument of type int. If the
   *   <b>GLU_TESS_BOUNDARY_ONLY</b> property is set to <b>GL_FALSE</b>, then
   *   the argument is set to either <b>GL_TRIANGLE_FAN</b>,
   *   <b>GL_TRIANGLE_STRIP</b>, or <b>GL_TRIANGLES</b>. If the
   *   <b>GLU_TESS_BOUNDARY_ONLY</b> property is set to <b>GL_TRUE</b>, then the
   *   argument will be set to <b>GL_LINE_LOOP</b>. The method prototype for
   *   this callback is:
   * </UL>
   *
   * <PRE>
   *         void begin(int type);</PRE><P>
   *
   * <b>GLU_TESS_BEGIN_DATA</b>
   * <UL>
   *   The same as the <b>GLU_TESS_BEGIN</b> callback except
   *   that it takes an additional reference argument. This reference is
   *   identical to the opaque reference provided when
   *   {@link #gluTessBeginPolygon gluTessBeginPolygon}
   *   was called. The method prototype for this callback is:
   * </UL>
   *
   * <PRE>
   *         void beginData(int type, Object polygonData);</PRE>
   *
   * <b>GLU_TESS_EDGE_FLAG</b>
   * <UL>
   *   The edge flag callback is similar to
   *   {@link javax.media.opengl.GL#glEdgeFlag glEdgeFlag}. The method takes
   *   a single boolean boundaryEdge that indicates which edges lie on the
   *   polygon boundary. If the boundaryEdge is <b>GL_TRUE</b>, then each vertex
   *   that follows begins an edge that lies on the polygon boundary, that is,
   *   an edge that separates an interior region from an exterior one. If the
   *   boundaryEdge is <b>GL_FALSE</b>, then each vertex that follows begins an
   *   edge that lies in the polygon interior. The edge flag callback (if
   *   defined) is invoked before the first vertex callback.<P>
   *
   *   Since triangle fans and triangle strips do not support edge flags, the
   *   begin callback is not called with <b>GL_TRIANGLE_FAN</b> or
   *   <b>GL_TRIANGLE_STRIP</b> if a non-null edge flag callback is provided.
   *   (If the callback is initialized to null, there is no impact on
   *   performance). Instead, the fans and strips are converted to independent
   *   triangles. The method prototype for this callback is:
   * </UL>
   *
   * <PRE>
   *         void edgeFlag(boolean boundaryEdge);</PRE>
   *
   * <b>GLU_TESS_EDGE_FLAG_DATA</b>
   * <UL>
   *   The same as the <b>GLU_TESS_EDGE_FLAG</b> callback except that it takes
   *   an additional reference argument. This reference is identical to the
   *   opaque reference provided when
   *   {@link #gluTessBeginPolygon gluTessBeginPolygon}
   *   was called. The method prototype for this callback is:
   * </UL>
   *
   * <PRE>
   *         void edgeFlagData(boolean boundaryEdge, Object polygonData);</PRE>
   *
   * <b>GLU_TESS_VERTEX</b>
   * <UL>
   *   The vertex callback is invoked between the begin and end callbacks. It is
   *   similar to {@link javax.media.opengl.GL#glVertex3f glVertex3f}, and it
   *   defines the vertices of the triangles created by the tessellation
   *   process. The method takes a reference as its only argument. This
   *   reference is identical to the opaque reference provided by the user when
   *   the vertex was described (see
   *   {@link #gluTessVertex gluTessVertex}). The method
   *   prototype for this callback is:
   * </UL>
   *
   * <PRE>
   *         void vertex(Object vertexData);</PRE>
   *
   * <b>GLU_TESS_VERTEX_DATA</b>
   * <UL>
   *   The same as the <b>GLU_TESS_VERTEX</b> callback except that it takes an
   *   additional reference argument. This reference is identical to the opaque
   *   reference provided when
   *   {@link #gluTessBeginPolygon gluTessBeginPolygon}
   *   was called. The method prototype for this callback is:
   * </UL>
   *
   * <PRE>
   *         void vertexData(Object vertexData, Object polygonData);</PRE>
   *
   * <b>GLU_TESS_END</b>
   * <UL>
   *   The end callback serves the same purpose as
   *   {@link javax.media.opengl.GL#glEnd glEnd}. It indicates the end of a
   *   primitive and it takes no arguments. The method prototype for this
   *   callback is:
   * </UL>
   *
   * <PRE>
   *         void end();</PRE>
   *
   * <b>GLU_TESS_END_DATA</b>
   * <UL>
   *   The same as the <b>GLU_TESS_END</b> callback except that it takes an
   *   additional reference argument. This reference is identical to the opaque
   *   reference provided when
   *   {@link #gluTessBeginPolygon gluTessBeginPolygon}
   *   was called. The method prototype for this callback is:
   * </UL>
   *
   * <PRE>
   *         void endData(Object polygonData);</PRE>
   *
   * <b>GLU_TESS_COMBINE</b>
   * <UL>
   *   The combine callback is called to create a new vertex when the
   *   tessellation detects an intersection, or wishes to merge features. The
   *   method takes four arguments: an array of three elements each of type
   *   double, an array of four references, an array of four elements each of
   *   type float, and a reference to a reference. The prototype is:
   * </UL>
   *
   * <PRE>
   *         void combine(double[] coords, Object[] data,
   *                      float[] weight, Object[] outData);</PRE>
   *
   * <UL>
   *   The vertex is defined as a linear combination of up to four existing
   *   vertices, stored in <i>data</i>. The coefficients of the linear
   *   combination are given by <i>weight</i>; these weights always add up to 1.
   *   All vertex pointers are valid even when some of the weights are 0.
   *   <i>coords</i> gives the location of the new vertex.<P>
   *
   *   The user must allocate another vertex, interpolate parameters using
   *   <i>data</i> and <i>weight</i>, and return the new vertex pointer
   *   in <i>outData</i>. This handle is supplied during rendering callbacks.
   *   The user is responsible for freeing the memory some time after
   *   {@link #gluTessEndPolygon gluTessEndPolygon} is
   *   called.<P>
   *
   *   For example, if the polygon lies in an arbitrary plane in 3-space, and a
   *   color is associated with each vertex, the <b>GLU_TESS_COMBINE</b>
   *   callback might look like this:
   * </UL>
   * <PRE>
   *         void myCombine(double[] coords, Object[] data,
   *                        float[] weight, Object[] outData)
   *         {
   *            MyVertex newVertex = new MyVertex();
   *
   *            newVertex.x = coords[0];
   *            newVertex.y = coords[1];
   *            newVertex.z = coords[2];
   *            newVertex.r = weight[0]*data[0].r +
   *                          weight[1]*data[1].r +
   *                          weight[2]*data[2].r +
   *                          weight[3]*data[3].r;
   *            newVertex.g = weight[0]*data[0].g +
   *                          weight[1]*data[1].g +
   *                          weight[2]*data[2].g +
   *                          weight[3]*data[3].g;
   *            newVertex.b = weight[0]*data[0].b +
   *                          weight[1]*data[1].b +
   *                          weight[2]*data[2].b +
   *                          weight[3]*data[3].b;
   *            newVertex.a = weight[0]*data[0].a +
   *                          weight[1]*data[1].a +
   *                          weight[2]*data[2].a +
   *                          weight[3]*data[3].a;
   *            outData = newVertex;
   *         }</PRE>
   *
   * <UL>
   *   If the tessellation detects an intersection, then the
   *   <b>GLU_TESS_COMBINE</b> or <b>GLU_TESS_COMBINE_DATA</b> callback (see
   *   below) must be defined, and it must write a non-null reference into
   *   <i>outData</i>. Otherwise the <b>GLU_TESS_NEED_COMBINE_CALLBACK</b> error
   *   occurs, and no output is generated.
   * </UL>
   *
   * <b>GLU_TESS_COMBINE_DATA</b>
   * <UL>
   *   The same as the <b>GLU_TESS_COMBINE</b> callback except that it takes an
   *   additional reference argument. This reference is identical to the opaque
   *   reference provided when
   *   {@link #gluTessBeginPolygon gluTessBeginPolygon}
   *   was called. The method prototype for this callback is:
   * </UL>
   *
   * <PRE>
   *         void combineData(double[] coords, Object[] data,
                              float[] weight, Object[] outData,
                              Object polygonData);</PRE>
   *
   * <b>GLU_TESS_ERROR</b>
   * <UL>
   *   The error callback is called when an error is encountered. The one
   *   argument is of type int; it indicates the specific error that occurred
   *   and will be set to one of <b>GLU_TESS_MISSING_BEGIN_POLYGON</b>,
   *   <b>GLU_TESS_MISSING_END_POLYGON</b>,
   *   <b>GLU_TESS_MISSING_BEGIN_CONTOUR</b>,
   *   <b>GLU_TESS_MISSING_END_CONTOUR</b>, <b>GLU_TESS_COORD_TOO_LARGE</b>,
   *   <b>GLU_TESS_NEED_COMBINE_CALLBACK</b> or <b>GLU_OUT_OF_MEMORY</b>.
   *   Character strings describing these errors can be retrieved with the
   *   {@link javax.media.opengl.glu.GLU#gluErrorString gluErrorString} call. The
   *   method prototype for this callback is:
   * </UL>
   *
   * <PRE>
   *         void error(int errnum);</PRE>
   *
   * <UL>
   *   The GLU library will recover from the first four errors by inserting the
   *   missing call(s). <b>GLU_TESS_COORD_TOO_LARGE</b> indicates that some
   *   vertex coordinate exceeded the predefined constant
   *   <b>GLU_TESS_MAX_COORD</b> in absolute value, and that the value has been
   *   clamped. (Coordinate values must be small enough so that two can be
   *   multiplied together without overflow.)
   *   <b>GLU_TESS_NEED_COMBINE_CALLBACK</b> indicates that the tessellation
   *   detected an intersection between two edges in the input data, and the
   *   <b>GLU_TESS_COMBINE</b> or <b>GLU_TESS_COMBINE_DATA</b> callback was not
   *   provided. No output is generated. <b>GLU_OUT_OF_MEMORY</b> indicates that
   *   there is not enough memory so no output is generated.
   * </UL>
   *
   * <b>GLU_TESS_ERROR_DATA</b>
   * <UL>
   *   The same as the GLU_TESS_ERROR callback except that it takes an
   *   additional reference argument. This reference is identical to the opaque
   *   reference provided when
   *   {@link #gluTessBeginPolygon gluTessBeginPolygon}
   *   was called. The method prototype for this callback is:
   * </UL>
   *
   * <PRE>
   *         void errorData(int errnum, Object polygonData);</PRE>
   *
   * @param which
   *        Specifies the callback being defined. The following values are
   *        valid: <b>GLU_TESS_BEGIN</b>, <b>GLU_TESS_BEGIN_DATA</b>,
   *        <b>GLU_TESS_EDGE_FLAG</b>, <b>GLU_TESS_EDGE_FLAG_DATA</b>,
   *        <b>GLU_TESS_VERTEX</b>, <b>GLU_TESS_VERTEX_DATA</b>,
   *        <b>GLU_TESS_END</b>, <b>GLU_TESS_END_DATA</b>,
   *        <b>GLU_TESS_COMBINE</b>,  <b>GLU_TESS_COMBINE_DATA</b>,
   *        <b>GLU_TESS_ERROR</b>, and <b>GLU_TESS_ERROR_DATA</b>.
   * @param aCallback
   *        Specifies the callback object to be called.
   *
   * @see javax.media.opengl.GL#glBegin              glBegin
   * @see javax.media.opengl.GL#glEdgeFlag           glEdgeFlag
   * @see javax.media.opengl.GL#glVertex3f           glVertex3f
   * @see javax.media.opengl.glu.GLU#gluErrorString      gluErrorString
   * @see #gluTessVertex       gluTessVertex
   * @see #gluTessBeginPolygon gluTessBeginPolygon
   * @see #gluTessBeginContour gluTessBeginContour
   * @see #gluTessProperty     gluTessProperty
   * @see #gluTessNormal       gluTessNormal
   ****************************************************************************/
  void gluTessCallback(int which, GLUtessellatorCallback aCallback);

  /*****************************************************************************
   * <b>gluTessVertex</b> describes a vertex on a polygon that the program
   * defines. Successive <b>gluTessVertex</b> calls describe a closed contour.
   * For example, to describe a quadrilateral <b>gluTessVertex</b> should be
   * called four times. <b>gluTessVertex</b> can only be called between
   * {@link #gluTessBeginContour gluTessBeginContour} and
   * {@link #gluTessBeginContour gluTessEndContour}.<P>
   *
   * Optional, throws GLException if not available in profile
   *
   * <b>data</b> normally references to a structure containing the vertex
   * location, as well as other per-vertex attributes such as color and normal.
   * This reference is passed back to the user through the
   * <b>GLU_TESS_VERTEX</b> or <b>GLU_TESS_VERTEX_DATA</b> callback after
   * tessellation (see the {@link #gluTessCallback
   * gluTessCallback} reference page).
   *
   * @param coords
   *        Specifies the coordinates of the vertex.
   * @param data
   *        Specifies an opaque reference passed back to the program with the
   *        vertex callback (as specified by
   *        {@link #gluTessCallback gluTessCallback}).
   *
   * @see #gluTessBeginPolygon gluTessBeginPolygon
   * @see #gluTessBeginContour gluTessBeginContour
   * @see #gluTessCallback     gluTessCallback
   * @see #gluTessProperty     gluTessProperty
   * @see #gluTessNormal       gluTessNormal
   * @see #gluTessEndPolygon   gluTessEndPolygon
   ****************************************************************************/
  void gluTessVertex(double[] coords, int coords_offset, Object data);

  /*****************************************************************************
   * <b>gluTessBeginPolygon</b> and
   * {@link #gluTessEndPolygon gluTessEndPolygon} delimit
   * the definition of a convex, concave or self-intersecting polygon. Within
   * each <b>gluTessBeginPolygon</b>/
   * {@link #gluTessEndPolygon gluTessEndPolygon} pair,
   * there must be one or more calls to
   * {@link #gluTessBeginContour gluTessBeginContour}/
   * {@link #gluTessEndContour gluTessEndContour}. Within
   * each contour, there are zero or more calls to
   * {@link #gluTessVertex gluTessVertex}. The vertices
   * specify a closed contour (the last vertex of each contour is automatically
   * linked to the first). See the {@link #gluTessVertex
   * gluTessVertex}, {@link #gluTessBeginContour
   * gluTessBeginContour}, and {@link #gluTessEndContour
   * gluTessEndContour} reference pages for more details.<P>
   *
   * Optional, throws GLException if not available in profile
   *
   * <b>data</b> is a reference to a user-defined data structure. If the
   * appropriate callback(s) are specified (see
   * {@link #gluTessCallback gluTessCallback}), then this
   * reference is returned to the callback method(s). Thus, it is a convenient
   * way to store per-polygon information.<P>
   *
   * Once {@link #gluTessEndPolygon gluTessEndPolygon} is
   * called, the polygon is tessellated, and the resulting triangles are
   * described through callbacks. See
   * {@link #gluTessCallback gluTessCallback} for
   * descriptions of the callback methods.
   *
   * @param data
   *        Specifies a reference to user polygon data.
   *
   * @see #gluTessBeginContour gluTessBeginContour
   * @see #gluTessVertex       gluTessVertex
   * @see #gluTessCallback     gluTessCallback
   * @see #gluTessProperty     gluTessProperty
   * @see #gluTessNormal       gluTessNormal
   * @see #gluTessEndPolygon   gluTessEndPolygon
   ****************************************************************************/
  void gluTessBeginPolygon(Object data);

  /*****************************************************************************
   * <b>gluTessBeginContour</b> and
   * {@link #gluTessEndContour gluTessEndContour} delimit
   * the definition of a polygon contour. Within each
   * <b>gluTessBeginContour</b>/
   * {@link #gluTessEndContour gluTessEndContour} pair,
   * there can be zero or more calls to
   * {@link #gluTessVertex gluTessVertex}. The vertices
   * specify a closed contour (the last vertex of each contour is automatically
   * linked to the first). See the {@link #gluTessVertex
   * gluTessVertex} reference page for more details. <b>gluTessBeginContour</b>
   * can only be called between
   * {@link #gluTessBeginPolygon gluTessBeginPolygon} and
   * {@link #gluTessEndPolygon gluTessEndPolygon}.
   *
   * Optional, throws GLException if not available in profile
   *
   *
   * @see #gluTessBeginPolygon gluTessBeginPolygon
   * @see #gluTessVertex       gluTessVertex
   * @see #gluTessCallback     gluTessCallback
   * @see #gluTessProperty     gluTessProperty
   * @see #gluTessNormal       gluTessNormal
   * @see #gluTessEndPolygon   gluTessEndPolygon
   ****************************************************************************/
  void gluTessBeginContour();

  /*****************************************************************************
   *  <b>gluTessEndContour</b> and
   * {@link #gluTessBeginContour gluTessBeginContour}
   * delimit the definition of a polygon contour. Within each
   * {@link #gluTessBeginContour gluTessBeginContour}/
   * <b>gluTessEndContour</b> pair, there can be zero or more calls to
   * {@link #gluTessVertex gluTessVertex}. The vertices
   * specify a closed contour (the last vertex of each contour is automatically
   * linked to the first). See the {@link #gluTessVertex
   * gluTessVertex} reference page for more details.
   * {@link #gluTessBeginContour gluTessBeginContour} can
   * only be called between {@link #gluTessBeginPolygon
   * gluTessBeginPolygon} and
   * {@link #gluTessEndPolygon gluTessEndPolygon}.
   *
   * Optional, throws GLException if not available in profile
   *
   *
   * @see #gluTessBeginPolygon gluTessBeginPolygon
   * @see #gluTessVertex       gluTessVertex
   * @see #gluTessCallback     gluTessCallback
   * @see #gluTessProperty     gluTessProperty
   * @see #gluTessNormal       gluTessNormal
   * @see #gluTessEndPolygon   gluTessEndPolygon
   ****************************************************************************/
  void gluTessEndContour();

  /*****************************************************************************
   * <b>gluTessEndPolygon</b> and
   * {@link #gluTessBeginPolygon gluTessBeginPolygon}
   * delimit the definition of a convex, concave or self-intersecting polygon.
   * Within each {@link #gluTessBeginPolygon
   * gluTessBeginPolygon}/<b>gluTessEndPolygon</b> pair, there must be one or
   * more calls to {@link #gluTessBeginContour
   * gluTessBeginContour}/{@link #gluTessEndContour
   * gluTessEndContour}. Within each contour, there are zero or more calls to
   * {@link #gluTessVertex gluTessVertex}. The vertices
   * specify a closed contour (the last vertex of each contour is automatically
   * linked to the first). See the {@link #gluTessVertex
   * gluTessVertex}, {@link #gluTessBeginContour
   * gluTessBeginContour} and {@link #gluTessEndContour
   * gluTessEndContour} reference pages for more details.<P>
   *
   * Optional, throws GLException if not available in profile
   *
   * Once <b>gluTessEndPolygon</b> is called, the polygon is tessellated, and
   * the resulting triangles are described through callbacks. See
   * {@link #gluTessCallback gluTessCallback} for
   * descriptions of the callback functions.
   *
   *
   * @see #gluTessBeginContour gluTessBeginContour
   * @see #gluTessVertex       gluTessVertex
   * @see #gluTessCallback     gluTessCallback
   * @see #gluTessProperty     gluTessProperty
   * @see #gluTessNormal       gluTessNormal
   * @see #gluTessBeginPolygon gluTessBeginPolygon
   ****************************************************************************/
  void gluTessEndPolygon();

  /*****************************************************************************

   * <b>gluBeginPolygon</b> and {@link #gluEndPolygon gluEndPolygon}
   * delimit the definition of a nonconvex polygon. To define such a
   * polygon, first call <b>gluBeginPolygon</b>. Then define the
   * contours of the polygon by calling {@link #gluTessVertex
   * gluTessVertex} for each vertex and {@link #gluNextContour
   * gluNextContour} to start each new contour. Finally, call {@link
   * #gluEndPolygon gluEndPolygon} to signal the end of the
   * definition. See the {@link #gluTessVertex gluTessVertex} and {@link
   * #gluNextContour gluNextContour} reference pages for more
   * details.<P>
   *
   * Optional, throws GLException if not available in profile
   *
   * Once {@link #gluEndPolygon gluEndPolygon} is called,
   * the polygon is tessellated, and the resulting triangles are described
   * through callbacks. See {@link #gluTessCallback
   * gluTessCallback} for descriptions of the callback methods.
   *
   *
   * @see #gluNextContour      gluNextContour
   * @see #gluTessCallback     gluTessCallback
   * @see #gluTessVertex       gluTessVertex
   * @see #gluTessBeginPolygon gluTessBeginPolygon
   * @see #gluTessBeginContour gluTessBeginContour
   ****************************************************************************/
  void gluBeginPolygon();

  /*****************************************************************************
   * <b>gluNextContour</b> is used to describe polygons with multiple
   * contours. After you describe the first contour through a series of
   * {@link #gluTessVertex gluTessVertex} calls, a
   * <b>gluNextContour</b> call indicates that the previous contour is complete
   * and that the next contour is about to begin. Perform another series of
   * {@link #gluTessVertex gluTessVertex} calls to
   * describe the new contour. Repeat this process until all contours have been
   * described.<P>
   *
   * Optional, throws GLException if not available in profile
   *
   * The type parameter defines what type of contour follows. The following
   * values are valid. <P>
   *
   * <b>GLU_EXTERIOR</b>
   * <UL>
   *   An exterior contour defines an exterior boundary of the polygon.
   * </UL>
   * <b>GLU_INTERIOR</b>
   * <UL>
   *   An interior contour defines an interior boundary of the polygon (such as
   *   a hole).
   * </UL>
   * <b>GLU_UNKNOWN</b>
   * <UL>
   *   An unknown contour is analyzed by the library to determine whether it is
   *   interior or exterior.
   * </UL>
   * <b>GLU_CCW, GLU_CW</b>
   * <UL>
   *   The first <b>GLU_CCW</b> or <b>GLU_CW</b> contour defined is considered
   *   to be exterior. All other contours are considered to be exterior if they
   *   are oriented in the same direction (clockwise or counterclockwise) as the
   *   first contour, and interior if they are not. If one contour is of type
   *   <b>GLU_CCW</b> or <b>GLU_CW</b>, then all contours must be of the same
   *   type (if they are not, then all <b>GLU_CCW</b> and <b>GLU_CW</b> contours
   *   will be changed to <b>GLU_UNKNOWN</b>). Note that there is no
   *   real difference between the <b>GLU_CCW</b> and <b>GLU_CW</b> contour
   *   types.
   * </UL><P>
   *
   * To define the type of the first contour, you can call <b>gluNextContour</b>
   * before describing the first contour. If you do not call
   * <b>gluNextContour</b> before the first contour, the first contour is marked
   * <b>GLU_EXTERIOR</b>.<P>
   *
   * <UL>
   *   <b>Note:</b>  The <b>gluNextContour</b> function is obsolete and is
   *   provided for backward compatibility only. The <b>gluNextContour</b>
   *   function is mapped to {@link #gluTessEndContour
   *   gluTessEndContour} followed by
   *   {@link #gluTessBeginContour gluTessBeginContour}.
   * </UL>
   *
   * @param type
   *        The type of the contour being defined.
   *
   * @see #gluTessBeginContour gluTessBeginContour
   * @see #gluTessBeginPolygon gluTessBeginPolygon
   * @see #gluTessCallback     gluTessCallback
   * @see #gluTessEndContour   gluTessEndContour
   * @see #gluTessVertex       gluTessVertex
   ****************************************************************************/
  void gluNextContour(int type);

  /*****************************************************************************
   * <b>gluEndPolygon</b> and {@link #gluBeginPolygon
   * gluBeginPolygon} delimit the definition of a nonconvex polygon. To define
   * such a polygon, first call {@link #gluBeginPolygon
   * gluBeginPolygon}. Then define the contours of the polygon by calling
   * {@link #gluTessVertex gluTessVertex} for each vertex
   * and {@link #gluNextContour gluNextContour} to start
   * each new contour. Finally, call <b>gluEndPolygon</b> to signal the end of
   * the definition. See the {@link #gluTessVertex
   * gluTessVertex} and {@link #gluNextContour
   * gluNextContour} reference pages for more details.<P>
   *
   * Optional, throws GLException if not available in profile
   *
   * Once <b>gluEndPolygon</b> is called, the polygon is tessellated, and the
   * resulting triangles are described through callbacks. See
   * {@link #gluTessCallback gluTessCallback} for
   * descriptions of the callback methods.
   *
   *
   * @see #gluNextContour      gluNextContour
   * @see #gluTessCallback     gluTessCallback
   * @see #gluTessVertex       gluTessVertex
   * @see #gluTessBeginPolygon gluTessBeginPolygon
   * @see #gluTessBeginContour gluTessBeginContour
   ****************************************************************************/
  void gluEndPolygon();

  int GLU_FALSE = 0;
  int GLU_TRUE = 1;

  int GLU_VERSION = 100800;
  int GLU_EXTENSIONS = 100801;

  String versionString = "1.3";
  String extensionString = "GLU_EXT_nurbs_tessellator " +
                                               "GLU_EXT_object_space_tess ";

  int GLU_INVALID_ENUM = 100900;
  int GLU_INVALID_VALUE = 100901;
  int GLU_OUT_OF_MEMORY = 100902;
  int GLU_INVALID_OPERATION = 100904;


  int GLU_POINT = 100010;
  int GLU_LINE = 100011;
  int GLU_FILL = 100012;
  int GLU_SILHOUETTE = 100013;


  int GLU_SMOOTH = 100000;
  int GLU_FLAT = 100001;
  int GLU_NONE = 100002;

  int GLU_OUTSIDE = 100020;
  int GLU_INSIDE = 100021;


  int GLU_ERROR = 100103;






  int GLU_TESS_BEGIN = 100100;
  int GLU_BEGIN = 100100;
  int GLU_TESS_VERTEX = 100101;
  int GLU_VERTEX = 100101;
  int GLU_TESS_END = 100102;
  int GLU_END = 100102;
  int GLU_TESS_ERROR = 100103;
  int GLU_TESS_EDGE_FLAG = 100104;
  int GLU_EDGE_FLAG = 100104;
  int GLU_TESS_COMBINE = 100105;
  int GLU_TESS_BEGIN_DATA = 100106;
  int GLU_TESS_VERTEX_DATA = 100107;
  int GLU_TESS_END_DATA = 100108;
  int GLU_TESS_ERROR_DATA = 100109;
  int GLU_TESS_EDGE_FLAG_DATA = 100110;
  int GLU_TESS_COMBINE_DATA = 100111;

  int GLU_CW = 100120;
  int GLU_CCW = 100121;
  int GLU_INTERIOR = 100122;
  int GLU_EXTERIOR = 100123;
  int GLU_UNKNOWN = 100124;

  int GLU_TESS_WINDING_RULE = 100140;
  int GLU_TESS_BOUNDARY_ONLY = 100141;
  int GLU_TESS_TOLERANCE = 100142;
  int GLU_TESS_AVOID_DEGENERATE_TRIANGLES = 100149;

  int GLU_TESS_ERROR1 = 100151;
  int GLU_TESS_ERROR2 = 100152;
  int GLU_TESS_ERROR3 = 100153;
  int GLU_TESS_ERROR4 = 100154;
  int GLU_TESS_ERROR5 = 100155;
  int GLU_TESS_ERROR6 = 100156;
  int GLU_TESS_ERROR7 = 100157;
  int GLU_TESS_ERROR8 = 100158;
  int GLU_TESS_MISSING_BEGIN_POLYGON = 100151;
  int GLU_TESS_MISSING_BEGIN_CONTOUR = 100152;
  int GLU_TESS_MISSING_END_POLYGON = 100153;
  int GLU_TESS_MISSING_END_CONTOUR = 100154;
  int GLU_TESS_COORD_TOO_LARGE = 100155;
  int GLU_TESS_NEED_COMBINE_CALLBACK = 100156;

  int GLU_TESS_WINDING_ODD = 100130;
  int GLU_TESS_WINDING_NONZERO = 100131;
  int GLU_TESS_WINDING_POSITIVE = 100132;
  int GLU_TESS_WINDING_NEGATIVE = 100133;
  int GLU_TESS_WINDING_ABS_GEQ_TWO = 100134;
  double GLU_TESS_MAX_COORD = 1.0e150;


  public static final int GL_LINE_LOOP = 0x0002;
  public static final int GL_TRIANGLES = 0x0004;
  public static final int GL_TRIANGLE_STRIP = 0x0005;
  public static final int GL_TRIANGLE_FAN = 0x0006;

}
