package bhestie.levpos;
import java.util.LinkedList;
import java.util.List;

public final class Minimax {
	
	/**
	 * If the scaling system is disabled.<br/>
	 * If true the depth can't be modified.
	 * @see Minimax.DEPTH
	 */
	public static boolean FIXEDDEPTH = false;
	/**
	 * Current depth at which the program is operating. It's also the starting depth position.
	 * @see Minimax.FIXEDDEPTH
	 */
	public static int DEPTH = 3; // current depth
	/**
	 * Time the algorithm can be executed before performing a scaling up.<br/>
	 * If set at 0 it updates every time it gets executed, if it's at 1 it gets executed two times and then it gets updated.<br/> 
	 * If the scaling system is disabled this part is ignored
	 * @see Minimax.DEPTH
	 * @see Minimax.FIXEDDEPTH
	 */
	public static int SCALINGFACTORUP = 10; // time it has to be signaled to start scaling up
	/**
	 * Time the algorithm can be signaled before performing a scaling up.<br/>
	 * If set at 0 it updates every time it gets signaled, if it's at 1 it gets signaled two times and then it gets updated.<br/> 
	 * If the scaling system is disabled this part is ignored
	 * @see Minimax.DEPTH
	 * @see Minimax.FIXEDDEPTH
	 */
	public static int SCALINGFACTORDOWN = 0; // time it has to be signaled to start scaling down
	
	private static int CURRENTSCALINGUP = 0, CURRENTSCALINGDOWN = 0; //current times it has been signaled to go up or down;
	
	public static int TIMEOUT = 59; // In seconds
	
	private static Interrupter interrupter;
	
	public static long MAXVALUE = 1000000L;
	
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
	public static final long alphaBethInit(final State state) {
		maxHeuFound = - Minimax.MAXVALUE;
		Minimax.signal = false;
		
		nodeExplored = 0;
		
		State.MULTIPLICATOR = 1;
		Minimax.MAXVALUE = 1000000L;
		for (int i = 0; i < Minimax.DEPTH; i++) { // = 2^depth
			State.MULTIPLICATOR *= 2;
			Minimax.MAXVALUE *= 2;
		}
		
		Thread interrupterThread = new Thread(interrupter, "Interrupter");
		interrupterThread.setDaemon(true);
		interrupterThread.start();
		long alphaBethResult = alphaBeth(state, Minimax.DEPTH, -Long.MAX_VALUE, Long.MAX_VALUE, true);
		interrupterThread.interrupt();
		if (!Minimax.FIXEDDEPTH && !Minimax.signal) {
			Minimax.scaleUp();
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
			final long utility = s.getHeuristic();
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
        v = -Long.MAX_VALUE;
        boolean entered = false;
        for(State c : s.getChildren()){
        		entered = true;
				v = Math.max(v, alphaBeth(c, depth - 1, alpha, beth, false));
				alpha = Math.max(alpha, v);
				if(beth <= alpha){
					//clean();
					break;
				}
			}
        if (!entered) {
        	v = -v;
        }
		}else{
        v = Long.MAX_VALUE;
        boolean entered = false;
        for(State c : s.getChildren()){
        		entered = true;
				v = Math.min(v, alphaBeth(c, depth - 1, alpha, beth, true));
				beth = Math.min(beth, v);
				if(beth <= alpha){
					break;
				}
			}
        if (!entered) {
        	v = -v;
        }
		}
		return v;
	}

	private static void scaleUp(){
		if(!Minimax.FIXEDDEPTH){
			if(Minimax.CURRENTSCALINGUP >= Minimax.SCALINGFACTORUP - 1){
				Minimax.DEPTH++;
				Minimax.CURRENTSCALINGUP = 0;
				Minimax.CURRENTSCALINGDOWN = 0;
				System.out.println("Increasing DEPTH. Now=" + Minimax.DEPTH); // TODO remove for the last commit
			}else{
				Minimax.CURRENTSCALINGUP++;
			}
		}
	}
	
	public static synchronized void interrupt() {
		Minimax.signal = true;
		if(!Minimax.FIXEDDEPTH){
			if (Minimax.CURRENTSCALINGDOWN >= Minimax.SCALINGFACTORDOWN - 1) {
				Minimax.DEPTH--;
				Minimax.CURRENTSCALINGDOWN = 0;
				Minimax.CURRENTSCALINGUP = 0;
				System.out.println("Decreasing DEPTH. Now=" + Minimax.DEPTH); // TODO remove for the last commit
			}else{
				Minimax.CURRENTSCALINGDOWN++;
			}
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
			Minimax.interrupt();
			System.out.println("Signaled"); // TODO remove in the last commit
		} catch (Exception e) {
			return;
		}
	}

}