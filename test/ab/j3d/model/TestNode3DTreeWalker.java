/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * ====================================================================
 */
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import junit.framework.*;
import org.jetbrains.annotations.*;

/**
 * This class tests the {@link Node3DTreeWalker} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestNode3DTreeWalker
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestNode3DTreeWalker.class.getName();

	/**
	 * List of paths in depth-first ordering through the test scene graph.
	 */
	private List<Node3DPath> _depthFirstPaths;

	/**
	 * Root of test scene graph.
	 */
	private Node3D _root;

	/**
	 * Set-up fixture. This created the following scene graph.
	 * <pre>
	 *  root
	 *    |- transform1
	 *    |    |
	 *    |    +- transform2
	 *    |         |
	 *    |         |- node1
	 *    |         |
	 *    |         +- object1
	 *    |              |
	 *    |              +- object2
	 *    |
	 *    +- node2
	 *         |
	 *         |- transform3
	 *         |    |
	 *         |    +- object3
	 *         |
	 *         |- node3
	 *         |
	 *         +- insert1 transform1
	 * </pre>
	 * @throws  Exception if set-up failed.
	 */
	@Override
	protected void setUp()
		throws Exception
	{
		super.setUp();

		final Node3D root = new Node3D();
		final Node3D transform1 = new Transform3D( Matrix3D.getTranslation( 10.0, 0.0, 0.0 ) );
		root.addChild( transform1 );
		final Node3D transform2 = new Transform3D( Matrix3D.getTranslation( 0.0, 10.0, 0.0 ) );
		transform1.addChild( transform2 );
		final Node3D node1 = new Node3D();
		transform2.addChild( node1 );
		final Node3D object1 = new Object3D();
		transform2.addChild( object1 );
		final Node3D object2 = new Object3D();
		object1.addChild( object2 );
		final Node3D node2 = new Node3D();
		root.addChild( node2 );
		final Node3D transform3 = new Transform3D( Matrix3D.getTranslation( 0.0, 0.0, 10.0 ));
		node2.addChild( transform3 );
		final Node3D object3 = new Object3D();
		transform3.addChild( object3 );
		final Node3D node3 = new Node3D();
		node2.addChild( node3 );
		final Node3D insert1 = new Transform3D( Matrix3D.getTranslation( -10.0, -10.0, -10.0 ) ); // -> transform1
		node2.addChild( insert1 );
		insert1.addChild( transform1 );

		final Node3DPath pathRoot        = new Node3DPath( null           , Matrix3D.IDENTITY,                              root );
		final Node3DPath pathTransform1  = new Node3DPath( pathRoot       , Matrix3D.getTranslation(  10.0,   0.0,   0.0 ), transform1 );
		final Node3DPath pathTransform2  = new Node3DPath( pathTransform1 , Matrix3D.getTranslation(  10.0,  10.0,   0.0 ), transform2 );
		final Node3DPath pathNode1       = new Node3DPath( pathTransform2 , Matrix3D.getTranslation(  10.0,  10.0,   0.0 ), node1 );
		final Node3DPath pathObject1     = new Node3DPath( pathTransform2 , Matrix3D.getTranslation(  10.0,  10.0,   0.0 ), object1 );
		final Node3DPath pathObject2     = new Node3DPath( pathObject1    , Matrix3D.getTranslation(  10.0,  10.0,   0.0 ), object2 );
		final Node3DPath pathNode2       = new Node3DPath( pathRoot       , Matrix3D.IDENTITY,                              node2 );
		final Node3DPath pathTransform3  = new Node3DPath( pathNode2      , Matrix3D.getTranslation(   0.0,   0.0,  10.0 ), transform3 );
		final Node3DPath pathObject3     = new Node3DPath( pathTransform3 , Matrix3D.getTranslation(   0.0,   0.0,  10.0 ), object3 );
		final Node3DPath pathNode3       = new Node3DPath( pathNode2      , Matrix3D.IDENTITY,                              node3 );
		final Node3DPath pathInsert1     = new Node3DPath( pathNode2      , Matrix3D.getTranslation( -10.0, -10.0, -10.0 ), insert1 );
		final Node3DPath pathTransform1i = new Node3DPath( pathInsert1    , Matrix3D.getTranslation(   0.0, -10.0, -10.0 ), transform1 );
		final Node3DPath pathTransform2i = new Node3DPath( pathTransform1i, Matrix3D.getTranslation(   0.0,   0.0, -10.0 ), transform2 );
		final Node3DPath pathNode1i      = new Node3DPath( pathTransform2i, Matrix3D.getTranslation(   0.0,   0.0, -10.0 ), node1 );
		final Node3DPath pathObject1i    = new Node3DPath( pathTransform2i, Matrix3D.getTranslation(   0.0,   0.0, -10.0 ), object1 );
		final Node3DPath pathObject2i    = new Node3DPath( pathObject1i   , Matrix3D.getTranslation(   0.0,   0.0, -10.0 ), object2 );

		_root = root;
		_depthFirstPaths = Arrays.asList( pathRoot, pathTransform1, pathTransform2, pathNode1, pathObject1, pathObject2, pathNode2, pathTransform3, pathObject3, pathNode3, pathInsert1, pathTransform1i, pathTransform2i, pathNode1i, pathObject1i, pathObject2i );
	}

	/**
	 * Test the {@link Node3DTreeWalker#walk} method(s).
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testWalk()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testWalk()" );

		final Node3D root = _root;
		final List<Node3DPath> expectedPaths = _depthFirstPaths;

		for ( int abortAt = -1 ; abortAt < expectedPaths.size(); abortAt++ )
		{
			final TestVisitor testVisitor = new TestVisitor( abortAt );
			Node3DTreeWalker.walk( testVisitor, root );
			final List<Node3DPath> visitedPaths = testVisitor._visitedNodePaths;

			final Map<Node3DPath,Node3DPath> exptectedToCollectedPath = new HashMap<Node3DPath, Node3DPath>();
			final int expectedNumberOfVisitedPaths = ( ( abortAt >= 0 ) && ( abortAt < expectedPaths.size() ) ) ? ( abortAt + 1 ) : expectedPaths.size();

			for ( int i = 0; i < Math.min( visitedPaths.size(), expectedNumberOfVisitedPaths ); i++ )
			{
				final Node3DPath expectedPath = expectedPaths.get( i );
				final Node3DPath visitedPath =  visitedPaths.get( i );

				Node3DPath expectedParent = expectedPath.getParent();
				if ( expectedParent != null )
				{
					expectedParent = exptectedToCollectedPath.get( expectedParent );
					assertNotNull( "Error in unit test data, path[" + i + " ] does not point to previously visited path", expectedParent );
				}

				assertSame( "paths[" + i + "].node", expectedPath.getNode(), visitedPath.getNode() );
				assertEquals( "paths[" + i + "].transform", expectedPath.getTransform(), visitedPath.getTransform() );
				assertEquals( "paths[" + i + "].parent", expectedParent, visitedPath.getParent() );

				exptectedToCollectedPath.put( expectedPath, visitedPath );
			}

			assertEquals( "Unexpected number of visited nodes (abortAt=" + abortAt + ')', expectedNumberOfVisitedPaths, visitedPaths.size() );
		}
	}

	/**
	 * Concrete implementation of {@link Node3DVisitor} for testing.
	 */
	private static class TestVisitor
		implements Node3DVisitor
	{
		/**
		 * Paths to nodes that were visited in order of visit.
		 */
		final List<Node3DPath> _visitedNodePaths = new ArrayList<Node3DPath>();

		/**
		 * Index of visited node to abort tree walk at (-1 if never).
		 */
		final int _abortAt;

		/**
		 * Create tree walker.
		 *
		 * @param   abortAt     Index to abort tree walk at (-1 if never).
		 */
		TestVisitor( final int abortAt )
		{
			_abortAt = abortAt;
		}

		public boolean visitNode( @NotNull final Node3DPath path )
		{
			_visitedNodePaths.add( path );
			return ( _abortAt < 0 ) || ( _visitedNodePaths.size() <= _abortAt );
		}
	}
}
