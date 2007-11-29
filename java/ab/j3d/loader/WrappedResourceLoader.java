package ab.j3d.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WrappedResourceLoader
	implements ResourceLoader
{

	private ResourceLoader _realResourceLoader;
	private Set<String> _requestedFiles = new HashSet<String>();

	public WrappedResourceLoader( final ResourceLoader realResourceLoader )
	{
		_realResourceLoader = realResourceLoader;
	}

	public InputStream getResource( final String name )
		throws IOException
	{
		_requestedFiles.add( name );
		return _realResourceLoader.getResource( name );
	}

	public Collection<String> getRequestedFiles()
	{
		return Collections.unmodifiableSet( _requestedFiles );
	}

}
