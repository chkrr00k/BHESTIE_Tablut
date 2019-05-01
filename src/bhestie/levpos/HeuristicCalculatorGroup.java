package bhestie.levpos;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class HeuristicCalculatorGroup {
	public static final Queue<State> statesToCalculateCache = new LinkedBlockingQueue<>();
	
	public static final Semaphore semaphoreStatesToBeCalculated = new Semaphore(0);
	
	private List<HeuristicCalculator> threads = new ArrayList<>(5);
	private ThreadGroup threadGroup = new ThreadGroup("Heuristic Calculator Group");
	
	private HeuristicCalculatorGroup() {}
	private static HeuristicCalculatorGroup instance = new HeuristicCalculatorGroup();
	public static HeuristicCalculatorGroup getInstance() {
		return instance;
	}
	
	public void addThreads(int num) {
		for (int i = 0; i < num; i++) {
			HeuristicCalculator t = new HeuristicCalculator(this.threadGroup);
			this.addThread(t);
		}
	}
	
	public void addThread(HeuristicCalculator t) {
		this.threads.add(t);
		if (!t.isAlive())
			t.start();
	}
	
	public void killAll() {
		for (HeuristicCalculator thread : this.threads) {
			thread.kill();
		}
		semaphoreStatesToBeCalculated.drainPermits();
		statesToCalculateCache.clear();
	}
	
	public void playAll() {
		for (HeuristicCalculator thread : this.threads) {
			thread.play();
		}
	}
	
	public void pauseAll() {
		for (HeuristicCalculator thread : this.threads) {
			thread.pause();
		}
		semaphoreStatesToBeCalculated.drainPermits();
		statesToCalculateCache.clear();
	}
	
}
