package nl.ru.cmbi.hssp.web.panel.align;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.ru.cmbi.hssp.data.Alignment;
import nl.ru.cmbi.hssp.data.ResidueInfo;
import nl.ru.cmbi.hssp.web.Utils;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntropyTablePanel extends Panel {
	
	Logger log = LoggerFactory.getLogger(EntropyTablePanel.class);

	public EntropyTablePanel(String id,
			final AlignmentPanel alignmentPanel,
			final List<Double> entropyValues,
			final List<Integer> variabilityValues) {
		super(id);
		
		add( new ListView<String>("header-cell",Arrays.asList(new String[] {
						"Alignment Position",
						"PDB Residue",
						"Entropy",
						"Variability",
						"Weight"
						}) ) {

			@Override
			protected void populateItem(ListItem<String> item) {
				
				item.add(new Label("header-cell-text",item.getModelObject()));
			}
		});
		
		add(new ListView("rows",Utils.listRange(0,alignmentPanel.getAlignment().countColumns())){

			@Override
			protected void populateItem(ListItem item) {
				
				final Integer index = (Integer) item.getModelObject();
				
				ResidueInfo residue = alignmentPanel.getResidueInfoFor(index);
				
				final Double entropy = entropyValues.get(index);
				final Integer variability = variabilityValues.get(index);
				
				String weightString = "";
				if(residue!=null) {
					weightString = String.format("%.2f",residue.getWeight());
				}
				
				final List<String> cellContents = new ArrayList<String>();
				
				cellContents.add(String.valueOf(index+1));
				cellContents.add(alignmentPanel.getPDBRepresentation(index));
				cellContents.add(String.format("%.2f",entropy));
				cellContents.add(String.valueOf(variability));
				cellContents.add(weightString);
						
				item.add(new AttributeModifier("onclick",String.format("toggleColumn('%s');",
						alignmentPanel.getPositionClassRepresentation(index))));
				
				item.add(new AttributeAppender("class",
						new Model( alignmentPanel.getColumnClassRepresentation(index) )
						," "));
				
				item.add(new ListView<String>("cells", cellContents){
					
					@Override
					protected void populateItem(ListItem<String> item) {
						
						item.add(new Label("cell-text",item.getModelObject()));
					}
				});
			}
			
		});
	}
}
