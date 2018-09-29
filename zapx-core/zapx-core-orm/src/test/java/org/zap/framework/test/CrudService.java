package org.zap.framework.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.test.pojo.TestVo;

@Service
@Transactional
@CacheConfig(cacheNames = { "userCache" })
public class CrudService {

	private Logger logger = LoggerFactory.getLogger(CrudService.class);
	
	@Autowired
	private BaseDao baseDao;
	
	@CachePut(key = "#test.id")
	public TestVo add(TestVo test) {
		baseDao.insert(test);
		return test;
	}
	
	@CachePut(key = "#test.id")
	public TestVo update(TestVo test) {
		baseDao.update(test);
		return test;
	}
	
	 //�Ƴ�ָ��key������
	@CacheEvict(key = "#user.id")
	public TestVo remove(TestVo test) {
		baseDao.deleteByPrimaryKey(TestVo.class, test.getId());
		return test;
	}
	
	@CacheEvict(allEntries = true)
	public void removeAll() {
//		baseDao.deleteByClause(TestVo.class, "");
	}
	
	@Cacheable(key = "#id")
	public TestVo find(String id) {
		logger.debug("==================�����ݿ��ѯ�л�ȡ����================");
		return baseDao.queryByPrimaryKey(TestVo.class, id);
	}
	
}
