package laziness;

public class word {
	public String word;
	public int count;
	
	public word(String inputWord) {
		word = inputWord;
		count = 1;
	}
	
	public void increment() {
		count++;
	}
	
	public String hashcode() {
		StringBuilder temp = null;
		temp.append(count + "." + "word");
		return temp.toString();
	}
}
