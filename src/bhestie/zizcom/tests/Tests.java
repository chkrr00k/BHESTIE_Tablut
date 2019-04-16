package bhestie.zizcom.tests;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.gson.Gson;

import bhestie.zizcom.Action;
import bhestie.zizcom.Board;
import bhestie.zizcom.Connector;

public class Tests {

	@Test
	public void testAction() {
		Action a = new Action("a2", "c3", "W");
		assertTrue("Action's from", a.getFrom().equals("a2"));
		assertTrue("Action's to", a.getTo().equals("c3"));
		assertTrue("Action's state", a.getState().equals("W"));
		
		String json = "{\"from\":\"a2\",\"to\":\"c3\",\"state\":\"W\"}";
		a = new Gson().fromJson(json, Action.class);
		assertTrue("Action's from from json", a.getFrom().equals("a2"));
		assertTrue("Action's to from json", a.getTo().equals("c3"));
		assertTrue("Action's state from json", a.getState().equals("W"));
		
		assertTrue("Action to json", new Gson().toJson(a).equals(json));
		
		a = new Action(1, 2, 3, 3, "W");
		assertTrue("Action to json from numeric coordinates", new Gson().toJson(a).equals(json));
	}
	
	@Test
	public void testBoard() {
		Board b = new Board();
		String board = "{\"board\":[[\"WHITE\",\"EMPTY\",\"EMPTY\"],[\"EMPTY\",\"EMPTY\",\"EMPTY\"],[\"EMPTY\",\"EMPTY\",\"EMPTY\"]]}";
		b = new Gson().fromJson(board, Board.class);
		b.convert();
		assertTrue("Board size from json", b.get().size() == 1);
		assertTrue("Pawn's x form json", b.get().get(0).x == 0);
		assertTrue("Pawn's y form json", b.get().get(0).y == 0);
		assertTrue("Pawn's type form json", !b.get().get(0).bw);
		assertTrue("If pawn is king form json", !b.get().get(0).king);
	}
	
	@Test
	public void testConnector() throws IOException{
		String name = "test";
		ServerSocket ss = new ServerSocket(9000);
		Connector c = new Connector(name, 9000);
		c.init();
		Socket s = ss.accept();
		
		c.present();
		DataInputStream din = new DataInputStream(s.getInputStream());
		int len = din.readInt();
		byte[] rB = new byte[len];
		din.readFully(rB, 0, len);
		assertTrue("Own name sending to server", new String(rB, StandardCharsets.UTF_8).equals(new Gson().toJson(name)));
		
		c.writeAction(new Action(1, 2, 3, 3, "W"));
		len = din.readInt();
		rB = new byte[len];
		din.readFully(rB, 0, len);
		assertTrue("Action sent to server", new String(rB, StandardCharsets.UTF_8).equals("{\"from\":\"a2\",\"to\":\"c3\",\"state\":\"W\"}"));
		
		String board = "{\"board\":[[\"WHITE\",\"EMPTY\",\"EMPTY\"],[\"EMPTY\",\"EMPTY\",\"EMPTY\"],[\"EMPTY\",\"EMPTY\",\"EMPTY\"]]}";
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		byte[] wB = board.getBytes(StandardCharsets.UTF_8);
		dos.writeInt(board.length());
		dos.write(wB);
		dos.flush();
		Board b = c.readBoard();
		b.convert();
		assertTrue("Board size from server", b.get().size() == 1);
		assertTrue("Pawn's x form server", b.get().get(0).x == 0);
		assertTrue("Pawn's y form server", b.get().get(0).y == 0);
		assertTrue("Pawn's type form server", !b.get().get(0).bw);
		assertTrue("If pawn is king form server", !b.get().get(0).king);
		
		s.close();
		ss.close();
		assertTrue("Dispose with success", c.dispose());
	}

}
