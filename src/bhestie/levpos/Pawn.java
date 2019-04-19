package bhestie.levpos;

import java.util.ArrayList;

public class Pawn {
	/**
	 * TRUE=Black, FALSE=White
	 */
	private final boolean bw;
	/**
	 * The Pawn position
	 */
	private final Position position;
	/**
	 * If the Pawn is a king
	 */
	private final boolean king; // true if is a King

	/**
	 * Pawn constructor.
	 * @param bw TRUE=Black, FALSE=White
	 * @param x X Position
	 * @param y Y Position
	 * @param king TRUE if the Pawn is the king. (Valid only if bw=FALSE)
	 * @throws IllegalArgumentException If both king and bw are TRUE
	 */
	public Pawn(boolean bw, int x, int y, boolean king) throws IllegalArgumentException {
		if (king && bw)
			throw new IllegalArgumentException("Can't exist a Black King.");
		this.bw = bw;
		this.position = Position.of(x, y);
		this.king = king;
	}

	public boolean filterByTurn(boolean turn) {
		if (turn) { // black
			return isBlack();
		} else { // white
			return !isBlack();
		}
	}
	/**
	 * @return the position
	 */
	public Position getPosition() {
		return this.position;
	}

	/**
	 * @return the king
	 */
	public boolean isKing() {
		return this.king;
	}

	/**
	 * @return the bw
	 */
	public boolean isBlack() {
		return this.bw;
	}

	public final int getX(){
		return this.position.x;
	}
	public final int getY() {
		return this.position.y;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.position.x);
		result.append(this.position.y);
		result.append(this.isBlack() ? 'B' : (this.king ? 'K' : 'W'));
		return result.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isBlack() ? 1231 : 1237);
		result = prime * result + (king ? 1231 : 1237);
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Pawn)) {
			return false;
		}
		Pawn pawn = (Pawn) obj;
		return (this.isBlack()==pawn.isBlack() && this.position.x==pawn.position.x && this.position.y==pawn.position.y && this.king==pawn.king);
	}
	
	public boolean equals(Pawn pawn) {
		return (this.isBlack()==pawn.isBlack() && this.position.x==pawn.position.x && this.position.y==pawn.position.y && this.king==pawn.king);
	}
	
	private static final ArrayList<Pawn> flightweightPawns = new ArrayList<>();
	/**
	 * Flightweight Pawn. Returns a Pawn.
	 * @param bw TRUE=Black, FALSE=White
	 * @param x X Position
	 * @param y Y Position
	 * @param king TRUE if the Pawn is the king. (Valid only if bw=FALSE)
	 * @return The pawn
	 * @throws IllegalArgumentException If both king and bw are TRUE
	 */
	public static Pawn of(final boolean bw, final int x, final int y, final boolean king) throws IllegalArgumentException {
		for (Pawn pawn : flightweightPawns) {
			if (bw==pawn.isBlack() && x==pawn.position.x && y==pawn.position.y && king==pawn.king)
				return pawn;
		}
		Pawn pawn = new Pawn(bw, x, y, king);
		flightweightPawns.add(pawn);
		return pawn;
	}


}
