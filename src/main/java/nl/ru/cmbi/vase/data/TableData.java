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
	
	public int getResidueNumber(int row) {
		
		return (Integer) getRowValues(row).get(residueNumberID);
	}
	
	public String getPDBResidue(int row) {

		return (String) getRowValues(row).get(pdbResidueID);
	}
	
	public enum ColumnDataType { 
		STRING(".*"), DOUBLE("^\\s*\\-?[0-9]+(\\.[0-9]+)?(e\\-?[0-9]+)?\\s*$"), INTEGER("^\\s*\\-?[0-9]+\\s*$") ;
		
		public final String regexp;
		
		public boolean matchesWithClass(Object obj) {
			
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

		@Setter(AccessLevel.PROTECTED)
		private ColumnDataType type = ColumnDataType.STRING; // set internally by the tabledata object
		
		public boolean isNumber() {
			
			return type==ColumnDataType.DOUBLE || type==ColumnDataType.INTEGER;
		}
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
	
	public List<Object> getColumnValues(int i) {
		
		List<Object> columnValues = new ArrayList<Object>();
		
		for(List<Object> row : matrix) {
			
			if(columns.get(i).getType().equals(ColumnDataType.STRING)) {

				columnValues.add(row.get(i).toString());
			}
			else {
				columnValues.add(row.get(i));
			}
		}
		
		return columnValues;
	}
	
	/**
	 * 
	 * @return a map where the keys are the column ids.
	 */
	public Map<String,Object> getRowValues(int i) {
		
		Map<String,Object> row = new HashMap<String,Object>();
		
		for(int j=0; j<columns.size(); j++) {
			
			if(columns.get(j).getType().equals(ColumnDataType.STRING)) {
				
				row.put(columns.get(j).getId(), matrix.get(i).get(j).toString());
			}
			else {
				row.put(columns.get(j).getId(), matrix.get(i).get(j));
			}
		}
		return row;
	}
	
	public int getNumberOfRows() {
		
		return matrix.size();
	}
	
	public String getValueAsString(String columnID, int rowIndex) {

		int columnIndex = getColumnIndexByID(columnID);
		ColumnInfo ci = columns.get(columnIndex);
		
		switch(ci.getType()) {
		case INTEGER:
			return String.format("%d", matrix.get(rowIndex).get(columnIndex));
		case DOUBLE:
			return String.format("%.2f", matrix.get(rowIndex).get(columnIndex));
		default:
			return matrix.get(rowIndex).get(columnIndex).toString();
		}
	}
	/**
	 * Automatically detects doubles and integers
	 * @param v String value to be parsed, or a number object
	 */
	public void setValue(String columnID, int rowIndex, Object v) {
		
		while(matrix.size()<(rowIndex+1)) {
			
			List<Object> emptyRow = new ArrayList<Object>();
			for(ColumnInfo ci : columns) {
				emptyRow.add(ci.getType().initialValue());
			}
			matrix.add(emptyRow);
		}
		
		int columnIndex = getColumnIndexByID(columnID);
		ColumnInfo ci = columns.get(columnIndex);

		Object value = null;
		for(ColumnDataType type : new ColumnDataType[] { ColumnDataType.INTEGER, ColumnDataType.DOUBLE, ColumnDataType.STRING }) {
			
			if( ci.getType().equals(type) || matrix.size()<=1 ) {
				
				if ( type.matchesWithClass(v) ) {

					value = v;
					ci.setType(type);
					break;
					
				} else if( String.class.isInstance(v) && ((String)v).matches(type.regexp) ) {

					value = type.fromString((String)v);
					ci.setType(type);
					break;
				}
			}
		}
		if(value==null) {
			value = v.toString();
			ci.setType(ColumnDataType.STRING);
		}
		
		matrix.get(rowIndex).set(columnIndex, value);
	}
}
