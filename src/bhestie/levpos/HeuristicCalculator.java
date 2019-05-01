package bhestie.levpos;

import java.util.concurrent.Semaphore;

public class HeuristicCalculator extends Thread {
	
	private boolean running = false;
	private boolean alive = true;
	private Semaphore semaphore = new Semaphore(0);
	
	public HeuristicCalculator(ThreadGroup threadroup) {
		super(threadroup, "Heuristic Calculator");
	}
	
	@Override
	public void run() {
		bhestie.levpos.State current = null;
		while (this.alive) {
			while (this.running) {
				HeuristicCalculatorGroup.semaphoreStatesToBeCalculated.acquireUninterruptibly();
				current = HeuristicCalculatorGroup.statesToCalculateCache.poll();
				if (current != null) { // Call to cache values
					current.isTerminal();
					current.getHeuristic();
				}
			}
			this.semaphore.acquireUninterruptibly();
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
