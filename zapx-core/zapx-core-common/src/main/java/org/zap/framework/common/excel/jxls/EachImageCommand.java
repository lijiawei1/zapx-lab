package org.zap.framework.common.excel.jxls;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.Command;
import org.jxls.common.*;
import org.jxls.transform.Transformer;

/**
 * Created by sqx on 2017/11/6.
 */
public class EachImageCommand extends AbstractCommand {

    private byte[] imageBytes;
    private ImageType imageType = ImageType.PNG;
    private Area area;
    /**
     * Expression that can be evaluated to image byte array byte[]
     */
    private String src;
    private String text; //无法读取图片时的提示

    public EachImageCommand() {
    }

    public EachImageCommand(String image, ImageType imageType) {
        this.src = image;
        this.imageType = imageType;
    }

    public EachImageCommand(byte[] imageBytes, ImageType imageType) {
        this.imageBytes = imageBytes;
        this.imageType = imageType;
    }

    /**
     * @return src expression producing image byte array
     */
    public String getSrc() {
        return src;
    }

    /**
     * @param src expression resulting in image byte array
     */
    public void setSrc(String src) {
        this.src = src;
    }

    public void setImageType(String strType){
        imageType = ImageType.valueOf(strType);
    }

    @Override
    public Command addArea(Area area) {
        if( super.getAreaList().size() >= 1){
            throw new IllegalArgumentException("You can add only a single area to 'eachImage' command");
        }
        this.area = area;
        return super.addArea(area);
    }

    public String getName() {
        return "eachImage";
    }

    public Size applyAt(CellRef cellRef, Context context) {
        if( area == null ){
            throw new IllegalArgumentException("No area is defined for image command");
        }
        Transformer transformer = getTransformer();
        Size size = area.getSize();
        //获取图片显示区域是时候，多加一行和一列，获取完之后再恢复原来大小
        size.setWidth(size.getWidth() + 1);
        size.setHeight(size.getHeight() + 1);
        AreaRef areaRef = new AreaRef(cellRef, size);
        size.setWidth(size.getWidth() - 1);
        size.setHeight(size.getHeight() - 1);
        byte[] imgBytes = imageBytes;
        if( src != null ){
            Object imgObj = getTransformationConfig().getExpressionEvaluator().evaluate(src, context.toMap());
            if(imgObj != null){
                if( !(imgObj instanceof byte[]) ){
                    throw new IllegalArgumentException("src value must contain image bytes (byte[])");
                }
                imgBytes = (byte[]) imgObj;
            }
        }
        if(imgBytes != null){
            transformer.addImage(areaRef, imgBytes, imageType);
        }
        area.applyAt(cellRef, context); //恢复原有的样式
        if(imgBytes == null && StringUtils.isNotBlank(text)){
            MyPoiTransformer poi = (MyPoiTransformer)transformer;
            Sheet sheet = poi.getWorkbook().getSheet(cellRef.getSheetName());
            Row row = sheet.getRow(cellRef.getRow());
            if(row != null && row.getCell(cellRef.getCol()) != null){
                row.getCell(cellRef.getCol()).setCellValue(text);
            }
        }
        return size;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}