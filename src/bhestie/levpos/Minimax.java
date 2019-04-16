package bhestie.levpos;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Minimax {
	private Minimax() {}

	private static final int TIMEOUT = 10;
	public static final Duration timeout = Duration.ofSeconds(TIMEOUT);
	private static int esplorati = 0;
	private static boolean signal = false;

	private static final ComparatoreEuristica comparatore = new ComparatoreEuristica();

	public static State minimaxDecision(State state) {
		esplorati = 0;
		signal = false;
		Stream<State> stream = state.getActions().stream().parallel();
		List<State> lista = stream.collect(Collectors.toList());
		Thread t = new Thread(new Run(timeout.getSeconds()));
		t.start();
		State result = lista.stream().max(Comparator.comparing(Minimax::minValue)).get();
		t.interrupt();
		System.out.println("Esplorati = " + esplorati);
		return result;
	}

	private static double maxValue(State state) {
		esplorati++;
		if(signal)
			return 0;
		if(state.isTerminal()){
			return state.getUtility();
		}
		return state.getActions().stream().parallel()
				.sorted(comparatore).limit(1)
				.map(Minimax::minValue)
				.max(Comparator.comparing(Double::valueOf)).get();
	}

	private static double minValue(State state) {
		esplorati++;
		if(signal)
			return 0;
		if(state.isTerminal()){
			return state.getUtility();
		}
		return state.getActions().stream().parallel()
				.sorted(comparatore).limit(1)
				.map(Minimax::maxValue)
				.min(Comparator.comparing(Double::valueOf)).get();
	}

	public static synchronized void interrupt() {
		Minimax.signal = true;
	}

}

class ComparatoreEuristica implements Comparator<State> {
	@Override
	public int compare(State o1, State o2) {
		return (int) (o1.getHeuristic() - o2.getHeuristic());
	}
}

class Run implements Runnable {
	private long secs;
	public Run(long l) {
		this.secs = l;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000 * secs);
			//LockSupport.parkNanos(Minimax.timeout.getNano());
			Minimax.interrupt();
			System.out.println("Segnalato");
		} catch (Exception e) {
			return;
		}
	}

}