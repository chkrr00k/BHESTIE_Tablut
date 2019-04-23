package bhestie.zizcom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import bhestie.levpos.Pawn;
import bhestie.levpos.State;
import com.google.gson.Gson;

public class Connector {
	private Socket s = null;
	private int port = -1;
	private String host = null;
	private String player = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private Gson gson = null;
	private boolean error;
	
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
		this.error = false;
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
			this.s.setTcpNoDelay(true); // i have no clue what this does
			this.dos = new DataOutputStream(this.s.getOutputStream());
			this.dis = new DataInputStream(this.s.getInputStream());
			this.host = null; // saves us some bytes;
			return true;
		}catch(IOException e){
			this.error = true;
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
	/**
	 * Finalized and close the socket
	 * @return if the disposal succeed
	 */

	public String readCode() throws IOException{
		return this.read();
	}


	public boolean dispose(){
		try{
			this.dis.close();
			this.dos.close();
			this.s.close();
		}catch(IOException e){
			this.error = true;
			return false;
		}
		return true;
	}
	/**
	 * @return if the socket is connected and no error was recorded
	 */
	public boolean isFault(){
		boolean result = !this.error;
		this.error = false;
		return result && this.s.isConnected();
	}
	/**
	 * Write an action to the server
	 * @param move the action to write
	 * @throws IOException
	 */
	public void writeAction(Action move) throws IOException{
		this.write(this.gson.toJson(move));

	}

	public void writeAction(State statoCorrente, State statoDestinazione) throws IOException{
		List<Pawn> pedoniStatoCorrente = statoCorrente.getPawns();
		List<Pawn> pedoniStatoDestinazione = statoDestinazione.getPawns();
		Pawn pedoneDestinazione = null;
		Pawn pedoneCorrente = null;



		for (Pawn pawnDestinazioneIterante : pedoniStatoDestinazione) {
			if (!pedoniStatoCorrente.contains(pawnDestinazioneIterante)) {
				pedoneDestinazione = pawnDestinazioneIterante;
			}
		}

		for (Pawn pawnCorrenteIterante : pedoniStatoCorrente) {
			if (!pedoniStatoDestinazione.contains(pawnCorrenteIterante)) {
				pedoneCorrente = pawnCorrenteIterante;
			}
		}

		Action action = new Action(pedoneCorrente.getX(),
				pedoneCorrente.getY(),
				pedoneDestinazione.getX(),
				pedoneDestinazione.getY(), statoCorrente.toString());
		writeAction(action);
	}


	private String read() throws IOException{
		try{
			int len = this.dis.readInt();
			byte[] rB = new byte[len];
			this.dis.readFully(rB, 0, len);
			String result = new String(rB, StandardCharsets.UTF_8);
			System.out.println(result);
			return result;
		}catch(IOException e){
			this.error = true;
			throw e;
		}

	}
	private void write(String input) throws IOException{
		try{
			byte[] wB = input.getBytes(StandardCharsets.UTF_8);
			this.dos.writeInt(input.length());
			this.dos.write(wB);
			this.dos.flush();
		}catch(IOException e){
			this.error = true;
			throw e;
		}
	}
	
}
