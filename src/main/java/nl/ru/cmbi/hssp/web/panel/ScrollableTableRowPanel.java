package nl.ru.cmbi.hssp.web.panel;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

public class ScrollableTableRowPanel extends Panel {

	public ScrollableTableRowPanel(String id, List<String> cellContents) {
		super(id);
		
		add(new ListView("cell",cellContents){

			@Override
			protected void populateItem(ListItem item) {
				
				String content = (String) item.getModelObject();
				
				Label textComponent = new Label("text",content);
				
				item.add(textComponent);
			}
		});
	}


}
