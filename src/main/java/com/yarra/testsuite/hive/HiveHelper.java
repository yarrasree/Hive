package com.yarra.testsuite.hive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

public class HiveHelper {
	static Logger log = Logger.getLogger(HiveHelper.class);
	
	private static final String BACKWARD_SLASH= "/";
	private final static BasicDataSource dataSource;
	private static String driverName = "org.apache.hive.jdbc.HiveDriver";
	private static String urlName;
	private static String userName;
	private static String pass;
	private static int poolInitialSize;
	private static int poolMaxActive;
	private static int poolMaxWait;
	private static int poolMaxIdle;
	private static boolean isPoolInitialized;
	
	public static int getPropertyValue (String key, Properties prop) {
		int value = 0;
		try {
			String KeyStr = prop.getProperty(key);
			if (KeyStr != null && KeyStr.trim().length() > 0) {
				value = Integer.parseInt(KeyStr);
			}
			
		} catch (Exception ex) {
			log.error("Exception @ parse cache Pool size property String to int : "+ex);
		}
		return value;
	}
	
	public static List<String> loadQueries(String externalPropertyFilePath, String fileName) {
		String line=null;
		BufferedReader br=null;
		String sql="select * from tab"; //Dummy Query
		List<String> queries= new ArrayList<String>();
		try {
			br=new BufferedReader(new FileReader(fileName));
			while((line=br.readLine())!=null) {
				queries.add(line);
			}
		} catch (FileNotFoundException ffe) {
			log.error("Exception @ loadQueries "+ffe);
		} catch (IOException ioe) {
			log.error("Exception @ loadQueries "+ioe);
		} finally {
			if(queries.size()==0) {
				queries.add(sql);
			}
			if(br!=null) {
				try {
					br.close();
				} catch (IOException ioe) {
					log.error("Exception @ loadQueries finally "+ioe);
				}
				br=null;
			}
		}
		return queries;
	}
	
	public static Properties loadProperties (String externalPropertyFilePath, String fileName) {
		Properties prop = new Properties();
		String propertyFilePath = null;
		InputStream input = null;
		try {
			propertyFilePath = validateFilePathAndAddSlashAtEnd(externalPropertyFilePath,fileName);
			log.info("loadProperties @ propertyFilePath :"+propertyFilePath);
			input = new FileInputStream(propertyFilePath);
			// load a config.properties file
			prop.load(input);
		} catch (Exception e) {
			log.error("Exception @ loadProperties " + e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					log.error("Exception at Hive Main finally:" + ex);
				}
			}
			input  = null;
			propertyFilePath = null;
		}
		return prop;
	}
	
	protected static boolean isExistFolderPath (String externalFolderPath) {
		File file = new File(externalFolderPath);
		if (file.exists()) {
			return true;
		}
		return false;
	}
	
	public static String addBackWardSlash(String filePath) {
		if (filePath.endsWith(BACKWARD_SLASH)) 
			return filePath;
		 else
			return filePath + BACKWARD_SLASH;
	}
	
	public static String validateFilePathAndAddSlashAtEnd(String externalFolderPath, String fileName) throws Exception
	{
		if (isExistFolderPath(externalFolderPath)) {
				return addBackWardSlash(externalFolderPath) + fileName;
		} else 
			throw new Exception (" Given file path is not exist : " + externalFolderPath);
	}
	
	public static void prePoolInitialize(Properties configProp) {
		driverName = configProp.getProperty(Constant.DRIVER_NAME);
		urlName = configProp.getProperty(Constant.URL);
		userName = configProp.getProperty(Constant.USER_NAME);
		pass = configProp.getProperty(Constant.PASS);
		pass =  configProp.getProperty(Constant.PASS);
		poolInitialSize = HiveHelper.getPropertyValue(Constant.POOL_INITIAL_SIZE, configProp);
		poolMaxActive = HiveHelper.getPropertyValue(Constant.POOL_MAX_ACTIVE, configProp);
		poolMaxWait = HiveHelper.getPropertyValue(Constant.POOL_MAX_WAIT, configProp);
		poolMaxIdle = HiveHelper.getPropertyValue(Constant.POOL_MAX_IDLE, configProp);
		initializePool();
	}
	
	private static void initializePool() {
       dataSource.setDriverClassName(driverName);
       dataSource.setUrl(urlName);
       dataSource.setUsername(userName);
       dataSource.setPassword(pass);
       dataSource.setInitialSize(poolInitialSize);
       dataSource.setMaxActive(poolMaxActive);
       dataSource.setMaxWait(poolMaxWait);
       dataSource.setMaxIdle(poolMaxIdle);
       isPoolInitialized = true;
	}
	static {
		dataSource = new BasicDataSource();
	}
	public static synchronized DataSource getDataSource() throws Exception {
		if (!hasPoolInitialized()) throw new Exception("Connection pool initialization is failed.");
	    return dataSource;
	}
	
	public static boolean hasPoolInitialized() {
		return isPoolInitialized;
	}
	   
}
