package org.zap.framework.common.excel.jxls;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.command.GridCommand;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.template.SimpleExporter;
import org.jxls.transform.poi.PoiUtil;
import org.zap.framework.common.entity.ListSupport;
import org.zap.framework.common.excel.parser.ExcelDataRequest;
import org.zap.framework.common.excel.parser.ExcelUtils;
import org.zap.framework.exception.BusinessException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jxls.template.SimpleExporter.GRID_TEMPLATE_XLS;
import static org.jxls.transform.poi.PoiTransformer.POI_CONTEXT_KEY;

/**
 * Excel导出功能
 */
public class ExcelExporter {

    static int MAX_ROWS = 60000;

    /**
     * 简单报表导出
     * 最基本的报表单表格式导出，第一行是列名，第二行开始是数据，自带template
     *
     * @param dataReqeust
     * @param request
     * @param response
     * @param dataList
     * @throws IOException
     */
    public static void exportSimple(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, List<?> dataList) throws IOException {
        if (dataList != null && dataList.size() > 0 && dataList.size() <= MAX_ROWS) {
            String outFileName = StringUtils.defaultIfEmpty(dataReqeust.getFile_name(), "导出报表.xls");
            // 中文乱码问题
            String agent = (String) request.getHeader("USER-AGENT");
            if (agent != null && agent.indexOf("MSIE") == -1) {
                outFileName = "=?UTF-8?B?" + (new String(Base64.encodeBase64(outFileName.getBytes("UTF-8")))) + "?="; // FireFox编码
            } else {
                outFileName = java.net.URLEncoder.encode(outFileName, "UTF-8"); // IE编码
            }
            response.setHeader("pragma", "no-cache");
            response.setHeader("Content-Disposition", "attachment;filename=" + outFileName);
            response.setContentType("application/msexcel;charset=UTF-8");

            OutputStream os = response.getOutputStream();
            try {
                // 生成Excel
                //List<String> headers = Arrays.asList("状态", "接收号码", "接收时间", "计费条数", "短信标题", "短信内容", "发送人", "公司名称");
                List<String> headers = Arrays.asList(dataReqeust.getHeaders().split(","));
                EnhanceExporter exporter = new EnhanceExporter();
                exporter.gridExport(headers, dataList, dataReqeust.getNames(), os);
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write((dataList != null && dataList.size() > MAX_ROWS) ? "单次导出数据量不能超过" + MAX_ROWS + "条，请分批查询导出" : "无数据");
            response.getWriter().flush();
            response.getWriter().close();
        }
    }


    public static void exportSimple(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, ListSupport list) throws IOException {

        if (list.getTotal() > 0) {
            exportSimple(dataReqeust, request, response, (List<Map<String, Object>>) list.getData());
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
     * @param dataReqeust 报表配置
     * @param request
     * @param response
     * @param dataList    数据列表
     * @param template    模板文件的路径
     * @param param       表头参数
     * @throws IOException
     */
    public static void exportTmpl(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, List<?> dataList,
                                  String template, Map<String, Object> param) throws IOException {

        if (dataList != null && dataList.size() > 0 && dataList.size() <= MAX_ROWS) {
            String outFileName = StringUtils.defaultIfEmpty(dataReqeust.getFile_name(), "导出报表.xls");
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

            Map<String, Object> contextMap = new HashMap<>();
            contextMap.put("details", dataList);
            contextMap.put("main", param);

            Context context = new Context(contextMap);
            ExcelEngine.processTemplate(ExcelUtils.class.getResourceAsStream(template), response.getOutputStream(), context);
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write((dataList != null && dataList.size() > MAX_ROWS) ? "单次导出数据量不能超过" + MAX_ROWS + "条，请分批查询导出" : "无数据");
            response.getWriter().flush();
            response.getWriter().close();
        }
    }

    /**
     * @param dataReqeust 报表配置
     * @param request
     * @param response
     * @param list        数据
     * @param template    模板文件的路径
     * @param param        报表表头参数
     * @throws IOException
     */
    public static void exportTmpl(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, ListSupport list, String template,
                                  Map<String, Object> param) throws IOException {

        if (list.getTotal() > 0) {
            Map<String, Object> params = list.getParams();
            if (param != null) {
                params.putAll(param);
            }
            exportTmpl(dataReqeust, request, response, (List<Map<String, Object>>) list.getData(), template, params);
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("无数据");
            response.getWriter().flush();
            response.getWriter().close();
        }

    }

    /**
     * @param dataReqeust
     * @param request
     * @param response
     * @param list
     * @param template    模板文件的路径
     * @throws IOException
     */
    public static void exportTmpl(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, ListSupport list, String template) throws IOException {
        exportTmpl(dataReqeust, request, response, list, template, null);
    }

    /**
     * 导出多个sheet
     *
     * @param configs  sheet配置
     * @param request
     * @param response
     * @param fileName 文件名称
     */
    public static void exportMultiSheet(ExcelUtils.SheetConfig[] configs, HttpServletRequest request, HttpServletResponse response, String fileName) {

        for (ExcelUtils.SheetConfig config : configs) {
            //TODO 注意数据量检查
        }

        try (InputStream templateStream = SimpleExporter.class.getResourceAsStream(GRID_TEMPLATE_XLS)) {
            try (OutputStream targetStream = response.getOutputStream()) {

                String outFileName = StringUtils.defaultIfEmpty(fileName, "导出报表.xls");
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

                MyPoiTransformer transformer = MyPoiTransformer.createTransformer(templateStream, targetStream);
                AreaBuilder areaBuilder = new MyXlsCommentAreaBuilder(transformer);
                ExcelEngine.addMyCommand();// 方便拓展
                List<Area> xlsAreaList = areaBuilder.build();
                if (xlsAreaList.isEmpty()) {
                    throw new BusinessException("没有设置模板");
                }

                Area xlsArea = xlsAreaList.get(0);
                String sourceSheetName = xlsArea.getStartCellRef().getSheetName();
                for (int i = 0; i < configs.length; i++) {
                    Context context = new Context();
                    context.putVar(POI_CONTEXT_KEY, new PoiUtil());
                    context.putVar("headers", configs[i].getHeaders());
                    context.putVar("data", configs[i].getDataObjects());
                    GridCommand gridCommand = (GridCommand) xlsArea.getCommandDataList().get(0).getCommand();
                    gridCommand.setProps(configs[i].getObjectProps());
                    xlsArea.applyAt(new CellRef(configs[i].getSheetName() + "!A1"), context);
                }

                transformer.deleteSheet(sourceSheetName);
                transformer.write();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出报表
     *
     * @param dataReqeust excel格式请求
     * @param request
     * @param response
     * @param dataList    数据
     * @param template    模板文件
     * @param param       表头参数
     * @param configList  配置信息
     * @throws IOException
     */
    public static void exportDynamicTmpl(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response, List<?> dataList,
                                         String template, Map<String, Object> param, List<DynamicGridConfig> configList) throws IOException {
        String outFileName = StringUtils.defaultIfEmpty(dataReqeust.getFile_name(), "导出报表.xls");
        // 中文乱码问题
        String agent = request.getHeader("USER-AGENT");
        if (null != agent && -1 != agent.indexOf("MSIE") || null != agent
                && -1 != agent.indexOf("Trident") || null != agent && -1 != agent.indexOf("Edge")) {// ie浏览器及Edge浏览器
            String name = java.net.URLEncoder.encode(outFileName, "UTF-8");
            outFileName = name;
        } else if (null != agent && -1 != agent.indexOf("Mozilla")) {// 火狐,Chrome等浏览器
            outFileName = new String(outFileName.getBytes("UTF-8"), "iso-8859-1");
        }
        response.setHeader("pragma", "no-cache");
        response.setHeader("Content-Disposition", "attachment;filename=" + outFileName);
        response.setContentType("application/msexcel;charset=UTF-8");

        List<String> headers = Arrays.asList(dataReqeust.getHeaders().split(","));
        List<String> footers = Stream.of(dataReqeust.getFooters().split(","))
                .map(f -> "#".equals(f) ? "" : f).collect(Collectors.toList());

        Context context = new Context(new HashMap<String, Object>() {
            {
                put("main", param);
                put("headers", headers);
                put("data", dataList);
                put("footers", footers);
                put("props", dataReqeust.getNames());
            }
        });
        ExcelEngine.processDynamicColumnTemplate(ExcelUtils.class.getResourceAsStream(template), response.getOutputStream(), context);
    }

    /**
     * 导出动态列报表
     * <p>
     * 报表模板配置
     * A
     * 5 ${header}
     * 6 ${cell}
     * 7 ${footer}
     * <p>
     * jx:dynamicGrid(lastCell="A7" headers="headers" data="data" footers="footers" areas=[A5:A5, A6:A6, A7:A7] formular="SUM(A6)")
     * <p>
     * headers 表头名称列表
     * data 数据列
     * footer 合计列
     *
     * @param dataReqeust
     * @param request
     * @param response
     * @param list
     * @param template    模板文件
     * @param vars        参数
     * @param configList
     * @param consumer    自定义参数处理
     * @throws IOException
     */
    public static void exportDynamicTmpl(ExcelDataRequest dataReqeust, HttpServletRequest request, HttpServletResponse response,
                                         ListSupport list, String template, Map<String, Object> vars,
                                         List<DynamicGridConfig> configList,
                                         Consumer<List<Map<String, Object>>> consumer) throws IOException {

        if (list.getTotal() > 0 && list.getTotal() <= MAX_ROWS) {
            Map<String, Object> params = list.getParams();
            if (vars != null) {
                params.putAll(vars);
            }

            List<Map<String, Object>> rows = (List<Map<String, Object>>) list.getData();
            consumer.accept(rows);

            Map<String, Object> entity = rows.get(0);
            Map<String, DynamicGridConfig> collect = configList.stream().collect(Collectors.toMap(c -> c.getName(), c -> c));

            List<DynamicGridConfig> filterConfigList = collect.entrySet().stream()
                    .filter(e -> entity.containsKey(e.getKey()))
                    .map(e -> e.getValue())
                    .collect(Collectors.toList());

            filterConfigList.sort(Comparator.comparingInt(DynamicGridConfig::getIndex));

            String header = filterConfigList.stream().map(c -> c.getHeader()).collect(Collectors.joining(","));
            String name = filterConfigList.stream().map(c -> c.getName()).collect(Collectors.joining(","));
            String footer = filterConfigList.stream().map(c -> c.getFooter()).collect(Collectors.joining(","));

            dataReqeust.setHeaders(header);
            dataReqeust.setNames(name);
            dataReqeust.setFooters(footer);

            exportDynamicTmpl(dataReqeust, request, response, rows, template, params, configList);
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write(list.getTotal() > MAX_ROWS ? "单次导出数据量不能超过" + MAX_ROWS + "条，请分批查询导出" : "无数据");
            response.getWriter().flush();
            response.getWriter().close();
        }

    }
}
