package bhestie.levpos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BlackHeuristic {
	public State state;
	
	private static final List<Position> escapeRouteBlocked = new ArrayList<Position>();
	static {
		//NW
		escapeRouteBlocked.add(Position.of(2, 3)); //I
		escapeRouteBlocked.add(Position.of(3, 2)); //II
		
		//NE
		escapeRouteBlocked.add(Position.of(7, 2)); //III
		escapeRouteBlocked.add(Position.of(8, 3)); //IV
		
		//SW
		escapeRouteBlocked.add(Position.of(2, 7)); //V
		escapeRouteBlocked.add(Position.of(8, 3)); //VI
		
		//SE
		escapeRouteBlocked.add(Position.of(7, 8)); //VII
		escapeRouteBlocked.add(Position.of(8, 7)); //VIII
	}
	
	public BlackHeuristic(State state) {
		if (state == null)
			throw new IllegalArgumentException("The state can't be null");
		if (!state.isTurn()) //FALSE=White
			throw new IllegalArgumentException("It's white turn. Black heuristic isn't supposed to be called");
		this.state = state;
	}
	
	public boolean isFirstEscapeRouteBlocked() {
		return (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(escapeRouteBlocked.get(0))));
	}
	
	public boolean isSecondEscapeRouteBlocked() {
		return (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(escapeRouteBlocked.get(1))));
	}
	
	public boolean isThirdEscapeRouteBlocked() {
		return (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(escapeRouteBlocked.get(2))));
	}
	
	public boolean isFourthEscapeRouteBlocked() {
		return (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(escapeRouteBlocked.get(3))));
	}
	
	public boolean isFifthEscapeRouteBlocked() {
		return (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(escapeRouteBlocked.get(4))));
	}
	
	public boolean isSixthEscapeRouteBlocked() {
		return (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(escapeRouteBlocked.get(5))));
	}
	
	public boolean isSeventhEscapeRouteBlocked() {
		return (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(escapeRouteBlocked.get(6))));
	}
	
	public boolean isEighthEscapeRouteBlocked() {
		return (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(escapeRouteBlocked.get(7))));
	}
	
	public static final Set<Position> criticalKingPositions = new HashSet<Position>();
	static {
		
	}
	
	public boolean isKingEscapeBlocked(Pawn king) {
		//TODO Set of positions is better?
		if (king.position.equals(Position.of(3, 5))) {
			if (this.state.getPawns().stream().anyMatch(p -> p.position.equals(Position.of(3, 4))
					 || p.position.equals(Position.of(3, 3)) || p.position.equals(Position.of(3, 2))
					 || p.position.equals(Position.of(3, 1)))
					&& this.state.getPawns().stream().anyMatch(p -> p.position.equals(Position.of(3, 6))
							|| p.position.equals(Position.of(3, 7)) || p.position.equals(Position.of(3, 8))
							|| p.position.equals(Position.of(3,  9))))
				return true;
			else {
				return false;
			}
		}
		return true;
	}
	
	public long calculateMoveGoodness() {
		Optional<Pawn> king = this.state.getPawns().stream().filter(p -> p.king).findAny();
		if (!king.isPresent())
			return Long.MAX_VALUE;
		else { //king is present
			//White victory imminent
			if(isKingEscapeBlocked(king.get()))
				return 10000;
			
			//Block Escape routes
			if(isFirstEscapeRouteBlocked())
				
			
			//Do a move "at random"
			return 1;
		}
		return 0; //Draw
	}
}
