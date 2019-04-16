package bhestie.zizcom;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import bhestie.levpos.Pawn;

public class Board implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final int PAWNS_NUMBER = 25;
	private static final String EMPTY_TILE = "EMPTY";
	private static final String BLACK_TILE = "BLACK";
	private static final String WHITE_TILE = "WHITE";
	private static final String KING_TILE = "KING";
	
	private String[][] board;
	private String turn;
	//XXX tmp structure;
	private transient List<Pawn> pawns = null;
	
	
	/**
	 * A new board object rapresenting current status
	 */
	public Board(){
		this.pawns = new LinkedList<Pawn>();
	}
	/**
	 * Convert the internal received array to a list of pawns
	 */
	public void convert(){
		final short size = (short) (this.board.length - 1); // start point of the array
		String tile = ""; // tmp value to optimize array access;
		for(short i = size, p = 0; i >= 0 && p <= PAWNS_NUMBER; i--){ // reversed loop because theoretically faster
			for(short j = size; j >= 0 && p <= PAWNS_NUMBER; j--){ // ditto
				if((tile = this.board[i][j]).equals(BLACK_TILE)){ // if the tile is black
					
					this.pawns.add(new Pawn(true, i, j, false));
					p++;
					
				}else if(tile.equals(WHITE_TILE)){ // if the tile is white
					
					this.pawns.add(new Pawn(false, i, j, false));
					p++;
					
				}else if(tile.equals(KING_TILE)){ // if the tile is king
					
					this.pawns.add(new Pawn(false, i, j, true));
					p++;
					
				}
			}
		}
	}
	
	public List<Pawn> get(){
		return this.pawns;
	}
	public List<Pawn> getAndInit(){
		if(this.pawns == null){
			this.convert();
		}
		return this.pawns;
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
