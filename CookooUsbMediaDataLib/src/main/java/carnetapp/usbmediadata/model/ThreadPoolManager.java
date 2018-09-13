package carnetapp.usbmediadata.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kobe
 * @version 
 * @create 2016年3月13日 上午11:01:54
 * @類說明  线程池管理类:应用内所有使用线程的地方都需要使用线程池进行管理
 **/
public class ThreadPoolManager {
	private static final String DEFAULT_SINGLE_POOL_NAME = "wifi-SingleTread";

	private static ThreadPoolProxy mNetWorkPool = new ThreadPoolProxy("wifi-NetWorkThread",1, 50, 5L);
	private static ThreadPoolProxy mShortPool = new ThreadPoolProxy("wifi-ShortThread",1, 10, 5L);
	private static ThreadPoolProxy mClickUploadPool = new ThreadPoolProxy("wifi-ClickUploadThread",2, 2, 5L);
	private static ThreadPoolProxy mUploadMsgPool = new ThreadPoolProxy("wifi-UploadMsgThread",2, 2, 5L);

	private static Map<String, ThreadPoolProxy> mMap = new HashMap<String, ThreadPoolProxy>();
	private static Object mSingleLock = new Object();

	/** 获取一个用于执行网络任务的线程池 */
	public static ThreadPoolProxy getNetWorkPool() {
		return mNetWorkPool;
	}

	/** 获取一个用于执行短耗时任务的线程池，避免因为和耗时长的任务处在同一个队列而长时间得不到执行，通常用来执行本地的IO/SQL */
	public static ThreadPoolProxy getShortPool() {
		return mShortPool;
	}

	/** 获取一个线程池,用来顺序执行统计上报任务 */
	public static ThreadPoolProxy getUploadPool() {
		return mClickUploadPool;
	}
	
	/** 获取一个线程池,用来顺序执行错误上报任务 */
	public static ThreadPoolProxy getUploadMsgPool() {
		return mUploadMsgPool;
	}

	/** 获取一个单线程池，所有任务将会被按照加入的顺序执行，免除了同步开销的问题 */
	public static ThreadPoolProxy getSinglePool() {
		return getSinglePool(DEFAULT_SINGLE_POOL_NAME);
	}

	/** 获取一个单线程池，所有任务将会被按照加入的顺序执行，免除了同步开销的问题 */
	public static ThreadPoolProxy getSinglePool(String name) {
		synchronized (mSingleLock) {
			ThreadPoolProxy singlePool = mMap.get(name);
			if (singlePool == null) {
				singlePool = new ThreadPoolProxy(name,1, 1, 5L);
				mMap.put(name, singlePool);
			}
			return singlePool;
		}
	}

	/**
	 * @author kobe
	 * @version 
	 * @create 2016年3月13日 上午11:01:54
	 * @類說明 用一个代理来管理线程池
	 **/
	public static class ThreadPoolProxy{
		private static final String DEFAULTNAME = "MyThread";
		private String mThreadName = DEFAULTNAME;

		private ThreadPoolExecutor mPool;
		private int mCorePoolSize = 5;
		private int mMaximumPoolSize = 5;
		private long mKeepAliveTime = 5L;

		private BlockingQueue<Runnable> mPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

		private Object mLock = new Object();

		public ThreadFactory mThreadFactory = new ThreadFactory() {
			private final AtomicInteger mCount = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, mThreadName + "#" + mCount.getAndIncrement());
			}
		};

		public ThreadPoolProxy(int corePoolSize,int maximumPoolSize,long keepAliveTime){
			this(DEFAULTNAME, corePoolSize, maximumPoolSize, keepAliveTime);
		}

		public ThreadPoolProxy(String threadName,int corePoolSize,int maximumPoolSize,long keepAliveTime){
			mThreadName = threadName;
			mCorePoolSize = corePoolSize;
			mMaximumPoolSize = maximumPoolSize;
			mKeepAliveTime = keepAliveTime;
		}

		/**执行任务，当线程池处于关闭，将会重新创建新的线程池*/
		public void execute(Runnable run){
			if(run==null){
				return;
			}

			if(mPool == null || mPool.isShutdown()){
				synchronized (mLock) {
					if(mPool == null || mPool.isShutdown()){
						//参数说明
						//当线程池中的线程小于mCorePoolSize，直接创建新的线程加入线程池执行任务
						//当线程池中的线程数目等于mCorePoolSize，将会把任务放入任务队列BlockingQueue中
						//当BlockingQueue中的任务放满了，将会创建新的线程去执行，
						//mKeepAliveTime是线程执行完任务后，且队列中没有可以执行的任务，存活的时间，后面的参数是时间单位
						//ThreadFactory是每次创建新的线程工厂
						//但是当总线程数大于mMaximumPoolSize时，将会抛出异常，交给RejectedExecutionHandler处理
						mPool = new ThreadPoolExecutor(mCorePoolSize, mMaximumPoolSize, mKeepAliveTime, TimeUnit.MILLISECONDS, mPoolWorkQueue, mThreadFactory, new AbortPolicy());
					}
				}
			}

			mPool.execute(run);
		}

		/** 取消线程池中某个还未执行的任务 */
		public synchronized void cancel(Runnable run) {
			if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
				mPool.getQueue().remove(run);
			}
		}

		/** 线程池队列中是否包含某个任务 */
		public synchronized boolean contains(Runnable run) {
			if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
				return mPool.getQueue().contains(run);
			} else {
				return false;
			}
		}

		/** 立刻关闭线程池，并且正在执行的任务也将会被中断 */
		public void stop() {
			if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
				mPool.shutdownNow();
			}
		}
	}

}
