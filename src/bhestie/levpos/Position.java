package bhestie.levpos;

public class Position {
	/**
	 * The X position
	 */
	public final int x;
	/**
	 * The Y position
	 */
	public final int y;
	/**
	 * Creates a new position with coord X and Y
	 * @param x X position
	 * @param y Y position
	 */
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		if (!(obj instanceof Position)) {
			return false;
		}
		Position position = (Position) obj;
		return (this.x==position.x && this.y==position.y);
	}
	public boolean equals(Position position) {
		return (this.x==position.x && this.y==position.y);
	}
	@Override
	public String toString() {
		return "[" + x + ";" + y + "]";
	}
	
	private static final Position[][] flightweightPositions = new Position[9][9]; // Can't have more than 81 elements
	/**
	 * Flightweight of position. It returns a Position.
	 * @param x The X position.
	 * @param y The Y position.
	 * @return A position of (X, Y)
	 */
	public static Position of(final int x, final int y) {
		Position result;
		try {
			result = flightweightPositions[x][y];
		} catch(IndexOutOfBoundsException e) {
			return new Position(x, y);
		}
		if (result == null) {
			result = new Position(x, y);
			flightweightPositions[x][y] = result;
		}
		return result;
	}
}
