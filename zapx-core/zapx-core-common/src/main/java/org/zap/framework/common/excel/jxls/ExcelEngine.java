package org.zap.framework.common.excel.jxls;

import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.formula.FastFormulaProcessor;
import org.jxls.formula.StandardFormulaProcessor;
import org.springframework.web.context.ContextLoader;
import org.zap.framework.exception.BusinessException;
import org.zap.framework.util.FileToolUtils;

import java.io.*;
import java.util.List;

public class ExcelEngine {

	private static boolean useFastFormulaProcessor = true;
    private static boolean processFormulas = true;
   

    //自定义类
	public static void processTemplateByMyHandle(String input, String output, Context context, String handleclass_name) throws Exception {
		IBaseJxls iBaseJxls = (IBaseJxls) ContextLoader.getCurrentWebApplicationContext().getBean(handleclass_name);
		processTemplate(input,output,iBaseJxls.processContext(context));
	}

	public static void processMultiSheetTemplate(InputStream templateStream, OutputStream targetStream, Context context) {

		try {

			MyPoiTransformer transformer = MyPoiTransformer.createTransformer(templateStream, targetStream);
			AreaBuilder areaBuilder = new MyXlsCommentAreaBuilder(transformer);
			addMyCommand();// 方便拓展
			List<Area> xlsAreaList = areaBuilder.build();
			if (xlsAreaList.isEmpty()) {
				throw new BusinessException("没有设置模板");
			}


			Area xlsArea = xlsAreaList.get(0);
			String sourceSheetName = xlsArea.getStartCellRef().getSheetName();

			//是否输出到另外一个页签
			xlsArea.applyAt(new CellRef(sourceSheetName + "!A1"), context);

		} catch (Exception ex) {
			throw new BusinessException("", ex);
		}

	}

	/**
	 * 导出报表动态列
	 * @param templateStream
	 * @param targetStream
	 * @param context
     */
	public static void processDynamicColumnTemplate(InputStream templateStream, OutputStream targetStream, Context context) {

		try {
			MyPoiTransformer transformer = MyPoiTransformer.createTransformer(templateStream, targetStream);
			AreaBuilder areaBuilder = new MyXlsCommentAreaBuilder(transformer);
			addMyCommand();// 方便拓展
			List<Area> xlsAreaList = areaBuilder.build();
			if (xlsAreaList.isEmpty()) {
				throw new BusinessException("没有设置模板");
			}
			Area xlsArea = xlsAreaList.get(0);
			String sourceSheetName = xlsArea.getStartCellRef().getSheetName();

			DynamicGridCommand gridCommand = (DynamicGridCommand) xlsArea.getCommandDataList().get(0).getCommand();
			gridCommand.setProps((String)context.getVar("props"));

			//是否输出到另外一个页签
			xlsArea.applyAt(new CellRef(sourceSheetName + "!A1"), context);

			if (processFormulas) {
				//使用动态列公式处理器
				xlsArea.setFormulaProcessor(new DynamicFormulaProcessor(context));
				xlsArea.processFormulas();
			}

			transformer.write();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new BusinessException("", ex);
		}

	}

	/**
	 * 输出报表
	 * @param templateStream
	 * @param targetStream
	 * @param context
     */
	public static void processTemplate(InputStream templateStream, OutputStream targetStream, Context context) {

		try {
			MyPoiTransformer transformer = MyPoiTransformer.createTransformer(templateStream, targetStream);
			AreaBuilder areaBuilder = new MyXlsCommentAreaBuilder(transformer);
			addMyCommand();// 方便拓展
			List<Area> xlsAreaList = areaBuilder.build();
			if (xlsAreaList.isEmpty()) {
				throw new BusinessException("没有设置模板");
			}
//		String fileName = outFile.getName().substring(0, outFile.getName().lastIndexOf("."));
			Area xlsArea = xlsAreaList.get(0);
			String sourceSheetName = xlsArea.getStartCellRef().getSheetName();

			//是否输出到另外一个页签
			xlsArea.applyAt(new CellRef(sourceSheetName + "!A1"), context);

			if (processFormulas) {
				setFormulaProcessor(xlsArea);
				xlsArea.processFormulas();
			}

//		if (!fileName.equals(sourceSheetName)) {
//			if (transformer.getWorkbook().getSheet(sourceSheetName) != null) {
//				transformer.changeSheetName(sourceSheetName, fileName); //修改sheet名称
//			}
//		}
			transformer.write();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new BusinessException("", ex);
		}
//		Transformer transformer = createTransformer(templateStream, targetStream);
//		areaBuilder.setTransformer(transformer);
//		List<Area> xlsAreaList = areaBuilder.build();
//		for (Area xlsArea : xlsAreaList) {
//			xlsArea.applyAt(
//					new CellRef(xlsArea.getStartCellRef().getCellName()), context);
//			if (processFormulas) {
//				setFormulaProcessor(xlsArea);
//				xlsArea.processFormulas();
//			}
//		}
//		transformer.write();

	}

