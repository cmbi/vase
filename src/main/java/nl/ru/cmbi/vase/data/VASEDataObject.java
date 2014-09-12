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
package nl.ru.cmbi.vase.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import nl.ru.cmbi.vase.data.stockholm.Alignment;

import org.dom4j.Element;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class VASEDataObject {
	
	private String title = "";
	
	@Data
	public static class PlotDescription {
		
		String	xAxisColumnID,
				yAxisColumnID,
				plotTitle,
				
				curve="";
	}
	
	@Setter(AccessLevel.NONE)
	private Alignment alignment;

	@Setter(AccessLevel.NONE)
	private TableData table;

	@Setter(AccessLevel.NONE)
	private String pdbContents = "";
	
	@Setter(AccessLevel.NONE)
	private URL pdbURL = null;

	@Setter(AccessLevel.NONE)
	private List<PlotDescription> plots = new ArrayList<PlotDescription>();
	
	@Setter(AccessLevel.NONE)
	private Map<String, URL> sequenceReferenceURLs = new HashMap<String, URL>();


	public VASEDataObject(Alignment alignment, 
			TableData tableData, String pdbContents) {
		
		this.alignment=alignment;
		this.table=tableData;
		this.pdbContents=pdbContents;
	}
	
	public VASEDataObject(Alignment alignment, 
			TableData tableData, URL pdbURL) {
		
		this.alignment=alignment;
		this.table=tableData;
		this.pdbURL = pdbURL;
	}
}
