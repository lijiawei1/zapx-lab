package org.zap.framework.common.excel.jxls;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jxls.common.*;
import org.jxls.transform.AbstractTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


//自定义处理器解决一些BUG
public class MyPoiTransformer extends AbstractTransformer {

	static Logger logger = LoggerFactory.getLogger(MyPoiTransformer.class);
	public static final int MAX_COLUMN_TO_READ_COMMENT = 50;
	public Workbook workbook;
	private OutputStream outputStream;
	private InputStream inputStream;

	public MyPoiTransformer(Workbook workbook) {
	        this.workbook = workbook;
    }
	   
    public static MyPoiTransformer createTransformer(InputStream is, OutputStream os) throws Exception {
    	MyPoiTransformer transformer = createTransformer(is);
        transformer.outputStream = os;
        transformer.inputStream = is;
        return transformer;
    }
    
    public static MyPoiTransformer createTransformer(InputStream is) throws Exception {
        Workbook workbook = WorkbookFactory.create(is);
        return createTransformer(workbook);
    }

    public static MyPoiTransformer createTransformer(Workbook workbook,OutputStream os) {
    	MyPoiTransformer transformer = createTransformer(workbook);
    	transformer.outputStream = os;
        return transformer;
    }
    
    public static MyPoiTransformer createTransformer(Workbook workbook) {
    	MyPoiTransformer transformer = new MyPoiTransformer(workbook);
        transformer.readCellData();
        return transformer;
    }
   

	public Workbook getWorkbook() {
		return workbook;
	}

	public void readCellData(){
		int numberOfSheets = workbook.getNumberOfSheets();
		for(int i = 0; i < numberOfSheets; i++){
			Sheet sheet = workbook.getSheetAt(i);
			SheetData sheetData = PoiSheetData.createSheetData(sheet, this);
			sheetMap.put(sheetData.getSheetName(), sheetData);
		}
	}
	

	
	@Override
	public void transform(CellRef srcCellRef, CellRef targetCellRef, Context context) {
		CellData cellData = this.getCellData(srcCellRef);
		if(cellData != null){
			if(targetCellRef == null || targetCellRef.getSheetName() == null){
				return;
			}
			Sheet destSheet = workbook.getSheet(targetCellRef.getSheetName());
			if(destSheet == null){
				destSheet = workbook.createSheet(targetCellRef.getSheetName());
				PoiUtils.copySheetProperties(workbook.getSheet(srcCellRef.getSheetName()), destSheet);
			}
			SheetData sheetData = sheetMap.get(srcCellRef.getSheetName());
			if(!isIgnoreColumnProps()){
				destSheet.setColumnWidth(targetCellRef.getCol(), sheetData.getColumnWidth(srcCellRef.getCol()));
			}
			
			Row destRow = destSheet.getRow(targetCellRef.getRow());
			if (destRow == null) {
				destRow = destSheet.createRow(targetCellRef.getRow());
			}
			if(!isIgnoreRowProps()){
				destSheet.getRow(targetCellRef.getRow()).setHeight((short) sheetData.getRowData(srcCellRef.getRow()).getHeight());
			}
			Cell destCell = destRow.getCell(targetCellRef.getCol());
			if (destCell == null) {
				destCell = destRow.createCell(targetCellRef.getCol());
			}
			try{
				destCell.setCellType(Cell.CELL_TYPE_BLANK);
				((PoiCellData)cellData).writeToCell(destCell, context, this);
				copyMergedRegions(cellData, targetCellRef);
			}catch(Exception e){
				logger.error("Failed to write a cell with " + cellData + " and " + context, e);
			}
		}
	}


	private void copyMergedRegions(CellData sourceCellData, CellRef destCell) {
		if(sourceCellData.getSheetName() == null ){ throw new IllegalArgumentException("Sheet name is null in copyMergedRegions");}
		PoiSheetData sheetData = (PoiSheetData)sheetMap.get( sourceCellData.getSheetName() );
		CellRangeAddress cellMergedRegion = null;
		for (CellRangeAddress mergedRegion : sheetData.getMergedRegions()) {
			CellRef souceRef  = sourceCellData.getCellRef() ;
			if(mergedRegion.getFirstRow() == souceRef.getRow() && mergedRegion.getFirstColumn() == souceRef.getCol()){
				cellMergedRegion = mergedRegion;
				break;
			}
		}
		if( cellMergedRegion != null){
			findAndRemoveExistingCellRegion(destCell);
			Sheet destSheet = workbook.getSheet(destCell.getSheetName());
			destSheet.addMergedRegion(new CellRangeAddress(destCell.getRow(), destCell.getRow() + cellMergedRegion.getLastRow() - cellMergedRegion.getFirstRow(),
			destCell.getCol(), destCell.getCol() + cellMergedRegion.getLastColumn() - cellMergedRegion.getFirstColumn()));
		}
	}

	private void findAndRemoveExistingCellRegion(CellRef cellRef) {
		Sheet destSheet = workbook.getSheet(cellRef.getSheetName());
		int numMergedRegions = destSheet.getNumMergedRegions();
		for(int i = 0; i < numMergedRegions; i++){
			CellRangeAddress mergedRegion = destSheet.getMergedRegion(i);
			if( mergedRegion.getFirstRow() <= cellRef.getRow() && mergedRegion.getLastRow() >= cellRef.getRow() &&
				mergedRegion.getFirstColumn() <= cellRef.getCol() && mergedRegion.getLastColumn() >= cellRef.getCol() ){
				destSheet.removeMergedRegion(i);
				break;
			}
		}
	}


