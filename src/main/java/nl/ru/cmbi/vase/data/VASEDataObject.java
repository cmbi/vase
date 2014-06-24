package nl.ru.cmbi.vase.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.dom4j.Element;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class VASEDataObject {
	
	@Data
	public static class PlotDescription {
		
		String	xAxisColumnID,
				yAxisColumnID,
				plotTitle;
	}
	
	@Setter(AccessLevel.NONE)
	private Map<String, String> alignment;

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


	public VASEDataObject(Map<String, String> fastaMap, 
			TableData tableData, String pdbContents) {
		
		this.alignment=fastaMap;
		this.table=tableData;
		this.pdbContents=pdbContents;
	}
	
	public VASEDataObject(Map<String, String> fastaMap, 
			TableData tableData, URL pdbURL) {
		
		this.alignment=fastaMap;
		this.table=tableData;
		this.pdbURL = pdbURL;
	}
}
