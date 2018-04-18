package com.yarra.testsuite.hive;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public class RemovableCompletedTask implements Runnable {
	final static Logger log = Logger.getLogger(RemovableCompletedTask.class);
	AtomicBoolean isDone;
	BlockingQueue<Future<Long>> queue ;
	RemovableCompletedTask (BlockingQueue<Future<Long>> queue, AtomicBoolean poolShutDown) {
		log.info("RemovableCompletedTask Constructor ********************");
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
		} finally {
			isDone = null;
			queue = null;
		}
	}
	
}
