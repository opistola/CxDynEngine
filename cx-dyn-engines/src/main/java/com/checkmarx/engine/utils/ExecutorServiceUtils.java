package com.checkmarx.engine.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ExecutorServiceUtils {
	
	private ExecutorServiceUtils() {
		// static class
	}
	
	public static ThreadFactory buildThreadFactory(String nameFormat, boolean daemon) {
		final ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat(nameFormat)
				.setDaemon(daemon)
				.build();
		return threadFactory;
	}
	
	public static ExecutorService buildPooledExecutorService(int count, String nameFormat, boolean daemon) {
		return Executors.newFixedThreadPool(count, buildThreadFactory(nameFormat, daemon));
	}

	public static ExecutorService buildSingleThreadExecutorService(String nameFormat, boolean daemon) {
		return Executors.newSingleThreadExecutor(buildThreadFactory(nameFormat, daemon));
	}

	public static ScheduledExecutorService buildScheduledExecutorService(String nameFormat, boolean daemon) {
		return Executors.newSingleThreadScheduledExecutor(buildThreadFactory(nameFormat, daemon));
	}
	
	

}
