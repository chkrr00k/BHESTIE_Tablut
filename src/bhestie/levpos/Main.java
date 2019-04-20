package bhestie.levpos;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
	
	private static List<Pawn> initialState = new LinkedList<>();

	static {
		// Blacks
		initialState.add(new Pawn(true, 4, 1, false));
		initialState.add(new Pawn(true, 5, 1, false));
		initialState.add(new Pawn(true, 6, 1, false));
		initialState.add(new Pawn(true, 5, 2, false));
		
		initialState.add(new Pawn(true, 1, 4, false));
		initialState.add(new Pawn(true, 1, 5, false));
		initialState.add(new Pawn(true, 1, 6, false));
		initialState.add(new Pawn(true, 2, 5, false));
		
		initialState.add(new Pawn(true, 4, 9, false));
		initialState.add(new Pawn(true, 5, 9, false));
		initialState.add(new Pawn(true, 6, 9, false));
		initialState.add(new Pawn(true, 5, 8, false));
		
		initialState.add(new Pawn(true, 9, 4, false));
		initialState.add(new Pawn(true, 9, 5, false));
		initialState.add(new Pawn(true, 9, 6, false));
		initialState.add(new Pawn(true, 8, 5, false));
		
		// King
		initialState.add(new Pawn(false, 5, 5, true));
		
		// White
		initialState.add(new Pawn(false, 5, 6, false));
		initialState.add(new Pawn(false, 5, 7, false));
		initialState.add(new Pawn(false, 4, 5, false));
		initialState.add(new Pawn(false, 3, 5, false));
		initialState.add(new Pawn(false, 5, 3, false));
		initialState.add(new Pawn(false, 5, 4, false));
		initialState.add(new Pawn(false, 6, 5, false));
		initialState.add(new Pawn(false, 7, 5, false));
	}
	
	public static void main(String[] args) throws InterruptedException{
		System.out.println("Used Memory   :  " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes");
	    System.out.println("Free Memory   : " + Runtime.getRuntime().freeMemory() + " bytes");
	    System.out.println("Total Memory  : " + Runtime.getRuntime().totalMemory() + " bytes");
	    System.out.println("Max Memory    : " + Runtime.getRuntime().maxMemory() + " bytes");

	    boolean player = false; //default player
	    if(args.length == 1) {//main invoked with parameters
			if(args[0].equalsIgnoreCase("white"))
				player = true;
			if(args[0].equalsIgnoreCase("black"))//redundant
				player = false;
		}

	    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		long a = System.nanoTime();
		System.out.println(Minimax.alphaBeth(new State(initialState, /*default false*/player), 6, -Double.MAX_VALUE, Double.MAX_VALUE, true));
		long b = System.nanoTime();
		for(State s : Minimax.stack){
			List<State> unfold = s.unfold();
			System.out.println(unfold);
		}
		long c = System.nanoTime();
		System.out.println("Completed:");
		System.out.println("Started at " + a + " time stamp");
		System.out.println("Generated the tree in " + (b - a) + "ns\n\t(which is: " + TimeUnit.MILLISECONDS.convert((b - a), TimeUnit.NANOSECONDS) +"ms)");
		System.out.println("Unfolded " + Minimax.stack.size() + " nodes in " + (c - b) + "ns\n\t(which is: " + TimeUnit.MILLISECONDS.convert((c - b), TimeUnit.NANOSECONDS) +"ms)");
		System.out.println("\twhich means " + (c - b)/Minimax.stack.size() + "ns per node\n\t\t(which is: " + TimeUnit.MILLISECONDS.convert((c - b)/Minimax.stack.size(), TimeUnit.NANOSECONDS) +"ms)");
		System.out.println("Total invocation time: " + (c - a) + "\n\t(which is " + TimeUnit.MILLISECONDS.convert((c - a), TimeUnit.NANOSECONDS) +"ms)");
		System.out.println("\t\t(which is " + TimeUnit.SECONDS.convert((c - a), TimeUnit.NANOSECONDS) +"s)");
		System.out.println("Explored " + Minimax.nodeExplored + " nodes.");
	    
        boolean end = true;
        State statoCorrente = new State(initialState , false); // Turno white iniziale
        while(!end) {
        	System.out.println("Turno " + (statoCorrente.turn ? "Black" : "White"));
        	System.out.println(statoCorrente.printBoard());
        	
        	//System.out.println(Minimax.alphaBeth(statoCorrente, 4, Double.MIN_VALUE, Double.MAX_VALUE, true));
        	
        	statoCorrente = Minimax.minimaxDecision(statoCorrente);
            if (statoCorrente.isTerminal()){
                end = true;
                System.out.println(statoCorrente.printBoard());
                System.out.println("Ha vinto: " + (statoCorrente.turn ? "Black" : "White"));
                System.out.println("Game over");
            }
            Thread.sleep(1000 * 5);
        }
    }
	
}
