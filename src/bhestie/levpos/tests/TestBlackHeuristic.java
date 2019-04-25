package bhestie.levpos.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Test;
import bhestie.levpos.*;

public class TestBlackHeuristic {
	@Test
	public void testThrone() {
		ArrayList<Pawn> pawns = new ArrayList<>();
		Pawn king = Pawn.of(false, 5, 5, true);
		
		pawns.add(king);
		pawns.add(Pawn.of(true, 3, 3, false));
		
		State state = new State(pawns, true);
		BlackHeuristic bh = new BlackHeuristic(state);
		assertTrue(bh.isKingEscapeBlocked(king));
	}
	
	@Test
	public void testCriticalKingPosition() {
		ArrayList<Pawn> pawns = new ArrayList<>();
		Pawn king = Pawn.of(false, 7, 3, true);
		pawns.add(Pawn.of(true, 3, 3, false));
		
		pawns.add(king);
		pawns.add(Pawn.of(true, 3, 3, false));
		
		State state = new State(pawns, true);
		BlackHeuristic bh = new BlackHeuristic(state);
		
		assertFalse(bh.isKingEscapeBlocked(king));
		
		pawns.add(Pawn.of(true, 7, 1, false));
		pawns.add(Pawn.of(true, 8, 3, false));
		pawns.add(Pawn.of(false, 7, 5, false)); //white
	}
}
