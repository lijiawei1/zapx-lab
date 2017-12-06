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
public class CombineRowCommand extends AbstractCommand {
    Area area;
    String combineBy;
    Object combineByValue;  
    List<String> combineCellList;  
    
    public String getName() {
        return "combineRow";
    }

    	
    public Size applyAt(CellRef cellRef, Context context) {
        Size resultSize = area.applyAt(cellRef, context);
        if(resultSize.equals(Size.ZERO_SIZE)) return resultSize;
        MyPoiTransformer transformer = (MyPoiTransformer) area.getTransformer();
        Workbook workbook = transformer.getWorkbook();
        Sheet sheet = workbook.getSheet(cellRef.getSheetName());
        int startRow = cellRef.getRow();
        int lastRow = startRow - resultSize.getHeight();
        int endRow = startRow + resultSize.getHeight() - 1;
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
        			int col  = item.getCol();
        			Cell  lastCell = null ;
        			Cell  thisCell = null ;
        			if(sheet.getRow(lastRow)!=null&&sheet.getRow(startRow)!=null){
        				lastCell = sheet.getRow(lastRow).getCell(col);
            			thisCell = sheet.getRow(startRow).getCell(col);
        			}
        			if(lastCell!=null&&thisCell!=null){
            			String lastCellValue =PoiUtils.getCellContents(lastCell);
            			String thisCellValue =PoiUtils.getCellContents(thisCell);
            			if(lastCellValue!=null&&thisCellValue!=null&&thisCellValue.equals(lastCellValue)){	
                            boolean mflag = true;
            				//判断是否属于合并单元，如果是则和之前的一起合并
            				int numMergedRegions = sheet.getNumMergedRegions();
            				//不同行
            				for(int i = 0; i < numMergedRegions; i++){
            					CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            					if( mergedRegion.getFirstRow() <= lastRow && mergedRegion.getLastRow() >= lastRow && mergedRegion.getFirstColumn() <= col && mergedRegion.getLastColumn() >= col){           						
            						int firstRow = mergedRegion.getFirstRow();
            						int lastColumn = mergedRegion.getLastColumn();
            						sheet.removeMergedRegion(i);
                    				
                    				//同一行
            						int numMergedRegions2 = sheet.getNumMergedRegions();
                      				for(int j = 0; j < numMergedRegions2; j++){
                    					CellRangeAddress mergedRegion2 = sheet.getMergedRegion(j);
                    					if( mergedRegion2.getFirstRow() <= endRow && mergedRegion2.getLastRow() >= endRow && mergedRegion2.getFirstColumn() <= col && mergedRegion2.getLastColumn() >= col){           						
                    						sheet.removeMergedRegion(j);
                    						break;
                    					}
                    			    }
                      				
            						sheet.addMergedRegion(new CellRangeAddress(firstRow, endRow,col, lastColumn));
            						mflag= false;
            						break;
            					}
            			    }
         
            				
            				//如果不属于合并单元格则合并
            				if(mflag){
            					sheet.addMergedRegion(new CellRangeAddress(lastRow,endRow,col,col));//合并单元格
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
