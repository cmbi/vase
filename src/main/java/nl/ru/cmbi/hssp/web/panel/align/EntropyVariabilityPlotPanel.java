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

public class EntropyVariabilityPlotPanel extends Panel {	

	public EntropyVariabilityPlotPanel(String id,final AlignmentPanel alignmentPanel,MutationDataObject data) {

		super(id);
		
		ScatterPlotOptions options = new ScatterPlotOptions();
		
		options.setMinX(0); options.setMaxX(Utils.max(data.getVariabilityScores())+1.0); // variability ranges from 1 to 20
		options.setMinY(0); options.setMaxY(Utils.max(data.getEntropyScores())+0.2); // entropy won't go under 0
		
		options.setXStepSize(1.0);
		options.setYStepSize(0.5);
		
		options.setImagePixWidth(800);
		options.setImagePixHeight(390);
		
		options.setXValues(data.getVariabilityScores());
		options.setYValues(data.getEntropyScores());
		
		options.setXAxisTitle("variability");
		options.setYAxisTitle("entropy");
		
		ScatterPlotPanel scatterPlot = new ScatterPlotPanel("chart",options) 
		{
			protected String xScaleRepresentation(double x) {

				return String.format("%d", (int)x);
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
