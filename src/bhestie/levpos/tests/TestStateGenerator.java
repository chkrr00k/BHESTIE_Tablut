package bhestie.levpos.tests;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;

import bhestie.levpos.Minimax;
import bhestie.levpos.Pawn;
import bhestie.levpos.State;

public class TestStateGenerator {

	private static final boolean whitePlayer = false;
    private static final boolean blackPlayer = !whitePlayer;
    
    @Test
    public void test() {
    	Minimax.player = blackPlayer;
    	Minimax.DEPTH = 3;
    	Minimax.FIXEDDEPTH = true;
    	Minimax.init();
    	
    	List<Pawn> pawns = new LinkedList<>();
    	pawns.add(new Pawn(false, 4, 2, true));
    	pawns.add(new Pawn(true, 3, 2, false));
    	pawns.add(new Pawn(false, 4, 5, false));
    	pawns.add(new Pawn(false, 5, 3, false));
    	
    	State s = new State(pawns, Minimax.player);
    	
    	// XXX it fails at depth=2 after moving pawn from (C2) to (C1). King doesn't generates extra moves
//    	Minimax.alphaBethInit(s);
    	
    	Minimax.player = whitePlayer;
    	Minimax.DEPTH = 3;
    	Minimax.FIXEDDEPTH = true;
    	Minimax.init();
    	
    	pawns.clear();
    	pawns.add(new Pawn(false, 4, 2, true));
    	pawns.add(new Pawn(true, 3, 1, false));
    	pawns.add(new Pawn(false, 4, 5, false));
    	pawns.add(new Pawn(false, 5, 3, false));
    	
    	s = new State(pawns, Minimax.player);
    	List<State> actions = StreamSupport.stream(s.getChildren().spliterator(), false).collect(Collectors.toList());
    	assertEquals(5, actions.size());
    }

}
