/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2007-2007 Numdata BV.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Numdata BV (http://www.numdata.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Numdata" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission of Numdata BV. For written permission, please contact
 *    info@numdata.com.
 *
 * 5. Products derived from this software may not be called "Numdata",
 *    nor may "Numdata" appear in their name, without prior written
 *    permission of Numdata BV.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE NUMDATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */

package ab.j3d.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is not an actual resourceloader, it only forwards calls to the actual resourceloader it was constructed with.
 * Additionally, it remembers the files being asked for and stores them in a {@link HashSet}.
 *
 * <br /><br />
 *
 * @author  Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public class WrappedResourceLoader
	implements ResourceLoader
{
	/**
	 * Actual resourceloader
	 */
	private ResourceLoader _actualResourceLoader;

	/**
	 * Set of requested files.
	 */
	private Set<String> _requestedFiles = new HashSet<String>();

	/**
	 * Constructs a new {@link WrappedResourceLoader}.
	 *
	 * @param actualResourceLoader  The actual resourceloader to load resource from.
	 */
	public WrappedResourceLoader( final ResourceLoader actualResourceLoader )
	{
		_actualResourceLoader = actualResourceLoader;
	}

	public InputStream getResource( final String name )
		throws IOException
	{
		_requestedFiles.add( name );
		return _actualResourceLoader.getResource( name );
	}

	/**
	 * Returns a collection of requested file names as strings.
	 *
	 * @return  a collection of requested file names a strings.
	 */
	public Collection<String> getRequestedFiles()
	{
		return Collections.unmodifiableSet( _requestedFiles );
	}

}
