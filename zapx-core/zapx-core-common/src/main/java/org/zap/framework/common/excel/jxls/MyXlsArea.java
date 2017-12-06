package org.zap.framework.common.excel.jxls;

import org.jxls.area.Area;
import org.jxls.area.CommandData;
import org.jxls.area.XlsArea;
import org.jxls.command.Command;
import org.jxls.common.*;
import org.jxls.common.cellshift.AdjacentCellShiftStrategy;
import org.jxls.common.cellshift.CellShiftStrategy;
import org.jxls.common.cellshift.InnerCellShiftStrategy;
import org.jxls.transform.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Core implementation of {@link Area} interface
 * @author sunqx
 * Date: 9/16/15
 * 测试用
 */
public class MyXlsArea extends XlsArea {
    static Logger logger = LoggerFactory.getLogger(MyXlsArea.class);
    CellRange cellRange;

    private final CellShiftStrategy innerCellShiftStrategy = new InnerCellShiftStrategy();
    private final CellShiftStrategy adjacentCellShiftStrategy = new AdjacentCellShiftStrategy();

    public MyXlsArea(AreaRef areaRef, Transformer transformer){
        super(areaRef, transformer);
    }
    
    public MyXlsArea(CellRef startCellRef, Size size, List<CommandData> commandDataList, Transformer transformer) {
    	 super(startCellRef, size,commandDataList,transformer);
    }
    
    public MyXlsArea(String areaRef, Transformer transformer){
        this(new AreaRef(areaRef), transformer);
    }
    
    public MyXlsArea(CellRef startCell, CellRef endCell, Transformer transformer){
        this(new AreaRef(startCell, endCell), transformer);
    }

    public MyXlsArea(CellRef startCellRef, Size size) {
        super(startCellRef, size, null, null);
    }

    public MyXlsArea(CellRef startCellRef, Size size, Transformer transformer) {
        super(startCellRef, size, null, transformer);
    }


    private void createCellRange(){
        cellRange = new CellRange(super.getStartCellRef(), super.getSize().getWidth(), super.getSize().getHeight());
        for(CommandData commandData: super.getCommandDataList()){
            cellRange.excludeCells(commandData.getStartCellRef().getCol() - super.getStartCellRef().getCol(), commandData.getStartCellRef().getCol() - super.getStartCellRef().getCol() + commandData.getSize().getWidth()-1,
                    commandData.getStartCellRef().getRow() - super.getStartCellRef().getRow(), commandData.getStartCellRef().getRow() - super.getStartCellRef().getRow() + commandData.getSize().getHeight()-1);
        }
    }


