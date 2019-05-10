package bhestie.levpos;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import bhestie.levpos.State.ParallelStateGenerator;

public class ThreadPool {
	
	private List<PoolThread> threads = new ArrayList<>();
	
	public static final BlockingDeque<StackItems> stackQueuesToCalculate = new LinkedBlockingDeque<>();
	
	private ThreadPool() {}
	private static ThreadPool instance = new ThreadPool();
	public static ThreadPool getInstance() {
		return instance;
	}
	
	public void addThreads(int num) {
		for (int i = 0; i < num; i++) {
			PoolThread t = new PoolThread();
			t.setDaemon(true);
			this.addThread(t);
		}
	}

	private void addThread(PoolThread t) {
		this.threads.add(t);
		if (!t.isAlive())
			t.start();
	}
	
	public void killAll() {
		for (PoolThread thread : this.threads) {
			thread.kill();
		}
		stackQueuesToCalculate.clear();
	}
	
	public void playAll() {
		for (PoolThread thread : this.threads) {
			thread.play();
		}
	}
	
	public void pauseAll() {
		for (PoolThread thread : this.threads) {
			thread.pause();
		}
		stackQueuesToCalculate.clear();
	}
	
}

class PoolThread extends Thread {
	private boolean running = false;
	private Semaphore semaphore = new Semaphore(0);
	
	public PoolThread() {
		super("Parallel Pool Thread");
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				while (running) {
					StackItems items = ThreadPool.stackQueuesToCalculate.take(); // sleep here until it gets the element
					ParallelStateGenerator stateGenerator = items.getStateGenerator();
					Queue<bhestie.levpos.State> queue = items.getQueue();
					stateGenerator.generate();
					bhestie.levpos.State generated = queue.peek();
					if (generated.isTerminal())
						generated.getUtility();
					else
						generated.getHeuristic();
					generated.threatenKingRemaining();
					
				}
				this.semaphore.acquire();
			}
		} catch (Exception e) { // ignore and terminate thread
		}
	}
	
	@SuppressWarnings("deprecation")
	public synchronized void kill() {
		this.stop();
	}
	
	public synchronized void play() {
		this.running = true;
		this.semaphore.release();
	}
	
	public synchronized void pause() {
		this.running = false;
	}
	
}