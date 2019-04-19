package bhestie.levpos.tests;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import bhestie.levpos.Pawn;
import bhestie.levpos.State;

public class TestState {

	@Test
	public void testPrintBoard() {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(true, 1, 2, false));
		
		State s = new State(pawns, false);
		assertTrue(s.toString().startsWith("000000000\nB"));
	}
	
	@Test
	public void testDoubleEatNotNearCitadel() {
		List<Pawn> initialPawnState = new LinkedList<>();
		initialPawnState.add(new Pawn(false, 1, 1, true)); // King, to be sure is in the board
		
		initialPawnState.add(new Pawn(true, 1, 4, false)); // Pawn to move
		
		initialPawnState.add(new Pawn(false, 7, 3, false));
		initialPawnState.add(new Pawn(true, 7, 2, false));
		initialPawnState.add(new Pawn(false, 7, 5, false));
		initialPawnState.add(new Pawn(true, 7, 6, false));
		
		State currentState = new State(initialPawnState, true); // Black turn
		
		Collection<State> afterState = currentState.getActions();
		
		boolean found = false;
		for (State state : afterState) {
			found = ((state.pawns.size() == initialPawnState.size() - 2));
			if (found)
				break;
		}
		assertTrue(found);
	}
	
	@Test
	public void testTripleEatNearCitadel() {
		List<Pawn> initialPawnState = new LinkedList<>();
		initialPawnState.add(new Pawn(false, 1, 1, true)); // King, to be sure is in the board
		
		initialPawnState.add(new Pawn(true, 1, 4, false)); // Pawn to move that eats pawns in (7,3) and (7,5)
		
		initialPawnState.add(new Pawn(false, 8, 4, false));
		initialPawnState.add(new Pawn(false, 7, 3, false));
		initialPawnState.add(new Pawn(true, 7, 2, false));
		initialPawnState.add(new Pawn(false, 7, 5, false));
		initialPawnState.add(new Pawn(true, 7, 6, false));
		
		State currentState = new State(initialPawnState, true); // Black turn
		
		Collection<State> afterState = currentState.getActions();
		
		boolean found = false;
		for (State state : afterState) {
			found = ((state.pawns.size() == initialPawnState.size() - 3));
			if (found)
				break;
		}
		assertTrue(found);
	}
	
	@Test
	public void testTripleEatNearCitadelOtherOrientation() {
		List<Pawn> initialPawnState = new LinkedList<>();
		initialPawnState.add(new Pawn(false, 1, 1, true)); // King, to be sure is in the board
		
		initialPawnState.add(new Pawn(true, 4, 1, false)); // Pawn to move that eats
		
		initialPawnState.add(new Pawn(false, 4, 8, false));
		initialPawnState.add(new Pawn(false, 3, 7, false));
		initialPawnState.add(new Pawn(true, 2, 7, false));
		initialPawnState.add(new Pawn(false, 5, 7, false));
		initialPawnState.add(new Pawn(true, 6, 7, false));
		
		State currentState = new State(initialPawnState, true); // Black turn
		
		Collection<State> afterState = currentState.getActions();
		
		boolean found = false;
		for (State state : afterState) {
			found = ((state.pawns.size() == initialPawnState.size() - 3));
			if (found)
				break;
		}
		assertTrue(found);
	}
	
	@Test
	public void testNotPassiveEat() {
		List<Pawn> initialPawnState = new LinkedList<>();
		initialPawnState.add(new Pawn(false, 1, 1, true)); // King, to be sure is in the board
		
		initialPawnState.add(new Pawn(true, 1, 4, false)); // Pawn to move
		
		initialPawnState.add(new Pawn(false, 7, 3, false));
		initialPawnState.add(new Pawn(false, 7, 5, false));
		
		State currentState = new State(initialPawnState, true); // Black turn
		
		Collection<State> afterState = currentState.getActions();
		
		boolean found = false;
		for (State state : afterState) {
			found = ((state.pawns.size() == initialPawnState.size() - 1));
			if (found)
				break;
		}
		assertFalse(found);
	}
	
	@Test
	public void testEatKingNearTrone() {
		List<Pawn> initialPawnState = new LinkedList<>();
		initialPawnState.add(new Pawn(false, 4, 5, true)); // King, to be sure is in the board
		
		initialPawnState.add(new Pawn(true, 3, 9, false)); // Pawn to move that eats 
		
		initialPawnState.add(new Pawn(true, 4, 6, false));
		initialPawnState.add(new Pawn(true, 4, 4, false));
		
		State currentState = new State(initialPawnState, true); // Black turn
		
		Collection<State> afterState = currentState.getActions();
		
		boolean found = false;
		for (State state : afterState) {
			found = ((state.pawns.size() == initialPawnState.size() - 1));
			if (found)
				break;
		}
		assertTrue(found);
	}
	
	@Test
	public void testNotEatWithInternatCitadelAsPartner() {
		List<Pawn> initialPawnState = new LinkedList<>();
		
		initialPawnState.add(new Pawn(false, 2, 7, false)); // Pawn to move that eats pawns in (7,3) and (7,5)
		
		initialPawnState.add(new Pawn(false, 1, 1, true)); // King, to be sure is in the board
		
		initialPawnState.add(new Pawn(true, 9, 6, false)); // Pawn to move that eats pawns in (7,3) and (7,5)
		
		State currentState = new State(initialPawnState, false); // White turn
		
		Collection<State> afterState = currentState.getActions();

		assertTrue(afterState.stream().allMatch(s -> s.pawns.size() == initialPawnState.size()));
	}
	
	@Test
	public void testSymmetricsGeneratingChildStates() {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, false));
		pawns.add(new Pawn(false, 7, 3, false));
		pawns.add(new Pawn(false, 3, 7, false));
		pawns.add(new Pawn(false, 7, 7, false));
		State s = new State(pawns, false); // White turn
		assertEquals(0, s.getActions().size() % 5); // Dubbio se essere 5 o 10
	}
	
	@Test
	public void test2SymmetricsGeneratingChildStates() {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, false));
		pawns.add(new Pawn(false, 7, 3, false));
		pawns.add(new Pawn(false, 3, 7, false));
		pawns.add(new Pawn(false, 7, 7, false));
		
		pawns.add(new Pawn(false, 2, 3, false));
		pawns.add(new Pawn(false, 2, 7, false));
		pawns.add(new Pawn(false, 8, 3, false));
		pawns.add(new Pawn(false, 8, 7, false));
		
		State s = new State(pawns, false); // White turn
		assertEquals(12, s.getActions().size());
	}
	@Test
	public void testDiagonalSystem() throws Exception {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 4, 4, false));
		pawns.add(new Pawn(false, 7, 1, false));
		pawns.add(new Pawn(false, 9, 3, false));
		pawns.add(new Pawn(false, 6, 6, false));
		

		boolean xsy = pawns.stream().collect(Collectors.groupingByConcurrent(Pawn::getX)).values().stream().parallel().allMatch((e) -> {
			if(e.size() != 2){
				return false;
			}else{
				return e.get(0).getY() + e.get(1).getY() == 10;
			}
		});
		boolean symmetricalDiagonal = true;
		for (Pawn pawn : pawns) {
			if(symmetricalDiagonal && !pawns.stream().anyMatch(p -> p.position.x + pawn.position.y == 10 
						&& p.position.y + pawn.position.x  == 10
						&& p.bw==pawn.bw 
						&& p.king==pawn.king))
					 {
				symmetricalDiagonal = false;
			}
		}
		assertTrue(symmetricalDiagonal);
		
		for (Pawn pawn : pawns.stream()/*.filter(p -> p.position.y < 5)*/.collect(Collectors.toList())) {
			if (!pawns.stream().anyMatch(p -> p.position.x==pawn.position.x && p.position.y+pawn.position.y==10 && p.bw==pawn.bw && p.king==pawn.king)) {
				break;
			}
		}
		

		boolean ysy = pawns.stream().collect(Collectors.groupingByConcurrent(Pawn::getY)).values().stream().allMatch((e) -> {
			if(e.size() != 2){
				return false;
			}else{
				return e.get(0).getX() + e.get(1).getX() == 10;
			}
		});
		assertFalse(ysy);
		assertFalse(xsy);
	}

	@Test
	public void testSymmetries() throws Exception {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, false));
		State s = new State(pawns, false); // White turn
		assertEquals(16, s.getActions().size()); // one pawn generates 16 successors
		
		pawns.add(new Pawn(false, 7, 3, false));
		s = new State(pawns, false); // White turn
		assertEquals(13, s.getActions().size()); // two pawn generates 13 successors in this configuration because x symmetry
	
		pawns.clear();
		pawns.add(new Pawn(false, 3, 3, false));
		pawns.add(new Pawn(false, 3, 7, false));
		s = new State(pawns, false); // White turn
		assertEquals(13, s.getActions().size()); // two pawn generates 13 successors in this configuration because y symmetry
		
		pawns.clear();
		pawns.add(new Pawn(false, 3, 3, false));
		pawns.add(new Pawn(false, 7, 7, false));
		s = new State(pawns, false); // White turn
		assertEquals(16, s.getActions().size()); // two pawn generates 16 successors in this configuration because diagonal symmetry
		
		pawns.clear();
		pawns.add(new Pawn(false, 7, 3, false));
		pawns.add(new Pawn(false, 3, 7, false));
		s = new State(pawns, false); // White turn
		assertEquals(16, s.getActions().size()); // two pawn generates 16 successors in this configuration because antidiagonal symmetry
		
		pawns.clear();
		pawns.add(new Pawn(false, 7, 3, false));
		pawns.add(new Pawn(false, 3, 7, false));
		pawns.add(new Pawn(false, 3, 3, false));
		pawns.add(new Pawn(false, 7, 7, false));
		s = new State(pawns, false); // White turn
		assertEquals(5, s.getActions().size()); // two pawn generates 5 successors in this configuration because all the symmetries

	}
}
