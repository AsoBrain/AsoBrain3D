/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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
package ab.j3d.view.control.planar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import ab.j3d.control.ControlInputEvent;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

/**
 * This class implements {@link SubPlaneControl} to control borders/corners of
 * the drag plane.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class SubPlaneBorderControl
	extends AbstractSubPlaneControl
{
	/**
	 * Size of border to distinguish manipulation areas.
	 */
	private final double _borderSize;

	/**
	 * Manipulation mode.
	 */
	public static enum ManipulationMode
	{
		/** Not manipulating a defined area. */ NONE ,
		/** Manipulating top-left corner.    */ TOP_LEFT ,
		/** Manipulating top border.         */ TOP ,
		/** Manipulating top-right corner.   */ TOP_RIGHT ,
		/** Manipulating left border.        */ LEFT ,
		/** Manipulating right border.       */ RIGHT ,
		/** Manipulating bottom-left corner. */ BOTTOM_LEFT ,
		/** Manipulating bottom border.      */ BOTTOM ,
		/** Manipulating bottom-right corner.*/ BOTTOM_RIGHT ,
	}

	/**
	 * Current manipulation mode.
	 */
	private ManipulationMode _manipulationMode;

	/**
	 * Construct control.
	 */
	protected SubPlaneBorderControl()
	{
		_manipulationMode = ManipulationMode.NONE;
		_borderSize = 100.0;
	}

	public void mousePressed( final ControlInputEvent event , final ViewModelNode viewModelNode , final double x , final double y )
	{
		super.mousePressed( event , viewModelNode , x , y );

		final double min  = _borderSize;
		final double maxX = getPlaneWidth() - min;
		final double maxY = getPlaneHeight() - min;

		final ManipulationMode manipulationMode = ( x < min  ) ? ( y < min  ) ? ManipulationMode.BOTTOM_LEFT :
		                                             ( y > maxY ) ? ManipulationMode.TOP_LEFT :
		                                                            ManipulationMode.LEFT :
		                              ( x > maxX ) ? ( y < min  ) ? ManipulationMode.BOTTOM_RIGHT :
		                                             ( y > maxY ) ? ManipulationMode.TOP_RIGHT :
		                                                            ManipulationMode.RIGHT :
		                                             ( y < min  ) ? ManipulationMode.BOTTOM :
		                                             ( y > maxY ) ? ManipulationMode.TOP :
		                                                            ManipulationMode.NONE;

		_manipulationMode = manipulationMode;
	}

	/**
	 * Get manipulation mode.
	 *
	 * @return  Manipulation mode.
	 */
	public ManipulationMode getManipulationMode()
	{
		return _manipulationMode;
	}

	/**
	 * Get a resize rectangle relative to itself.
	 *
	 * @return  Resize rectangle;
	 *          <code>null</code> if the resized rectangle can not be determined.
	 */
	public Rectangle2D getResizeRectangle()
	{
		final Rectangle2D result;

		final double endX = getEndX();
		final double endY = getEndY();
		final double maxX = getPlaneWidth();
		final double maxY = getPlaneHeight();

		switch ( getManipulationMode() )
		{
			case TOP_LEFT     : result = ( endX < maxX ) && ( endY > 0.0  ) ? new Rectangle2D.Double( endX , 0.0  , maxX - endX , endY        ) : null; break;
			case TOP          : result =                    ( endY > 0.0  ) ? new Rectangle2D.Double( 0.0  , 0.0  , maxX        , endY        ) : null; break;
			case TOP_RIGHT    : result = ( endX > 0.0  ) && ( endY > 0.0  ) ? new Rectangle2D.Double( 0.0  , 0.0  , endX        , endY        ) : null; break;
			case LEFT         : result = ( endX < maxX )                    ? new Rectangle2D.Double( endX , 0.0  , maxX - endX , maxY        ) : null; break;
			case RIGHT        : result = ( endX > 0.0  )                    ? new Rectangle2D.Double( 0.0  , 0.0  , endX        , maxY        ) : null; break;
			case BOTTOM_LEFT  : result = ( endX < maxX ) && ( endY < maxY ) ? new Rectangle2D.Double( endX , endY , maxX - endX , maxY - endY ) : null; break;
			case BOTTOM       : result =                    ( endY < maxY ) ? new Rectangle2D.Double( 0.0  , endY , maxX        , maxY - endY ) : null; break;
			case BOTTOM_RIGHT : result = ( endX > 0.0  ) && ( endY < maxY ) ? new Rectangle2D.Double( 0.0  , endY , endX        , maxY - endY ) : null; break;

			default :
			case NONE :
				result = null;
		}

		return result;
	}

	public void paint( final ViewModelView view , final Graphics2D g2d )
	{
		if ( isActive() )
		{
			final Rectangle2D rectangle = getResizeRectangle();
			if ( rectangle != null )
			{
				g2d.setColor( Color.BLUE );
				g2d.setStroke( new BasicStroke( 1.0f , BasicStroke.CAP_BUTT , BasicStroke.JOIN_MITER ) );
				g2d.draw( rectangle );
			}
		}
	}
}
