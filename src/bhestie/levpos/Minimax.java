package bhestie.levpos;
import java.util.LinkedList;
import java.util.List;

public final class Minimax {
	
	public static boolean FIXEDDEPTH = false; // If true the DEPTH can't be modified
	public static int DEPTH = 3;
	
	public static int TIMEOUT = 60; // In seconds
	
	private static Interrupter interrupter;
	
	public static final long MAXVALUE = 1000000L;
	
	private static ThreadPool threadPool = ThreadPool.getInstance();
	
	public static void init() {
		interrupter = new Interrupter(TIMEOUT);
	}
	
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
	 * @param state Stato where find next best solution
	 * @param depth Max depth
	 * @return The alphabeth value
	 */
	@SuppressWarnings("deprecation")
	public static final long alphaBethInit(final State state) {
		maxHeuFound = -Minimax.MAXVALUE;
		Minimax.signal = false;
		nodeExplored = 0;
		Thread interrupterThread = new Thread(interrupter, "Interrupter");
		interrupterThread.setDaemon(true);
		threadPool.playAll();
		interrupterThread.start();
		long alphaBethResult = alphaBeth(state, Minimax.DEPTH, -Minimax.MAXVALUE, Minimax.MAXVALUE, true);
		interrupterThread.interrupt();
		if (!Minimax.FIXEDDEPTH && !Minimax.signal) {
			Minimax.DEPTH++;
			System.out.println("Increasing DEPTH. Now=" + Minimax.DEPTH); // TODO remove for the last commit
		}
		threadPool.pauseAll();
		return alphaBethResult;
	}
	
	private static long maxHeuFound;
	public static long nodeExplored = 0; // TODO remove in the last commit
	private static final long alphaBeth(final State s, final int depth, double alpha, double beth, final boolean max){
		nodeExplored++;
		long v = 0;
		if(s.isTerminal()){
			final long utility = s.getUtility();
			if (utility > maxHeuFound) {
				stack.clear();
				maxHeuFound = utility;
				stack.add(s);
			} else if (utility == maxHeuFound) {
				stack.add(s);
			}
			return utility;
		} else if(depth == 0 || signal) {
			final long heuristic = s.getHeuristic();
			if (heuristic > maxHeuFound) {
				stack.clear();
				maxHeuFound = heuristic;
				stack.add(s);
			} else if (heuristic == maxHeuFound) {
				stack.add(s);
			}
			return heuristic;
		} else if(max){
			v = -Minimax.MAXVALUE;
			for(State c : s.getChildren()){
				v = Math.max(v, alphaBeth(c, depth - 1, alpha, beth, false));
				alpha = Math.max(alpha, v);
				if(beth <= alpha){
					//clean();
					break;
				}
			}
		}else{
			v = Minimax.MAXVALUE;
			for(State c : s.getChildren()){
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
		if (!Minimax.FIXEDDEPTH) {
			Minimax.DEPTH--;
			System.out.println("Decreasing DEPTH. Now=" + Minimax.DEPTH); // TODO remove for the last commit
			Minimax.FIXEDDEPTH = true;
		}
	}

}

class Interrupter implements Runnable {
	private final long secs;
	public Interrupter(final long l) {
		this.secs = l;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000 * secs - 500);
			//LockSupport.parkNanos(Minimax.timeout.getNano());
			Minimax.interrupt();
			System.out.println("Signaled"); // TODO remove in the last commit
		} catch (Exception e) {
			return;
		}
	}

}