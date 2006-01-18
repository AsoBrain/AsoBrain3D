/* ====================================================================
 * (C) Copyright Numdata BV 2006-2006
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
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.BoundedRangeModel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

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
	 * The {@link ViewModel} that this action uses.
	 */
	private final ViewModel _model;

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
	 * The {@link ImagePanel} is constructed and added to the view. When the
	 * user clicks on the view, the panel is set invisible and the viewcomponent
	 * is set visible (the original view is visible again).
	 *
	 * @param locale Needed to retrieve the correct resource bundle.
	 * @param model The model that this action uses.
	 * @param view The view this action belongs to.
	 * @param viewContainer Container that holds the view components.
	 * @param constraints Layout constraints for the imagepanel.
	 * @param textureDirectory Directory containing the POV-Ray textures.
	 */
	public ViewModelToPovAction( final Locale locale , final ViewModel model , final ViewModelView view , final JPanel viewContainer , final Object constraints , final String textureDirectory )
	{
		super( ResourceBundleTools.getBundle( ViewModelToPovAction.class , locale ) , "pov" );

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

		_model            = model;
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
	 * rendered with POV-Ray and the resulting image is placed onto the
	 * imagepanel.
	 */
	private void render()
	{
		final Component viewComponent = _view.getComponent();

		int viewWidth  = viewComponent.getWidth();
		int viewHeight = viewComponent.getHeight();

		if ( viewComponent instanceof Container )
		{
			final Insets insets = ( (Container)viewComponent ).getInsets();
			viewWidth  -= insets.left + insets.right;
			viewHeight -= insets.top + insets.bottom;
		}

		/*
		 * Convert the model to a povscene.
		 */
		final AbToPovConverter converter = new AbToPovConverter( _textureDirectory );
		final PovScene scene = converter.convert( _model );

		/*
		 * Manually add the camera, since Camera3D isnt't integrated yet.
		 */
		scene.add( AbToPovConverter.convertCamera3D( _view ) );

		/*
		 * Show progressbar.
		 */
		final JDialog      progress    = WindowTools.createProgressWindow( (Frame)WindowTools.getWindow( viewComponent ) , "Rendering..." , "Rendering..." );
		final Container    contentPane = progress.getContentPane();
		final JProgressBar progressBar = new JProgressBar();

		contentPane.add( progressBar , BorderLayout.SOUTH );
		WindowTools.packAndCenter( progress );

		final BoundedRangeModel progressModel = progressBar.getModel();
		final BufferedImage     image;

		/*
		 * Render the povscene to an image and place the image on the imagepanel.
		 */
		image = scene.render( viewWidth , viewHeight , progressModel );
		WindowTools.close( progress );

		if ( image != null )
		{
			viewComponent.setVisible( false );
			_imagePanel.setImage( image );
			_imagePanel.setVisible( true );
		}
	}
}
