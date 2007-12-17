/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2006
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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.BoundedRangeModel;

import com.numdata.oss.ArrayTools;
import com.numdata.oss.io.IndentingWriter;
import com.numdata.oss.log.ClassLogger;

/**
 * World/Scene contains all geometry and predefined textures.
 *
 * @FIXME   Improve geometry declaration (could be done in one class)
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public class PovScene
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( PovScene.class );

	/**
	 * All main branches of geometry in the scene.
	 */
	private List<PovGeometry> _geometry = new ArrayList<PovGeometry>();

	/**
	 * All globally defined textures in the scene.
	 * These textures will be declared on top of the pov source
	 * like : #declare TEX_name = texture { definition }
	 */
	private Map<String,PovTexture> _textures = new HashMap<String,PovTexture>();

	/**
	 * Declared or predefined shapes in the scene.
	 */
	private Map<String,PovGeometry> _declaredShapes = new HashMap<String,PovGeometry>();

	/**
	 * Ambient light in the scene.
	 */
	private PovVector _ambientLight = new PovVector( Color.BLACK );

	/**
	 * Assumed gamma level.
	 */
	private double _assumedGamma = 2.2;

	/**
	 * Get indenting writer from the specified writer. If the specified writer
	 * is already an {@link IndentingWriter}, it will be returned as-is.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @return  Indenting writer (may be the same as <code>out</code>).
	 *
	 * @throws  NullPointerException if <code>out</code> is <code>null</code>.
	 */
	static IndentingWriter getIndentingWriter( final Writer out )
	{
		if ( out == null )
			throw new NullPointerException( "out" );

		return ( out instanceof IndentingWriter ) ? (IndentingWriter)out : new IndentingWriter( out , "\t" );
	}

	/**
	 * Adds new geometry to the scene.
	 *
	 * @param   object the geometry to add.
	 */
	public void add( final PovGeometry object )
	{
		_geometry.add( object );
	}

	/**
	 * Removes an object from the scene.
	 *
	 * @param   object  Object to remove.
	 */
	public void remove( final PovGeometry object )
	{
		_geometry.remove( object );
	}

	/**
	 * Adds geometry to the declared list, these
	 * geometry objects are declared and can be referenced
	 * to use them more than once.
	 *
	 * @param   geom    Geometry to declare.
	 *
	 * @return  the name of the declared geometry.
	 */
	public String declare( final PovGeometry geom )
	{
		final String name = PovDeclared.getDeclaredName( geom.getName() );
		_declaredShapes.put( name , geom );
		return name;
	}

	/**
	 * Adds a new declared texture to the scene.
	 *
	 * @param   code        Reference name of the texture.
	 * @param   texture     Texture to add.
	 */
	public void addTexture( final String code , final PovTexture texture )
	{
		_textures.put( code , texture );
	}

	/**
	 * Gets a declared texture from the scene.
	 *
	 * @param   code    Reference name of the texture.
	 *
	 * @return  the texture with the specified reference name.
	 */
	public PovTexture getTexture( final String code )
	{
		return _textures.get( code );
	}

	/**
	 * Returns the scene's ambient light intensity.
	 *
	 * @return  Ambient light.
	 */
	public PovVector getAmbientLight()
	{
		return _ambientLight;
	}

	/**
	 * Sets the scene's ambient light intensity.
	 *
	 * @param   ambientLight    Ambient light to be set.
	 */
	public void setAmbientLight( final PovVector ambientLight )
	{
		_ambientLight = ambientLight;
	}

	/**
	 * Get assumed gamma level.
	 *
	 * @return  Assumed gamma level.
	 */
	public final double getAssumedGamma()
	{
		return _assumedGamma;
	}

	/**
	 * Get assumed gamma level.
	 *
	 * @param   assumedGamma   Assumed gamma level.
	 */
	public final void setAssumedGamma( final double assumedGamma )
	{
		_assumedGamma = assumedGamma;
	}

	/**
	 * Renders the scene to an image with the specified size and returns the
	 * resulting image.
	 *
	 * @param   width           The width of the rendered image.
	 * @param   height          The height of the rendered image.
	 * @param   progressModel   Progressbar model.
	 * @param   log             Log to write console output to.
	 * @param   background      Wether or not to draw a background.
	 *
	 * @return  Rendered image;
	 *          <code>null</code> if the pov scene could not be rendered.
	 */
	public BufferedImage render( final int width , final int height , final BoundedRangeModel progressModel , final PrintWriter log , final boolean background )
	{
		if ( progressModel != null )
		{
			progressModel.setMinimum( 0 );
			progressModel.setMaximum( height );
			progressModel.setValue( 0 );
		}

		write( new File( "x.pov" ) ); // write POV file for debugging purposes.

		File tempFile = null;
		try
		{
			tempFile = File.createTempFile( "PovScene-", ".pov" , null );
			LOG.debug( "created temporary POV-Ray file: " + tempFile.getPath() );
			tempFile.deleteOnExit();
		}
		catch ( IOException e )
		{
			LOG.error( "failed to create temporary file" , e );
		}

		BufferedImage result = null;

		if ( tempFile != null && write( tempFile ) )
		{
			final String[] command =
			{
				"povray"
				, "+I" + tempFile.getPath() /* Input file ('-' = stdin) */
				, "+O-"                     /* Output file ('-' = stdout) */
				, "+FN"                     /* File format: PNG */
				, "+W" + width              /* Image width */
				, "+H" + height             /* Image height */
				, "-D"                      /* Don't show preview */
				, "+A"                      /* Turn on anti-aliasing */
				, "+GA"                     /* Turn on all debug, fatal, render, statistic, and warning text to the console */
				, (background)?"+UA":""
			};

			final Runtime runtime = Runtime.getRuntime();

			try
			{
				LOG.debug( "rendering (command=" + ArrayTools.toString( command ) + ')' );
				final Process process = runtime.exec( command , null , null );

				try
				{
					/* close stdin */
					final OutputStream os = process.getOutputStream();
					try
					{
						os.close();
					}
					catch ( IOException e )
					{
						e.printStackTrace();
					}

					/* start progress monitor of POV-Ray console */
					if ( ( progressModel != null ) || ( log != null ) )
					{
						final Thread stderrMonitor = new Thread( new Runnable() {
							public void run()
							{
								final InputStream stderr = process.getErrorStream();
								try
								{
									final BufferedReader errorStream = new BufferedReader( new InputStreamReader( stderr ) );
									String line;
									while ( ( line = errorStream.readLine() ) != null )
									{
										if ( log != null )
										{
											log.println( line );
											log.flush();
										}

										if ( ( progressModel != null ) && ( line.contains( " Rendering line " ) ) )
										{
											String temp = line.substring( line.indexOf( " Rendering line " ) + 16 );
											final int end = temp.indexOf( (int)' ' );
											temp = temp.substring( 0 , end );

											try
											{
												final int value = Integer.parseInt( temp );
												progressModel.setValue( value );
											}
											catch( Exception e )
											{
												/* ignore */
											}

										}
									}
								}
								catch ( IOException e )
								{
									/* ignore */
								}
								finally
								{
									try
									{
										stderr.close();
									}
									catch ( IOException e )
									{
										/* ignore */
									}
								}
							} } );
						stderrMonitor.start();
					}

					/* read image from stdout */
					LOG.debug( "reading rendered image data" );
					final InputStream is = process.getInputStream();
					try
					{
						result = ImageIO.read( is );
					}
					catch ( IOException e )
					{
						LOG.error( "failed to render image" , e );
					}
					finally
					{
						is.close();
					}

					LOG.trace( "waiting for POV-Ray process to finish" );
				}
				finally
				{
					process.destroy();
				}
			}
			catch ( IOException e )
			{
				LOG.error( "POV-Ray command exeution failed" , e );
			}
		}

		if ( tempFile != null )
		{
			LOG.debug( "deleting temporary POV-Ray file: " + tempFile.getPath() );

			try
			{
				tempFile.delete();
			}
			catch ( Exception e )
			{
				LOG.debug( "failed to delete temporary POV-Ray file: " + tempFile.getPath() , e );
			}
		}

		return result;
	}

	/**
	 * Saves the current scene as a .pov file.
	 *
	 * @param   file    File to write.
	 *
	 * @return  True if the file was succesfully written.
	 */
	public boolean write( final File file )
	{
		boolean result = false;

		if ( file != null )
		{
			try
			{
				final FileWriter writer = new FileWriter( file );
				try
				{
					write( writer );
					result = true;
				}
				finally
				{
					writer.close();
				}
			}
			catch (IOException e)
			{
//				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Saves the current scene as a .pov file.
	 *
	 * @param   out     Stream to save to.
	 *
	 * @throws  IOException when writing failed.
	 */
	public void write( final OutputStream out )
		throws IOException
	{
		final OutputStreamWriter streamWriter = new OutputStreamWriter( out );
		write( streamWriter );
		streamWriter.flush();
	}

	/**
	 * Saves the current scene as a .pov file.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	public void write( final Writer out )
		throws IOException
	{
		final IndentingWriter iw = getIndentingWriter( out );

		/*
		 * Make sure, all geometry is sorted alfabetically.
		 */
		final PovGeometry[] geometry = _geometry.toArray( new PovGeometry[ _geometry.size() ] );
		Arrays.sort( geometry );

		//System.out.println( "WRITING POV FILE" );

		writeFileHeader( iw );
		writeGlobalSettings( iw );
		writeCameras( iw, geometry );
		writeLights( iw, geometry );
		writeTextureDefs( iw );
		writeDeclaredShapes( iw );
		writeGeometry( iw, geometry );
	}

	protected void writeFileHeader( final IndentingWriter out )
		throws IOException
	{
		//out.writeln( "#version unofficial MegaPov 0.7;" );
		out.writeln( "#include \"colors.inc\"" );
		//out.writeln( "#include \"textures.inc\"" );
		//out.writeln( "#include \"glass.inc\"" );
		//out.writeln( "#include \"metals.inc\"" );
		out.newLine();
	}

	protected void writeGlobalSettings( final IndentingWriter out )
		throws IOException
	{
		out.writeln( "global_settings" );
		out.writeln( "{" );
		out.indentIn();
		out.write( "ambient_light rgb " );
		_ambientLight.write( out );
		out.newLine();
		out.write( "assumed_gamma " );
		out.write( PovObject.format( getAssumedGamma() ) );
		out.newLine();
		out.indentOut();
		out.writeln( "}" );

		out.newLine();
	}

	protected void writeCameras( final IndentingWriter out , final PovGeometry[] geometry )
		throws IOException
	{
		for ( final PovGeometry geom : geometry )
		{
			if ( geom instanceof PovCamera )
			{
				geom.write( out );
				out.write( "\n" );
			}
		}
	}

	protected void writeLights( final IndentingWriter out , final PovGeometry[] geometry )
		throws IOException
	{
		for ( final PovGeometry geom : geometry )
		{
			if ( geom instanceof PovLight )
			{
				geom.write( out );
				out.write( "\n" );
			}
		}
	}

	/**
	 * Writes all declared textures to the output stream.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	protected void writeTextureDefs( final IndentingWriter out )
		throws IOException
	{
		final Map<String,PovTexture> textures = _textures;

		if ( !textures.isEmpty() )
		{
			final Set<String> textureKeySet = textures.keySet();
			final Object[]    textureKeys   = textureKeySet.toArray();

			Arrays.sort( textureKeys );

			out.writeln( "/*" );
			out.writeln( " * Texture definitions" );
			out.writeln( " */" );

			for ( final Object key : textureKeys )
			{
				final PovTexture texture = textures.get( key );

				texture.declare( out );
				out.newLine();
			}
		}
	}

	/**
	 * Writes all declared shapes to the output stream.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	protected void writeDeclaredShapes( final IndentingWriter out )
		throws IOException
	{
		final Map<String,PovGeometry> declaredShapes = _declaredShapes;

		if ( !declaredShapes.isEmpty() )
		{
			final Set<String> declaredKeySet = declaredShapes.keySet();
			final String[]    declaredKeys   = declaredKeySet.toArray( new String[ declaredKeySet.size() ] );
			Arrays.sort( declaredKeys );

			out.writeln( "/*" );
			out.writeln( " * Declared geometry" );
			out.writeln( " */" );

			for ( final String name : declaredKeys )
			{
				final PovGeometry geom = declaredShapes.get( name );

				out.write( "#declare " );
				out.write( name );
				out.write( " =" );
				out.newLine();
				out.indentIn();

				geom.write( out );
				out.indentOut();
			}

			out.newLine();
		}
	}

	protected void writeGeometry( final IndentingWriter out , final PovGeometry[] geometry )
		throws IOException
	{
		if ( ( geometry != null ) && ( geometry.length > 0 ) )
		{
			out.writeln( "/*" );
			out.writeln( " * Geometry" );
			out.writeln( " */" );

			//System.out.print( "Writing geometry : " );
			for ( final PovGeometry geom : geometry )
			{
				if ( !( geom instanceof PovLight )
				     && !( geom instanceof PovCamera ) )
				{
					geom.write( out );
					out.newLine();
				}
			}
			//System.out.println( "" );
		}
	}
}
