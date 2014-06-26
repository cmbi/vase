package nl.ru.cmbi.vase.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

public class TableData {
	
	Logger log = LoggerFactory.getLogger(TableData.class);
	
	public static String residueNumberID="residue_number",
							pdbResidueID="pdb_residue";
	
	/**
	 * 
	 * @return the rowIndex or -1 if the given number is not in the table
	 */
	public int getRowIndexForResidueNumber(int residueNumber) {
		
		return getRowIndex(residueNumberID, new Integer(residueNumber));
	}
	
	public String getPDBResidueForResidueNumber(int residueNumber) {
		
		int rowIndex = getRowIndexForResidueNumber( residueNumber );
		if(rowIndex==-1)
			return "";
		else
			return getPDBResidue( rowIndex );
	}
	
	public int getResidueNumber(int row) {
		
		return (Integer) getRowValues(row).get(residueNumberID);
	}
	
	public String getPDBResidue(int row) {

		return (String) getRowValues(row).get(pdbResidueID);
	}
	
	public enum ColumnDataType { 
		STRING(".*"), DOUBLE("^\\s*\\-?[0-9]+(\\.[0-9]+)?(e\\-?[0-9]+)?\\s*$"), INTEGER("^\\s*\\-?[0-9]+\\s*$") ;
		
		public final String regexp;
		
		public boolean matchesByClass(Object obj) {
			
			switch(this) {
			case INTEGER:
				return Integer.class.isInstance(obj);
			case DOUBLE:
				return Double.class.isInstance(obj);
			default:
				return String.class.isInstance(obj);
			}
		}
		
		public Object fromString(String s) {
			
			switch(this) {
			case INTEGER:
				return Integer.parseInt(s);
			case DOUBLE:
				return Double.parseDouble(s);
			default:
				return new String(s);
			}
		}
		public Object initialValue() {

			switch(this) {
			case INTEGER:
				return new Integer(0);
			case DOUBLE:
				return new Double(0.0);
			default:
				return "";
			}
		}
		
		private ColumnDataType(String regexp) {
			this.regexp=regexp;
		}
	}
	
	@Data
	public static class ColumnInfo {
		
		private String title="", id;

		private boolean hidden = false, mouseOver = false;
	}
	
	public boolean columnIsOfType(String columnID, ColumnDataType type) {
		
		int columnIndex = this.getColumnIndexByID(columnID);
		for(int rowIndex =0; rowIndex<matrix.size(); rowIndex++) {
			
			if( !type.matchesByClass( matrix.get(rowIndex).get(columnIndex) ) ) {
				
				return false;
			}
		}
		return true;
	}
	
	public boolean columnIsNumber(String columnID) {
		
		int columnIndex = this.getColumnIndexByID(columnID);
		for(int rowIndex =0; rowIndex<matrix.size(); rowIndex++) {
			
			if( !ColumnDataType.DOUBLE.matchesByClass( matrix.get(rowIndex).get(columnIndex) )
				&& !ColumnDataType.INTEGER.matchesByClass( matrix.get(rowIndex).get(columnIndex) ) ) {
				
				return false;
			}
		}
		return true;
	}

	private List<List<Object>> matrix = new ArrayList<List<Object>>();
	
	private List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
	
	public TableData(List<ColumnInfo> cs) {
		
		for(ColumnInfo ci : cs) {
			if(ci.getId()==null) {
				throw new NullPointerException("Unset column id on "+ci.getTitle());
			}
		}
		
		this.columns.addAll(cs);
	}
	
	public List<ColumnInfo> getColumnInfos() {
		
		return columns;
	}
	
	public List<ColumnInfo> getVisibleColumnInfos() {
		
		List<ColumnInfo> r = new ArrayList<ColumnInfo>();
		for(ColumnInfo ci : columns) {
			if(!ci.isHidden())
				r.add(ci);
		}
		return r;
	}
	
	public ColumnInfo getColumnInfo(int i) {
		
		return columns.get(i);
	}
	
	/**
	 * 
	 * @return the column with the specified id or null if it doesn't exist
	 */
	public ColumnInfo getColumnByID(String id) {
		
		for(ColumnInfo ci : columns) {
			
			if(ci.getId().equals(id))
				return ci;
		}
		return null;
	}
	/**
	 * 
	 * @return the index of the column with the specified id or -1 if it doesn't exist
	 */
	public int getColumnIndexByID(String id) {
		
		for(int i=0; i<columns.size(); i++) {
			
			if(columns.get(i).getId().equals(id))
				return i;
		}
		return -1;
	}

