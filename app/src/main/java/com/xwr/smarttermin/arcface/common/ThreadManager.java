package com.xwr.smarttermin.arcface.common;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManager {

	private static ThreadPool threadPool;

	public static ThreadPool getInstance() {
		if (threadPool == null) {
			synchronized (ThreadManager.class) {
				if (threadPool == null) {
					threadPool = new ThreadPool();//AsyncTask的配置
				}
			}
		}
		return threadPool;
	}

	/**
	 * 自定义线程池
	 */
	public static class ThreadPool {

		private ThreadPoolExecutor executor;

		ThreadPool() {

		}

		public void execute(Runnable r) {
			try {
				if (executor == null) {
					// 参1:核心线程数;参2:最大线程数;参3:线程休眠时间;参4:时间单位;参5:线程队列;参6:生产线程的工厂;参7:线程异常处理策略
          int cpuCount = Runtime.getRuntime().availableProcessors();//cpu数量
					executor = new ThreadPoolExecutor(cpuCount+1, cpuCount*2+1, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(cpuCount*2+1));
				}
				// 线程池执行一个Runnable对象, 具体运行时机线程池说了算
				executor.execute(r);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//从队列中取消任务
		public void cancel(Runnable r) {
			if (executor != null) {
				executor.getQueue().remove(r);
			}
		}


		public void clear(){
			if (executor != null) {
				executor.shutdownNow();
				executor.getQueue().clear();
			}
		}
	}
}
