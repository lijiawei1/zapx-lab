package org.zap.framework.common.excel.parser;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.command.GridCommand;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.template.SimpleExporter;
import org.jxls.transform.poi.PoiUtil;
import org.springframework.util.Assert;
import org.zap.framework.common.entity.LigerGrid;
import org.zap.framework.common.excel.jxls.ExcelEngine;
import org.zap.framework.common.excel.jxls.ExcelExporter;
import org.zap.framework.common.excel.jxls.MyPoiTransformer;
import org.zap.framework.common.excel.jxls.MyXlsCommentAreaBuilder;
import org.zap.framework.exception.BusinessException;
import org.zap.framework.lang.LDouble;
import org.zap.framework.util.DateUtils;
import org.zap.framework.util.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.jxls.template.SimpleExporter.GRID_TEMPLATE_XLS;
import static org.jxls.transform.poi.PoiTransformer.POI_CONTEXT_KEY;

/**
 * Created by Shin on 2016/3/29.
 */
public class ExcelUtils {

    static int MAX_ROWS = 65535;

    public static void updateCache(String key, String url, String file) {
        excelImportCache.put(key, new ExcelCacheHolder(url, file));
    }

    public static ConcurrentHashMap<String, ExcelCacheHolder> excelImportCache = new ConcurrentHashMap<>();

    static class ExcelCacheHolder {
        private String url;
        private String file;

        public ExcelCacheHolder() {
        }

        public ExcelCacheHolder(String url, String file) {
            this.url = url;
            this.file = file;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
    }

    /**
     * 转换Excel数据集
     *
     * @param contentRangeData
     * @param clazz
     * @param headers
     * @param <T>
     * @return
     */
    public static <T> ExcelResult<T> toList(ExcelRangeData contentRangeData, Class<T> clazz, Map<String, Integer> headers) {
        return toList(0, contentRangeData, clazz, headers);
    }

    /**
     * 转换Excel数据集
     *
     * @param start_row        起始行
     * @param contentRangeData 内容窗口
     * @param clazz
     * @param headers
     * @param <T>
     * @return
     */
    public static <T> ExcelResult<T> toList(int start_row, ExcelRangeData contentRangeData, Class<T> clazz, Map<String, Integer> headers) {

        ExcelResult<T> result = new ExcelResult<>();
        List<ExcelResultWrapper<T>> data = new ArrayList<>();
        result.setData(data);

        List<Field> fieldList = new ArrayList<>();
        Class<?> currClazz = clazz;

        while (!currClazz.equals(Object.class)) {

            Field[] declaredFields = currClazz.getDeclaredFields();
            currClazz = currClazz.getSuperclass();
            for (int i = 0; i < declaredFields.length; i++) {
                if (Modifier.isStatic(declaredFields[i].getModifiers())) {
                    continue;
                }
                declaredFields[i].setAccessible(true);
                fieldList.add(declaredFields[i]);
            }
        }

        Map<String, Field> fieldMap = Utils.collection2FieldMap(fieldList, "name", String.class);

        //将数据集，转换到实体里
        for (int i = start_row; i < contentRangeData.getRowCount(); i++) {

            try {

                T entity = clazz.newInstance();
                ExcelResultMsg msg = new ExcelResultMsg();
                msg.setRow(i);

                for (String key : headers.keySet()) {
                    int columnIndex = headers.get(key);

                    Field field = fieldMap.get(key);

                    if (field != null) {

                        Object value = null;
                        //字段类型
                        Class<?> type = field.getType();

                        //基础数据类型转换
                        if (String.class.equals(type)) {
                            value = contentRangeData.getString(i, columnIndex);
                        } else if (Long.class.equals(type) || long.class.equals(type)) {
                            value = contentRangeData.getLong(i, columnIndex);
                            if (long.class.equals(type) && value == null) continue;
                        } else if (Integer.class.equals(type) || int.class.equals(type)) {
                            value = contentRangeData.getInt(i, columnIndex);
                            if (int.class.equals(type) && value == null) continue;
                        } else if (Double.class.equals(type) || double.class.equals(type)) {
                            try {
                                value = contentRangeData.getDouble(i, columnIndex);
                                if (double.class.equals(type) && value == null) continue;
                            } catch (Exception ex) {
                                value = contentRangeData.getString(i, columnIndex);
                                try {
                                    value = Double.parseDouble((String) value);
                                } catch (Exception ex1) {
                                    value = 0.0;
                                }
                            }
                        } else if (LDouble.class.equals(type)) {
                            try {
                                value = contentRangeData.getDouble(i, columnIndex);
                                value = (value == null ? null : new LDouble((Double) value));
                            } catch (Exception ex) {
                                value = contentRangeData.getString(i, columnIndex);
                                try {
                                    value = new LDouble((String) value);
                                } catch (Exception ex1) {
                                    value = LDouble.ZERO_DBL;
                                }
                            }
                        } else if (LocalDateTime.class.equals(type)) {
                            try {
                                Date date = contentRangeData.getDate(i, columnIndex);
                                SimpleDateFormat formatter = new SimpleDateFormat(DateUtils.PATTERN_DATETIME);
                                value = date == null ? "" : formatter.format(date);
                            } catch (Exception e) {
                                value = contentRangeData.getString(i, columnIndex);
                            }
                            value = DateUtils.parseDateTime((String) value);
                        } else if (LocalDate.class.equals(type)) {
                            try {
                                Date date = contentRangeData.getDate(i, columnIndex);
                                SimpleDateFormat formatter = new SimpleDateFormat(DateUtils.PATTERN_DATE);
                                value = date == null ? "" : formatter.format(date);
                            } catch (Exception e) {
                                value = contentRangeData.getString(i, columnIndex);
                            }
                            value = DateUtils.parseDate((String) value);
                        } else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
                            String cellValue = contentRangeData.getString(i, columnIndex);
                            value = ("true".equalsIgnoreCase(cellValue)
                                    || "y".equalsIgnoreCase(cellValue)
                                    || "yes".equalsIgnoreCase(cellValue) ? true : false
                                    || "是".equalsIgnoreCase(cellValue) ? true : false)
                            ;
                        }
                        field.set(entity, value);
                    }
                }

                data.add(new ExcelResultWrapper<>(entity, msg));

            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            }
        }
        return result;
    }

