package bhestie.levpos.tests;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import bhestie.levpos.Minimax;
import bhestie.levpos.Pawn;
import bhestie.levpos.State;
import bhestie.levpos.utils.HistoryStorage;
import bhestie.zizcom.Action;

public class TestState {

	private static final boolean whitePlayer = false;
    private static final boolean blackPlayer = !whitePlayer;
    
    @Test
    public void testDrawBlackTurn() {
    	Minimax.player = blackPlayer;
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(true, 2, 2, false));
		State s = new State(pawns, true, new HistoryStorage(), null, true);
		assertTrue(s.isTerminal());
		double utility = s.getUtility();
		assertEquals(0, utility, 0.01);
    }
    
    @Test
	public void testLoop() throws Exception {
    	Minimax.player = blackPlayer;
 //   	HeuristicCalculatorGroup.getInstance().addThreads(3);
 //   	Minimax.init();
    	
    	List<Pawn> p = new LinkedList<Pawn>();
    	p.add(new Pawn(false, 5, 5, true));
    	
    	p.add(new Pawn(false, 6, 6, false));
    	p.add(new Pawn(false, 5, 4, false));
    	p.add(new Pawn(false, 9, 7, false));
    	p.add(new Pawn(false, 5, 3, false));
    	p.add(new Pawn(false, 3, 5, false));
    	p.add(new Pawn(false, 4, 5, false));
    	p.add(new Pawn(false, 7, 1, false));
    	p.add(new Pawn(false, 6, 5, false));
    	
    	p.add(new Pawn(true, 1, 4, false));
    	p.add(new Pawn(true, 1, 5, false));
    	p.add(new Pawn(true, 1, 6, false));
    	p.add(new Pawn(true, 2, 7, false));
    	
    	p.add(new Pawn(true, 9, 4, false));
    	p.add(new Pawn(true, 9, 5, false));
    	p.add(new Pawn(true, 9, 6, false));
    	p.add(new Pawn(true, 8, 3, false));
    	
    	p.add(new Pawn(true, 4, 1, false));
    	p.add(new Pawn(true, 5, 1, false));
    	p.add(new Pawn(true, 6, 1, false));
    	p.add(new Pawn(true, 3, 2, false));
    	
    	p.add(new Pawn(true, 4, 9, false));
    	p.add(new Pawn(true, 5, 9, false));
    	p.add(new Pawn(true, 6, 9, false));
    	p.add(new Pawn(true, 5, 8, false));
    	
    	State s = new State(p, !blackPlayer);
//    	System.out.println(s);
    	
    	assertFalse(s.veryUglyKingPosition());
    	assertFalse(s.isTerminal());
    	assertEquals(4, s.threatenKingRemaining().get(0).size());
    	
/*    	Minimax.DEPTH = 4;
    	Minimax.FIXEDDEPTH = true;*/
//    	System.out.println(Minimax.alphaBethInit(s));
    	
    	p.clear();
    	p.add(new Pawn(false, 5, 5, true));
    	
    	p.add(new Pawn(false, 6, 6, false));
    	p.add(new Pawn(false, 5, 4, false));
    	p.add(new Pawn(false, 9, 7, false));
    	p.add(new Pawn(false, 5, 3, false));
    	p.add(new Pawn(false, 3, 5, false));
    	p.add(new Pawn(false, 4, 5, false));
    	p.add(new Pawn(false, 7, 1, false));
    	p.add(new Pawn(false, 6, 4, false));
    	
    	p.add(new Pawn(true, 1, 4, false));
    	p.add(new Pawn(true, 1, 5, false));
    	p.add(new Pawn(true, 1, 6, false));
    	p.add(new Pawn(true, 2, 7, false));
    	
    	p.add(new Pawn(true, 9, 4, false));
    	p.add(new Pawn(true, 9, 5, false));
    	p.add(new Pawn(true, 9, 6, false));
    	p.add(new Pawn(true, 8, 3, false));
    	
    	p.add(new Pawn(true, 4, 1, false));
    	p.add(new Pawn(true, 5, 1, false));
    	p.add(new Pawn(true, 6, 1, false));
    	p.add(new Pawn(true, 3, 2, false));
    	
    	p.add(new Pawn(true, 4, 9, false));
    	p.add(new Pawn(true, 5, 9, false));
    	p.add(new Pawn(true, 6, 9, false));
    	p.add(new Pawn(true, 5, 8, false));
    	
    	s = new State(p, !blackPlayer);
//    	System.out.println(s);
    	
    	assertFalse(s.veryUglyKingPosition());
    	assertFalse(s.isTerminal());
    	assertEquals(4, s.threatenKingRemaining().get(0).size());
    	
/*    	Minimax.DEPTH = 4;
    	Minimax.FIXEDDEPTH = true;
    	Minimax.TIMEOUT = 30;
    	Minimax.init();*/
//    	System.out.println(Minimax.alphaBethInit(s));
	}
    
    @Test
	public void testThreatenKing() throws Exception {
    	Minimax.player = whitePlayer;
    	List<Pawn> p = new LinkedList<Pawn>();
    	p.add(new Pawn(false, 5, 3, true));
    	State s = new State(p, !whitePlayer);
    	assertEquals(1, s.threatenKingRemaining().get(0).size());

    	p.add(new Pawn(true, 4, 3, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(1, s.threatenKingRemaining().get(0).size());
    	
    	p.add(new Pawn(true, 5, 4, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(1, s.threatenKingRemaining().get(0).size());
    	
    	p.clear();
    	p.add(new Pawn(false, 2, 2, true));
    	s = new State(p, !whitePlayer);
    	assertEquals(4, s.threatenKingRemaining().stream().mapToInt(l -> l.size()).sum());
    	
    	p.clear();
    	p.add(new Pawn(false, 5, 4, true));
    	s = new State(p, !whitePlayer);
    	assertEquals(3, s.threatenKingRemaining().get(0).size());
    	
    	p.add(new Pawn(true, 5, 6, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(3, s.threatenKingRemaining().get(0).size());
    	
    	p.add(new Pawn(true, 5, 3, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(2, s.threatenKingRemaining().get(0).size());
    	
    	p.add(new Pawn(true, 4, 4, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(1, s.threatenKingRemaining().get(0).size());
    	
     	p.add(new Pawn(true, 6, 4, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(0, s.threatenKingRemaining().size());
    	
    	p.clear();
    	p.add(new Pawn(false, 5, 5, true));
    	s = new State(p, !whitePlayer);
    	assertEquals(4, s.threatenKingRemaining().get(0).size());
    	
    	p.add(new Pawn(true, 5, 6, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(3, s.threatenKingRemaining().get(0).size());
    	
    	p.add(new Pawn(true, 5, 4, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(2, s.threatenKingRemaining().get(0).size());
    	
    	p.add(new Pawn(true, 4, 5, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(1, s.threatenKingRemaining().get(0).size());
    	
     	p.add(new Pawn(true, 6, 5, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(0, s.threatenKingRemaining().size());
    	
    	p.clear();
    	p.add(new Pawn(false, 2, 4, true));
    	s = new State(p, !whitePlayer);
    	assertTrue(s.threatenKingRemaining().stream().allMatch(l -> l.size() == 1));
    	
    	//XXX NOT A BUG, special case not useful
    	p.clear();
    	p.add(new Pawn(false, 1, 1, true));
    	s = new State(p, !whitePlayer);
    	assertTrue(s.threatenKingRemaining().stream().allMatch(l -> l.size() == 2));
	}
    
    @Test
    public void testKingEatThing() throws Exception {
    	Minimax.player = whitePlayer;
    	List<Pawn> p = new LinkedList<Pawn>();
    	p.add(new Pawn(false, 5, 3, true));
    	State s = new State(p, !whitePlayer);
    	assertEquals(3, s.remainingPositionForSurroundingKing());
    	assertFalse(s.veryUglyKingPosition());
    	p.add(new Pawn(true, 6, 3, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(2, s.remainingPositionForSurroundingKing());
    	assertFalse(s.veryUglyKingPosition());
    	p.clear();
    	p.add(new Pawn(false, 5, 4, true));
    	p.add(new Pawn(true, 6, 4, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(2, s.remainingPositionForSurroundingKing());
    	assertFalse(s.veryUglyKingPosition());
    	p.add(new Pawn(true, 4, 4, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(1, s.remainingPositionForSurroundingKing());
    	assertFalse(s.veryUglyKingPosition());
    	p.add(new Pawn(true, 1, 3, false));
    	s = new State(p, !whitePlayer);
    	assertEquals(1, s.remainingPositionForSurroundingKing());
    	assertTrue(s.veryUglyKingPosition());
	}
    
    @Test
    public void testDrawWhiteTurn() {
    	Minimax.player = whitePlayer;
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(true, 2, 2, false));
		State s = new State(pawns, true, new HistoryStorage(), null, true);
		assertTrue(s.isTerminal());
		double utility = s.getUtility();
		assertEquals(0, utility, 0.01);
    }
    
	@Test
	public void testGetUtilityBlackWinsWhiteTurn() {
		Minimax.player = whitePlayer;
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(true, 2, 2, false));
		State s = new State(pawns, false);
		assertTrue(s.isTerminal());
		double utility = s.getUtility();
		assertTrue(utility < 0);
	}
	
	@Test
	public void testGetUtilityBlackWinsBlackTurn() {
		Minimax.player = blackPlayer;
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(true, 2, 2, false));
		State s = new State(pawns, true);
		assertTrue(s.isTerminal());
		double utility = s.getUtility();
		assertTrue(utility > 0);
	}
	
	@Test
	public void testGetUtilityWhiteWinsWhiteTurn() {
		Minimax.player = whitePlayer;
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 1, 2, true));
		pawns.add(new Pawn(true, 1, 1, false));
		State s = new State(pawns, false);
		assertTrue(s.isTerminal());
		double utility = s.getUtility();
		assertTrue(utility > 0);
	}
	
	@Test
	public void testGetUtilityWhiteWinsBlackTurn() {
		Minimax.player = blackPlayer;
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 1, 2, true));
		pawns.add(new Pawn(true, 1, 1, false));
		State s = new State(pawns, true);
		assertTrue(s.isTerminal());
		double utility = s.getUtility();
		assertTrue(utility < 0);
	}
	
	@Test
	public void testPrintBoard() {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(true, 1, 2, false));
		
		State s = new State(pawns, false);
		assertTrue(s.toString().startsWith("         \nB"));
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
			found = ((state.getPawns().size() == initialPawnState.size() - 2));
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
			found = ((state.getPawns().size() == initialPawnState.size() - 3));
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
			found = ((state.getPawns().size() == initialPawnState.size() - 3));
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
			found = ((state.getPawns().size() == initialPawnState.size() - 1));
			if (found)
				break;
		}
		assertFalse(found);
	}
	
	@Test
	public void testNotEatKingInTrone() {
		List<Pawn> initialPawnState = new LinkedList<>();
		initialPawnState.add(new Pawn(false, 5, 5, true)); // King
		
		initialPawnState.add(new Pawn(true, 4, 5, false)); // Moving pawn
		
		initialPawnState.add(new Pawn(true, 7, 5, false));
		
		State currentState = new State(initialPawnState, true); // Black turn
		Collection<State> afterState = currentState.getActions();
		
		boolean found = false;
		for (State state : afterState) {
			found = ((state.getPawns().size() == initialPawnState.size() - 1));
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
			found = ((state.getPawns().size() == initialPawnState.size() - 1));
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

		assertTrue(afterState.stream().allMatch(s -> s.getPawns().size() == initialPawnState.size()));
	}
	
	@Test
	public void testSymmetricsGeneratingChildStates() {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, false));
		pawns.add(new Pawn(false, 7, 3, false));
		pawns.add(new Pawn(false, 3, 7, false));
		pawns.add(new Pawn(false, 7, 7, false));
		State s = new State(pawns, false); // White turn
		assertEquals(5, s.getActions().size());
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
			if(symmetricalDiagonal && !pawns.stream().anyMatch(p -> p.getX() + pawn.getY() == 10 
						&& p.getY() + pawn.getX()  == 10
						&& p.isBlack()==pawn.isBlack() 
						&& p.king==pawn.king))
					 {
				symmetricalDiagonal = false;
			}
		}
		assertTrue(symmetricalDiagonal);
		
		for (Pawn pawn : pawns.stream()/*.filter(p -> p.position.y < 5)*/.collect(Collectors.toList())) {
			if (!pawns.stream().anyMatch(p -> p.getX()==pawn.getX() && p.getY()+pawn.getY()==10 && p.isBlack()==pawn.isBlack() && p.king==pawn.king)) {
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
	@Test
	public void testGetAction() throws Exception {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, false));
		State s = new State(pawns, false);
		try{
			s.getAction();
			fail("It should throw exception");
		}catch(NullPointerException e){
			;
		}
		Action a = s.getActions().stream().findFirst().get().getAction();
		assertEquals("d3", a.getTo());
		assertEquals("c3", a.getFrom());
		assertEquals("W", a.getState());
	}
	
	@Test
	public void testGetActionOnEat() throws Exception {
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
			found = ((state.getPawns().size() == initialPawnState.size() - 3));
			if (found){
				Action a = state.getAction();
				assertEquals("g4", a.getTo());
				assertEquals("a4", a.getFrom());
				assertEquals("B", a.getState());
				break;
			}
		}
		assertTrue(found);
	}
	
	@Test
	public void testROI() throws Exception {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, false));
		State s = new State(pawns, false);
		assertTrue(s.checkROI(3, 2, 3, 4, p -> true));
		assertFalse(s.checkROI(2, 2, 2, 4, p -> true));
		
		assertFalse(s.checkROI(3, 2, 3, 4, p -> p.isBlack()));
		
		assertEquals(1, s.checkROIQuantity(3, 2, 3, 4, p -> true));
		assertEquals(0, s.checkROIQuantity(2, 2, 2, 4, p -> true));
	}
	@Test
	public void testholedROI() throws Exception {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, false));
		State s = new State(pawns, false);
		assertTrue(s.checkROI(3, 2, 5, 6, State.holedROIPredicateFactory(3, 3, 4, 6)));
		assertFalse(s.checkROI(2, 2, 6, 4, State.holedROIPredicateFactory(2, 3, 4, 6)));
		
		assertFalse(s.checkROI(3, 2, 3, 4, State.holedROIPredicateFactory(3, 3, 4, 6).and(p -> p.isBlack())));
		
		assertEquals(1, s.checkROIQuantity(3, 2, 3, 4, State.holedROIPredicateFactory(3, 3, 4, 6)));
		assertEquals(0, s.checkROIQuantity(2, 2, 2, 4, State.holedROIPredicateFactory(3, 3, 4, 6)));
	}
	@Test
	public void testKingEscape() throws Exception {
		List<Pawn> pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, true));
		State s = new State(pawns, false);
		assertEquals(4, s.kingEscape());
		
		pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, true));
		pawns.add(new Pawn(false, 4, 3, false));
		s = new State(pawns, false);
		assertEquals(3, s.kingEscape());
		
		pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 3, 3, true));
		pawns.add(new Pawn(false, 4, 3, false));
		pawns.add(new Pawn(true, 3, 4, false));
		s = new State(pawns, false);
		assertEquals(2, s.kingEscape());
		
		pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 7, 2, true));
		s = new State(pawns, false);
		assertEquals(3, s.kingEscape());
		
		pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 7, 2, true));
		pawns.add(new Pawn(false, 7, 3, false));
		s = new State(pawns, false);
		assertEquals(2, s.kingEscape());
		
		pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 4, 6, true));
		s = new State(pawns, false);
		assertEquals(0, s.kingEscape());
		
		pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 4, 8, true));
		s = new State(pawns, false);
		assertEquals(1, s.kingEscape());
		
		pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 7, 3, true));
		s = new State(pawns, false);
		assertEquals(4, s.kingEscape());
		
		pawns.add(new Pawn(false, 7, 9, false));
		s = new State(pawns, false);
		assertEquals(3, s.kingEscape());
		
		pawns.add(new Pawn(false, 7, 1, false));
		s = new State(pawns, false);
		assertEquals(2, s.kingEscape());
		
		pawns.add(new Pawn(false, 1, 3, false));
		s = new State(pawns, false);
		assertEquals(1, s.kingEscape());
		
		pawns.add(new Pawn(false, 9, 3, false));
		s = new State(pawns, false);
		assertEquals(0, s.kingEscape());
		
		pawns = new LinkedList<>();
		pawns.add(new Pawn(false, 7, 5, true));
		s = new State(pawns, false);
		assertEquals(2, s.kingEscape());
		
		pawns.add(new Pawn(false, 7, 6, false));
		s = new State(pawns, false);
		assertEquals(1, s.kingEscape());
	}
}
