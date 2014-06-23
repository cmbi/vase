package nl.ru.cmbi.hssp.web.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScatterPlotOptions implements Serializable {
	
	private String xAxisTitle="",  yAxisTitle="";
	
	private double imagePixWidth, imagePixHeight, //The size of the image in pixels
	
		minX,maxX,minY,maxY, // The range on the X and Y axis respectively
	
		xStepSize, yStepSize; // the step sizes on the scales.
	
	private List<? extends Number> // The values themselves :
		xValues = new ArrayList<Double>(),
		yValues = new ArrayList<Double>();

}
