package com.liup.project.generator.builder;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.liup.project.generator.AbstractRepositoryBuilder;
import com.liup.project.generator.bean.DBConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/repository-generator-context.xml") 
public class ApiRepositoryGenerator extends AbstractRepositoryBuilder {

	private static final Logger log = LoggerFactory.getLogger(ApiRepositoryGenerator.class);

	@Override
	protected String getModuleName() {
		return "yilian-api";
	}

	@Override
	protected String getAdminRoot() {
		return null;
	}
	
	@Before public void init(){
		moduleName = getModuleName();
		
		config = ConfigFactory.getConfig();
	}
	
	@Override
	protected String getJavaFileOutputFolder() {
		// 自定义输出路径
//		return "d:\\tmp";
		return super.getJavaFileOutputFolder();
	}
	
	@Test public void build() throws Exception {
		buildSingleRepository();
//		buildAllRepositories();
	}
	
	private void buildSingleRepository() throws Exception {
		String[] tables = new String[]{"t_chat_top"};
		generateStubs(tables);
	}

	private void buildAllRepositories() throws Exception {
		List<String> tableNames = getAllTableNames();
		generateStubs(tableNames.toArray(new String[0]));
	}

}
