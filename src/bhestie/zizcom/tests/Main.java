package bhestie.zizcom.tests;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import bhestie.levpos.HeuristicCalculatorGroup;
import bhestie.levpos.Minimax;
import bhestie.levpos.State;
import bhestie.levpos.utils.HistoryStorage;
import bhestie.zizcom.Board;
import bhestie.zizcom.Connector;

public class Main {

	private static final boolean whitePlayer = false;
	private static final boolean blackPlayer = !whitePlayer;
	
	private static final int WhitePort = 5800;
	private static final int BlackPort = 5801;
	
	private static int port;
	
	public static void main(String[] args) throws IOException {
		HeuristicCalculatorGroup.getInstance().addThreads(3);
		
	    Minimax.player = whitePlayer;
	    
	    if (Minimax.player == whitePlayer)
	    	port = WhitePort;
	    else
	    	port = BlackPort;
		
		Connector c = new Connector("__BHeStIE__", port);
		Board b = null;
		c.init();
		c.present();
		b = c.readBoard(); // Read the initial board
		
		if (Minimax.player == blackPlayer) {
			b = c.readBoard(); // Wait for enemy move
		}
		
		State currentState = new State(b.convert().get(), Minimax.player);
		try{
			while(true) {
				LocalTime before = LocalTime.now();
				long result = Minimax.alphaBethInit(currentState);
				System.out.println("Explored = " + Minimax.nodeExplored + " in " + ChronoUnit.MILLIS.between(before, LocalTime.now()));
				System.out.println(result + " Prevedo di " + (result == 0 ? "pareggiare" : (result > 0 ? "vincere" : "perdere")));

				
				currentState = Minimax.stack.get((int) Math.random() * Minimax.stack.size());
				List<State> unfold = currentState.unfold();
				int unfoldSize = unfold.size();
				if (unfoldSize > 0)
					currentState = unfold.get(unfoldSize - 1);
				c.writeAction(currentState.getAction()); // Sends our move
				
				long memoryBefore = Runtime.getRuntime().totalMemory() / 1024 / 1024;
				long freeBefore = Runtime.getRuntime().freeMemory() / 1024 / 1024;
				System.out.println("Before\n\tMemory allocated = " + memoryBefore + "\tFree = " + freeBefore);
				HistoryStorage historyStorage = currentState.historyStorage; // Keep it
				currentState = null;
				unfold = null;
				Minimax.stack.clear();
				System.gc();
				long memoryAfter = Runtime.getRuntime().totalMemory() / 1024 / 1024;
				long freeAfter = Runtime.getRuntime().freeMemory() / 1024 / 1024;
				System.out.println("After\n\tMemory allocated = " + memoryAfter + "\tFree = " + freeAfter);
				b = c.readBoard(); // Gets the board after our move
				Thread.yield(); // Let the enemy "think correctly"
				b = c.readBoard(); // Gets the board after the enemy move
				
				State.TURN++;
				currentState = new State(b.convert().get(), Minimax.player, historyStorage, null);
				
				System.out.println(State.TURN);
			}
		}catch(Exception e){
			HeuristicCalculatorGroup.getInstance().killAll();
			System.exit(0);
		}
	}

}