    /**
     * 解析表头所有的字段
     *
     * @param data
     * @return
     */
    public static Map<String, Integer> parseAllHeaderName(ExcelRangeData data) {
        Map<String, Integer> mapping = new HashMap<>();
        for (int i = 0; i < data.getColumnCount(); i++) {
            String value = StringUtils.trimToEmpty(data.getString(0, i));
            mapping.put(value, i);
        }
        return mapping;
    }

    public static Map<String, Integer> parseHeader(ExcelRangeData data, Map<String, String> namesMapping) {
        Map<String, Integer> mapping = new HashMap<>();
        for (int i = 0; i < data.getColumnCount(); i++) {
            String value = StringUtils.trimToEmpty(data.getString(0, i));
            if (namesMapping.containsKey(value)) {
                mapping.put(namesMapping.get(value), i);
            }
        }
        return mapping;
    }

    /**
     * 解析表头对应关系
     *
     * @param data
     * @param names  表头中文
     * @param fields 表头实际映射的字段名
     * @return
     */
    public static Map<String, Integer> parseHeader(ExcelRangeData data, String[] names, String[] fields) {

        Map<String, Integer> mapping = new HashMap<>();

        Map<String, String> namesMapping = new HashMap<>();

        for (int i = 0; i < names.length; i++) {
            namesMapping.put(names[i], fields[i]);
        }

        for (int i = 0; i < data.getColumnCount(); i++) {
            String value = data.getString(0, i);
            if (namesMapping.containsKey(value)) {
                mapping.put(namesMapping.get(value), i);
            }
        }
        return mapping;
    }

    /**
     * 导出
     *
     * @param outputStream 输出流
     * @param varsData     变量数据-多列表
     * @param listData     列表数据-多主表
     */
    public static void export(OutputStream outputStream, Map<String, Map<String, ?>> varsData, Map<String, List<?>> listData) {

    }

    public static void exportTmpl() {

    }

