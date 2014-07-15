package nl.ru.cmbi.vase.tools.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Config {
	
	public final static Properties properties = getProperties();
	
	public static File getCacheDir() {
		
		return new File(properties.getProperty("cache"));
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

	public static File getHSSPCacheDir() {

		return new File(properties.getProperty("hsspcache"));
	}
}
