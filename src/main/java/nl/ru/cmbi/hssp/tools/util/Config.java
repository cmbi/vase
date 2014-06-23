package nl.ru.cmbi.hssp.tools.util;

import java.io.IOException;
import java.util.Properties;

public class Config {
	
	public static Properties properties = getProperties();
	
	private static Properties getProperties() {
		
		Properties p = new Properties();
		try {
			p.load(Config.class.getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			
			throw new RuntimeException("Error parsing config file",e);
		}
		
		return p;
	}

	
}
