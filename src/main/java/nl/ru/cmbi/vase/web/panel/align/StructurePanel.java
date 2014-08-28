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
package nl.ru.cmbi.vase.web.panel.align;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.ru.cmbi.vase.data.TableData;
import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.data.stockholm.Alignment;
import nl.ru.cmbi.vase.data.stockholm.ResidueInfoSet;
import nl.ru.cmbi.vase.tools.util.Utils;
import nl.ru.cmbi.vase.web.page.AlignmentPage;
import nl.ru.cmbi.vase.web.page.HomePage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructurePanel extends Panel {
	
	Logger log = LoggerFactory.getLogger(StructurePanel.class);
	
	public StructurePanel(String id, final String structurePath, final VASEDataObject data) {
		
		super(id);
		
		String urlString = RequestCycle.get().urlFor(HomePage.class, new PageParameters()).toString();
		
		//WebMarkupContainer applet = new WebMarkupContainer("applet");
		//applet.add(new AttributeModifier("codebase",urlString));
		//add(applet);
		
		final boolean oneChain = Character.isLetter(data.getAlignment().getChainID()) || Character.isDigit(data.getAlignment().getChainID());
		
		Component script = new Component("init-script"){

			@Override
			protected void onRender() {
				
				JavaScriptUtils.writeOpenTag(getResponse());
				
				getResponse().write("var jmolSelectableAtomColor=\"[126, 193, 255]\";\n");
				
				getResponse().write("var jmolClearColors=\"");
				
				if(oneChain) {
				
					getResponse().write("select :"+data.getAlignment().getChainID()+" and Protein;");
					getResponse().write("color \"+ jmolSelectableAtomColor +\";");
				}
				else {
					
					for(Object res : data.getTable().getColumnValues(TableData.pdbResidueID)) {
						
						String resString = res.toString().trim();
						if (resString.isEmpty()) {
							
							continue;
						}
						
						getResponse().write( "select " + resString + "; color \"+ jmolSelectableAtomColor +\";");
					}
				}
				
				getResponse().write("\";\n");
				
				getResponse().write("var jmolInit=\"");
				
				getResponse().write("background white;");
				getResponse().write("load "+structurePath+";");
				getResponse().write("select *;");
				getResponse().write("color atoms lightgrey structure;");
				
				getResponse().write("\" + jmolClearColors;\n");

				JavaScriptUtils.writeCloseTag(getResponse());
			}
		};
		add(script);
	}
}
