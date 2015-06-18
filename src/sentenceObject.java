package laziness;

public class sentenceObject {
	public int sentenceNum;
	public String line;
	public int score;
	
	public sentenceObject(String inputLine, int num) {
		line = inputLine;
		score = 0;
		sentenceNum = num;
	}
	
	public void genSentenceScore(String[] keywords) {
		for (String keyword: keywords) {
			if (line.contains(keyword)) {
				score++;
			}
		}
	}


}
