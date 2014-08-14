/**
 * Copyright 2014 CMBI (contact: <Coos.Baakman@radboudumc.nl>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	
	public static boolean cacheEnabled() {
		
		return properties.getProperty("cache")!=null && getCacheDir().isDirectory();
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
	
	public static boolean hsspPdbCacheEnabled() {
		
		return properties.getProperty("hsspcache")!=null && getHSSPCacheDir().isDirectory();
	}

	public static File getHSSPCacheDir() {

		String path=properties.getProperty("hsspcache");
		if(path==null) return null;
		
		return new File(path);
	}
}
