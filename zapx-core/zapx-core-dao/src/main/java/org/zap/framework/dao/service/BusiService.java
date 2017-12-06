package org.zap.framework.dao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zap.framework.common.entity.*;
import org.zap.framework.common.json.CustomObjectMapper;
import org.zap.framework.exception.BusinessException;
import org.zap.framework.orm.annotation.JdbcTable;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.common.entity.pagination.PaginationSupport;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.util.SqlUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 业务相关接口
 *
 * @author Shin
 */
@Service("busiService")
@Transactional
public class BusiService extends BaseService {

    Logger log = LoggerFactory.getLogger(BusiService.class);

    @Autowired
    CustomObjectMapper customObjectMapper;

    public CustomObjectMapper getCustomObjectMapper() {
        return customObjectMapper;
    }

    public void setCustomObjectMapper(CustomObjectMapper customObjectMapper) {
        this.customObjectMapper = customObjectMapper;
    }

    /**
     * @param clazz    实体
     * @param req      请求封装
     * @param where    前台封装的条件
     * @param alias    别名
     * @param sql      查询脚本
     * @param consumer 参数处理
     * @param <T>
     * @return
     */
    public <T> LigerGrid gridByScript(Class<T> clazz, LigerGridPager<?> req, String where, String alias, String sql, Consumer<Map<String, Object>> consumer) {
        //增强多字段排序
        String orderPart = SqlUtils.getSortPart(req.getSortname(), req.getSortorder());
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
            a = getBaseDao().getNamedParameterJdbcTemplate().query(sql, parmsMap, new BeanListExtractor<>(clazz));
            return new LigerGrid(a, a.size(), parmsMap);

        } catch (Exception ex) {
            log.error("查询错误", ex);
            return new LigerGrid();
        }
    }

    /**
     * 生成报表数据
     *
     * @param lgp      分页控件参数
     * @param where    条件
     * @param alias    拼装条件的别名
     * @param consumer 参数回调
     * @return
     */
    public LigerGrid gridByScript(LigerGridPager<?> lgp, String where, String alias, String resource,
                                  Consumer<Map<String, Object>> consumer) {
        return (LigerGrid) gridCommonByScript(lgp, where, alias, readSql(resource), false, consumer);
    }

    /**
     * @param lgp      分页参数
     * @param where    条件
     * @param alias    别名
     * @param sql      脚本
     * @param page     是否分页数据
     * @param consumer 参数处理
     * @param <T>
     * @return
     */
    public Object gridCommonByScript(LigerGridPager<?> lgp, String where, String alias, String sql, boolean page, Consumer<Map<String, Object>> consumer) {
        //增强多字段排序
        String orderPart = SqlUtils.getSortPart(lgp.getSortname(), lgp.getSortorder());
        List<Map<String, Object>> a = new ArrayList<>();
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

            if (page) {
                //分页表格
                return new LigerGridPager<>(getBaseDao().queryPage(sql, lgp.getPage() - 1, lgp.getPagesize(), parmsMap));
            } else {
                //非分页表格
                a = getBaseDao().queryForEnhanceMapList(sql, parmsMap);
                return new LigerGrid(a, a.size(), parmsMap);
            }
        } catch (Exception ex) {
            log.error("查询错误", ex);
            return page ? new LigerGridPager<>(new PaginationSupport<>()) : new LigerGrid();
        }
    }

    /**
     * 分页报表
     *
     * @param lgp      分页参数
     * @param where    条件
     * @param alias    别名
     * @param resource 资源文件
     * @return
     */

    public LigerGridPager gridPagerByScript(LigerGridPager<?> lgp, String where, String alias, String resource) {
        return (LigerGridPager) gridCommonByScript(lgp, where, alias, readSql(resource), true, null);
    }

    /**
     * 分页报表
     *
     * @param lgp      分页参数
     * @param where    条件
     * @param alias    别名
     * @param resource 资源文件
     * @param consumer 参数处理
     * @return
     */
    public LigerGridPager gridPagerByScript(LigerGridPager<?> lgp, String where, String alias, String resource,
                                            Consumer<Map<String, Object>> consumer) {
        return (LigerGridPager) gridCommonByScript(lgp, where, alias, readSql(resource), true, consumer);
    }

    /**
     * 生成报表数据
     *
     * @param lgp   分页控件参数
     * @param where 条件
     * @param alias 拼装条件的别名
     * @return
     */
    public LigerGrid gridByScript(LigerGridPager<?> lgp, String where, String alias, String resource) {
        return gridByScript(lgp, where, alias, resource, null);
    }


    /**
     * @param clazz
     * @param where
     * @param sortname
     * @param sortorder
     * @param dr
     * @param <T>
     * @return
     */
    public <T> List<T> list(Class<T> clazz, String where, String sortname, String sortorder, boolean dr) {

        String orderPart = StringUtils.isNotBlank(sortname) ? String.format(" ORDER BY %s %s ", sortname, sortorder) : "";
        JdbcTable annotation = clazz.getAnnotation(JdbcTable.class);
        if (annotation == null)
            throw new BusinessException("实体没有JdbcTable注解");

        String alias = annotation.alias();

        if (StringUtils.isNotBlank(where)) {
            try {
                FilterGroup group = customObjectMapper.readValue(where, FilterGroup.class);

                //翻译where条件
                FilterTranslator whereTranslator = new FilterTranslator(group, alias, baseDao.getDbTypeString());
                //翻译的sql和参数
                String commandText = whereTranslator.getCommandText();
                Object[] parmsArray = whereTranslator.getParmsArray();

                return baseDao.queryByClause(clazz, commandText + orderPart, parmsArray);
            } catch (IOException e) {
                throw new BusinessException("WHERE条件解析出错", e);
            }
        }
        return baseDao.queryByClause(clazz, (dr ? alias + ".DR = 0 " : "") + orderPart);
    }

    /**
     * 分页查询接口
     *
     * @param clazz
     * @param req
     * @param where       外部JSON过滤条件
     * @param dr
     * @param filterRules 服务端过滤条件
     * @param <T>
     * @return
     */
    public <T> PaginationSupport<T> page(Class<T> clazz, LigerGridPager<?> req, String where, boolean dr, FilterRule[] filterRules) {

        //增强多字段排序
        String orderPart = SqlUtils.getSortPart(req.getSortname(), req.getSortorder());

        //分页参数
        int currentPage = req.getPage() - 1;
        int pageSize = req.getPagesize();

        //获取表别名
        JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
        String alias = annTable.alias();

        FilterGroup group = new FilterGroup();

        if (StringUtils.isNotBlank(where)) {
            try {
                group = customObjectMapper.readValue(where, FilterGroup.class);
            } catch (IOException e) {
            }
        }

        //DR条件
        if (dr) {
            group.getRules().add(new FilterRule("DR", 0));
        }

        //添加额外的条件
        if (filterRules != null && filterRules.length > 0) {
            for (int i = 0; i < filterRules.length; i++) {
                group.getRules().add(filterRules[i]);
            }
        }

        if (group.getRules() != null && group.getRules().size() > 0) {

            //翻译where条件
            FilterTranslator whereTranslator = new FilterTranslator(group, alias, baseDao.getDbTypeString());
            //翻译的sql和参数
            String commandText = whereTranslator.getCommandText();
            Object[] parmsArray = whereTranslator.getParmsArray();
            //数据库分页查询
            return baseDao.queryPage(clazz, commandText + orderPart, parmsArray, currentPage, pageSize);
        }

        return baseDao.queryPage(clazz, orderPart, currentPage, pageSize);
    }


    /**
     * 分页查询
     *
     * @param clazz
     * @param req
     * @param where
     * @param dr
     * @param <T>
     * @return
     */
    public <T> PaginationSupport<T> page(Class<T> clazz, LigerGridPager<?> req, String where, boolean dr) {

        //排序部分
        String orderPart = SqlUtils.getSortPart(req.getSortname(), req.getSortorder());

        //分页参数
        int currentPage = req.getPage() - 1;
        int pageSize = req.getPagesize();
        //获取表别名
        JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
        String alias = annTable.alias();
        //如果where条件不为空
        if (where != null & !"".equals(where)) {
            //反序列化where条件
            FilterGroup group = null;
            try {
                group = customObjectMapper.readValue(where, FilterGroup.class);
            } catch (IOException e) {
                group = new FilterGroup();
            }
            //翻译where条件
            FilterTranslator whereTranslator = new FilterTranslator(group, alias, baseDao.getDbTypeString());
            //翻译的sql和参数
            String commandText = whereTranslator.getCommandText();
            Object[] parmsArray = whereTranslator.getParmsArray();
            //数据库分页查询
            return baseDao.queryPage(clazz, commandText + orderPart, parmsArray, currentPage, pageSize);
        }

        return baseDao.queryPage(clazz, (dr ? alias + ".DR = 0 " : "") + orderPart, currentPage, pageSize);
    }

    public <T> PaginationSupport<T> page(Class<T> clazz, LigerGridPager<?> req, String where) {
        return page(clazz, req, where, true);
    }


    /**
     *
     */
    public <T> PaginationSupport<T> page(T entity, int currentPage, int pageSize, String clause) {
        return (PaginationSupport<T>) queryPage(entity.getClass(), clause, currentPage, pageSize);
    }

    public <T> PaginationSupport<T> page(Class<T> clazz, int currentPage, int pageSize, String clause) {
        return baseDao.queryPage(clazz, clause, currentPage, pageSize);
    }

    public <T> PaginationSupport<T> page(Class<T> clazz, int currentPage, int pageSize, String clause, Object[] params) {
        return baseDao.queryPage(clazz, clause, params, currentPage, pageSize);
    }

    public <T> List<T> tree(Class<T> clazz, String clause, Object... params) {
        return baseDao.queryTreeByClause(clazz, clause, params);
    }

    public <T> List<T> tree(Class<T> clazz, String clause) {
        return baseDao.queryTreeByClause(clazz, clause);
    }

    /**
     */
    public <T> List<T> list(Class<T> clazz, String clause) {
        return baseDao.queryByClause(clazz, clause);
    }

    /**
     */
    public <T> List<T> list(Class<T> clazz, String clause, Object[] params) {
        return baseDao.queryByClause(clazz, clause, params);
    }

    public <T> T get(Class<T> clazz, String id) {
        return (T) baseDao.queryByPrimaryKey(clazz, id);
    }


    /**
     */
    @SuppressWarnings("unchecked")
    public <T> T get(T entity, String id) {
        return (T) baseDao.queryByPrimaryKey(entity.getClass(), id);
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

}
