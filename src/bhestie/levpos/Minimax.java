package bhestie.levpos;
import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Minimax {
	private Minimax() {}

	/**
	 * TRUE=Black, FALSE=White
	 */
	public static boolean player = false; // White default
	
	private static final int TIMEOUT = 10;
	public static final Duration timeout = Duration.ofSeconds(TIMEOUT);
	private static int esplorati = 0;
	private static boolean signal = false;

	private static final ComparatoreEuristica comparatore = new ComparatoreEuristica();

	public static State minimaxDecision(State state) {
		esplorati = 0;
		signal = false;
		Stream<State> stream = state.getActions().stream().parallel();
		List<State> lista = stream.collect(Collectors.toList());
		Thread t = new Thread(new Run(timeout.getSeconds()));
		t.start();
		State result = lista.stream().max(Comparator.comparing(Minimax::minValue)).get();
		t.interrupt();
		System.out.println("Esplorati = " + esplorati);
		return result;
	}

	private static double maxValue(State state) {
		esplorati++;
		if(signal)
			return 0;
		if(state.isTerminal()){
			return state.getUtility();
		}
		return state.getActions().stream().parallel()
				//.sorted(comparatore).limit(1)
				.map(Minimax::minValue)
				.max(Comparator.comparing(Double::valueOf)).get();
	}

	private static double minValue(State state) {
		esplorati++;
		if(signal)
			return 0;
		if(state.isTerminal()){
			return state.getUtility();
		}
		return state.getActions().stream().parallel()
				//.sorted(comparatore).limit(1)
				.map(Minimax::maxValue)
				.min(Comparator.comparing(Double::valueOf)).get();
	}
	
	private static long lastRun = 0;
	private static final void clean(){
		long used  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//		System.out.println(used);
		if(used > 100000000 && lastRun + 10000 < System.currentTimeMillis()){
			System.out.println("GC running");
			lastRun = System.currentTimeMillis();
			System.gc();
		}
	}
	public static List<State> stack;
	static{
		stack = new LinkedList<State>();
	}
	private static double maxHeuFound = -Double.MAX_VALUE;
	public static long nodeExplored = 0;
	public static final double alphaBeth(final State n, final int depth, double alpha, double beth, final boolean max){
		nodeExplored++;
		double v = 0;
		if(n.isTerminal()){
			final double utility = n.getUtility();
			if (max) {
				if (utility > maxHeuFound) {
					stack.clear();
					maxHeuFound = utility;
					stack.add(n);
				} else if (utility == maxHeuFound) {
					stack.add(n);
				}
			}
			return utility;
		} else if(depth == 0) {
			final double heuristic = n.getHeuristic();
			if (heuristic > maxHeuFound) {
				stack.clear();
				maxHeuFound = heuristic;
				stack.add(n);
			} else if (heuristic == maxHeuFound) {
				stack.add(n);
			}
			return heuristic;
		} else if(max){
			v = -Double.MAX_VALUE;
			for(State c : n.getActions()){
				v = Math.max(v, alphaBeth(c, depth - 1, alpha, beth, false));
				alpha = Math.max(alpha, v);
				if(beth <= alpha){
					//clean();
					break;
				}
			}
			return v;
		}else{
			v = Double.MAX_VALUE;
			for(State c : n.getActions()){
				v = Math.min(v, alphaBeth(c, depth - 1, alpha, beth, true));
				beth = Math.min(beth, v);
				if(beth <= alpha){
					break;
				}
			}
		}
		return v;
	}

	public static synchronized void interrupt() {
		Minimax.signal = true;
	}

}

class ComparatoreEuristica implements Comparator<State> {
	@Override
	public int compare(State o1, State o2) {
		return (int) (o1.getHeuristic() - o2.getHeuristic());
	}
}

class Run implements Runnable {
	private long secs;
	public Run(long l) {
		this.secs = l;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000 * secs);
			//LockSupport.parkNanos(Minimax.timeout.getNano());
			Minimax.interrupt();
			System.out.println("Segnalato");
		} catch (Exception e) {
			return;
		}
	}

}