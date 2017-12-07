package org.zap.framework.common.excel.parser;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.zap.framework.common.config.DeployConfig;
import org.zap.framework.util.FileToolUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Shin on 2017/5/9.
 */
public class ExcelImportEngine<T> {

    Logger log = LoggerFactory.getLogger(ExcelImportEngine.class);

    HttpServletRequest request;

    HttpServletResponse response;

    ExcelImportRequest config;

    ExcelBusiHandler busiHandler;

    Class<T> clazz;

    /**
     * 处理结果开始行
     */
    int resultRow = 1;

    /**
     * 处理结果所在列
     */
    int resultColumn = 0;

    private ExcelImportEngine(Class<T> clazz, ExcelImportRequest config, HttpServletRequest request, HttpServletResponse response, ExcelBusiHandler busiHandler) {
        this.clazz = clazz;
        this.request = request;
        this.response = response;
        this.config = config;
        this.busiHandler = busiHandler;

        this.resultColumn = this.config.getMeta() == null ? 0 : this.config.getMeta().size() + 1;
    }

    public static <T> ExcelResult<T> work(Class<T> clazz, ExcelImportRequest config, HttpServletRequest request, HttpServletResponse response, ExcelBusiHandler busiHandler) {
        return new ExcelImportEngine<>(clazz, config, request, response, busiHandler).doWork();
    }

    /**
     * 开始工作
     */
    private ExcelResult<T> doWork() {

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        // 获得上传的文件：
        List<MultipartFile> files = multipartRequest.getFiles(config.getName());

        //头部起始行
        int header_row = config.getHeader_row();

        //数据起始行
        int data_start_row = config.getData_start_row();

        if (files != null && files.size() > 0) {

            MultipartFile file = files.get(0);

            ExcelHandler handler;
            try {
                handler = new ExcelHandler(file.getInputStream(), Version.of(FilenameUtils.getExtension(file.getOriginalFilename())));
            } catch (IOException e) {
                log.error("上传Excel文件失败", e);
                return new ExcelResult<>("上传Excel文件失败", true);
            }

            if (handler.getSheetCount() <= 0) {
                return new ExcelResult<>("空模板", true);
            }

            //模板头部
            ExcelRange headerRange = ExcelRange.sheetIndex(0).rowRange(header_row, header_row).columnRange(0, resultColumn);
            //模板数据
            ExcelRangeData contentRangeData = handler.readRange(ExcelRange.sheetIndex(0).columnRange(0, resultColumn));

            ExcelCell topLeftCell = ExcelCell.sheetIndex(0).row(resultRow + header_row).column(resultColumn);

            try {

                //检查模板合法性
                Map<String, Integer> namesMap = ExcelUtils.parseAllHeaderName(handler.readRange(headerRange));

                if (config.isOnly_contain()) {
                  //只要包含
                    String collect = namesMap.keySet().stream()
                            .filter(key -> StringUtils.isNotBlank(key) && !config.getMeta().containsKey(key))
                            .collect(Collectors.joining(","));

                    if (StringUtils.isNotBlank(collect))
                        return new ExcelResult<>("无法匹配字段：" + collect, true);

                    topLeftCell.column(namesMap.size());

                } else {

                    //全部必需
                    String lessFields = config.getMeta().keySet().stream().filter(key -> !namesMap.containsKey(key)).collect(Collectors.joining(","));
                    if (StringUtils.isNotBlank(lessFields))
                        return new ExcelResult<>("模板格式错误，缺少以下字段：" + lessFields, true);
                }

                //解析头部数据
                Map<String, Integer> headersMap = ExcelUtils.parseHeader(handler.readRange(headerRange), config.getMeta());
                ExcelResult<T> excelResult = ExcelUtils.toList(data_start_row, contentRangeData, clazz(), headersMap);

                //输出数据解释阶段的错误信息
                if (excelResult.isError()) {
                    //计算处理结果行
                    excelResult.setMessage("模板数据解析失败：" + excelResult.getDetailMsg());
                    excelResult.setUrl(writeContent(excelResult, handler, topLeftCell, "解析成功"));
                    return excelResult;
                }

                //进入业务分析阶段
                ExcelBusiHolder<T> holder = new ExcelBusiHolder<>();
                busiHandler.checkBusiData(excelResult, getConfig(), holder);

                if (excelResult.isError()) {
                    //计算处理结果行
                    excelResult.setMessage("业务校验不通过：" + excelResult.getDetailMsg());
                    excelResult.setUrl(writeContent(excelResult, handler, topLeftCell, "校验通过"));

                    //忽略错误更新数据
                    if (config.isIgnore_error()) {
                        busiHandler.updateData(getConfig(), holder);
                        excelResult.setMessage("导入记录[" + excelResult.getData().size() + "]条。其中" + excelResult.getMessage());
                    }

                    return excelResult;
                } else {
                    //写入结果
                    writeContent(excelResult, handler, topLeftCell, "成功");
                }

                //更新数据
                busiHandler.updateData(getConfig(), holder);

                excelResult.setMessage("导入成功");
                return excelResult;

            } catch (Exception ex) {
                log.error("EXCEL解析失败", ex);
                return new ExcelResult("EXCEL解析失败：" + ex.getMessage(), true);
            }
        }

        return new ExcelResult("导入成功");
    }

