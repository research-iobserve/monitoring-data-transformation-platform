package markovAnalysis;

import java.util.HashSet;
import java.util.Set;

public class VariableAnnotatedMarkovState extends MarkovState{
	
	Set<String> setVariables;

	public VariableAnnotatedMarkovState(String name) {
		super(name);
		setVariables = new HashSet<>();
	}
	
	
	
	public Set<String> getSetVariables() {
		return setVariables;
	}



	public VariableAnnotatedMarkovState clone() {
		VariableAnnotatedMarkovState copy = new VariableAnnotatedMarkovState(getName());
		copy.getSetVariables().addAll(getSetVariables());
		return copy;
	}



	@Override
	public String toString() {
		return "vars="+setVariables+" - " + super.toString();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableAnnotatedMarkovState other = (VariableAnnotatedMarkovState) obj;
		if (setVariables == null) {
			if (other.setVariables != null)
				return false;
		} else if (!setVariables.containsAll(other.setVariables) || !other.setVariables.containsAll(setVariables))
			return false;
		return true;
	}
	
	

}
