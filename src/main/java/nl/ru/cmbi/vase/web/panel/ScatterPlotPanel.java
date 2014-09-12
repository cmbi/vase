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
package nl.ru.cmbi.vase.web.panel;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Data;

import nl.ru.cmbi.vase.tools.util.Utils;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatterPlotPanel extends Panel {
	
	static Logger log = LoggerFactory.getLogger(ScatterPlotPanel.class);
	
	public class DotComponent extends WebMarkupContainer {

		public DotComponent(String id, double x, double y, int index) {
			super(id);
			
			add(new Label("dot-tooltip",new PropertyModel<String>(this,"tooltip")));
			this.index = index;
			this.x=x;
			this.y=y;
		}
		
		private String tooltip;
		private int index;
		private double x, y;
		
		public double getXValue() {
			return x;
		}
		public double getYValue() {
			return y;
		}
		
		public void setTooltip(String s) {
			this.tooltip = s;
		}

		public int getIndex() {
			
			return this.index;
		}
	}

	protected void onDotCreate(DotComponent dot) {
	}

	protected String xScaleRepresentation(final double x) {

		return String.valueOf(x);
	}
	protected String yScaleRepresentation(final double y) {

		return String.valueOf(y);
	}

	private final double pixTitleSpacing = 40.0; // distance between axis-title and origin

	// space between plot and edge of the svg
	private double	pixMargeLeft = 30.0,
					pixMargeRight=10.0,
					pixMargeUp=20.0,
					pixMargeDown=30.0;

	public ScatterPlotPanel(String id,

			final ScatterPlotOptions options) {

		super(id);

		// Add extra space for titles:
		if(!options.getXAxisTitle().isEmpty())
			pixMargeLeft = 60;
		if(!options.getYAxisTitle().isEmpty())
			pixMargeDown = 50;

		double	plotPixWidth  = options.getImagePixWidth () - pixMargeLeft - pixMargeRight,
				plotPixHeight = options.getImagePixHeight() - pixMargeDown - pixMargeUp   ;

		final double	// data-to-pixels ratio:
						pixXScaling = plotPixWidth/(options.getMaxX()-options.getMinX()),
						pixYScaling = plotPixHeight/(options.getMaxY()-options.getMinY()),

						// position of the origin in pixels:
						pixOriginYPos = options.getImagePixHeight() + options.getMinY() * pixYScaling,
						pixOriginXPos =-options.getMinX() * pixXScaling;

		WebMarkupContainer svg = new WebMarkupContainer("svg");
		svg.add(new AttributeModifier("width" , String.valueOf(options.getImagePixWidth())));
		svg.add(new AttributeModifier("height", String.valueOf(options.getImagePixHeight())));
		add(svg);

        // move everything to the right and up to make it fit in:
        // (locale must be english for the svg transform attributes)
		WebMarkupContainer transformGroup = new WebMarkupContainer("transformgroup");
		transformGroup.add(new AttributeModifier("transform", String.format(Locale.ENGLISH, "translate(%.1f %.1f)", pixMargeLeft, -pixMargeDown)));
		svg.add(transformGroup);

		double	yAxisXpos = pixOriginXPos,
				xAxisYpos = pixOriginYPos;

		Component xAxis = new Label("x-axis");
		xAxis.add(new AttributeModifier("x1","0"));
		xAxis.add(new AttributeModifier("x2", String.valueOf(plotPixWidth)));
		xAxis.add(new AttributeModifier("y1", String.valueOf(xAxisYpos)));
		xAxis.add(new AttributeModifier("y2", String.valueOf(xAxisYpos)));
		transformGroup.add(xAxis);

		WebMarkupContainer xTitle = new WebMarkupContainer("x-title");
		xTitle.add(new Label("x-text",options.getXAxisTitle()));
		xTitle.add(new AttributeModifier("transform",String.format(Locale.ENGLISH, "translate(%.1f %.1f)",pixOriginXPos + pixTitleSpacing, pixOriginYPos + pixTitleSpacing)));
		transformGroup.add(xTitle);

		Component yAxis = new Label("y-axis");
		yAxis.add(new AttributeModifier("x1", String.valueOf(yAxisXpos)));
		yAxis.add(new AttributeModifier("x2", String.valueOf(yAxisXpos)));
		yAxis.add(new AttributeModifier("y1", String.valueOf(pixOriginYPos - plotPixHeight)));
		yAxis.add(new AttributeModifier("y2", String.valueOf(pixOriginYPos)));
		transformGroup.add(yAxis);

		WebMarkupContainer yTitle = new WebMarkupContainer("y-title");
		yTitle.add(new Label("y-text",options.getYAxisTitle()));
		yTitle.add(new AttributeModifier("transform",String.format(Locale.ENGLISH, "translate(%.1f %.1f) rotate(-90)",pixOriginXPos - pixTitleSpacing, pixOriginYPos - pixTitleSpacing)));
		transformGroup.add(yTitle);

		Component curve = new Label("curve");
		
		String curveD="M";
		for(int i=0; i<options.getCurveXs().size(); i++) {
			
			double x=options.getCurveXs().get(i).doubleValue(), y=options.getCurveYs().get(i).doubleValue();
			
			curveD += String.format(Locale.ENGLISH, " %f,%f",
					x * pixXScaling + pixOriginXPos,
					pixOriginYPos - y * pixYScaling
				);
		}
		curve.add(new AttributeModifier("d",curveD));
		transformGroup.add(curve);

		ListView<Integer> dots = new ListView<Integer>("dots",Utils.listRange(0, options.getXValues().size())) {

			@Override
			protected void populateItem(ListItem item) {

				final Integer index = (Integer) item.getModelObject();

				double	x=options.getXValues().get(index).doubleValue(),
						y=options.getYValues().get(index).doubleValue(),

						pxX = x * pixXScaling + pixOriginXPos,
						pxY = pixOriginYPos - y * pixYScaling;

				DotComponent dot=new DotComponent ("dot",x,y,index);

				dot.add(new AttributeModifier("cx",String.valueOf(pxX)));
				dot.add(new AttributeModifier("cy",String.valueOf(pxY)));
				
				dot.setTooltip(String.format("%s:%s, %s:%s", 
					
					options.getXAxisTitle(),
					ScatterPlotPanel.this.xScaleRepresentation(dot.getXValue()),
					options.getYAxisTitle(),
					ScatterPlotPanel.this.yScaleRepresentation(dot.getYValue())
				));
				
				ScatterPlotPanel.this.onDotCreate(dot);
				item.add(dot);
			}
		};
		transformGroup.add(dots);

		ListView<Double> xScales = new ListView<Double>("xscale", getScalePositions(options.getMinX(), options.getMaxX(), options.getXStepSize())) {

			@Override
			protected void populateItem(ListItem item) {

				Double	x=(Double) item.getModelObject(),

						pxX = x * pixXScaling + pixOriginXPos;

				item.add(new AttributeModifier("transform",String.format(Locale.ENGLISH, "translate(%.1f %.1f)",pxX,pixOriginYPos)));

				item.add(new Label("xscale-number",ScatterPlotPanel.this.xScaleRepresentation(x)));
			}
		};
		transformGroup.add(xScales);

		ListView<Double> yScales = new ListView<Double>("yscale", getScalePositions(options.getMinY(), options.getMaxY(), options.getYStepSize())) {

			@Override
			protected void populateItem(ListItem item) {

				Double	y = (Double) item.getModelObject(),

						pxY = pixOriginYPos - y * pixYScaling;

				item.add(new AttributeModifier("transform",String.format(Locale.ENGLISH, "translate(%.1f %.1f)",pixOriginXPos, pxY)));

				item.add(new Label("yscale-number",ScatterPlotPanel.this.yScaleRepresentation(y)));
			}
		};
		transformGroup.add(yScales);
	}

	private static List<Double> getScalePositions(double start, double end, double step) {
		
		double pos=start;
		List<Double> positions = new ArrayList<Double>();
		positions.add(start);
		while( ( pos + step ) < end ) {

			positions.add( pos + step );
			pos += step;
		}
		return positions;
	}
}
