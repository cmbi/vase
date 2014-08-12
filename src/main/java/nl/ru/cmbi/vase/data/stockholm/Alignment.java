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

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.io.Serializable;

import java.lang.Math;

import nl.ru.cmbi.vase.analysis.Calculator;
import nl.ru.cmbi.vase.analysis.MutationDataObject;
import nl.ru.cmbi.vase.tools.util.AminoAcid;

public class Alignment implements Serializable {
	
	private LinkedHashMap<String,String> alignment = new LinkedHashMap<String,String>();
	
	private int length = 0;
	
	private char chainID;
	
	public char getChainID() {
		
		return chainID;
	}
	
	public Alignment(char chainID,LinkedHashMap<String,StringBuilder> seqs) {

		this.chainID=chainID;
		
		for (Entry<String,StringBuilder> e : seqs.entrySet()) {
			
			alignment.put(e.getKey(), e.getValue().toString());
			
			length=Math.max(length,e.getValue().length());
		}
	}
	public Alignment(Map<String,String> seqs) {

		this.chainID='-';
		
		for (Entry<String,String> e : seqs.entrySet()) {
			
			alignment.put(e.getKey(), e.getValue());
			
			length=Math.max(length,e.getValue().length());
		}
	}
	
	public List<String> getLabels() {
		
		return new ArrayList<String>(alignment.keySet());
	}
	
	public String getAlignedSeq(String label) {
		
		return alignment.get(label);
	}
	
	public int countColumns() {
		
		return length;
	}
	
	public int countAlignedSeqs() {
		
		return alignment.size();
	}
	
	public Map<String,String> getMap() {
		
		return alignment;
	}
}
