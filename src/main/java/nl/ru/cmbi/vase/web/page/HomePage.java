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
