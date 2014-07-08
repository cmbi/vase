package nl.ru.cmbi.vase.web.panel.align;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.ru.cmbi.vase.data.Alignment;
import nl.ru.cmbi.vase.data.ResidueInfo;
import nl.ru.cmbi.vase.data.TableData;
import nl.ru.cmbi.vase.data.TableData.ColumnInfo;
import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.web.Utils;

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

public class AlignmentTablePanel extends Panel {
	
	public static final String tableCellClassPrefix = "tablecell_";
	
	Logger log = LoggerFactory.getLogger(AlignmentTablePanel.class);

	public AlignmentTablePanel(String id,
			final AlignmentDisplayPanel alignmentPanel,
			final VASEDataObject data) {
		super(id);
		
		final TableData tableData = data.getTable();
		final List<ColumnInfo> columnInfos = tableData.getVisibleColumnInfos();
		
		add( new ListView<ColumnInfo>("header-cell",columnInfos ) {

			@Override
			protected void populateItem(ListItem<ColumnInfo> item) {
				
				ColumnInfo ci = item.getModelObject();
				
				WebMarkupContainer headerDiv = new WebMarkupContainer("header-div");
				headerDiv.add( new Label("header-title",ci.getTitle()) );

				Component image = new WebMarkupContainer("header-toggle-image");
				image.add(new AttributeAppender("class", 
						new Model(tableCellClassPrefix + ci.getId())," "));
				headerDiv.add(image);
				
				item.add(headerDiv);
				
				headerDiv.add(new AttributeModifier("onclick",
					String.format("orderTableBy('%s');", ci.getId()))
				);
			}
		});
		
		add(new ListView("rows",Utils.listRange(0,tableData.getNumberOfRows())){

			@Override
			protected void populateItem(ListItem item) {
				
				final Integer	rowIndex = (Integer) item.getModelObject(),
								residueNumber = tableData.getResidueNumber(rowIndex);
						
				item.add(new AttributeModifier("onclick",String.format("toggleColumn('%s');",
						alignmentPanel.getResidueNumberClassRepresentation(residueNumber))));
				
				item.add(new AttributeAppender("class",
						new Model( 
							alignmentPanel.getColumnClassRepresentation(residueNumber) )
						," "));
				
				item.add(new ListView<ColumnInfo>("cells", columnInfos){
					
					@Override
					protected void populateItem(ListItem<ColumnInfo> item) {
						
						ColumnInfo ci = item.getModelObject();
						String value = tableData.getValueAsString(ci.getId(), rowIndex);
						
						Component cellText = new Label("cell-text", value);
						
						cellText.add(new AttributeAppender("class", 
							new Model(tableCellClassPrefix + ci.getId())," "));
						
						
						item.add(cellText);
					}
				});
			}
			
		});
	}
}
