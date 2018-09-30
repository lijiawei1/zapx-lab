package org.zap.framework.common.excel.jxls;

/**
 * Created by Administrator on 2017/12/21 0021.
 */
public class DynamicGridConfig {

    public static String FOOTER_TAG = "#";

    public DynamicGridConfig(int index, String header, String name, String footer) {
        this.header = header;
        this.name = name;
        this.footer = footer;
        this.index = index;
    }

    int index;

    String header;

    String name;

    String footer;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }
}
