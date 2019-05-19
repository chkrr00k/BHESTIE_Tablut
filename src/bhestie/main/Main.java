package bhestie.main;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import bhestie.levpos.Minimax;
import bhestie.levpos.State;
import bhestie.levpos.ThreadPool;
import bhestie.levpos.utils.HistoryStorage;
import bhestie.zizcom.Board;
import bhestie.zizcom.Connector;

public class Main {

	private static final boolean whitePlayer = false;
	private static final boolean blackPlayer = !whitePlayer;
	
	private static final int WhitePort = 5800;
	private static final int BlackPort = 5801;
	
	private static int port;
	private static String host = "localhost";
	public static boolean verbose = false;
	
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
							"Ipse est principium viarum Dei, qui fecit eum, applicabit gladium eius.\n" +
							"[...]\n" +
							"In oculis eius quasi hamo capiet eum, et in sudibus perforabit nares eius.\n");
	}
	
	private static final int DEFAULT_THREADS_NUMBER = 4;
	private static final String WHITE = "white";
	private static final String BLACK = "black";
	private static final String THREAD_FLAG = "-t";
	private static final String FIXED_DEPTH_FLAG = "-f";
	private static final String DEPTH_FLAG = "-d";
	private static final String TIMEOUT_FLAG = "-l";
	private static final String HELP_FLAG = "-h";
	private static final String HOST_FLAG = "-H";
	private static final String SCALING_UP_FLAG = "-s:up";
	private static final String SCALING_DOWN_FLAG = "-s:dw";
	private static final String VERBOSE_FLAG = "-v";
	private static final String HELP_STRING = "HELP!\n"
			+ "\t[white|black]\tThe color the player will play\n"
			+ "\t" + HELP_FLAG + " <n>\t\tYes, i'm telling you this is the command to show the help even if you just did it\n"
			+ "\t" + THREAD_FLAG + " <n>\t\tHow many thread the program will use (default: " + DEFAULT_THREADS_NUMBER + ")\n"
			+ "\t" + FIXED_DEPTH_FLAG + "\t\tIf the program can't autoscale its depthness (default: " + Minimax.FIXEDDEPTH + ")\n"
			+ "\t" + DEPTH_FLAG + " <n>\t\tThe current max depth (default: " + Minimax.DEPTH + ")\n"
			+ "\t" + SCALING_DOWN_FLAG + " <n>\tThe times the process have to signaled to be scaled down in depth "
					+ "(default: " + Minimax.SCALINGFACTORDOWN + ")\n"
			+ "\t" + SCALING_UP_FLAG + " <n>\tThe times the process have to signaled to be scaled up in depth "
					+ "(default: " + Minimax.SCALINGFACTORUP + ")\n"
			+ "\t" + HOST_FLAG + " <ip>\t\tThe game server host address you want to connect (default: " + host + ")\n"
			+ "\t" + TIMEOUT_FLAG + " <n>\t\tTHe max timeout time (default: " + Minimax.TIMEOUT + ")\n";
	
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
		int numberOfThreads = DEFAULT_THREADS_NUMBER;
		for(int i = 1; i < args.length; i++){
			switch(args[i]){
			case HELP_FLAG:
				System.out.println(HELP_STRING);
				System.exit(0);
				break;
			case THREAD_FLAG:
				try{
					numberOfThreads = Integer.parseInt(args[++i]);
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					System.err.println("You need to give me the number of threads you want!\n " + THREAD_FLAG + " <number>");
					System.exit(-2);
				}
				break;
			case HOST_FLAG:
				try{
					host = args[++i];
				}catch(ArrayIndexOutOfBoundsException e){
					System.err.println("You need to give me the host ip you want to connect!\n " + HOST_FLAG + " <ip>");
					System.exit(-9);
				}
				break;
			case FIXED_DEPTH_FLAG:
				Minimax.FIXEDDEPTH = true;
				break;
			case VERBOSE_FLAG:
				Main.verbose = true;
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
					Minimax.TIMEOUT = Integer.parseInt(args[++i]);
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					System.err.println("You need to give me the max timeout you want!\n " + TIMEOUT_FLAG + " <number>");
					System.exit(-4);
				}
				break;
			case SCALING_DOWN_FLAG:
				try{
					Minimax.SCALINGFACTORDOWN = Integer.parseInt(args[++i]);
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					System.err.println("You need to give me the scale factor you want!\n " + SCALING_DOWN_FLAG + " <number>");
					System.exit(-10);
				}
				break;
			case SCALING_UP_FLAG:
				try{
					Minimax.SCALINGFACTORUP = Integer.parseInt(args[++i]);
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					System.err.println("You need to give me the scale factor you want!\n " + SCALING_UP_FLAG + " <number>");
					System.exit(-11);
				}
				break;
			}
		}
		ThreadPool.getInstance().setMaxThreads(numberOfThreads);	
	}
	
	private static List<State> getBaseState(){
		return Minimax.stack.stream()
				.parallel()
				.map(s -> {
					final List<State> r = s.unfold();
					return r.get(r.size() - 1);
				})
				.distinct()
				.collect(Collectors.toList());
	}
	
	private static State panicState(State currentState, Comparator<State> cs) {
		List<State> actions = StreamSupport.stream(currentState.getChildren(true).spliterator(), false).collect(Collectors.toList());
		int size = actions.size();
		if(size > 0){
			//currentState = actions.get(r.nextInt(size));
			try{
				actions.sort(cs);
				System.out.println("VNA SALVS VICTIS NVLLAM SPERARE SALVTEM");
				return actions.get(0);
			}catch (NullPointerException npe) {
				System.out.println("I Ii II L");
				System.exit(-1000);
			}
		}else{
			System.out.println("I Ii II L");
			System.exit(-1000);
		}
		return null;
	}

	public static void main(String[] args) {
		try{
			//args = new String[]{"white", SCALING_DOWN_FLAG, "0", SCALING_UP_FLAG, "0", DEPTH_FLAG, "3", TIMEOUT_FLAG, "50"}; //FIXME remove this to start it from CLI
			//args = new String[]{"white", FIXED_DEPTH_FLAG, DEPTH_FLAG, "3", TIMEOUT_FLAG, "50"}; //FIXME remove this to start it from CLI

			parse(args);
			printLogo();
			
			Minimax.init();
		    
		    if (Minimax.player == whitePlayer){
		    	port = WhitePort;
		    }else{
		    	port = BlackPort;
		    }
			
			Connector c = new Connector("__BHeStIE__", port, host);
			Comparator<State> cs = new Comparator<State>(){

				@Override
				public int compare(State arg0, State arg1) {
					int result = 0;
					if(Minimax.player){
						result = arg1.movesToGoal() - arg0.movesToGoal();
					}else{
						result = arg0.movesToGoal() - arg1.movesToGoal();
					}
					if(result == 0){
						if(Minimax.player){
							result = arg1.routeBlocked() - arg0.routeBlocked();
						}else{
							result = arg0.routeBlocked() - arg1.routeBlocked();
						}
					}
					if(result == 0){
						if(Minimax.player){
							result = arg0.whitePawnSurroundingKing() - arg1.whitePawnSurroundingKing();
						}else{
							result = arg1.whitePawnSurroundingKing() - arg0.whitePawnSurroundingKing();
						}
					}
					return result;
				}
				
			};
			Board b = null;
			if (!c.init()) {
				System.err.println("Where's my server?");
				System.exit(-8);
			}
			c.present();
			b = c.readBoard(); // Read the initial board
			
			if (Minimax.player == blackPlayer) {
				b = c.readBoard(); // Wait for enemy move
			}
			Random r = new Random(port + System.currentTimeMillis());
			State currentState = new State(b.convert().get(), Minimax.player);
			List<State> unfold = null;
			for(;;) {
				long result = Minimax.alphaBethInit(currentState);
				if(verbose){
					LocalTime before = LocalTime.now();
					System.out.println("Explored = " + Minimax.nodeExplored + " in " + ChronoUnit.MILLIS.between(before, LocalTime.now()));
					System.out.println(result + " Prevedo di " + (result == 0 ? "pareggiare" : (result > 0 ? "vincere" : "perdere")));
				}
				if (Minimax.stack.size() > 0) {
					try{
						final List<State> ls = getBaseState();
						ls.sort(cs);
						currentState = ls.get(0);
					}catch(Exception e){
						currentState = Minimax.stack.get(r.nextInt(Minimax.stack.size()));
						unfold = currentState.unfold();
						int unfoldSize = unfold.size();
						if (unfoldSize > 0){
							currentState = unfold.get(unfoldSize - 1);
						}else{
							currentState = panicState(currentState, cs);
						}
						unfold = null;
					}
				} else {
					currentState = panicState(currentState, cs);
				}
				try{
					c.writeAction(currentState.getAction()); // Sends our move
				}catch(NullPointerException | SocketException npe){
					System.out.println("I Ii II L");
					break;
				}
				
				if(verbose){
					long memoryBefore = Runtime.getRuntime().totalMemory() / 1024 / 1024;
					long freeBefore = Runtime.getRuntime().freeMemory() / 1024 / 1024;
					System.out.println("Before\n\tMemory allocated = " + memoryBefore + "\tFree = " + freeBefore);
				}
				HistoryStorage historyStorage = currentState.historyStorage; // Keep it
				currentState = null;
				Minimax.stack.clear();
				System.gc();
				if(verbose){
					long memoryAfter = Runtime.getRuntime().totalMemory() / 1024 / 1024;
					long freeAfter = Runtime.getRuntime().freeMemory() / 1024 / 1024;
					System.out.println("After\n\tMemory allocated = " + memoryAfter + "\tFree = " + freeAfter);
				}
				b = c.readBoard(); // Gets the board after our move
				Thread.yield(); // Let the enemy "think correctly"
				try{
				b = c.readBoard(); // Gets the board after the enemy move
				}catch(IOException ioe){
					System.out.println("VAE VICTIS");
					System.exit(1000);
				}
				State.TURN++;
				currentState = new State(b.convert().get(), Minimax.player, historyStorage, null);
				if(verbose){
					System.out.println(State.TURN);
				}
			}
		}catch(Exception e){
			System.err.println("Something happened.\nSomething happened.");
			e.printStackTrace();
			ThreadPool.getInstance().killAll();
			System.exit(-7);
		}
	}

}
