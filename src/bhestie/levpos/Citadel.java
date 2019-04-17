package bhestie.levpos;

import java.util.ArrayList;

public class Citadel {
	public ArrayList<Position> citadelPositions = null;
	public Citadel(ArrayList<Position> positions) {
		this.citadelPositions = positions;
	}
	public boolean isPawnInCitadel(Pawn pawn) {
		return (this.citadelPositions.indexOf(pawn.position) >= 0);
	}
	public boolean isPositionInCitadel(Position position) {
		return (this.citadelPositions.indexOf(position) >= 0);
	}
	public boolean isXYInCitadel(int x, int y) {
		return this.isPositionInCitadel(new Position(x, y));
	}
	public boolean isXYInFringeCitadels(int x, int y) {
		int position = (this.citadelPositions.indexOf(new Position(x, y)));
		return (position >= 0 && position != 3);
	}
}
