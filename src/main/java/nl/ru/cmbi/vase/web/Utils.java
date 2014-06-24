package nl.ru.cmbi.vase.web;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	
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
