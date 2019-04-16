package bhestie.zizcom;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Board implements Serializable{
	private String[][] board;
	private String turn;
	//XXX tmp structure;
	private transient List<Pawn> pawns;
	private class Pawn{
		int x, y;
		public Pawn(int x, int y) {
			this.x = x; this.y = y;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append(x);
			builder.append(",");
			builder.append(y);
			builder.append(")");
			return builder.toString();
		}
		
	}
	
	/**
	 * A new board object rapresenting current status
	 */
	public Board(){
		this.pawns = new LinkedList<Board.Pawn>();
	}
	/**
	 * Convert the internal received array to a list of pawns
	 */
	public void convert(){
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				if(!this.board[i][j].equals("EMPTY")){
					this.pawns.add(new Pawn(i, j));
				}
			}
		}
	}
	
	public synchronized String[][] getBoard() {
		return board;
	}
	public synchronized void setBoard(String[][] board) {
		this.board = board;
	}
	public synchronized String getTurn() {
		return turn;
	}
	public synchronized void setTurn(String turn) {
		this.turn = turn;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Board [pawns=");
		builder.append(pawns);
		builder.append("]");
		return builder.toString();
	}

	
	
}
