package org.zap.framework.common.excel.jxls;

import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.command.GridCommand;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.JxlsException;
import org.jxls.template.SimpleExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * 增加自定义类型字段导出
 */
public class EnhanceExporter {

    public static final String GRID_TEMPLATE_XLS = "grid_template.xls";
    static Logger logger = LoggerFactory.getLogger(EnhanceExporter.class);

    private byte[] templateBytes;

    public EnhanceExporter() {
        InputStream is = SimpleExporter.class.getResourceAsStream(GRID_TEMPLATE_XLS);
        try {
            registerGridTemplate(is);
        } catch (IOException e) {
            String message = "Failed to read default template file " + GRID_TEMPLATE_XLS;
            logger.error(message);
            throw new JxlsException(message, e);
        }
    }

    public void registerGridTemplate(InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int count;
        while ((count = inputStream.read(data)) != -1) {
            os.write(data, 0, count);
        }
        templateBytes = os.toByteArray();
    }

    public void gridExport(Iterable headers, Iterable dataObjects, String objectProps, OutputStream outputStream) {
        InputStream is = new ByteArrayInputStream(templateBytes);
        MyPoiTransformer transformer = null;
        try {
            transformer = MyPoiTransformer.createTransformer(is, outputStream);
        } catch (Exception e) {
        }
        AreaBuilder areaBuilder = new MyXlsCommentAreaBuilder(transformer);
        List<Area> xlsAreaList = areaBuilder.build();
        Area xlsArea = xlsAreaList.get(0);
        Context context = new Context();
        context.putVar("headers", headers);
        context.putVar("data", dataObjects);
        GridCommand gridCommand = (GridCommand) xlsArea.getCommandDataList().get(0).getCommand();
        gridCommand.setProps(objectProps);
        xlsArea.applyAt(new CellRef("Sheet1!A1"), context);
        try {
            transformer.write();
        } catch (IOException e) {
            logger.error("Failed to write to output stream", e);
            throw new JxlsException("Failed to write to output stream", e);
        }

    }
}
