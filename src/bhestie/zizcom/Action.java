package bhestie.zizcom;

import java.io.Serializable;

public class Action implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private final String from;
	private final String to;
	private final String state;
	
	/**
	 * An action object to write to the server the next move
	 * @param from starting position
	 * @param to landing poistion
	 * @param state current status
	 */
	public Action(String from, String to, String state) {
		this.from = from;
		this.to = to;
		this.state = state;
	}
	/**
	 *  An action object to write to the server the next move
	 * @param fx form x
	 * @param fy from y
	 * @param tx to x
	 * @param ty to y
	 * @param state current status
	 */
	public Action(int fx, int fy, int tx, int ty, String state) {
		this.from = String.format("%c%d", this.convertToAlphaBeth(fx), fy);
		this.to = String.format("%c%d", this.convertToAlphaBeth(tx), ty);
		this.state = state;
	}

	private char convertToAlphaBeth(int input){
		return (char) ('`' + input);
	}
	
	public synchronized String getFrom() {
		return from;
	}
	public synchronized String getTo() {
		return to;
	}
	public synchronized String getState() {
		return state;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Action [from=");
		builder.append(from);
		builder.append(", to=");
		builder.append(to);
		builder.append(", state=");
		builder.append(state);
		builder.append("]");
		return builder.toString();
	}

}
