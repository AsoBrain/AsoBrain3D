package apicheck;

/**
 * Checks availability of Java API's.
 */
public class ApiCheck
{
	public static boolean test( final boolean ok , final String component , final String className )
	{
		try
		{
			Class.forName( className );
			return ok;
		}
		catch ( Throwable t )
		{
			System.out.print  ( "MISSING REQUIRED JAVA API: " );
			System.out.print  ( component );
			System.out.print  ( " (" );
			System.out.print  ( className );
			System.out.println( ")" );
			return false;
		}
	}

	public static void main( final String[] args )
	{
		boolean ok = true;
		ok = test( ok , "JUnit test framework"  , "junit.framework.TestCase" );
		ok = test( ok , "Java 3D API (core)"    , "javax.media.j3d.Shape3D" );
		ok = test( ok , "Java 3D API (vecmath)" , "javax.vecmath.Vector3f" );

		if ( !ok )
		{
			System.out.println();
			System.err.println( "One or more required Java API's is missing. Please verify your build environment." );
			System.exit( 1 );
		}
	}
}
