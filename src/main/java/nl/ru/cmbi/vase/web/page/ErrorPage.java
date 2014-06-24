package nl.ru.cmbi.vase.web.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class ErrorPage extends WebPage {

	public ErrorPage(String message) {
	
		add(new Label("message",message));
	}
}
