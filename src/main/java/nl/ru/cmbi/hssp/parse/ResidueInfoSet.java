package nl.ru.cmbi.hssp.parse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResidueInfoSet implements Serializable {
	
	static Logger log = LoggerFactory.getLogger(ResidueInfoSet.class);
	
	// A pdb number could be accompanied by an insertion code
	private Map<Character,List<String>> pdbNumbers = new HashMap<Character,List<String>>();
	
	public void addChain(char chainID) {
		
		if(!pdbNumbers.containsKey(chainID))
			pdbNumbers.put(chainID, new ArrayList<String>());
	}
	
	public void addPDBNumber(char chainID, String pdbNumber) {
		
		if(!pdbNumbers.containsKey(chainID))
			addChain(chainID);
		
		pdbNumbers.get(chainID).add(pdbNumber);
	}

	public int getIndexOfPDBNumber(char chainID, String pdbNumber) {
		
		return pdbNumbers.get(chainID).indexOf(pdbNumber);
	}
	
	public List<String> getPDBNumbers(char chainID) {
		
		return pdbNumbers.get(chainID);
	}
	public void addChainReference(char sourceChain, char destChain) {
		
		if(!pdbNumbers.containsKey(sourceChain))
			addChain(sourceChain);
		
		pdbNumbers.put(destChain, pdbNumbers.get(sourceChain));
	}
}
