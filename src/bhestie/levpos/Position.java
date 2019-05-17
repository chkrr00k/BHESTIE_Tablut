package bhestie.levpos;

import java.util.List;

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
	private Position(int x, int y) {
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

	public boolean equalsAny(List<Position> listPosition) {
		for(Position position : listPosition){
			if(this.equals(position))
				return true;
		}
		return false;

	}
	
	public boolean equalsAny(Position[] positions) {
		for(Position position : positions){
			if(this.equals(position))
				return true;
		}
		return false;
	}
	
	public boolean equals(Position position) {
		return (this.x==position.x && this.y==position.y);
	}
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('[');
		result.append(x);
		result.append(';');
		result.append(y);
		result.append(']');
		return result.toString();
	}
	
	static {
		int size = 11;
		flightweightPositions = new Position[size][size];
		for (int i = 1; i <= 9; i++){
			for (int j = 1; j <= 9; j++){
				Position.of(i, j); // Cache all board values (not the perimetral)
			}
		}
	}
	private static final Position[][] flightweightPositions; // 11x11. In this way I cover from 0 (impossibile) to 10 (impossile) and I can save the generated value in the board linger
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
			System.out.println("Non ottimizzato");
			return new Position(x, y);
		}
		if (result == null) {
			result = new Position(x, y);
			flightweightPositions[x][y] = result;
		}
		return result;
	}
}
