package bhestie.levpos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import bhestie.levpos.utils.HistoryStorage;

public class State {
	public final HistoryStorage historyStorage;
	/**
	 * Turn. TRUE=Black, FALSE=White
	 */
	public final boolean turn;
	/**
	 * List of Pawn in the board.
	 */
	public final List<Pawn> pawns;

	/**
	 * List of citadels
	 */
	private static final List<Citadel> citadels = new ArrayList<>(4); 
	/**
	 * King trone position
	 */
	private static final Position tronePosition = new Position(5, 5);
	/**
	 * List of escape positions. The king in this positions wins.
	 */
	private static final List<Position> escapePositions = new ArrayList<>(16);
	/**
	 * List of positions where the king have to be fully surrounded.
	 */
	private static final List<Position> protectedKingPositions = new LinkedList<>();
	static {
		ArrayList<Position> citadelPositions = new ArrayList<>(4);
		citadelPositions.add(new Position(5, 1));
		citadelPositions.add(new Position(4, 1));
		citadelPositions.add(new Position(6, 1));
		citadelPositions.add(new Position(5, 2));
		citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		citadelPositions.add(new Position(1, 5));
		citadelPositions.add(new Position(1, 4));
		citadelPositions.add(new Position(1, 6));
		citadelPositions.add(new Position(2, 5));
		citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		citadelPositions.add(new Position(9, 5));
		citadelPositions.add(new Position(9, 4));
		citadelPositions.add(new Position(9, 6));
		citadelPositions.add(new Position(8, 5));
		citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		citadelPositions.add(new Position(5, 9));
		citadelPositions.add(new Position(4, 9));
		citadelPositions.add(new Position(6, 9));
		citadelPositions.add(new Position(5, 8));
		citadels.add(new Citadel(citadelPositions));
		
		/***** ESCAPE POSITIONS *****/
		
		escapePositions.add(new Position(1, 2));
		escapePositions.add(new Position(1, 3));
		escapePositions.add(new Position(1, 7));
		escapePositions.add(new Position(1, 8));
		
		escapePositions.add(new Position(2, 1));
		escapePositions.add(new Position(3, 1));
		escapePositions.add(new Position(7, 1));
		escapePositions.add(new Position(8, 1));
		
		escapePositions.add(new Position(9, 2));
		escapePositions.add(new Position(9, 3));
		escapePositions.add(new Position(9, 7));
		escapePositions.add(new Position(9, 8));
		
		escapePositions.add(new Position(2, 9));
		escapePositions.add(new Position(3, 9));
		escapePositions.add(new Position(7, 9));
		escapePositions.add(new Position(8, 9));
		
		/***** PROTECTED KING POSITIONS *****/
		protectedKingPositions.add(tronePosition);
		protectedKingPositions.add(new Position(5, 6));
		protectedKingPositions.add(new Position(5, 4));
		protectedKingPositions.add(new Position(4, 5));
		protectedKingPositions.add(new Position(6, 5));
	}
	
	public State(List<Pawn> pawns, boolean turn) {
		this(pawns, turn, new HistoryStorage());
	}
	
	/**
	 * Creates a new State
	 * @param pawns The pawns in the board
	 * @param turn The turn. TRUE=Black, FALSE=White
	 */
	public State(List<Pawn> pawns, boolean turn, HistoryStorage historyStorage){
		this.turn = turn;
		this.pawns = new LinkedList<>(pawns);
		this.historyStorage = historyStorage;
	}

