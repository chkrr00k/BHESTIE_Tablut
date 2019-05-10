package bhestie.levpos;

import java.util.Queue;

import bhestie.levpos.State.ParallelStateGenerator;

public class StackItems {
	private final Queue<State> queue;
	private final State.ParallelStateGenerator stateGenerator;
	
	public StackItems(Queue<State> queue, ParallelStateGenerator stateGenerator) {
		super();
		this.queue = queue;
		this.stateGenerator = stateGenerator;
	}

	public Queue<State> getQueue() {
		return queue;
	}

	public State.ParallelStateGenerator getStateGenerator() {
		return stateGenerator;
	}
}
