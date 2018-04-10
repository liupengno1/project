package com.liup.project.generator.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liup.project.generator.bean.DBConfig;


public final class ConfigFactory {

	private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);
	
	public static DBConfig getConfig(){
		DBConfig config = new DBConfig();
		config.setIp("123.57.230.40");
		config.setPort(3306);
		config.setUser("yilian_dev");
		config.setPassword("Yilian123.");
		config.setDb("yilian_dev");
		
		return config;
	}
	
	private ConfigFactory(){}
}
