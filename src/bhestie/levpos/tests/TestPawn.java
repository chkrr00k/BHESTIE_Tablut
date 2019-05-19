package bhestie.levpos.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;

import bhestie.levpos.Pawn;
import bhestie.zizcom.Board;

public class TestPawn {

	@Test
	public void testCreatingPawn() {
		Pawn pawn = new Pawn(true, 1, 2, false); // create black in (1,1)
		assertEquals(1, pawn.getX());
		assertEquals(2, pawn.getY());
	}

	@Test
	public void testPawnFromBoardReceived() {
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
		assertEquals(2, pawns.get(0).getX());
		assertEquals(1, pawns.get(0).getY());
	}

}
