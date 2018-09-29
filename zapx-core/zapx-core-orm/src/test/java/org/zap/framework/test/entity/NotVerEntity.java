package org.zap.framework.test.entity;

import org.zap.framework.orm.annotation.JdbcColumn;
import org.zap.framework.orm.annotation.JdbcTable;

import java.io.Serializable;

/**
 * Created by Shin on 2015/12/24.
 */
@JdbcTable(value = "ZAP_TEST", alias = "TV")
public class NotVerEntity implements Serializable {

    @JdbcColumn(id = true)
    private String id;

    @JdbcColumn
    private int int_field;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getInt_field() {
        return int_field;
    }

    public void setInt_field(int int_field) {
        this.int_field = int_field;
    }
}
