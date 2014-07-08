package nl.ru.cmbi.vase.web.panel.align;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignmentLinkedPlotPanel extends Panel {
	
	static Logger log = LoggerFactory.getLogger(AlignmentLinkedPlotPanel.class);
	
	private static double determineStepSize(double smallestValue, double largestValue) {
		
		double initial = (largestValue - smallestValue) / 10, stepSize = initial, s=1.0 ;
		
		// Round the number to appropriate significance
		if( initial > 1.0 ) {
			
			while ( s < initial ) {
				
				stepSize = s * ((int) (initial / s));
				
				s *= 10.0 ;
			}
		} else {
			
			while ( s > initial ) {
				
				s *= 0.1;
				
				stepSize = s * ((int) (initial / s));
			}
		}
		
		return stepSize;
	}

	public AlignmentLinkedPlotPanel(String id,
			final AlignmentDisplayPanel alignmentPanel,
			final PlotDescription pd,
			final TableData tableData) {
		
		super(id);
		
		ScatterPlotOptions options = new ScatterPlotOptions();
		
		List<Number>	xValues = new ArrayList<Number>(),
						yValues = new ArrayList<Number>();
		
		final ColumnInfo	xColumnInfo = tableData.getColumnByID( pd.getXAxisColumnID() ),
							yColumnInfo = tableData.getColumnByID( pd.getYAxisColumnID() );
		
		final Map<Integer,Integer> dotIndexToResidueNumber
			=new HashMap<Integer,Integer>(); // remembers which residue is associated with each dot
		
		for(int residueNumber=1; residueNumber<=alignmentPanel.getNumberOfColumns(); residueNumber++) {
			
			int rowIndex = tableData.getRowIndexForResidueNumber( residueNumber );
			if(rowIndex==-1) {
				throw new RuntimeException("residue number not found in table: "+residueNumber);
			}
			
			dotIndexToResidueNumber.put(xValues.size(),residueNumber);
			
			xValues.add((Number) tableData.getRowValues(rowIndex).get( xColumnInfo.getId() ));
			yValues.add((Number) tableData.getRowValues(rowIndex).get( yColumnInfo.getId() ));
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
				
		options.setXStepSize(determineStepSize(options.getMinX(),options.getMaxX()));
		options.setYStepSize(determineStepSize(options.getMinY(),options.getMaxY()));
		
		options.setImagePixWidth(800);
		options.setImagePixHeight(380);
		
		options.setXValues(xValues);
		options.setYValues(yValues);
		
		options.setXAxisTitle(xColumnInfo.getTitle());
		options.setYAxisTitle(yColumnInfo.getTitle());
		
		ScatterPlotPanel scatterPlot = new ScatterPlotPanel("chart",options) 
		{
			protected String xScaleRepresentation(double x) {
				
				if(tableData.columnIsOfType(xColumnInfo.getId(),ColumnDataType.INTEGER))
				
					return String.format("%d", (int)x);
				else
					return String.format("%.1f", x);
			}
			
			protected String yScaleRepresentation(double y) {
				
				if(tableData.columnIsOfType(yColumnInfo.getId(),ColumnDataType.INTEGER))
				
					return String.format("%d", (int)y);
				else
					return String.format("%.1f", y);
			}
			
			protected Component createDot(final String markupID, final int index) {
				
				int residueNumber = dotIndexToResidueNumber.get(index);
				Component dot = new Label(markupID);
				
				dot.add(new AttributeModifier("onclick",
						String.format("toggleColumn('%s');", alignmentPanel.getResidueNumberClassRepresentation(residueNumber))));
				
				dot.add(new AttributeAppender("class",new Model(alignmentPanel.getColumnClassRepresentation(residueNumber)), " "));
								
				return dot;
			}
		};
		
		add(scatterPlot);
	}
}