    //生成报表
	public static void processTemplate(String input, String output, Context context) throws Exception {
		InputStream is = null;
		OutputStream os = null;
		try {			
	        File tempFile = new File(FileToolUtils.getValidateFilePath(input));
			if(!tempFile.exists()){ 
				throw new BusinessException("找不到模板文件");
			}
	        File outFile = new File(FileToolUtils.getValidateFilePath(output));
			if(outFile.exists()){ 
				outFile.delete();
			}
	        FileToolUtils.insureFileExists(outFile);
			
			is = new FileInputStream(tempFile);
			os = new FileOutputStream(output);
	
			MyPoiTransformer transformer = MyPoiTransformer.createTransformer(is,os);
			AreaBuilder areaBuilder = new MyXlsCommentAreaBuilder(transformer);
			addMyCommand();// 方便拓展
			List<Area> xlsAreaList = areaBuilder.build();
	        if( xlsAreaList.isEmpty() ){
	           throw new BusinessException("没有设置模板");
	        }
	        String fileName = outFile.getName().substring(0,outFile.getName().lastIndexOf("."));
	        Area xlsArea = (MyXlsArea)xlsAreaList.get(0);
	        String sourceSheetName = xlsArea.getStartCellRef().getSheetName();
	                
	        //是否输出到另外一个页签
	        xlsArea.applyAt( new CellRef(sourceSheetName+"!A1"), context);
	        
	        if( processFormulas ){
	           setFormulaProcessor(xlsArea);
	           xlsArea.processFormulas();
	        }

	        if(!fileName.equals(sourceSheetName)){
	        	if(transformer.getWorkbook().getSheet(sourceSheetName)!=null){
		        	  transformer.changeSheetName(sourceSheetName,fileName); //修改sheet名称
		        }
	        }
	        transformer.write();
	        
		} catch (Exception e) {
			 e.printStackTrace();
			 throw new Exception(e.getMessage());
		} finally {
			if(is!=null){
				is.close();
				is = null ;
			}
			if(os!=null){
				os.flush();
				os.close();
				os = null ;
			}
		}

	}

	public static void addMyCommand() throws Exception {
		MyXlsCommentAreaBuilder.addCommandMapping("eachImage", EachImageCommand.class);
		MyXlsCommentAreaBuilder.addCommandMapping("groupRow",GroupRowCommand.class);
		MyXlsCommentAreaBuilder.addCommandMapping("combineRow",CombineRowCommand.class);
		MyXlsCommentAreaBuilder.addCommandMapping("combineCol",CombineColCommand.class);
		MyXlsCommentAreaBuilder.addCommandMapping("eachGroup",EachGroupCommand.class);
		MyXlsCommentAreaBuilder.addCommandMapping("eachGrid",EachGridCommand.class);
		MyXlsCommentAreaBuilder.addCommandMapping("gridHeader",GridHeaderCommand.class);
		MyXlsCommentAreaBuilder.addCommandMapping("gridData",GridDataCommand.class);
		MyXlsCommentAreaBuilder.addCommandMapping("dynamicGrid",DynamicGridCommand.class);

	}

	private static void setFormulaProcessor(Area xlsArea) {
		if (useFastFormulaProcessor) {
			xlsArea.setFormulaProcessor(new FastFormulaProcessor());
		} else {
			xlsArea.setFormulaProcessor(new StandardFormulaProcessor());
		}
	}
}
