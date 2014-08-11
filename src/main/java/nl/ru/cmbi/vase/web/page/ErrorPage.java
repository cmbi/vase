package nl.ru.cmbi.vase.web.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class ErrorPage extends BasePage {

	public ErrorPage(String message) {
	
		setPageTitle("ERROR");
		add(new Label("message",message));
		
		add(new BookmarkablePageLink("home",this.getApplication().getHomePage()));
	}
}
