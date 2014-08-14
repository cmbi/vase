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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	
	public static final String stockholmLocationURL = "ftp://ftp.cmbi.ru.nl/pub/molbio/data/hssp3",
								rcsbLocationURL = "http://www.rcsb.org/pdb/files";
	
	
	public static URL getStockholmURL( String pdbid ) throws MalformedURLException {
		
		return new URL(String.format("%s/%s.hssp.bz2",stockholmLocationURL, pdbid));
	}
	
	public static URL getRcsbURL( String pdbid ) throws MalformedURLException {
		
		return new URL(String.format("%s/%s.pdb",rcsbLocationURL, pdbid));
	}
	
	public static <T extends Number> T max(List<T> numbers) {
		
		T highest = numbers.get(0);
		for(T n : numbers) {
			
			if(n.doubleValue()>highest.doubleValue()) {
				
				highest=n;
			}
		}
		return highest;
	}
	public static <T extends Number> T min(List<T> numbers) {
		
		T lowest = numbers.get(0);
		for(T n : numbers) {
			
			if(n.doubleValue()<lowest.doubleValue()) {
				
				lowest=n;
			}
		}
		return lowest;
	}
	
	public static List<Integer> listRange(int start,int end) {

		List<Integer> is = new ArrayList<Integer>();
		for(int i=start; i<end; i++){
			is.add(i);
		}
		return is;
	}
}
