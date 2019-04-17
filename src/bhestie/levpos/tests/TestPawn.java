package bhestie.levpos.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import bhestie.levpos.Pawn;
import bhestie.zizcom.Board;

class TestPawn {

	@Test
	void testCreatingPawn() {
		Pawn pawn = new Pawn(true, 1, 2, false); // create black in (1,1)
		assertEquals(1, pawn.position.x);
		assertEquals(2, pawn.position.y);
	}

	@Test
	void testPawnFromBoardReceived() {
		Board b = new Board();
		String[][] exampleBoard = {
				{"EMPTY","BLACK","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY"},
				{"EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY"},
				{"EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY"},
				{"EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY"},
				{"EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY"},
				{"EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY"},
				{"EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY"},
				{"EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY"},
				{"EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY","EMPTY"},
				};
		b.setBoard(exampleBoard);
		b.convert();
		List<Pawn> pawns = b.get();
		assertEquals(2, pawns.get(0).position.x);
		assertEquals(1, pawns.get(0).position.y);
	}

}
