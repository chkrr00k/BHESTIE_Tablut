package bhestie.levpos;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import bhestie.levpos.State.ParallelStateGenerator;

public class ThreadPool {

	private ExecutorService threads = Executors.newCachedThreadPool();

	public static final BlockingDeque<ParallelStateGenerator> dequeToCalculate = new LinkedBlockingDeque<>();

	private ThreadPool() {}
	private static ThreadPool instance = new ThreadPool();
	public static ThreadPool getInstance() {
		return instance;
	}

	public void setMaxThreads(int num) {
		this.threads = Executors.newFixedThreadPool(num);
	}
	
	private static final PoolThread poolThread = new PoolThread();
	public void add(ParallelStateGenerator stateGenerator) {
		dequeToCalculate.add(stateGenerator);
		this.threads.submit(poolThread);
	}
	
	public void pauseAll() {
		//this.threads.shutdownNow();
		//this.threads = Executors.newFixedThreadPool(4);
		dequeToCalculate.clear();
	}
	
	public void killAll() {
		this.threads.shutdownNow();
		dequeToCalculate.clear();
	}

}

class PoolThread extends Thread implements Runnable {
	
	public PoolThread() {
		super("Parallel Pool Thread");
	}

	@Override
	public void run() {
		try {
			ParallelStateGenerator stateGenerator = ThreadPool.dequeToCalculate.take();
			if (stateGenerator != null)
				stateGenerator.generateAndCache();
		} catch (Exception e) {
		}
	}

}