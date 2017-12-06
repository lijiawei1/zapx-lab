package org.zap.framework.common.excel.jxls;

import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.Command;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.util.Util;

import java.util.Collection;

/**
 * The command implements a grid with dynamic columns and rows
 * Created by Leonid Vysochyn on 25-Jun-15.
 */
public class GridHeaderCommand extends AbstractCommand {
    public static final String COMMAND_NAME = "gridHeader";
    public static final String HEADER_BY_VAR= "header_by";
    public static final String HEADER_VAR = "header";

    Area area;
	String items;
	String headerBy;
	String headerOrder;
	String var;

    
    public GridHeaderCommand() {
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


	public String getHeaderBy() {
		return headerBy;
	}

	public void setHeaderBy(String headerBy) {
		this.headerBy = headerBy;
	}


    public String getHeaderOrder() {
		return headerOrder;
	}

	public void setHeaderOrder(String headerOrder) {
		this.headerOrder = headerOrder;
	}
	
	
	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public Command addArea(Area area) {
		if( area == null ){
			return this;
		}
        if( super.getAreaList().size() >= 1){
            throw new IllegalArgumentException("You can add only a single area to 'gridHeader' command");
        }
        this.area = area;
		return super.addArea(area);
	}
	


	@Override
    public Size applyAt(CellRef cellRef, Context context) {	
        Collection itemsCollection = Util.transformToCollectionObject(getTransformationConfig().getExpressionEvaluator(), this.items, context);
        Collection headers = ContextUtils.getGridHeaderCollection(itemsCollection,headerBy,headerOrder);
        if(itemsCollection == null||headers == null){
            return Size.ZERO_SIZE;
        }
    	context.putVar(HEADER_VAR, headers);
    	context.putVar(HEADER_BY_VAR, headerBy);
        Size headerAreaSize = processGridHeaders(headers,cellRef, context);
        return headerAreaSize;
    }

    private Size processGridHeaders(Collection headers,CellRef cellRef, Context context) {
        if(area == null){
            return Size.ZERO_SIZE;
        }
        CellRef currentCell = cellRef;
        int width = 0;
        int height = 0;
        for( Object header : headers){
    		context.putVar(var, header);
            Size size = area.applyAt(currentCell, context);
            currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
            width += size.getWidth();
            height = Math.max( height, size.getHeight() );
        }
 		context.removeVar(var);
        return new Size(width, height);
    }

}
