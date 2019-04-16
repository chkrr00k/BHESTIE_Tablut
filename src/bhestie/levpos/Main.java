package bhestie.levpos;

import java.util.LinkedList;
import java.util.List;

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
		
        boolean end = false;
        State statoCorrente = new State(initialState , false); // Turno white iniziale
        while(!end) {
        	System.out.println("Turno " + (statoCorrente.turn ? "Black" : "White"));
        	System.out.println(statoCorrente.stampaScacchiera());
        	
        	statoCorrente = Minimax.minimaxDecision(statoCorrente);
            if (statoCorrente.isTerminal()){
                end = true;
                System.out.println(statoCorrente.stampaScacchiera());
                System.out.println("Ha vinto: " + (statoCorrente.turn ? "Black" : "White"));
                System.out.println("Game over");
            }
            Thread.sleep(1000 * 5);
        }
    }
	
}
