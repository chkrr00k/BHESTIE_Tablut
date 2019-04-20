package bhestie.levpos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import bhestie.levpos.utils.HistoryStorage;

public class State {
	private final State parent;
	private final boolean drawCase;
	/**
	 * History storage. Storage of all mosses done.
	 */
	private final HistoryStorage historyStorage;
	/**
	 * Turn. TRUE=Black, FALSE=White
	 */
	private final boolean turn;
	/**
	 * List of Pawn in the board.
	 */
	private final List<Pawn> pawns;

	/**
	 * Creates a new State
	 * @param pawns The pawns in the board
	 * @param turn The turn. TRUE=Black, FALSE=White
	 */
	public State(List<Pawn> pawns, boolean turn) {
		this(pawns, turn, new HistoryStorage(), null);
	}

	/**
	 * Creates a new State
	 * @param pawns The pawns in the board
	 * @param turn The turn. TRUE=Black, FALSE=White
	 * @param historyStorage The history storage
	 * @param parent The parent
	 */
	public State(List<Pawn> pawns, boolean turn, HistoryStorage historyStorage, State parent){
		this(pawns, turn, historyStorage, parent, false);
	}
	public State(List<Pawn> pawns, boolean turn, HistoryStorage historyStorage, State parent, boolean drawCase){
		this.turn = turn;
		this.pawns = new LinkedList<Pawn>(pawns);
		this.historyStorage = historyStorage;
		this.parent = parent;
		this.drawCase = drawCase;
	}

	/**
	 * @return the pawns
	 */
	public List<Pawn> getPawns() {
		return this.pawns;
	}

	/**
	 * @return the turn
	 */
	public boolean isTurn() {
		return this.turn;
	}

