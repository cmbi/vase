package nl.ru.cmbi.vase.web.page;

import java.io.File;
import java.util.Arrays;

import nl.ru.cmbi.vase.tools.util.Config;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class XmlListingPage extends BasePage {
	
	public XmlListingPage() {
		
		add(new ListView<File>("alignments",
				Arrays.asList( Config.getCacheDir().listFiles() )){

			@Override
			protected void populateItem(ListItem<File> item) {
				
				File xmlFile = item.getModelObject();
				String	id = xmlFile.getName().split("\\.")[0];
				
				PageParameters pp = new PageParameters();
				pp.set(0, id);
				
				BookmarkablePageLink link = 
					new BookmarkablePageLink("alignment-link",AlignmentPage.class,pp);
				
				link.add(new Label("alignment-id",id));
				
				item.add(link);
			}
		});
	}
}
