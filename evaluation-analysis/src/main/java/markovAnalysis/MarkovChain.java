package markovAnalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealVector;

import weka.core.matrix.EigenvalueDecomposition;

public class MarkovChain {

	private List<MarkovState> states;
	private MarkovState beginMarkovState;
	private MarkovState endMarkovState;

	public MarkovChain(List<MarkovState> states, MarkovState beginMarkovState, MarkovState endMarkovState) {
		super();
		this.states = states;
		this.beginMarkovState = beginMarkovState;
		this.endMarkovState = endMarkovState;
	}

	protected List<MarkovState> getMarkovStates() {
		return states;
	}

	protected MarkovState getBeginMarkovState() {
		return beginMarkovState;
	}

	protected MarkovState getEndMarkovState() {
		return endMarkovState;
	}

	public MarkovState getStateByName(String name) {
		for (MarkovState state : states) {
			if (state.getName().equals(name)) {
				return state;
			}
		}
		return null;
	}

	public Map<MarkovState, Double> getSteadyState() {
		List<MarkovState> indexList = new ArrayList<>(states);
		indexList.remove(endMarkovState);
		Array2DRowRealMatrix transitionMatrix = new Array2DRowRealMatrix(indexList.size(), indexList.size());
		for(int i=0; i<indexList.size(); i++) {
			double[] transProps = new double[indexList.size()];
			for(Transition trans : indexList.get(i).getTransitions()) {
				MarkovState target = trans.getTargetState();
				if(target == endMarkovState) {
					target = beginMarkovState;
				}
				int toIndex = indexList.indexOf(target);
				transProps[toIndex] += trans.getProbability();
			}
			transitionMatrix.setColumn(i, transProps);
		}
		
		//the steady state equals to the eigen vecotr with eigenvalue 1
		EigenDecomposition decomp = new EigenDecomposition(transitionMatrix);
		int count = decomp.getRealEigenvalues().length;
		for(int i=0; i< count; i++) {
			double val = decomp.getRealEigenvalue(i);
			RealVector vec = decomp.getEigenvector(i);
			if(Math.abs(val - 1) < 0.00001d) {
				double sum = 0.0;
				for(int j=0; j<indexList.size(); j++) {
					sum += vec.getEntry(j);
				}
				Map<MarkovState, Double> result = new HashMap<>();
				for(int j=0; j<indexList.size(); j++) {
					result.put(indexList.get(j),  vec.getEntry(j) / sum);
				}
				return result;
			}
		}
		
		return null;
	}
	
	public static MarkovChain parse(String transitionMatrixFile) throws IOException {
		List<String> transCont = Files.readAllLines(Paths.get(transitionMatrixFile));

		List<MarkovState> statesOrdered = new ArrayList<>();
		Map<String, MarkovState> statesMap = new HashMap<>();
		MarkovState startMarkovState = null;
		MarkovState exitMarkovState = null;

		String[] stateNames = transCont.get(0).split(",");
		for (int i = 1; i < stateNames.length; i++) {
			MarkovState state = new MarkovState(stateNames[i]);
			statesMap.put(stateNames[i], state);
			statesOrdered.add(state);
		}
		exitMarkovState = statesMap.get("$");

		for (int i = 1; i < transCont.size(); i++) {
			String[] line = transCont.get(i).split(",");
			MarkovState state;
			if (line[0].endsWith("*")) {
				state = statesMap.get(line[0].substring(0, line[0].length() - 1));
				startMarkovState = state;
			} else {
				state = statesMap.get(line[0]);
			}
			for (int j = 1; j < line.length; j++) {
				double prob = Double.parseDouble(line[j]);
				if (prob > 0) {
					Transition trans = new Transition();
					trans.setTargetState(statesOrdered.get(j - 1));
					trans.setProbability(prob);
					state.getTransitions().add(trans);
				}
			}
		}
		return new MarkovChain(statesOrdered, startMarkovState, exitMarkovState);
	}

	public MarkovChain getGuardAnnotatedVariant(String guardsFile) throws IOException {

		Map<String, Guard> guards = new HashMap<>();
		Map<String, Action> actions = new HashMap<>();

		for (String line : Files.readAllLines(Paths.get(guardsFile))) {
			String[] l = line.split(",");
			guards.put(l[0], Guard.parse(l[1]));
			actions.put(l[0], Action.parse(l[2]));

		}
		Set<VariableAnnotatedMarkovState> allStates = new HashSet<>();
		VariableAnnotatedMarkovState newStart = translateState(this.beginMarkovState, Collections.emptySet(), guards, actions, allStates);
		VariableAnnotatedMarkovState newEnd = allStates.stream().filter((s) -> s.getName().equals(endMarkovState.getName())).findFirst().get();

		return new MarkovChain(new ArrayList<>(allStates), newStart, newEnd);
	}

	private VariableAnnotatedMarkovState translateState(MarkovState originalState, Set<String> currentSetVariables, Map<String, Guard> guards, Map<String, Action> actions,
			Set<VariableAnnotatedMarkovState> translatedStates) {
		// Step one: check if it already exists
		VariableAnnotatedMarkovState refState = new VariableAnnotatedMarkovState(originalState.getName());
		refState.getSetVariables().addAll(currentSetVariables);
		for (VariableAnnotatedMarkovState state : translatedStates) {
			if (state.equals(refState)) {
				return state;
			}
		}
		// step two create it as it does not exist, translate transitions while
		// taking guards and actions into account
		translatedStates.add(refState);

		if (originalState != endMarkovState) {
			HashSet<String> postActionVars = new HashSet<>(currentSetVariables);
			// apply the action of this state

			actions.get(refState.getName()).apply(postActionVars);
			for (Transition orgTrans : originalState.getTransitions()) {
				// check the guard
				MarkovState orgTarget = orgTrans.getTargetState();
				if (orgTarget == endMarkovState || guards.get(orgTarget.getName()).doesVariableSetFulfillGuard(postActionVars)) {
					// translate the transition
					Transition newTrans = new Transition();
					newTrans.setProbability(orgTrans.getProbability());
					newTrans.setTargetState(translateState(orgTarget, postActionVars, guards, actions, translatedStates));
					refState.getTransitions().add(newTrans);
				}

			}

			// step three: renormalize probabilities
			double sum = refState.getTransitions().stream().mapToDouble(Transition::getProbability).sum();
			for (Transition trans : refState.getTransitions()) {
				trans.setProbability(trans.getProbability() / sum);
			}
		}

		return refState;

	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (MarkovState state : states) {
			sb.append(state.toString()).append("\n");
		}
		return sb.toString();
	}

}
