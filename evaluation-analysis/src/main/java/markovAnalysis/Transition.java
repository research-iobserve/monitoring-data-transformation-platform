package markovAnalysis;

import java.util.HashMap;
import java.util.Map;

public class Transition {

	private MarkovState targetState;
	private double probability;

	public Transition() {
		super();
		probability = 0;
	}

	public MarkovState getTargetState() {
		return targetState;
	}

	public void setTargetState(MarkovState targetState) {
		this.targetState = targetState;
	}

	protected double getProbability() {
		return probability;
	}

	protected void setProbability(double probability) {
		this.probability = probability;
	}


}
