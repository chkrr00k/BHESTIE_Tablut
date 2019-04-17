package bhestie.levpos.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import bhestie.levpos.Citadel;
import bhestie.levpos.Pawn;
import bhestie.levpos.Position;

class TestCitadel {

	@Test
	void testCitadel() {
		ArrayList<Position> positions = new ArrayList<>(4);
		positions.add(new Position(5, 1));
		positions.add(new Position(4, 1));
		positions.add(new Position(6, 1));
		positions.add(new Position(5, 2));
		Citadel c = new Citadel(positions);
		
		assertTrue(c.isXYInCitadel(4, 1));
		assertTrue(c.isPositionInCitadel(new Position(5, 1)));
		assertTrue(c.isPawnInCitadel(new Pawn(true, 5, 2, false)));
		assertTrue(c.isXYInFringeCitadels(6, 1));
		assertFalse(c.isXYInFringeCitadels(5, 1));
	}

}
