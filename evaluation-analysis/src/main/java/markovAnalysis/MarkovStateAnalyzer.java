package markovAnalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkovStateAnalyzer {
	
	
	public static void main(String[] args) throws IOException {
		MarkovChain chain = MarkovChain.parse("C:\\Users\\JKU\\Desktop\\thesis\\broadleaf\\scripts\\workloadProfiles\\behaviours\\script_default_behaviour.csv");
		System.out.println(chain);
		Map<MarkovState,Double> steadyState = chain.getSteadyState();
		//steadyState.forEach((s,v) -> System.out.println(s.getName()+" - " + v));
		
		MarkovChain guardVaraint = chain.getGuardAnnotatedVariant("C:\\Users\\JKU\\Desktop\\thesis\\broadleaf\\scripts\\workloadProfiles\\behaviours\\guards.csv");
		System.out.println(guardVaraint);
		steadyState = guardVaraint.getSteadyState();
		
		Map<String, Double> combined = new HashMap<>();
		steadyState.forEach((s,v) -> {
			if(!combined.containsKey(s.getName())) {
				combined.put(s.getName(), 0.0d);
			}
			combined.put(s.getName(), combined.get(s.getName())+v);
		});
		
		System.out.println("total probabilities:");
		combined.forEach((name,val) -> System.out.print(name+": " + (val * 100)+","));
		System.out.println();
		
		List<MarkovState> states = new ArrayList<>(chain.getMarkovStates());
		states.remove(chain.getEndMarkovState());
		states.remove(chain.getBeginMarkovState());
		states.add(0, chain.getBeginMarkovState());
		states.add(chain.getEndMarkovState());
		
		final double[][] combinedTransitions = new double[states.size()-1][states.size()];
		for(int i=0; i< states.size()-1; i++) {
			final int row = i;
			final String name = states.get(i).getName();
			steadyState.forEach((state,val) -> {
				if(state.getName().equals(name)) {
					for(Transition trans : state.getTransitions()) {
						MarkovState target = chain.getStateByName(trans.getTargetState().getName());
						int targetIndex = states.indexOf(target);
						combinedTransitions[row][targetIndex] += val * trans.getProbability();
					}
				}
			});
		}
		for(MarkovState state : states) {
			System.out.print(","+state.getName());
		}
		System.out.println();
		for(int i=0; i< combinedTransitions.length; i++) {
			System.out.print(states.get(i).getName());
			double sum = 0;
			for(int j=0; j<combinedTransitions[i].length; j++) {
				sum += combinedTransitions[i][j];
			}
			for(int j=0; j<combinedTransitions[i].length; j++) {
				combinedTransitions[i][j] /= sum/100.0;
				System.out.print(","+combinedTransitions[i][j]);
			}
			System.out.println();
		}
	}
	
}
