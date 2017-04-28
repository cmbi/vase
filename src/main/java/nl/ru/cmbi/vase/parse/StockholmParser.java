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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import nl.ru.cmbi.vase.analysis.Calculator;
import nl.ru.cmbi.vase.analysis.MutationDataObject;
import nl.ru.cmbi.vase.data.TableData;
import nl.ru.cmbi.vase.data.TableData.ColumnInfo;
import nl.ru.cmbi.vase.data.pdb.PDBResidueInfo;
import nl.ru.cmbi.vase.data.stockholm.Alignment;
import nl.ru.cmbi.vase.data.stockholm.AlignmentSet;
import nl.ru.cmbi.vase.data.stockholm.ResidueInfo;
import nl.ru.cmbi.vase.data.stockholm.ResidueInfoSet;
import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.tools.util.AminoAcid;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

import lombok.Data;

public class StockholmParser {
	
	static Logger log = LoggerFactory.getLogger(StockholmParser.class);
	
	// Regular expressions for to recognize specific lines in the stockholm files:
	public static final String
		pdbIDLinePattern = "^#=GF CC PDBID\\s+[\\w\\-]+\\s*$",
		chainLinePattern = "^#=GF ID\\s+([\\-_0-9a-zA-Z]*)\\/([A-Z0-9])\\s*$",
		
		dbRefLinePattern = "^#=GF CC DBREF\\s+[1-9][A-Z0-9]{3}\\s+([A-Z0-9])\\s+.*$",
		equalchainsLinePattern = "^#=GF CC Chain ([A-Z]) is considered to be the same as ([A-Z](?:, [A-Z])*(?: and [A-Z])?)$",
		
		variabilityLinePattern = "^#=GF\\s+RI" +
			"\\s+([0-9]+)" + // SeqNo
			"\\s+(\\-?[0-9]+[\\sA-Z])([A-Za-z0-9])" + // PDBNo CHAIN
			"\\s+([A-Za-z])" + // AA
			"\\s+([A-Za-z0-9\\s\\-\\+\\>\\<]{9})" + // STRUCTURE
			"\\s*([0-9]+)\\s*([0-9]+[A-Z]?)\\s+([0-9]+)\\s+" + // BP1 BP2  ACC
		"([0-9]+)\\s+([0-9]+)\\s*$", // NOCC VAR
	
		profileLinePattern = "^#=GF\\s+PR" +
			"\\s+([0-9]+)" + // SeqNo
			"\\s+(\\-?[0-9]+[\\sA-Z])([A-Za-z0-9])" + // PDBNo CHAIN
			"\\s+((?:[0-9]+\\s+){20})" + // per amino acid score
			"([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+" + // NOCC NDEL NINS 
			"([0-9]+\\.[0-9]+)\\s+([0-9]+)\\s+([0-9]+\\.[0-9]+)\\s*$", // ENTROPY RELENT WEIGHT
	
		seqLinePattern = "^([\\w\\-\\/]*)\\s+([A-Z\\.]+)$",
		
		pdbAcPattern = "^([0-9][0-9a-zA-Z]{3})(\\/[A-Z0-9a-z])?$",
		
		uniprotAcPattern = "^([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(\\/[0-9]+\\-[0-9]+)?$";

	
	public static final Pattern
			pPDBAC		= Pattern.compile(pdbAcPattern),
			pUniprotAC	= Pattern.compile(uniprotAcPattern);
	
	private static URL determineRefecenceURL(String label) {
		
		Matcher mPDB	= pPDBAC.matcher(label),
				mUniprot= pUniprotAC.matcher(label);

		try {
			if(mPDB.matches()) {
				
				return new URL("http://www.rcsb.org/pdb/explore/explore.do?structureId="
						+ mPDB.group(1));
			}
			else if(mUniprot.matches()) {
				
				return new URL("http://www.uniprot.org/uniprot/"+mUniprot.group(1));
			}
			else return null;
			
		} catch (MalformedURLException e) {
			
			return null;
		}
	}
	
	public static Map<Character,VASEDataObject> parseStockHolm(InputStream stockholmIn, InputStream pdbIn, String pdbID)
		throws Exception {
		
		AlignmentSet alignments = new AlignmentSet();
		ResidueInfoSet residueInfoSet = new ResidueInfoSet();
		
		goThroughStockholm(stockholmIn,alignments,residueInfoSet,'*');
		
		return generateVaseObjects (alignments, residueInfoSet, pdbID, pdbIn);
	}	
	
