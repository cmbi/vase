package nl.ru.cmbi.vase.data;

import lombok.Data;

@Data
public class PDBResidueInfo {
	
	private char chain;
	
	private String	residueNumber, // includes insertion code
					residueName;
	
	public PDBResidueInfo(char chain, String residueNumber, String residueName) {
		
		this.chain=chain;
		this.residueNumber=residueNumber;
		this.residueName=residueName;
	}
}
