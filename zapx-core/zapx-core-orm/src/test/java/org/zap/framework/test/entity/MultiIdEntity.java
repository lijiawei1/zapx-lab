package org.zap.framework.test.entity;

import org.zap.framework.orm.annotation.JdbcColumn;
import org.zap.framework.orm.annotation.JdbcTable;

import java.io.Serializable;

/**
 * Created by Shin on 2016/7/14.
 */
@JdbcTable(value = "TMS_BM_PLAN_CAR", alias = "PC")
public class MultiIdEntity implements Serializable {

    @JdbcColumn(id = true)
    String mst_id;

    @JdbcColumn(id = true)
    String car_id;

    public String getMst_id() {
        return mst_id;
    }

    public void setMst_id(String mst_id) {
        this.mst_id = mst_id;
    }

    public String getCar_id() {
        return car_id;
    }

    public void setCar_id(String car_id) {
        this.car_id = car_id;
    }
}