	/**
	 * Fast alternative to parsing the entire file
	 * @param chain the requested chain in the stockholm file
	 */
	public static VASEDataObject parseStockHolm(InputStream stockholmIn, InputStream pdbIn, String pdbID, char chain)
		throws Exception {
		
		AlignmentSet alignments = new AlignmentSet();
		ResidueInfoSet residueInfoSet = new ResidueInfoSet();
		
		goThroughStockholm(stockholmIn,alignments,residueInfoSet,chain);
		
		Map<Character,VASEDataObject> map = generateVaseObjects(alignments, residueInfoSet, pdbID, pdbIn);
		
		if(!map.containsKey(chain)) {
			
			throw new Exception("Chain not parsed: "+chain+", alignments: "+alignments.getChainIDs()+", infos:"+residueInfoSet.listChainIDs());
		}
		
		return map.get(chain);
	}
	
	private static Map<Character,VASEDataObject> generateVaseObjects ( 
			AlignmentSet alignments,
			ResidueInfoSet residueInfoSet,
			String pdbID,
			InputStream pdbIn) throws Exception {
		
		log.info("generating vase object with " + pdbIn);

		Map<Character,VASEDataObject> map = new HashMap<Character,VASEDataObject>();
		for(char chainID : alignments.getChainIDs()) {
			
			Alignment alignment = alignments.getAlignment(chainID);
			
			Map<Character,Map<String,PDBResidueInfo>> pdbResidues = PDBParser.parseResidues(pdbIn);
			pdbIn.close();
			
			VASEDataObject data = new VASEDataObject(
					alignment, 
					getTable(alignments, pdbResidues, residueInfoSet, chainID),
					pdbID);

			data.setTitle( String.format("Alignment of %s chain %c", pdbID, chainID) );
						
			for( String label : alignment.getLabels() ) {

				URL url = determineRefecenceURL(label);
				if(url!=null) {
					data.getSequenceReferenceURLs().put(label, url);
				}
			}
			
			VASEDataObject.PlotDescription pd = new VASEDataObject.PlotDescription();
			pd.setPlotTitle("Entropy vs. Variability");
			pd.setXAxisColumnID("variability");
			pd.setYAxisColumnID("entropy");
			data.getPlots().add(pd);

			pd = new VASEDataObject.PlotDescription();
			pd.setPlotTitle("Entropy vs. Alignment Position");
			pd.setXAxisColumnID("residue_number");
			pd.setYAxisColumnID("entropy");
			data.getPlots().add(pd);
			
			map.put(chainID, data);
		}
		
		return map;
	}
	
	/**
	 * Fast alternative to parsing the entire file
	 */
	public static Set<Character> listChainsInStockholm(InputStream stockholmIn) throws IOException {
		
		Set<Character> chains = new HashSet<Character>();
		
		String pdbID = null;
		char currentChain='A';
		final Pattern	vp = Pattern.compile(variabilityLinePattern),
						pp = Pattern.compile(profileLinePattern),
						cp = Pattern.compile(chainLinePattern);

		final BufferedReader reader = new BufferedReader(new InputStreamReader(stockholmIn));
		String line;
		while((line=reader.readLine())!=null) {

			Matcher vm = vp.matcher(line),
					pm = pp.matcher(line),
					cm = cp.matcher(line);
			
			if(line.trim().equals("//")) { // indicates the end of the current chain
				
				currentChain = ' ';
			}
			else if (cm.matches()) {
			
				currentChain=cm.group(2).charAt(0);
				
				chains.add(currentChain);
				
			} else if (line.matches(dbRefLinePattern)) {
				
				// DBRefs don't indicate the current chain
				continue;
				
			} else if(vm.matches()) {
						
				currentChain = vm.group(3).charAt(0);
				
				chains.add(currentChain);
				
			} else if (pm.matches()) {
				
				currentChain = pm.group(3).charAt(0);
				
				chains.add(currentChain);
						
			} else if (line.matches(equalchainsLinePattern)) {

				final String[] s = line.trim().split("\\s+");
				final char sourceChain = s[3].charAt(0);
				
				// Chain listing starts at the 11th word in the expression.
				for(int i=11; i<s.length; i++) {
					
					if(s.equals("and") ) continue; // 'and' is not a chain-ID, it's an interjection
					
					final char destChain=s[i].charAt(0); // Take the first character in the word, thus not the commas!
					
					chains.add(destChain);
				}
			}
		}
			
		reader.close();
		
		return chains;
	}