    public Size applyAt(CellRef cellRef, Context context) {
        fireBeforeApplyEvent(cellRef, context);
        createCellRange();
        int topStaticAreaLastRow = transformTopStaticArea(cellRef, context);
        for (int i = 0; i < super.getCommandDataList().size(); i++) {
            cellRange.resetChangeMatrix();
            CommandData commandData = super.getCommandDataList().get(i);
            String shiftMode = commandData.getCommand().getShiftMode();
            CellShiftStrategy commandCellShiftStrategy = detectCellShiftStrategy(shiftMode);
            cellRange.setCellShiftStrategy(commandCellShiftStrategy);
            CellRef newCell = new CellRef(cellRef.getSheetName(), commandData.getStartCellRef().getRow() - super.getStartCellRef().getRow() + cellRef.getRow(), commandData.getStartCellRef().getCol() - super.getStartCellRef().getCol() + cellRef.getCol());
            Size initialSize = commandData.getSize();
            Size newSize = commandData.getCommand().applyAt(newCell, context);
            int widthChange = newSize.getWidth() - initialSize.getWidth();
            int heightChange = newSize.getHeight() - initialSize.getHeight();
            if( widthChange != 0 || heightChange != 0){
                if( widthChange != 0 ){
                    cellRange.shiftCellsWithRowBlock(commandData.getStartCellRef().getRow() - super.getStartCellRef().getRow(),
                            commandData.getStartCellRef().getRow() - super.getStartCellRef().getRow() + newSize.getHeight()-1,
                            commandData.getStartCellRef().getCol() - super.getStartCellRef().getCol() + commandData.getSize().getWidth()-1, widthChange);
                }
                if( heightChange != 0 ){
                    cellRange.shiftCellsWithColBlock(commandData.getStartCellRef().getCol() - super.getStartCellRef().getCol(),
                            commandData.getStartCellRef().getCol() - super.getStartCellRef().getCol() + newSize.getWidth()-1,
                            commandData.getStartCellRef().getRow() - super.getStartCellRef().getRow() + commandData.getSize().getHeight()-1, heightChange);
                
                }
                for (int j = i + 1; j < super.getCommandDataList().size(); j++) {
                    CommandData data = super.getCommandDataList().get(j);
                    int newRow = data.getStartCellRef().getRow() - super.getStartCellRef().getRow() + cellRef.getRow();
                    int newCol = data.getStartCellRef().getCol() - super.getStartCellRef().getCol() + cellRef.getCol();
                    if(newRow > newCell.getRow() && ((newCol >= newCell.getCol() && newCol <= newCell.getCol() + newSize.getWidth()) ||
                            (newCol + data.getSize().getWidth() >= newCell.getCol() && newCol + data.getSize().getWidth() <= newCell.getCol() + newSize.getWidth()) ||
                            (newCell.getCol() >= newCol && newCell.getCol() <= newCol + data.getSize().getWidth() )
                    )){
                        cellRange.shiftCellsWithColBlock(data.getStartCellRef().getCol() - super.getStartCellRef().getCol(),
                                data.getStartCellRef().getCol() - super.getStartCellRef().getCol() + data.getSize().getWidth()-1,
                                data.getStartCellRef().getRow() - super.getStartCellRef().getRow() + data.getSize().getHeight()-1, heightChange);
                        data.setStartCellRef(new CellRef(data.getStartCellRef().getSheetName(), data.getStartCellRef().getRow() + heightChange, data.getStartCellRef().getCol()));
                    }else
                    if( newCol > newCell.getCol() && ( (newRow >= newCell.getRow() && newRow <= newCell.getRow() + newSize.getHeight()) ||
                            (newRow + data.getSize().getHeight() >= newCell.getRow() && newRow + data.getSize().getHeight() <= newCell.getRow() + newSize.getHeight()) ||
                             newCell.getRow() >= newRow && newCell.getRow() <= newRow + data.getSize().getHeight()) ){
                        cellRange.shiftCellsWithRowBlock(data.getStartCellRef().getRow() - super.getStartCellRef().getRow(),
                                data.getStartCellRef().getRow() - super.getStartCellRef().getRow() + data.getSize().getHeight()-1,
                                data.getStartCellRef().getCol() - super.getStartCellRef().getCol() + data.getSize().getWidth()-1, widthChange);
                        data.setStartCellRef(new CellRef(data.getStartCellRef().getSheetName(), data.getStartCellRef().getRow(), data.getStartCellRef().getCol() + widthChange));
                    }
                }
            }
        }

        transformStaticCells(cellRef, context, topStaticAreaLastRow + 1);
        fireAfterApplyEvent(cellRef, context);
        Size finalSize = new Size(cellRange.calculateWidth(), cellRange.calculateHeight());
        AreaRef newAreaRef = new AreaRef(cellRef, finalSize);
        updateCellDataFinalAreaForFormulaCells(newAreaRef);
        return finalSize;
    }

    private CellShiftStrategy detectCellShiftStrategy(String shiftMode) {
        if( shiftMode != null && Command.ADJACENT_SHIFT_MODE.equalsIgnoreCase(shiftMode)){
            return adjacentCellShiftStrategy;
        }else{
            return innerCellShiftStrategy;
        }
    }

    private void updateCellDataFinalAreaForFormulaCells(AreaRef newAreaRef) {
        String sheetName = super.getStartCellRef().getSheetName();
        int offsetRow = super.getStartCellRef().getRow();
        int startCol = super.getStartCellRef().getCol();
        for(int col = 0; col < super.getSize().getWidth(); col++){
            for(int row = 0; row < super.getSize().getHeight(); row++){
                if( !cellRange.isExcluded(row, col) ){
                    CellRef srcCell = new CellRef(sheetName, offsetRow + row, startCol + col);
                    CellData cellData = super.getTransformer().getCellData(srcCell);
                    if( cellData != null && cellData.isFormulaCell() ){
                        cellData.addTargetParentAreaRef( newAreaRef );
                    }
                }
            }
        }
    }

