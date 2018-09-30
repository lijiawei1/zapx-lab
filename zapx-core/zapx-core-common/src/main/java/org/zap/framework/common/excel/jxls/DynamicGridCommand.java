package org.zap.framework.common.excel.jxls;

import org.apache.commons.beanutils.PropertyUtils;
import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.Command;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.transform.Transformer;
import org.jxls.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The command implements a grid with dynamic columns and rows
 * Created by Leonid Vysochyn on 25-Jun-15.
 */
public class DynamicGridCommand extends AbstractCommand {
    public static final String COMMAND_NAME = "grid";
    public static final String HEADER_VAR = "header";
    public static final String FOOTER_VAR = "footer";
    public static final String DATA_VAR = "cell";
    public static final String FORMULA_VAR = "formula";

    static Logger logger = LoggerFactory.getLogger(org.jxls.command.GridCommand.class);

    /**
     * Name of a context variable containing a collection of headers
     */
    String headers;
    /**
     * Name of a context variable containing a collection of data objects for body
     */
    String data;
    /**
     * Comma-separated list of object properties for each grid row
     */
    String props;

    /**
     * Name of a context variable containing a collection of footers
     */
    String footers;

    /**
     * 公式
     */
    String formular;

    /**
     * Comma-separated list of format type cells
     * e.g. formatCells="Double:E1, Date:F1"
     */
    String formatCells;
    Map<String, String> cellStyleMap = new HashMap<>();

    /**
     * Comma-separated list of formulas
     * e.g. formulas="[SUM()]:E1, Date:F1"
     */
    Map<String, String> footerMap = new HashMap<>();


    List<String> rowObjectProps = new ArrayList<>();

    Area headerArea;
    Area bodyArea;
    Area footerArea;


    public DynamicGridCommand() {
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
        if (props != null) {
            rowObjectProps = Arrays.asList(props.replaceAll("\\s+", "").split(","));
        }
    }

    public String getFormular() {
        return formular;
    }

    public void setFormular(String formular) {
        this.formular = formular;
    }

    public String getFormatCells() {
        return formatCells;
    }

    public void setFormatCells(String formatCells) {
        this.formatCells = formatCells;
        if (formatCells != null) {
            List<String> cellStyleList = Arrays.asList(formatCells.split(","));
            try {
                for (String cellStyleString : cellStyleList) {
                    String[] styleCell = cellStyleString.split(":");
                    cellStyleMap.put(styleCell[0].trim(), styleCell[1].trim());
                }
            } catch (Exception e) {
                logger.error("Failed to parse formatCells attribute");
            }
        }
    }

    public String getFooters() {
        return footers;
    }

    public void setFooters(String footers) {
        this.footers = footers;
    }

    public DynamicGridCommand(String headers, String data) {
        this.headers = headers;
        this.data = data;
    }

    public DynamicGridCommand(String headers, String data, String props, Area headerArea, Area bodyArea) {
        this.headers = headers;
        this.data = data;
        this.props = props;
        this.headerArea = headerArea;
        this.bodyArea = bodyArea;
    }

    public DynamicGridCommand(String headers, String data, Area headerArea, Area bodyArea) {
        this.headers = headers;
        this.data = data;
        this.headerArea = headerArea;
        this.bodyArea = bodyArea;
        addArea(headerArea);
        addArea(bodyArea);
    }

    public DynamicGridCommand(String headers, String data, Area headerArea, Area bodyArea, Area footerArea) {
        this.headers = headers;
        this.data = data;
        this.headerArea = headerArea;
        this.bodyArea = bodyArea;
        this.footerArea = footerArea;
        addArea(headerArea);
        addArea(bodyArea);
        addArea(footerArea);
    }

    @Override
    public Command addArea(Area area) {
        List<Area> areaList = super.getAreaList();
        if (areaList.size() >= 3) {
            throw new IllegalArgumentException("Cannot add any more areas to GridCommand. You can add only 1 area as a 'header' and 1 area as a 'body'");
        }
        if (areaList.size() == 0) {
            headerArea = area;
        } else if (areaList.size() == 1){
            bodyArea = area;
        } else if (areaList.size() == 2) {
            footerArea = area;
        }
        return super.addArea(area);
    }

    @Override
    public Size applyAt(CellRef cellRef, Context context) {
        Size headerAreaSize = processHeaders(cellRef, context);
        CellRef bodyCellRef = new CellRef(cellRef.getSheetName(), cellRef.getRow() + headerAreaSize.getHeight(), cellRef.getCol());
        Size bodyAreaSize = processBody(bodyCellRef, context);

        int gridHeight = headerAreaSize.getHeight() + bodyAreaSize.getHeight();
        int gridWidth = Math.max(headerAreaSize.getWidth(), bodyAreaSize.getWidth());

        if (footerArea != null) {
            CellRef footerCellRef = new CellRef(cellRef.getSheetName(), cellRef.getRow() + headerAreaSize.getHeight() + bodyAreaSize.getHeight(), cellRef.getCol());
            Size footerAreaSize = processFooter(footerCellRef, context);

            gridHeight += footerAreaSize.getHeight();
            gridWidth = Math.max(gridWidth, footerAreaSize.getWidth());
        }

        return new Size(gridWidth, gridHeight);
    }

