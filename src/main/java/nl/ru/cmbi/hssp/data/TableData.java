package nl.ru.cmbi.hssp.data;

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
	
	@Data
	public static class ColumnInfo {
		
		private String title="", id;

		private boolean hidden = false, mouseOver = false;

		@Setter(AccessLevel.PROTECTED)
		private boolean isNumber = true; // set internally by the tabledata object
	}

	private List<List<Object>> matrix = new ArrayList<List<Object>>();
	
	private List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
	
	public TableData(List<ColumnInfo> columns) {
		
		this.columns.addAll(columns);
	}
	
	public List<ColumnInfo> getColumnInfos() {
		
		return columns;
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
	
	public List<Object> getColumnValues(int i) {
		
		List<Object> columnValues = new ArrayList<Object>();
		
		for(List<Object> row : matrix) {
			
			columnValues.add(row.get(i));
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
			
			row.put(columns.get(j).id, matrix.get(i).get(j));
		}
		return row;
	}
	
	public int getNumberOfRows() {
		
		return matrix.size();
	}
	
	public void addRow(List<String> values) throws Exception {
		
		if(values.size()!=columns.size()) {
			throw new Exception("number of values doesn\'t match the number of columns");
		}
		
		List<Object> row = new ArrayList<Object>();
		for(int i=0; i<values.size(); i++) {
			
			if(columns.get(i).isNumber()) {
				
				try {
					
					Integer n = Integer.parseInt(values.get(i));
					row.add(i, n);
					
				} catch(NumberFormatException ei) {
					
					try {
						
						Double d = Double.parseDouble(values.get(i));
						row.add(i, d);
						
					} catch(NumberFormatException ed) {
						
						columns.get(i).setNumber(false);
						row.add(i, values.get(i));
					}
				}
			}
			else
				row.add(i, values.get(i));
		}
		matrix.add(row);
	}
}
