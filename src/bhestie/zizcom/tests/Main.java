package bhestie.zizcom.tests;

import java.io.IOException;
import bhestie.levpos.Pawn;
import bhestie.levpos.State;
import bhestie.zizcom.Action;
import bhestie.zizcom.Board;
import bhestie.zizcom.Connector;

public class Main {

	public static void main(String[] args) throws IOException {
		Connector c = new Connector("__BHeStIE__", 5800);
		Action a = new Action("e4", "f4", "W");
		Board b = null;
		c.init();
		c.present();
		b = c.readBoard();
		b.convert();
		
		State currentState = new State(b.get(), false);
		
		for(int i = 0; i < 100; i++) {
			State oldState = currentState;
			currentState = currentState.getActions().stream().findFirst().get();
			int fx = 0;
			int fy = 0;
			int tx = 0;
			int ty = 0;
			for (Pawn pawn : currentState.getPawns()) {
				if (!oldState.getPawns().contains(pawn) && pawn.isBlack() == false) {
					tx = pawn.getX();
					ty = pawn.getY();
				}
			}
			for (Pawn pawn : oldState.getPawns()) {
				if (!currentState.getPawns().contains(pawn) && pawn.isBlack() == false) {
					fx = pawn.getX();
					fy = pawn.getY();
				}
			}
			Action aa = new Action(fx, fy, tx, ty, "W");
			c.writeAction(aa);
			
			b = c.readBoard();
			b.convert();
			b = c.readBoard();
			b.convert();
			currentState = new State(b.get(), false);
		}
		
		c.writeAction(a);

		b.convert();
		System.out.println(b);
		b = c.readBoard();
		b.convert();
		System.out.println(b);
	}

}
