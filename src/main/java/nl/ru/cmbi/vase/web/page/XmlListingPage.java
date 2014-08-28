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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.ru.cmbi.vase.tools.util.Config;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class XmlListingPage extends BasePage {
	
	public XmlListingPage() {
		
		List<File> files = new ArrayList<File>();
		if(Config.cacheEnabled()) {
			
			for(File file : Config.getCacheDir().listFiles()) {
				
				if(file.getName().endsWith(".xml") || file.getName().endsWith(".xml.gz"))
				files.add(file);
			}
		}
		
		add(new ListView<File>("alignments",files){

			@Override
			protected void populateItem(ListItem<File> item) {
				
				File xmlFile = item.getModelObject();
				String	id = xmlFile.getName().split("\\.")[0];
				
				PageParameters pp = new PageParameters();
				pp.set(0, id);
				
				BookmarkablePageLink link = 
					new BookmarkablePageLink("alignment-link",AlignmentPage.class,pp);
				
				link.add(new Label("alignment-id",id));
				
				item.add(link);
			}
		});
	}
}
