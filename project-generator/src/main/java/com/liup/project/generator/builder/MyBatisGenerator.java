package com.liup.project.generator.builder;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.joinway.yilian.generator.dao.AbstractMapperBuilder;
import com.joinway.yilian.generator.dao.bean.DBConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/mbg-context.xml") 
public class MyBatisGenerator extends AbstractMapperBuilder {

	private static final Logger log = LoggerFactory.getLogger(MyBatisGenerator.class);
	
	@Override
	protected String getMBGXmlPath() {
		return "d:\\generatorConfig.xml";
	}

	@Before public void init(){
		moduleName = "yilian-data";
		config = ConfigFactory.getConfig();
	}
	
	@Test public void build() throws Exception {
		buildSingleMapper();
//		buildAllMappers();
	}
	
	private void buildSingleMapper() throws Exception {

		String[] tables = new String[]{"t_extension"};

		generateStubs(tables);
		generateDomainConstants(tables);
	}

	private void buildAllMappers() throws Exception {
		List<String> tableNames = getAllTableNames();
		String[] tables = tableNames.toArray(new String[0]);
		
		generateStubs(tables);
		generateDomainConstants(tables);
	}

}
