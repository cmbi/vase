package nl.ru.cmbi.vase.web.page;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public HomePage(final PageParameters parameters) {
		super(parameters);

		// TODO Add your page's components here
		
		add(new Link("alignment") {
			@Override
			public void onClick() {

				PageParameters pageParameters = new PageParameters();
				pageParameters.set(0, "1crn");
				pageParameters.set(1, "A");

				setResponsePage(AlignmentPage.class, pageParameters);
			}
		});

    }
}
