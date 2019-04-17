package bhestie.levpos;

import java.util.ArrayList;

public class Citadel {
	public final ArrayList<Position> citadelPositions;
	/**
	 * Citadel constructor.
	 * Important: the first element of the ArrayList MUST be the internal citadel
	 * @param ArrayList of citadel positions
	 */
	public Citadel(ArrayList<Position> positions) {
		this.citadelPositions = positions;
	}
	/**
	 * Checks if a Pawn is inside this citadel
	 * @param pawn
	 * @return if a Pawn is inside this citadel
	 */
	public boolean isPawnInCitadel(Pawn pawn) {
		return (this.citadelPositions.indexOf(pawn.position) >= 0);
	}
	/**
	 * Checks if a Position is inside this citadel
	 * @param position
	 * @return if a Position is inside this citadel
	 */
	public boolean isPositionInCitadel(Position position) {
		return (this.citadelPositions.indexOf(position) >= 0);
	}
	/**
	 * Checks if a position (identified by X and Y) is inside a citadel
	 * @param x
	 * @param y
	 * @return if a position (identified by X and Y) is inside a citadel 
	 */
	public boolean isXYInCitadel(int x, int y) {
		return this.isPositionInCitadel(new Position(x, y));
	}
	/**
	 * Checks if a position (identified by X and Y) is inside a citadel (excluding the internal position)
	 * @param x
	 * @param y
	 * @return if a position (identified by X and Y) is inside a citadel (excluding the internal position)
	 */
	public boolean isXYInFringeCitadels(int x, int y) {
		int position = (this.citadelPositions.indexOf(new Position(x, y)));
		return (position > 0);
	}
}
