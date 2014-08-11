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
				plotTitle;
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