	public void setFormula(CellRef cellRef, String formulaString) {
		if(cellRef == null || cellRef.getSheetName() == null ) return;
		Sheet sheet = workbook.getSheet(cellRef.getSheetName());
		if( sheet == null){
			sheet = workbook.createSheet(cellRef.getSheetName());
		}
		Row row = sheet.getRow(cellRef.getRow());
		if( row == null ){
			row = sheet.createRow(cellRef.getRow());
		}
		Cell poiCell = row.getCell(cellRef.getCol());
		if( poiCell == null ){
			poiCell = row.createCell(cellRef.getCol());
		}
		try{
			poiCell.setCellFormula( formulaString );
		}catch (Exception e){
			logger.error("Failed to set formula = " + formulaString + " into cell = " + cellRef.getCellName(), e);
		}
	}

	public void clearCell(CellRef cellRef) {
		if(cellRef == null || cellRef.getSheetName() == null ) return;
		Sheet sheet = workbook.getSheet(cellRef.getSheetName());
		if( sheet == null ) return;
		removeCellComment(sheet, cellRef.getRow(), cellRef.getCol());
		Row row = sheet.getRow(cellRef.getRow());
		if( row == null ) return;
		Cell cell = row.getCell(cellRef.getCol());
		if ( cell == null ) {
			if ( sheet.getCellComment(cellRef.getRow(), cellRef.getCol()) != null ){
				cell = row.createCell(cellRef.getCol());
				cell.removeCellComment();
			}
			return;
		}
		cell.setCellType(Cell.CELL_TYPE_BLANK);
		cell.setCellStyle(workbook.getCellStyleAt((short) 0));
		if( cell.getCellComment() != null ){
			cell.removeCellComment();
		}
		findAndRemoveExistingCellRegion(cellRef);
	}

	private void removeCellComment(Sheet sheet, int row, int col) {
		Comment comment = sheet.getCellComment(row, col);
		
	}

	public List<CellData> getCommentedCells() {
		List<CellData> commentedCells = new ArrayList<CellData>();
		for (SheetData sheetData : sheetMap.values()) {
			for (RowData rowData : sheetData) {
				if(rowData == null ) continue;
				for(CellData cellData : rowData) {
					if(cellData != null && cellData.getCellComment() != null ){
						commentedCells.add(cellData);
					}
				}
				if( rowData.getNumberOfCells() == 0 ){
					List<CellData> commentedCellData = readCommentsFromSheet(((PoiSheetData)sheetData).getSheet(), ((PoiRowData)rowData).getRow());
					commentedCells.addAll( commentedCellData );
				}
			}
		}
		return commentedCells;
	}

	private void addImage(AreaRef areaRef, int imageIdx) {
		CreationHelper helper = workbook.getCreationHelper();
		Sheet sheet = workbook.getSheet(areaRef.getSheetName());
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(areaRef.getFirstCellRef().getCol());
		anchor.setRow1(areaRef.getFirstCellRef().getRow());
		anchor.setCol2(areaRef.getLastCellRef().getCol());
		anchor.setRow2(areaRef.getLastCellRef().getRow());
		drawing.createPicture(anchor, imageIdx);
	}

	public void addImage(AreaRef areaRef, byte[] imageBytes, ImageType imageType) {
		int poiPictureType = findPoiPictureTypeByImageType(imageType);
		int pictureIdx = workbook.addPicture(imageBytes, poiPictureType);
		addImage(areaRef, pictureIdx);
	}

	public void write() throws IOException {
		if( outputStream == null ){
			throw new IllegalStateException("Cannot write a workbook with an uninitialized output stream");
		}
		if( workbook == null ){
			throw new IllegalStateException("Cannot write an uninitialized workbook");
		}
		workbook.write(outputStream);
		outputStream.close();
	}


	private int findPoiPictureTypeByImageType(ImageType imageType){
		int poiType = -1;
		if( imageType == null ){
			throw new IllegalArgumentException("Image type is undefined");
		}
		switch (imageType){
		case PNG:
			poiType = Workbook.PICTURE_TYPE_PNG;
			break;
		case JPEG:
			poiType = Workbook.PICTURE_TYPE_JPEG;
			break;
		case EMF:
			poiType = Workbook.PICTURE_TYPE_EMF;
			break;
		case WMF:
			poiType = Workbook.PICTURE_TYPE_WMF;
			break;
		case DIB:
			poiType = Workbook.PICTURE_TYPE_DIB;
			break;
		case PICT:
			poiType = Workbook.PICTURE_TYPE_PICT;
			break;
		}
		return poiType;
	}

	private List<CellData> readCommentsFromSheet(Sheet sheet, Row row) {
		List<CellData> commentDataCells = new ArrayList<CellData>();
		int rowNum = row.getRowNum();
		for(int i = 0; i <= MAX_COLUMN_TO_READ_COMMENT; i++){
			Comment comment = sheet.getCellComment(rowNum, i);
			if( comment != null && comment.getString() != null ){
				CellData cellData = new CellData( new CellRef(sheet.getSheetName(), rowNum, i) );
				cellData.setCellComment( comment.getString().getString() );
				commentDataCells.add( cellData );
			}
		}
		return commentDataCells;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void changeSheetName(String oldSheetName,String newSheetName) {
		int sheetIndex = workbook.getSheetIndex(oldSheetName);
		workbook.setSheetName(sheetIndex,newSheetName);
	}


	@Override
	public void deleteSheet(String sheetName) {
		int sheetIndex = workbook.getSheetIndex(sheetName);
		workbook.removeSheetAt(sheetIndex);
	}

	@Override
	public void setHidden(String sheetName, boolean hidden) {
		int sheetIndex = workbook.getSheetIndex(sheetName);
		workbook.setSheetHidden(sheetIndex, hidden);
	}
}
