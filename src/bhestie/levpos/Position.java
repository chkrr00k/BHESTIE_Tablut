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
		Position other = (Position) obj;
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		return true;
	}
	@Override
	public String toString() {
		return "[" + x + ";" + y + "]";
	}
}
