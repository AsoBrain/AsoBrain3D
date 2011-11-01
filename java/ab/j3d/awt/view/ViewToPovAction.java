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
package ab.j3d.awt.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.awt.*;
import ab.j3d.model.*;
import ab.j3d.pov.*;
import ab.j3d.view.*;

/**
 * This action converts the given {@link View3D} to a POV-Ray image using
 * the {@link AbToPovConverter} and paints this image on top of the view when
 * needed.
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public class ViewToPovAction
	extends AbstractAction
{
	/**
	 * Resource bundle for this class.
	 */
	private final ResourceBundle _bundle;

	/**
	 * The {@link View3D} this action belongs to.
	 */
	private final View3D _view;

	/**
	 * The {@link ImagePanel} used to draw the rendered POV-Ray image.
	 */
	private final ImagePanel _imagePanel;

	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 3750555513179329632L;

	/**
	 * The {@link ImagePanel} is constructed and added to the view. When the
	 * user clicks on the view, the panel is set invisible and the view component
	 * is set visible (the original view is visible again).
	 *
	 * @param   locale              Needed to retrieve the correct resource bundle.
	 * @param   view                View this action belongs to.
	 * @param   viewContainer       Container that holds the view components.
	 * @param   constraints         Layout constraints for the image panel.
	 */
	public ViewToPovAction( final Locale locale, final View3D view, final JPanel viewContainer, final Object constraints )
	{
		final ResourceBundle bundle = ResourceBundle.getBundle( "LocalStrings", locale );
		_bundle = bundle;

		final String name = "pov";
		putValue( ACTION_COMMAND_KEY, name );
		putValue( NAME, bundle.getString( name ) );
		putValue( SHORT_DESCRIPTION, bundle.getString( name + "Tip" ) );

		final KeyStroke keyStroke = KeyStroke.getKeyStroke( bundle.getString( name + "Mnemonic" ) );
		if ( keyStroke != null )
		{
			putValue( MNEMONIC_KEY, Integer.valueOf( keyStroke.getKeyCode() ) );
		}

		final URL iconUrl = ViewToPovAction.class.getResource( "/ab3d/povray-16x16.png" );
		if ( iconUrl != null )
		{
			putValue( SMALL_ICON, new ImageIcon( iconUrl ) );
		}

		final Component viewComponent = view.getComponent();

		final ImagePanel imagePanel = new ImagePanel();
		imagePanel.setVisible( false );
		imagePanel.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mousePressed( final MouseEvent e)
			{
				imagePanel.setVisible( false );
				viewComponent.setVisible( true );
			} } );

		viewContainer.add(  imagePanel, constraints );

		_view = view;
		_imagePanel = imagePanel;
	}

	/**
	 * Create a thread for rendering the image.
	 */
	public void actionPerformed( final ActionEvent e )
	{
		final Thread thread = new Thread( new Runnable() {
			public void run()
			{
				render();
			} } );

		thread.start();
	}

	/**
	 * The {@link RenderEngine} is converted to a {@link PovScene}, the scene is
	 * rendered with POV-Ray, and the resulting image is placed onto the
	 * image panel.
	 */
	private void render()
	{
		final ResourceBundle res  = _bundle;

		/*
		 * Get view properties.
		 */
		final View3D view = _view;
		final Component viewComponent = view.getComponent();
		if ( viewComponent == null )
		{
			throw new IllegalStateException( "No view component!" );
		}

		Window viewWindow = null;
		for ( Component c = viewComponent; c != null; c = c.getParent() )
		{
			if ( c instanceof Window )
			{
				viewWindow = (Window)c;
				break;
			}
		}

		if ( viewWindow == null )
		{
			throw new IllegalStateException( "View component is not on any window!" );
		}

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
		final String progressTitle = res.getString( "progressTitle" );
		final JProgressBar progressBar = new JProgressBar();
		final JPanel progressContent = new JPanel( new BorderLayout() );
		progressContent.setBorder( BorderFactory.createEmptyBorder( 16, 16, 16, 16 ) );
		progressContent.add( new JLabel( res.getString( "progressMessage" ), JLabel.CENTER ), BorderLayout.CENTER );
		progressContent.add( progressBar, BorderLayout.SOUTH );

		final JDialog progressDialog = new JDialog( viewWindow, progressTitle );
		progressDialog.setContentPane( progressContent );
		progressDialog.setResizable( false );
		progressDialog.pack();

		final Toolkit toolkit = progressDialog.getToolkit();
		final GraphicsConfiguration graphicsConfiguration = progressDialog.getGraphicsConfiguration();
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final Insets screenInsets = toolkit.getScreenInsets( graphicsConfiguration );
		final int windowWidth  = Math.min( screenBounds.width, progressDialog.getWidth()  );
		final int windowHeight = Math.min( screenBounds.height, progressDialog.getHeight() );
		progressDialog.setBounds( screenBounds.x + ( screenBounds.width + screenInsets.left + screenInsets.right - windowWidth ) / 2, screenBounds.y + ( screenBounds.height + screenInsets.top + screenInsets.bottom - windowHeight ) / 2, windowWidth, windowHeight );

		progressDialog.setVisible( true );

		if ( SwingUtilities.isEventDispatchThread() )
		{
			final RepaintManager repaintManager = RepaintManager.currentManager( progressDialog );
			repaintManager.addDirtyRegion( progressDialog, 0, 0, progressDialog.getWidth(), progressDialog.getHeight() );
			repaintManager.paintDirtyRegions();
		}

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
			final Scene    scene       = view.getScene();
			final double   aspectRatio = (double)viewWidth / (double)viewHeight;

			/*
			 * Convert scene to POV-Ray.
			 */
			final AbToPovConverter converter = new AbToPovConverter();
			final PovScene povScene = converter.convert( scene );
			povScene.add( new PovCamera( view.getLabel(), view.getView2Scene(), Math.toDegrees( view.getFieldOfView() ), aspectRatio ) );

			/*
			 * Render the povscene to an image and place the image on the image panel.
			 */
			try
			{
				image = PovRenderer.render( povScene, null, viewWidth, viewHeight, progressBar.getModel(), logWriter, false );
			}
			catch ( IOException e )
			{
				System.err.println( e );
			}
		}
		finally
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					try
					{
						progressDialog.dispose();
					}
					catch ( Throwable t ) { /* ignore */ }

					try
					{
						progressDialog.setVisible( false );
					}
					catch ( Throwable t ) { /* ignore */ }
				}
			} );
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
			{
				pos = logText.lastIndexOf( (int) '\n', pos - 1 );
			}

			final String message = MessageFormat.format( res.getString( "errorMessage" ), ( ( pos < 0 ) ? logText : logText.substring( pos ) ) );
			final String title = res.getString( "errorTitle" );
			JOptionPane.showMessageDialog( viewWindow, message, title, JOptionPane.ERROR_MESSAGE );
		}
	}

	/**
	 * Panel displaying the rendered image.
	 */
	private static class ImagePanel
		extends JPanel
	{
		/**
		 * Image displayed by this panel.
		 */
		private BufferedImage _image;

		/**
		 * Construct border-less image panel without an image.
		 */
		ImagePanel()
		{
			_image = null;
		}

		/**
		 * Set image to display.
		 *
		 * @param   image   Image to display (<code>null</code> = none).
		 */
		public void setImage( final BufferedImage image )
		{
			_image = image;
			invalidate();
		}

		@Override
		public Dimension getPreferredSize()
		{
			final Dimension result;

			if ( !isPreferredSizeSet() && ( _image != null ) )
			{
				final Insets i = getInsets();
				result = new Dimension( _image.getWidth( this ) + i.left + i.right , _image.getHeight( this ) + i.top  + i.bottom );
			}
			else
			{
				result = super.getPreferredSize();
			}

			return result;
		}

		@Override
		protected void paintComponent( final Graphics g )
		{
			super.paintComponent( g );

			if ( _image != null )
			{
				final Insets i = getInsets();
				g.drawImage( _image , i.left , i.top , getWidth()  - i.left - i.right , getHeight() - i.top  - i.bottom , this );
			}
		}
	}
}
