package org.zap.framework.orm.itf;

/**
 * Created by Shin on 2016/4/21.
 */
public interface IDataLang {


    /**
     * @return
     */
    public String typeUuid();

    /**
     *
     */
    public String typeRemark();

    /**
     * 普通文本100
     *
     * @return
     */
    public String typeText();

    /**
     * @return
     */
    public String typeInt();

    /**
     * 整形
     *
     * @return
     */
    public String typeDouble();

    /**
     * @param length    长度
     * @param precision 精度
     * @return
     */
    public String typeNumber(int length, int precision);

    /**
     * 变长
     *
     * @param length 长度
     * @return
     */
    public String typeVarchar2(int length);

    /**
     * 固定长度
     *
     * @param length 长度
     * @return
     */
    public String typeChar(int length);

    /**
     *
     */
    public String typeLocalDateTime();

    /**
     *
     */
    public String typeLocalDate();

    /**
     * @return
     */
    public String typeDate();

}
