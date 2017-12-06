package org.zap.framework.common.excel.jxls;

import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.CellRefGenerator;
import org.jxls.command.Command;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.GroupData;
import org.jxls.common.Size;
import org.jxls.expression.JexlExpressionEvaluator;
import org.jxls.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class EachGroupCommand extends AbstractCommand {
	
    public static final String COMMAND_NAME = "eachGroup";
    public static final String GROUP_VAR= "group";
    
    public enum Direction {RIGHT, DOWN}
    
    String var;
    String items;
    String select;
    Area area;
    Direction direction = Direction.DOWN;
    CellRefGenerator cellRefGenerator;
    List<String> groupTotalList;
	String groupBy;
	String groupOrder;
	
    public EachGroupCommand() {
    }

    /**
     * @param var name of the key in the context to contain each collection items during iteration
     * @param items name of the collection bean in the context
     * @param direction defines processing by rows (DOWN - default) or columns (RIGHT)
     */
    public EachGroupCommand(String var, String items, Direction direction) {
        this.var = var;
        this.items = items;
        this.direction = direction == null ? Direction.DOWN : direction;
    }

    public EachGroupCommand(String var, String items, Area area) {
        this(var, items, area, Direction.DOWN);
    }
    
    public EachGroupCommand(String var, String items, Area area, Direction direction) {
        this( var, items, direction );
        if( area != null ){
            this.area = area;
            addArea(this.area);
        }
    }

    /**
     *
     * @param var name of the key in the context to contain each collection items during iteration
     * @param items name of the collection bean in the context
     * @param area body area for this command
     * @param cellRefGenerator generates target cell ref for each collection item during iteration
     */
    public EachGroupCommand(String var, String items, Area area, CellRefGenerator cellRefGenerator) {
        this(var, items, area, (Direction)null );
        this.cellRefGenerator = cellRefGenerator;
    }

    /**
     * Gets iteration directino
     * @return current direction for iteration
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets iteration direction
     * @param direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setDirection(String direction){
        this.direction = Direction.valueOf(direction);
    }

    /**
     * Gets defined cell ref generator
     * @return current {@link CellRefGenerator} instance or null
     */
    public CellRefGenerator getCellRefGenerator() {
        return cellRefGenerator;
    }

    public void setCellRefGenerator(CellRefGenerator cellRefGenerator) {
        this.cellRefGenerator = cellRefGenerator;
    }

    public String getName() {
        return COMMAND_NAME ;
    }

    /**
     * Gets current variable name for collection item in the context during iteration
     * @return collection item key name in the context
     */
    public String getVar() {
        return var;
    }

    /**
     * Sets current variable name for collection item in the context during iteration
     * @param var
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Gets collection bean name
     * @return collection bean name in the context
     */
    public String getItems() {
        return items;
    }

    /**
     * Sets collection bean name
     * @param items collection bean name in the context
     */
    public void setItems(String items) {
        this.items = items;
    }

    /**
     * Gets current 'select' expression for filtering out collection items
     * @return current 'select' expression or null if undefined
     */
    public String getSelect() {
        return select;
    }

    /**
     * Sets current 'select' expression for filtering collection
     * @param select filtering expression
     */
    public void setSelect(String select) {
        this.select = select;
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
	
	//分组小计
	public void setGroupTotals(String groupTotals) {
		List<String> groupTotalList = new ArrayList<String>();
	    if (groupTotals != null) {
	    	groupTotalList = Arrays.asList(groupTotals.split(","));
	    }
		this.groupTotalList = groupTotalList;
	}
    

    @Override
    public Command addArea(Area area) {
        if( area == null ){
            return this;
        }
        if( super.getAreaList().size() >= 1){
            throw new IllegalArgumentException("You can add only a single area to 'each' command");
        }
        this.area = area;
        return super.addArea(area);
    }

    /* (non-Javadoc)
     * @see org.jxls.command.Command#applyAt(org.jxls.common.CellRef, org.jxls.common.Context)
     */
    public Size applyAt(CellRef cellRef, Context context) {
    	Collection itemsCollection = Util.transformToCollectionObject(getTransformationConfig().getExpressionEvaluator(), getItems(), context);
    	Collection<GroupData> groupData = ContextUtils.groupCollection(itemsCollection, groupBy, groupOrder, groupTotalList);
    	if(itemsCollection == null||groupData == null){
    		return Size.ZERO_SIZE;
    	}
    	context.putVar(GROUP_VAR, groupData);
    	Size headerAreaSize = processGroupDatas(groupData,cellRef, context);
    	return headerAreaSize;
    }
    
    
    public Size processGroupDatas(Collection itemsCollection,CellRef cellRef, Context context) {
        int width = 0;
        int height = 0;
        int index = 0;
        CellRef currentCell = cellRefGenerator != null ? cellRefGenerator.generateCellRef(index, context) : cellRef;
        JexlExpressionEvaluator selectEvaluator = null;
        if( select != null ){
            selectEvaluator = new JexlExpressionEvaluator( select );
        }
        for( Object obj : itemsCollection){
            context.putVar(var, obj);
            if( selectEvaluator != null && !Util.isConditionTrue(selectEvaluator, context) ){
                context.removeVar(var);
                continue;
            }
            Size size = area.applyAt(currentCell, context);
            index++;
            if( cellRefGenerator != null ){
                width = Math.max(width, size.getWidth());
                height = Math.max(height, size.getHeight());
                currentCell = cellRefGenerator.generateCellRef(index, context);
            }else if( direction == Direction.DOWN ){
                currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow() + size.getHeight(), currentCell.getCol());
                width = Math.max(width, size.getWidth());
                height += size.getHeight();
            }else{
                currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
                width += size.getWidth();
                height = Math.max( height, size.getHeight() );
            }
            context.removeVar(var);
        }
        return new Size(width, height);
    }

}




