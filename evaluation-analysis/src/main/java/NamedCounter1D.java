import java.util.HashMap;
import java.util.Map;


public class NamedCounter1D {
	
	private Map<String, Double> counts = new HashMap<>();
	
	public void addOne(String key) {
		if(!counts.containsKey(key)) {
			counts.put(key, 1.0D);
		} else {
			counts.put(key, counts.get(key) + 1.0D);
		}
	}
	
	public double getCount(String name) {
		if(counts.containsKey(name)) {
			return counts.get(name);			
		} else {
			return 0;
		}
	}
	
	public void normalize(double expectedSum) {
		double sum = counts.values().stream().reduce(0.0d, (a,b) -> a+b);
		Map<String, Double> normalized = new HashMap<>();
		counts.forEach((k,v) -> normalized.put(k, v*expectedSum/sum));
		this.counts = normalized;
	}
	@Override
	public String toString() {
		return counts.toString();
	}

}
