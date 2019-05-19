package bhestie.levpos.tests;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import bhestie.levpos.Minimax;
import bhestie.levpos.Pawn;
import bhestie.levpos.State;

public class TestMinimax {
	private static final boolean whitePlayer = false;
    private static final boolean blackPlayer = !whitePlayer;
    
	@Test
	public void testScaleUp() {
		Minimax.player = blackPlayer;
		Minimax.FIXEDDEPTH = false;
		Minimax.TIMEOUT = 0;
		Minimax.DEPTH = 1;
		Minimax.SCALINGFACTORUP = 0;
		Minimax.SCALINGFACTORDOWN = 0;
		
		Minimax.init();
		
		List<Pawn> p = new LinkedList<>();
		p.add(new Pawn(true, 2, 2, false));
		State s = new State(p, Minimax.player);
		
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1 and increase
		assertEquals(2, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 2 and increase
		assertEquals(3, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 3 and increase
		assertEquals(4, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 4 and increase
		assertEquals(5, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 5 and increase
		assertEquals(6, Minimax.DEPTH);
		
		Minimax.interrupt(); // signals
		assertEquals(5, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 5 and increase
		assertEquals(6, Minimax.DEPTH);
		
		Minimax.DEPTH = 1;
		Minimax.SCALINGFACTORUP = 5;
		Minimax.SCALINGFACTORDOWN = 0;
		
		Minimax.init();
		
		p.clear();
		p.add(new Pawn(true, 2, 2, false));
		s = new State(p, Minimax.player);
		
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1 and increase
		assertEquals(2, Minimax.DEPTH);
		
		
		Minimax.alphaBethInit(s);
		assertEquals(2, Minimax.DEPTH);
		
		Minimax.interrupt(); // signals
		assertEquals(1, Minimax.DEPTH);

		Minimax.alphaBethInit(s); // do this at depth 1
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1
		assertEquals(1, Minimax.DEPTH);
		Minimax.alphaBethInit(s); // do this at depth 1 and increase
		assertEquals(2, Minimax.DEPTH);
	}

}
