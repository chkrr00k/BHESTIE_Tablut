package bhestie.levpos;

public class Pawn {
	/**
	 * TRUE=Black, FALSE=White
	 */
	public final boolean bw;
	/**
	 * The Pawn position
	 */
	public final Position position;
	/**
	 * If the Pawn is a king
	 */
	public final boolean king; // true if is a King

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
		this.position = new Position(x, y);
		this.king = king;
	}

	public boolean filterByTurn(boolean turn) {
		if (turn) { // black
			return bw;
		} else { // white
			return !bw;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.position.x);
		result.append(this.position.y);
		result.append(this.bw ? 'B' : (this.king ? 'K' : 'W'));
		return result.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (bw ? 1231 : 1237);
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
		Pawn other = (Pawn) obj;
		if (bw != other.bw) {
			return false;
		}
		if (king != other.king) {
			return false;
		}
		if (position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!position.equals(other.position)) {
			return false;
		}
		return true;
	}
}
