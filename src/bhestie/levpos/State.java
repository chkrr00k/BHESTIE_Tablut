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

public class State {
	private static final int MULTIPLICATOR = 10;
	
	private static final int REMAINING_POSITION_FOR_CAPTURE_KING_VALUE_FOR_WHITE_HEURISTIC = 85 * MULTIPLICATOR;

	//TODO is positive to be eaten for white? change paremeter if it is...
	private static final int WHITE_PAWNS_VALUE_FOR_WHITE_HEURISTIC = 30  * MULTIPLICATOR;
	//if a state has less black pawns, it will have a more positive value because the malus
	//BLACK_PAWNS_VALUE_FOR_WHITE_HEURISTIC will be subtracted less times
	private static final int BLACK_PAWNS_VALUE_FOR_WHITE_HEURISTIC = 40 * MULTIPLICATOR;

	//raw distance from nearest escape, the more it is, the more malus we get
	private static final int DISTANCE_FROM_ESCAPE_VALUE_FOR_WHITE_HEURISTIC = 15 * MULTIPLICATOR;

	//having white pawn on main axis (default position) is a malus
	private static final int WHITE_PAWNS_ON_MAIN_AXIS = -10 * MULTIPLICATOR;
	private static final int WHITE_KING_ESCAPES = 50 * MULTIPLICATOR;
	private static final int WHITE_KING_MORE_ESCAPES_THEN_PARENT = 10 * MULTIPLICATOR;
	private static final long WHITE_KING_IN_GOOD_POSITION = 5 * MULTIPLICATOR;

	private static final long BLACK_PAWNS_GOES_OUT_OF_CITADEL = 35 * MULTIPLICATOR;

	
	public static int TURN = 0;
	
	private Long heuristicCache = null; 	// Cached heuristic
	private Boolean isTerminalCache = null;	// Cache isTerminal
	private Long utilityCache = null;		// Cache utility
	
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
	 * Cache of king pawn
	 */
	private Pawn king;
	
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
	 * Gets the king Pawn, null if none
	 * @return The king pawn
	 */
	private Pawn getKing() {
		if (this.king == null) {
			for (Pawn p : this.pawns) {
				if (p.king) {
					this.king = p;
					break; // Performance reason
				}
			}
		}
		return this.king;
	}
	
	/**
	 * Get the list of the next possible States.
	 * @return The list of the next possible States.
	 */
	public List<State> getActions(){
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

		if (currentPawn.isWhite()
				|| (currentPawn.isBlack() && !pawnInCitadel) /*il pezzo è nero e non in una citadel*/ ) {
			haveToAddThePawn = haveToAddThePawn && !citadels.stream().anyMatch(c -> c.isXYInCitadel(x, y));
		}

		if (currentPawn.isBlack()
				&& pawnInCitadel) { // pezzo nero e dentro una citadel, allora può muoversi solo dentro la sua citadel, non può andare in un'altra citadel
			haveToAddThePawn = haveToAddThePawn && !citadels.stream().filter(c -> !c.isPawnInCitadel(currentPawn)).anyMatch(c -> c.isXYInCitadel(x, y));
		}

		haveToAddThePawn = haveToAddThePawn && (x != tronePosition.x || y != tronePosition.y);

		if (haveToAddThePawn) {
			List<Pawn> newPawns = new ArrayList<>(this.getPawns());
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
			boolean haveToAddTheNewState = true;
			if (!this.turn) { // White turn
				// Check if is going to suicide
				haveToAddTheNewState = newState.isTerminal() || !newState.veryUglyKingPosition();
			}
			if (haveToAddTheNewState) {
				actions.add(newState);
				HeuristicCalculatorGroup.statesToCalculateCache.add(newState);
				HeuristicCalculatorGroup.semaphoreStatesToBeCalculated.release();
			}
			return true;
		}
		return false;
	}
	
