package nl.ru.cmbi.vase.entropy;

import java.io.Serializable;

public class EntropyStoreObject implements Serializable {
	private String entropyValues;
	private String sequences;

	public String getEntropyValues() {
		return entropyValues;
	}

	public void setEntropyValues(String entropyValues) {
		this.entropyValues = entropyValues;
	}

	public String getSequences() {
		return sequences;
	}

	public void setSequences(String sequences) {
		this.sequences = sequences;
	}
}