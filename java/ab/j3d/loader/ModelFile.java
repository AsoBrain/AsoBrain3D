package ab.j3d.loader;

import java.io.Serializable;


public class ModelFile
implements Serializable
{
	private static final long serialVersionUID = -7773739391985062123L;

	public  int ID;
	public static String HIGH_MODEL = "hi";
	public static String LOW_MODEL = "lo";
	public static String THUMBNAIL = "thumbnail";

	public  byte[] hi;
	public  byte[] lo;
	public  byte[] thumbnail;
	public  String name;
	public static final String TABLE_NAME = "JarFiles";

}