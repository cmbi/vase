package nl.ru.cmbi.vase.web.page;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import nl.ru.cmbi.vase.analysis.Calculator;
import nl.ru.cmbi.vase.analysis.MutationDataObject;
import nl.ru.cmbi.vase.data.Alignment;
import nl.ru.cmbi.vase.data.AlignmentSet;
import nl.ru.cmbi.vase.data.ResidueInfo;
import nl.ru.cmbi.vase.data.ResidueInfoSet;
import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.data.VASEDataObject.PlotDescription;
import nl.ru.cmbi.vase.parse.StockholmParser;
import nl.ru.cmbi.vase.parse.VASEXMLParser;
import nl.ru.cmbi.vase.tools.util.Config;
import nl.ru.cmbi.vase.tools.util.Utils;
import nl.ru.cmbi.vase.web.panel.align.AlignmentDisplayPanel;
import nl.ru.cmbi.vase.web.panel.align.AlignmentLinkedPlotPanel;
import nl.ru.cmbi.vase.web.panel.align.AlignmentTablePanel;
import nl.ru.cmbi.vase.web.panel.align.StructurePanel;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.util.string.JavaScriptUtils;

public class AlignmentPage extends BasePage {
	
	private static final Logger log = LoggerFactory.getLogger(AlignmentPage.class);
	
	private String PDBID;
	private Character chainID=null;
	
	private static Calculator calculator = new Calculator();

	public AlignmentPage(final PageParameters parameters) {
		
		StringValue	pdbIDString		= parameters.get(0),
					chainIDString	= parameters.get(1);
		
		Map<Character,VASEDataObject> dataPerChain;
		
		if(pdbIDString==null || pdbIDString.isNull() || pdbIDString.isEmpty()) {
					
			throw new RestartResponseAtInterceptPageException(new ErrorPage("pdb id missing"));
			
		} else {
		
			PDBID = pdbIDString.toString().toLowerCase();
	
			try {
				
				// Some files that might be there or not:
				File	xmlFile = new File(Config.getCacheDir(), PDBID+".xml.gz"),
						hsspFile = new File(Config.getHSSPCacheDir(), PDBID+".hssp.bz2"),
						pdbFile = new File(Config.getHSSPCacheDir(), PDBID+".pdb.gz");
				if(xmlFile.isFile()) {
					
					dataPerChain = new HashMap<Character,VASEDataObject>(); // just a wrapper
					
					dataPerChain.put('-', 
						VASEXMLParser.parse( new GZIPInputStream( new FileInputStream(xmlFile) ) )
					);
				}
				else if(hsspFile.isFile() && pdbFile.isFile())
				{					
					dataPerChain = StockholmParser.parseStockHolm (
						new BZip2CompressorInputStream(new FileInputStream(hsspFile)),
								new GZIPInputStream(new FileInputStream(pdbFile)) );
				}
				else {
				
					URL stockholmURL = Utils.getStockholmURL(PDBID), pdbURL = Utils.getRcsbURL(PDBID);
				
					dataPerChain  = StockholmParser.parseStockHolm ( 
						new BZip2CompressorInputStream(stockholmURL.openStream()), pdbURL);
				}
			} catch (Exception e) {
							
				log.error("Error getting alignments for " + pdbIDString + " : " + e.getMessage(),e);
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage("Error getting alignments for " + pdbIDString + " : " + e.toString()));
			}
			
			if(chainIDString!=null && !chainIDString.isNull() && !chainIDString.isEmpty()) {
				
				chainID = chainIDString.toChar();
			}
			else if (dataPerChain.size()==1) {
				
				List<Character> chainIDs = new ArrayList<Character>();
				chainIDs.addAll(dataPerChain.keySet());
				
				chainID = chainIDs.get(0);
			}
			else if(chainID==null) {
				
				String chains = "Select one of the following chains: ";
				for(char chain : dataPerChain.keySet()) {
					chains += " "+chain;
				}
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage(chains));
			}
			else if(!dataPerChain.containsKey(chainID)) {
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage("No such chain: "+chainID));
			}
		}
		

