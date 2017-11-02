package org.zap.framework.orm.dao.dialect.lang;

import org.zap.framework.orm.itf.IDataLang;

/**
 *
 * Created by Shin on 2016/4/21.
 */
public class OracleDataLang implements IDataLang {

    /**
     * 创建临时表
     * @return
     */
    @Override
    public String typeUuid() {
        return typeChar(36);
    }

    @Override
    public String typeRemark() {
        return typeVarchar2(255);
    }

    @Override
    public String typeText() {
        return typeVarchar2(100);
    }

    @Override
    public String typeInt() {
        return "INTEGER";
    }

    @Override
    public String typeDouble() {
        return null;
    }

    @Override
    public String typeNumber(int length, int precision) {
        return null;
    }

    @Override
    public String typeChar(int length) {
        return "CHAR(" + length + ")";
    }

    @Override
    public String typeVarchar2(int length) {
        return "VARCHAR2(" + length + ")";
    }


    @Override
    public String typeLocalDateTime() {
        return typeChar(19);
    }

    @Override
    public String typeLocalDate() {
        return typeChar(10);
    }

    @Override
    public String typeDate() {
        return "DATE";
    }
}
