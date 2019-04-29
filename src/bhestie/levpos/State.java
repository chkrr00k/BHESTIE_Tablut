package bhestie.levpos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import bhestie.levpos.utils.HistoryStorage;
import bhestie.zizcom.Action;

@SuppressWarnings("unused") // TODO remove
public class State {
	private static final int REMAINING_POSITION_FOR_CAPTURE_KING_VALUE_FOR_BLACK_HEURISTIC = -50;
	private static final int REMAINING_POSITION_FOR_CAPTURE_KING_VALUE_FOR_WHITE_HEURISTIC = 50;

	//TODO is positive to be eaten for white? change paremeter if it is...
	public static final int WHITE_PAWNS_VALUE_FOR_WHITE_HEURISTIC = -100;
	//if a state has less black pawns, it will have a more positive value because the malus
	//BLACK_PAWNS_VALUE_FOR_WHITE_HEURISTIC will be subtracted less times
	public static final int BLACK_PAWNS_VALUE_FOR_WHITE_HEURISTIC = -400;
	public static final int WHITE_PAWNS_VALUE_FOR_BLACK_HEURISTIC = -100;
	public static final int BLACK_PAWNS_VALUE_FOR_BLACK_HEURISTIC = 200;

	//raw distance from nearest escape, the more it is, the more malus we get
	private static final int DISTANCE_FROM_ESCAPE_VALUE_FOR_WHITE_HEURISTIC = -50;
	private static final int DISTANCE_FROM_ESCAPE_VALUE_FOR_BLACK_HEURISTIC = 50;

	//having white pawn on main axis (default position) is a malus
	private static final int WHITE_PAWNS_ON_MAIN_AXIS = -50;
	private static final int EATEN_PAWN_VALUE_FOR_WHITE_HEURISTIC = 200;
	private static final int EATEN_PAWN_VALUE_FOR_BLACK_HEURISTIC = 50;

	
	private Double heuristicCache = null; // Cached heuristic
	private Boolean isTerminalCache = null; // Cache isTerminal
	
	private final State parent;
	private final boolean drawCase;
	/**
	 * History storage. Storage of all mosses done.
	 */
	public final HistoryStorage historyStorage;
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
		this.pawns = new ArrayList<Pawn>(pawns);
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
			HeuristicCalculatorGroup.statesToCalculateCache.add(newState);
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
				final int partnerPositionX_2 = pawn.getX() + (deltaY != 0 ? 1 : 0);
				final int partnerPositionY_2 = pawn.getY() + (deltaX != 0 ? 1 : 0);
				haveToEat = newPawns.stream().anyMatch(p -> p.isBlack() == movedPawn.isBlack() && (p.getX() == partnerPositionX_2 && p.getY() == partnerPositionY_2)) ||
						(tronePosition.x == partnerPositionX_2 && tronePosition.y == partnerPositionY_2);
				final int partnerPositionX_3 = pawn.getX() + (deltaY != 0 ? -1 : 0);
				final int partnerPositionY_3 = pawn.getY() + (deltaX != 0 ? -1 : 0);
				haveToEat = haveToEat && newPawns.stream().anyMatch(p -> p.isBlack() == movedPawn.isBlack() && (p.getX() == partnerPositionX_3 && p.getY() == partnerPositionY_3)) ||
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
	 * It generates a list with all the parents State of the current State
	 * @return The list of unfolded State
	 */
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
	 * Checks if the king is threaten (minacciato) from other black pawns.
	 * in particular calculates the remaining pawns that has to be put near the king for make it caught
	 *rules: if the king is in the central position, 4 pawn have to be near him.
	 * if the king is near the central position: 3
	 * other positions = 2
	 */
	private int remainingPositionForCaptureKing(){
		Optional<Pawn> kingS = this.pawns.stream().filter(p -> p.king).findAny();
		if(!kingS.isPresent())
			return 0;
		else {
			Pawn king = kingS.get();

			int startingValue;
			if(king.getX() == 5 && king.getY() == 5)
				startingValue = 3;
			else if(king.getX() == 4 && king.getY() == 5)
				startingValue = 3;
			else if(king.getX() == 5 && king.getY() == 4)
				startingValue = 3;
			else if(king.getX() == 6 && king.getY() == 5)
				startingValue = 3;
			else if(king.getX() == 5 && king.getY() == 6)
				startingValue = 3;
			else startingValue = 2;

			List<Position> threatenPositions = new ArrayList<Position>();
			threatenPositions.add(Position.of(king.getX() + 1, king.getY()));
			threatenPositions.add(Position.of(king.getX(), king.getY() + 1));
			threatenPositions.add(Position.of(king.getX() - 1, king.getY()));
			threatenPositions.add(Position.of(king.getX(), king.getY() - 1));

			Stream<Pawn> found =
					this.pawns.stream().filter(pawn -> pawn.isBlack())
							.filter(pawn -> pawn.position.equalsAny(threatenPositions));
			int count = (int) found.count();

			return startingValue - count;
		}
	}

