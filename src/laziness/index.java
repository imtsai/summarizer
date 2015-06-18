package laziness;

public class index {
	public int lineNumber;
	public int wordNumber;
	public index(int line, int word) {
		lineNumber = line;
		wordNumber = word;
	}
	
	public String toString() {
		StringBuilder all = new StringBuilder();
		all.append("(" + lineNumber + ", " + wordNumber + ")");
		return all.toString();
	}
}
