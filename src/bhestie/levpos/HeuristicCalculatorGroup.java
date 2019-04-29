package bhestie.levpos;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class HeuristicCalculatorGroup {
	public static final Queue<State> statesToCalculateCache = new LinkedBlockingQueue<>();
	
	private LinkedList<HeuristicCalculator> threads = new LinkedList<>();
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
	
	public void playAll() {
		this.threads.forEach(HeuristicCalculator::play);
	}
	
	public void pauseAll() {
		this.threads.forEach(HeuristicCalculator::pause);
		statesToCalculateCache.clear();
	}
	
}
