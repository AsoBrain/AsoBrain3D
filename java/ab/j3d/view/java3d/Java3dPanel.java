/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2003-2004 Numdata BV.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Numdata BV (http://www.numdata.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Numdata" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission of Numdata BV. For written permission, please contact
 *    info@numdata.com.
 *
 * 5. Products derived from this software may not be called "Numdata",
 *    nor may "Numdata" appear in their name, without prior written
 *    permission of Numdata BV.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE NUMDATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package com.numdata.soda.Gerwin.AbtoJ3D;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;

import com.numdata.soda.Gerwin.MyUniverse;

/**
 * This panel can be used to display 3D models using Java 3D. It allows content
 * to be replaced while the panel is visible. Convenience methods have been
 * created to simplify viewing SODA project data.
 *
 * @author  Peter S. Heijnen
 * @version @version $Revision$ $Date$
 */
public class J3dPanel
    extends Canvas3D
{
	/**
	 * Java3D scene.
	 */
	private final BranchGroup _j3dScene;

	/**
	 * Construct Java 3D panel.
	 */
	public J3dPanel( final J3dUniverse universe )
	{
		super( MyUniverse.getPreferredConfiguration() );

		final BranchGroup staticScene = universe.getStaticScene();

		// create sub-tree for dynamic content
		final BranchGroup dynamicScene = new BranchGroup();
//		dynamicScene.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		dynamicScene.setCapability( BranchGroup.ALLOW_CHILDREN_WRITE );
		dynamicScene.setCapability( BranchGroup.ALLOW_CHILDREN_EXTEND );
		staticScene.addChild( dynamicScene );

		_j3dScene = dynamicScene;
	}

	/**
	 * Clear content of panel.
	 */
	public void clearContent()
	{
		_j3dScene.removeAllChildren();
	}

	/**
	 * Set content of panel to the specified <code>BranchGroup</code>. The
	 * <code>ALLOW_DETACH</code> cabaility is set to allow the content to
	 * be removed. After that, the branch group is compiled and added as
	 * dynamic content.
	 *
	 * @param   bg          BranchGroup to set as content.
	 */
	public void setContent( final BranchGroup bg )
	{
		bg.setCapability( BranchGroup.ALLOW_DETACH );
		bg.compile();

		clearContent();
		_j3dScene.addChild( bg );
	}
}