	public List<List<Position>> threatenKingRemaining(){
		Pawn k = this.getKing();
		final boolean kingInProtected = protectedKingPositions.contains(k.position); // XXX remove for optimization
		
		Position[] tp = new Position[]{
			Position.of(k.getX() + 1, k.getY()), //e
			Position.of(k.getX(), k.getY() + 1), //s
			Position.of(k.getX() - 1, k.getY()), //w
			Position.of(k.getX(), k.getY() - 1)  //n
		};
		
		// here are all the pawns surrounding the king
		List<Position> l = Stream.concat(this.pawns.stream()
					.filter(p -> p.isBlack())
					.map(p -> p.position),
				citadels.stream()
					.flatMap(c -> c.citadelPositions.stream())
				).distinct()
				.filter(p -> p.equalsAny(tp))
				.limit(4)
				.collect(Collectors.toList());
		
		if(tronePosition.equalsAny(tp)){
			l.add(tronePosition);
		}

		List<List<Position>> result = new LinkedList<>();
		List<Position> tmp = null;
		for (int i = 0; i < tp.length; i++) {
			if (kingInProtected) {
				if (!l.contains(tp[i])) {
					if (tmp == null) {
						tmp = new LinkedList<>();
					}
					tmp.add(tp[i]);
				}
			} else {
				if (l.contains(tp[i]) && !l.contains(tp[(i+2) % 4])) {
					tmp = new ArrayList<>(1);
					tmp.add(tp[(i+2) % 4]);
					result.add(tmp);
					tmp = null;
				}
			}
		}
		if (kingInProtected) {
			if (tmp != null) {
				result.add(tmp);
			}
		} else if (result.size() == 0) {
			tmp = new ArrayList<Position>(2);
			tmp.add(tp[0]);
			tmp.add(tp[2]);
			result.add(tmp);
			tmp = new ArrayList<Position>(2);
			tmp.add(tp[1]);
			tmp.add(tp[3]);
			result.add(tmp);
		}
		
		return result;
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
	 * Checks if the king is threaten by other black pawns.
	 * in particular calculates the remaining pawns that has to be put near the king for make it caught
	 *rules: if the king is in the central position, 4 pawn have to be near him.
	 * if the king is near the central position: 3
	 * other positions = 2
	 */
	public int remainingPositionForSurroundingKing(){
		final Pawn king = this.getKing();
		if(king == null){
			return 0;
		}else {
			List<Position> threatenPositions = new ArrayList<Position>(4);
			threatenPositions.add(Position.of(king.getX() + 1, king.getY()));
			threatenPositions.add(Position.of(king.getX(), king.getY() + 1));
			threatenPositions.add(Position.of(king.getX() - 1, king.getY()));
			threatenPositions.add(Position.of(king.getX(), king.getY() - 1));

			final Stream<Position> s = Stream.concat(this.pawns.stream().filter(p -> p.isBlack()).map(p -> p.position),
					citadels.stream().flatMap(c -> c.citadelPositions.stream()));
			final int count = (int) s.distinct().filter(p -> p.equalsAny(threatenPositions)).count();
			
			return 4 - count - (tronePosition.equalsAny(threatenPositions) ? 1 : 0);
		}
	}

	/**
	this evaluation should make the heuristic going to an escape way instead staying in a static situation
	it calculates the distance between the king and the nearest escape
	it must be modified to take care of the rest of the table situation
	(black's pawn have closed the escape way)
	 */
	private int rawDistanceFromEscape(){
		final Pawn king = this.getKing();
		if(king == null)
			return 0;
		else {
			final int x = king.getX();
			final int y = king.getY();

			List<Position> escapes = escapePositions.stream().filter(e -> !this.pawns.stream().anyMatch(p -> p.position.equals(e))).collect(Collectors.toList());
			int distanceRecord = 6;//maximum distance
			for(Position position : escapes) {
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
		return (int) pawns.stream().filter(pawn -> pawn.isWhite() && pawn.position.equalsAny(defaultWhitePawnsPosition)).count();
	}

	/**
	 * Returns a value that stimate the "goodness" of the pawns in the board.
	 * @return A value that stimate the "goodness" of the pawns in the board.
	 */
	public long getHeuristic() {
		return this.getHeuristic(1);
	}
	
	private long getHeuristic(double minPercentage) {
		long result;
		if (this.heuristicCache == null) {
			if (!this.turn) { // Black turn
				result = this.getHeuristicBlack();
				if (!Minimax.player){
					result = -result;
				}
			} else { // White turn
				result = this.getHeuristicWhite();
				if (Minimax.player){
					result = -result;
				}
			}
			this.heuristicCache = result;
		} else {
			result = this.heuristicCache;
		}
		
		// The following code set the percentage in order to set more weight the more the State is near the init State (the State I have to find the best move)
		double percentage = 1;
		final int size = this.unfold().size(); // Numbers of parents
		for (int i = 0; i < size; i++) { // (1/2) ^ size
			percentage /= 2;
		}
		if (percentage < minPercentage)
			minPercentage = percentage;
		if (size > 1) { // If the parent is not the initial state calculate the heuristic recursivly
			result *= percentage;
			result += this.parent.getHeuristic(minPercentage);
		} else { // If I don't have parents -> I'm the last one (with percentage 0.5) and I add the minPercentage to my percentage value
			percentage += minPercentage; // If is the last one (and the percentage should be 0.5) add the minPercentage
			result *= Math.min(percentage, 1);
		}
		
		return Math.min(result, Minimax.MAXVALUE - 1);
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that stimates the "goodness" of the board 
	 */
	private long getHeuristicBlack() {
		if (this.isTerminal()){
			return this.getUtility();
		}
		long result = 800;
		
		int numRouteBlocked = this.routeBlocked();
		
		if (numRouteBlocked >= 6) { // new heuristic. Do eat! Do not be eaten! Do stay in octagon
			long eaten = this.pawns.stream().filter(p -> p.isWhite()).count() - this.parent.pawns.stream().filter(p -> p.isWhite()).count(); 
			if (eaten > 0) {
				result += WHITE_PAWNS_VALUE_FOR_WHITE_HEURISTIC * eaten;
			}
			eaten =  this.pawns.stream().filter(p -> p.isBlack()).count() - this.parent.pawns.stream().filter(p -> p.isBlack()).count();
			if (eaten > 0) {
				result -= BLACK_PAWNS_VALUE_FOR_WHITE_HEURISTIC * eaten;
			}
			if (numRouteBlocked == 8) {
				result += 100000;
			}
			result -= 50 * MULTIPLICATOR * this.checkROIQuantity(1, 1, 9, 9, holedROIPredicateFactory(1, 1, 9, 9).and(p -> p.isBlack()));
		} 
		if (numRouteBlocked < 8){ // Do create the octagon
			//Number of blocked goal tiles
			result += (numRouteBlocked * WHITE_KING_ESCAPES);
			if(numRouteBlocked < this.parent.routeBlocked()){
				result -= WHITE_KING_ESCAPES;
			}
		}
		
		int kingEscape = this.kingEscape();
		
		if(this.checkROI(4, 4, 6, 6, p -> p.king)){
			result += 20; // result = 7000;
		}else if(kingEscape == 0) {
			result += WHITE_KING_ESCAPES * 4;
		}else{
			result -= WHITE_KING_ESCAPES * kingEscape;
		}
		
		if(this.checkROI(2, 2, 8, 8, holedROIPredicateFactory(3, 3, 7, 7).and(p -> p.king))){
			result -= WHITE_KING_IN_GOOD_POSITION;
		}
		
		if(State.TURN < 7) { // preparation phase
			//number of black in the corners
			long blackInCorners = this.checkROIQuantity(7, 7, 9, 9, p -> p.isBlack()) 
					+ this.checkROIQuantity(1, 7, 3, 9, p -> p.isBlack())
					+ this.checkROIQuantity(1, 1, 3, 3, p -> p.isBlack())
					+ this.checkROIQuantity(7, 1, 9, 3, p -> p.isBlack());
			//number of white in the corners
			long whiteInCorners = this.checkROIQuantity(7, 7, 9, 9, p -> p.isWhite()) 
					+ this.checkROIQuantity(1, 7, 3, 9, p -> p.isWhite())
					+ this.checkROIQuantity(1, 1, 3, 3, p -> p.isWhite())
					+ this.checkROIQuantity(7, 1, 9, 3, p -> p.isWhite());
			result += (blackInCorners * 2 - whiteInCorners) * 4; // it's positive black in corners and negative for blacks
			// here is nice having black too
			result += 10 * MULTIPLICATOR * this.checkROIQuantity(1, 1, 9, 9, holedROIPredicateFactory(1, 1, 9, 9).and(p -> p.isBlack()));
			// NOT nice if they are white
			result -= 20 * MULTIPLICATOR * this.checkROIQuantity(1, 1, 9, 9, holedROIPredicateFactory(1, 1, 9, 9).and(p -> p.isWhite()));;
			// to avoid cycling
			result -= 10.5 * MULTIPLICATOR * this.checkROIQuantity(1, 1, 1, 1, p -> (p.position.x == 1 || p.position.x == 9) 
						&& (p.position.y == 1 || p.position.y == 9) 
						&& p.isBlack());
		}
		
		if (State.TURN <= 16) {
			final int minPawnsInCitadel = (State.TURN < 8 ? 2 : 1);
			long pawnsInCitadel = 0;
			long pawnsInParentCitadel = 0;
			for (int i = 0; i < 4; i++) {
				pawnsInCitadel = this.pawns.stream().filter(p -> citadels.get(0).isPawnInCitadel(p)).count();
				if (pawnsInCitadel <= minPawnsInCitadel) {
					pawnsInParentCitadel = this.parent.pawns.stream().filter(p -> citadels.get(0).isPawnInCitadel(p)).count();
					if (pawnsInCitadel < pawnsInParentCitadel) {
						result -= BLACK_PAWNS_GOES_OUT_OF_CITADEL * 4;
					}
				}
			}
		}
		
		result += WHITE_KING_MORE_ESCAPES_THEN_PARENT * 2 * (kingEscape - this.parent.kingEscape());
		
		if (kingEscape != this.parent.kingEscape()) {
			result = Minimax.MAXVALUE * 2 * (this.parent.kingEscape() - kingEscape);
		}
		
		return result;
	}
	
	public boolean veryUglyKingPosition() {
		List<List<Position>> positionWhereKingCanBeEaten = this.threatenKingRemaining();
		for (List<Position> list : positionWhereKingCanBeEaten) {
			if (list.size() == 1) {
				final Position toCheck = list.get(0);
				for (int i = toCheck.x + 1; i <= 9; i++) {
					final Position tmp = Position.of(i, toCheck.y);
					if (this.pawns.stream().anyMatch(p -> p.position.equals(tmp) && p.isBlack())) {
						return true;
					} else if (this.pawns.stream().anyMatch(p -> p.position.equals(tmp) && p.isWhite())) {
						break;
					}
				}
				for (int i = toCheck.x - 1; i >= 1; i--) {
					final Position tmp = Position.of(i, toCheck.y);
					if (this.pawns.stream().anyMatch(p -> p.position.equals(tmp) && p.isBlack())) {
						return true;
					} else if (this.pawns.stream().anyMatch(p -> p.position.equals(tmp) && p.isWhite())) {
						break;
					}
				}
				for (int i = toCheck.y + 1; i <= 9; i++) {
					final Position tmp = Position.of(toCheck.x, i);
					if (this.pawns.stream().anyMatch(p -> p.position.equals(tmp) && p.isBlack())) {
						return true;
					} else if (this.pawns.stream().anyMatch(p -> p.position.equals(tmp) && p.isWhite())) {
						break;
					}
				}
				for (int i = toCheck.y - 1; i >= 1; i--) {
					final Position tmp = Position.of(toCheck.x, i);
					if (this.pawns.stream().anyMatch(p -> p.position.equals(tmp) && p.isBlack())) {
						return true;
					} else if (this.pawns.stream().anyMatch(p -> p.position.equals(tmp) && p.isWhite())) {
						break;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that stimates the "goodness" of the board 
	 */
	private long getHeuristicWhite() {
		long result = 370;
		// TODO calculate remaining position for caputure king. ora se il re è circondato da 2 parti potrebbe capitare che venga mangiato da 2 parti, quindi la remaining poisition è 1, non 2 (anche se è circondato da 2 posizioni)
		int remainingPositionForSurroundingKing = this.remainingPositionForSurroundingKing();
		result += remainingPositionForSurroundingKing * REMAINING_POSITION_FOR_CAPTURE_KING_VALUE_FOR_WHITE_HEURISTIC;

		if (State.TURN > 2 && State.TURN <= 5) {
			result += this.rawDistanceFromEscape() * DISTANCE_FROM_ESCAPE_VALUE_FOR_WHITE_HEURISTIC;
		}

		if (State.TURN < 3) {
			result += this.mainAxisDefaultPosition() * WHITE_PAWNS_ON_MAIN_AXIS;
		} else if (State.TURN == 3) {
			result += this.mainAxisDefaultPosition() * WHITE_PAWNS_ON_MAIN_AXIS / 2;
		}
		
		if (State.TURN > 2) {
			int kingEscapes = this.kingEscape();
			int parentKingEscapes = this.parent.kingEscape();
			result += kingEscapes * WHITE_KING_ESCAPES;
			if (kingEscapes > parentKingEscapes) { // more escapes then parent
				result += (kingEscapes - parentKingEscapes) * WHITE_KING_MORE_ESCAPES_THEN_PARENT;
			}
			
			if(this.checkROI(3, 3, 7, 7, holedROIPredicateFactory(3, 3, 7, 7).and(p -> p.king))){
				result += WHITE_KING_IN_GOOD_POSITION;
			}
			
		}

		result += pawns.stream().filter(pawn -> pawn.isWhite()).count() * WHITE_PAWNS_VALUE_FOR_WHITE_HEURISTIC;

		result += (16 - pawns.stream().filter(pawn -> pawn.isBlack()).count()) * BLACK_PAWNS_VALUE_FOR_WHITE_HEURISTIC;

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

	private boolean kingEscaped(Pawn king) {
		return (escapePositions.contains(king.position)); // Se il re è nella posizione di una via di fuga
	}

	/**
	 * Returns a value that stimate the "goodness" of a terminal state of the game.
	 * @return A value that stimate the "goodness" of a terminal state of the game.
	 */
	public long getUtility() {
		if (this.utilityCache != null)
			return this.utilityCache;
		
		long result = 0;
		if (this.isTerminal()) {
			if (drawCase){
				this.utilityCache = 0L;
				return 0;
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
			result = this.getHeuristic(); // In case you ask getUtility and it's not a terminalState -> returns the getHeuristic value
		}
		
		this.utilityCache = result;
		return result;
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that says if the board is a winning or losing board
	 */
	private long getUtilityBlack() {
		// TODO da scrivere. Viene chiamata quando la scacchiera è vincente per il nero.
		// Valore alto = la mossa è migliore per il nero
		
		return Minimax.MAXVALUE - (this.unfold().size() - 1) * 5000;
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that says if the board is a winning or losing board
	 */
	private long getUtilityWhite() {
		// TODO da scrivere. Viene chiamata quando la scacchiera è vincente per il bianco.
		// Valore alto = la mossa è migliore per il bianco
	
		return this.getUtilityBlack();
		//return Long.MAX_VALUE - (this.unfold().size() - 1) * 500;
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
					result.append(' ');
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
	 * @return the action between this State and his parent
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
		if(k.getY() == 9 || k.getY() == 1){
			return 1; // king alreay escaped;
		}else if(k.getY() <= 6 && k.getY() >=4){
			return 0; // king can't escape
		}else if(k.getY() == 7 || k.getY() == 3){ // king could escape in two place;
			int result = 0;
			if(!this.checkROI(1, k.getY(), k.getX(), k.getY(), b -> !b.king)){ // check if any pawns on the left
				result++;
			}
			if(!this.checkROI(k.getX(), k.getY(), 9, k.getY(), b -> !b.king)){ //check if any pawns on the right
				result++;
			}
			return result;
		}else if(k.getY() == 2 || k.getY() == 8){ // king could escape in one place;
			int result = 0;
			if(k.getX() <= 4){
				// check left side
				if(!this.checkROI(1, k.getY(), k.getX(), k.getY(), b -> !b.king)){ // check if any pawns on the left
					result++;
				}
			}else if(k.getX() >= 6){
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
		if(k.getX() == 9 || k.getX() == 1){
			return 1; // king alreay escaped;
		}else if(k.getX() <= 6 && k.getX() >=4){
			return 0; // king can't escape
		}else if(k.getX() == 7 || k.getX() == 3){ // king could escape in two place;
			int result = 0;
			if(!this.checkROI(k.getX(), 1, k.getX(), k.getY(), b -> !b.king)){ // check if any pawns on the bottom
				result++;
			}
			if(!this.checkROI(k.getX(), k.getY(), k.getX(), 9, b -> !b.king)){ //check if any pawns on the top
				result++;
			}
			return result;
		}else if(k.getX() == 2 || k.getX() == 8){ // king could escape in one place;
			int result = 0;
			if(k.getY() <= 4){
				// check left side
				if(!this.checkROI(k.getX(), 1, k.getX(), k.getY(), b -> !b.king)){ // check if any pawns on the bottom
					result++;
				}
			}else if(k.getY() >= 6){
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
		Pawn king = this.getKing();
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
					&& pr.test(p); // predicate to custom check
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
	
	/*private boolean kingProtrudingSouth(){
		Pawn king = this.pawns.stream().filter(p -> p.king).findFirst().get();
		return (king.getY() > 5 || this.checkROIQuantity(6, 5, 7, 5, p -> p.isWhite()) < 2); // king is in north sector;
	}
	private boolean kingProtrudingNorth(){
		Pawn king = this.pawns.stream().filter(p -> p.king).findFirst().get();
		return (king.getY() < 5 || this.checkROIQuantity(3, 5, 4, 5, p -> p.isWhite()) < 2); // king is in south sector;

	}
	private boolean kingProtrudingEast(){
		Pawn king = this.pawns.stream().filter(p -> p.king).findFirst().get();
		return (king.getX() > 5 || this.checkROIQuantity(5, 6, 5, 7, p -> p.isWhite()) < 2); // king is in east sector;
	}
	private boolean kingProtrudingWest(){
		Pawn king = this.pawns.stream().filter(p -> p.king).findFirst().get();
		return (king.getX() < 5 || this.checkROIQuantity(5, 3, 5, 4, p -> p.isWhite()) < 2); // king is in west sector;
	}*/
	
	public int routeBlocked(){
		int result = 0;
		int modificator = 1; // express if the blocked route is in the right side of the chessboard
		//boolean north = this.kingProtrudingNorth(), east = this.kingProtrudingEast(), south = this.kingProtrudingSouth(), west = this.kingProtrudingWest();
		for(Position pos : escapeRouteBlocked){
			if(this.pawns.stream().anyMatch(p -> p.isBlack() && p.position.equals(pos))){
				result++;
				
				//this looks like it's written by an idiot, but don't worry: it is.
				//if the move has been made in the (eg) NW position of the chessboard 
				//we want to block on that side because it's the smart thing to do
				//but only in the first steps
				/*if(State.TURN < 6) {
					if(north && pos.y < 5){// game is protruding north
						modificator+=0.05;
					}
					if(south && pos.y > 5){// game is protruding south
						modificator+=0.05;
					}
					if(east && pos.x > 5){// game is protruding east
						modificator+=0.05;
					}
					if(west && pos.x < 5){// game is protruding west
						modificator+=0.05;
					}
				}*/
			}
		}
		return result * modificator;
	}
	
	/**
	 * default white pawns positions
	 */
	private static final List<Position> defaultWhitePawnsPosition = new ArrayList<Position>(8);
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
	private static final List<Position> protectedKingPositions = new ArrayList<>(5);
	private static final List<Position> escapeRouteBlocked = new ArrayList<Position>(8);
	static {
		ArrayList<Position> citadelPositions = new ArrayList<>(4);
		// North
		citadelPositions.add(Position.of(5, 1));
		citadelPositions.add(Position.of(4, 1));
		citadelPositions.add(Position.of(6, 1));
		citadelPositions.add(Position.of(5, 2));
		citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		// West
		citadelPositions.add(Position.of(1, 5));
		citadelPositions.add(Position.of(1, 4));
		citadelPositions.add(Position.of(1, 6));
		citadelPositions.add(Position.of(2, 5));
		citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		// East
		citadelPositions.add(Position.of(9, 5));
		citadelPositions.add(Position.of(9, 4));
		citadelPositions.add(Position.of(9, 6));
		citadelPositions.add(Position.of(8, 5));
		citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		// South
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
