package bhestie.zizcom;

import java.io.Serializable;

public class Action implements Serializable{
	private static final long serialVersionUID = 1L;
	private String from;
	private String to;
	private String state;
	
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
	
	
	public Action() {
		this.from = "";
		this.to = "";
		this.state = "";
	}


	public synchronized String getFrom() {
		return from;
	}
	public synchronized void setFrom(String from) {
		this.from = from;
	}
	public synchronized String getTo() {
		return to;
	}
	public synchronized void setTo(String to) {
		this.to = to;
	}
	public synchronized String getState() {
		return state;
	}
	public synchronized void setState(String state) {
		this.state = state;
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
