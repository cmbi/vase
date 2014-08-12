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
package nl.ru.cmbi.vase.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import nl.ru.cmbi.vase.data.stockholm.Alignment;

public class Calculator {

	/** logger. */
	private static final Logger LOG = Logger.getLogger(Calculator.class);

	public static MutationDataObject generateCorrelatedMutationAndEntropyVariabilityData( Alignment alignment ) {
		
		MutationDataObject cmdo = new MutationDataObject();

		List<Double> entropyScores = new ArrayList<Double>();
		List<Integer> variabilityScores = new ArrayList<Integer>();
		for (int i = 0; i < alignment.countColumns(); i++) {

			Map<Character, Double> residueOccurrences = new HashMap<Character, Double>();
			
			int n=0;
			for (String label : alignment.getLabels()) {
				
				char oneLetterCode = Character.toUpperCase( alignment.getAlignedSeq(label).charAt(i) );
				if( !Character.isLetter(oneLetterCode) ||
					oneLetterCode == 'X' || oneLetterCode== 'Z' || 
					oneLetterCode == 'B' || oneLetterCode== 'J' ||
					oneLetterCode == 'U' || oneLetterCode== 'O' ) {
					
					continue;
				}
				
				if(!residueOccurrences.containsKey(oneLetterCode)) {
					
					residueOccurrences.put(oneLetterCode, 1.0);
				}
				else {
					residueOccurrences.put(oneLetterCode, residueOccurrences.get(oneLetterCode) + 1.0);
				}
				n++;
			}

			double entropyScore = 0.0;
			int variabilityScore = 0;
			for (Character oneLetterCode : residueOccurrences.keySet()) {

				// min 0.0, max 1.0
				double relativeOccurrence = residueOccurrences.get(oneLetterCode) / (1.0 * n );
				
				if(relativeOccurrence > 0.0) {
					entropyScore -= relativeOccurrence * Math.log(relativeOccurrence);
				}
				
				if (relativeOccurrence > 0.005) {
					variabilityScore++;
				}
			}
			entropyScores.add(entropyScore);
			variabilityScores.add(variabilityScore);
			
		}
		
		cmdo.setEntropyScores(entropyScores);
		cmdo.setVariabilityScores(variabilityScores);
		return cmdo;
	}
}
