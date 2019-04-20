package bhestie.levpos;

import bhestie.zizcom.Connector;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MainThatPlayWithServerAsBlack {
	
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
	    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

	    try {
			Connector connector = new Connector("bhestieNere", 5801);
			connector.init();
			connector.present();

			boolean end = false;

			State statoCorrente = new State(connector.readBoard().get(), true);

			while(!end){
				statoCorrente = Minimax.minimaxDecision(statoCorrente);
				if (statoCorrente.isTerminal()){
					end = true;
					System.out.println(statoCorrente.printBoard());
					System.out.println("Ha vinto: " + (statoCorrente.isTurn() ? "Black" : "White"));
					System.out.println("Game over");
				}else{
					//TODO connector.sendMovement
				}
				Thread.sleep(1000 * 5);
				statoCorrente = new State(connector.readBoard().get(), true);
			}
		}catch(IOException e){
	    	System.out.println("eccezione non gestita nel nero");
		}
    }
	
}
