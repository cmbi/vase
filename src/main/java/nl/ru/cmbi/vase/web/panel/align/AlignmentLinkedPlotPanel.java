package nl.ru.cmbi.vase.web.panel.align;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.ru.cmbi.vase.analysis.MutationDataObject;
import nl.ru.cmbi.vase.data.TableData;
import nl.ru.cmbi.vase.data.TableData.ColumnInfo;
import nl.ru.cmbi.vase.data.TableData.ColumnDataType;
import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.data.VASEDataObject.PlotDescription;
import nl.ru.cmbi.vase.web.Utils;
import nl.ru.cmbi.vase.web.panel.ScatterPlotOptions;
import nl.ru.cmbi.vase.web.panel.ScatterPlotPanel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

public class AlignmentLinkedPlotPanel extends Panel {

	public AlignmentLinkedPlotPanel(String id,
			final AlignmentPanel alignmentPanel,
			final PlotDescription pd,
			final TableData tableData) {
		
		super(id);
		
		ScatterPlotOptions options = new ScatterPlotOptions();
		
		List<Number>	xValues = new ArrayList<Number>(),
						yValues = new ArrayList<Number>();
		
		final ColumnInfo	xColumnInfo = tableData.getColumnByID( pd.getXAxisColumnID() ),
							yColumnInfo = tableData.getColumnByID( pd.getYAxisColumnID() );
		
		for(int i=0; i<tableData.getNumberOfRows(); i++) {
			
			xValues.add((Number) tableData.getRowValues(i).get( xColumnInfo.getId() ));
			yValues.add((Number) tableData.getRowValues(i).get( yColumnInfo.getId() ));
		}
		
		double	smallestX = Utils.min(xValues).doubleValue(),
				smallestY = Utils.min(yValues).doubleValue();
		
		if(smallestX>0)
			smallestX = 0;
		if(smallestY>0)
			smallestY=0;
		
		options.setMinX(smallestX * 1.1);
		options.setMaxX(Utils.max(xValues).doubleValue() * 1.1); 
		options.setMinY(smallestY * 1.1);
		options.setMaxY(Utils.max(yValues).doubleValue() * 1.1);
				
		options.setXStepSize((options.getMaxX() - options.getMinX())/10);
		options.setYStepSize((options.getMaxY() - options.getMinY())/10);
		
		if(xColumnInfo.getType().equals(ColumnDataType.INTEGER)) {

			options.setXStepSize((int)options.getXStepSize());
		}
		if(yColumnInfo.getType().equals(ColumnDataType.INTEGER)) {

			options.setYStepSize((int)options.getYStepSize());
		}
		
		options.setImagePixWidth(800);
		options.setImagePixHeight(390);
		
		options.setXValues(xValues);
		options.setYValues(yValues);
		
		options.setXAxisTitle(xColumnInfo.getTitle());
		options.setYAxisTitle(yColumnInfo.getTitle());
		
		ScatterPlotPanel scatterPlot = new ScatterPlotPanel("chart",options) 
		{
			protected String xScaleRepresentation(double x) {
				
				if(xColumnInfo.getType().equals(ColumnDataType.INTEGER))
				
					return String.format("%d", (int)x);
				else
					return String.format("%.1f", x);
			}
			
			protected String yScaleRepresentation(double y) {
				
				if(yColumnInfo.getType().equals(ColumnDataType.INTEGER))
				
					return String.format("%d", (int)y);
				else
					return String.format("%.1f", y);
			}
			
			protected Component createDot(final String markupID, final int index) {
				
				Component dot = new Label(markupID);;
				
				dot.add(new AttributeModifier("onclick",
						String.format("toggleColumn('%s');", alignmentPanel.getPositionClassRepresentation(index))));
				
				dot.add(new AttributeAppender("class",new Model(alignmentPanel.getColumnClassRepresentation(index)), " "));
								
				return dot;
			}
		};
		
		add(scatterPlot);
	}
}
