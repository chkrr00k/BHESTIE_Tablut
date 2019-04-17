package bhestie.levpos.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import bhestie.levpos.Pawn;
import bhestie.levpos.State;

class TestState {

	@Test
	void testPrintBoard() {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(true, 1, 2, false));
		
		State s = new State(pawns, false);
		assertTrue(s.toString().startsWith("000000000\nB"));
		
	}

}
