package com.yarra.testsuite.hive;

public interface Constant {
	public final static String DRIVER_NAME = "hive.jdbc.driver.name";
	public final static String URL = "hive.jdbc.url";
	public final static String USER_NAME = "hive.jdbc.schema.user";
	public final static String PASS = "hive.jdbc.schema.password";
	public final static String CACHE_POOL_SIZE = "concurrent.cache.pool.size";
	public final static String INITIAL_SIZE = "hive.concurrent.thread.initial.size";
	public final static String POOL_INITIAL_SIZE = "hive.connection.pool.initial.size";
	public final static String POOL_MAX_ACTIVE = "hive.connection.pool.max.active";
	public final static String POOL_MAX_WAIT = "hive.connection.pool.max.wait";
	public final static String POOL_MAX_IDLE = "hive.connection.pool.max.idle";
	
	
}