	/**
	 * Get the list of the next possible States.
	 * @return The list of the next possible States.
	 */
	public Collection<State> getActions(){
		List<State> actions = new LinkedList<State>();

		boolean symmetricalNorthSouth = true;
		boolean symmetricalEastWest = true;
		boolean symmetricalDiagonal = true;
		boolean symmetricalAntiDiagonal = true;

		// Check the symmetries
		for (Pawn pawn : this.getPawns()) {
			if (symmetricalNorthSouth && !this.getPawns().stream().anyMatch(p -> p.getX()==pawn.getX() && p.getY() +pawn.getY()==10 && p.isBlack()==pawn.isBlack() && p.king==pawn.king)) {
				symmetricalNorthSouth = false;
			}
			if (symmetricalEastWest && !this.getPawns().stream().anyMatch(p -> p.getX()+pawn.getX()==10 && p.getY()==pawn.getY() && p.isBlack()==pawn.isBlack() && p.king==pawn.king)) {
				symmetricalEastWest = false;
			}
			if (symmetricalDiagonal && !this.getPawns().stream().anyMatch(p -> p.getX()==pawn.getY() && p.getY()==pawn.getX() && p.isBlack()==pawn.isBlack() && p.king==pawn.king)) {
				symmetricalDiagonal = false;
			}
			if(symmetricalAntiDiagonal && !getPawns().stream().anyMatch(p -> p.getX() + pawn.getY() == 10 && p.getY() + pawn.getX() == 10 && p.isBlack()==pawn.isBlack()  && p.king==pawn.king)) {
				symmetricalAntiDiagonal = false;
			}
			if(!(symmetricalDiagonal || symmetricalEastWest || symmetricalNorthSouth || symmetricalAntiDiagonal)){
				break;
			}
		}

		Stream<Pawn> pawnToScanStream = this.getPawns().stream().filter(p -> p.filterByTurn(this.turn));

		if (symmetricalEastWest){
			pawnToScanStream = pawnToScanStream.filter(p -> p.getX()>= 5); // Takes only the high part, X from 5 to 9 (from E to I)
		}
		if (symmetricalNorthSouth){
			pawnToScanStream = pawnToScanStream.filter(p -> p.getY() >= 5); // Takes only the high part, Y from 5 to 9
		}
		if (symmetricalDiagonal){
			pawnToScanStream = pawnToScanStream.filter(p -> p.getY() >= p.getX()); // Takes only the lower triangle part
		}
		if (symmetricalAntiDiagonal){
			pawnToScanStream = pawnToScanStream.filter(p -> p.getX() + p.getY() >= 10); // Takes only the lower triangle part (built in the anti-diagonal way)
		}

		Collection<Pawn> pawnToScan = pawnToScanStream.collect(Collectors.toList());

		final boolean checkXY = !(symmetricalEastWest && symmetricalNorthSouth && symmetricalDiagonal && symmetricalAntiDiagonal); // if this -> it's simmetrical and I can check only the X assis
		int stopDecrementX = 0, stopDecrementY = 0;
		for (Pawn currentPawn : pawnToScan) {

			stopDecrementX = (symmetricalEastWest && currentPawn.getX() == 5 ? 5 : 1);
			for (int i = currentPawn.getX() + 1; i <= 9; i++) {
				if (!this.checkXY(i, currentPawn.getY(), actions, currentPawn))
					break;
			}

			for (int i = currentPawn.getX() - 1; i >= stopDecrementX; i--) {
				if (!this.checkXY(i, currentPawn.getY(), actions, currentPawn))
					break;
			}

			if (checkXY){
				stopDecrementY = (symmetricalNorthSouth && currentPawn.getY() == 5 ? 5 : 1);
				for (int i = currentPawn.getY() + 1; i <= 9; i++) {
					if(!this.checkXY(currentPawn.getX(), i, actions, currentPawn)){
						break;
					}
				}

				for (int i = currentPawn.getY() - 1; i >= stopDecrementY; i--) {
					if(!this.checkXY(currentPawn.getX(), i, actions, currentPawn)){
						break;
					}
				}
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
	private boolean checkXY(final int x, final int y, List<State> actions, Pawn currentPawn) {

		final boolean pawnInCitadel = citadels.stream().anyMatch(c -> c.isPawnInCitadel(currentPawn));
		boolean haveToAddThePawn = !this.getPawns().stream().anyMatch(p -> p.getY() == y && p.getX() == x); // Se non c'è già un altro pezzo

		if (currentPawn.isBlack() == false /*is white*/
				|| (currentPawn.isBlack() == true && !pawnInCitadel) /*il pezzo è nero e non in una citadel*/ ) {
			haveToAddThePawn = haveToAddThePawn && !citadels.stream().anyMatch(c -> c.isXYInCitadel(x, y));
		}

		if (currentPawn.isBlack() == true /*is black*/
				&& pawnInCitadel) { // pezzo nero e dentro una citadel, allora può muoversi solo dentro la sua citadel, non può andare in un'altra citadel
			haveToAddThePawn = haveToAddThePawn && !citadels.stream().filter(c -> !c.isPawnInCitadel(currentPawn)).anyMatch(c -> c.isXYInCitadel(x, y));
		}

		haveToAddThePawn = haveToAddThePawn && (x != tronePosition.x || y != tronePosition.y);

		if (haveToAddThePawn) {
			List<Pawn> newPawns = new LinkedList<>(this.getPawns());
			newPawns.remove(currentPawn);
			Pawn newPawn = new Pawn(currentPawn.isBlack(), x, y, currentPawn.king);
			final boolean haveEaten = checkPawnsEaten(x, y, currentPawn, newPawns);
			newPawns.add(newPawn);
			HistoryStorage newHistoryStorage = (haveEaten ? new HistoryStorage() : this.historyStorage.clone()); // if eaten -> new storage
			boolean drawCase = false;
			try {
				newHistoryStorage.add(newPawns);
			}catch(IllegalArgumentException e) {
				drawCase = true;
			}
			State newState = new State(newPawns, !this.turn, newHistoryStorage, this, drawCase);
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
		// Get all the "near" pawns (near means +1 or -1 in vertical or horizontal) 
		List<Pawn> pawnOfInterest = newPawns.stream().filter(p -> p.isBlack() != movedPawn.isBlack() && (Math.abs(p.getX() - toX) + Math.abs(p.getY()- toY) == 1)).collect(Collectors.toList());
		int deltaX, deltaY;
		boolean haveToEat = false;
		
		for (Pawn pawn : pawnOfInterest) {
			deltaX = (pawn.getX() - toX)*2;
			deltaY = (pawn.getY() - toY)*2;
			final int partnerPositionX = deltaX + toX;
			final int partnerPositionY = deltaY + toY;
			haveToEat = newPawns.stream().anyMatch(p -> p.isBlack() == movedPawn.isBlack() && (p.getX() == partnerPositionX && p.getY() == partnerPositionY)) ||
					(tronePosition.x == partnerPositionX && tronePosition.y == partnerPositionY) ||
					(citadels.stream().anyMatch(c -> c.isXYInFringeCitadels(partnerPositionX, partnerPositionY)));

			if (haveToEat && pawn.king && protectedKingPositions.contains(pawn.position)) { // Special case for king
				final int partnerPositionX_2 = pawn.getX() + (deltaY > 0 ? 1 : 0);
				final int partnerPositionY_2 = pawn.getY() + (deltaX > 0 ? 1 : 0);
				haveToEat = newPawns.stream().anyMatch(p -> p.isBlack() == movedPawn.isBlack() && (p.getX() == partnerPositionX_2 && p.getY() == partnerPositionY_2)) ||
						(tronePosition.x == partnerPositionX_2 && tronePosition.y == partnerPositionY_2);
				final int partnerPositionX_3 = pawn.getX() + (deltaY > 0 ? -1 : 0);
				final int partnerPositionY_3 = pawn.getY() + (deltaX > 0 ? -1 : 0);
				haveToEat = newPawns.stream().anyMatch(p -> p.isBlack() == movedPawn.isBlack() && (p.getX() == partnerPositionX_3 && p.getY() == partnerPositionY_3)) ||
						(tronePosition.x == partnerPositionX_3 && tronePosition.y == partnerPositionY_3);
			}

			if (haveToEat) {
				newPawns.remove(pawn);
				haveEaten = true;
			}
		}
		return haveEaten;
	}

	public List<State> unfold(){
		List<State> result = new LinkedList<State>();
		State tmp = this;
		while(tmp.parent != null){
			result.add(tmp);
			tmp = tmp.parent;
		}
		//result.add(tmp);
		return result;
	}
	
	/**
	 * Returns a value that stimate the "goodness" of the pawns in the board.
	 * @return A value that stimate the "goodness" of the pawns in the board.
	 */
	public double getHeuristic() {
		// TODO da fare
		double result = new Random().nextDouble();
		if (new Random().nextInt() % 100000 == 1324)
			return 1000 * (this.turn ? 1 : -1);
		return Math.floor(result * 6 * (new Random().nextBoolean() ? 1 : -1));
	}

	/**
	 * Checks if the board is in a terminal position.
	 * @return If the board is in a terminal position.
	 */
	public boolean isTerminal() {
		if (this.drawCase)
			return true;
		if (!this.getPawns().stream().anyMatch(p -> p.isBlack() == true /*is black*/)) // No more black pawns
			return true;
		Optional<Pawn> king = this.getPawns().stream().filter(p -> p.king).findAny();
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
			if (drawCase){
				return 0; // Draw
			}
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
				Optional<Pawn> pawn = this.getPawns().stream().filter(p -> p.getX() == x && p.getY() == y).findAny();
				if(pawn.isPresent()) {
					result.append((pawn.get().isBlack() ? 'B' : (pawn.get().king) ? 'K' : 'W'));
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

	/**
	 * List of citadels
	 */
	private static final List<Citadel> citadels = new ArrayList<>(4); 
	/**
	 * King trone position
	 */
	private static final Position tronePosition = Position.of(5, 5);
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
		citadelPositions.add(Position.of(5, 1));
		citadelPositions.add(Position.of(4, 1));
		citadelPositions.add(Position.of(6, 1));
		citadelPositions.add(Position.of(5, 2));
		citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		citadelPositions.add(Position.of(1, 5));
		citadelPositions.add(Position.of(1, 4));
		citadelPositions.add(Position.of(1, 6));
		citadelPositions.add(Position.of(2, 5));
		citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		citadelPositions.add(Position.of(9, 5));
		citadelPositions.add(Position.of(9, 4));
		citadelPositions.add(Position.of(9, 6));
		citadelPositions.add(Position.of(8, 5));
		citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		citadelPositions.add(Position.of(5, 9));
		citadelPositions.add(Position.of(4, 9));
		citadelPositions.add(Position.of(6, 9));
		citadelPositions.add(Position.of(5, 8));
		citadels.add(new Citadel(citadelPositions));

		/***** ESCAPE POSITIONS *****/

		escapePositions.add(Position.of(1, 2));
		escapePositions.add(Position.of(1, 3));
		escapePositions.add(Position.of(1, 7));
		escapePositions.add(Position.of(1, 8));

		escapePositions.add(Position.of(2, 1));
		escapePositions.add(Position.of(3, 1));
		escapePositions.add(Position.of(7, 1));
		escapePositions.add(Position.of(8, 1));

		escapePositions.add(Position.of(9, 2));
		escapePositions.add(Position.of(9, 3));
		escapePositions.add(Position.of(9, 7));
		escapePositions.add(Position.of(9, 8));

		escapePositions.add(Position.of(2, 9));
		escapePositions.add(Position.of(3, 9));
		escapePositions.add(Position.of(7, 9));
		escapePositions.add(Position.of(8, 9));

		/***** PROTECTED KING POSITIONS *****/
		protectedKingPositions.add(tronePosition);
		protectedKingPositions.add(Position.of(5, 6));
		protectedKingPositions.add(Position.of(5, 4));
		protectedKingPositions.add(Position.of(4, 5));
		protectedKingPositions.add(Position.of(6, 5));
	}
}
