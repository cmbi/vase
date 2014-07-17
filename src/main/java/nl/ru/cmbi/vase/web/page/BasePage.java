package nl.ru.cmbi.vase.web.page;

import nl.ru.cmbi.vase.tools.util.Config;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

public class BasePage extends WebPage {
	
	private String pageTitle = "";
	
	public void setPageTitle(String title) {
		
		this.pageTitle = title;
	}

	public BasePage () {
		
		WebMarkupContainer navBar = new WebMarkupContainer("navbar");
		
		if(Config.isXmlOnly()) {
			
			navBar.add(new AttributeAppender("style",new Model("display:none"),";"));
		}
		
		navBar.add(new BookmarkablePageLink("home-link",HomePage.class));
		add(navBar);
		
		add(new Label("page-title",new PropertyModel<String>(this,"pageTitle")));
	}
}
