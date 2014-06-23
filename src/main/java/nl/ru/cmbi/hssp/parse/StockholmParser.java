package nl.ru.cmbi.hssp.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import nl.ru.cmbi.hssp.data.AlignmentSet;
import nl.ru.cmbi.hssp.data.ResidueInfo;
import nl.ru.cmbi.hssp.data.ResidueInfoSet;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;

public class StockholmParser {
	
	static Logger log = LoggerFactory.getLogger(StockholmParser.class);
	
	// Regular expressions for to recognize specific lines in the stockholm files:
	private static final String
		pdbIDPattern = "^#=GF CC PDBID\\s+[\\w\\-]+\\s*$",
		chainPattern = "^#=GF ID\\s+[\\w]{4}\\/[A-Z0-9]\\s*$",
		
		chainsdefPattern = "^#=GF CC DBREF\\s+[1-9][A-Z0-9]{3}\\s+([A-Z0-9])\\s+.*$",
		equalchainsPattern = "^#=GF CC Chain ([A-Z]) is considered to be the same as ([A-Z](?:, [A-Z])*(?: and [A-Z])?)$",
		
		variabilityPattern = "^#=GF\\s+RI" +
			"\\s+([0-9]+)" + // SeqNo
			"\\s+(\\-?[0-9]+[\\sA-Z])([A-Za-z0-9])" + // PDBNo CHAIN
			"\\s+([A-Za-z])" + // AA
			"\\s+([A-Za-z0-9\\s\\-\\+\\>\\<]{9})" + // STRUCTURE
			"\\s*([0-9]+)\\s*([0-9]+[A-Z]?)\\s+([0-9]+)\\s+" + // BP1 BP2  ACC
		"([0-9]+)\\s+([0-9]+)\\s*$", // NOCC VAR
	
		profilePattern = "^#=GF\\s+PR" +
			"\\s+([0-9]+)" + // SeqNo
			"\\s+(\\-?[0-9]+[\\sA-Z])([A-Za-z0-9])" + // PDBNo CHAIN
			"\\s+((?:[0-9]+\\s+){20})" + // per amino acid score
			"([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+" + // NOCC NDEL NINS 
			"([0-9]+\\.[0-9]+)\\s+([0-9]+)\\s+([0-9]+\\.[0-9]+)\\s*$", // ENTROPY RELENT WEIGHT
	
		seqPattern = "^[\\w\\-\\/]+\\s+[A-Z\\.]+$";
	
	public static ResidueInfoSet parseResidueInfo(InputStream is) throws Exception {
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		final Pattern	vp = Pattern.compile(variabilityPattern),
						pp = Pattern.compile(profilePattern);
		
		ResidueInfoSet residueInfoSet = new ResidueInfoSet();

		String line;
		while((line=reader.readLine())!=null) {

			Matcher vm = vp.matcher(line),
					pm = pp.matcher(line);
			
			if(vm.matches()) {
				
				char chain = vm.group(3).charAt(0), AA = vm.group(4).charAt(0);
				String pdbno = vm.group(2).trim();
				int seqno	= Integer.parseInt(vm.group(1).trim()),
					var		= Integer.parseInt(vm.group(10).trim());
				
				ResidueInfo res = residueInfoSet.getResidue(chain, seqno);
				
				res.setPdbNumber(pdbno);
				res.setVar(var);
				res.setAa(AA);
				
			} else if (pm.matches()) {
				
				char chain = pm.group(3).charAt(0);
				
				int seqno	= Integer.parseInt(pm.group(1).trim()),
					relent	= Integer.parseInt(pm.group(9).trim());
				
				double	entropy	= Double.parseDouble(pm.group(8)),
						weight	= Double.parseDouble(pm.group(10));
				
				ResidueInfo res = residueInfoSet.getResidue(chain, seqno);
				
				res.setEntropy(entropy);
				res.setRelent(relent);
				res.setWeight(weight);
				
			} else if (line.matches(equalchainsPattern)) {
			// these lines define references of one chain to the other

				final String[] s = line.trim().split("\\s+");
				final char sourceChain = s[3].charAt(0);
				
				// Chain listing starts at the 11th word in the expression.
				for(int i=11; i<s.length; i++) {
					
					if(s.equals("and") ) continue; // 'and' is not a chain-ID, it's an interjection
					
					final char destChain=s[i].charAt(0); // Take the first character in the word, thus not the commas!
					
					residueInfoSet.addChainReference(sourceChain,destChain);
				}
			}
		}
		
		return residueInfoSet;
	}
	
	public static AlignmentSet parseAlignments(InputStream is) throws Exception {
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		final AlignmentSet alignments = new AlignmentSet();

		String pdbID = null;
		char currentChain='A';
		String line; int linenr=0;
		while((line=reader.readLine())!=null) {
			linenr++;
			
			if(line.matches(pdbIDPattern)) {
				
				final String[] s = line.trim().split("\\s+");
				
				pdbID=s[s.length-1];
				
			} else if (line.matches(chainPattern)) {
			
				final String[] s = line.trim().split("\\s+");
				final String id=s[s.length-1], ac=id.substring(0,4);
				currentChain=id.charAt(5);
	
				if(pdbID!=null && !pdbID.equalsIgnoreCase(ac)) {
					throw new Exception("line "+linenr+": got id "+ac+", but expected: "+pdbID);
				}
				pdbID=ac;
				
				alignments.addChain(currentChain);
				
			} else if (line.matches(chainsdefPattern)) {
			// these lines list the IDs of the chains in the pdb file
				
				final String[] s = line.trim().split("\\s+");
				final String ac=s[3];
				final char chain = s[4].charAt(0);

				if(pdbID!=null && !pdbID.equalsIgnoreCase(ac)) {
					throw new Exception("line "+linenr+": got id "+ac+", but expected: "+pdbID);
				}
				pdbID=ac;
				
				alignments.addChain(chain);
				
			} else if (line.matches(equalchainsPattern)) {
			// these lines define references of one chain to the other

				final String[] s = line.trim().split("\\s+");
				final char sourceChain = s[3].charAt(0);
				
				// Chain listing starts at the 11th word in the expression.
				for(int i=11; i<s.length; i++) {
					
					if(s.equals("and") ) continue; // 'and' is not a chain-ID, it's an interjection
					
					final char destChain=s[i].charAt(0); // Take the first character in the word, thus not the commas!
					
					alignments.addChainReference(sourceChain,destChain);
				}

			}
			else if(line.matches(seqPattern)) {
				
				final String[] s = line.split("\\s+");
				
				final String key = s[0], seq = s[1];
				
				alignments.addToSeq(currentChain,key,seq);
			}
		}
		
		is.close();
		
		return alignments;
	}
}
