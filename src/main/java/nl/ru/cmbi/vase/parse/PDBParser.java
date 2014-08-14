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
package nl.ru.cmbi.vase.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.ru.cmbi.vase.data.pdb.PDBResidueInfo;

public class PDBParser {
	
	private static final List<String> proteinBackboneAtoms = Arrays.asList(new String[] {"CA","C","N","O"});
	
	public static Map<Character,Map<String,PDBResidueInfo>> parseResidues(InputStream pdbIn) throws IOException {
		
		Map<Character,Map<String,PDBResidueInfo>> residues = new HashMap<Character,Map<String,PDBResidueInfo>>();
		Map<String,List<String>> residueAtoms = new HashMap<String,List<String>>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(pdbIn));
		
		int nmodels=0;
		String line; while((line=reader.readLine())!=null) {
			
			if(line.startsWith("MODEL ")) { // only use the first NMR model
				
				if(nmodels>0) break;
				
				nmodels++;
			}
			
			if(line.startsWith("ATOM ") || line.startsWith("HETATM ")) {
				
				String	atomName	= line.substring(12,16).trim(),
						resName		= line.substring(17,20).trim(),
						resNumber	= line.substring(22,27).trim(); // includes insertion code
				char chain = line.charAt(21);
				
				if(!residueAtoms.containsKey(resNumber)) {
					residueAtoms.put(resNumber, new ArrayList<String>());
				}
				
				residueAtoms.get(resNumber).add(atomName);
				
				if( residueAtoms.get(resNumber).containsAll(proteinBackboneAtoms) ) {
					
					if(!residues.containsKey(chain)) {
						
						residues.put(chain, new HashMap<String,PDBResidueInfo>());
					}
					
					residues.get(chain).put(resNumber, new PDBResidueInfo(chain,resNumber,resName));
					
					residueAtoms.remove(resNumber); // to prevent adding it twice
				}
			}
		}
		
		return residues;
	}
}
