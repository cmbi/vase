package nl.ru.cmbi.vase.web.panel.align;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import nl.ru.cmbi.vase.data.Alignment;
import nl.ru.cmbi.vase.data.ResidueInfo;
import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.tools.util.AminoAcid;
import nl.ru.cmbi.vase.web.Utils;

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
	
	/**
	 * 
	 * @param columnIndex
	 * @return a string containing the pdb residue's chain ID, amino acid, pdb number and maybe an insertion code
	 */
	public String getPDBClassRepresentation(int columnIndex) {
		
		String pdbRepr = data.getTable().getPDBResidue(columnIndex);
		
		if(pdbRepr.isEmpty()) {
			
			return ""; // If it's a gap
		}
		else return pdbResiduePrefix + pdbRepr;
	}
	
	public static final String pdbResiduePrefix = "pdbres";
	
	private boolean selectedColumns[];
	
	private VASEDataObject data;
	private Alignment alignment;
	
	public Alignment getAlignment() {
		return new Alignment( data.getAlignment() );
	}
	
	public static final String columnHeaderClassname="columnheader";
	
	public AlignmentPanel(String id, VASEDataObject data) {
		super(id);
		
		this.data = data;
		this.alignment = new Alignment( data.getAlignment() );
		
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
}
