package bhestie.levpos;
public class Position {
	public int x;
	public int y;
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof Position) {
			Position pos = (Position)o;
			return (this.x == pos.x && this.y == pos.y);
		}
		return false;
	}
	public String toString() {
		return "[" + x + ";" + y + "]";
	}
}
