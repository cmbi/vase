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
package nl.ru.cmbi.vase.web.panel.align;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.tools.util.AminoAcid;
import nl.ru.cmbi.vase.tools.util.Utils;
import nl.ru.cmbi.vase.data.TableData.ColumnInfo;
import nl.ru.cmbi.vase.data.stockholm.Alignment;
import nl.ru.cmbi.vase.data.stockholm.ResidueInfo;
import nl.ru.cmbi.vase.data.TableData;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignmentDisplayPanel extends Panel {
	
	private static final Logger log = LoggerFactory.getLogger(AlignmentDisplayPanel.class);
	
	private static final int maxResult = 100; 
	
	public String getColumnClassRepresentation(int residueNumber) {
		
		return getResidueNumberClassRepresentation(residueNumber) + " " + getPDBClassRepresentation(residueNumber);
	}
	
	public static String getResidueNumberClassRepresentation(int residueNumber) {
		
		return alignmentPositionPrefix + residueNumber;
	}
	
	public final static String alignmentPositionPrefix = "alignmentpos";
	
	/**
	 * 
	 * @param columnIndex
	 * @return a string containing the pdb residue's chain ID, amino acid, pdb number and maybe an insertion code
	 */
	public String getPDBClassRepresentation(int residueNumber) {
		
		String pdbRepr = data.getTable().getPDBResidueForResidueNumber(residueNumber);
		
		if(pdbRepr.isEmpty()) {
			
			return ""; // If it's a gap
		}
		else return pdbResiduePrefix + pdbRepr;
	}
	
	public static final String pdbResiduePrefix = "pdbres";
	
	private VASEDataObject data;
	
	private Alignment alignment;
	
	public int getNumberOfColumns() {
		
		return alignment.countColumns();
	}
	
	public static final String columnHeaderClassname="columnheader";
	
	public AlignmentDisplayPanel(String id, VASEDataObject data) {
		super(id);
		
		this.data = data;
		this.alignment = data.getAlignment();
		
		add(new ListView("positions",Utils.listRange(1,AlignmentDisplayPanel.this.alignment.countColumns() + 1)){

			@Override
			protected void populateItem(ListItem item) {
				
				final Integer residueNumber = (Integer) item.getModelObject();
				
				Label pos = new Label("position","#");
				item.add(pos);
				
				String columnClass = columnHeaderClassname + " "
					+ AlignmentDisplayPanel.this.getColumnClassRepresentation(residueNumber);
				
				pos.add(new AttributeModifier("class",columnClass));

				TableData tableData = AlignmentDisplayPanel.this.data.getTable();
				int rowIndex = tableData.getRowIndexForResidueNumber(residueNumber);
				String title="";
				for(ColumnInfo ci : tableData.getColumnInfos()) {

					if(ci.isMouseOver()) {
						title += ci.getTitle() + ":"
							+ tableData.getValueAsString(ci.getId(), rowIndex) + "\n" ;
					}
				}

				pos.add(new AttributeModifier("data-placements", "top"));
				pos.add(new AttributeModifier("title",title.trim()));
				pos.add(new AttributeModifier("onclick",
						String.format("toggleColumn('%s');",
								AlignmentDisplayPanel.this.getResidueNumberClassRepresentation(residueNumber))));
			}
		});
		
		int nseqs=this.alignment.countAlignedSeqs();

		add(new ListView("labels",Utils.listRange(0,nseqs)) {

			@Override
			protected void populateItem(ListItem item) {
				
				final Integer row = (Integer) item.getModelObject();
				
				final String label = AlignmentDisplayPanel.this.alignment.getLabels().get(row);
								
				VASEDataObject data = AlignmentDisplayPanel.this.data;

				String ref="";
				if(data.getSequenceReferenceURLs().containsKey(label))
					ref=data.getSequenceReferenceURLs().get(label).toString();
								
				if( !ref.isEmpty() && !label.isEmpty() )
					item.add(new LabelFragment("label",label,ref));
				else
					item.add(new Label("label",label.isEmpty()?"&nbsp":label).setEscapeModelStrings(false) );
			}
		});
		
		add(new ListView("seqs",Utils.listRange(0,nseqs)) {

			@Override
			protected void populateItem(ListItem item) {
				
				final Integer row = (Integer) item.getModelObject();
				
				final String	label	= AlignmentDisplayPanel.this.alignment.getLabels().get(row),
								seq		= AlignmentDisplayPanel.this.alignment.getAlignedSeq(label);
				
				String numberstring = ""+row + " ";
				
				Label seqPre = new Label("seq",seq);
				seqPre.add(new AttributeModifier("id",label));
				
				item.add(seqPre);
			}
		});
	}
	
	public class LabelFragment extends Fragment {
		public LabelFragment(final String id, String label, String ref) {
			super(id, "labelfragment", AlignmentDisplayPanel.this);
			
			ExternalLink link = new ExternalLink("ref",ref);;
			link.add(new Label("label",label));
			add(link);
		}
	}
}
