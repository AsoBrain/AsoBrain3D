package apicheck;

/**
 * Checks availability of Java API's.
 *
 * @author  unascribed
 * @version $Revision$
 */
public final class ApiCheck
{
	/**
	 * Application class is not supposed to be instantiated.
	 */
	private ApiCheck()
	{
	}

	/**
	 * Test presence of API.
	 *
	 * @param   ok          Previous test result.
	 * @param   component   Name of component to display to user.
	 * @param   className   Name of class belonging to API to detect presence.
	 *
	 * @return  Test result.
	 */
	public static boolean test( final boolean ok , final String component , final String className )
	{
		boolean result = ok;
		try
		{
			Class.forName( className );
		}
		catch ( ClassNotFoundException e )
		{
			System.out.print  ( "MISSING REQUIRED JAVA API: " );
			System.out.println( component );
			result = false;
		}
		return result;
	}

	/**
	 * Run application.
	 *
	 * @param   args    Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		boolean ok = true;
		ok = test( ok , "JUnit test framework"                 , "junit.framework.TestCase" );
		ok = test( ok , "Java 3D API (core)"                   , "javax.media.j3d.Shape3D" );
		ok = test( ok , "Java 3D API (vecmath)"                , "javax.vecmath.Vector3f" );
		ok = test( ok , "Numdata Open Source Software Library" , "com.numdata.oss.TextTools" );

		if ( !ok )
		{
			System.out.println();
			System.err.println( "One or more required Java API's is missing. Please verify your build environment." );
			System.exit( 1 );
		}
	}
}
