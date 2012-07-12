/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.awt;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.*;

import ab.j3d.pov.*;
import org.jetbrains.annotations.*;

/**
 * This class provides a frontend for the POV-Ray renderer. It can be used to
 * render a {@link PovScene} using a native POV-Ray executable.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class PovRenderer
{
	/**
	 * Renders the scene to an image with the specified size and returns the
	 * resulting image.
	 *
	 * @param   povScene        Scene to render.
	 * @param   povFile         File or directory to write POV file to (optional).
	 * @param   width           The width of the rendered image.
	 * @param   height          The height of the rendered image.
	 * @param   progressModel   Progress model.
	 * @param   log             Log to write console output to.
	 * @param   background      Whether or not to draw a background.
	 *
	 * @return  Rendered image.
	 *
	 * @throws  IOException if there was a problem reading/writing data.
	 */
	public static BufferedImage render( final PovScene povScene, final File povFile, final int width, final int height, final BoundedRangeModel progressModel, final PrintWriter log, final boolean background )
		throws IOException
	{
		BufferedImage result;

		final File actualPovFile = povScene.write( povFile );

		final Process process = startPovRay( actualPovFile, width, height, background );
		try
		{
			monitorPovRayProcess( process, height, progressModel, log );

			/*
			 * Pipe data from 'stdout' to 'out'
			 */
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
			}
			catch ( IOException e )
			{
				throw new IOException( "POV-Ray command execution failed", e );
			}
		}
		finally
		{
			process.destroy();

			if ( ( actualPovFile != null ) && ( actualPovFile != povFile ) )
			{
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
	 * @param   background  Whether or not to draw a background.
	 *
	 * @return  POV-Ray process.
	 *
	 * @throws  IOException if the POV-Ray executable could not be accessed.
	 * @throws  SecurityException if a security manager prevents file access.
	 */
	public static Process startPovRay( final File povFile, final int width, final int height, final boolean background )
		throws IOException
	{
		return startPovRay( povFile, width, height, background, 0, width );
	}

	/**
	 * Start POV-Ray render process. By specifying start and end columns, only
	 * a part of the image can be rendered.
	 *
	 * @param   povFile         File containing POV-scene.
	 * @param   width           The width of the rendered image.
	 * @param   height          The height of the rendered image.
	 * @param   background      Whether or not to draw a background.
	 * @param   startColumn     First column (x-coordinate) to render.
	 * @param   endColumn       Last column (x-coordinate) to render.
	 *
	 * @return  POV-Ray process.
	 *
	 * @throws  IOException if the POV-Ray executable could not be accessed.
	 * @throws  SecurityException if a security manager prevents file access.
	 */
	public static Process startPovRay( final File povFile, final int width, final int height, final boolean background, final int startColumn, final int endColumn )
		throws IOException
	{
		return startPovRay( getPovrayCommand( povFile, width, height, background, startColumn, endColumn ) );
	}

	/**
	 * Start POV-Ray render process.
	 *
	 * @param   command         POV-Ray command.
	 *
	 * @return  POV-Ray process.
	 *
	 * @throws  IOException if the POV-Ray executable could not be accessed.
	 * @throws  SecurityException if a security manager prevents file access.
	 */
	public static Process startPovRay( final List<String> command )
		throws IOException
	{
		/*
		 * Start POV-Ray process.
		 */
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
			System.err.println( "failed to close 'stdin' of render process:" + e );
		}

		return result;
	}

	/**
	 * Get POV-Ray command-line to execute. By specifying start and end columns,
	 * only a part of the image can be rendered.
	 *
	 * @param   povFile         File containing POV-scene.
	 * @param   width           The width of the rendered image.
	 * @param   height          The height of the rendered image.
	 * @param   background      Whether or not to draw a background.
	 * @param   startColumn     First column (x-coordinate) to render.
	 * @param   endColumn       Last column (x-coordinate) to render.
	 *
	 * @return  POV-Ray command line.
	 *
	 * @throws  SecurityException if a security manager prevents file access.
	 */
	public static List<String> getPovrayCommand( final File povFile, final int width, final int height, final boolean background, final int startColumn, final int endColumn )
	{
		final List<String> command = new ArrayList<String>( 16 );

		command.add( "povray" );                   /* POV-Ray executable */
		command.add( "+I" + povFile.getPath() );   /* Input file ('-' = stdin) */
		command.add( "+O-" );                      /* Output file ('-' = stdout) */
		command.add( "+FN" );                      /* File format: PNG */
		command.add( "+W" + width );               /* Image width */
		command.add( "+H" + height );              /* Image height */
		command.add( "-D" );                       /* Don't show preview */
		command.add( "+Q9" );                      /* Quality (default=9) */
		command.add( "+A0.2" );                    /* Turn on anti-aliasing */
		command.add( "+AM1" );
		command.add( "+R3" );
		command.add( "+GA" );                      /* Turn on all debug, fatal, render, statistic, and warning text to the console */
		command.add( background ? "-UA" : "+UA" ); /* Turn on/off alpha channel output */

		if ( startColumn != 0 )
		{
			command.add( "+SC" + startColumn ); /* Turn on/off alpha channel output */
		}
		if ( endColumn != height - 1 )
		{
			command.add( "+EC" + endColumn ); /* Turn on/off alpha channel output */
		}

		return command;
	}

	/**
	 * Monitor 'stderr' for progress monitoring and logging from POV-Ray.
	 *
	 * @param   povProcess      POV-Ray process to monitor.
	 * @param   height          The height of the rendered image.
	 * @param   progressModel   Progress model.
	 * @param   log             Log to write console output to.
	 */
	public static void monitorPovRayProcess( @NotNull final Process povProcess, final int height, @Nullable final BoundedRangeModel progressModel, @Nullable final PrintWriter log )
	{
		if ( ( progressModel != null ) || ( log != null ) )
		{
			final PovRayProcessMonitor monitor = new PovRayProcessMonitor( povProcess, height, progressModel, log );
			final Thread stderrMonitor = new Thread( monitor );
			stderrMonitor.start();
		}
	}

	/**
	 * Monitor for POV-Ray to read output from 'stderr' and monitor progress
	 * reported by POV-Ray in real time.
	 */
	public static class PovRayProcessMonitor
		implements Runnable
	{
		/**
		 * Progress model.
		 */
		@NotNull
		private final Process _povProcess;

		/**
		 * Progress model.
		 */
		@Nullable
		private final BoundedRangeModel _progressModel;

		/**
		 * Log to write console output to.
		 */
		@Nullable
		private final PrintWriter _log;

		/**
		 * Construct monitor.
		 *
		 * @param   povProcess      POV-Ray process to monitor.
		 * @param   height          The height of the rendered image.
		 * @param   progressModel   Progress model.
		 * @param   log             Log to write console output to.
		 */
		public PovRayProcessMonitor( @NotNull final Process povProcess, final int height, @Nullable final BoundedRangeModel progressModel, @Nullable final PrintWriter log )
		{
			if ( progressModel != null )
			{
				progressModel.setMinimum( 0 );
				progressModel.setValue( 0 );
				progressModel.setMaximum( height );
			}

			_povProcess = povProcess;
			_progressModel = progressModel;
			_log = log;
		}

		public void run()
		{
			final InputStream stderr = _povProcess.getErrorStream();
			try
			{
				final BufferedReader errorStream = new BufferedReader( new InputStreamReader( stderr ) );
				String line;
				while ( ( line = errorStream.readLine() ) != null )
				{
					if ( line.contains( " Rendering line " ) )
					{
						if ( _progressModel != null )
						{
							String temp = line.substring( line.indexOf( " Rendering line " ) + 16 );
							final int end = temp.indexOf( (int)' ' );
							temp = temp.substring( 0, end );

							try
							{
								final int value = Integer.parseInt( temp );
								_progressModel.setValue( value );
							}
							catch( Exception e )
							{
								/* ignore */
							}

						}
					}
					else if ( _log != null )
					{
						_log.println( line );
						_log.flush();
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
	}
}