//		####################################################################
		
		String title=String.format("Alignment of %s chain %c", PDBID,chainID);
		
		setPageTitle(title);
		add(new Label("page-header",title));
		
		final VASEDataObject data = dataPerChain.get(chainID);
		
		final AlignmentDisplayPanel alignmentPanel = new AlignmentDisplayPanel("alignment",data);
		add(alignmentPanel);
		
		add(new JSDefinitions("js-definitions", alignmentPanel));
		
		final RepeatingView tabs = new RepeatingView("tabs");
		
		addToTabs( "data-table", "Table", 
				new AlignmentTablePanel("data-table",alignmentPanel,data),
				tabs);
		
		add(new ListView<PlotDescription>("plots",data.getPlots()){
			
			private int plotCount=0;

			@Override
			protected void populateItem(ListItem<PlotDescription> item) {
				
				PlotDescription pd = item.getModelObject();
				plotCount++;
				
				Component plot = new AlignmentLinkedPlotPanel("plot",alignmentPanel,pd,data.getTable());
				
				String id="plot"+plotCount; // must be unique
				
				addToTabs( id, pd.getPlotTitle(), plot, tabs);
				
				item.add(plot);
			}
		});
		
		add(tabs);
		
		String structurePath =
			RequestCycle.get().urlFor(HomePage.class, new PageParameters()).toString()
			+ "rest/structure/" + PDBID;
		
		if(data.getPdbURL()!=null) {
			
			structurePath = data.getPdbURL().toString();
		}
		
		add(new StructurePanel("structure",structurePath));
	}
	
	private Map<String,String> tabTitles = new LinkedHashMap<String,String>();
	
	private IModel<List<String>> tabidsModel = new LoadableDetachableModel<List<String>>() {

		@Override
		protected List<String> load() {
			
			return new ArrayList<String>(tabTitles.keySet());
		}
	};
	
	private void addToTabs(String id, String tabTitle, Component component, RepeatingView tabs) {

		// first added becomes the active tab
		String display="none";
		boolean active=false;
		if(tabTitles.size()==0) {
			
			active=true;
			display="block";
		}
		
		tabTitles.put(id,tabTitle);
		component.add(new AttributeModifier("id",id));
		component.add(new AttributeAppender("style",new Model("display:"+display),";"));
		
		add(component);
		
		tabs.add(new TabFragment(tabs.newChildId(),id,active));
	}
	
	public class TabFragment extends Fragment {
		
		public TabFragment(String id, String tabid, boolean startActive) {
			
			super(id, "tab-fragment", AlignmentPage.this);
			
			add(new AttributeModifier("id","switch-"+tabid));
			
			WebMarkupContainer link = new WebMarkupContainer("tab-link");
			
			link.add(new AttributeModifier("onclick",String.format("switchTabVisibility('%s');", tabid)));
			link.add(new Label("tab-title",tabTitles.get(tabid)));
			
			if(startActive) {
				
				add(new AttributeAppender("class",new Model("active"), " "));
			}
			
			add(link);
		}
	}
	
	private class JSDefinitions extends Component {
		
		private AlignmentDisplayPanel alignmentPanel;
		
		public JSDefinitions(String id, AlignmentDisplayPanel a) {
			super(id);
			
			alignmentPanel = a;
		}

		@Override
		protected void onRender() {
			
			JavaScriptUtils.writeOpenTag(getResponse());
			
			//////////////////////////////////////////////////
			
			getResponse().write("var tabids=[");
			
			for(String tabid : AlignmentPage.this.tabTitles.keySet()) {
				
				getResponse().write(String.format("\'%s\',", tabid));
			}
			getResponse().write("];\n");

			String urlString = RequestCycle.get().urlFor(HomePage.class, new PageParameters()).toString();
			getResponse().write(String.format("var baseURL='%s';\n", urlString));
			
			getResponse().write("var pPDBresclass=/"
					+ AlignmentDisplayPanel.pdbResiduePrefix+"([^\\s]+)/;\n");
			
			getResponse().write("var palignmentposclass=/"
					+ AlignmentDisplayPanel.alignmentPositionPrefix+"([0-9]+)/;\n");
			
			getResponse().write("var pTableCellClass=/"
					+ AlignmentTablePanel.tableCellClassPrefix+"([^\\s]+)/;\n");

			getResponse().write("var alignment_columnheader_classname='"
					+ AlignmentDisplayPanel.columnHeaderClassname+"';\n");
					
			JavaScriptUtils.writeCloseTag(getResponse());
		}
		
	}
}
