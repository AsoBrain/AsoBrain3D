/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.control.controltest;

import ab.j3d.control.controltest.model.Model;
import ab.j3d.control.controltest.model.Wall;
import ab.j3d.control.controltest.model.Floor;
import ab.j3d.control.controltest.model.TetraHedron;
import ab.j3d.control.Control;

/**
 * The ControlTest test application demonstrates what is possible with the
 * {@link Control} system. The test application is structured using MVC; model,
 * view controller.<p>
 * There is a {@link Model} that holds a number of objects. A {@link Floor},
 * some {@link Wall}s, some pyramids ({@link TetraHedron}s). The objects all
 * have a number of properties that can be changed, such as the location,
 * rotation and size.<p>
 * There is a 3d representation of this model ({@link Model3D}), with a number of
 * views on it ({@link View3D}). This can count as the view side.<p>
 * And lastly, there is the controller side, which are the {@link Control}s. At
 * this moment there is anly a control that manages selection of objects
 * ({@link SelectionControl}), but this can be extended in the future, to
 * showcase new features possible with the Control system.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class ControlTest
{
	/**
	 * The Model for this application.
	 */
	private Model _model;

	/**
	 * Construct new ControlTest.
	 */
	public ControlTest()
	{
		_model = new Model();
		final Model model = _model;

		model.addSceneElement( new Floor( 200.0 , 200.0 ) );

		model.addSceneElement( new Wall( 20.0 , 120.0 ,  3.0 ) );
		model.addSceneElement( new Wall(  0.0 ,  20.0 , 90.0 , 15.0 , 80.0 , 5.0 ) );
		model.addSceneElement( new Wall( 20.0 , -50.0 ,  0.0 , 17.0 , 60.0 , 4.0 ) );

		model.addSceneElement( new TetraHedron(  20.0,  20.0, 0.0, 30.0 ) );
		model.addSceneElement( new TetraHedron( -40.0, -40.0, 0.0, 15.0 ) );
		model.addSceneElement( new TetraHedron(  40.0, -20.0, 0.0, 20.0 ) );
		model.addSceneElement( new TetraHedron( -20.0,  40.0, 0.0, 20.0 ) );

		new GUI( this );
	}

	/**
	 * Returns the model for this application.
	 * @return the model for this application.
	 */
	protected Model getModel()
	{
		return _model;
	}

	public static void main( final String[] args )
	{
		new ControlTest();
	}
}
