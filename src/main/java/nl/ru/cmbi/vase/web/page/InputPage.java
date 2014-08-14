/**
 * Copyright 2014 CMBI (contact: <Coos.Baakman@radboudumc.nl>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
					RequestCycle.get().urlFor( this.getApplication().getHomePage(), null) ));
			
			getResponse().write(String.format("var baseURL='%s';\n", urlString));
			
			JavaScriptUtils.writeCloseTag(getResponse());
		}
	}
}
