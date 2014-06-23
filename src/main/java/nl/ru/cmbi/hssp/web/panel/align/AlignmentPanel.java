package nl.ru.cmbi.hssp.web.panel.align;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import nl.ru.cmbi.hssp.data.Alignment;
import nl.ru.cmbi.hssp.data.ResidueInfo;
import nl.ru.cmbi.hssp.tools.util.AminoAcid;
import nl.ru.cmbi.hssp.web.Utils;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignmentPanel extends Panel {
	
	private static final Logger log = LoggerFactory.getLogger(AlignmentPanel.class);
	
	private static final int maxResult = 100; 
	
	public String getColumnClassRepresentation(int columnIndex) {
		
		return getPositionClassRepresentation(columnIndex) + " " + getPDBClassRepresentation(columnIndex);
	}
	
	public static String getPositionClassRepresentation(int columnIndex) {
		
		return alignmentPositionPrefix + columnIndex;
	}
	
	public final static String alignmentPositionPrefix = "alignmentpos";
	
	public ResidueInfo getResidueInfoFor(int columnIndex) {
		
		char aa = alignedPDBSeq.charAt(columnIndex);
		if(Character.isLetter(aa)) {

			int seqno = 1 + alignedPDBSeq.substring(0,columnIndex).replace(".", "").length();
			
			return residueInfoMap.get(seqno);
		}
		else return null; // If it's a gap
	}
	
	/**
	 * 
	 * @param columnIndex
	 * @return a string containing the pdb residue's chain ID, amino acid, pdb number and maybe an insertion code
	 */	
	public String getPDBRepresentation(int columnIndex) {
		
		ResidueInfo res = getResidueInfoFor(columnIndex);
		if(res!=null) {
			
			String pdbno = res.getPdbNumber();
			
			char finalPDBnoChar = pdbno.charAt(pdbno.length()-1);
			if(Character.isLetter(finalPDBnoChar)) {
				// there's an insertion code, convert it's notation to jmol syntax
				
				pdbno = pdbno.substring(0,pdbno.length()-1)+"^"+finalPDBnoChar;
			}
			
			return String.format("[%s]%s:%c",AminoAcid.aa1to3(alignedPDBSeq.charAt(columnIndex)),pdbno,alignment.getChainID() );
		}
		else return ""; // If it's a gap
	}
	
	/**
	 * 
	 * @param columnIndex
	 * @return a string containing the pdb residue's chain ID, amino acid, pdb number and maybe an insertion code
	 */
	public String getPDBClassRepresentation(int columnIndex) {
		
		String pdbRepr = getPDBRepresentation(columnIndex);
		
		if(pdbRepr.isEmpty()) {
			
			return ""; // If it's a gap
		}
		else return pdbResiduePrefix + pdbRepr;
	}
	
	public static final String pdbResiduePrefix = "pdbres";
	
	private boolean selectedColumns[];

	private Alignment alignment;
	private Map<Integer, ResidueInfo> residueInfoMap;
	private String alignedPDBSeq;
	
	public Alignment getAlignment() {
		return alignment;
	}
	
	public static final String columnHeaderClassname="columnheader";
	
	public AlignmentPanel(String id, Alignment alignment, Map<Integer, ResidueInfo> residueInfoMap) {
		super(id);
		this.alignment=alignment;
		this.residueInfoMap=residueInfoMap;
		this.alignedPDBSeq = alignment.getAlignedSeq(alignment.getLabels().get(0));
		
		this.selectedColumns=new boolean[alignment.countColumns()];
		for(int i=0;i<alignment.countColumns();i++)
			this.selectedColumns[i]=false;
		
		final String labelFill = "                  ";
		
		add(new ListView("positions",Utils.listRange(0,AlignmentPanel.this.alignment.countColumns())){

			@Override
			protected void populateItem(ListItem item) {
				
				final Integer index = (Integer) item.getModelObject();
				
				Label pos = new Label("position","#");
				item.add(pos);
				
				String columnClass = columnHeaderClassname+" "+AlignmentPanel.this.getColumnClassRepresentation(index);
				
				pos.add(new AttributeModifier("class",columnClass));
				pos.add(new AttributeModifier("title","position "+index));
			}
		});
		
		int nseqs=this.alignment.countAlignedSeqs();
		/*if(nseqs>maxResult)
			nseqs=maxResult;*/
		
		add(new Label("showing",String.format("Showing %d of %d aligned sequences", nseqs, this.alignment.countAlignedSeqs())));
		

		add(new ListView("labels",Utils.listRange(0,nseqs)) {

			@Override
			protected void populateItem(ListItem item) {
				
				final Integer row = (Integer) item.getModelObject();
				
				final String label = AlignmentPanel.this.alignment.getLabels().get(row);
				
				String id = label.split("/")[0], ref=null;
				if(id.length()==4)
					ref="http://www.rcsb.org/pdb/explore/explore.do?structureId="+id;
				else if(id.length()==6)
					ref="http://www.uniprot.org/uniprot/"+id;
				
				item.add(new ExternalLink("ref",ref).add(new Label("label",label)));
			}
		});
		
		add(new ListView("seqs",Utils.listRange(0,nseqs)) {

			@Override
			protected void populateItem(ListItem item) {
				
				final Integer row = (Integer) item.getModelObject();
				
				final String	label	= AlignmentPanel.this.alignment.getLabels().get(row),
								seq		= AlignmentPanel.this.alignment.getAlignedSeq(label);
				
				String numberstring = ""+row + " ";
				
				Label seqPre = new Label("seq",seq);
				seqPre.add(new AttributeModifier("id",label));
				
				item.add(seqPre);
			}
		});
	}
	
	public void clearSelection() {
		
		for(int i=0; i<selectedColumns.length; i++) {
			
			selectedColumns[i]=false;
		}
	}
	
	public boolean columnSelected(int index) {
		
		return selectedColumns[index];
	}

	public void toggleColumn(int index) {
		
		selectedColumns[index]=!selectedColumns[index];
	}
}
