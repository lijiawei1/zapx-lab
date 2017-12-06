package org.zap.framework.common.excel.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shin on 2017/5/4.
 */
public class ExcelResultMsg {

    /**
     * 数据冲突
     * 1.行号
     * 2.数据库已经存在该记录
     * 3.自身excel内数据冲突
     * <p>
     * 行2 系统记录[]冲突
     * 行2 模板记录行x[]冲突
     */
    public final static String DATA_CONFILCT = "data_confilct";
    /**
     * 类型转换错误
     * 1.行号
     * 2.字段名
     * 3.数据格式
     * <p>
     * 行2 列[]格式解释出错，值[],错误[ex.getMessage]
     */
    public final static String TYPE_ERROR = "type_error";

    int row;

    int col;

    String err;

    String msg;

    List<String> temp = new ArrayList<>();

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void addMsg(String msg) {
        temp.add(msg);
    }

    public List<String> getTemp() {
        return temp;
    }

    public void setTemp(List<String> temp) {
        this.temp = temp;
    }
}
