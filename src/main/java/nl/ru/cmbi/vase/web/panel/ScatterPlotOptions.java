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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScatterPlotOptions implements Serializable {
	
	private String xAxisTitle="",  yAxisTitle="", curveTitle="";
	
	private double imagePixWidth, imagePixHeight, //The size of the image in pixels
	
		minX,maxX,minY,maxY, // The range on the X and Y axis respectively
	
		xStepSize, yStepSize; // the step sizes on the scales.
	
	private List<? extends Number> // The values themselves :
	
		xValues = new ArrayList<Double>(),
		yValues = new ArrayList<Double>(),
		
		// Values for a curve
		CurveXs = new ArrayList<Double>(),
		CurveYs = new ArrayList<Double>();

}
