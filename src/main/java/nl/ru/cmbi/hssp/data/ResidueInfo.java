package nl.ru.cmbi.hssp.data;

import lombok.Data;

@Data
public class ResidueInfo {

	String pdbNumber; // includes insertion code
	char aa; // lowercase for S-S bridges
	
	double entropy, weight;
	int relent, var;
}
