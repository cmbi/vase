package nl.ru.cmbi.vase.web.page;

import org.apache.wicket.Component;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

public class InputPage extends BasePage {

	public InputPage() {
		
		setPageTitle("Submit a Structure");
		
		add(new JSDefinitions("js-definitions"));
	}
	
	private class JSDefinitions extends Component {


		public JSDefinitions(String id) {
			super(id);
		}

		@Override
		protected void onRender() {
			
			JavaScriptUtils.writeOpenTag(getResponse());

			String urlString = RequestCycle.get().getUrlRenderer().renderFullUrl(
				Url.parse(
					RequestCycle.get().urlFor(HomePage.class, null) ));
			
			getResponse().write(String.format("var baseURL='%s';\n", urlString));
			
			JavaScriptUtils.writeCloseTag(getResponse());
		}
	}
}
