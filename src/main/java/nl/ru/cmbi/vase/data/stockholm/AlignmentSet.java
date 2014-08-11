package nl.ru.cmbi.vase.data.stockholm;

import java.io.Serializable;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AlignmentSet implements Serializable {

	final LinkedHashMap<Character,LinkedHashMap<String,StringBuilder>> alignmentsPerChain = new LinkedHashMap<Character,LinkedHashMap<String,StringBuilder>>();
	
	public void addChain(char chainID) {
		
		if(!alignmentsPerChain.containsKey(chainID)) {
			alignmentsPerChain.put(chainID, new LinkedHashMap<String,StringBuilder>());
		}
	}
	
	public void addChainReference(char sourceChain, char destChain) {
		

		if(!alignmentsPerChain.containsKey(sourceChain))
			addChain(sourceChain);
		
		alignmentsPerChain.put(destChain, alignmentsPerChain.get(sourceChain));
	}
	
	public void addToSeq(char chainID, String seqID, String seq) {
		
		if(!alignmentsPerChain.containsKey(chainID)) {
			
			addChain(chainID);
		}
		
		if(!alignmentsPerChain.get(chainID).containsKey(seqID)) {
			
			alignmentsPerChain.get(chainID).put(seqID, new StringBuilder());
		}
		
		alignmentsPerChain.get(chainID).get(seqID).append(seq);
	}
	
	public List<Character> getChainIDs() {
		
		return new ArrayList(alignmentsPerChain.keySet());
	}
	
	public boolean hasChain(char chainID) {
		
		return alignmentsPerChain.containsKey(chainID);
	}
	
	public Alignment getAlignment(char chainID) {
		
		return new Alignment(chainID, alignmentsPerChain.get(chainID));
	}
}
