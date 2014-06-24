package nl.ru.cmbi.vase.web.panel.align;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.ru.cmbi.vase.data.Alignment;
import nl.ru.cmbi.vase.data.ResidueInfoSet;
import nl.ru.cmbi.vase.web.Utils;
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

public class StructurePanel extends Panel {
	
	public StructurePanel(String id, final String structurePath) {
		
		super(id);
		
		String urlString = RequestCycle.get().urlFor(HomePage.class, new PageParameters()).toString();
		
		//WebMarkupContainer applet = new WebMarkupContainer("applet");
		//applet.add(new AttributeModifier("codebase",urlString));
		//add(applet);
		
		Component script = new Component("init-script"){

			@Override
			protected void onRender() {
				
				JavaScriptUtils.writeOpenTag(getResponse());
				
				getResponse().write("var jmolInit=\"");
				
				getResponse().write("background white;");
				getResponse().write("load "+structurePath+";");
				getResponse().write("select *;");
				getResponse().write("color atoms lightgrey structure;");
				
				getResponse().write("\";\n");

				JavaScriptUtils.writeCloseTag(getResponse());
			}
		};
		add(script);
	}
}
