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

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BoundedRangeModel;

import com.numdata.oss.ArrayTools;
import com.numdata.oss.io.IndentingWriter;
import com.numdata.oss.log.ClassLogger;

/**
 * World/Scene contains all geometry and predefined textures.
 *
 * @FIXME Improve geometry declaration (could be done in one class)
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
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
	private List _geometry = new ArrayList();

	/**
	 * All globally defined textures in the scene.
	 * These textures will be declared on top of the pov source
	 * like : #declare TEX_name = texture { definition }
	 */
	private Map textures = new HashMap();

	/**
	 * Declared or predefined shapes in the scene.
	 */
	private Map declaredShapes = new HashMap();

	public float gamma = 1.8f;

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
	 * Adds a new declared texture to the scene.
	 *
	 * @param   code	the reference name of the texture.
	 * @param   texture	the texture to add.
	 */
	public void addTexture( final String code , final PovTexture texture )
	{
		textures.put( code , texture );
	}

	/**
	 * Adds geometry to the declared list, these
	 * geometry objects are declared and can be referenced
	 * to use them more than once.
	 *
	 * @param   geom	the geometry to declare.
	 *
	 * @return  the name of the declared geometry.
	 */
	public String declare( final PovGeometry geom )
	{
		final String name = PovDeclared.getDeclaredName( geom.name );
		declaredShapes.put( name , geom );
		return name;
	}

	/**
	 * Gets a declared texture from the scene.
	 *
	 * @param   code	the reference name of the texture.
	 *
	 * @return  the texture with the specified reference name.
	 */
	public PovTexture getTexture( final String code )
	{
		return (PovTexture)textures.get( code );
	}

	/**
	 * Removes an object from the scene.
	 *
	 * @param   object	the object to remove.
	 */
	public void remove( final PovGeometry object )
	{
		_geometry.remove( object );
	}

	/**
	 * Renders the specified input file and returns the rendered output file.
	 *
	 * @param width         The width of the rendered image.
	 * @param height        The height of the rendered image.
	 * @param progressModel Progressbar model.
	 * @return              Rendered image;
	 *                      <code>null</code> if the pov scene could not be rendered.
	 */
	public BufferedImage render( final int width , final int height , final BoundedRangeModel progressModel )
	{
		if ( progressModel != null )
		{
			progressModel.setMinimum( 0 );
			progressModel.setMaximum( height );
			progressModel.setValue( 0 );
		}

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
				"povray" ,
				"+I" + tempFile.getPath() , // input file ('-' = stdin)
				"+O-" ,                     // output file ('-' = stdout)
				"+FN" ,                     // file format: PNG
				"+W" + width ,              // image width
				"+H" + height ,             // image height
				"-D" ,                      // don't show preview
				"+A"  ,                     // turn on anti-aliasing
				"+GA" ,
				"+L/numdata/bin/pov/povray-3.6/share/povray-3.6/include"
			};

			final Runtime runtime = Runtime.getRuntime();

			try
			{
				LOG.debug( "rendering (command=" + ArrayTools.toString( command ) + ')' );
				final Process process = runtime.exec( command , null , null );

				/* close stdin */
				final OutputStream os = process.getOutputStream();
				try
				{
					os.close();
				}
				catch ( IOException e )
				{
				}

				/* start progress monitor of POV-Ray console */
				if ( progressModel != null )
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
									if  ( line.indexOf(  " Rendering line " ) != -1 )
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
					process.destroy();
				}

				LOG.trace( "waiting for POV-Ray process to finish" );
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
//			e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Saves the current scene as a .pov file.
	 *
	 * @param   outstream   Stream to save to.
	 */
	public void write( final OutputStream outstream )
		throws IOException
	{
		final OutputStreamWriter osw = new OutputStreamWriter( outstream );
		write( osw );
		osw.flush();
	}

	/**
	 * Saves the current scene as a .pov file.
	 *
	 * @param   out     Destination writer.
	 */
	public void write( final Writer out )
		throws IOException
	{
		final IndentingWriter iw = ( out instanceof IndentingWriter ) ? (IndentingWriter)out : new IndentingWriter( out );

		/*
		 * Make sure, all geometry is sorted alfabetically.
		 */
		final PovGeometry[] geometry = (PovGeometry[])_geometry.toArray( new PovGeometry[ _geometry.size() ] );
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
		out.writeln( "assumed_gamma " + gamma );
		out.indentOut();
		out.writeln( "}" );
		out.newLine();

		// infinite floor
		out.writeln( "plane" );
		out.writeln( "{" );
		out.indentIn();
		out.writeln( "z , 0.0" );
		out.writeln( "texture");
		out.writeln( "{" );
		out.indentIn();
		out.writeln( "pigment { image_map { jpeg \"SODA_BaseComponents/images/textures/PF_R5474\" } }" );
		out.writeln( "scale < 1000 , 1000 , 1000 >" );
		out.writeln( "finish" );
		out.writeln( "{" );
		out.indentIn();
		out.writeln( "ambient 0.2" );
		out.writeln( "diffuse 0.6" );
		out.indentOut();
		out.writeln( "}" );
		out.indentOut();
		out.writeln( "}" );
		out.indentOut();
		out.writeln( "}" );
		out.newLine();
	}

	protected void writeCameras( final IndentingWriter out , final PovGeometry[] geometry )
		throws IOException
	{
		for ( int i = 0 ; i < geometry.length ; i++ )
		{
			final PovGeometry geom = (PovGeometry)geometry[i];
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
		for ( int i = 0 ; i < geometry.length ; i++ )
		{
			final PovGeometry geom = (PovGeometry)geometry[i];
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
	 * @param   out	the stream to write to.
	 */
	protected void writeTextureDefs( final IndentingWriter out )
		throws IOException
	{
		int i = 0;

		final Object[] textureKeys = new Object[ textures.size() ];
		for ( Iterator iterator = textures.keySet().iterator() ; iterator.hasNext() ; )
		{
			textureKeys[ i++ ] = iterator.next();
		}
		Arrays.sort( textureKeys );

		out.writeln( "/*" );
		out.writeln( " * Texture definitions" );
		out.writeln( " */" );

		for ( i = 0 ; i < textureKeys.length ; i++ )
		{
			final Object     key     = textureKeys[ i ];
			final PovTexture texture = (PovTexture)textures.get( key );

			texture.declare( out );
			out.newLine();
		}
	}

	/**
	 * Writes all declared shapes to the output stream.
	 *
	 * @param   out	the stream to write to.
	 */
	protected void writeDeclaredShapes( final IndentingWriter out )
		throws IOException
	{
		int i = 0;

		final String[] shapeKeys = new String[ declaredShapes.size() ];
		for ( final Iterator iterator = declaredShapes.keySet().iterator() ; iterator.hasNext() ; )
			shapeKeys[ i++ ] = (String)iterator.next();
		Arrays.sort( shapeKeys );

		out.writeln( "/*" );
		out.writeln( " * Declared geometry" );
		out.writeln( " */" );

		for ( i = 0 ; i  < shapeKeys.length ; i++ )
		{
			final String      name = shapeKeys[ i ];
			final PovGeometry geom = (PovGeometry)declaredShapes.get( name );

			out.writeln( "#declare " + name + " =" );
			out.indentIn();
			geom.write( out );
			out.indentOut();
		}

		out.newLine();
	}

	protected void writeGeometry( final IndentingWriter out , final PovGeometry[] geometry )
		throws IOException
	{
		out.writeln( "/*" );
		out.writeln( " * Geometry" );
		out.writeln( " */" );
		//System.out.print( "Writing geometry : " );
		for ( int i = 0 ; i < geometry.length ; i++ )
		{
			final PovGeometry geom = (PovGeometry)geometry[i];
			if ( !( geom instanceof PovLight ) && !( geom instanceof PovCamera ) )
			{
				geom.write( out );
				out.write( "\n" );
			}
		}
		//System.out.println( "" );
	}
}
