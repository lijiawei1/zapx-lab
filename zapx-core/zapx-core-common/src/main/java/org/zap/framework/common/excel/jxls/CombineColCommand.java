package org.zap.framework.common.excel.jxls;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.Command;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of a Command for row combine
 * @author Leonid Sunqx
 *         Date: 9/9/15
 */
public class CombineColCommand extends AbstractCommand {
    Area area;
    String combineBy;
    Object combineByValue;  
    List<String> combineCellList;  
    
    public String getName() {
        return "combineCol";
    }

    	
    public Size applyAt(CellRef cellRef, Context context) {
        Size resultSize = area.applyAt(cellRef, context);
        if(resultSize.equals(Size.ZERO_SIZE)) return resultSize;
        MyPoiTransformer transformer = (MyPoiTransformer) area.getTransformer();
        Workbook workbook = transformer.getWorkbook();
        Sheet sheet = workbook.getSheet(cellRef.getSheetName());
        int startCol = cellRef.getCol();
        int lastCol = startCol - resultSize.getWidth();
        int endCol = startCol + resultSize.getWidth() - 1;
        boolean flag = false ;
        if(combineBy==null){
        	flag = true;
        }else{
            Object currentCombineByValue = getTransformationConfig().getExpressionEvaluator().evaluate(combineBy, context.toMap());
            if(currentCombineByValue!=null&&combineByValue!=null&&combineByValue.toString().equals(currentCombineByValue.toString())){
            	flag = true;
            }
            combineByValue = currentCombineByValue ;
        }
        
        if(flag){	   	
        	if(!CollectionUtils.isEmpty(combineCellList)){
        		for(String cell : combineCellList){
        			CellRef item =  new CellRef(cell);
        			int row  = item.getRow();
        			Cell  lastCell = sheet.getRow(row).getCell(lastCol);
        			Cell  thisCell = sheet.getRow(row).getCell(startCol);
        			if(lastCell!=null&&thisCell!=null){
            			String lastCellValue =PoiUtils.getCellContents(lastCell);
            			String thisCellValue =PoiUtils.getCellContents(thisCell);
            			if(lastCellValue!=null&&thisCellValue!=null&&thisCellValue.equals(lastCellValue)){	
                            boolean mflag = true;
            				//判断是否属于合并单元，如果是则和之前的一起合并
            				int numMergedRegions = sheet.getNumMergedRegions();
            				for(int j = 0; j < numMergedRegions; j++){
            					CellRangeAddress mergedRegion = sheet.getMergedRegion(j);
            					if( mergedRegion.getFirstColumn() < lastCol && mergedRegion.getLastColumn() >= lastCol && mergedRegion.getFirstRow() <= row && mergedRegion.getLastRow() >= row){       						
            						sheet.removeMergedRegion(j);
            						sheet.addMergedRegion(new CellRangeAddress(row,row, mergedRegion.getFirstColumn() ,endCol));
            						mflag= false;
            						break;
            					}
            			    }
            				
            				//如果不属于合并单元格则合并
            				if(mflag){
            					sheet.addMergedRegion(new CellRangeAddress(row,row,lastCol,endCol));//合并单元格
            				}           				
            			}
        			}
        		
        		}
        	}      
        }     	
        return resultSize;
    }

    @Override
    public Command addArea(Area area) {
        super.addArea(area);
        this.area = area;
        return this;
    }

    
	//要合并的单元格
	public void setCombineCells(String combineCells) {
		List<String> combineCellList = new ArrayList<String>();
	    if (combineCells != null) {
	    	combineCellList = Arrays.asList(combineCells.split(","));
	    }
		this.combineCellList = combineCellList;
	}
	  
    public void setCombineBy(String combineBy) {
    	this.combineByValue = null;
        this.combineBy = combineBy;
    }
      
}
