package markovAnalysis;

import java.util.ArrayList;
import java.util.List;

public class MarkovState {
	
	private String name;
	private List<Transition> transitions;
	
	public MarkovState(String name) {
		this.name = name;
		transitions = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Transition> getTransitions() {
		return transitions;
	}
	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name).append(": ");
		for(Transition trans : transitions) {
			sb.append(trans.getTargetState().getName()+"(" + trans.getProbability()+"), ");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarkovState other = (MarkovState) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	
	
}