	/**
	 * Get the list of the next possible States.
	 * @return The list of the next possible States.
	 */
	public Collection<State> getActions(){
		List<State> actions = new LinkedList<>();

		// TODO controllare se la scacchiera è simmetrica, nel caso non controllare tutti i pawn
		// se è simmetrica NORD-SUD e/o EST-OVEST
		for (Pawn currentPawn : this.pawns.stream().filter(p -> p.filterByTurn(this.turn)).collect(Collectors.toList())) {

			for (int i = currentPawn.position.x + 1; i <= 9; i++) {
				if (!this.checkXY(i, currentPawn.position.y, actions, currentPawn))
					break;
			}

			for (int i = currentPawn.position.x - 1; i >= 1; i--) {
				if (!this.checkXY(i, currentPawn.position.y, actions, currentPawn))
					break;
			}

			for (int i = currentPawn.position.y + 1; i <= 9; i++) {
				if(!this.checkXY(currentPawn.position.x, i, actions, currentPawn))
					break;
			}

			for (int i = currentPawn.position.y - 1; i >= 1; i--) {
				if(!this.checkXY(currentPawn.position.x, i, actions, currentPawn))
					break;
			}

		}

		return actions;
	}

	/**
	 * Checks if a Pawn can move to X and Y. In this case inserts a new action in the list
	 * @param x The nex X position
	 * @param y The new Y position
	 * @param actions The list of next possible states
	 * @param currentPawn The pawn that is moving
	 * @return If it added a new action of not
	 */
	private boolean checkXY(final int x,final int y, List<State> actions, Pawn currentPawn) {
		boolean haveToAddThePawn;

		haveToAddThePawn = !this.pawns.stream().anyMatch(p -> p.position.y == y && p.position.x == x); // Se non c'è già un altro pezzo

		if (currentPawn.bw == false /*is white*/
				|| currentPawn.bw == true && !citadels.stream().anyMatch(c -> c.isPawnInCitadel(currentPawn)) /*il pezzo è nero e non in una citadel*/ ) {
			haveToAddThePawn = haveToAddThePawn && !citadels.stream().anyMatch(c -> c.isXYInCitadel(x, y));
		}

		if (currentPawn.bw == true /*is black*/
				&& citadels.stream().anyMatch(c -> c.isPawnInCitadel(currentPawn))) { // pezzo nero e dentro una citadel, allora può muoversi solo dentro la sua citadel, non può andare in un'altra citadel
			haveToAddThePawn = haveToAddThePawn && !citadels.stream().filter(c -> !c.isPawnInCitadel(currentPawn)).anyMatch(c -> c.isXYInCitadel(x, y));
		}

		haveToAddThePawn = haveToAddThePawn && (x != tronePosition.x || y != tronePosition.y);

		if (haveToAddThePawn) {
			List<Pawn> newPawns = new LinkedList<>(this.pawns);
			newPawns.remove(currentPawn);
			Pawn newPawn = new Pawn(currentPawn.bw, x, y, currentPawn.king);
			boolean haveEaten = checkPawnsEaten(x, y, currentPawn, newPawns);
			newPawns.add(newPawn);
			HistoryStorage newHistoryStorage = (haveEaten ? new HistoryStorage() : this.historyStorage.clone()); // if eaten -> new storage
			newHistoryStorage.add(newPawns);
			State newState = new State(newPawns, !this.turn, newHistoryStorage);
			actions.add(newState);
			return true;
		}
		return false;
	}

