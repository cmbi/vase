package nl.ru.cmbi.vase.tools.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Config {
	
	public final static Properties properties = getProperties();
	
	public static File getCacheDir() {
		
		String path=properties.getProperty("cache");
		if(path==null) return null;
		
		return new File(path);
	}
	
	private static Properties getProperties() {
		
		Properties p = new Properties();
		try {
			p.load(Config.class.getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			
			throw new RuntimeException("Error parsing config file",e);
		}
		
		return p;
	}
	
	public static boolean isXmlOnly() {
		
		return Boolean.parseBoolean( properties.getProperty("xmlonly") );
	}

	public static File getHSSPCacheDir() {

		String path=properties.getProperty("hsspcache");
		if(path==null) return null;
		
		return new File(path);
	}
}