    /**
     * 提供下载
     * 注意文件名的中文乱码问题
     *
     * @param filename
     * @param response
     * @param varsData
     * @param listData
     */
    public static void exportDownload(String filename, HttpServletRequest request, HttpServletResponse response, Map<String, Map<String, ?>> varsData, Map<String, List<?>> listData) throws IOException {

        try (InputStream templateStream = ExcelUtils.class.getResourceAsStream("/script/tms/report/36DriverAppLogQuery.xls")) {
            try (OutputStream targetStream = response.getOutputStream()) {

                String outFileName = StringUtils.defaultIfEmpty(filename, "导出报表.xls");
                // 中文乱码问题
                String agent = request.getHeader("USER-AGENT");
                if (agent != null && agent.indexOf("MSIE") == -1) {
                    outFileName = "=?UTF-8?B?" + (new String(Base64.encodeBase64(outFileName.getBytes("UTF-8")))) + "?="; // FireFox编码
                } else {
                    outFileName = java.net.URLEncoder.encode(outFileName, "UTF-8"); // IE编码
                }
                response.setHeader("pragma", "no-cache");
                response.setHeader("Content-Disposition", "attachment;filename=" + outFileName);
                response.setContentType("application/msexcel;charset=UTF-8");
                ExcelUtils.export(response.getOutputStream(), varsData, listData);

            } catch (Exception e) {
                throw new BusinessException("", e);
            }
        }
    }


    /**
     * @param filename
     * @param outputStream
     * @param data
     */
//    public static void export(String filename, OutputStream outputStream, Map<String, List<?>> list) {
//
//    }


    /**
     * @param dataReqeust
     * @param request
     * @param response
     * @param data
     * @param nullMessage
     * @throws IOException
     */
    public static void export(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, List<Map<String, Object>> data, String nullMessage) throws IOException {
        if (data != null) {
            ExcelUtils.export(dataReqeust, request, response, data == null ? new ArrayList<>() : data);
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write(nullMessage);
            response.getWriter().flush();
            response.getWriter().close();
        }
    }


    /**
     * excel导出工具类
     *
     * @param dataReqeust
     * @param request
     * @param response
     * @param dataList
     * @throws IOException
     */
    public static void export(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, List<?> dataList) throws IOException {
        ExcelExporter.exportSimple(dataReqeust, request, response, dataList);
        //String outFileName = StringUtils.defaultIfEmpty(dataReqeust.getFile_name(), "导出报表.xls");
        //// 中文乱码问题
        //String agent = (String) request.getHeader("USER-AGENT");
        //if (agent != null && agent.indexOf("MSIE") == -1) {
        //    outFileName = "=?UTF-8?B?" + (new String(Base64.encodeBase64(outFileName.getBytes("UTF-8")))) + "?="; // FireFox编码
        //} else {
        //    outFileName = java.net.URLEncoder.encode(outFileName, "UTF-8"); // IE编码
        //}
        //response.setHeader("pragma", "no-cache");
        //response.setHeader("Content-Disposition", "attachment;filename=" + outFileName);
        //response.setContentType("application/msexcel;charset=UTF-8");
        //
        //OutputStream os = response.getOutputStream();
        //try {
        //    // 生成Excel
        //    //List<String> headers = Arrays.asList("状态", "接收号码", "接收时间", "计费条数", "短信标题", "短信内容", "发送人", "公司名称");
        //    List<String> headers = Arrays.asList(dataReqeust.getHeaders().split(","));
        //    SimpleExporter exporter = new SimpleExporter();
        //    exporter.gridExport(headers, dataList, dataReqeust.getNames(), os);
        //} finally {
        //    if (os != null) {
        //        os.close();
        //    }
        //}
    }


    public static void export(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, LigerGrid grid) throws IOException {
        if (grid.getTotal() > 0) {
            //ExcelUtils.export(dataReqeust, request, response, (List<Map<String, Object>>) grid.getRows());
            ExcelExporter.exportSimple(dataReqeust, request, response, (List<Map<String, Object>>) grid.getRows());
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("无数据");
            response.getWriter().flush();
            response.getWriter().close();
        }
    }

