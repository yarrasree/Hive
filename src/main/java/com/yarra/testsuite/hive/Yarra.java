package com.yarra.testsuite.hive;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public class Yarra {
	final static Logger log = Logger.getLogger(Yarra.class);
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService cachedPool = Executors.newCachedThreadPool();
        // Cast the object to its class type
        ThreadPoolExecutor pool = (ThreadPoolExecutor) cachedPool;
        pool.setCorePoolSize(10);
        BlockingQueue<Future<Long>> queue = new LinkedBlockingQueue<Future<Long>>();
        AtomicBoolean isDone = new AtomicBoolean(false);
        Thread removerThread = new Thread(new RemoveCompletedTask(queue, isDone));
        removerThread.start();
        for (int i =0; i<100; i++) {
	        queue.add(pool.submit(new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					return 100l;
				}}));
        }
        for (Future<Long> f: queue){
        	log.info(" isDone: " +f.isDone() +" get:: "+f.get());
        }
        while(true) {
        	if (queue.size() <= 0) {
        		log.info(" Thread cache pool is going to shutdown ******** ");
        		 pool.shutdown();
        		 isDone.set(true);
        		 break;
        	}
        }
        
       
	}
	
	
}

class RemoveCompletedTask implements Runnable {
	final static Logger log = Logger.getLogger(RemoveCompletedTask.class);
	AtomicBoolean isDone;
	BlockingQueue<Future<Long>> queue ;
	RemoveCompletedTask (BlockingQueue<Future<Long>> queue, AtomicBoolean poolShutDown) {
		this.queue = queue;
		this.isDone= poolShutDown;
	}
	
	@Override
	public void run() { 
		try {
			while (true) {
				for ( Future<Long> future : queue) {
					if (future.isDone()) {
						log.debug("Removed completed Task : " + queue.remove(future));
					}
				}
				if (isDone.get()) break;
		   }
		}
		catch(Exception ex) {
			log.error("Exception @ run method of RemoveCompletedTask :: " + ex);
		}
	}
	
}

