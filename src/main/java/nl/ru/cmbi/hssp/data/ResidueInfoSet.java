package nl.ru.cmbi.hssp.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResidueInfoSet implements Serializable {
	
	static Logger log = LoggerFactory.getLogger(ResidueInfoSet.class);
	
	private Map<Character,Map<Integer,ResidueInfo>> residues = new HashMap<Character,Map<Integer,ResidueInfo>>();
	
	public void addChain(char chainID) {
		
		if(!residues.containsKey(chainID))
			residues.put(chainID, new HashMap<Integer,ResidueInfo>());
	}
	
	public void addResidue(char chainID,int seqno) {

		if(!residues.containsKey(chainID))
			addChain(chainID);
		
		residues.get(chainID).put(seqno, new ResidueInfo());
	}
	
	public Map<Integer,ResidueInfo> getChain(char chainID) {
		
		if(!residues.containsKey(chainID))
			addChain(chainID);
		
		return residues.get(chainID);
	}
	
	public ResidueInfo getResidue(char chainID, int seqno) {

		if(!residues.containsKey(chainID))
			addChain(chainID);
		
		if(!residues.get(chainID).containsKey(seqno))
			addResidue(chainID,seqno);
		
		return residues.get(chainID).get(seqno);
	}

	public void addChainReference(char sourceChain, char destChain) {
		
		if(!residues.containsKey(sourceChain))
			addChain(sourceChain);
		
		residues.put(destChain, residues.get(sourceChain));
	}
}