    /**
     * 通过模板导出
     *
     * @param dataReqeust
     * @param request
     * @param response
     * @param dataList 数据列表
     * @param template 模板文件的路径
     * @throws IOException
     */
    public static void exportTmpl(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, List<?> dataList,
                                  String template, Map<String, Object> param) throws IOException {

        //String outFileName = StringUtils.defaultIfEmpty(dataReqeust.getFile_name(), "导出报表.xls");
        //// 中文乱码问题
        //String agent = request.getHeader("USER-AGENT");
        //if (agent != null && agent.indexOf("MSIE") == -1) {
        //    outFileName = "=?UTF-8?B?" + (new String(Base64.encodeBase64(outFileName.getBytes("UTF-8")))) + "?="; // FireFox编码
        //} else {
        //    outFileName = java.net.URLEncoder.encode(outFileName, "UTF-8"); // IE编码
        //}
        //response.setHeader("pragma", "no-cache");
        //response.setHeader("Content-Disposition", "attachment;filename=" + outFileName);
        //response.setContentType("application/msexcel;charset=UTF-8");
        //
        //Map<String, Object> contextMap = new HashMap<>();
        //contextMap.put("details", dataList);
        //contextMap.put("main", param);
        //
        //Context context = new Context(contextMap);
        //ExcelEngine.processTemplate(ExcelUtils.class.getResourceAsStream(template), response.getOutputStream(), context);
        ExcelExporter.exportTmpl(dataReqeust, request, response, dataList, template, param);

    }

    /**
     * @param dataReqeust
     * @param request
     * @param response
     * @param grid
     * @param template 模板文件的路径
     * @throws IOException 1
     */
    public static void exportTmpl(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, LigerGrid grid, String template) throws IOException {
        exportTmpl(dataReqeust, request, response, grid, template, null);
    }


    /**
     * @param dataReqeust
     * @param request
     * @param response
     * @param grid
     * @param template 模板文件的路径
     * @param vars     参数
     * @throws IOException 1
     */
    public static void exportTmpl(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, LigerGrid grid, String template, Map<String, Object> vars) throws IOException {

        if (grid.getTotal() > 0) {
            Map<String, Object> params = grid.getParams();
            if (vars != null) {
                params.putAll(vars);
            }
            ExcelExporter.exportTmpl(dataReqeust, request, response, (List<Map<String, Object>>) grid.getRows(), template, params);
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("无数据");
            response.getWriter().flush();
            response.getWriter().close();
        }

    }


    /**
     * 生成表单配置
     *
     * @param sheetName   sheet名称
     * @param headers     头字段
     * @param dataObjects 数据
     * @param objectProps 属性
     * @return
     */
    public static SheetConfig sheetConfig(String sheetName, String headers, Iterable dataObjects, String objectProps) {
        return new SheetConfig(sheetName, Arrays.asList(headers.split(",")), dataObjects, objectProps);
    }

