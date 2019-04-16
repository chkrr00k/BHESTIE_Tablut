package bhestie.zizcom.tests;

import java.io.IOException;

import bhestie.zizcom.Action;
import bhestie.zizcom.Board;
import bhestie.zizcom.Connector;

public class Main {

	public static void main(String[] args) throws IOException {
		Connector c = new Connector("white", 5800);
		Action a = new Action("e4", "f4", "W");
		Board b = null;
		c.init();
		c.present();
		c.writeAction(a);
		b = c.readBoard();
		b.convert();
		System.out.println(b);
		b = c.readBoard();
		b.convert();
		System.out.println(b);
	}

}
