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
package nl.ru.cmbi.vase.data.stockholm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	public Set<Character> listChainIDs() {
		
		return residues.keySet();
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
