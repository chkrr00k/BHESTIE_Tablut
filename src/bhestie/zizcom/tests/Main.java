package bhestie.zizcom.tests;

import java.io.IOException;
import java.util.List;

import org.omg.CORBA.TIMEOUT;

import bhestie.levpos.HeuristicCalculatorGroup;
import bhestie.levpos.Minimax;
import bhestie.levpos.State;
import bhestie.zizcom.Board;
import bhestie.zizcom.Connector;

public class Main {

	private static final boolean whitePlayer = false;
	private static final boolean blackPlayer = !whitePlayer;
	
	private static final int WhitePort = 5800;
	private static final int BlackPort = 5801;
	
	private static int port;
	
	private static final void printLogo(){
		System.out.println( "BBBBBB  HH    H          SSSSS       II EEEEEE\n" +
							"BB    B HH    H   eeee  S       t    II EE\n" +
							"BBBBBB  HHHHHHH  e    e  SSSS  tttt  II EEEEEE\n" +
							"BB    B HH    H  eeeeee      S  t    II EE\n" +
							"BB    B HH    H  e           S  t    II EE\n" +
							"BBBBBB  HH    H   eeee  SSSSS    tt  II EEEEEE\n" +
							"	(Behemoth Heuristical Strategical Intelligence Engine)\n\n" +
							
							"Ecce, Behemoth, quem feci tecum, foenum quasi bos comedet:\n" +
							"Fortitudo eius in lumbis eius, et virtus illius in umbilico ventris eius.\n" +
							"Stringit caudam suam quasi cedrum, nervi testiculorum eius perplexi sunt.\n" +
							"Ossa eius velut fistulae aeris, cartilago illius quasi laminae ferreae.\n" +
							"Ipse est principium viarum Dei, qui fecit eum, applicabit gladium eius\n." +
							"[...]\n" +
							"In oculis eius quasi hamo capiet eum, et in sudibus perforabit nares eius.\n");
	}
	
	private static final String WHITE = "white";
	private static final String BLACK = "black";
	private static final String THREAD_FLAG = "-t";
	private static final String FIXED_DEPTH_FLAG = "-f";
	private static final String DEPTH_FLAG = "-d";
	private static final String TIMEOUT_FLAG = "-l";
	private static final String HELP_FLAG = "-h";
	private static final String HELP_STRING = "HELP!\n"
			+ "\t[white|black]\tThe color the player will play\n"
			+ "\t" + THREAD_FLAG + "\t\tHow many thread the program will use (default: 3)\n"
			+ "\t" + FIXED_DEPTH_FLAG + "\t\tIf the program can autoscale his depthness (default: true)\n"
			+ "\t" + DEPTH_FLAG + "\t\tThe current max depth (default: 3)\n"
			+ "\t" + TIMEOUT_FLAG + "\t\tTHe max timeout time (default: 50)\n\n\n\n";
	
	private static void parse(String[] args){
		if(args.length < 1){
			System.err.println("You need to give me the color you want to play!\n [white|black]");
			System.exit(-5);
		}
		if(args[0].equalsIgnoreCase(WHITE)){
			Minimax.player = whitePlayer;
		}else if(args[0].equalsIgnoreCase(BLACK)){
			Minimax.player = blackPlayer;
		}else if(args[0].equals(HELP_FLAG)){
			System.out.println(HELP_STRING);
			System.exit(0);
		}else{
			System.err.println("You need to give me the color you want to play!\n [white|black]");
			System.exit(-1);
		}
		boolean defaultThreads = true;
		for(int i = 1; i < args.length; i++){
			switch(args[i]){
			case HELP_FLAG:
				System.out.println(HELP_STRING);
				System.exit(0);
				break;
			case THREAD_FLAG:
				try{
					HeuristicCalculatorGroup.getInstance().addThreads(Integer.parseInt(args[++i]));
					defaultThreads = false;
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					System.err.println("You need to give me the number of threads you want!\n " + THREAD_FLAG + " <number>");
					System.exit(-2);
				}
				break;
			case FIXED_DEPTH_FLAG:
				Minimax.FIXEDDEPTH = true;
				break;
			case DEPTH_FLAG:
				try{
					Minimax.DEPTH = Integer.parseInt(args[++i]);
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					System.err.println("You need to give me the number of the depth you want!\n " + DEPTH_FLAG + " <number>");
					System.exit(-3);
				}
				break;
			case TIMEOUT_FLAG:
				try{
					Minimax.DEPTH = Integer.parseInt(args[++i]);
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					System.err.println("You need to give me the max timeout you want!\n " + TIMEOUT_FLAG + " <number>");
					System.exit(-4);
				}
				break;
			}
			if(defaultThreads){
				HeuristicCalculatorGroup.getInstance().addThreads(3);		

			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
//	    Minimax.player = blackPlayer;
	    args = new String[]{"white"};
	    
		parse(args);
		printLogo();
	    
	    if (Minimax.player == whitePlayer)
	    	port = WhitePort;
	    else
	    	port = BlackPort;
		
		Connector c = new Connector("__BHeStIE__", port);
		Board b = null;
		c.init();
		c.present();
		b = c.readBoard(); // Read the initial board
		
		if (Minimax.player == blackPlayer) {
			b = c.readBoard(); // Wait for enemy move
		}
		
		State currentState = new State(b.convert().get(), Minimax.player);
		try{
			while(true) {
				long result = Minimax.alphaBethInit(currentState);
				System.out.println(result + " Prevedo di " + (result == 0 ? "pareggiare" : (result > 0 ? "vincere" : "perdere")));

				
				currentState = Minimax.stack.get((int) Math.random() * Minimax.stack.size());
				List<State> unfold = currentState.unfold();
				int unfoldSize = unfold.size();
				if (unfoldSize > 0)
					currentState = unfold.get(unfoldSize - 1);
				c.writeAction(currentState.getAction()); // Sends our move
				
				Minimax.stack.clear();
				System.gc();
				b = c.readBoard(); // Gets the board after our move
				Thread.yield(); // Let the enemy "think correctly"
				b = c.readBoard(); // Gets the board after the enemy move
				
				State.TURN++;
				currentState = new State(b.convert().get(), Minimax.player, currentState.historyStorage, null);
				
				System.out.println(State.TURN);
			}
		}catch(Exception e){
			HeuristicCalculatorGroup.getInstance().killAll();
			System.exit(0);
		}
	}

}
