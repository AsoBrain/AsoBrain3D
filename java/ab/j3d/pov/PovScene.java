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
package ab.j3d.pov;

import java.io.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * World/Scene contains all geometry and predefined textures.
 *
 * FIXME   Improve geometry declaration (could be done in one class)
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public class PovScene
{
	/**
	 * All main branches of geometry in the scene.
	 */
	private final List<PovGeometry> _geometry = new ArrayList<PovGeometry>();

	/**
	 * All globally defined textures in the scene.
	 * These textures will be declared on top of the pov source
	 * like : #declare TEX_name = texture { definition }
	 */
	private final Map<String,PovTexture> _textures = new LinkedHashMap<String,PovTexture>();

	/**
	 * Declared or predefined shapes in the scene.
	 */
	private final Map<String,PovGeometry> _declaredShapes = new HashMap<String,PovGeometry>();

	/**
	 * Background color of the scene.
	 */
	private PovVector _background = new PovVector( 0.0f, 0.0f, 0.0f );

	/**
	 * Ambient light in the scene.
	 */
	private PovVector _ambientLight = new PovVector( 0.0f, 0.0f, 0.0f );

	/**
	 * Assumed gamma level.
	 */
	private double _assumedGamma = 2.2;

	/**
	 * Indicates whether radiosity is enabled.
	 */
	private boolean _radiosity = false;

	/**
	 * Changes the intensity of radiosity effects. A value of <code>0.0</code>
	 * would be the same as without radiosity. The default value,
	 * <code>1.0</code> should work correctly in most cases. If effects are too
	 * strong you can reduce this. Larger values lead to quite strange results
	 * in most cases.
	 */
	private double _radiosityBrightness = 1.0;

	/**
	 * The integer number of rays that are sent out whenever a new radiosity
	 * value has to be calculated is given by count. A value of 35 is the
	 * default, the maximum is 1600. When this value is too low, the light level
	 * will tend to look a little bit blotchy, as if the surfaces you are
	 * looking at were slightly warped. If this is not important to your scene
	 * (as in the case that you have a bump map or if you have a strong texture)
	 * then by all means use a lower number.
	 */
	private int _radiosityCount = 35;

	/**
	 * This value mainly affects the structures of the shadows. Values larger
	 * than the default of <code>1.8</code> do not have much effects, they make
	 * the shadows even flatter. Extremely low values can lead to very good
	 * results, but the rendering time can become very long.
	 */
	private double _radiosityErrorBound = 1.8;

	/**
	 * Fraction of the image width which sets the minimum radius of reuse for
	 * each sample point (actually, it is the fraction of the distance from the
	 * eye but the two are roughly equal). The theory is that you do not want to
	 * calculate values for every pixel into every crevice everywhere in the
	 * scene, it will take too long. If this value is too low, (which it should
	 * be in theory) rendering gets slow, and inside corners can get a little
	 * grainy. If it is set too high, you do not get the natural darkening of
	 * illumination near inside edges, since it reuses.
	 * The default value is <code>0.015</code>.
	 */
	private double _radiosityMinimumReuse = 0.015;

	/**
	 * Get indenting writer from the specified writer. If the specified writer
	 * is already an {@link PovWriter}, it will be returned as-is.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @return  Indenting writer (may be the same as <code>out</code>).
	 *
	 * @throws  NullPointerException if <code>out</code> is <code>null</code>.
	 */
	static PovWriter getPovWriter( @NotNull final Writer out )
	{
		return ( out instanceof PovWriter ) ? (PovWriter)out : new PovWriter( out, "\t" );
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
		_declaredShapes.put( name, geom );
		return name;
	}

	/**
	 * Returns the geometry in the scene. This includes any kind of
	 * {@link PovGeometry}, including cameras and lights for example.
	 *
	 * @return  Geometry in the scene.
	 */
	public List<PovGeometry> getGeometry()
	{
		return Collections.unmodifiableList( _geometry );
	}

	/**
	 * Returns the geometry in the scene, including only instances of the given
	 * class (including any subclasses).
	 *
	 * @param   type    Type of geometry to include.
	 *
	 * @return  Geometry in the scene.
	 */
	public <T extends PovGeometry> List<T> getGeometry( final Class<T> type )
	{
		final List<T> result = new ArrayList<T>();
		for ( final PovGeometry geometry : _geometry )
		{
			if ( type.isInstance( geometry ) )
			{
				result.add( type.cast( geometry ) );
			}
		}
		return result;
	}

	/**
	 * Adds a new declared texture to the scene.
	 *
	 * @param   code        Reference name of the texture.
	 * @param   texture     Texture to add.
	 */
	public void addTexture( final String code, final PovTexture texture )
	{
		_textures.put( code, texture );
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
	 * Get codes of textures that are declared by this scene.
	 *
	 * @return  Texture codes.
	 */
	public Set<String> getTextureCodes()
	{
		return Collections.unmodifiableSet( _textures.keySet() );
	}

	/**
	 * Returns the background color of the scene.
	 *
	 * @return  Background color.
	 */
	public PovVector getBackground()
	{
		return _background;
	}

	/**
	 * Sets the background color of the scene.
	 *
	 * @param   background  Background color to be set.
	 */
	public void setBackground( final PovVector background )
	{
		_background = background;
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
	 * Returns whether radiosity is enabled.
	 *
	 * @return  <code>true</code> if radiosity is enabled;
	 *          <code>false</code> otherwise.
	 */
	public boolean isRadiosity()
	{
		return _radiosity;
	}

	/**
	 * Sets whether radiosity is enabled.
	 *
	 * @param   radiosity   <code>true</code> to enable radiosity;
	 *                      <code>false</code> to disable radiosity.
	 */
	public void setRadiosity( final boolean radiosity )
	{
		_radiosity = radiosity;
	}

	/**
	 * Returns the value that specifies the intensity of radiosity effects.
	 *
	 * @return  Brightness of radiosity effects.
	 */
	public double getRadiosityBrightness()
	{
		return _radiosityBrightness;
	}

	/**
	 * Sets the value that specifies the intensity of radiosity effects.
	 *
	 * <p>
	 * A value of <code>0.0</code> would be the same as without radiosity. The
	 * default value, <code>1.0</code> should work correctly in most cases. If
	 * effects are too strong you can reduce this. Larger values lead to quite
	 * strange results in most cases.

	 * @param   radiosityBrightness     Brightness to be set.
	 *
	 * @throws  IllegalArgumentException if <code>radiosityBrightness < 0.0</code>.
	 */
	public void setRadiosityBrightness( final double radiosityBrightness )
	{
		if ( radiosityBrightness < 0.0 )
		{
			throw new IllegalArgumentException( "radiosityBrightness" );
		}

		_radiosityBrightness = radiosityBrightness;
	}

	/**
	 * Returns the number of rays that are sent out whenever a new radiosity
	 * value has to be calculated.
	 *
	 * @return  Number of rays for radiosity calculations.
	 */
	public int getRadiosityCount()
	{
		return _radiosityCount;
	}

	/**
	 * Sets the number of rays that are sent out whenever a new radiosity
	 * value has to be calculated.
	 *
	 * <p>A value of 35 is the default, the maximum is 1600. When this value is
	 * too low, the light level will tend to look a little bit blotchy, as if
	 * the surfaces you are looking at were slightly warped. If this is not
	 * important to your scene (as in the case that you have a bump map or if
	 * you have a strong texture) then by all means use a lower number.
	 *
	 * @param   radiosityCount  Number of rays for radiosity calculations.
	 */
	public void setRadiosityCount( final int radiosityCount )
	{
		_radiosityCount = radiosityCount;
	}

	/**
	 * Returns the value of the 'error_bound' setting for radiosity effects.
	 *
	 * @return  Error bound for radiosity effects.
	 */
	public double getRadiosityErrorBound()
	{
		return _radiosityErrorBound;
	}

	/**
	 * Sets the value of the 'error_bound' setting for radiosity effects.
	 *
	 * <p>This value mainly affects the structures of the shadows. Values larger
	 * than the default of <code>1.8</code> do not have much effects, they make
	 * the shadows even flatter. Extremely low values can lead to very good
	 * results, but the rendering time can become very long.
	 *
	 * @param   radiosityErrorBound     Error bound to be set.
	 *
	 * @throws  IllegalArgumentException if <code>radiosityErrorBound <= 0.0</code>.
	 */
	public void setRadiosityErrorBound( final double radiosityErrorBound )
	{
		if ( radiosityErrorBound <= 0.0 )
		{
			throw new IllegalArgumentException( "radiosityErrorBound" );
		}

		_radiosityErrorBound = radiosityErrorBound;
	}

	/**
	 * Returns the fraction of the image width which sets the minimum radius of
	 * reuse for each sample point (actually, it is the fraction of the distance
	 * from the eye but the two are roughly equal).
	 *
	 * @return  Minimum reuse for radiosity effects.
	 */
	public double getRadiosityMinimumReuse()
	{
		return _radiosityMinimumReuse;
	}

	/**
	 * Sets the fraction of the image width which sets the minimum radius of
	 * reuse for each sample point (actually, it is the fraction of the distance
	 * from the eye but the two are roughly equal).
	 *
	 * <p>The theory is that you do not want to calculate values for every pixel
	 * into every crevice everywhere in the scene, it will take too long. If
	 * this value is too low, (which it should be in theory) rendering gets
	 * slow, and inside corners can get a little grainy. If it is set too high,
	 * you do not get the natural darkening of illumination near inside edges,
	 * since it reuses. The default value is <code>0.015</code>.
	 *
	 * @param   radiosityMinimumReuse   Minimum reuse for radiosity effects.
	 */
	public void setRadiosityMinimumReuse( final double radiosityMinimumReuse )
	{
		_radiosityMinimumReuse = radiosityMinimumReuse;
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
	@Nullable
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
				result = File.createTempFile( "PovScene-", ".pov", ( ( file != null ) && file.isDirectory() ) ? file : null );
				result.deleteOnExit();
			}
			catch ( IOException e )
			{
				throw new IOException( "failed to create temporary file: " + e.getMessage(), e );
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
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	public void write( final Writer out )
		throws IOException
	{
		final PovWriter iw = getPovWriter( out );

		/*
		 * Make sure, all geometry is sorted alfabetically.
		 */
		final PovGeometry[] geometry = _geometry.toArray( new PovGeometry[ _geometry.size() ] );
		Arrays.sort( geometry );

		//System.out.println( "WRITING POV FILE" );

		writeFileHeader( iw );
		writeAtmosphericEffects( iw );
		writeGlobalSettings( iw );
		writeCameras( iw, geometry );
		writeLights( iw, geometry );
		writeTextureDefs( iw );
		writeDeclaredShapes( iw );
		writeGeometry( iw, geometry );
	}

	protected static void writeFileHeader( final PovWriter out )
		throws IOException
	{
		//out.writeln( "#version unofficial MegaPov 0.7;" );
		out.writeln( "#include \"colors.inc\"" );
		//out.writeln( "#include \"textures.inc\"" );
		//out.writeln( "#include \"glass.inc\"" );
		//out.writeln( "#include \"metals.inc\"" );
		out.newLine();
	}

	protected void writeAtmosphericEffects( final PovWriter out )
		throws IOException
	{
		if ( _background != null )
		{
			out.writeln( "background" );
			out.writeln( "{" );
			out.indentIn();
			out.write( "rgb " );
			_background.write( out );
			out.newLine();
			out.indentOut();
			out.writeln( "}" );
		}
	}

	protected void writeGlobalSettings( final PovWriter out )
		throws IOException
	{
		out.writeln( "global_settings" );
		out.writeln( "{" );
		out.indentIn();

		out.write( "ambient_light rgb " );
		_ambientLight.write( out );
		out.newLine();
		out.write( "assumed_gamma " );
		out.writeln( PovObject.format( getAssumedGamma() ) );

		if ( _radiosity )
		{
			out.writeln( "radiosity" );
			out.writeln( "{" );
			out.indentIn();

			if ( _radiosityBrightness != 1.0 )
			{
				out.write( "brightness " );
				out.writeln( String.valueOf( _radiosityBrightness ) );
			}

			if ( _radiosityCount != 35 )
			{
				out.write( "count " );
				out.writeln( String.valueOf( _radiosityCount ) );
			}

			if ( _radiosityErrorBound != 1.8 )
			{
				out.write( "error_bound " );
				out.writeln( String.valueOf( _radiosityErrorBound ) );
			}

			if ( _radiosityMinimumReuse != 0.015 )
			{
				out.write( "minimum_reuse " );
				out.writeln( String.valueOf( _radiosityMinimumReuse ) );
			}

			out.indentOut();
			out.writeln( "}" );
		}

		out.indentOut();
		out.writeln( "}" );

		out.newLine();
	}

	protected static void writeCameras( final PovWriter out, final PovGeometry[] geometry )
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

	protected static void writeLights( final PovWriter out, final PovGeometry[] geometry )
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
	protected void writeTextureDefs( final PovWriter out )
		throws IOException
	{
		final Map<String,PovTexture> textures = _textures;

		if ( !textures.isEmpty() )
		{
			out.writeln( "/*" );
			out.writeln( " * Texture definitions" );
			out.writeln( " */" );

			for ( final PovTexture texture : textures.values() )
			{
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
	protected void writeDeclaredShapes( final PovWriter out )
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

	protected static void writeGeometry( final PovWriter out, final PovGeometry[] geometry )
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
				if ( !( geom instanceof PovLight  ) &&
				     !( geom instanceof PovCamera ) )
				{
					geom.write( out );
					out.newLine();
				}
			}
			//System.out.println( "" );
		}
	}

	/**
	 * Removes all lights from the scene.
	 */
	public void removeLights()
	{
		for ( final Iterator<PovGeometry> i = _geometry.iterator() ; i.hasNext() ; )
		{
			final PovGeometry geometry = i.next();
			if ( geometry instanceof PovLight )
			{
				i.remove();
			}
		}
	}
}
