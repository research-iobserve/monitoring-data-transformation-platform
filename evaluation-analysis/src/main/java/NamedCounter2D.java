import java.util.HashMap;
import java.util.Map;


public class NamedCounter2D {
	
	private Map<String, NamedCounter1D> counts = new HashMap<>();
	
	public void addOne(String keyFirstDim, String keySecondDim) {
		if(!counts.containsKey(keyFirstDim)) {
			counts.put(keyFirstDim, new NamedCounter1D());
		} 
		counts.get(keyFirstDim).addOne(keySecondDim);
	}

	
	public double getCount(String keyFirstDim, String keySecondDim) {
		if(counts.containsKey(keyFirstDim)) {
			return counts.get(keyFirstDim).getCount(keySecondDim);			
		} else {
			return 0;
		}
	}
	
	public void normalize(double expectedRowSum) {
		counts.forEach((k,v) -> v.normalize(expectedRowSum));
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		counts.forEach((k,v) -> sb.append(k).append(": ").append(v).append("\n"));
		
		return sb.toString();
	}

}
