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
}
