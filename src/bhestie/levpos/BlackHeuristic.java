package bhestie.levpos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlackHeuristic {
	public State state;
	
	public final Map<Position, Boolean> criticalKingPositions = new HashMap<Position, Boolean>();
	
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
		
		//if(this.criticalKingPositions == null)
			initMap();
	}
	
	public boolean isRowBlocked(int fromX, int toX, int y) {
		boolean result = false;
		
		for (int i = fromX; i <= toX; i++) {
			int x = i;
			//XXX Perché qui i non lo compila, mentre in isColumnBlocked sotto sì?
			if (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(Position.of(x, y)))) {
				result = true;
			}
		}
		return result;
	}
	
	public boolean isColumnBlocked(int x, int fromY, int toY) {
		boolean result = false;
		
		for (int i = fromY; i <= toY; i++) {
			int y = i;
			if (this.state.getPawns().stream().anyMatch(p -> !p.king && p.position.equals(Position.of(x, y)))) {
				result = true;
			}
		}
		return result;
	}
	
	
	/**
	 * This map contains all the possible cases where the king is in a critical position:
	 * i.e. outside the throne
	 * TODO Static class to implement this map is better
	 */
	public void initMap() {
		//NW
		criticalKingPositions.put(Position.of(2, 2), (this.isColumnBlocked(2, 1, 1)
								&& this.isRowBlocked(1, 1, 2)));
		criticalKingPositions.put(Position.of(2, 3), (this.isColumnBlocked(2, 1, 2)
				&& this.isRowBlocked(1, 3, 3) && this.isRowBlocked(3, 9, 3)));
		criticalKingPositions.put(Position.of(2, 4), (this.isColumnBlocked(2, 1, 3)));
		criticalKingPositions.put(Position.of(3, 2), (this.isColumnBlocked(3, 1, 1)
				&& this.isRowBlocked(1, 2, 2)));
		criticalKingPositions.put(Position.of(3, 3), (this.isColumnBlocked(3, 4, 9))
				&& this.isColumnBlocked(3, 1, 2) && this.isRowBlocked(1, 2, 3) 
				&& this.isRowBlocked(3, 9, 3));
		criticalKingPositions.put(Position.of(3, 4), (this.isRowBlocked(1, 3, 3) 
				&& this.isRowBlocked(5, 9, 3)));
		criticalKingPositions.put(Position.of(3, 5), (this.isRowBlocked(1, 4, 3) 
				&& this.isRowBlocked(6, 9, 3)));
		criticalKingPositions.put(Position.of(4, 2), (this.isRowBlocked(1, 3, 2)));
		criticalKingPositions.put(Position.of(4, 3), (this.isRowBlocked(1, 3, 3) 
				&& this.isRowBlocked(5, 9, 3)));
		
		//NE
		criticalKingPositions.put(Position.of(6, 2), (this.isRowBlocked(7, 9, 2)));
		criticalKingPositions.put(Position.of(6, 3), (this.isRowBlocked(7, 9, 3)));
		criticalKingPositions.put(Position.of(7, 2), (this.isRowBlocked(8, 9, 2) 
				&& this.isColumnBlocked(7, 1, 1) && this.isColumnBlocked(7, 3, 9)));
		criticalKingPositions.put(Position.of(7, 3), (this.isRowBlocked(1, 6, 3)
				&& this.isRowBlocked(8, 9, 3) && this.isColumnBlocked(7, 1, 2) 
				&& this.isColumnBlocked(7, 4, 9)));
		criticalKingPositions.put(Position.of(7, 4), (this.isRowBlocked(8, 9, 4) 
				&& this.isColumnBlocked(7, 1, 3) && this.isColumnBlocked(7, 5, 9)));
		criticalKingPositions.put(Position.of(7, 5), (this.isRowBlocked(8, 9, 5) 
				&& this.isColumnBlocked(7, 1, 4) && this.isColumnBlocked(7, 6, 9)));
		criticalKingPositions.put(Position.of(8, 2), (this.isRowBlocked(9, 9, 2) 
				&& this.isColumnBlocked(8, 1, 1)));
		criticalKingPositions.put(Position.of(8, 3), (this.isRowBlocked(9, 9, 3) 
				&& this.isColumnBlocked(8, 1, 2)));
		criticalKingPositions.put(Position.of(8, 4), (this.isColumnBlocked(8, 1, 3)));
		
		//SW
		criticalKingPositions.put(Position.of(2, 6), (this.isColumnBlocked(2, 7, 9)));
		criticalKingPositions.put(Position.of(2, 7), (this.isColumnBlocked(2, 8, 9)));
		criticalKingPositions.put(Position.of(2, 8), (this.isColumnBlocked(2, 9, 9)));
		criticalKingPositions.put(Position.of(3, 6), (this.isColumnBlocked(3, 1, 5)
				&& this.isColumnBlocked(3, 7, 9)));
		criticalKingPositions.put(Position.of(3, 7), (this.isColumnBlocked(3, 1, 6)
				&& this.isColumnBlocked(3, 8, 9) && this.isRowBlocked(1, 2, 7) 
				&& this.isRowBlocked(4, 9, 7)));
		criticalKingPositions.put(Position.of(3, 8), (this.isColumnBlocked(3, 1, 7)
				&& this.isColumnBlocked(3, 9, 9) && this.isRowBlocked(1, 2, 8)));
		criticalKingPositions.put(Position.of(4, 7), (this.isRowBlocked(1, 3, 7)
				&& this.isRowBlocked(5, 9, 7)));
		criticalKingPositions.put(Position.of(4, 8), (this.isRowBlocked(1, 3, 8)));
		
		//SE
		criticalKingPositions.put(Position.of(6, 7), (this.isRowBlocked(1, 5, 7)
				&& this.isRowBlocked(7, 9, 7)));
		criticalKingPositions.put(Position.of(6, 8), (this.isRowBlocked(7, 9, 8)));
		criticalKingPositions.put(Position.of(7, 6), (this.isColumnBlocked(7, 1, 5)
				&& this.isColumnBlocked(7, 7, 9)));
		criticalKingPositions.put(Position.of(7, 7), (this.isColumnBlocked(7, 1, 6)
				&& this.isColumnBlocked(7, 8, 9) && this.isRowBlocked(1, 6, 7) 
				&& this.isRowBlocked(8, 9, 7)));
		criticalKingPositions.put(Position.of(7, 8), (this.isColumnBlocked(7, 1, 6)
				&& this.isColumnBlocked(7, 9, 9)));
		criticalKingPositions.put(Position.of(8, 6), (this.isColumnBlocked(8, 7, 9)));
		criticalKingPositions.put(Position.of(8, 7), (this.isColumnBlocked(8, 8, 9)
				&& this.isRowBlocked(1, 7, 7) && this.isRowBlocked(9, 9, 7)));
		criticalKingPositions.put(Position.of(8, 8), (this.isColumnBlocked(8, 9, 9)
				&& this.isRowBlocked(9, 9, 8)));
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
	
	public boolean isKingEscapeBlocked(Pawn king) {
		if(this.criticalKingPositions.containsKey(king.position)) {
			return this.criticalKingPositions.get(king.position);
		}
		return true;
	}
	
	public double calculateMoveGoodness() {
		Optional<Pawn> king = this.state.getPawns().stream().filter(p -> p.king).findAny();
		
		//Terminal states
		if (!king.isPresent()) { //Win
			return Double.MAX_VALUE;
		}
		if (this.state.isDrawCase()) { //Draw
			return 0;
		} else if (this.state.isTerminal() && king.isPresent() && !this.state.isDrawCase()) { //Lost
			return -Double.MAX_VALUE;
		}
		
		//King is outside the "citadel" (throne)
		if(isKingEscapeBlocked(king.get())) {
			return 10000;
		} else if(!isKingEscapeBlocked(king.get()))
			return -10000;
		
		//Block Escape routes
		//TODO C'è un modo per evitare le permutazioni di tutti i casi? (Es. 3 casi -> return 3000, 4 casi -> return 4000)
		if (isFirstEscapeRouteBlocked() || isSecondEscapeRouteBlocked() || isThirdEscapeRouteBlocked() || isFourthEscapeRouteBlocked()
				|| isFifthEscapeRouteBlocked() || isSixthEscapeRouteBlocked() || isSeventhEscapeRouteBlocked() || isEighthEscapeRouteBlocked()) {
			int temp = 0;
			
			if (isFirstEscapeRouteBlocked())
				temp++;
			if (isSecondEscapeRouteBlocked())
				temp++;
			if (isThirdEscapeRouteBlocked())
				temp++;
			if (isFourthEscapeRouteBlocked())
				temp++;
			if (isFifthEscapeRouteBlocked())
				temp++;
			if (isSixthEscapeRouteBlocked())
				temp++;
			if (isSeventhEscapeRouteBlocked())
				temp++;
			if (isEighthEscapeRouteBlocked())
				temp++;
			return temp*1000;
		}
		else {
			//Do a move "at random"
			return 1;
		}
	}
}