	/*
	this evaluation should make the heuristinc going to an escape way instead staying in a static situation
	it calculates the distance between the king and the nearest escape
	it must be modified for taking care of the rest of the table situation
	(black's pawn have closed the escape way)
	 */
	private int rawDistanceFromEscape(){
		Optional<Pawn> kingS = this.pawns.stream().filter(p -> p.king).findAny();
		if(!kingS.isPresent())
			return 0;
		else {
			Pawn king = kingS.get();
			int x = king.getX();
			int y = king.getY();

			int distanceRecord = 6;//maximum distance
			for(Position position : escapePositions){
				int distance = Math.abs(position.x - x);
				distance += Math.abs(position.y - y);
				if(distance < distanceRecord)
					distanceRecord = distance;
			}
			return distanceRecord;
		}
	}

	/**
	*is positive to have pawn in escape positions for controlling that area
	 **/

	/**
	*as in chess heuristic you have to move from default locations for getting table's control
	*we put a negative heuristic for leaving default position
	*this combined with king's threatenPositionHeuristic should make the algorithm move the pawn's that are
	*far away from king
	 **/
	private int mainAxisDefaultPosition(){
		long count = pawns.stream().filter(pawn -> pawn.isWhite()).filter(pawn -> pawn.position.equalsAny(defaultWhitePawnsPosition)).count();
		return (int) count;
	}

