package org.zap.framework.test.entity;

import org.zap.framework.orm.annotation.JdbcColumn;
import org.zap.framework.orm.annotation.JdbcTable;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Shin on 2015/12/23.
 */

@JdbcTable(value="ZAP_TEST", alias="TV", view = true)
public class TestView implements Serializable {

    @JdbcColumn
    String varchar_field;

    @JdbcColumn
    int int_field;

    @JdbcColumn
    LocalDateTime old_datetime;

    @JdbcColumn
    LocalDateTime datetime_field;

    public String getVarchar_field() {
        return varchar_field;
    }

    public void setVarchar_field(String varchar_field) {
        this.varchar_field = varchar_field;
    }

    public int getInt_field() {
        return int_field;
    }

    public void setInt_field(int int_field) {
        this.int_field = int_field;
    }

    public LocalDateTime getOld_datetime() {
        return old_datetime;
    }

    public void setOld_datetime(LocalDateTime old_datetime) {
        this.old_datetime = old_datetime;
    }

    public LocalDateTime getDatetime_field() {
        return datetime_field;
    }

    public void setDatetime_field(LocalDateTime datetime_field) {
        this.datetime_field = datetime_field;
    }
}
