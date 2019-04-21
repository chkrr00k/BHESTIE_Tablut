package bhestie.zizcom.tests;

import java.io.IOException;
import java.util.List;

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
		
		boolean whitePlayer = false;
	    boolean blackPlayer = !whitePlayer;
	    
	    Minimax.player = whitePlayer;
	    
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
		b.convert();
		
		if (Minimax.player == blackPlayer) {
			b = c.readBoard();
			b.convert();
		}
		
		State currentState = new State(b.get(), Minimax.player);
		while(true) {
			double result = Minimax.alphaBethInit(currentState, 3);
			System.out.println(result + " Prevedo di " + (result == 0 ? "pareggiare" : (result > 0 ? "vincere" : "perdere")));
			State oldState = currentState;
			
			currentState = Minimax.stack.get(0);
			List<State> unfold = currentState.unfold();
			if (unfold.size() > 0)
				currentState = unfold.get(unfold.size() - 1);
			
			int fx = 0;
			int fy = 0;
			int tx = 0;
			int ty = 0;
			for (Pawn pawn : currentState.getPawns()) {
				if (!oldState.getPawns().contains(pawn) && pawn.filterByTurn(Minimax.player)) {
					tx = pawn.getX();
					ty = pawn.getY();
				}
			}
			for (Pawn pawn : oldState.getPawns()) {
				if (!currentState.getPawns().contains(pawn) && pawn.filterByTurn(Minimax.player)) {
					fx = pawn.getX();
					fy = pawn.getY();
				}
			}
			Action aa = new Action(fx, fy, tx, ty, Minimax.player == whitePlayer ? "W" : "B");
			c.writeAction(aa);
			
			Minimax.stack.clear();
			
			b = c.readBoard();
			b.convert();
			b = c.readBoard();
			b.convert();
			
			currentState = new State(b.get(), Minimax.player, currentState.historyStorage, null);
		}
		/*
		c.writeAction(a);

		b.convert();
		System.out.println(b);
		b = c.readBoard();
		b.convert();
		System.out.println(b);*/
	}

}
