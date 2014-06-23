package nl.ru.cmbi.hssp.analysis;

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
