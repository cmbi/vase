package nl.ru.cmbi.vase.web.page;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends BasePage {
	private static final long serialVersionUID = 1L;

	public HomePage(final PageParameters parameters) {

		add(new BookmarkablePageLink("input-link",InputPage.class));
		
		String exampleID="1CRN";
		PageParameters pp = new PageParameters();
		pp.set(0, exampleID);
		Link exampleLink = new BookmarkablePageLink("example-link",AlignmentPage.class,pp);
		exampleLink.add(new Label("example-id",exampleID));
		add(exampleLink);
    }
}
