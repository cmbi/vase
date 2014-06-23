package nl.ru.cmbi.hssp.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MutationDataObject implements Serializable {
	private List<Double> entropyScores;
	private List<Integer> variabilityScores;

	public List<Integer> getVariabilityScores() {
		return variabilityScores;
	}

	public List<Double> getEntropyScores() {
		return entropyScores;
	}

	public void setEntropyScores(List<Double> entropyScores) {
		this.entropyScores = entropyScores;
	}

	public List<Integer> getVariabiltyScores() {
		return variabilityScores;
	}

	public void setVariabilityScores(List<Integer> variabilityScores) {
		this.variabilityScores = variabilityScores;
	}
}