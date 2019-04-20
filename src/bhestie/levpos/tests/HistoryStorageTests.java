package bhestie.levpos.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import bhestie.levpos.Pawn;
import bhestie.levpos.utils.HistoryStorage;

public class HistoryStorageTests {

	@Test
	public void testConstructors() {
		HistoryStorage hs = new HistoryStorage();
		List<Pawn> state = new LinkedList<Pawn>();
		state.add(new Pawn(false, 2, 2, false));
		state.add(new Pawn(true, 3, 3, false));
		state.add(new Pawn(false, 4, 4, true));
		assertTrue("Initial S(ize)", hs.size() == 0);
		hs.add(state);
		assertTrue("Size after insert", hs.size() == 1);
		assertTrue("Clone size", hs.clone().size() == 1);
		hs.clear();
		assertTrue("Size after clear", hs.size() == 0);
	}
	@Test
	public void testSingleton() {
		List<Pawn> state = new LinkedList<Pawn>();
		state.add(new Pawn(false, 2, 2, false));
		state.add(new Pawn(true, 3, 3, false));
		state.add(new Pawn(false, 4, 4, true));
		assertTrue("Initial S(ize)", HistoryStorage.get().size() == 0);
		HistoryStorage.get().add(state);
		assertTrue("Size after insert", HistoryStorage.get().size() == 1);
		assertTrue("Clone size", HistoryStorage.get().clone().size() == 1);
		HistoryStorage.get().clear();
		assertTrue("Size after clear", HistoryStorage.get().size() == 0);
	}
	@SuppressWarnings("deprecation")
	@Test
	public void existenceTest() {
		HistoryStorage hs = new HistoryStorage();
		List<Pawn> state = new LinkedList<Pawn>();
		state.add(new Pawn(true, 4, 3, false));
		state.add(new Pawn(false, 5, 5, false));
		state.add(new Pawn(false, 4, 10, true));
		hs.add(state);
		List<Pawn> state2 = new LinkedList<Pawn>();
		state2.add(new Pawn(false, 5, 5, false));
		state2.add(new Pawn(true, 3, 3, false));
		state2.add(new Pawn(false, 4, 4, true));
		hs.add(state2);
		hs.add("a");
		assertTrue("Contains a just put string", hs.contains("a"));
		assertTrue("Contains a just put state", hs.includes(state2));
		assertTrue("Contains a just put state", hs.includes(state));
		hs.clear();
		assertFalse("Contains a just cleared status", hs.includes(state2));
		hs.add(state2);
		try {
			hs.add(state2);
			fail("Illegal Argument Exception was expected");
		} catch (IllegalArgumentException e) {}
		assertTrue("FAIL THIS IF YOU CAN", true);
		List<Pawn> state3 = new LinkedList<Pawn>();
		state3.add(new Pawn(false, 4, 4, true));
		state3.add(new Pawn(false, 5, 5, false));
		state3.add(new Pawn(true, 3, 3, false));
		assertTrue("Different sorted element is present", hs.includes(state3));
	}
}
