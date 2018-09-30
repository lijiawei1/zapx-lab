package org.zap.framework.dao.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zap.framework.common.entity.*;
import org.zap.framework.common.entity.pagination.PaginationRequest;
import org.zap.framework.common.entity.pagination.PaginationSupport;
import org.zap.framework.common.json.CustomObjectMapper;
import org.zap.framework.dao.service.IQueryService;
import org.zap.framework.exception.BusinessException;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.util.SqlUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;

@Service
public class QueryServiceImpl implements IQueryService {

    @Override
    public PaginationSupport queryPage(PaginationRequest request, String where, String alias, String resource) {
        return (PaginationSupport) queryInner(request, where, alias, readSql(resource), true, null);
    }

    @Override
    public ListSupport queryList(PaginationRequest request, String where, String alias, String resource) {
        return queryList(request, where, alias, resource, null);
    }

    @Override
    public ListSupport queryList(PaginationRequest request, String where, String alias, String resource,
                                 Consumer<Map<String, Object>> consumer) {
        return (ListSupport) queryInner(request, where, alias, readSql(resource), false, consumer);
    }

    Object queryInner(PaginationRequest request, String where, String alias, String sql, boolean page,
                 Consumer<Map<String, Object>> consumer) {
        return query(request, where, alias, sql, page, consumer, new FilterRule[]{});
    }

    /**
     *
     * @param request
     * @param where
     * @param alias
     * @param sql
     * @param page
     * @param consumer
     * @param extRules
     * @return
     */
    public Object query(PaginationRequest request, String where, String alias, String sql, boolean page,
                               Consumer<Map<String, Object>> consumer, FilterRule[] extRules) {
        //增强多字段排序
        String orderPart = SqlUtils.getSortPart(request.getSortName(), request.getSortOrder());
        List<Map<String, Object>> a = new ArrayList<>();
        try {
//            String sql = readSql(resource);

            String commandText = "";
            Map<String, Object> parmsMap = new HashMap<>();

            FilterGroup group = new FilterGroup();
            if (StringUtils.isNotBlank(where)) {
                group = read(where, FilterGroup.class);
            }

            //插入新条件
            if (extRules != null && extRules.length > 0) {
                if (group.getRules() != null) {
                    group.getRules().addAll(Arrays.asList(extRules));
                } else {
                    group.setRules(Arrays.asList(extRules));
                }
            }

            if (group.getRules() != null && group.getRules().size() > 0) {
                //翻译where条件
                FilterTranslator whereTranslator = new FilterTranslator(group, alias, getBaseDao().getDbTypeString(), true);
                //翻译的sql和参数
                commandText = whereTranslator.getNameParmsCommandText();
                parmsMap = whereTranslator.getParmsMap();

                if (consumer != null) {
                    consumer.accept(parmsMap);
                }

                //插入条件
                if (StringUtils.isNotBlank(commandText)) {
                    sql = sql.replace("/*AND*/", " AND " + commandText);
                    sql = sql.replace("/*WHERE*/", " WHERE " + commandText);
                }
            }

            sql += (" " + orderPart);

            if (page) {
                //分页表格
                return getBaseDao().queryPage(sql, request.getPage(), request.getPageSize(), parmsMap);
            } else {
                //非分页表格
                a = getBaseDao().queryForEnhanceMapList(sql, parmsMap);
                return new ListSupport(a, a.size(), parmsMap);
            }
        } catch (Exception ex) {
            log.error("查询错误", ex);
            return page ? new LigerGridPager<>(new PaginationSupport<>()) : new LigerGrid();
        }
    }

    /**
     * 查询对象列表
     * @param request
     * @param where
     * @param alias
     * @param sql
     * @param clazz
     * @param consumer
     * @param <T>
     * @return
     */
    @Override
    public <T> ListSupport queryForEntityList(PaginationSupport request, String where, String alias, String sql, Class<T> clazz,
                                              Consumer<Map<String, Object>> consumer) {
        //增强多字段排序
        String orderPart = SqlUtils.getSortPart(request.getSortname(), request.getSortorder());
        List<T> a = new ArrayList<>();
        try {
//            String sql = readSql(resource);

            String commandText = "";
            Map<String, Object> parmsMap = new HashMap<>();

            FilterGroup group = new FilterGroup();
            if (StringUtils.isNotBlank(where)) {
                group = read(where, FilterGroup.class);
            }
            if (group.getRules() != null && group.getRules().size() > 0) {
                //翻译where条件
                FilterTranslator whereTranslator = new FilterTranslator(group, alias, getBaseDao().getDbTypeString(), true);
                //翻译的sql和参数
                commandText = whereTranslator.getNameParmsCommandText();
                parmsMap = whereTranslator.getParmsMap();

                if (consumer != null) {
                    consumer.accept(parmsMap);
                }

                //插入条件
                if (StringUtils.isNotBlank(commandText)) {
                    sql = sql.replace("/*AND*/", " AND " + commandText);
                    sql = sql.replace("/*WHERE*/", " WHERE " + commandText);
                }
            }

            sql += (" " + orderPart);
            //非分页表格
            a = getBaseDao().getNameTemplate().query(sql, parmsMap, new BeanListExtractor<>(clazz));
            return new ListSupport(a, a.size(), parmsMap);

        } catch (Exception ex) {
            log.error("查询错误", ex);
            return new ListSupport();
        }

    }

    /**
     * 读取SQL脚本
     *
     * @param src
     * @return
     */
    public String readSql(String src) {
        String path = this.getClass().getResource(src).getPath();
        try {
            return FileUtils.readFileToString(new File(path), Charset.forName("UTF-8"));
        } catch (IOException e) {
            LoggerFactory.getLogger(this.getClass()).error("读取SQL脚本路径出错：" + src, e);
            return "";
        }
    }

    public String json(Object value) {
        try {
            return customObjectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据格式JSON化出错", e);
        }
    }

    public <T> T read(String value, Class<T> clazz) {
        try {
            return customObjectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据格式JSON读取出错", e);
        } catch (IOException e) {
            throw new BusinessException("数据格式JSON读取出错", e);
        }
    }

    public <T> T read(String value, TypeReference<T> typeReference) {
        try {
            return customObjectMapper.readValue(value, typeReference);
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据格式JSON读取出错", e);
        } catch (IOException e) {
            throw new BusinessException("数据格式JSON读取出错", e);
        }
    }

    Logger log = LoggerFactory.getLogger(QueryServiceImpl.class);

    @Autowired
    CustomObjectMapper customObjectMapper;

    @Autowired
    IBaseDao baseDao;

    public IBaseDao getBaseDao() {
        return baseDao;
    }
}
