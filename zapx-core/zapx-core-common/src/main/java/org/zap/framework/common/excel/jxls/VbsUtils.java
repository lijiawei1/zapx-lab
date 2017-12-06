package org.zap.framework.common.excel.jxls;

import org.zap.framework.exception.BusinessException;
import org.zap.framework.util.FileToolUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class VbsUtils {

	public static String processName  = "wscript" ;
	public static long timeOutMinutes  = 150*1000l;//300秒
	
	public static String combineExcels(String outFilePath,List<String> paths) throws BusinessException {

		String combinePath = "";
		try {
			//--动态生成VBS文件。然后调用这些VBS文件，打开并保存excel文件
			if(paths!=null&&paths.size()>0)
			{		
				File outFile = new File(FileToolUtils.getValidateFilePath(outFilePath));
				if(outFile.exists()){
					outFile.delete();
				}
				String outPath = outFile.getAbsolutePath();
				
				String filePath = outFile.getParent();//父路径
				String fileName = outFile.getName().substring(0,outFile.getName().lastIndexOf("."));// 文件名称
				String vbsFileName = filePath+"//"+fileName+".vbs";
				File vbsFile = new File(FileToolUtils.getValidateFilePath(vbsFileName));
				if(vbsFile.exists()){
					vbsFile.delete();
				}
				
				vbsFile.createNewFile();
				writerLine(vbsFileName," on error resume next");
				writerLine(vbsFileName," Dim objExcel ");
				writerLine(vbsFileName," Set objExcel = CreateObject(\"Excel.Application\")");
				writerLine(vbsFileName," objExcel.Visible = False ");
				writerLine(vbsFileName," objExcel.DisplayAlerts = False ");
				writerLine(vbsFileName," Dim objDesWorkbook ");
				writerLine(vbsFileName," Set objDesWorkbook = objExcel.Workbooks.Add '创建空文件 ");
				writerLine(vbsFileName," Do While objDesWorkbook.Worksheets.Count > 1  ");
				writerLine(vbsFileName,"          objDesWorkbook.Worksheets(objDesWorkbook.Worksheets.Count).Delete ");
				writerLine(vbsFileName," Loop ");		
				
				int index = 0;
				for(String path : paths ){
					index++;
					File file2 = new File(FileToolUtils.getValidateFilePath(path));
					if(!file2.exists()){ 
						continue;//把校验文件存在挪到了最外层
					}
					if(index==1){
						writerLine(vbsFileName," Dim objSrcWorkbook "); //为了避免名称引用变量名称重复
					}
					writerLine(vbsFileName," Set objSrcWorkbook = objExcel.Workbooks.Open (\""+file2.getAbsolutePath()+"\")");
					writerLine(vbsFileName," For i = 1 To objSrcWorkbook.Worksheets.Count ");
					writerLine(vbsFileName," objSrcWorkbook.Worksheets(i).Copy objDesWorkbook.Worksheets(objDesWorkbook.Worksheets.Count)");
					writerLine(vbsFileName," Next ");
					writerLine(vbsFileName," objSrcWorkbook.Close  ");
					writerLine(vbsFileName," Set objSrcWorkbook = Nothing ");
				}
				writerLine(vbsFileName," For Each sh In objDesWorkbook.Worksheets  ");
				writerLine(vbsFileName,"   If sh.Name = \"Sheet1\" Then ");
				writerLine(vbsFileName,"      sh.Delete ");
				writerLine(vbsFileName,"   End If");
				writerLine(vbsFileName," Next ");
				writerLine(vbsFileName," objDesWorkbook.Worksheets(1).Activate ");
				writerLine(vbsFileName," objDesWorkbook.SaveAs \""+outPath+"\" ,-4143 "); //保存
				writerLine(vbsFileName," objDesWorkbook.Close 1");
				writerLine(vbsFileName," Set objDesWorkbook = Nothing ");
				writerLine(vbsFileName," Set objExcel = Nothing ");

				//执行VBS命令
				executeCommand(new String[]{processName, vbsFileName},timeOutMinutes);
				combinePath = outFilePath;
			}
		} catch (Exception e) {
			e.printStackTrace();
			combinePath = "";
			throw new BusinessException("合并Excel异常，请联系IT!");
		}
		return combinePath;	

	}
	

	/**
     * 运行一个外部命令，返回状态.若超过指定的超时时间，抛出TimeoutException
     * @param command
     * @param timeout
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public static int executeCommand(final String[] command, final long timeout) throws IOException, InterruptedException, TimeoutException {
        Process process = Runtime.getRuntime().exec(command);
        Worker worker = new Worker(process);
        worker.start();
        try {
            worker.join(timeout);
            if (worker.exit != null){
                return worker.exit;
            } else{
                throw new TimeoutException();
            }
        } catch (InterruptedException ex) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw ex;
        } finally {
            process.destroy();
        }
    }
     
  
    private static class Worker extends Thread {
        private final Process process;
        private Integer exit;
  
        private Worker(Process process) {
            this.process = process;
        }
  
        public void run() {
            try {
                exit = process.waitFor();
            } catch (InterruptedException ignore) {
                return;
            }
        }
    }

	
	public static void writerLine(String path, String contents) {
		try {			
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(path, true),"GB2312");
			osw.write(contents);
			osw.write("\r\n");		
			osw.flush();
			osw.close();
		} catch (IOException ioe) {
		}
	}
}
