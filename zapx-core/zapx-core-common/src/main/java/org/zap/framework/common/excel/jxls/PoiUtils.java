package org.zap.framework.common.excel.jxls;

import org.apache.poi.ss.usermodel.*;
import org.jxls.builder.xls.XlsCommentAreaBuilder;

public class PoiUtils {
	
	
	 public static int getMaxColoumNum(Sheet sheet) {		
		int maxColoumNum = 0;
	    int numberOfRows = sheet.getLastRowNum() + 1;
	    for(int i = 0; i < numberOfRows; i++){
	    	if(sheet.getRow(i)!=null){
		    	int coloumNum = sheet.getRow(i).getLastCellNum();
		    	if(coloumNum>maxColoumNum){
		    		maxColoumNum=coloumNum;
		    	}
	    	}
	    }
		return maxColoumNum;
	 }
	 
	 public static void copySheetProperties(Sheet src, Sheet dest) {
		 dest.setAutobreaks(src.getAutobreaks());
		 dest.setDisplayGuts(src.getDisplayGuts());
		 dest.setDisplayZeros(src.isDisplayZeros());
		 dest.setFitToPage(src.getFitToPage());
		 dest.setRowSumsRight(src.getRowSumsRight());
		 dest.setRowSumsBelow(src.getRowSumsBelow());
		 dest.setDisplayGridlines(true);
		 dest.setDisplayFormulas(src.isDisplayFormulas());
		 dest.setSelected(src.isSelected());
		 dest.setDisplayRowColHeadings(src.isDisplayRowColHeadings());
		 dest.setVerticallyCenter(src.getVerticallyCenter());
		 dest.setHorizontallyCenter(src.getHorizontallyCenter());
		 dest.setForceFormulaRecalculation(src.getForceFormulaRecalculation());
		 copyPrintSetup(src, dest);
	 }

	 
	  private static void copyPrintSetup(Sheet src, Sheet dest) {
		  PrintSetup srcPrintSetup = src.getPrintSetup();
		  PrintSetup destPrintSetup = dest.getPrintSetup();
		  destPrintSetup.setCopies(srcPrintSetup.getCopies());
		  destPrintSetup.setDraft(srcPrintSetup.getDraft());
		  destPrintSetup.setFitHeight(srcPrintSetup.getFitHeight());
		  destPrintSetup.setFitWidth(srcPrintSetup.getFitWidth());
		  destPrintSetup.setFooterMargin(srcPrintSetup.getFooterMargin());
		  destPrintSetup.setHeaderMargin(srcPrintSetup.getHeaderMargin());
		  destPrintSetup.setHResolution(srcPrintSetup.getHResolution());
		  destPrintSetup.setLandscape(srcPrintSetup.getLandscape());
		  destPrintSetup.setLeftToRight(srcPrintSetup.getLeftToRight());
		  destPrintSetup.setNoColor(srcPrintSetup.getNoColor());
		  destPrintSetup.setNoOrientation(srcPrintSetup.getNoOrientation());
		  destPrintSetup.setNotes(srcPrintSetup.getNotes());
		  destPrintSetup.setPageStart(srcPrintSetup.getPageStart());
		  destPrintSetup.setPaperSize(srcPrintSetup.getPaperSize());
		  destPrintSetup.setScale(srcPrintSetup.getScale());
		  destPrintSetup.setUsePage(srcPrintSetup.getUsePage());
		  destPrintSetup.setValidSettings(srcPrintSetup.getValidSettings());
		  destPrintSetup.setVResolution(srcPrintSetup.getVResolution());
	  }	  
	  
	  public static void setCellComment(Cell cell, String commentText, String commentAuthor, ClientAnchor anchor){
	        Sheet sheet = cell.getSheet();
	        Workbook wb = sheet.getWorkbook();
	        Drawing drawing = sheet.createDrawingPatriarch();
	        CreationHelper factory = wb.getCreationHelper();
	        if( anchor == null ){
	            anchor = factory.createClientAnchor();
	            anchor.setCol1(cell.getColumnIndex() + 1);
	            anchor.setCol2(cell.getColumnIndex() + 3);
	            anchor.setRow1(cell.getRowIndex());
	            anchor.setRow2(cell.getRowIndex() + 2);
	        }
	        Comment comment = drawing.createCellComment(anchor);
	        comment.setString(factory.createRichTextString(commentText));
	        comment.setAuthor(commentAuthor != null ? commentAuthor : "");
	        cell.setCellComment( comment );
	    }

	    public WritableCellValue hyperlink(String address, String link, String linkTypeString){
	        return new WritableHyperlink(address, link, linkTypeString);
	    }

	    public WritableCellValue hyperlink(String address, String title){
	        return new WritableHyperlink(address, title);
	    }



	    public static boolean isJxComment(String cellComment) {
	        if(cellComment == null ) return false;
	        String[] commentLines = cellComment.split("\\n");
	        for (String commentLine : commentLines) {
	            if( (commentLine != null) && XlsCommentAreaBuilder.isCommandString( commentLine.trim() ) ){
	                return true;
	            }
	        }
	        return false;
	    }
	    
	    
	    public static String getCellContents(Cell cell) {
	    	String cellValue =null;
	        switch( cell.getCellType() ){
	            case Cell.CELL_TYPE_STRING:
	                cellValue = cell.getRichStringCellValue()==null?null:cell.getRichStringCellValue().getString();
	                break;
	            case Cell.CELL_TYPE_BOOLEAN:
	                cellValue = String.valueOf(cell.getBooleanCellValue());
	                break;
	            case Cell.CELL_TYPE_NUMERIC:
	                if(DateUtil.isCellDateFormatted(cell)) {
	                    cellValue = cell.getDateCellValue()==null?null:String.valueOf(cell.getDateCellValue());
	                } else {
	                    cellValue = String.valueOf(cell.getNumericCellValue());
	                }
	                break;
	            case Cell.CELL_TYPE_FORMULA:
	                cellValue = cell.getCellFormula()==null?null:String.valueOf(cell.getCellFormula());
	                break;
	            case Cell.CELL_TYPE_BLANK:
	                cellValue = null;
	                break;
	        }
	         return cellValue;
	    }
	    
	    
	    
	    
}
