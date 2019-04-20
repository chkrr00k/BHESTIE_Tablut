package bhestie.levpos;
import java.util.LinkedList;
import java.util.List;

public final class Minimax {
	
	private static final int TIMEOUT = 30;
	
	private static Thread interrupterThread = new Thread(new Interrupter(TIMEOUT), "Interrupter");
	
	private Minimax() {}

	/**
	 * TRUE=Black, FALSE=White
	 */
	public static boolean player = false; // White default
	
	private static boolean signal = false;

	public final static List<State> stack;
	static{
		stack = new LinkedList<State>();
	}
	
	/**
	 * 
	 * @param n
	 * @param depth
	 * @param alpha
	 * @param beth
	 * @param player
	 * @return
	 */
	public static final double alphaBethInit(final State state, final int depth) {
		Minimax.signal = false;
		interrupterThread.start();
		double alphaBethResult = alphaBeth(state, depth, -Double.MAX_VALUE, Double.MAX_VALUE, !Minimax.player);
		interrupterThread.interrupt();
		return alphaBethResult;
	}
	
	private static double maxHeuFound = -Double.MAX_VALUE;
	public static long nodeExplored = 0;
	private static final double alphaBeth(final State s, final int depth, double alpha, double beth, final boolean max){
		nodeExplored++;
		double v = 0;
		if(s.isTerminal()){
			final double utility = s.getUtility();
			if (max) {
				if (utility > maxHeuFound) {
					stack.clear();
					maxHeuFound = utility;
					stack.add(s);
				} else if (utility == maxHeuFound) {
					stack.add(s);
				}
			}
			return utility;
		} else if(depth == 0 || signal) {
			final double heuristic = s.getHeuristic();
			if (heuristic > maxHeuFound) {
				stack.clear();
				maxHeuFound = heuristic;
				stack.add(s);
			} else if (heuristic == maxHeuFound) {
				stack.add(s);
			}
			return heuristic;
		} else if(max){
			v = -Double.MAX_VALUE;
			for(State c : s.getActions()){
				v = Math.max(v, alphaBeth(c, depth - 1, alpha, beth, false));
				alpha = Math.max(alpha, v);
				if(beth <= alpha || signal){
					//clean();
					break;
				}
			}
			return v;
		}else{
			v = Double.MAX_VALUE;
			for(State c : s.getActions()){
				v = Math.min(v, alphaBeth(c, depth - 1, alpha, beth, true));
				beth = Math.min(beth, v);
				if(beth <= alpha || signal){
					break;
				}
			}
		}
		return v;
	}

	public static synchronized void interrupt() {
		Minimax.signal = true;
	}
	
	private static long lastRun = 0;
	@SuppressWarnings("unused")
	private static final void clean(){
		long used  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//		System.out.println(used);
		if(used > 100000000 && lastRun + 10000 < System.currentTimeMillis()){
			System.out.println("GC running");
			lastRun = System.currentTimeMillis();
			System.gc();
		}
	}

}

class Interrupter implements Runnable {
	private final long secs;
	public Interrupter(long l) {
		this.secs = l;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000 * secs);
			//LockSupport.parkNanos(Minimax.timeout.getNano());
			Minimax.interrupt();
			System.out.println("Signaled");
		} catch (Exception e) {
			return;
		}
	}

}