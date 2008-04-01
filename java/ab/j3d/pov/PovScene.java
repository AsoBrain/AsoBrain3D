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
import javax.imageio.stream.MemoryCacheImageInputStream;
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
	 * @param   povFile         File or directory to write POV file to (optional).
	 * @param   width           The width of the rendered image.
	 * @param   height          The height of the rendered image.
	 * @param   progressModel   Progressbar model.
	 * @param   log             Log to write console output to.
	 * @param   background      Wether or not to draw a background.
	 *
	 * @return  Rendered image.
	 *
	 * @throws  IOException if there was a problem reading/writing data.
	 */
	public BufferedImage render( final File povFile , final int width , final int height , final BoundedRangeModel progressModel , final PrintWriter log , final boolean background )
		throws IOException
	{
		BufferedImage result;

		final File actualPovFile = write( povFile );

		final Process process = startPovRay( actualPovFile , width , height , background );
		try
		{
			monitorPovRayProcess( process , height , progressModel , log );

			/*
			 * Pipe data from 'stdout' to 'out'
			 */
			LOG.debug( "reading rendered image data" );
			try
			{
				final InputStream is = process.getInputStream();
				try
				{
					result = ImageIO.read( new MemoryCacheImageInputStream( is ) );
				}
				finally
				{
					is.close();
				}

				LOG.trace( "waiting for POV-Ray process to finish" );
			}
			catch ( IOException e )
			{
				throw new IOException( "POV-Ray command exeution failed" , e );
			}
		}
		finally
		{
			process.destroy();

			if ( actualPovFile != povFile )
			{
				LOG.debug( "deleting temporary POV-Ray file: " + actualPovFile.getPath() );
				actualPovFile.delete();
			}
		}

		return result;
	}

	/**
	 * Start POV-Ray render process.
	 *
	 * @param   povFile     File containing POV-scene.
	 * @param   width       The width of the rendered image.
	 * @param   height      The height of the rendered image.
	 * @param   background  Wether or not to draw a background.
	 *
	 * @return  POV-Ray process.
	 *
	 * @throws  IOException if the POV-Ray executable could not be accessed.
	 * @throws  SecurityException if a security manager prevents file access.
	 */
	public static Process startPovRay( final File povFile , final int width , final int height , final boolean background )
		throws IOException
	{
		/*
		 * Start POV-Ray process.
		 */
		final List<String> command = new ArrayList<String>( 16 );

		command.add( "povray" );                   /* POV-Ray executable */
		command.add( "+I" + povFile.getPath() );   /* Input file ('-' = stdin) */
		command.add( "+O-" );                      /* Output file ('-' = stdout) */
		command.add( "+FN" );                      /* File format: PNG */
		command.add( "+W" + width );               /* Image width */
		command.add( "+H" + height );              /* Image height */
		command.add( "-D" );                       /* Don't show preview */
		command.add( "+Q9" );                      /* Quality (default=9) */
		command.add( "+A" );                       /* Turn on anti-aliasing */
		command.add( "+AM2" );
		command.add( "+R3" );
		command.add( "+GA" );                      /* Turn on all debug, fatal, render, statistic, and warning text to the console */
		command.add( background ? "-UA" : "+UA" ); /* Turn on/off alpha channel output */

		LOG.debug( "rendering (command=" + ArrayTools.toString( command ) + ')' );

		final ProcessBuilder processBuilder = new ProcessBuilder( command );
		final Process result = processBuilder.start();

		/*
		 * Close 'stdin'
		 */
		final OutputStream os = result.getOutputStream();
		try
		{
			os.close();
		}
		catch ( Exception e )
		{
			LOG.warn( "failed to close 'stdin' of render process" , e );
		}


		return result;
	}

	/**
	 * Monitor 'stderr' for progress monitoring and logging from POV-Ray
	 *
	 * @param   povProcess      POV-Ray process to monitor.
	 * @param   height          The height of the rendered image.
	 * @param   progressModel   Progressbar model.
	 * @param   log             Log to write console output to.
	 */
	public static void monitorPovRayProcess( final Process povProcess , final int height , final BoundedRangeModel progressModel , final PrintWriter log )
	{
		if ( ( progressModel != null ) || ( log != null ) )
		{
			if ( progressModel != null )
			{
				progressModel.setMinimum( 0 );
				progressModel.setValue( 0 );
				progressModel.setMaximum( height );
			}

			final Thread stderrMonitor = new Thread( new Runnable()
				{
					public void run()
					{
						final InputStream stderr = povProcess.getErrorStream();
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
					}
				} );
			stderrMonitor.start();
		}
	}

	/**
	 * Saves the current scene as a .pov file.
	 *
	 * @param   file    File or directory to write scene to.
 	 *
	 * @return  File that was written (different from <code>file</code> if
	 *          it was set to <code>null</code> or a directory(.
	 *
	 * @throws  IOException if the file could not be written.
	 */
	public File write( final File file )
		throws IOException
	{
		File result;

		if ( ( file != null ) && !file.isDirectory() )
		{
			result = file;
		}
		else
		{
			try
			{
				result = File.createTempFile( "PovScene-" , ".pov" , ( ( file != null ) && file.isDirectory() ) ? file : null );
				LOG.debug( "created temporary POV-Ray file: " + result.getPath() );
				result.deleteOnExit();
			}
			catch ( IOException e )
			{
				throw new IOException( "failed to create temporary file: " + e.getMessage() , e );
			}
		}

		try
		{
			final FileWriter writer = new FileWriter( result );
			try
			{
				write( writer );
			}
			finally
			{
				writer.close();
			}
		}
		catch ( IOException e )
		{
			if ( file != result )
			{
				result.delete();
				result = null;
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

	protected static void writeFileHeader( final IndentingWriter out )
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

	protected static void writeCameras( final IndentingWriter out , final PovGeometry[] geometry )
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

	protected static void writeLights( final IndentingWriter out , final PovGeometry[] geometry )
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

	protected static void writeGeometry( final IndentingWriter out , final PovGeometry[] geometry )
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