	/**
	 * Returns a value that stimate the "goodness" of the pawns in the board.
	 * @return A value that stimate the "goodness" of the pawns in the board.
	 */
	public double getHeuristic() {
		if (this.heuristicCache != null)
			return this.heuristicCache;
		double result = 0;
		if (!this.turn) { // Black turn
			result = this.getHeuristicBlack();
			if (!Minimax.player)
				result = -result;
		} else { // White turn
			result = this.getHeuristicWhite();
			if (Minimax.player)
				result = -result;
		}
		this.heuristicCache = result;
		return result;
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that stimates the "goodness" of the board 
	 */
	private double getHeuristicBlack() {
		List<State> unfolded = this.unfold();
		double result = 1;
		int size = unfolded.size();
		for (int i = 0; i < size; i++) {
			result += unfolded.get(i).parent.getHeuristic();
			result /= 2;
		}
		
		/*for (State state : unfolded) {
			result += state.calculateMoveGoodness();
			result /= 2;
		}*/
		return result + calculateMoveGoodness();
		// TODO da scrivere.
		// Più è alto il valore più la mossa è bella per il nero
		/*double result = -1;
		
		long numeroMangiati = 0;
		int numeroPassi = 0;*/


		/*
		State tmp = this.parent;
		while (tmp != null) {
			numeroMangiati = tmp.pawns.stream().filter(p -> p.isWhite()).count() - this.pawns.stream().filter(p -> p.isWhite()).count();
			result += numeroMangiati * EATEN_PAWN_VALUE_FOR_BLACK_HEURISTIC;
			//result -= tmp.getHeuristicWhite();
			//numeroMangiati = tmp.pawns.stream().filter(p -> p.isBlack()).count() - this.pawns.stream().filter(p -> p.isBlack()).count();
			//result -= numeroMangiati * EATEN_PAWN_HEURISTIC_VALUE;
			numeroPassi++;
			tmp = tmp.parent;
		}
		*/

		
		/*while (numeroPassi-- >= 0)
			result *= 0.5;

		result += remainingPositionForCaptureKing() * REMAINING_POSITION_FOR_CAPTURE_KING_VALUE_FOR_BLACK_HEURISTIC;

		result += pawns.stream().filter(pawn -> !pawn.isBlack()).count() * WHITE_PAWNS_VALUE_FOR_BLACK_HEURISTIC;

		result += pawns.stream().filter(pawn -> pawn.isBlack()).count() * BLACK_PAWNS_VALUE_FOR_BLACK_HEURISTIC;

		return result;*/
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that stimates the "goodness" of the board 
	 */
	private double getHeuristicWhite() {
		// TODO da scrivere.
		// Più è alto il valore più la mossa è bella per il bianco
		double result = 0;
		

		/*
		long numeroMangiati = 0;
		int numeroPassi = 0;
		State tmp = this.parent;
		while (tmp != null) {
			numeroMangiati = tmp.pawns.stream().filter(p -> p.isBlack()).count() - this.pawns.stream().filter(p -> p.isBlack()).count();
			result += numeroMangiati * EATEN_PAWN_VALUE_FOR_WHITE_HEURISTIC;
			//result -= tmp.getHeuristicBlack();
			numeroPassi++;
			tmp = tmp.parent;
		}
		while (numeroPassi-- >= 0)
			result *= 0.5;
		*/

		result += remainingPositionForCaptureKing() * REMAINING_POSITION_FOR_CAPTURE_KING_VALUE_FOR_WHITE_HEURISTIC;

		result += rawDistanceFromEscape() * DISTANCE_FROM_ESCAPE_VALUE_FOR_WHITE_HEURISTIC;

		result += mainAxisDefaultPosition() * WHITE_PAWNS_ON_MAIN_AXIS;

		result += pawns.stream().filter(pawn -> !pawn.isBlack()).count() * WHITE_PAWNS_VALUE_FOR_WHITE_HEURISTIC;

		result += pawns.stream().filter(pawn -> pawn.isBlack()).count() * BLACK_PAWNS_VALUE_FOR_WHITE_HEURISTIC;

		result = 1; // Disabled heuristic
		
		return result;
	}

	/**
	 * Checks if the board is in a terminal position.
	 * @return If the board is in a terminal position.
	 */
	public boolean isTerminal() {
		if (this.isTerminalCache != null)
			return this.isTerminalCache;
		
		if (this.drawCase) {
			this.isTerminalCache = true;
			return true;
		}
		if (!this.getPawns().stream().anyMatch(p -> p.isBlack())) { // No more black pawns -> white wins
			this.isTerminalCache = true;
			return true;
		}
		Optional<Pawn> king = this.getPawns().stream().filter(p -> p.king).findAny();
		// XXX comprimere gli if di sotto
		if (!king.isPresent()) { // No king -> black wins
			this.isTerminalCache = true;
			return true;
		} else if (kingEscaped(king.get())) { // Re runs away -> white wins
			this.isTerminalCache = true;
			return true;
		}
		this.isTerminalCache = false;
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
		double result = 0;
		if (this.isTerminal()) {
			if (drawCase){
				return 0; // Draw
			}
			if (!this.getPawns().stream().anyMatch(p -> p.king)) { // Black wins
				result = getUtilityBlack();
				if (!Minimax.player) { // White player
					result = -result;
				}
			} else { // Is terminal and black not win -> White wins
				result = getUtilityWhite();
				if (Minimax.player) { // Black player
					result = -result;
				}
			}
		} else {
			return this.getHeuristic(); // In case you ask getUtility and it's not a terminalState -> returns the getHeuristic value 
		}
		
		return result;
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that says if the board is a winning or losing board
	 */
	private double getUtilityBlack() {
		// TODO da scrivere. Viene chiamata quando la scacchiera è vincente per il nero.
		// Valore alto = la mossa è migliore per il nero
		
		return 10000000 - this.unfold().size() + 1;
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that says if the board is a winning or losing board
	 */
	private double getUtilityWhite() {
		// TODO da scrivere. Viene chiamata quando la scacchiera è vincente per il bianco.
		// Valore alto = la mossa è migliore per il bianco
		
		return 10000000 - this.unfold().size() + 1;
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
	 * @return the action between the old status and the new status just created.
	 */
	public Action getAction(){
		Pawn from = this.parent.pawns.stream().filter((p) -> {
			return p.filterByTurn(this.parent.turn) && !this.pawns.contains(p);
		}).findFirst().get();
		Pawn to = this.pawns.stream().filter((p) -> {
			return p.filterByTurn(this.parent.turn) && !this.parent.pawns.contains(p);
		}).findFirst().get();
		return new Action(from.getX(), from.getY(), to.getX(), to.getY(), this.parent.turn ? "B" : "W");
	}
	
	private int kingEscapeX(Pawn k){
		if(k.getX() == 9 || k.getX() == 1){
			return 1; // king alreay escaped;
		}else if(k.getX() <= 6 && k.getX() >=4){
			return 0; // king can't escape
		}else if(k.getX() == 7 || k.getX() == 3){ // king could escape in two place;
			int result = 0;
			if(!this.checkROI(k.getX(), k.getY(), 1, k.getY(), b -> !b.king)){ // check if any pawns on the left
				result++;
			}
			if(!this.checkROI(k.getX(), k.getY(), 9, k.getY(), b -> !b.king)){ //check if any pawns on the right
				result++;
			}
			return result;
		}else if(k.getX() == 2 || k.getX() == 8){ // king could escape in one place;
			int result = 0;
			if(k.getY() <= 4){
				// check left side
				if(!this.checkROI(k.getX(), k.getY(), 1, k.getY(), b -> !b.king)){ // check if any pawns on the left
					result++;
				}
			}else if(k.getY() >= 6){
				// check right side
				if(!this.checkROI(k.getX(), k.getY(), 9, k.getY(), b -> !b.king)){ //check if any pawns on the right
					result++;
				}
			}
			return result;
		}else{
			return 0;
		}
	}
	private int kingEscapeY(Pawn k){
		if(k.getY() == 9 || k.getY() == 1){
			return 1; // king alreay escaped;
		}else if(k.getY() <= 6 && k.getY() >=4){
			return 0; // king can't escape
		}else if(k.getY() == 7 || k.getY() == 3){ // king could escape in two place;
			int result = 0;
			if(!this.checkROI(k.getX(), k.getY(), k.getX(), 1, b -> !b.king)){ // check if any pawns on the bottom
				result++;
			}
			if(!this.checkROI(k.getX(), k.getY(), k.getX(), 9, b -> !b.king)){ //check if any pawns on the top
				result++;
			}
			return result;
		}else if(k.getY() == 2 || k.getY() == 8){ // king could escape in one place;
			int result = 0;
			if(k.getX() <= 4){
				// check left side
				if(!this.checkROI(k.getX(), k.getY(), k.getX(), 1, b -> !b.king)){ // check if any pawns on the bottom
					result++;
				}
			}else if(k.getX() >= 6){
				// check right side
				if(!this.checkROI(k.getX(), k.getY(), k.getX(), 9, b -> !b.king)){ //check if any pawns on the top
					result++;
				}
			}
			return result;
		}else{
			return 0;
		}
	}
	/**
	 * Check if the king pawn can reach any of the goal tiles and if so how many tiles can ot reach in the next move
	 * @return the number of possible goal tiles reached
	 */
	public int kingEscape(){
		Pawn king = null;
		for(Pawn p : this.pawns){
			if(p.king){
				king = p;
				break; // for faster performance
			}
		}
		if(king == null){
			return 0; // no king found (weird!)
		}else{
			return this.kingEscapeX(king) + this.kingEscapeY(king);
		}
	}
	
	/**
	 * 
	 * @param stx the top left starting point x
	 * @param sty the top left starting point y
	 * @param enx the bottom right ending point x
	 * @param eny the bottom right ending point y
	 * @param pr the condition the Pawn in the row must verify
	 * @return if there are any selected pawns there
	 */
	public boolean checkROI(int stx, int sty, int enx, int eny, Predicate<Pawn> pr){
		return this.checkROIQuantity(stx, sty, enx, eny, pr) > 0;
	}
	/**
	 * 
	 * @param stx the top left starting point x
	 * @param sty the top left starting point y
	 * @param enx the bottom right ending point x
	 * @param eny the bottom right ending point y
	 * @param pr the condition the Pawn in the row must verify
	 * @return the number of selected pawns there
	 */
	public long checkROIQuantity(int stx, int sty, int enx, int eny, Predicate<Pawn> pr){
		return this.pawns.stream().filter((p)->{
			return (p.getX() >= stx && p.getX() <= enx) // X check
					&& (p.getY() >= sty && p.getY() <= eny) // Y check
					&& pr.test(p); // predicate to costum check
		}).count();
	}
	/**
	 * Generates a predicate to check squared holed ROIs
	 * @param istx the top left starting point x of the hole
	 * @param isty the top left starting point y of the hole
	 * @param ienx the bottom right ending point x of the hole
	 * @param ieny the bottom right ending point y of the hole
	 * @return the new predicate just created for the occasion
	 */
	public static Predicate<Pawn> holedROIPredicateFactory(int istx, int isty, int ienx, int ieny){
		return (p) -> {
			return (p.getX() <= istx || p.getX() >= ienx)
					&& (p.getY() <= isty || p.getY() >= ieny);
		};
	}
	
	
	
	
	
	
	private boolean isRowBlocked(int fromX, int toX, int y) {
		boolean result = false;
		
		for (int i = fromX; i <= toX; i++) {
			int x = i;
			if (this.pawns.stream().anyMatch(p -> !p.king && p.position.equals(Position.of(x, y)))) {
				result = true;
			}
		}
		return result;
	}
	
	private boolean isColumnBlocked(int x, int fromY, int toY) {
		boolean result = false;
		
		for (int i = fromY; i <= toY; i++) {
			int y = i;
			if (this.pawns.stream().anyMatch(p -> !p.king && p.position.equals(Position.of(x, y)))) {
				result = true;
			}
		}
		return result;
	}
	
	
	private boolean isNthEscapeRouteBlocked(final int nth) {
		return (this.pawns.stream().anyMatch(p -> !p.king && p.isBlack() && p.position.equals(escapeRouteBlocked.get(nth))));
	}
	
	private boolean isKingEscapeBlocked(Pawn king) {
		// NW
		if (king.position.equals(Position.of(2, 2)))
			return (this.isColumnBlocked(2, 1, 1) && this.isRowBlocked(1, 1, 2));
		else if (king.position.equals(Position.of(2, 3)))
			return (this.isColumnBlocked(2, 1, 2)	&& this.isRowBlocked(1, 3, 3) && this.isRowBlocked(3, 9, 3)); 
		else if (king.position.equals(Position.of(2, 4)))
			return (this.isColumnBlocked(2, 1, 3));
		else if (king.position.equals(Position.of(3, 2)))
			return (this.isColumnBlocked(3, 1, 1) && this.isRowBlocked(1, 2, 2));
		else if (king.position.equals(Position.of(3, 3)))
			return (this.isColumnBlocked(3, 4, 9)) && this.isColumnBlocked(3, 1, 2) && this.isRowBlocked(1, 2, 3) && this.isRowBlocked(3, 9, 3);
		else if (king.position.equals(Position.of(3, 4)))
			return (this.isRowBlocked(1, 3, 3) && this.isRowBlocked(5, 9, 3));
		else if (king.position.equals(Position.of(3, 5)))
			return (this.isRowBlocked(1, 4, 3) && this.isRowBlocked(6, 9, 3));
		else if (king.position.equals(Position.of(4, 2)))
			return (this.isRowBlocked(1, 3, 2));
		else if (king.position.equals(Position.of(4, 3)))
			return (this.isRowBlocked(1, 3, 3) && this.isRowBlocked(5, 9, 3));
		else if (king.position.equals(Position.of(5, 3)))
			return (this.isRowBlocked(1, 4, 3) && this.isRowBlocked(6, 9, 3));
		
		// NE
		if (king.position.equals(Position.of(6, 2)))
			return (this.isRowBlocked(7, 9, 2));
		else if (king.position.equals(Position.of(6, 3)))
			return (this.isRowBlocked(7, 9, 3));
		else if (king.position.equals(Position.of(7, 2)))
			return (this.isRowBlocked(8, 9, 2) && this.isColumnBlocked(7, 1, 1) && this.isColumnBlocked(7, 3, 9));
		else if (king.position.equals(Position.of(7, 3))) 
			return (this.isRowBlocked(1, 6, 3) && this.isRowBlocked(8, 9, 3) && this.isColumnBlocked(7, 1, 2) && this.isColumnBlocked(7, 4, 9));
		else if (king.position.equals(Position.of(7, 4)))
			return (this.isRowBlocked(8, 9, 4)	&& this.isColumnBlocked(7, 1, 3) && this.isColumnBlocked(7, 5, 9));
		else if (king.position.equals(Position.of(7, 5)))
			return (this.isRowBlocked(8, 9, 5)	&& this.isColumnBlocked(7, 1, 4) && this.isColumnBlocked(7, 6, 9));
		else if (king.position.equals(Position.of(8, 2)))
			return (this.isRowBlocked(9, 9, 2) && this.isColumnBlocked(8, 1, 1));
		else if (king.position.equals(Position.of(8, 3)))
			return (this.isRowBlocked(9, 9, 3) && this.isColumnBlocked(8, 1, 2));
		else if (king.position.equals(Position.of(8, 4)))
			return (this.isColumnBlocked(8, 1, 3));
		
		// SW
		if (king.position.equals(Position.of(2, 6))) 
			return (this.isColumnBlocked(2, 7, 9));
		else if (king.position.equals(Position.of(2, 7))) 
			return (this.isColumnBlocked(2, 8, 9));
		else if (king.position.equals(Position.of(2, 8))) 
			return (this.isColumnBlocked(2, 9, 9));
		else if (king.position.equals(Position.of(3, 6)))
			return (this.isColumnBlocked(3, 1, 5) && this.isColumnBlocked(3, 7, 9));
		else if (king.position.equals(Position.of(3, 7))) 
			return (this.isColumnBlocked(3, 1, 6)	&& this.isColumnBlocked(3, 8, 9) && this.isRowBlocked(1, 2, 7) && this.isRowBlocked(4, 9, 7));
		else if (king.position.equals(Position.of(3, 8))) 
			return (this.isColumnBlocked(3, 1, 7) && this.isColumnBlocked(3, 9, 9) && this.isRowBlocked(1, 2, 8));
		else if (king.position.equals(Position.of(4, 7))) 
			return (this.isRowBlocked(1, 3, 7) && this.isRowBlocked(5, 9, 7));
		else if (king.position.equals(Position.of(4, 8))) 
			return (this.isRowBlocked(1, 3, 8));
		else if (king.position.equals(Position.of(5, 7)))
			return (this.isRowBlocked(1, 4, 7) && this.isRowBlocked(6, 9, 7));
		
		//SE
		if (king.position.equals(Position.of(6, 7)))
			return (this.isRowBlocked(1, 5, 7) && this.isRowBlocked(7, 9, 7));
		else if (king.position.equals(Position.of(6, 8)))
			return (this.isRowBlocked(7, 9, 8));
		else if (king.position.equals(Position.of(7, 6)))
			return (this.isColumnBlocked(7, 1, 5) && this.isColumnBlocked(7, 7, 9));
		else if (king.position.equals(Position.of(7, 7)))
			return (this.isColumnBlocked(7, 1, 6) && this.isColumnBlocked(7, 8, 9) && this.isRowBlocked(1, 6, 7) && this.isRowBlocked(8, 9, 7));
		else if (king.position.equals(Position.of(7, 8)))
			return (this.isColumnBlocked(7, 1, 6) && this.isColumnBlocked(7, 9, 9));
		else if (king.position.equals(Position.of(8, 6)))
			return (this.isColumnBlocked(8, 7, 9));
		else if (king.position.equals(Position.of(8, 7)))
			return (this.isColumnBlocked(8, 8, 9) && this.isRowBlocked(1, 7, 7) && this.isRowBlocked(9, 9, 7));
		else if (king.position.equals(Position.of(8, 8)))
			return (this.isColumnBlocked(8, 9, 9) && this.isRowBlocked(9, 9, 8));

		return true;
	}
	
	private double calculateMoveGoodness() {
		if (this.isTerminal())
			return this.getUtility();
		double result = 0;
		Pawn king = this.pawns.stream().filter(p -> p.king).findFirst().get();
		
		// TODO sistemare qui perch� se le vie sono bloccate d� 10K che va bene, ma se non sono bloccate devo prevedere un "malus"
		//King is outside the "citadel" (throne)
		if(isKingEscapeBlocked(king)) {
			result += 10000;
		} else result -= 10000;/*else if(!isKingEscapeBlocked(king.get()))
			return -10000;*/
		
		//Block Escape routes
		int numRouteBlocked = 0;
		for (int i = 0; i < 8; i++)
			if (isNthEscapeRouteBlocked(i))
				numRouteBlocked++;
		
		if (numRouteBlocked != 0)
			return numRouteBlocked*1000 + result;
		else
			return 1 + result; // Random move
		//}
		//else {
			//Do a "random" move
			//return 1;
		//}
	}

	
	
	
	
	/**
	 * default white pawns positions
	 */
	private static final List<Position> defaultWhitePawnsPosition = new ArrayList<Position>();
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
	private static final List<Position> escapeRouteBlocked = new ArrayList<Position>(8);
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

		/***** DEFAULT WHITE PAWNS POSITION *****/
		
		defaultWhitePawnsPosition.add(Position.of(5,3));
		defaultWhitePawnsPosition.add(Position.of(5,4));
		defaultWhitePawnsPosition.add(Position.of(5,6));
		defaultWhitePawnsPosition.add(Position.of(5,7));

		defaultWhitePawnsPosition.add(Position.of(3,5));
		defaultWhitePawnsPosition.add(Position.of(4,5));
		defaultWhitePawnsPosition.add(Position.of(6,5));
		defaultWhitePawnsPosition.add(Position.of(7,5));
		
		/***** ESCAPE ROUTE BLOCKED *****/
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
}
