package bhestie.levpos;

import bhestie.zizcom.Connector;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainThatPlayWithServerAsWhite {
	
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
			Connector connector = new Connector("bhestieBianche", 5800);
			connector.init();
			connector.present();

			boolean end = false;
			State statoCorrente = new State(connector.readBoard().convert().get(), false);
			//State statoCorrente = new State(initialState , false); // Turno white iniziale

			while(!end){

				Minimax.alphaBeth(statoCorrente, 3, -Double.MAX_VALUE, Double.MAX_VALUE, true);
				List<State> miniMaxStack = Minimax.stack;
				State statoDestinazione = Minimax.stack.get(0).getInitialState();

				/*
				for(State s : miniMaxStack){
					if(!s.getInitialState().equals(nextState)) {
						nextState = s.getInitialState();
						System.out.println(nextState);
					}

					//List<State> unfold = s.unfold();
					//System.out.println(unfold);
				}
				*/

				if (statoDestinazione.isTerminal()){
					end = true;
					System.out.println(statoDestinazione.printBoard());
					System.out.println("Ha vinto: " + (statoDestinazione.isTurn() ? "Black" : "White"));
					System.out.println("Game over");
				}
				connector.writeAction(statoCorrente, statoDestinazione);

				Thread.sleep(1000 * 5);
				String serviceMessage = connector.readCode();

				statoCorrente = new State(connector.readBoard().get(), false);
			}
		}catch(IOException e){
	    	System.out.println("eccezione non gestita nel bianco");
		}
    }
	
}
