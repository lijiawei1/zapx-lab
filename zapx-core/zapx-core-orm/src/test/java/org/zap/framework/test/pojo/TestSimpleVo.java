package org.zap.framework.test.pojo;

import org.zap.framework.orm.annotation.JdbcColumn;
import org.zap.framework.orm.annotation.JdbcTable;

@JdbcTable(value = "ZAP_TEST_SIMPLE", alias = "T")
public class TestSimpleVo {

    @JdbcColumn(id = true)
    String id;

    @JdbcColumn(version = true)
    int version;

    @JdbcColumn
    int dr;
    @JdbcColumn
    String code;
    @JdbcColumn
    String sname;
    @JdbcColumn
    String fname;
    @JdbcColumn
    String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getDr() {
        return dr;
    }

    public void setDr(int dr) {
        this.dr = dr;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
