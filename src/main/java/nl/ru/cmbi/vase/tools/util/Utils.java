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
