package com.liup.project.generator.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liup.project.generator.bean.DBConfig;


public final class ConfigFactory {

	private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);
	
	public static DBConfig getConfig(){
		DBConfig config = new DBConfig();
		config.setIp("127.0.0.1");
		config.setPort(3306);
		config.setUser("root");
		config.setPassword("liup");
		config.setDb("liup");
		
		return config;
	}
	
	private ConfigFactory(){}
}
