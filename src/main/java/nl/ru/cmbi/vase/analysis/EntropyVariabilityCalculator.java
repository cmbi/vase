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

import java.util.Collection;
import java.util.Map;

public class EntropyVariabilityCalculator {

	public int calculateVariability(Map<String, Integer> residueOccurrences) {
		int variability = 0;

		Collection<Integer> numbers = residueOccurrences.values();
		int totalLength = 0;
		for (int i : numbers) {
			totalLength += i;
		}

		for (String residueType : residueOccurrences.keySet()) {
			int numberOfOccurrences = residueOccurrences.get(residueType);
			double relativeOccurrence = numberOfOccurrences
					/ (totalLength * 1.0);
			if (relativeOccurrence > 0.005) {
				variability++;
			}
		}
		return variability;
	}

	public double calculateEntropy(Map<String, Integer> residueOccurrences) {
		double entropy = 0.0;

		Collection<Integer> numbers = residueOccurrences.values();
		int totalLength = 0;
		for (int i : numbers) {
			totalLength += i;
		}

		for (String residueType : residueOccurrences.keySet()) {
			int numberOfOccurrences = residueOccurrences.get(residueType);
			double relativeOccurrence = numberOfOccurrences
					/ (totalLength * 1.0);
			double partialEntropy = relativeOccurrence
					* Math.log(relativeOccurrence);
			entropy += partialEntropy;
		}
		return -1.0 * entropy;
	}

}
