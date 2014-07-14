package nl.ru.cmbi.vase.web.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

public class BasePage extends WebPage {
	
	private String pageTitle = "";
	
	public void setPageTitle(String title) {
		
		this.pageTitle = title;
	}

	public BasePage () {
		
		add(new BookmarkablePageLink("home-link",HomePage.class));
		
		add(new Label("page-title",new PropertyModel<String>(this,"pageTitle")));
	}
}
