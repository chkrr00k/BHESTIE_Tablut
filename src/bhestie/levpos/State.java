package bhestie.levpos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class State {
	public boolean turn = false; // false=white, true=black
	public List<Pawn> pawns;

	private final List<Citadel> citadels = new ArrayList<>(4); // List of citadels
	//private final List<Position> citadels = new LinkedList<>(); // List of Citadel positions
	private final Position tronePosition = new Position(5, 5); // King position
	public State(List<Pawn> pawns, boolean turn){
		this.turn = turn;
		this.pawns = new LinkedList<>(pawns);

		ArrayList<Position> citadelPositions = new ArrayList<>(4);
		citadelPositions.add(new Position(4, 1));
		citadelPositions.add(new Position(5, 1));
		citadelPositions.add(new Position(6, 1));
		citadelPositions.add(new Position(5, 2));
		this.citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		citadelPositions.add(new Position(1, 4));
		citadelPositions.add(new Position(1, 5));
		citadelPositions.add(new Position(1, 6));
		citadelPositions.add(new Position(2, 5));
		this.citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		citadelPositions.add(new Position(9, 4));
		citadelPositions.add(new Position(9, 5));
		citadelPositions.add(new Position(9, 6));
		citadelPositions.add(new Position(8, 5));
		this.citadels.add(new Citadel(citadelPositions));

		citadelPositions = new ArrayList<>(4);
		citadelPositions.add(new Position(4, 9));
		citadelPositions.add(new Position(5, 9));
		citadelPositions.add(new Position(6, 9));
		citadelPositions.add(new Position(5, 8));
		this.citadels.add(new Citadel(citadelPositions));
	}

	public Collection<State> getActions(){
		List<State> actions = new LinkedList<>();

		for (Pawn currentPawn : this.pawns.stream().filter(p -> p.filterByTurn(this.turn)).collect(Collectors.toList())) {

			for (int i = currentPawn.x + 1; i <= 9; i++) {
				if (!this.checkXY(i, currentPawn.y, actions, currentPawn))
					break;
			}

			for (int i = currentPawn.x - 1; i >= 1; i--) {
				if (!this.checkXY(i, currentPawn.y, actions, currentPawn))
					break;
			}

			for (int i = currentPawn.y + 1; i <= 9; i++) {
				if(!this.checkXY(currentPawn.x, i, actions, currentPawn))
					break;
			}

			for (int i = currentPawn.y - 1; i >= 1; i--) {
				if(!this.checkXY(currentPawn.x, i, actions, currentPawn))
					break;
			}

		}

		return actions;
	}

	private boolean checkXY(int x, int y, List<State> actions, Pawn currentPawn) {
		boolean haveToAddThePawn;

		haveToAddThePawn = !this.pawns.stream().anyMatch(p -> p.y == y && p.x == x); // Se non c'è già un altro pezzo

		if (currentPawn.bw == false /*is white*/
				|| currentPawn.bw == true && !this.citadels.stream().anyMatch(c -> c.isPawnInCitadel(currentPawn)) /*il pezzo è nero e non in una citadel*/ ) {
			haveToAddThePawn = haveToAddThePawn && !this.citadels.stream().anyMatch(c -> c.isXYInCitadel(x, y));
		}

		if (currentPawn.bw == true /*is black*/
				&& this.citadels.stream().anyMatch(c -> c.isPawnInCitadel(currentPawn))) { // pezzo nero e dentro una citadel, allora può muoversi solo dentro la sua citadel, non può andare in un'altra citadel
			haveToAddThePawn = haveToAddThePawn && !this.citadels.stream().filter(c -> !c.isPawnInCitadel(currentPawn)).anyMatch(c -> c.isXYInCitadel(x, y));
		}

		haveToAddThePawn = haveToAddThePawn && (x != this.tronePosition.x && y != this.tronePosition.y);

		if (haveToAddThePawn) {
			List<Pawn> newPawns = new LinkedList<>(this.pawns);
			newPawns.remove(currentPawn);
			Pawn newPawn = new Pawn(currentPawn.bw, x, y, currentPawn.king);
			// TODO aggiungere funzione per calcolare i "mangiati"
			newPawns.add(newPawn);
			actions.add(new State(newPawns, !this.turn));
			return true;
		}
		return false;
	}

	public long getHeuristic() {
		// TODO da fare
		return 0;
	}

	public boolean isTerminal() {
		// TODO calcolare se è terminale
		return false;
	}

	public double getUtility() {
		// TODO Dire se ho vinto o perso (o pareggiato)
		return 0;
		/*if(firstPlayer)
			return Double.MIN_VALUE;
		else
			return Double.MAX_VALUE;*/
	}

	public String stampaScacchiera() {
		StringBuilder result = new StringBuilder();
		for(int i = 1; i <= 9; i++) {
			for(int j = 1; j <= 9; j++) {
				int x = i;
				int y = j;
				Optional<Pawn> pawn = this.pawns.stream().filter(p -> p.x==x && p.y==y).findAny();
				if(pawn.isPresent()) {
					result.append((pawn.get().bw ? 'B' : (pawn.get().king) ? 'K' : 'W'));
				} else {
					result.append('0');
				}
			}
			result.append('\n');
		}
		return result.toString();
	}
	
	@Override
	public String toString() {
		return this.stampaScacchiera();
	}
}