    /**
     * 导出多个sheet
     *
     * @param configs  sheet配置
     * @param request
     * @param response
     * @param fileName 文件名称 1
     */
    public static void exportMultiSheet(SheetConfig[] configs, HttpServletRequest request, HttpServletResponse response, String fileName) {
        ExcelExporter.exportMultiSheet(configs, request, response, fileName);
        //try (InputStream templateStream = SimpleExporter.class.getResourceAsStream(GRID_TEMPLATE_XLS)) {
        //    try (OutputStream targetStream = response.getOutputStream()) {
        //
        //        String outFileName = StringUtils.defaultIfEmpty(fileName, "导出报表.xls");
        //        // 中文乱码问题
        //        String agent = request.getHeader("USER-AGENT");
        //        if (agent != null && agent.indexOf("MSIE") == -1) {
        //            outFileName = "=?UTF-8?B?" + (new String(Base64.encodeBase64(outFileName.getBytes("UTF-8")))) + "?="; // FireFox编码
        //        } else {
        //            outFileName = java.net.URLEncoder.encode(outFileName, "UTF-8"); // IE编码
        //        }
        //        response.setHeader("pragma", "no-cache");
        //        response.setHeader("Content-Disposition", "attachment;filename=" + outFileName);
        //        response.setContentType("application/msexcel;charset=UTF-8");
        //
        //        MyPoiTransformer transformer = MyPoiTransformer.createTransformer(templateStream, targetStream);
        //        AreaBuilder areaBuilder = new MyXlsCommentAreaBuilder(transformer);
        //        ExcelEngine.addMyCommand();// 方便拓展
        //        List<Area> xlsAreaList = areaBuilder.build();
        //        if (xlsAreaList.isEmpty()) {
        //            throw new BusinessException("没有设置模板");
        //        }
        //
        //        Area xlsArea = xlsAreaList.get(0);
        //        String sourceSheetName = xlsArea.getStartCellRef().getSheetName();
        //        for (int i = 0; i < configs.length; i++) {
        //            Context context = new Context();
        //            context.putVar(POI_CONTEXT_KEY, new PoiUtil());
        //            context.putVar("headers", configs[i].getHeaders());
        //            context.putVar("data", configs[i].getDataObjects());
        //            GridCommand gridCommand = (GridCommand) xlsArea.getCommandDataList().get(0).getCommand();
        //            gridCommand.setProps(configs[i].getObjectProps());
        //            xlsArea.applyAt(new CellRef(configs[i].getSheetName() + "!A1"), context);
        //        }
        //
        //        transformer.deleteSheet(sourceSheetName);
        //        transformer.write();
        //    } catch (Exception e) {
        //        e.printStackTrace();
        //    }
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}


    }

    public static byte[] registerGridTemplate(InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int count;
        while ((count = inputStream.read(data)) != -1) {
            os.write(data, 0, count);
        }
        return os.toByteArray();
    }

    public static class SheetConfig {
        String sheetName;
        Iterable headers;
        Iterable dataObjects;
        String objectProps;

        public SheetConfig(String sheetName, Iterable headers, Iterable dataObjects, String objectProps) {
            this.sheetName = sheetName;
            this.headers = headers;
            this.dataObjects = dataObjects;
            this.objectProps = objectProps;
        }

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        public Iterable getHeaders() {
            return headers;
        }

        public void setHeaders(Iterable headers) {
            this.headers = headers;
        }

        public Iterable getDataObjects() {
            return dataObjects;
        }

        public void setDataObjects(Iterable dataObjects) {
            this.dataObjects = dataObjects;
        }

        public String getObjectProps() {
            return objectProps;
        }

        public void setObjectProps(String objectProps) {
            this.objectProps = objectProps;
        }

    }


    /**
     * 将列名转换为列索引，例如将列"A"转换为0
     *
     * @param columnName 要转换的列名
     * @return 参数columnName代表的列的索引
     */
    public static int convertColumnNameToIndex(String columnName) {
        Assert.notNull(columnName);
        String theColumn = columnName.toUpperCase();
        int length = theColumn.length();
        int result = letterToInt(theColumn.charAt(length - 1));
        if (length == 1) {
            return result;
        }
        for (int i = 1; i < length; i++) {
            int letter = theColumn.charAt(length - i - 1);
            result = (letterToInt(letter) + 1) * ((int) Math.pow(26, i)) + result;
        }
        return result;
    }

    private static int letterToInt(int letter) {
        return letter - 65;
    }

    public static Double getDouble(Object data) {
        if (data == null) {
            return null;
        }
        if (!(data instanceof Double)) {
            throw new IllegalStateException("数据类型错误：单元格中的数据不是数值类型");
        }
        return (Double) data;
    }

    public static Integer getInt(Object data) {
        Double value = getDouble(data);
        return value == null ? null : value.intValue();
    }

    public static Long getLong(Object data) {
        Double value = getDouble(data);
        return value == null ? null : value.longValue();
    }

    public static Boolean getBoolean(Object data) {
        if (data == null) {
            return null;
        }
        if (!(data instanceof Boolean)) {
            throw new IllegalStateException("数据类型错误：单元格中的数据不是布尔类型");
        }
        return (Boolean) data;
    }

    public static String getString(Object data) {
        if (data == null) {
            return null;
        }

        //处理科学计数法的问题
        if (data instanceof Double) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(10);
            nf.setGroupingUsed(false);
            return nf.format(data);
        }

        return data == null ? null : StringUtils.trimToNull(data.toString());
    }

    public static Date getDate(Object data, Version version, boolean isDate1904) {
        Double value = getDouble(data);
        return version.getDate(value, isDate1904);
    }
}