	/**
	 * Checks if (after moving a @param movedPawn) some pawns have to be eaten.
	 * @param toX The new X position
	 * @param toY The new Y position
	 * @param movedPawn The pawn that is moved
	 * @param newPawns List of new Pawns in the next state board
	 * @return If some pawns have been eaten.
	 */
	private boolean checkPawnsEaten(int toX, int toY, Pawn movedPawn, List<Pawn> newPawns) {
		boolean haveEaten = false;
		// Get all the "near" pawns (near means +1 or -1 in vertical or horrizontal) 
		List<Pawn> pawnOfInterest = newPawns.stream().filter(p -> p.bw != movedPawn.bw && (Math.abs(p.position.x - toX) + Math.abs(p.position.y - toY) == 1)).collect(Collectors.toList());
		for (Pawn pawn : pawnOfInterest) {
			final int deltaX = (pawn.position.x - toX)*2;
			final int deltaY = (pawn.position.y - toY)*2;
			final int partnerPositionX = deltaX + toX;
			final int partnerPositionY = deltaY + toY;
			boolean haveToEat = newPawns.stream().anyMatch(p -> p.bw == movedPawn.bw && (p.position.x == partnerPositionX && p.position.y == partnerPositionY)) ||
					(tronePosition.x == partnerPositionX && tronePosition.y == partnerPositionY) ||
					(citadels.stream().anyMatch(c -> c.isXYInFringeCitadels(partnerPositionX, partnerPositionY)));
			
			if (haveToEat && pawn.king && protectedKingPositions.contains(pawn.position)) { // Special case for king
				final int partnerPositionX_2 = pawn.position.x + (deltaY>0 ? 1 : 0);
				final int partnerPositionY_2 = pawn.position.y + (deltaX>0 ? 1 : 0);
				haveToEat = newPawns.stream().anyMatch(p -> p.bw == movedPawn.bw && (p.position.x == partnerPositionX_2 && p.position.y == partnerPositionY_2)) ||
						(tronePosition.x == partnerPositionX_2 && tronePosition.y == partnerPositionY_2);
				final int partnerPositionX_3 = pawn.position.x + (deltaY>0 ? -1 : 0);
				final int partnerPositionY_3 = pawn.position.y + (deltaX>0 ? -1 : 0);
				haveToEat = newPawns.stream().anyMatch(p -> p.bw == movedPawn.bw && (p.position.x == partnerPositionX_3 && p.position.y == partnerPositionY_3)) ||
						(tronePosition.x == partnerPositionX_3 && tronePosition.y == partnerPositionY_3);
			}
			
			if (haveToEat) {
				newPawns.remove(pawn);
				haveEaten = true;
			}
		}
		return haveEaten;
	}

	/**
	 * Returns a value that stimate the "goodness" of the pawns in the board.
	 * @return A value that stimate the "goodness" of the pawns in the board.
	 */
	public long getHeuristic() {
		// TODO da fare
		return 0;
	}

	/**
	 * Checks if the board is in a terminal position.
	 * @return If the board is in a terminal position.
	 */
	public boolean isTerminal() {
		if (!this.pawns.stream().anyMatch(p -> p.bw == true /*is black*/)) // No more black pawns
			return true;
		Optional<Pawn> king = this.pawns.stream().filter(p -> p.king).findAny();
		// XXX comprimere gli if di sotto
		if (!king.isPresent()) { // Se non c'è il re -> nero vince
			return true;
		} else if (kingEscaped(king.get())) { // Il re scappa -> bianco vince
			return true;
		}
		return false;
	}

	// XXX rimuovere ed evitare la chiamata a funzione
	private boolean kingEscaped(Pawn king) {
		return (escapePositions.contains(king.position)); // Se il re è nella posizione di una via di fuga
	}

	/**
	 * Returns a value that stimate the "goodness" of a terminal state of the game.
	 * @return A value that stimate the "goodness" of a terminal state of the game.
	 */
	public double getUtility() {
		if (this.isTerminal()) {
			// TODO Dire se ho vinto o perso (o pareggiato)
		} else {
			System.err.println("Non ci devo andare");
		}
		return Math.random();
		/*if(firstPlayer)
			return Double.MIN_VALUE;
		else
			return Double.MAX_VALUE;*/
	}

	/**
	 * Prints the board in a String
	 * @return The board as a String
	 */
	public String printBoard() {
		StringBuilder result = new StringBuilder();
		for(int i = 1; i <= 9; i++) { // scan y
			for(int j = 1; j <= 9; j++) { // scan x
				int x = j;
				int y = i;
				Optional<Pawn> pawn = this.pawns.stream().filter(p -> p.position.x==x && p.position.y==y).findAny();
				if(pawn.isPresent()) {
					result.append((pawn.get().bw ? 'B' : (pawn.get().king) ? 'K' : 'W'));
				} else {
					result.append('0');
				}
			}
			result.append('\n');
		}
		return result.toString();
	}
	
	@Override
	public String toString() {
		return this.printBoard();
	}
}
