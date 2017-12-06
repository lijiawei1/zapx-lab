package org.zap.framework.common.excel.parser;

/**
 * 处理
 * Created by Shin on 2017/5/9.
 */
public interface ExcelBusiHandler<T> {

    /**
     * 文件缓存Key生成
     * @param excelResult
     * @param config
     * @return
     */
    String cacheKey(ExcelResult<T> excelResult, ExcelImportRequest config);

    /**
     * 业务逻辑检查
     * @param excelResult
     * @param config
     */
    void checkBusiData(ExcelResult<T> excelResult, ExcelImportRequest config, ExcelBusiHolder<T> holder);

    /**
     * 数据更新操作
     * @param config
     * @param holder
     */
    void updateData(ExcelImportRequest config, ExcelBusiHolder<T> holder);

}
