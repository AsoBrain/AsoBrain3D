/*
 * (C) Copyright Numdata BV 2013-2013 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

/**
 * Common interface for 3D views that use a JOGL-based rendering engine. This
 * allows some of the advanced features supported by this engine to be used
 * without creating a direct dependency on a specific JOGL version.
 *
 * @author Gerrit Meinders
 */
public interface JOGLViewInterface
{
	/**
	 * Returns the view's rendering configuration.
	 *
	 * @return Rendering configuration.
	 */
	JOGLConfiguration getConfiguration();
}
