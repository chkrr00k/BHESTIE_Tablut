package bhestie.zizcom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

public class Connector {
	private Socket s = null;
	private int port = -1;
	private String host = null;
	private String player = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private Gson gson = null;
	
	/**
	 * Generate a new connector object for the communicate with the server.
	 * @param player the name of the player
	 * @param port the port where to connect
	 * @param host the host where to connect
	 */
	public Connector(String player, int port, String host) {
		super();
		this.port = port;
		this.host = host;
		this.player = player;
		this.gson = new Gson();
	}
	/**
	 * Generate a new connector object for the communicate with the server.
	 * @param player the name of the player
	 * @param port the port where to connect
	 */
	public Connector(String player, int port){
		this(player, port, "localhost");
	}
	/**
	 * Inizialize the server and stipulates the connection
	 * @return if the connection succeed
	 */
	public boolean init(){
		try{
			this.s = new Socket(this.host, this.port);
			this.dos = new DataOutputStream(this.s.getOutputStream());
			this.dis = new DataInputStream(this.s.getInputStream());
			this.host = null; // saves us some bytes;
			return true;
		}catch(IOException e){
			return false;
		}
	}
	/**
	 * Send player name to the server
	 * @throws IOException
	 */
	public void present() throws IOException{
		this.write(this.gson.toJson(this.player));
	}
	/**
	 * Read current read board status
	 * @return the read board
	 * @throws IOException
	 */
	public Board readBoard() throws IOException{
		return this.gson.fromJson(this.read(), Board.class);
	}
	public boolean dispose(){
		try{
			this.dis.close();
			this.dos.close();
			this.s.close();
		}catch(IOException e){
			return false;
		}
		return true;
	}
	/**
	 * Write an action to the server
	 * @param move the action to write
	 * @throws IOException
	 */
	public void writeAction(Action move) throws IOException{
		this.write(this.gson.toJson(move));

	}
	private String read() throws IOException{
		int len = this.dis.readInt();
		byte[] rB = new byte[len];
		this.dis.readFully(rB, 0, len);
		return new String(rB, StandardCharsets.UTF_8);
	}
	private void write(String input) throws IOException{
		byte[] wB = input.getBytes(StandardCharsets.UTF_8);
		this.dos.writeInt(input.length());
		this.dos.write(wB);
		this.dos.flush();
	}
	
}
