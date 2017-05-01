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
package nl.ru.cmbi.vase.tools.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.ru.cmbi.vase.parse.StockholmParser;
import nl.ru.cmbi.vase.web.WicketApplication;
import nl.ru.cmbi.vase.web.rest.JobRestResource;

public class Utils {
	
	static Logger log = LoggerFactory.getLogger(Utils.class);
	
	public static final String stockholmLocationURL = "ftp://ftp.cmbi.ru.nl/pub/molbio/data/hssp3",
							   rcsbLocationURL = "https://files.rcsb.org/view";
	
	
	public static URL getStockholmURL(String pdbid) throws MalformedURLException {
		
		return new URL(String.format("%s/%s.hssp.bz2", stockholmLocationURL, pdbid.toLowerCase()));
	}
	
	public static URL getRcsbURL(String pdbid) throws MalformedURLException {
		
		return new URL(String.format("%s/%s.pdb", rcsbLocationURL, pdbid));
	}
	
	public static <T extends Number> T max(List<T> numbers) {
		
		T highest = numbers.get(0);
		for(T n : numbers) {
			
			if(n.doubleValue()>highest.doubleValue()) {
				
				highest=n;
			}
		}
		return highest;
	}
	public static <T extends Number> T min(List<T> numbers) {
		
		T lowest = numbers.get(0);
		for(T n : numbers) {
			
			if(n.doubleValue()<lowest.doubleValue()) {
				
				lowest=n;
			}
		}
		return lowest;
	}
	
	public static List<Integer> listRange(int start,int end) {

		List<Integer> is = new ArrayList<Integer>();
		for(int i=start; i<end; i++){
			is.add(i);
		}
		return is;
	}
	
	private static JobRestResource getRest() {
		WicketApplication vase = (WicketApplication)WicketApplication.get();
		
		return (JobRestResource)vase.getRestReference().getResource();
	}
	
	public static String getPDBPath(String structureID) {

		/*
		 * The rcsb website doesn't host the pdb files over http, so
		 * for jsmol to work, VASE must provide the file itself.
		 */
		WicketApplication vase = (WicketApplication)WicketApplication.get();
		
		String path = vase.getServletContext().getContextPath();
		
		return path + "/rest/structure/" + structureID;
	}
	public static InputStream getStockholmInputStream(String structureID)
		throws MalformedURLException, IOException {

		if(structureID.matches(StockholmParser.pdbAcPattern)) {
			
			return new BZip2CompressorInputStream(getStockholmURL(structureID).openStream());
		}
		
		if(Config.hsspPdbCacheEnabled()) {
			
			// Some files that might be there or not:
			File hsspFile = new File(Config.getHSSPCacheDir(), structureID + ".hssp.bz2");
		
			if(hsspFile.isFile()) {
				
				return new BZip2CompressorInputStream(
					new FileInputStream(hsspFile) );
			}
		}
		return new ByteArrayInputStream(getRest().hsspResult(structureID).getBytes());
	}
	public static InputStream getPdbInputStream(String structureID)
			throws MalformedURLException, IOException {

			if(structureID.matches(StockholmParser.pdbAcPattern)) {
				
				return getRcsbURL(structureID).openStream();
			}
			
			if(Config.hsspPdbCacheEnabled()) {
				
				// Some files that might be there or not:
				File pdbFile = new File(Config.getHSSPCacheDir(), structureID + ".pdb.gz");
			
				if(pdbFile.isFile()) {
					
					return new GzipCompressorInputStream(
						new FileInputStream(pdbFile) );
				}
			}
			return new ByteArrayInputStream(getRest().structure(structureID).getBytes());
		}

	public static String getPdbContents(String structureID) throws IOException {
		StringBuffer buf = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(getPdbInputStream(structureID)));
		String line;
		while ((line = reader.readLine()) != null)
		{
			buf.append(line + "\n");
		}
		reader.close();
		
		return buf.toString();
	}
}
