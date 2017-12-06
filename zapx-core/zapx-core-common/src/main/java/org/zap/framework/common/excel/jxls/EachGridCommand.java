package org.zap.framework.common.excel.jxls;

import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.Command;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.GroupData;
import org.jxls.common.Size;
import org.jxls.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The command implements a grid with dynamic columns and rows
 * Created by Leonid Vysochyn on 25-Jun-15.
 */
public class EachGridCommand extends AbstractCommand {
    public static final String COMMAND_NAME = "eachGrid";
    public static final String HEADER_VAR = "header";
    public static final String CELL_VAR = "cell";
    public static final String DATA_VAR = "data";

    
	String items;
	String headerBy;
	String headerOrder;
	String groupBy;
	String groupOrder;
    List<String> groupTotalList;
    Area headerArea;
    Area cellArea;
    Area dataArea;
    
    public EachGridCommand() {
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
	
	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public String getGroupOrder() {
		return groupOrder;
	}

	public void setGroupOrder(String groupOrder) {
		this.groupOrder = groupOrder;
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
	
	//分组小计
	public void setGroupTotals(String groupTotals) {
		List<String> groupTotalList = new ArrayList<String>();
	    if (groupTotals != null) {
	    	groupTotalList = Arrays.asList(groupTotals.split(","));
	    }
		this.groupTotalList = groupTotalList;
	}


    public List<String> getGroupTotalList() {
		return groupTotalList;
	}

	public void setGroupTotalList(List<String> groupTotalList) {
		this.groupTotalList = groupTotalList;
	}
	
	
	public Command addArea(Area area) {
		if( area == null ){
			return this;
		}
		
		if(super.getAreaList().isEmpty()){
			this.headerArea = area;
		}else if(super.getAreaList().size() == 1){
			this.cellArea = area;
		}else if(super.getAreaList().size() == 2){
			this.dataArea = area;
		}

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
        Size headerAreaSize = processHeaders(headers,cellRef, context);
        CellRef bodyDataRef = new CellRef(cellRef.getSheetName(), cellRef.getRow() + headerAreaSize.getHeight(), cellRef.getCol());
        Size bodyAreaSize = processBody(itemsCollection,headers,bodyDataRef, context);
        int gridHeight = bodyAreaSize.getHeight();
        int gridWidth = Math.max( headerAreaSize.getWidth(), bodyAreaSize.getWidth());
        return new Size(gridWidth, gridHeight);
    }

    private Size processBody(Collection itemsCollection,Collection headers,CellRef cellRef, Context context) {
    	if(dataArea == null|| groupBy == null){
    		return Size.ZERO_SIZE;
    	}
    	Collection<GroupData> datas = ContextUtils.groupCollection(itemsCollection, groupBy, groupOrder, groupTotalList);
        CellRef currentCell = cellRef;
		if(cellArea != null){
			currentCell = cellArea.getStartCellRef();
		}

        int totalWidth = 0;
        int totalHeight = 0;
    	for(GroupData groupData : datas){
    		if(cellArea != null){
        		context.putVar(CELL_VAR, groupData.getItem());
        		Size size = cellArea.applyAt(currentCell, context);
        		currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol()+ size.getWidth());
                totalWidth = totalWidth+size.getWidth();
                totalHeight = Math.max( totalHeight, size.getHeight() );
    		}

            int width = 0;
            int height = 0;          
            Collection dataCollection = ContextUtils.getGridDataByHeaderCollection(groupData.getItems(),headers,headerBy,null);
            for(Object data : dataCollection){
                context.putVar(DATA_VAR, data);
                Size size = dataArea.applyAt(currentCell, context);
                currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
                width += size.getWidth();
                height = Math.max( height, size.getHeight() );
            }
            context.removeVar(DATA_VAR);
            
            totalWidth = Math.max( width, totalWidth );
            totalHeight = totalHeight + height;
            currentCell = new CellRef(cellRef.getSheetName(), currentCell.getRow() + height, cellRef.getCol());
    	}
        context.removeVar(CELL_VAR);
        return new Size(totalWidth, totalHeight);
    }

    private Size processHeaders(Collection headers,CellRef cellRef, Context context) {
        if(headerArea == null){
            return Size.ZERO_SIZE;
        }
        CellRef currentCell = cellRef;
        int width = 0;
        int height = 0;
        for( Object header : headers){
    		context.putVar(HEADER_VAR, header);
            Size size = headerArea.applyAt(headerArea.getStartCellRef(), context);
            currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
            width += size.getWidth();
            height = Math.max( height, size.getHeight() );
        }
        return new Size(width, height);
    }

}
