package nl.ru.cmbi.vase.data.stockholm;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ResidueInfo {

	String pdbNumber; // includes insertion code
	char aa; // lowercase for S-S bridges
	
	double entropy, weight;
	int relent, var;
}
