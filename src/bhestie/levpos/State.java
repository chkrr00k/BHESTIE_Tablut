package bhestie.levpos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import bhestie.levpos.utils.HistoryStorage;
import bhestie.zizcom.Action;

public class State {

	public static int MULTIPLICATOR = 1;

	private static final int END_PREPARATION_PHASE = 7;
	private static final int END_MAIN_PHASE = 12;
	private static final int END_ATTACK_PHASE = 20;

	public static int TURN = 0;
	
	private static final ThreadPool threadPool = ThreadPool.getInstance();
	
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
	
	@Deprecated
	public List<State> getActions(){
		throw new UnsupportedOperationException("This function was moved use getChildren() instead");
	}
	/**
	 * Returns a generator that generates all possible combination of future states of the current state
	 * @return an iterator to generate children
	 */
	public StateGenerator getChildGenerator() {//FIXEME was parallel
		return new StateGenerator(this);
	}
	/**
	 * Returns an Iterable object that generates all possible children of the current status
	 * @return an Iterable object
	 */
	public StateChild getChildren(){
		return new StateChild(this);
	}

	public int whitePawnSurroundingKing(){
		Pawn k = this.getKing();
		
		if (k == null) {
			return 0;
		}

		return (int) this.pawns.stream().filter(p -> p.isWhite() && !p.king && Math.abs(p.getX() - k.getX()) + Math.abs(p.getY() - k.getY()) == 1).count();
		
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
	
	private long getHeuristic(int level) {
		long result;
		if (this.heuristicCache == null) {
			
			if (this.isTerminal()) {
				if (drawCase){
					this.utilityCache = 0L;
					return 0;
				}
				result = this.getUtilityValue();
				if (!this.getPawns().stream().anyMatch(p -> p.king)) { // Black wins
					if (!Minimax.player) { // White player
						result = -result;
					}
				} else { // Is terminal and black not win -> White wins
					if (Minimax.player) { // Black player
						result = -result;
					}
				}
			} else { // Not terminal -> heuristic
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
			}
			this.heuristicCache = result;
		} else {
			result = this.heuristicCache;
		}
		
		if (result == Long.MAX_VALUE) {
			return result;
		}
		
		// The following code set the percentage in order to set more weight the more the State is near the init State (the State I have to find the best move)
		double percentage = 1d / (level + 1);
		final int size = this.unfold().size(); // Numbers of parents
		if (size > 1) { // If the parent is not the initial state calculate the heuristic recursivly
			result *= percentage;
			result += this.parent.getHeuristic(level + 1);
		} else { // If I don't have parents -> I'm the last one
			result *= Math.min(percentage, 1);
		}
		
		return Math.min(result, Minimax.MAXVALUE);
	}
	
	private long getPointsForOctagonInCardinalPoint(Position p1Internal, Position p2Internal, Position fromInternal, Position toInternal, Position excludeForInternal, Position p1External, Position p2External, Position fromExternal, Position toExternal, Position excludeForExternal, long maxResult, boolean preferInternal) {
		long currentResult = 0;
		long numberOfWhitesOutOfExternalOctagon = this.checkROIQuantity(fromExternal.x, fromExternal.y, toExternal.x, toExternal.y, p -> p.isWhite() && !p.position.equals(excludeForExternal));
		long numberOfWhitesOutOfInternalOctagon = this.checkROIQuantity(fromInternal.x, fromInternal.y, toInternal.x, toInternal.y, p -> p.isWhite() && !p.position.equals(excludeForInternal));
		long numberOfBlackPawnsInExternalOctagon = this.pawns.stream().filter(p -> p.isBlack() && (p.position.equals(p1External) || p.position.equals(p2External))).count();
		long numberOfBlackPawnsInInternalOctagon = numberOfWhitesOutOfInternalOctagon == 0 ? this.pawns.stream().filter(p -> p.isBlack() && (p.position.equals(p1Internal) || p.position.equals(p2Internal))).count() : 0;
		
		if (numberOfWhitesOutOfExternalOctagon >= 2) {
			maxResult = 0;
		} else if (numberOfWhitesOutOfExternalOctagon == 1) {
			maxResult = 90 * maxResult / 200;
		}
		
		if (numberOfBlackPawnsInInternalOctagon == 2) { 				// Se ho fatto l'ottagono interno
			currentResult = maxResult - 50;
		} else if (numberOfBlackPawnsInExternalOctagon == 2) { 			// Altrimenti se ho fatto l'ottagono esterno
			currentResult = ((preferInternal && numberOfWhitesOutOfInternalOctagon == 0) ? maxResult/2 : maxResult);
		} else { 														// Non ho fatto nè quello interno nè quello esterno
			currentResult = Math.max(numberOfBlackPawnsInInternalOctagon, numberOfBlackPawnsInExternalOctagon) * maxResult / 2;
			if (numberOfBlackPawnsInInternalOctagon > numberOfBlackPawnsInExternalOctagon)
				currentResult -= 50;
		}
		return currentResult;
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that stimates the "goodness" of the board 
	 */
	private long getHeuristicBlack() {
		// Avoid to let the enemy win
		int kingEscape = this.kingEscape();
		if (kingEscape > 0) {
			return -this.getUtilityValue();
		}
		
		// Negative if black has 3 or less pawns and white has still 2 pawns
		if (this.pawns.stream().filter(p -> p.isBlack()).count() <= 3 && this.pawns.stream().filter(p -> p.isWhite()).count() >= 2) {
			return -this.getUtilityValue(); // try to draw
		}
		
		final int octagonPoints;
		final int eatingPoints;
		final int notBeEatenPoints;
		final int whiteKingGoodPositionPoints;
		final int remainInCitadelsPoints;
		final int kingAssaultPoints;
		
		if (State.TURN <= END_PREPARATION_PHASE) {
			octagonPoints = 400;
			eatingPoints = 350;
			notBeEatenPoints = 150;
			whiteKingGoodPositionPoints = 0;
			remainInCitadelsPoints = 125;
			kingAssaultPoints = -25;
		} else if (State.TURN <= END_MAIN_PHASE) {
			octagonPoints = 250;
			eatingPoints = 350;
			notBeEatenPoints = 176;
			whiteKingGoodPositionPoints = 50;
			remainInCitadelsPoints = 174;
			kingAssaultPoints = 0;
		} else if (State.TURN <= END_ATTACK_PHASE) {
			octagonPoints = 350;
			eatingPoints = 275;
			notBeEatenPoints = 175;
			whiteKingGoodPositionPoints = 100;
			remainInCitadelsPoints = 10;
			kingAssaultPoints = 100;
		} else { // Desperation phase
			octagonPoints = 400;
			eatingPoints = 150;
			notBeEatenPoints = 200;
			whiteKingGoodPositionPoints = 100;
			remainInCitadelsPoints = 0;
			kingAssaultPoints = 150;
		}
		
		long result = 0;
		
		long malusSouth = this.checkROI(1, 1, 9, 4, p -> p.king) || (this.pawns.stream().filter(p -> p.isWhite() && !p.king && p.position.x == 5 && (p.position.y == 3 || p.position.y == 4)).count() < 2) ? 25 : 0;
		long malusNorth = this.checkROI(1, 6, 9, 9, p -> p.king) || (this.pawns.stream().filter(p -> p.isWhite() && !p.king && p.position.x == 5 && (p.position.y == 6 || p.position.y == 7)).count() < 2) ? 25 : 0;
		long malusWest =  this.checkROI(6, 1, 9, 9, p -> p.king) || (this.pawns.stream().filter(p -> p.isWhite() && !p.king && p.position.y == 5 && (p.position.x == 6 || p.position.x == 7)).count() < 2) ? 25 : 0;
		long malusEast =  this.checkROI(1, 1, 4, 9, p -> p.king) || (this.pawns.stream().filter(p -> p.isWhite() && !p.king && p.position.y == 5 && (p.position.x == 4 || p.position.x == 4)).count() < 2) ? 25 : 0;
		
		
		
		// Check for North-east octagon
		long maxResultNorthEast = (200 - malusNorth - malusEast) * octagonPoints / 800;
		long resultNorthEast = this.getPointsForOctagonInCardinalPoint(Position.of(6, 3), Position.of(7, 4), Position.of(6, 1), Position.of(9, 4), Position.of(6, 4), Position.of(7, 2), Position.of(8, 3), Position.of(7, 1), Position.of(9, 3), Position.of(7, 3), maxResultNorthEast, malusNorth > 0 || malusEast > 0);
		result += resultNorthEast;
		
		// Check for North-west octagon
		long maxResultNorthWest = (200 - malusNorth - malusWest) * octagonPoints / 800;
		long resultNorthWest = this.getPointsForOctagonInCardinalPoint(Position.of(3, 4), Position.of(4, 3), Position.of(1, 1), Position.of(4, 4), Position.of(4, 4), Position.of(2, 3), Position.of(3, 2), Position.of(1, 1), Position.of(3, 3), Position.of(3, 3), maxResultNorthWest, malusNorth > 0 || malusWest > 0);
		result += resultNorthWest;
		
		// Check for South-east octagon
		long maxResultSouthEast = (200 - malusSouth - malusEast) * octagonPoints / 800;
		long resultSouthEast = this.getPointsForOctagonInCardinalPoint(Position.of(6, 7), Position.of(7, 6), Position.of(6, 6), Position.of(9, 9), Position.of(6, 6), Position.of(7, 8), Position.of(8, 7), Position.of(7, 7), Position.of(9, 9), Position.of(7, 7), maxResultSouthEast, malusSouth > 0 || malusEast > 0);
		result += resultSouthEast;
		
		// Check for South-west octagon
		long maxResultSouthWest = (200 - malusSouth - malusWest) * octagonPoints / 800;
		long resultSouthWest = this.getPointsForOctagonInCardinalPoint(Position.of(3, 6), Position.of(4, 7), Position.of(1, 6), Position.of(4, 9), Position.of(4, 6), Position.of(2, 7), Position.of(3, 8), Position.of(1, 7), Position.of(3, 9), Position.of(3, 7), maxResultSouthWest, malusSouth > 0 || malusWest > 0);
		result += resultSouthWest;
		
		// Check for number of black pawns (not be eaten)
		{
			long numberOfBlackPawns = this.pawns.stream().filter(p -> p.isBlack()).count();
			final double currentResultForBlackPawns;
			
			if (numberOfBlackPawns >= 14) {
				currentResultForBlackPawns = 2+5+9 + (numberOfBlackPawns - 2) * 2 / 2;
			} else if (numberOfBlackPawns >= 8) {
				currentResultForBlackPawns = 2+5 + (numberOfBlackPawns - 6) * 9 / 8;
			} else if (numberOfBlackPawns >= 4) {
				currentResultForBlackPawns = 2 + (numberOfBlackPawns - 4) * 5 / 4;
			} else {
				currentResultForBlackPawns = (numberOfBlackPawns - 4) * 2 / 4;
			}
			
			result += currentResultForBlackPawns * notBeEatenPoints / 18;
		}
		
		// Check for number of white pawns (the less are the more it increases) (eat)
		{
			long numberOfWhitePawns = this.pawns.stream().filter(p -> p.isWhite() && !p.king).count();
			final double currentResultForWhitePawns;
			// ln( (x+1) / 9 ) / ( ln(9) * (-1) )
			double tmp = ((double)(numberOfWhitePawns + 1)) / 10;
			currentResultForWhitePawns = (long) (Math.log(tmp) * -eatingPoints / Math.log(10));
			result += currentResultForWhitePawns;
		}
		
		// White king in good position
		if (!this.checkROI(2, 2, 8, 8, holedROIPredicateFactory(3, 3, 7, 7).and(p -> p.king))) {
			result += whiteKingGoodPositionPoints;
		}
		
		// Remain in citadel
		{
			final int minPawnsInCitadel = (State.TURN < END_PREPARATION_PHASE ? 2 : 1);
			long points = 0;
			for (int i = 0; i < 4; i++) {
				final int j = i;
				if (this.pawns.stream().filter(p -> citadels.get(j).isPawnInCitadel(p)).count() >= minPawnsInCitadel) {
					points += 24;
				}
			}
			if (points == 96) {
				points += 4;
			}
			result += points * remainInCitadelsPoints / 100;
		}
		
		// King assault
		{
			long points = 0;
			long currentThreatenPositions = this.threatenKingRemaining().stream().flatMap(l -> l.stream()).count();
			if (currentThreatenPositions == 3) {
				points = 100;
			} else if (currentThreatenPositions == 2) {
				points = 50;
			} else if (currentThreatenPositions == 1) {
				points = 25;
			}
			
			// King center of gravity dependency
			int xBar = 0;
			int yBar = 0;
			final Pawn king = this.getKing();
			for (Pawn p : this.pawns.stream().filter(p -> p.isBlack()).collect(Collectors.toList())) {
				xBar += p.getX() - king.getX();
				yBar += p.getY() - king.getY();
			}
			
			if (xBar == 0 && yBar == 0) {
				points += 100;
			} else {
				points += 100 /  Math.sqrt(xBar * xBar + yBar * yBar);
			}
			
			result += points * kingAssaultPoints;
		}
		
		//result = 10; // XXX disabled
		return result * MULTIPLICATOR;
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that stimates the "goodness" of the board 
	 */
	private long getHeuristicWhite() {
		// Negative if black has 3 or more pawns and white has only the king
		if (this.pawns.stream().filter(p -> p.isBlack()).count() >= 3 && this.pawns.stream().filter(p -> p.isWhite()).count() <= 1) {
			return -Minimax.MAXVALUE; // I'm gonna lose -> try to draw!
		}
		
		final int eatingPoints;
		final int dontBeEatenPoints;
		final int kingUnderCheckPoints;
		final int kingInGoodPositionPoints;
		final int kingEscapesPoints;
		final int whiteOnMainAxisPoints;
		final int rawDistanceFromEscapePoints;
		final int kingProtectedPoints;
		
		if (State.TURN <= END_PREPARATION_PHASE) {
			eatingPoints = 150;
			dontBeEatenPoints = 225;
			kingUnderCheckPoints = 150;
			kingInGoodPositionPoints = 50;
			kingEscapesPoints = 100;
			whiteOnMainAxisPoints = 100;
			rawDistanceFromEscapePoints = -10; //XXX negative
			kingProtectedPoints = 235;
		} else if (State.TURN <= END_MAIN_PHASE) {
			eatingPoints = 175;
			dontBeEatenPoints = 225;
			kingUnderCheckPoints = 175;
			kingInGoodPositionPoints = 100;
			kingEscapesPoints = 100;
			whiteOnMainAxisPoints = -50;
			rawDistanceFromEscapePoints = 25;
			kingProtectedPoints = 200;
		} else if (State.TURN <= END_ATTACK_PHASE) {
			eatingPoints = 75;
			dontBeEatenPoints = 165;
			kingUnderCheckPoints = 190;
			kingInGoodPositionPoints = 150;
			kingEscapesPoints = 140;
			whiteOnMainAxisPoints = -25;
			rawDistanceFromEscapePoints = 75;
			kingProtectedPoints = 240;
		} else { //DESPERATION PHASE
			eatingPoints = 50;
			dontBeEatenPoints = 200;
			kingUnderCheckPoints = 225;
			kingInGoodPositionPoints = 75;
			kingEscapesPoints = 200;
			whiteOnMainAxisPoints = 0;
			rawDistanceFromEscapePoints = 0;
			kingProtectedPoints = 250;
		}
		
		long result = 0;
		
		// Check for number of black pawns (eat)
		{
			long blackPawnsNumber = pawns.stream().filter(pawn -> pawn.isBlack()).count();
			final long currentEatingPoints;
			if (blackPawnsNumber > 12) {
				currentEatingPoints = 14+5 + (blackPawnsNumber - 12) * 8 / 4;
			} else if (blackPawnsNumber > 7) {
				currentEatingPoints = 14 + (blackPawnsNumber - 7) * 5 / 5;
			} else {
				currentEatingPoints = (blackPawnsNumber) * 14 / 7;
			}
			result += currentEatingPoints * eatingPoints / 25;
		}
		
		// Check for number of white pawns (not being eaten)
		{
			long numberOfWhitePawns = this.pawns.stream().filter(p -> p.isWhite() && !p.king).count();
			final long currentDontBeEaten;
			if (numberOfWhitePawns > 7) {
				currentDontBeEaten = 2+2+8+4 + (numberOfWhitePawns - 7) * 6 / 1;
			} else if (numberOfWhitePawns > 2) {
				currentDontBeEaten = 2+2 + (numberOfWhitePawns - 2) * 8 / 5;
			} else {
				currentDontBeEaten = numberOfWhitePawns * 2 / 2;
			}
			result += currentDontBeEaten * dontBeEatenPoints / 22;
		}
		
		// King under check
		{
			result += this.remainingPositionForSurroundingKing() * kingUnderCheckPoints / 4;
		}
		
		// Protected King
		{
			// Maybe count white pawns "linger" the king, then multiply it with the points
			//if (checkROI(3, 3, 7, 7, holedROIPredicateFactory(3, 3, 7, 7).and(p -> p.king)))
			int whitePawnsSuroundingKing = this.whitePawnSurroundingKing();
			if (whitePawnsSuroundingKing < 4) {
				result += whitePawnsSuroundingKing * kingProtectedPoints / 4;
			} else {
				result += 3 * kingProtectedPoints / 4;
			}
		}
		
		// Raw distance from escape
		{
			result += (6 - this.rawDistanceFromEscape()) * rawDistanceFromEscapePoints / 6;
		}
		
		// White on main axis
		{
			result += (8 - this.mainAxisDefaultPosition()) * whiteOnMainAxisPoints / 8;
		}
		
		// Go in a spot with some escapes
		{
			final int kingEscapes = this.kingEscape();
			
			if(kingEscapes >= 2) {
				return this.getUtilityValue();
			}
			result += kingEscapes * kingEscapesPoints / 4;
		}
		
		// Go in a "good position"
		{
			if(this.checkROI(3, 3, 7, 7, holedROIPredicateFactory(3, 3, 7, 7).and(p -> p.king))){
				result += kingInGoodPositionPoints;
			}
		}
	
	
		//result = 10; // XXX disabled
		return result * MULTIPLICATOR;
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
		if (!king.isPresent()) { // No king -> black wins
			this.isTerminalCache = true;
			return true;
		} else if (kingEscaped(king.get())) { // King runs away -> white wins
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
			result = this.getUtilityValue();
			if (!this.getPawns().stream().anyMatch(p -> p.king)) { // Black wins
				if (!Minimax.player) { // White player
					result = -result;
				}
			} else { // Is terminal and black not win -> White wins
				if (Minimax.player) { // Black player
					result = -result;
				}
			}
		}
		
		this.utilityCache = result;
		return result;
	}
	
	/**
	 * The biggest value the more "good" is the board
	 * @return A number that says if the board is a winning or losing board
	 */
	private long getUtilityValue() {
		if (this.unfold().size() == 1) { // If the move is the first or the second (hence the first for the player) is the best
			return Long.MAX_VALUE;
		} else {
			return Minimax.MAXVALUE; // It is a very good move, but not optimal because not sure to win!
		}
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

	
	public class StateGenerator implements Iterator<State>{
		private final State s;
		
		private State next;
		private boolean nextPresent;
		
		private boolean symmetricalNorthSouth;
		private boolean symmetricalEastWest;
		private boolean symmetricalDiagonal;
		private boolean symmetricalAntiDiagonal;
		
		private final List<Pawn> pawnsToScan;
		
		private int stopDecrementX, stopDecrementY;
		private final boolean checkAll;

		private final Iterator<Pawn> pawnIter;
		private Pawn currentPawn;
		private int ei, wi, si, ni;
		private boolean estop, wstop, sstop, nstop;
			
 		public StateGenerator(State s) {
			super();
			this.s = s;
			this.next = null;
			this.nextPresent = false;
			
			this.symmetricalNorthSouth = true;
			this.symmetricalEastWest = true;
			this.symmetricalDiagonal = true;
			this.symmetricalAntiDiagonal = !this.s.turn; // Only for white
			
			this.pawnsToScan = this.filterPawns();
			this.stopDecrementX = 0;
			this.stopDecrementY = 0;
			
			this.checkAll = !(this.symmetricalEastWest && this.symmetricalNorthSouth && this.symmetricalDiagonal && this.symmetricalAntiDiagonal);
			this.pawnIter = this.pawnsToScan.iterator();
			
			this.currentPawn = null;
			this.ei = 0;
			this.si = 0;
			this.ni = 0;
			this.wi = 0;
			
			this.estop = false;
			this.wstop = false;
			this.sstop = false;
			this.nstop = false;
			
			this.refreshCurrentPawn();
			
		}

		private List<Pawn> filterPawns(){
			// Check the symmetries
			for (Pawn pawn : this.s.getPawns()) {
				if (this.symmetricalNorthSouth && !this.s.getPawns().stream().anyMatch(p -> p.getX()==pawn.getX() && p.getY() +pawn.getY()==10 && p.isBlack()==pawn.isBlack() && p.king==pawn.king)) {
					this.symmetricalNorthSouth = false;
				}
				if (this.symmetricalEastWest && !this.s.getPawns().stream().anyMatch(p -> p.getX()+pawn.getX()==10 && p.getY()==pawn.getY() && p.isBlack()==pawn.isBlack() && p.king==pawn.king)) {
					this.symmetricalEastWest = false;
				}
				if (this.symmetricalDiagonal && !this.s.getPawns().stream().anyMatch(p -> p.getX()==pawn.getY() && p.getY()==pawn.getX() && p.isBlack()==pawn.isBlack() && p.king==pawn.king)) {
					this.symmetricalDiagonal = false;
				}
				if(this.symmetricalAntiDiagonal && !this.s.getPawns().stream().anyMatch(p -> p.getX() + pawn.getY() == 10 && p.getY() + pawn.getX() == 10 && p.isBlack()==pawn.isBlack()  && p.king==pawn.king)) {
					this.symmetricalAntiDiagonal = false;
				}
				if(!(this.symmetricalDiagonal || this.symmetricalEastWest || this.symmetricalNorthSouth || this.symmetricalAntiDiagonal)){
					break;
				}
			}

			Stream<Pawn> pawnToScanStream = this.s.getPawns().stream().filter(p -> p.filterByTurn(s.turn));

			if (this.symmetricalEastWest){
				pawnToScanStream = pawnToScanStream.filter(p -> p.getX() >= 5); // Takes only the high part, X from 5 to 9 (from E to I)
			}
			if (this.symmetricalNorthSouth){
				pawnToScanStream = pawnToScanStream.filter(p -> p.getY() >= 5); // Takes only the high part, Y from 5 to 9
			}
			if (this.symmetricalDiagonal){
				pawnToScanStream = pawnToScanStream.filter(p -> p.getY() >= p.getX()); // Takes only the lower triangle part
			}
			if (this.symmetricalAntiDiagonal){
				pawnToScanStream = pawnToScanStream.filter(p -> p.getX() + p.getY() >= 10); // Takes only the lower triangle part (built in the anti-diagonal way)
			}

			return pawnToScanStream.collect(Collectors.toList());
		}
		
		private void refreshCurrentPawn(){
			if(this.pawnIter.hasNext()){
				this.currentPawn = this.pawnIter.next();
				
				this.estop = false;
				this.wstop = false;
				this.sstop = false;
				this.nstop = false;
				
				this.ei = this.currentPawn.getX() + 1;
				this.si = this.currentPawn.getX() - 1;
				this.stopDecrementX = (this.symmetricalEastWest && this.currentPawn.getX() == 5 ? 5 : 1);
				if(this.checkAll){
					this.ni = this.currentPawn.getY() + 1;
					this.wi = this.currentPawn.getY() - 1;
					this.stopDecrementY = (this.symmetricalNorthSouth && currentPawn.getY() == 5 ? 5 : 1);
				}
				this.nextPresent = true;
			}else{
				this.nextPresent = false;
			}
		}
		/**
		 * Get the list of the next possible States.
		 * @return The list of the next possible States.
		 */
		private boolean generateNext(){
			OptionalState tmp = OptionalState.empty();
			boolean found = false;
			if(this.nextPresent){
				while(!found){
					if(!this.estop && this.ei <= 9) {
						if (this.nextPresent = (tmp = this.processPosition(this.ei++, this.currentPawn.getY(), this.currentPawn)).isPresentValid()){
							this.next = tmp.get();
							found = true;
						}else if(!tmp.isPresent()){
							this.estop = true;
						}
					}else if(!this.sstop && this.si >= this.stopDecrementX){
						if (this.nextPresent = (tmp = this.processPosition(this.si--, this.currentPawn.getY(), this.currentPawn)).isPresentValid()){
							this.next = tmp.get();
							found = true;
						}else if(!tmp.isPresent()){
							this.sstop = true;
						}
					}else if(!this.nstop && this.checkAll && this.ni <= 9){
						if(this.nextPresent = (tmp = this.processPosition(this.currentPawn.getX(), this.ni++, this.currentPawn)).isPresentValid()){
							this.next = tmp.get();
							found = true;
						}else if(!tmp.isPresent()){
							this.nstop = true;
						}
					}else if(!this.wstop && this.checkAll && this.wi >= this.stopDecrementY){
						if(this.nextPresent = (tmp = this.processPosition(this.currentPawn.getX(), this.wi--, this.currentPawn)).isPresentValid()){
							this.next = tmp.get();
							found = true;
						}else if(!tmp.isPresent()){
							this.wstop = true;
						}
					}else{
						if(this.pawnIter.hasNext()){
							this.refreshCurrentPawn();
						}else{
							break; // this break is evil
						}
					}
				}
				if(!found){
					this.nextPresent = false;
				}
			}
			return this.nextPresent;
		}

		/**
		 * Checks if a Pawn can move to X and Y. In this case inserts a new action in the list
		 * @param x The nex X position
		 * @param y The new Y position
		 * @param actions The list of next possible states
		 * @param currentPawn The pawn that is moving
		 * @return If it added a new action of not
		 */
		private OptionalState processPosition(final int x, final int y, Pawn currentPawn) {
			OptionalState result = OptionalState.empty();
			final boolean pawnInCitadel = citadels.stream().anyMatch(c -> c.isPawnInCitadel(currentPawn));
			boolean haveToAddThePawn = !s.getPawns().stream().anyMatch(p -> p.getY() == y && p.getX() == x); // Se non c'è già un altro pezzo

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
				List<Pawn> newPawns = new ArrayList<>(s.getPawns());
				newPawns.remove(currentPawn);
				Pawn newPawn = new Pawn(currentPawn.isBlack(), x, y, currentPawn.king);
				final boolean haveEaten = checkPawnsEaten(x, y, currentPawn, newPawns);
				newPawns.add(newPawn);
				HistoryStorage newHistoryStorage = (haveEaten ? new HistoryStorage() : s.historyStorage.clone()); // if eaten -> new storage
				boolean drawCase = false;
				try {
					newHistoryStorage.add(newPawns);
				}catch(IllegalArgumentException e) {
					drawCase = true;
				}
				State newState = new State(newPawns, !s.turn, newHistoryStorage, s, drawCase);
				boolean haveToAddTheNewState = true;
				if (!s.turn) { // White turn
					// Check if is going to suicide
					haveToAddTheNewState = newState.isTerminal() || !newState.veryUglyKingPosition();
				}
				if (haveToAddTheNewState) {
					result = OptionalState.of(newState);
				}else{
					result = OptionalState.invalid(newState);
				}
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
		
		@Override
		public boolean hasNext() {
			return this.generateNext();
		}

		@Override
		public State next() {
			if(this.nextPresent){
				return this.next;
			}else{
				throw new IllegalStateException("You need to call hasNext() first");
			}
		}
		
		protected State getState() {
			return this.s;
		}
		
	}
	public class StateChild implements Iterable<State>{
		private StateGenerator sg;
		
		public StateChild(State s) {
			super();
			this.sg = s.getChildGenerator();
		}

		public Stream<State> stream() {
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.sg, Spliterator.ORDERED), false);
		}

		@Override
		public Iterator<State> iterator() {
			return this.sg;
		}
	}
	
	private static class OptionalState {
		private State s;
		private boolean isValid;
		private boolean isPresent;
		
		private OptionalState(State s, boolean isValid, boolean isPresent) {
			if(s == null){
				this.s = null;
				this.isValid = false;
				this.isPresent = false;
			}else{
				this.s = s;
				this.isValid = isValid;
				this.isPresent = isPresent;
			}
		}
		public State get() {
			if(this.isPresent){
				return this.s;
			}else{
				throw new NullPointerException();
			}
		}
		public boolean isValid() {
			return this.isValid;
		}
		public boolean isPresent() {
			return this.isPresent;
		}
		public boolean isPresentValid(){
			return this.isPresent() && this.isValid();
		}

		public static OptionalState of(State s){
			return new OptionalState(s, true, true);
		}
		public static OptionalState empty(){
			return new OptionalState(null, false, false);
		}
		public static OptionalState invalid(State s){
			return new OptionalState(s, false, true);
		}
	}
	
	public class ParallelStateGenerator extends State.StateGenerator {
		private static final int QUEUESIZE = 3;
		
		private boolean finishedToGenerate = false;
		private ConcurrentLinkedQueue<State> generatedQueue = new ConcurrentLinkedQueue<>();
		
		public ParallelStateGenerator(State s) {
			super(s);
			
			// Chiedo al pool di thread di calcolare i QUEUESIZE next values
			for (int i = 0; i < QUEUESIZE; i++) {
				threadPool.add(this);
			}
		}
		
		private Optional<State> getNext() {
			synchronized (this) {
				if (super.hasNext())
					return Optional.of(super.next());
				else
					return Optional.empty();
			}
		}
		
		public void generateAndCache() {
			if (this.finishedToGenerate) // Finished yet!
				return;
			
			Optional<State> next = this.getNext();
			if (next.isPresent()) { // Found a new state, save it!
				this.generatedQueue.add(next.get());
				State n = next.get();
				if (n.isTerminal())
					n.getUtility();
				else 
					n.getHeuristic();
				n.threatenKingRemaining();
			} else { // not got the next -> I have finished to generating
				finishedToGenerate = true;
			}
		}
		
		public void generate() {
			if (this.finishedToGenerate) // Finished yet!
				return;
			
			Optional<State> next = this.getNext();
			if (next.isPresent()) { // Found a new state, save it!
				this.generatedQueue.add(next.get());
			} else { // not got the next -> I have finished to generating
				finishedToGenerate = true;
			}
		}
		
		private Optional<State> next = Optional.empty();
		@Override
		public boolean hasNext() {
			if (this.generatedQueue.isEmpty()) { // Nothing cached, calculate!
				this.generate();
			}
			if (this.finishedToGenerate && this.generatedQueue.isEmpty()) { // Finished to generate and nothing in queue
				return false;
			} else {
				this.next = Optional.of(this.generatedQueue.poll());
				if (!this.finishedToGenerate)
					threadPool.add(this);
			}
			return true;
		}
		
		@Override
		public State next() {
			if (!this.next.isPresent())
				throw new IllegalStateException("You must call hasNext() before!");
			State next = this.next.get();
			this.next = Optional.empty();
			return next;
		}
		
	}

}
