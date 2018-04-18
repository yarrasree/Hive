package com.yarra.testsuite.hive;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;



public class HiveMain {
	final static Logger log = Logger.getLogger(HiveMain.class);
	static String externalPropertyFilePath = null;
	static final String propertyHiveConfigFileName ="hive_config.properties";
	static final String propertyQueryFileName ="hive_query.properties";
	static String userName = "";
	static String pass = "";
	static String driverName = null;
	static String url = null;
	static int cachePoolSize = 100;
	static int initialParallelThreadSize = 1;
	
	private Properties hiveConfigProp = null;
	private BlockingQueue<Future<Long>> queue = new LinkedBlockingQueue<Future<Long>>();
    private AtomicBoolean isDone = new AtomicBoolean(false);
	public static void main(String[] args) {
		log.info("Hive Main....");
		List<String> queries = null;
		HiveMain main = new HiveMain();
		try {
			
			log.info("inside HiveMain");
			externalPropertyFilePath = args[0];
			log.info(externalPropertyFilePath);
			if (externalPropertyFilePath == null || externalPropertyFilePath.trim().length()==0) {
				throw new Exception("External File path should not be null ");
			}
			main.init();
			queries = HiveHelper.loadQueries(externalPropertyFilePath, propertyQueryFileName);
			main.execute(queries);
		}
		catch (Exception e) {
			log.error("Exception at Hive Main :"+e);
		}
		finally {
			queries = null;
			log.info("Hive Main >>>>>>>>>>>>>>>>>>>>>>>>>>>>> end .");
		}
		
	}
	
	public void init () throws Exception {
		 hiveConfigProp = HiveHelper.loadProperties(externalPropertyFilePath, propertyHiveConfigFileName);
		 HiveHelper.prePoolInitialize(hiveConfigProp);
		 Thread removableCompletedThread = new Thread(new RemovableCompletedTask(queue, isDone));
	     removableCompletedThread.start();
	}
	
	public void execute (List<String> queries) {
	
		Random random = new Random();
		ExecutorService cachedPool = Executors.newCachedThreadPool();
        // Cast the object to its class type
        ThreadPoolExecutor pool = (ThreadPoolExecutor) cachedPool;
        pool.setCorePoolSize(cachePoolSize);
        boolean flag = true;
        HiveJdbcClient client = null;
        try {
	  
        	do {
	        	if (queue.size() < 1000 ){
		        	client = new HiveJdbcClient(hiveConfigProp, queries.get(random.nextInt(queries.size())));
		            cpuPerCoreLoadAvg();
		        	refreshSystemInfo();
		        	queue.add(cachedPool.submit(client));
	        	}
	        	
	        } while(flag);
        
        }
        catch (Exception ex) {
        	log.error( "Exception @ executive method :: "+ex);
        }
        finally {
        	if (pool !=null && pool.isShutdown()) {
        		pool.shutdown();
        		isDone.set(true);
        		pool = null;
        		log.error(" finally >>>>>>>>>>>>>>>>>>>>> pool is shutdown ...!");
        	}
        }
   }
	
	public double cpuPerCoreLoadAvg() {
		double perCoreLoadAvg = 0;
		try {
			OperatingSystemMXBean osmxBean = (OperatingSystemMXBean) ManagementFactory   
		               .getOperatingSystemMXBean();   
			perCoreLoadAvg = osmxBean.getSystemLoadAverage()/osmxBean.getAvailableProcessors();
			BigDecimal bg = new BigDecimal(perCoreLoadAvg);
			perCoreLoadAvg = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			log.info("Core Load Avg >>>>>> "+perCoreLoadAvg);
		} 
		catch (Exception ex) {
			log.error("Exception @ cpuPerCoreLoadAvg() "+ex);
		}
		return perCoreLoadAvg;
	}
	 
	
	public void refreshSystemInfo() {
		try {
			List<GarbageCollectorMXBean> gcList = ManagementFactory.getGarbageCollectorMXBeans();
			MemoryMXBean m = ManagementFactory.getMemoryMXBean();
			MemoryUsage heapMem = m.getHeapMemoryUsage();
			MemoryUsage  nonHeapMem = m.getNonHeapMemoryUsage();
			int usedRatio = (int) ((heapMem.getUsed()) * 100 / heapMem.getCommitted());
			log.info("Heap Memory >>>>>> "+heapMem);
			log.info("Non-Heap Memory >>>>>> "+nonHeapMem);
			log.info("Memory used ratio >>>>>> "+usedRatio);
			OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
			int cpuNumber = osBean.getAvailableProcessors();
			double loadAverage = osBean.getSystemLoadAverage();
			log.info("CPU No >>>>>> "+cpuNumber);
			log.info("System load Avg >>>>>> "+loadAverage);
			ThreadMXBean tBean = ManagementFactory.getThreadMXBean();
			int totalThreadCount = tBean.getThreadCount();
			log.info("Total Thread Count >>>>>> "+totalThreadCount);
		}
		catch(Exception ex) {
			log.error("Exception @ refreshSystemInfo ::"+ex);
			ex.printStackTrace();
		}
	}
	
	
	

}
