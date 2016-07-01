package markovAnalysis;

import java.util.Set;

public interface Action {

	void apply(Set<String> variableSet) ;
	
	public static Action setVariable(String varName) {
		return (set) -> set.add(varName);
	}

	public static Action unsetVariable(String varName) {
		return (set) -> set.remove(varName);
	}
	
	public static Action nop() {
		return (set) -> {};
	}
	
	public static Action parseSingle(String action) {
		if(action.equals("-")) {
			return Action.nop();
		} else {
			if(action.endsWith("=true")) {
				return Action.setVariable(action.substring(0,action.length() - "=true".length()));
			} else if(action.endsWith("=false")) {
				return Action.unsetVariable(action.substring(0,action.length() - "=false".length()));
			} else {
				throw new IllegalArgumentException();
			}
		}
	}
	
	public static Action parse(String actions) {
		String[] action = actions.split(";");
		Action curr = Action.nop();
		for(String act :action) {
			final Action last = curr;
			final Action next = parseSingle(act);
			curr = (v) -> {
				last.apply(v);
				next.apply(v);
			};
		}
		return curr;
	}
	
	
}
