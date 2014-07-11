package nl.ru.cmbi.vase.web.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;

public class BasePage extends WebPage {
	
	private String pageTitle = "";
	
	public void setPageTitle(String title) {
		
		this.pageTitle = title;
	}

	public BasePage () {
		
		add(new Label("page-title",new PropertyModel<String>(this,"pageTitle")));
	}
}