	/**
	 * @return the list of duplicate values in the column, or null if the column doesn't exist
	 */
	public List<Object> listDuplicateValues(String columnID) {

		int columnIndex = getColumnIndexByID(columnID);
		if(columnIndex==-1) {
			
			return null;
		}
		
		Map<Object,Integer> valueOccurence = new HashMap<Object,Integer>();
		List<Object> duplicates = new ArrayList<Object>();
		for(List<Object> row : matrix) {
			
			Object value = row.get(columnIndex);
			if(!valueOccurence.containsKey(value)) {
				valueOccurence.put(value,0);
			}
			
			valueOccurence.put(value,valueOccurence.get(value) + 1);
			
			if(valueOccurence.get(value)>1) {
				
				duplicates.add(value);
			}
		}
		
		return duplicates;
	}
	
	/**
	 * @return the list of values in the column, or null if the column doesn't exist
	 */
	public List<Object> getColumnValues(String columnID) {
		
		int columnIndex = getColumnIndexByID(columnID);
		if(columnIndex==-1) {
			
			return null;
		}
		else return getColumnValues (columnIndex);
	}
	
	public List<Object> getColumnValues(int columnIndex) {
		
		List<Object> columnValues = new ArrayList<Object>();
		
		for(List<Object> row : matrix) {
			
			columnValues.add(row.get(columnIndex));
		}
		
		return columnValues;
	}

	/**
	 * 
	 * @return the rowIndex or -1 if the given value is not in the column
	 */
	public int getRowIndex(String columnID, Object value) {
		
		int columnIndex = getColumnIndexByID(columnID);
		for(int rowIndex=0; rowIndex<matrix.size();rowIndex++) {
			
			if( matrix.get(rowIndex).get(columnIndex).equals(value) ) {
				
				return rowIndex;
			}
		}
		
		return -1;
	}
	
	/**
	 * 
	 * @return a map where the keys are the column ids.
	 */
	public Map<String,Object> getRowValues(int i) {
		
		Map<String,Object> row = new HashMap<String,Object>();
		
		for(int j=0; j<columns.size(); j++) {
			
			row.put(columns.get(j).getId(), matrix.get(i).get(j));
		}
		
		return row;
	}
	
	public int getNumberOfRows() {
		
		return matrix.size();
	}
	
	public boolean columnHasValue(int columnIndex, Object value) {
		
		for(int rowIndex =0; rowIndex<matrix.size(); rowIndex++) {
			
			if(matrix.get(rowIndex).get(columnIndex).equals(value))
				return true;
		}
		return false;
	}
	public boolean columnHasValue(String columnID, Object value) {
		
		int columnIndex = getColumnIndexByID(columnID);
		
		return columnHasValue(columnIndex, value);
	}
	
	public String getValueAsString(String columnID, int rowIndex) {

		int columnIndex = getColumnIndexByID(columnID);
		ColumnInfo ci = columns.get(columnIndex);
		
		Object value = matrix.get(rowIndex).get(columnIndex);
		
		if(ColumnDataType.INTEGER.matchesByClass(value))
			
			return String.format("%d", value);
		
		else if(ColumnDataType.DOUBLE.matchesByClass(value))
			
			return String.format("%.2f", matrix.get(rowIndex).get(columnIndex));
		else
			return matrix.get(rowIndex).get(columnIndex).toString();
	}
	
	/**
	 * @return the index of the column that was just created
	 */
	public int addColumn(ColumnInfo toAdd) {
		
		for(ColumnInfo ci : columns) {
			
			if(ci.getId().equals(toAdd.getId())) {
				throw new RuntimeException(ci.getId()+": a column with that ID already exists");
			}
		}
		
		columns.add(toAdd);
		for(List<Object> row : matrix) {
			
			row.add(null);
		}
		
		return columns.size() - 1;
	}
	
	
	/**
	 * Automatically detects doubles and integers
	 * @param v String value to be parsed, or a number object
	 */
	public void setValue(String columnID, int rowIndex, Object v) {
		
		while(matrix.size()<(rowIndex+1)) {
			
			List<Object> emptyRow = new ArrayList<Object>();
			for(ColumnInfo ci : columns) {
				emptyRow.add( "" );
			}
			matrix.add(emptyRow);
		}
		
		int columnIndex = getColumnIndexByID(columnID);
		if(columnIndex == -1) {
			
			ColumnInfo nci = new ColumnInfo();
			nci.setId(columnID);
			nci.setTitle(columnID);
			columnIndex = addColumn(nci);
		}
		
		ColumnInfo ci = columns.get(columnIndex);

		Object value = null;
		for(ColumnDataType type : 
			new ColumnDataType[] {	ColumnDataType.INTEGER, // order matters here !
									ColumnDataType.DOUBLE,
									ColumnDataType.STRING }) {
			
			if ( type.matchesByClass(v) ) {

				value = v;
				break;
				
			} else if( String.class.isInstance(v) && ((String)v).matches(type.regexp) ) {

				value = type.fromString((String)v);
				break;
			}
		}
		if(value==null) {
			value = v.toString();
		}
		
		matrix.get(rowIndex).set(columnIndex, value);
	}
}
