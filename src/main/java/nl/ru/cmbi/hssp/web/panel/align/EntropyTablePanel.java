package nl.ru.cmbi.hssp.web.panel.align;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.ru.cmbi.hssp.data.Alignment;
import nl.ru.cmbi.hssp.data.ResidueInfo;
import nl.ru.cmbi.hssp.web.Utils;
import nl.ru.cmbi.hssp.web.panel.ScrollableTableRowPanel;

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
		
		final int cellWidth = 160;
		
		Component header = new ScrollableTableRowPanel("header",
				Arrays.asList(new String[] {
						"Alignment Position",
						"PDB Residue",
						"Entropy",
						"Variability",
						"Weight"
						}));
		
		header.add(new AttributeModifier("class","entropytable_header"));
		
		add(header);
		
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
				
				List<String> cellContents = new ArrayList<String>();
				
				cellContents.add(String.valueOf(index+1));
				cellContents.add(alignmentPanel.getPDBRepresentation(index));
				cellContents.add(String.format("%.2f",entropy));
				cellContents.add(String.valueOf(variability));
				cellContents.add(weightString);
				
				Component row = new ScrollableTableRowPanel("row",cellContents);
				
				item.add(row);
				
				row.add(new AttributeModifier("onclick",String.format("toggleColumn('%s');",
						alignmentPanel.getPositionClassRepresentation(index))));
				
				row.add(new AttributeModifier("class",
						"entropy_row " + alignmentPanel.getColumnClassRepresentation(index)));

			}
			
		});
	}
}