	private static void goThroughStockholm(InputStream stockholmIn,
			
				final AlignmentSet alignments, // output
				final ResidueInfoSet residueInfoSet, // output
				char requestedChain // '*' for all chains
				
				) throws Exception {
		
		final StringBuilder pdbID = new StringBuilder();
		
		final boolean takeAllChains = ( requestedChain == '*' );
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stockholmIn));

		char currentChain='A';
		final Pattern	vp = Pattern.compile(variabilityLinePattern),
						pp = Pattern.compile(profileLinePattern),
						sp = Pattern.compile(seqLinePattern),
						cp = Pattern.compile(chainLinePattern);

		String line; int linenr=0;
		while((line=reader.readLine())!=null) {
			linenr++;

			Matcher vm = vp.matcher(line),
					pm = pp.matcher(line),
					sm = sp.matcher(line),
					cm = cp.matcher(line);
			
			if(line.trim().equals("//")) { // indicates the end of the current chain
				
				if(!takeAllChains && currentChain==requestedChain) {
					
					break; // end of the requested chain
				}
				
				currentChain = ' ';
			}
			else if(line.matches(pdbIDLinePattern)) {
				
				final String[] s = line.trim().split("\\s+");
				
				pdbID.replace(0, pdbID.length(), s[s.length-1] );
				
			} else if (cm.matches()) {
			
				final String ac=cm.group(1);
				currentChain=cm.group(2).charAt(0);
	
				if( !pdbID.toString().equalsIgnoreCase(ac)) {
					throw new Exception("line "+linenr+": got id "+ac+", but expected: "+pdbID);
				}
				
				pdbID.replace(0, pdbID.length(), ac );
				
				if(!takeAllChains && currentChain!=requestedChain) {
					continue;
				}
				
				alignments.addChain(currentChain);
				
			} else if (line.matches(dbRefLinePattern)) {

				// DBRefs don't indicate the current chain
				continue;
				
			} else if(vm.matches()) {
				
				char chain = vm.group(3).charAt(0), AA = vm.group(4).charAt(0);
				String pdbno = vm.group(2).trim();
				int seqno	= Integer.parseInt(vm.group(1).trim()),
					var		= Integer.parseInt(vm.group(10).trim());
				
				if(!takeAllChains && chain!=requestedChain) {
					continue;
				}
				
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
				
				if(!takeAllChains && chain!=requestedChain) {
					continue;
				}
				
				ResidueInfo res = residueInfoSet.getResidue(chain, seqno);
				
				res.setEntropy(entropy);
				res.setRelent(relent);
				res.setWeight(weight);
				
			} else if (line.matches(equalchainsLinePattern)) {
			// these lines define references of one chain to the other

				final String[] s = line.trim().split("\\s+");
				final char sourceChain = s[3].charAt(0);
				
				// Chain listing starts at the 11th word in the expression.
				for(int i=11; i<s.length; i++) {
					
					if(s.equals("and") ) continue; // 'and' is not a chain-ID, it's an interjection
					
					final char destChain=s[i].charAt(0); // Take the first character in the word, thus not the commas!

					if(!takeAllChains) {
						
						if(destChain==requestedChain) {
							
							// means we must parse this chain instead
							requestedChain = sourceChain;
							alignments.addChain(sourceChain);
							
						} else continue;
					}
					
					residueInfoSet.addChainReference(sourceChain,destChain);
					alignments.addChainReference(sourceChain,destChain);
				}
			}
			else if( (takeAllChains || currentChain==requestedChain )
				&& sm.matches()) {
				
				final String label = sm.group(1), seq = sm.group(2);

				alignments.addToSeq(currentChain,label,seq);
			}
		}
		
		reader.close();
	}
	
	private static String getAlignedPDBSeq(Alignment alignment) {
		
		return alignment.getAlignedSeq(alignment.getLabels().get(0));
	}
	
	private static ResidueInfo getResidueInfoFor(
			Alignment alignment, char chainID,
			ResidueInfoSet residueInfoSet, int columnIndex) {
		
		String alignedPDBSeq = getAlignedPDBSeq(alignment);
		
		char aa = alignedPDBSeq.charAt(columnIndex);
		if(Character.isLetter(aa)) {

			int residueInfoIndex = alignedPDBSeq.substring(0, columnIndex).replace(".", "").length();
			
			return residueInfoSet.getResidueFromOrder(chainID, residueInfoIndex);
		}
		else return null; // If it's a gap
	}

	private static String getPDBRepresentation(PDBResidueInfo pdbResInfo) {
		
		String pdbno = pdbResInfo.getResidueNumber();
		
		char finalPDBnoChar = pdbno.charAt(pdbno.length()-1);
		if(Character.isLetter(finalPDBnoChar)) {
			// there's an insertion code, convert it's notation to jmol syntax
			
			pdbno = pdbno.substring(0,pdbno.length()-1)+"^"+finalPDBnoChar;
		}
		
		return String.format("[%s]%s:%c",
								pdbResInfo.getResidueName(),
								pdbno,
								pdbResInfo.getChain() );
	}
	
	private static String getPDBRepresentation(Alignment alignment, char chainID,
			ResidueInfoSet residueInfoSet, List<PDBResidueInfo> pdbResidues, int columnIndex) {

		String alignedPDBSeq = getAlignedPDBSeq(alignment);
		ResidueInfo res = getResidueInfoFor(alignment, chainID, residueInfoSet, columnIndex);
		if(res!=null) {
			
			String pdbno = res.getPdbNumber();
			
			char finalPDBnoChar = pdbno.charAt(pdbno.length()-1);
			if(Character.isLetter(finalPDBnoChar)) {
				// there's an insertion code, convert it's notation to jmol syntax
				
				pdbno = pdbno.substring(0,pdbno.length()-1)+"^"+finalPDBnoChar;
			}
			
			return String.format("[%s]%s:%c",AminoAcid.aa1to3(alignedPDBSeq.charAt(columnIndex)),pdbno,alignment.getChainID() );
		}
		else return ""; // If it's a gap
	}

	
	private static TableData getTable(
			AlignmentSet alignments,
			Map<Character,Map<String,PDBResidueInfo>> pdbResidues, 
			ResidueInfoSet residueInfos, char chainID) throws Exception {
		
		Alignment alignment = alignments.getAlignment(chainID);
		
		MutationDataObject mutationData = Calculator.generateCorrelatedMutationAndEntropyVariabilityData( alignment );
		
		List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
		
		ColumnInfo colResidueNumber = new ColumnInfo();
		colResidueNumber.setTitle("Residue Number");
		colResidueNumber.setId("residue_number");
		colResidueNumber.setMouseOver(true);
		columns.add(colResidueNumber);
		
		ColumnInfo colPDBResidue = new ColumnInfo();
		colPDBResidue.setTitle("PDB residue");
		colPDBResidue.setId("pdb_residue");
		columns.add(colPDBResidue);
		
		ColumnInfo colEntropy = new ColumnInfo();
		colEntropy.setTitle("Entropy");
		colEntropy.setId("entropy");
		columns.add(colEntropy);
		
		ColumnInfo colVariability = new ColumnInfo();
		colVariability.setTitle("Variability");
		colVariability.setId("variability");
		columns.add(colVariability);
		
		ColumnInfo colWeight = new ColumnInfo();
		colWeight.setTitle("Weight");
		colWeight.setId("weight");
		columns.add(colWeight);
		
		TableData table = new TableData(columns);
		
		for(int i=0; i<alignment.countColumns(); i++) {
			
			ResidueInfo resInfo = getResidueInfoFor(alignment, chainID, residueInfos, i);
			
			List<String> values = new ArrayList<String>(columns.size());
			
			table.setValue(colResidueNumber.getId(), i, new Integer(i + 1));
			table.setValue(colEntropy.getId(), i, new Double(mutationData.getEntropyScores().get(i)));
			table.setValue(colVariability.getId(), i, new Integer(mutationData.getVariabilityScores().get(i)));
						
			if(resInfo != null) {
				
				PDBResidueInfo pdbResInfo = pdbResidues.get(chainID).get(resInfo.getPdbNumber());
				if(pdbResInfo != null) {
					
					table.setValue(colPDBResidue.getId(), i, getPDBRepresentation(pdbResInfo));
				}
				
				table.setValue(colWeight.getId(), i, new Double(resInfo.getWeight()));
			}
		}
		
		return table;
	}
}
