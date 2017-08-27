package com.checkmarx.engine.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses an single threaded ExecutorService to perform a task with a timeout.
 * 
 * @author randy@checkmarx.com
 *
 * @param <T> the task return type
 */
public class TimedTask<T> {
	
	private static final Logger log = LoggerFactory.getLogger(TimedTask.class);

	private final String taskName;
	private final int timeout;
	private final TimeUnit timeUnit;
	
	public TimedTask(String taskName, int timeout, TimeUnit timeUnit) {
		this.taskName = taskName;
		this.timeout = timeout;
		this.timeUnit = timeUnit;
	}
	
	public T execute(Callable<T> task) throws InterruptedException, ExecutionException, TimeoutException {
		log.trace("execute(): task={}; timeout={}; timeUnit={}", taskName, timeout, timeUnit);
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<T> future = executor.submit(task);
		return future.get(timeout, timeUnit);
	}
	
}