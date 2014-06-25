package nl.ru.cmbi.vase.parse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.ru.cmbi.vase.data.TableData;
import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.data.TableData.ColumnInfo;
import nl.ru.cmbi.vase.data.VASEDataObject.PlotDescription;

import org.apache.commons.io.IOUtils;
import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VASEXMLParser {
	
	static Logger log = LoggerFactory.getLogger(VASEXMLParser.class);
	
	private static void table2xml(TableData tableData, Element root) {
		
		Element table = root.addElement("data_table");
		
		for(ColumnInfo ci : tableData.getColumnInfos()) {
			
			Element column = table.addElement("column");
			column.addAttribute("id", ci.getId());
			
			if(!ci.getTitle().isEmpty())
				column.addAttribute("title", ci.getTitle());
			
			column.addAttribute("hidden", new Boolean(ci.isHidden()).toString());
			column.addAttribute("mouseover", new Boolean(ci.isMouseOver()).toString());
		}
		
		for(int i=0; i<tableData.getNumberOfRows(); i++) {
			
			Element row = table.addElement("row");
			
			Map<String,Object> rowData = tableData.getRowValues(i);
			
			for(ColumnInfo ci : tableData.getColumnInfos()) {
				
				Element value = row.addElement("value");
				value.addText(rowData.get(ci.getId()).toString());
			}
		}
	}
	
	private static TableData parseTable(Element root) throws Exception {
		
		Element table = root.element("data_table");
		if(table==null) {
			throw new Exception("missting table element");
		}
		List<ColumnInfo> columns=new ArrayList<ColumnInfo>();
		
		for(Element column : (List<Element>)table.elements("column")) {
			
			if(column.attribute("id")==null) {
				throw new Exception("there\'s a column with no id");
			}
			
			ColumnInfo ci = new ColumnInfo();
			ci.setId(column.attribute("id").getValue());
			
			if(column.attribute("title")!=null) {
				ci.setTitle(column.attribute("title").getValue());
			}
			if(column.attribute("hidden")!=null) {
				ci.setHidden(Boolean.parseBoolean(column.attribute("hidden").getValue()));
			}
			if(column.attribute("mouseover")!=null) {
				ci.setMouseOver(Boolean.parseBoolean(column.attribute("mouseover").getValue()));
			}
			columns.add(ci);
		}
		
		TableData tableData = new TableData(columns);
		
		int rowIndex=0;
		for(Element row : (List<Element>)table.elements("row")) {
			
			List<Element> values = row.elements("value");
			if( columns.size() != values.size() ) {
				throw new Exception("the number of value tags must always match the number of columns");
			}
			
			for(int i=0; i<values.size(); i++) {
				
				tableData.setValue(columns.get(i).getId(), rowIndex, values.get(i).getText());
			}
			rowIndex++;
		}
		
		ColumnInfo columnResidueNumber = tableData.getColumnByID(TableData.residueNumberID);
		if(columnResidueNumber==null) {
			
			throw new Exception("missing column: "+TableData.residueNumberID);
		}
		else if(!columnResidueNumber.isNumber()) {

			throw new Exception("not numerical: "+TableData.residueNumberID);
		}
		if(tableData.getColumnByID(TableData.pdbResidueID)==null) {
			
			throw new Exception("missing column: "+TableData.pdbResidueID);
		}
		
		return tableData;
	}
	
	public static void write(VASEDataObject data, OutputStream xmlOut) throws IOException {
		
		DocumentFactory df = DocumentFactory.getInstance();
		
		Document doc = df.createDocument();
		
		Element root = doc.addElement("xml");
		
		Element fasta = root.addElement("fasta");
		ByteArrayOutputStream fastaStream = new ByteArrayOutputStream();
		FastaParser.toFasta(data.getAlignment(),fastaStream);
		
		fasta.add( df.createCDATA ( 
				new String( fastaStream.toByteArray(),StandardCharsets.UTF_8 ) ) );

		Element pdb = root.addElement("pdb");
		if(data.getPdbURL()!=null)
			pdb.addAttribute("url", data.getPdbURL().toString());
		else
			pdb.add( df.createCDATA ( data.getPdbContents() ) );
		
		table2xml(data.getTable(), root);
		
		if(data.getPlots().size()>0) {
			Element plots = root.addElement("plots");
			for(PlotDescription pd : data.getPlots()) {
				
				Element plot = plots.addElement("plot");
				plot.addElement("x").setText(pd.getXAxisColumnID());
				plot.addElement("y").setText(pd.getYAxisColumnID());
				plot.addAttribute("title", pd.getPlotTitle());
			}
		}
		
		XMLWriter writer = new XMLWriter(xmlOut);
		writer.write(doc);
		writer.close();
	}

	public static VASEDataObject parse(InputStream xmlIn) throws Exception {
		
		Document document = (new SAXReader()).read(xmlIn);

		Element root = document.getRootElement();

		Element fasta = root.element("fasta");
		if(fasta==null) {
			throw new Exception("no fasta tag");
		}
		Map<String,String> fastaMap = FastaParser.parseFasta(IOUtils.toInputStream(fasta.getText(), "UTF-8")); 

		Element pdb = root.element("pdb");
		if(pdb==null) {
			throw new Exception("no pdb tag");
		}
		
		TableData tableData = parseTable(root);
		
		VASEDataObject data;
		if(pdb.attribute("url")!=null)
			data = new VASEDataObject(fastaMap,tableData,new URL(pdb.attributeValue("url")));
		else
			data = new VASEDataObject(fastaMap,tableData,pdb.getText());
		
		for(Element sequenceUrl : (List<Element>)root.elements("sequence-url")) {

			URL url = new URL(sequenceUrl.getText());
			if(sequenceUrl.attribute("id")==null) {
				throw new Exception("no id given for "+url.toString());
				
			}
			String id = sequenceUrl.attribute("id").getValue();
			
			if(!fastaMap.containsKey(id)) {
				throw new Exception("no sequence with id "+id+" in fasta");
			}
			data.getSequenceReferenceURLs().put(id, url);
		}

		Element plots = root.element("plots");
		if(plots!=null) {
			
			for(Element plot : (List<Element>)plots.elements("plot")) {
				
				PlotDescription d = new PlotDescription();
				
				if(plot.attribute("title")==null) {
					throw new Exception("evry plot must have a title");
				}
				d.setPlotTitle(plot.attribute("title").getValue());
				
				Element x = plot.element("x"), y = plot.element("y");
				if(x==null || y==null) {
					throw new Exception("every plot must have a column specified for x and y");
				}
				
				for(Element axis : (new Element[] {x,y}) ) {
					
					ColumnInfo column = tableData.getColumnByID(axis.getText());
					if(column==null) {
	
						throw new Exception("There\'s no column with id "+axis.getText()+" (specified in plot)");
					}
					else if(!column.isNumber()) {
	
						throw new Exception("Column with id "+axis.getText()+" cannot be used in plot, since it\'s not numerical");
					}
				}
				d.setXAxisColumnID(x.getText());
				d.setYAxisColumnID(y.getText());
				
				data.getPlots().add(d);
			}
		}
		
		return data;
	}
}
