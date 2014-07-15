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
import nl.ru.cmbi.vase.web.WicketApplication;
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
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.util.string.JavaScriptUtils;

public class AlignmentPage extends BasePage {
	
	private static final Logger log = LoggerFactory.getLogger(AlignmentPage.class);
	
	private String structureID;
	private Character chainID=null;

	public AlignmentPage(final PageParameters parameters) {
		
		StringValue	structureIDString	= parameters.get(0),
					chainIDString		= parameters.get(1);
		
		Map<Character,VASEDataObject> dataPerChain;
		
		if(structureIDString==null || structureIDString.isNull() || structureIDString.isEmpty()) {
					
			throw new RestartResponseAtInterceptPageException(new ErrorPage("structure id missing"));
			
		} else {
		
			structureID = structureIDString.toString().toLowerCase();
	
			try {
				
				// Some files that might be there or not:
				File	xmlFile = new File(Config.getCacheDir(), structureID+".xml.gz"),
						hsspFile = new File(Config.getHSSPCacheDir(), structureID+".hssp.bz2"),
						pdbFile = new File(Config.getHSSPCacheDir(), structureID+".pdb.gz");
				if(xmlFile.isFile()) {
					
					dataPerChain = new HashMap<Character,VASEDataObject>(); // just a wrapper
					
					dataPerChain.put('-', 
						VASEXMLParser.parse( new GZIPInputStream( new FileInputStream(xmlFile) ) )
					);
				}
				else if(hsspFile.isFile() && pdbFile.isFile())
				{					
					String fullPDBURL = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse("../rest/structure/"+structureID));
					
					URL pdbURL = new URL( fullPDBURL );
					
					dataPerChain  = StockholmParser.parseStockHolm ( 
							new BZip2CompressorInputStream(new FileInputStream(hsspFile)), pdbURL);
				}
				else {
				
					URL stockholmURL = Utils.getStockholmURL(structureID), pdbURL = Utils.getRcsbURL(structureID);
				
					dataPerChain  = StockholmParser.parseStockHolm ( 
						new BZip2CompressorInputStream(stockholmURL.openStream()), pdbURL);
				}
			} catch (Exception e) {
							
				log.error("Error getting alignments for " + structureIDString + " : " + e.getMessage(),e);
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage("Error getting alignments for " + structureIDString + " : " + e.toString()));
			}
			
			if(chainIDString!=null && !chainIDString.isNull() && !chainIDString.isEmpty()) {
				
				chainID = chainIDString.toChar();
			}
			else if (dataPerChain.size()==1) {
				
				// no chain id needs to be given, since there's only one
				
				List<Character> chainIDs = new ArrayList<Character>();
				chainIDs.addAll(dataPerChain.keySet());
				
				chainID = chainIDs.get(0);
			}
			else if(chainID==null) {
				
				// Redirect to a page with chain selection
				
				PageParameters params = new PageParameters();
				params.add( SearchResultsPage.parameterName, structureID) ;
				
				setResponsePage(SearchResultsPage.class, params);
				
				return;
			}
			else if(!dataPerChain.containsKey(chainID)) {
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage("No such chain: "+chainID));
			}
		}
		
		this.initPageWith( dataPerChain.get(chainID) );
	}
	
	private void initPageWith(final VASEDataObject data) {
		
		setPageTitle(data.getTitle());
		add(new Label("alignment-header",data.getTitle()));
		
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
			+ "rest/structure/" + structureID;
		
		if(data.getPdbURL()!=null) {
			
			structurePath = data.getPdbURL().toString();
		}
		
		add(new StructurePanel("structure",structurePath));
	}
	
	private List<String> tabids = new ArrayList<String>();
	
	private void addToTabs(String id, String tabTitle, Component component, RepeatingView tabs) {

		// first added becomes the active tab
		String display="none";
		boolean bActive=false;
		if(tabids.size()==0) {
			
			bActive=true;
			display="block";
		}
		
		tabids.add(id);
		component.add(new AttributeModifier("id",id));
		component.add(new AttributeAppender("style",new Model("display:"+display),";"));
		
		add(component);
		
		tabs.add(new TabFragment(tabs.newChildId(),id,tabTitle,bActive));
	}
	
	public class TabFragment extends Fragment {
		
		public TabFragment(String id, String tabid, String title, boolean startActive) {
			
			super(id, "tab-fragment", AlignmentPage.this);
			
			add(new AttributeModifier("id","switch-"+tabid));
			
			WebMarkupContainer link = new WebMarkupContainer("tab-link");
			
			link.add(new AttributeModifier("onclick",String.format("switchTabVisibility('%s');", tabid)));
			link.add(new Label("tab-title",title));
			
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
			
			for(String tabid : AlignmentPage.this.tabids) {
				
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
