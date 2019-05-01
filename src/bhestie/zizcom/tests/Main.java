package bhestie.zizcom.tests;

import java.io.IOException;
import java.util.List;

import bhestie.levpos.HeuristicCalculatorGroup;
import bhestie.levpos.Minimax;
import bhestie.levpos.Pawn;
import bhestie.levpos.State;
import bhestie.zizcom.Action;
import bhestie.zizcom.Board;
import bhestie.zizcom.Connector;

public class Main {

	private static final int WhitePort = 5800;
	private static final int BlackPort = 5801;
	
	private static int port;
	
	public static void main(String[] args) throws IOException {
		HeuristicCalculatorGroup.getInstance().addThreads(3);
		
		boolean whitePlayer = false;
	    boolean blackPlayer = !whitePlayer;
	    
	    Minimax.player = blackPlayer;
	    
	    if (Minimax.player == whitePlayer)
	    	port = WhitePort;
	    else
	    	port = BlackPort;
		
		Connector c = new Connector("__BHeStIE__", port);
		//Action a = new Action("e4", "f4", "W");
		Board b = null;
		c.init();
		c.present();
		b = c.readBoard();
		
		if (Minimax.player == blackPlayer) {
			b = c.readBoard();
		}
		
		State currentState = new State(b.convert().get(), Minimax.player);
		while(true) {
			HeuristicCalculatorGroup.getInstance().playAll();
			double result = Minimax.alphaBethInit(currentState, 3);
			HeuristicCalculatorGroup.getInstance().pauseAll();
			System.out.println(result + " Prevedo di " + (result == 0 ? "pareggiare" : (result > 0 ? "vincere" : "perdere")));

			
			currentState = Minimax.stack.get((int) Math.random() * Minimax.stack.size());
			List<State> unfold = currentState.unfold();
			if (unfold.size() > 0)
				currentState = unfold.get(unfold.size() - 1);
			c.writeAction(currentState.getAction());
			Minimax.stack.clear();
			
			b = c.readBoard();
			b = c.readBoard();
			
			currentState = new State(b.convert().get(), Minimax.player, currentState.historyStorage, null);
		}
	}

}