    /**
     * 写入内容
     *
     * @param excelResult
     * @param handler
     * @param topLeftCell
     * @param nullMsg
     * @return
     */
    private String writeContent(ExcelResult<T> excelResult, ExcelHandler handler, ExcelCell topLeftCell, String nullMsg) {
        //写入模板
        List<Object[]> collect = excelResult.getData().stream().map(d -> new Object[]{
                d.getMsg().getTemp().size() > 0 ? StringUtils.join(d.getMsg().getTemp(), ";") : nullMsg
        }).collect(Collectors.toList());

        handler.appendRange(topLeftCell, collect);

        //写入临时文件
        return writeTempFile(busiHandler.cacheKey(excelResult, getConfig()), handler);
    }

    /**
     * 写入临时文件
     *
     * @param cacheKey
     */
    private String writeTempFile(String cacheKey, ExcelHandler handler) {

        String upload_path = "";
        if (StringUtils.isNotBlank(config.getUpload_path())) {
            upload_path = config.getUpload_path();
        } else {
            DeployConfig config = (DeployConfig) request.getServletContext().getAttribute("deploy_config");
            if (config == null) {
                upload_path = config.getSys_upload_path();
            }
        }

        String contextPath = "/upload/excelTemp";
        String extension = "xls";
        String uploadPath = upload_path + "/excelTemp";
        String fileName = (System.currentTimeMillis() + RandomUtils.nextLong()) + "." + extension;
        //文件系统绝对路径
        String path = FilenameUtils.concat(uploadPath, fileName);
        //访问路径
        String url = FilenameUtils.concat(contextPath, fileName);

        //创建文件
        File destFile = new File(path);
        FileToolUtils.insureFileExists(destFile);
        handler.outputTo(destFile);
        ExcelUtils.updateCache(cacheKey, url, path);
        return url;
    }

    private Class<T> clazz() {
        return clazz;
    }


    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public ExcelImportRequest getConfig() {
        return config;
    }

    public void setConfig(ExcelImportRequest config) {
        this.config = config;
    }

    public ExcelBusiHandler getBusiHandler() {
        return busiHandler;
    }

    public void setBusiHandler(ExcelBusiHandler busiHandler) {
        this.busiHandler = busiHandler;
    }

    public int getResultRow() {
        return resultRow;
    }

    public void setResultRow(int resultRow) {
        this.resultRow = resultRow;
    }

    public int getResultColumn() {
        return resultColumn;
    }

    public void setResultColumn(int resultColumn) {
        this.resultColumn = resultColumn;
    }
}