    private Size processFooter(CellRef cellRef, Context context) {
        if (footerArea == null || footers == null) {
            return Size.ZERO_SIZE;
        }
        Collection footers = Util.transformToCollectionObject(getTransformationConfig().getExpressionEvaluator(), this.footers, context);
        CellRef currentCell = cellRef;
        CellRef lastCell;
        int width = 0;
        int height = 0;

        CellRef startCellRef = footerArea.getStartCellRef();
        Transformer transformer = super.getTransformer();

        CellData startCellData = transformer.getCellData(startCellRef);
        //startCellData.setFormula("SUM(INDIRECT(\"A\"&3):INDIRECT(\"A\"&ROW()-1))");
        startCellData.setFormula(formular);

        for (Object footer : footers) {
            context.putVar(FOOTER_VAR, footer);

            //startCellData.setFormula(StringUtils.trimToEmpty((String)footer));

            Size size = footerArea.applyAt(currentCell, context);
            currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
            width += size.getWidth();
            height = Math.max(height, size.getHeight());

        }
        context.removeVar(FOOTER_VAR);

        return new Size(width, height);
    }

    private Size processBody(final CellRef cellRef, Context context) {
        if (bodyArea == null || data == null) {
            return Size.ZERO_SIZE;
        }
        Collection dataCollection = Util.transformToCollectionObject(getTransformationConfig().getExpressionEvaluator(), this.data, context);

        CellRef currentCell = cellRef;
        int totalWidth = 0;
        int totalHeight = 0;
        Context.Config config = context.getConfig();
        boolean oldIgnoreSourceCellStyle = config.isIgnoreSourceCellStyle();
        config.setIgnoreSourceCellStyle(true);
        Map<String, String> oldStyleCellMap = config.getCellStyleMap();
        config.setCellStyleMap(this.cellStyleMap);
        for (Object rowObject : dataCollection) {
            if (rowObject.getClass().isArray() || rowObject instanceof Iterable) {
                Iterable cellCollection = null;
                if (rowObject.getClass().isArray()) {
                    cellCollection = Arrays.asList((Object[]) rowObject);
                } else {
                    cellCollection = (Iterable) rowObject;
                }
                int width = 0;
                int height = 0;
                for (Object cellObject : cellCollection) {
                    context.putVar(DATA_VAR, cellObject);
                    Size size = bodyArea.applyAt(currentCell, context);
                    currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
                    width += size.getWidth();
                    height = Math.max(height, size.getHeight());
                }
                totalWidth = Math.max(width, totalWidth);
                totalHeight = totalHeight + height;
                currentCell = new CellRef(cellRef.getSheetName(), currentCell.getRow() + height, cellRef.getCol());
            } else {
                if (rowObjectProps.isEmpty()) {
                    throw new IllegalArgumentException("Got a non-collection object type for a Grid row but object properties list is empty");
                }
                int width = 0;
                int height = 0;
                for (String prop : rowObjectProps) {
                    try {
                        Object value = PropertyUtils.getProperty(rowObject, prop);
                        context.putVar(DATA_VAR, value);
                        Size size = bodyArea.applyAt(currentCell, context);
                        currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
                        width += size.getWidth();
                        height = Math.max(height, size.getHeight());
                    } catch (Exception e) {
                        String message = "Failed to evaluate property " + prop + " of row object of class " + rowObject.getClass().getName();
                        logger.error(message, e);
                        throw new IllegalStateException(message, e);
                    }
                }
                totalWidth = Math.max(width, totalWidth);
                totalHeight = totalHeight + height;
                currentCell = new CellRef(cellRef.getSheetName(), currentCell.getRow() + height, cellRef.getCol());
            }
        }
        context.removeVar(DATA_VAR);
        config.setIgnoreSourceCellStyle(oldIgnoreSourceCellStyle);
        config.setCellStyleMap(oldStyleCellMap);
        return new Size(totalWidth, totalHeight);
    }

    private Size processHeaders(CellRef cellRef, Context context) {
        if (headerArea == null || headers == null) {
            return Size.ZERO_SIZE;
        }
        Collection headers = Util.transformToCollectionObject(getTransformationConfig().getExpressionEvaluator(), this.headers, context);
        CellRef currentCell = cellRef;
        int width = 0;
        int height = 0;
        for (Object header : headers) {
            context.putVar(HEADER_VAR, header);
            Size size = headerArea.applyAt(currentCell, context);
            currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
            width += size.getWidth();
            height = Math.max(height, size.getHeight());
        }
        context.removeVar(HEADER_VAR);

        return new Size(width, height);
    }

}

