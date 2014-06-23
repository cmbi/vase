package nl.ru.cmbi.hssp.web.panel.align;

import nl.ru.cmbi.hssp.analysis.MutationDataObject;
import nl.ru.cmbi.hssp.web.Utils;
import nl.ru.cmbi.hssp.web.panel.ScatterPlotOptions;
import nl.ru.cmbi.hssp.web.panel.ScatterPlotPanel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class VariabilityPositionPlotPanel extends Panel {

	public VariabilityPositionPlotPanel(String id,final AlignmentPanel alignmentPanel,MutationDataObject data) {

		super(id);
		
		ScatterPlotOptions options = new ScatterPlotOptions();
		
		options.setMinX(0); options.setMaxX(alignmentPanel.getAlignment().countColumns()+1); 
		options.setMinY(0); options.setMaxY(Utils.max(data.getVariabilityScores())+1.0); // variability ranges from 1 to 20
		
		options.setXStepSize((int)(options.getMaxX()/10));
		options.setYStepSize(1.0);
		
		options.setImagePixWidth(800);
		options.setImagePixHeight(390);
		
		options.setXValues(Utils.listRange(1, alignmentPanel.getAlignment().countColumns()+1));
		options.setYValues(data.getVariabilityScores());
		
		options.setYAxisTitle("variability");
		options.setXAxisTitle("alignment position");
		
		ScatterPlotPanel scatterPlot = new ScatterPlotPanel("chart",options) 
		{
			protected String xScaleRepresentation(double x) {
				
				return String.format("%d", (int)x);
			}
			protected String yScaleRepresentation(double y) {
				
				return String.format("%d", (int)y);
			}
			
			protected Component createDot(final String markupID, final int index) {
				
				Component dot = new Label(markupID);;
				
				dot.add(new AttributeModifier("onclick",
						String.format("toggleColumn('%s');", alignmentPanel.getPositionClassRepresentation(index))));
				
				dot.add(new AttributeModifier("class",alignmentPanel.getColumnClassRepresentation(index)));
				
				dot.add(new AttributeModifier("fill",
					
					new LoadableDetachableModel<String>() {

						@Override
						protected String load() {
							
							if(alignmentPanel.columnSelected(index))
								return "red";
							else
								return "green";
						}
					}
				));
				
				return dot;
			}
		};
		
		add(scatterPlot);
	}
}
