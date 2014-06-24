package nl.ru.cmbi.vase.tools.util;

import java.util.HashMap;
import java.util.Map;

public class AminoAcid {
	
	private static final Map<Character,String> threeLetterCodes = new HashMap<Character,String>();
	private static final Map<String,Character> oneLetterCodes = new HashMap<String,Character>();
	
	public static Character aa3to1(String threeLetterCode) {
		
		if(threeLetterCodes.isEmpty())
			initData();
		
		return oneLetterCodes.get(threeLetterCode);
	}
	
	public static String aa1to3(char oneLetterCode) {
		
		if(oneLetterCodes.isEmpty())
			initData();
		
		return threeLetterCodes.get(oneLetterCode);
	}
	
	private static void initData() {
	
		threeLetterCodes.put('A', "ALA");
		
		threeLetterCodes.put('B', "ASX");
		
		threeLetterCodes.put('C', "CYS");
		
		threeLetterCodes.put('D', "ASP");
		
		threeLetterCodes.put('E', "GLU");
		
		threeLetterCodes.put('F', "PHE");
		
		threeLetterCodes.put('G', "GLY");
		
		threeLetterCodes.put('H', "HIS");
		
		threeLetterCodes.put('I', "ILE");
		
		threeLetterCodes.put('J', "XLE");
		
		threeLetterCodes.put('K', "LYS");
		
		threeLetterCodes.put('L', "LEU");
		
		threeLetterCodes.put('M', "MET");
		
		threeLetterCodes.put('N', "ASN");
		
		threeLetterCodes.put('O', "PYL");
		
		threeLetterCodes.put('P', "PRO");
		
		threeLetterCodes.put('Q', "GLN");
		
		threeLetterCodes.put('R', "ARG");
		
		threeLetterCodes.put('S', "SER");
		
		threeLetterCodes.put('T', "THR");
		
		threeLetterCodes.put('U', "SEC");
		
		threeLetterCodes.put('V', "VAL");
		
		threeLetterCodes.put('W', "TRP");
		
		threeLetterCodes.put('X', "XAA");
		
		threeLetterCodes.put('Y', "TYR");
		
		threeLetterCodes.put('Z', "GLX");
		
		for(Character oneLetterCode : threeLetterCodes.keySet()) {
			
			oneLetterCodes.put(threeLetterCodes.get(oneLetterCode),oneLetterCode);
		}
	
	}
}
