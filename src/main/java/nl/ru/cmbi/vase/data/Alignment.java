package nl.ru.cmbi.vase.data;

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