    private int transformTopStaticArea(CellRef cellRef, Context context) {
        String sheetName = super.getStartCellRef().getSheetName();
        int startRow = super.getStartCellRef().getRow();
        int startCol = super.getStartCellRef().getCol();
        int topStaticAreaLastRow = findRelativeTopCommandRow() - 1;
        for(int col = 0; col < super.getSize().getWidth(); col++){
            for(int row = 0; row <= topStaticAreaLastRow; row++){
                if( !cellRange.isExcluded(row, col) ){
                    CellRef relativeCell = cellRange.getCell(row, col);
                    CellRef srcCell = new CellRef(sheetName, startRow + row, startCol + col);
                    CellRef targetCell = new CellRef(cellRef.getSheetName(), relativeCell.getRow() + cellRef.getRow(), relativeCell.getCol() + cellRef.getCol());
                    fireBeforeTransformCell(srcCell, targetCell, context);
                    try{
                        updateCellDataArea(srcCell, targetCell);
                        super.getTransformer().transform(srcCell, targetCell, context);
                    }catch(Exception e){
                        logger.error("Failed to transform " + srcCell + " into " + targetCell, e);
                    }
                    fireAfterTransformCell(srcCell, targetCell, context);
                }
            }
        }
        return topStaticAreaLastRow;
    }

    private int findRelativeTopCommandRow() {
        int topCommandRow = super.getStartCellRef().getRow() + super.getSize().getHeight() - 1;
        for(CommandData data : super.getCommandDataList()){
            topCommandRow = Math.min(data.getStartCellRef().getRow(), topCommandRow);
        }
        return topCommandRow -  super.getStartCellRef().getRow();
    }

    private void fireBeforeApplyEvent(CellRef cellRef, Context context) {
        for (AreaListener areaListener : super.getAreaListeners()) {
            areaListener.beforeApplyAtCell(cellRef, context);
        }
    }

    private void fireAfterApplyEvent(CellRef cellRef, Context context) {
        for (AreaListener areaListener : super.getAreaListeners()) {
            areaListener.afterApplyAtCell(cellRef, context);
        }
    }




    private void transformStaticCells(CellRef cellRef, Context context, int relativeStartRow) {
        String sheetName = super.getStartCellRef().getSheetName();
        int offsetRow = super.getStartCellRef().getRow();
        int startCol = super.getStartCellRef().getCol();
        for(int col = 0; col < super.getSize().getWidth(); col++){
            for(int row = relativeStartRow; row < super.getSize().getHeight(); row++){
                if( !cellRange.isExcluded(row, col) ){
                    CellRef relativeCell = cellRange.getCell(row, col);
                    CellRef srcCell = new CellRef(sheetName, offsetRow + row, startCol + col);
                    CellRef targetCell = new CellRef(cellRef.getSheetName(), relativeCell.getRow() + cellRef.getRow(), relativeCell.getCol() + cellRef.getCol());
                    fireBeforeTransformCell(srcCell, targetCell, context);
                    try{
                        updateCellDataArea(srcCell, targetCell);
                        super.getTransformer().transform(srcCell, targetCell, context);
                    }catch(Exception e){
                        logger.error("Failed to transform " + srcCell + " into " + targetCell, e);
                    }
                    fireAfterTransformCell(srcCell, targetCell, context);
                }
            }
        }
    }

    private void updateCellDataArea(CellRef srcCell, CellRef targetCell) {
        CellData cellData = super.getTransformer().getCellData(srcCell);
        if( cellData != null ) {
            cellData.setArea(this);
            cellData.addTargetPos(targetCell);
        }
    }

    private void fireBeforeTransformCell(CellRef srcCell, CellRef targetCell, Context context) {
        for (AreaListener areaListener : super.getAreaListeners()) {
            areaListener.beforeTransformCell(srcCell, targetCell, context);
        }
    }

    private void fireAfterTransformCell(CellRef srcCell, CellRef targetCell, Context context) {
        for (AreaListener areaListener : super.getAreaListeners()) {
            areaListener.afterTransformCell(srcCell, targetCell, context);
        }
    }


}