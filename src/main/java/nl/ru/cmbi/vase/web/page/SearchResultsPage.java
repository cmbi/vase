package nl.ru.cmbi.vase.web.page;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nl.ru.cmbi.vase.parse.StockholmParser;
import nl.ru.cmbi.vase.tools.util.Config;
import nl.ru.cmbi.vase.tools.util.Utils;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchResultsPage extends BasePage {
	
	private static Logger log = LoggerFactory.getLogger(SearchResultsPage.class);

	public SearchResultsPage(final PageParameters parameters) {
		
		String structureID = parameters.get("structureID").toString();
		List<SearchResult> results = search(structureID) ;
		
		if(results.size()==0) {
			
			add(new Label("resultslist","No Results Found"));
		}
		else if(results.size()==1) {

			PageParameters pp = new PageParameters();
			pp.set(0, results.get(0).structureID);
			
			if(results.get(0).chainID!=null)
				pp.set(1, results.get(0).chainID.toString());
			
			setResponsePage(AlignmentPage.class, pp);
		}
		else { // more than 1
						
			add(new ResultFragment("resultslist",results));
		}
	}
	
	private static class SearchResult {
		
		public String structureID;
		public Character chainID;
		
		public SearchResult(String structureID, Character c) {
			
			this.structureID = structureID;
			this.chainID = c;
		}

		public SearchResult(String structureID) {
			
			this.structureID = structureID;
			this.chainID = null;
		}
	}

	private static List<SearchResult> search(String structureID) {

		List<SearchResult> results = new ArrayList<SearchResult> ();
		
		File xmlFile = new File(Config.getCacheDir(), structureID+".xml.gz");
		if(xmlFile.isFile()) {
			
			results.add( new SearchResult (structureID) );
			
		} else { // treat it as a pdb id
			
			try {
				URL stockholmURL = Utils.getStockholmURL(structureID);
				
				for(Character chain : StockholmParser.listChainsInStockholm(new BZip2CompressorInputStream( stockholmURL.openStream()))) {
					
					results.add( new SearchResult(structureID,chain) );
				}
				
			} catch (IOException e) {
				
				log.error(e.getMessage(),e);
			}
		}
		
		return results;
	}
	
	public class ResultFragment extends Fragment {
		public ResultFragment(final String id, final List<SearchResult> results) {
			super(id, "resultfragment", SearchResultsPage.this);
			
			ListView<SearchResult> lv = new ListView<SearchResult>("results", results) {
				@Override
				protected void populateItem(final ListItem<SearchResult> item) {
					
					SearchResult result = item.getModelObject();
					item.add(new Label("structureid", result.structureID));
					item.add(new Label("chain",result.chainID.toString()));
					
					item.add(new AttributeModifier("onclick",
						"location.href='../align/"+result.structureID+"/"+result.chainID.toString()+"';"));
				}
			};
			add(lv);
		}
	}
}
