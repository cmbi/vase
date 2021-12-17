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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastaParser {
	
	static final Logger log = LoggerFactory.getLogger(FastaParser.class);
	
	public static void toFasta(Map<String,String> seqs, OutputStream out) throws IOException {
		
		Writer writer = new OutputStreamWriter(out);
		for(String id : seqs.keySet()) {
			
			writer.write(String.format(">%s\n",id));
			
			String seq = seqs.get(id);
			int i=0,j; while(i<seq.length()) {
				
				j=Math.min(i+100, seq.length());

				writer.write(String.format("%s\n",seq.substring(i,j)));
				
				i=j;
			}
		}
		writer.close();
	}

	public static Map<String,String> parseFasta(InputStream in) throws IOException {
		
		Map<String,String> map = new LinkedHashMap<String,String>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line, currentID="";
		while ((line=reader.readLine())!=null) {
			
			line=line.trim();
			if(line.isEmpty()) continue;
			
			if(line.startsWith(">")) {
				
				currentID=line.substring(1);
				map.put(currentID, "");
			}
			else {
				map.put(currentID, map.get(currentID) + line);
			}
		}
		reader.close();
		
		return map;
	}
}
