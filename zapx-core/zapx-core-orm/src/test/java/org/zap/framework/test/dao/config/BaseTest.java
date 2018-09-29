package org.zap.framework.test.dao.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.zap.framework.test.pojo.TestVo;

/**
 * 基础配置
 * 初始化测试数据
 */
public abstract class BaseTest {

	protected String DATA_PATH = "/script/data%s.dat";
	protected String DATA_20 = String.format(DATA_PATH, "20");
	protected String DATA_1000 = String.format(DATA_PATH, "1000");
	protected String DATA_10000 = String.format(DATA_PATH, "10000");
	
	protected TestVo[] init20() {
		return init(new ClassPathResource(DATA_20));
	}
	
	protected TestVo[] init1000() {
		return init(new ClassPathResource(DATA_1000));
	}
	
	protected TestVo[] init10000() {
		return init(new ClassPathResource(DATA_10000));
	}
	
	protected abstract TestVo[] init(Resource resource);
	
	
}
