package nl.ru.cmbi.hssp.web.page;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.ru.cmbi.hssp.analysis.Calculator;
import nl.ru.cmbi.hssp.analysis.MutationDataObject;
import nl.ru.cmbi.hssp.data.Alignment;
import nl.ru.cmbi.hssp.data.AlignmentSet;
import nl.ru.cmbi.hssp.data.ResidueInfo;
import nl.ru.cmbi.hssp.data.ResidueInfoSet;
import nl.ru.cmbi.hssp.parse.StockholmParser;
import nl.ru.cmbi.hssp.web.panel.align.AlignmentPanel;
import nl.ru.cmbi.hssp.web.panel.align.EntropyPositionPlotPanel;
import nl.ru.cmbi.hssp.web.panel.align.EntropyTablePanel;
import nl.ru.cmbi.hssp.web.panel.align.EntropyVariabilityPlotPanel;
import nl.ru.cmbi.hssp.web.panel.align.StructurePanel;
import nl.ru.cmbi.hssp.web.panel.align.VariabilityPositionPlotPanel;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.wicket.core.util.string.JavaScriptUtils;

public class AlignmentPage extends WebPage {
	
	private static final Logger log = LoggerFactory.getLogger(AlignmentPage.class);

	private AlignmentSet alignments;
	private ResidueInfoSet residueInfo;
	
	private String PDBID;
	private Character chainID=null;
	
	private static Calculator calculator = new Calculator();

	public AlignmentPage(final PageParameters parameters) {
		
		StringValue	pdbIDString		= parameters.get(0),
					chainIDString	= parameters.get(1);
		
		if(pdbIDString==null || pdbIDString.isNull() || pdbIDString.isEmpty()) {
					
			throw new RestartResponseAtInterceptPageException(new ErrorPage("pdb id missing"));
			
		} else {
		
			PDBID = pdbIDString.toString().toLowerCase();
	
			try {
				
				URL url = new URL(String.format("ftp://ftp.cmbi.ru.nl/pub/molbio/data/hssp3/%s.hssp.bz2",PDBID));
				
				alignments = StockholmParser.parseAlignments(new BZip2CompressorInputStream(url.openStream()));
				residueInfo = StockholmParser.parseResidueInfo(new BZip2CompressorInputStream(url.openStream()));
				
			} catch (Exception e) {
							
				log.error("Error getting alignments for " + pdbIDString + " : " + e.getMessage(),e);
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage("Error getting alignments for " + pdbIDString + " : " + e.toString()));
			}
			
			if(chainIDString!=null && !chainIDString.isNull() && !chainIDString.isEmpty()) {
				
				chainID = chainIDString.toChar();
			}
			else if (alignments.getChainIDs().size()==1) {
				
				chainID = alignments.getChainIDs().get(0);
			}
			else if(chainID==null) {
				
				String chains = "Select one of the following chains: ";
				for(char chain : alignments.getChainIDs()) {
					chains += " "+chain;
				}
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage(chains));
			}
			else if(!alignments.hasChain(chainID)) {
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage("No such chain: "+chainID));
			}
		}
		
		final MutationDataObject data = calculator .generateCorrelatedMutationAndEntropyVariabilityData( alignments.getAlignment(chainID) );
		final List<Double> entropyValues = data.getEntropyScores();
		final List<Integer> variabilityValues = data.getVariabilityScores();

//		####################################################################
		
		String title=String.format("Alignment of %s chain %c", PDBID,chainID);
		
		add(new Label("page-title",title));
		add(new Label("page-header",title));
		
		final AlignmentPanel alignmentPanel =
			new AlignmentPanel("alignment",
					alignments.getAlignment(chainID),
					residueInfo.getChain(chainID));
		add(alignmentPanel);
		
		add(new JSDefinitions("js-definitions",
				alignmentPanel));
		
		addToTabs( "entropy-table", "Entropy Table", 
				new EntropyTablePanel("entropy-table",alignmentPanel,entropyValues,variabilityValues));
		
		addToTabs( "ev-plot", "Entropy vs. Variability Plot", 
				new EntropyVariabilityPlotPanel("ev-plot",alignmentPanel,data));
		
		addToTabs( "ep-plot", "Entropy vs. Position Plot", 
				new EntropyPositionPlotPanel("ep-plot",alignmentPanel,data));
		
		addToTabs( "vp-plot", "Variability vs. Position Plot", 
				new VariabilityPositionPlotPanel("vp-plot",alignmentPanel,data));
		
		add(new ListView<String>("tabs", this.tabidsModel){

			@Override
			protected void populateItem(ListItem<String> item) {
				
				String tabid = item.getModelObject();
				
				WebMarkupContainer link = new WebMarkupContainer("tab-link");
				
				link.add(new AttributeModifier("onclick",String.format("switchTabVisibility('%s');", tabid)));
				link.add(new Label("tab-title",tabTitles.get(tabid)));
				
				item.add(link);
			}
		});
		
		add(new StructurePanel("structure",alignmentPanel,
				String.format("http://www.rcsb.org/pdb/files/%s.pdb", PDBID)));
	}
	
	private Map<String,String> tabTitles = new LinkedHashMap<String,String>();
	
	private IModel<List<String>> tabidsModel = new LoadableDetachableModel<List<String>>() {

		@Override
		protected List<String> load() {
			
			return new ArrayList<String>(tabTitles.keySet());
		}
	};
	
	private void addToTabs(String id, String tabTitle, Component component) {

		String display="none";
		if(tabTitles.size()==0)
			display="block";
		
		tabTitles.put(id,tabTitle);
		component.add(new AttributeModifier("id",id));
		component.add(new AttributeModifier("style","display:"+display+";"));
		
		add(component);
	}
	
	private class JSDefinitions extends Component {
		
		private AlignmentPanel alignmentPanel;
		
		public JSDefinitions(String id, AlignmentPanel a) {
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
			
			getResponse().write("var pPDBresclass=/"+AlignmentPanel.pdbResiduePrefix+"([^\\s]+)/;\n");
			
			getResponse().write("var palignmentposclass=/"+AlignmentPanel.alignmentPositionPrefix+"([0-9]+)/;\n");

			getResponse().write("var alignment_columnheader_classname='"+AlignmentPanel.columnHeaderClassname+"';\n");
			
			JavaScriptUtils.writeCloseTag(getResponse());
		}
		
	}
}
