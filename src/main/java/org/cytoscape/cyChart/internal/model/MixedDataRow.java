package org.cytoscape.cyChart.internal.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MixedDataRow 	 
{
    IntegerProperty rowNum = new SimpleIntegerProperty(0);
    LongProperty rowID = new SimpleLongProperty(0);
    private ObservableList<SimpleStringProperty> strs = FXCollections.observableArrayList();
    private ObservableList<SimpleDoubleProperty> vals = FXCollections.observableArrayList();
    private ObservableList<SimpleIntegerProperty> types = FXCollections.observableArrayList();
    private ObservableList<SimpleIntegerProperty> ranges = FXCollections.observableArrayList();
	
    public MixedDataRow(int nCols)
	{
        for(int i=0; i<nCols; ++i)
        {
	        strs.add(new SimpleStringProperty(""));
	        vals.add(new SimpleDoubleProperty(0.));
	        types.add(new SimpleIntegerProperty(0));
	        ranges.add(new SimpleIntegerProperty(0));
        }
	}
    public int getRowNum()				{	return rowNum.get();		}
	public void setRowNum(int row)		{ 	rowNum.set(row); } 
	public void setRowID(long id)		{ 	rowID.set(id); } 
	public void setRowID(int row, long id){ 	setRowNum(row); setRowID(id);} 
	public void set(int i, Double s)		{ 	vals.get(i).set(s);	} 
	public void setString(int i, String s)		{ 	strs.get(i).set(s);	} 
    public DoubleProperty get(int i) 	{ 	return vals.get(i); }
    public StringProperty getString(int i) 	{ 	return strs.get(i); }
    public IntegerProperty getType(int i) 	{ 	return types.get(i); }
    public IntegerProperty getRange(int i) 	{ 	return ranges.get(i); }
    
    public boolean isString(int i)		{ return types.get(i).intValue() == 0; }
    public boolean isBool(int i)			{ return types.get(i).intValue() == 1; }
    public boolean isNumber(int i)		{ return types.get(i).intValue() > 1; }
    
    public int getWidth()				{ 	return vals.size();	}
}
