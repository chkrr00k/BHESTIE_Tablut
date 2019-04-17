package bhestie.levpos;

public class Pawn {
	public boolean bw; //false=white, true=black
	public Position position;
	public int x; // 1 to 9
	public int y; // 1 to 9
	public boolean king = false; // true if is a King

	public Pawn(boolean bw, int x, int y, boolean king) {
		this.bw = bw;
		this.position = new Position(x, y);
		this.x = x;
		this.y = y;
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
		result.append(x);
		result.append(y);
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
