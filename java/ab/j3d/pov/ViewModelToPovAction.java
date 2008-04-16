/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
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
package ab.j3d.pov;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BoundedRangeModel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ab.j3d.Matrix3D;
import ab.j3d.model.Camera3D;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelView;

import com.numdata.oss.ResourceBundleTools;
import com.numdata.oss.ui.BasicAction;
import com.numdata.oss.ui.ImagePanel;
import com.numdata.oss.ui.WindowTools;

/**
 * This action converts the given {@link ViewModel} to a POV-Ray image using
 * the {@link AbToPovConverter} and this image is then painted on an
 * {@link ImagePanel}. This panel is added to the view and set visible when
 * needed.
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public final class ViewModelToPovAction
	extends BasicAction
{
	/**
	 * Resource bundle for this class.
	 */
	private final ResourceBundle _res;

	/**
	 * The {@link ViewModel} that this action uses.
	 */
	private final ViewModel _viewModel;

	/**
	 * The {@link ViewModelView} this action belongs to.
	 */
	private final ViewModelView _view;

	/**
	 * The {@link ImagePanel} used to draw the rendered POV-Ray image.
	 */
	private final ImagePanel _imagePanel;

	/**
	 * Location of the POV-Ray textures.
	 */
	private final String _textureDirectory;

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 3750555513179329632L;

	/**
	 * The {@link ImagePanel} is constructed and added to the view. When the
	 * user clicks on the view, the panel is set invisible and the view component
	 * is set visible (the original view is visible again).
	 *
	 * @param   locale              Needed to retrieve the correct resource bundle.
	 * @param   viewModel           View model that this action uses.
	 * @param   view                View this action belongs to.
	 * @param   viewContainer       Container that holds the view components.
	 * @param   constraints         Layout constraints for the image panel.
	 * @param   textureDirectory    Directory containing the POV-Ray textures.
	 */
	public ViewModelToPovAction( final Locale locale , final ViewModel viewModel , final ViewModelView view , final JPanel viewContainer , final Object constraints , final String textureDirectory )
	{
		super( ResourceBundleTools.getBundle( ViewModelToPovAction.class , locale ) , "pov" );
		_res = ResourceBundleTools.getBundle( ViewModelToPovAction.class , locale );

		final Component viewComponent = view.getComponent();

		final ImagePanel imagePanel = new ImagePanel();
		imagePanel.setVisible( false );
		imagePanel.addMouseListener( new MouseAdapter() {
			public void mousePressed( final MouseEvent e)
			{
				imagePanel.setVisible( false );
				viewComponent.setVisible( true );
			} } );

		viewContainer.add(  imagePanel , constraints );

		_viewModel        = viewModel;
		_view             = view;
		_imagePanel       = imagePanel;
		_textureDirectory = textureDirectory;
	}

	/**
	 * Create a thread for rendering the image.
	 */
	public void run()
	{
		final Thread thread = new Thread( new Runnable() {
			public void run()
			{
				render();
			} } );

		thread.start();
	}

	/**
	 * The {@link ViewModel} is converted to a {@link PovScene}, the scene is
	 * rendered with POV-Ray, and the resulting image is placed onto the
	 * image panel.
	 */
	private void render()
	{
		final ResourceBundle res  = _res;

		/*
		 * Get view properties.
		 */
		final ViewModelView view          = _view;
		final Component     viewComponent = view.getComponent();
		final Window        viewWindow    = WindowTools.getWindow( viewComponent );

		int viewWidth  = viewComponent.getWidth();
		int viewHeight = viewComponent.getHeight();

		if ( viewComponent instanceof Container )
		{
			final Insets insets = ( (Container)viewComponent ).getInsets();
			viewWidth  -= insets.left + insets.right;
			viewHeight -= insets.top + insets.bottom;
		}

		/*
		 * Show progress bar.
		 */
		final JDialog           progress        = WindowTools.createProgressWindow( viewWindow , res.getString( "progressTitle" ) , res.getString( "progressMessage" ) );
		final Container         progressContent = progress.getContentPane();
		final JProgressBar      progressBar     = new JProgressBar();
		final BoundedRangeModel progressModel   = progressBar.getModel();
		progressContent.add( progressBar , BorderLayout.SOUTH );

		WindowTools.packAndCenter( progress );

		/*
		 * Perform conversion
		 */
		final StringWriter logBuffer = new StringWriter();
		final PrintWriter  logWriter = new PrintWriter( logBuffer );

		BufferedImage image = null;
		try
		{
			/*
			 * Convert view properties to camera properties.
			 */
			final Camera3D camera          = view.getCamera();
			final Matrix3D viewTransform   = view.getViewTransform();
			final Matrix3D cameraTransform = viewTransform.inverse();
			final double   aspectRatio     = (double)viewWidth / (double)viewHeight;

			/*
			 * Convert scene to POV-Ray.
			 */
			final AbToPovConverter converter = new AbToPovConverter( _textureDirectory );
			final PovScene scene = converter.convert( _viewModel.getScene() );
			scene.add( AbToPovConverter.convertCamera3D( cameraTransform , camera , aspectRatio ) );

			/*
			 * Render the povscene to an image and place the image on the image panel.
			 */
			try
			{
				image = scene.render( null , viewWidth , viewHeight , progressModel , logWriter , false );
			}
			catch ( IOException e )
			{
				System.err.println( e );
			}
		}
		finally
		{
			WindowTools.close( progress );
		}

		/*
		 * Process conversion result.
		 */
		if ( image != null )
		{
			viewComponent.setVisible( false );

			final ImagePanel imagePanel = _imagePanel;
			imagePanel.setImage( image );
			imagePanel.setVisible( true );
		}
		else
		{
			final String logText = logBuffer.toString();
			int pos = logText.length();
			for ( int i = 0 ; ( pos > 0 ) && ( i < 25 ) ; i++ )
				pos = logText.lastIndexOf( (int)'\n' , pos - 1 );

			WindowTools.showErrorDialog( viewWindow , res.getString( "errorTitle" ) , MessageFormat.format( res.getString( "errorMessage" ) , ( ( pos < 0 ) ? logText : logText.substring( pos ) ) ) );
		}
	}
}
