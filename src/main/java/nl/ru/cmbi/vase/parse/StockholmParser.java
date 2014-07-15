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
import nl.ru.cmbi.vase.data.Alignment;
import nl.ru.cmbi.vase.data.AlignmentSet;
import nl.ru.cmbi.vase.data.PDBResidueInfo;
import nl.ru.cmbi.vase.data.ResidueInfo;
import nl.ru.cmbi.vase.data.ResidueInfoSet;
import nl.ru.cmbi.vase.data.TableData;
import nl.ru.cmbi.vase.data.TableData.ColumnInfo;
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
	private static final String
		pdbIDPattern = "^#=GF CC PDBID\\s+[\\w\\-]+\\s*$",
		chainPattern = "^#=GF ID\\s+[\\w]{4}\\/[A-Z0-9]\\s*$",
		
		dbRefPattern = "^#=GF CC DBREF\\s+[1-9][A-Z0-9]{3}\\s+([A-Z0-9])\\s+.*$",
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
	
	public static Map<Character,VASEDataObject> parseStockHolm(InputStream stockholmIn, URL pdbURL)
		throws Exception {
		
		AlignmentSet alignments = new AlignmentSet();
		ResidueInfoSet residueInfoSet = new ResidueInfoSet();
		StringBuilder pdbID = new StringBuilder("");
		
		goThroughStockholm(stockholmIn,alignments,residueInfoSet,pdbID);
		
		Map<Character,VASEDataObject> map = new HashMap<Character,VASEDataObject>();
		for(char chainID : alignments.getChainIDs()) {
			
			Alignment alignment = alignments.getAlignment(chainID);
			
			InputStream pdbIn = pdbURL.openStream();
			Map<Character,Map<String,PDBResidueInfo>> pdbResidues = PDBParser.parseResidues(pdbIn);
			pdbIn.close();
			
			VASEDataObject data = new VASEDataObject(
					alignment, 
					getTable(alignments,pdbResidues,residueInfoSet,chainID),
					pdbURL);

			data.setTitle( String.format("Alignment of %s chain %c", pdbID.toString(), chainID) );
			
			String label = alignment.getLabels().get(0);
			
			String pdbURLString = pdbURL.toString().toLowerCase(), pdbid;
			if(pdbURLString.startsWith("http://www.rcsb.org/pdb/files/") && pdbURLString.endsWith(".pdb")) {
				
				pdbid = pdbURLString.split("\\/files\\/")[1].split("\\.")[0];
				pdbURLString = "http://www.rcsb.org/pdb/explore/explore.do?structureId="+pdbid;
			}
			
			data.getSequenceReferenceURLs().put( label, new URL(pdbURLString) );
			
			for( int i=1; i< alignment.getLabels().size(); i++ ) {

				label = alignment.getLabels().get(i);
				String id = label.split("/")[0];
				
				URL url = new URL("http://www.uniprot.org/uniprot/"+id);
				
				data.getSequenceReferenceURLs().put(label, url);
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
	
	public static Set<Character> listChainsInStockholm(InputStream stockholmIn) throws IOException {
		
		Set<Character> chains = new HashSet<Character>();
		
		String pdbID = null;
		char currentChain='A';
		final Pattern	vp = Pattern.compile(variabilityPattern),
						pp = Pattern.compile(profilePattern);

		final BufferedReader reader = new BufferedReader(new InputStreamReader(stockholmIn));
		String line;
		while((line=reader.readLine())!=null) {
			
			log.debug("listchains line "+line);

			Matcher vm = vp.matcher(line),
					pm = pp.matcher(line);
			
			if(line.trim().equals("//")) { // indicates the end of the current chain
				
				currentChain = ' ';
			}
			else if (line.matches(chainPattern)) {
			
				final String[] s = line.trim().split("\\s+");
				final String id=s[s.length-1];
				currentChain=id.charAt(5);
				
				chains.add(currentChain);
				
			} else if (line.matches(dbRefPattern)) {
				
				// DBRefs don't indicate the current chain
				continue;
				
			} else if(vm.matches()) {
						
				currentChain = vm.group(3).charAt(0);
				
				chains.add(currentChain);
				
			} else if (pm.matches()) {
				
				currentChain = pm.group(3).charAt(0);
				
				chains.add(currentChain);
						
			} else if (line.matches(equalchainsPattern)) {

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
				final StringBuilder pdbID // output
				
				) throws Exception {
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stockholmIn));

		char currentChain='A';
		final Pattern	vp = Pattern.compile(variabilityPattern),
						pp = Pattern.compile(profilePattern);

		String line; int linenr=0;
		while((line=reader.readLine())!=null) {
			linenr++;

			Matcher vm = vp.matcher(line),
					pm = pp.matcher(line);
			
			if(line.trim().equals("//")) { // indicates the end of the current chain
				
				currentChain = ' ';
			}
			else if(line.matches(pdbIDPattern)) {
				
				final String[] s = line.trim().split("\\s+");
				
				pdbID.replace(0, pdbID.length(), s[s.length-1] );
				
			} else if (line.matches(chainPattern)) {
			
				final String[] s = line.trim().split("\\s+");
				final String id=s[s.length-1], ac=id.substring(0,4);
				currentChain=id.charAt(5);
	
				if( !pdbID.toString().equalsIgnoreCase(ac)) {
					throw new Exception("line "+linenr+": got id "+ac+", but expected: "+pdbID);
				}
				
				pdbID.replace(0, pdbID.length(), ac );
				
				alignments.addChain(currentChain);
				
			} else if (line.matches(dbRefPattern)) {

				// DBRefs don't indicate the current chain
				continue;
				
			} else if(vm.matches()) {
				
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
					alignments.addChainReference(sourceChain,destChain);
				}
			}
			else if(line.matches(seqPattern)) {

				final String[] s = line.split("\\s+");

				final String key = s[0], seq = s[1];

				alignments.addToSeq(currentChain,key,seq);
			}
		}
		
		reader.close();
	}
	
	private static String getAlignedPDBSeq(Alignment alignment) {
		
		return alignment.getAlignedSeq(alignment.getLabels().get(0));
	}
	
	private static ResidueInfo getResidueInfoFor(
			Alignment alignment, 
			Map<Integer, ResidueInfo> residueInfoMap, int columnIndex) {
		
		String alignedPDBSeq = getAlignedPDBSeq(alignment);
		
		char aa = alignedPDBSeq.charAt(columnIndex);
		if(Character.isLetter(aa)) {

			int seqno = 1 + alignedPDBSeq.substring(0,columnIndex).replace(".", "").length();
			
			return residueInfoMap.get(seqno);
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
	
	private static String getPDBRepresentation(Alignment alignment, 
			Map<Integer, ResidueInfo> residueInfoMap, List<PDBResidueInfo> pdbResidues, int columnIndex) {

		String alignedPDBSeq = getAlignedPDBSeq(alignment);
		ResidueInfo res = getResidueInfoFor(alignment,residueInfoMap,columnIndex);
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
		Map<Integer,ResidueInfo> chainResInfos = residueInfos.getChain(chainID);
		
		for(int i=0; i<alignment.countColumns(); i++) {
			
			ResidueInfo resInfo = getResidueInfoFor(alignment,chainResInfos,i);
			
			List<String> values = new ArrayList<String>(columns.size());
			
			table.setValue(colResidueNumber.getId(), i, new Integer(i+1));
			table.setValue(colEntropy.getId(), i, new Double(mutationData.getEntropyScores().get(i)));
			table.setValue(colVariability.getId(), i, new Integer(mutationData.getVariabilityScores().get(i)));
						
			if(resInfo!=null) {
				
				PDBResidueInfo pdbResInfo = pdbResidues.get(chainID).get(resInfo.getPdbNumber());
				if(pdbResInfo!=null) {
					
					table.setValue(colPDBResidue.getId(), i, getPDBRepresentation(pdbResInfo));
				}
				
				table.setValue(colWeight.getId(), i, new Double(resInfo.getWeight()));
			}
		}
		
		return table;
	}
}
