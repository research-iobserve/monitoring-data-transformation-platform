package markovAnalysis;

import java.util.Set;

public interface Guard {
	
	public boolean doesVariableSetFulfillGuard(Set<String> setVariables);
	
	public static Guard variableNotSet(final String varName) {
		return (set) -> !set.contains(varName);
	}

	public static Guard variableSet(final String varName) {
		return (set) -> set.contains(varName);
	}
	
	public static Guard alwaysTrue() {
		return (set) -> true;
	}
	
	public static Guard parse(String guard) {
		if(guard.equals("-")) {
			return Guard.alwaysTrue();
		} else {
			if(guard.startsWith("!")) {
				return Guard.variableNotSet(guard.substring(1));
			} else {
				return Guard.variableSet(guard);
			}
		}
	}

}
