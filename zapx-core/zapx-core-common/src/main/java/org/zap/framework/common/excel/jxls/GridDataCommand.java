package org.zap.framework.common.excel.jxls;

import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.Command;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.util.Util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The command implements a grid with dynamic columns and rows
 * Created by Leonid Vysochyn on 25-Jun-15.
 */
public class GridDataCommand extends AbstractCommand {
    public static final String COMMAND_NAME = "gridData";
    public static final String HEADER_BY_VAR= "header_by";
    public static final String HEADER_VAR = "header";

    Area area;
	String items;
	String var;
	String defaultValues ;
    
    public GridDataCommand() {
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

	public String getItems() {
		return items;
	}


	public void setItems(String items) {
		this.items = items;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}
	
	public String getDefaultValues() {
		return defaultValues;
	}

	public void setDefaultValues(String defaultValues) {
		this.defaultValues = defaultValues;
	}

	public Command addArea(Area area) {
		if( area == null ){
			return this;
		}
        if( super.getAreaList().size() >= 1){
            throw new IllegalArgumentException("You can add only a single area to 'gridData' command");
        }
        this.area = area;
		return super.addArea(area);
	}
	
	@Override
    public Size applyAt(CellRef cellRef, Context context) {	
        Collection itemsCollection = Util.transformToCollectionObject(getTransformationConfig().getExpressionEvaluator(), this.items, context);
        Collection headers = Util.transformToCollectionObject(getTransformationConfig().getExpressionEvaluator(), HEADER_VAR, context);
        Object headerBy = getTransformationConfig().getExpressionEvaluator().evaluate(HEADER_BY_VAR, context.toMap());       
        if(itemsCollection == null||headers == null||headerBy == null){
            return Size.ZERO_SIZE;
        }       
        Map defaultValueMap = null;
        if(defaultValues!=null){
	    	defaultValueMap  = new HashMap();
	    	String[] defaultFields = defaultValues.split(",");
	    	if(defaultFields!=null&&defaultFields.length>0){
	    		for(int i=0 ; i < defaultFields.length ; i ++){
	    			String defaultField = defaultFields[i];
	    			String[] defaultFieldValue = defaultField.split(":");
	    			if(defaultFieldValue==null||defaultFieldValue.length!=2){
	    				 throw new IllegalArgumentException("defaultValues标签定义有误");
	    			}
	    			defaultValueMap.put(defaultFieldValue[0], defaultFieldValue[1]);
	    		}
	    	}
        }
        Collection itemsByHeaderCollection =  ContextUtils.getGridDataByHeaderCollection(itemsCollection, headers, headerBy.toString(),defaultValueMap);
        Size resultSize = processGridDatas(itemsByHeaderCollection,cellRef,context);
        return resultSize;
    }

    private Size processGridDatas(Collection itemsByHeaderCollection,CellRef cellRef, Context context) {
        if(area == null){
            return Size.ZERO_SIZE;
        }
        CellRef currentCell = cellRef;
        int width = 0;
        int height = 0;
        for( Object item : itemsByHeaderCollection){
        	context.putVar(this.var, item);
        	Size size = this.area.applyAt(currentCell, context);        	 
            currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());       
            width += size.getWidth();
            height = Math.max(height, size.getHeight());
        }
 		context.removeVar(var);
        return new Size(width, height);
    }

}
