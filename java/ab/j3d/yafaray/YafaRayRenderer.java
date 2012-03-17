/*
 * $Id$
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
package ab.j3d.yafaray;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import ab.j3d.model.*;

/**
 * Renders scenes using YafaRay. To render a {@link Scene}, it has to be
 * converted to a YafaRay scene first, e.g. using {@link YafaRayWriter}.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class YafaRayRenderer
{
	/**
	 * File name of the YafaRay executable.
	 */
	private String _executable;

	/**
	 * Constructs a new instance.
	 *
	 * @param   executable  File name of the YafaRay executable.
	 */
	public YafaRayRenderer( final String executable )
	{
		_executable = executable;
	}

	/**
	 * Renders the specified YafaRay scene and saves the resulting rendering
	 * at the specified target location in PNG format.
	 *
	 * @param   scene   Scene to be rendered.
	 * @param   target  Location of rendered image.
	 *
	 * @throws  IOException if an I/O error occurs.
	 * @throws  InterruptedException if the current thread is interrupted while
	 *          waiting for YafaRay.
	 */
	public void render( final File scene, final File target )
	throws IOException, InterruptedException
	{
		final File outputFolder = File.createTempFile( "yafaray", null );
		outputFolder.delete();
		outputFolder.mkdir();

		final File outputFile = new File( outputFolder, "yafaray.tga" );
		final BufferedImage renderedImage;

		try
		{
			final ProcessBuilder processBuilder = new ProcessBuilder( _executable, "-o", outputFolder.getAbsolutePath(), scene.getAbsolutePath() );
			final Process process = processBuilder.start();

			final InputStream processOutput = process.getInputStream();
			int read;
			while ( ( read = processOutput.read() ) != -1 )
			{
				System.out.write( read );
				System.out.flush();
			}

			process.waitFor();

			if ( process.exitValue() != 0 )
			{
				throw new IOException( "YafaRay process exited with value " + process.exitValue() );
			}

			final InputStream tgaIn = new BufferedInputStream( new FileInputStream( outputFile ) );
			try
			{
				final TargaReader targaReader = new TargaReader();
				renderedImage = targaReader.read( tgaIn );
			}
			finally
			{
				tgaIn.close();
			}
		}
		finally
		{
			if ( outputFile.delete() )
			{
				outputFolder.delete();
			}
		}

		ImageIO.write( renderedImage, "png", target );
	}
}
