package laziness;


import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;



public class initializer{
	public static String title;
	public static String header;

	public static ArrayList<sentenceObject> doc = new ArrayList<sentenceObject>();
	public static HashMap<String, ArrayList<index>> data = new HashMap<String, ArrayList<index>>();
    public static TreeMap<String,ArrayList<index>> sorted_data = new TreeMap<String,ArrayList<index>>();
    public static TreeMap<String,ArrayList<index>> post_data = new TreeMap<String,ArrayList<index>>();
    public static ArrayList<index> sentenceStarts = new ArrayList<index>();

	public static void main(String [ ] args) throws FileNotFoundException{
		if (args[2]!=null) {
			title = args[2];
		}
		if (args[1]!=null) {
			header = args[1];
		}
		if (args[0] != null) {
			read(args[0]);

		} else {
			throw new FileNotFoundException();
		}
	}
	
	private static void read(String article_name){
		String textOfPdf;
	    PDFTextStripper stripper;
		try {
			stripper = new PDFTextStripper();
			File input = new File(article_name);  // The PDF file from where you would like to extract
			PDDocument pdDoc = PDDocument.load(input);
		    textOfPdf = stripper.getText(pdDoc);
		    pdDoc.close();
		    BufferedWriter writer = null;
		    writer = new BufferedWriter( new FileWriter("temp.txt"));
		    writer.write(textOfPdf);
	        writer.close();
	        doc = SentenceBoundary.makeSentences("temp.txt", header);
//	        printDoc();
		    getWordCount(doc);
	        sorted_data = sortByCount(data);
	        post_data = sortByCount(removeStopWords());
		    System.out.println("**SUMMARY\n" + genSummary());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	private static String genSummary() {
		// TODO Auto-generated method stub
        SortedSet<String> keys = (SortedSet<String>) post_data.keySet();
        ArrayList<String> top10 = new ArrayList<String>();
        String[] sentenceHolder = new String[doc.size()+1];
        int count = 1;
        for (String key: keys) {
        	if (count <21) {
        		top10.add(key);
        		System.out.println("word " + count + "=" + key);
        	}
        	count++;
        }
        StringBuilder result = new StringBuilder();
        ArrayList<sentenceObject> mostHitSentences = new ArrayList<sentenceObject>();
        for (String key: top10) { //add first and last sentence that feature the keyword
        	ArrayList<index> wordOccurrences = data.get(key);
        	boolean found = false;
        	for (int i = 1; i < wordOccurrences.size() &&!found; i++) {
        		int currOcurrence = wordOccurrences.get(i).lineNumber;
        		doc.get(currOcurrence-1).score++;
            	String currSentence = doc.get(currOcurrence-1).line;
    			if (currSentence.contains(title)) {
    				currSentence = currSentence.replaceAll(title, "");
            	}
    			if (currSentence.contains(header)) {
    				currSentence = currSentence.replaceAll(header, "");
            	}
    			if (sentenceHolder[currOcurrence]==null) {
                	sentenceHolder[currOcurrence] = currSentence;
//    				System.out.println("we added " + currSentence);
    				found = true;
    			}
        	}
        }
        HashMap<sentenceObject, Integer> preSortSen = new HashMap<sentenceObject, Integer>();
        sentenceComparator bvc =  new sentenceComparator(preSortSen);
        for (sentenceObject currSen: doc) {
        	preSortSen.put(currSen, currSen.score);
        }
        TreeMap<sentenceObject, Integer> postSortSen = new TreeMap<sentenceObject, Integer>(bvc);
        postSortSen.putAll(preSortSen);
        SortedSet<sentenceObject> postSortSenSet = (SortedSet<sentenceObject>) postSortSen.keySet();
        count = 1;
        int senNum= 1;
        for (sentenceObject currSen: postSortSenSet) {
        	System.out.println("new " +currSen.line + "@" + currSen.sentenceNum);
        	if (count >10) {
        		break;
        	} else {
        		if (sentenceHolder[currSen.sentenceNum+1]==null) {
            		sentenceHolder[currSen.sentenceNum+1] = currSen.line;
                	count++;
        		}
        	}
        	senNum++;
        }
        count = 1;
        for (String chosenSentence: sentenceHolder) {
        	if (chosenSentence !=null) {
        		System.out.println(count + ": " + chosenSentence);
        		if (chosenSentence.contains("-") && !chosenSentence.contains("—-")) {
            		chosenSentence = chosenSentence.replaceAll("-\n", "");
//        		} else {
        		}
        		chosenSentence = chosenSentence.replaceAll("\n", " ");
            	result.append(chosenSentence + "\n");
    			count++;
        	}
        	count++;
        }
		return result.toString();
	}

	private static void getWordCount(ArrayList<sentenceObject> doc2) {
		// TODO Auto-generated method stub
		int lineNumber = 1;
		boolean incomplete = false;
		String wordFragment = null;
        for (sentenceObject currLine : doc2) {
        	int wordNumber = 1;
    		currLine.line = currLine.line.replaceAll("[[,.;:!?(){}\\[\\]<>%],]", "");
            String[] wordsInLine = currLine.line.split("[\\s]");
            int maxWordNumber = wordsInLine.length;
            for (String word:wordsInLine){
            	String currWord = word;
            	if (incomplete) {
            		wordFragment = wordFragment + currWord;
            		if (!data.containsKey(wordFragment)) {
                    	ArrayList<index> places = new ArrayList<index>();
                    	index newSpot = new index(lineNumber, wordNumber);
                		places.add(newSpot);
                		data.put(wordFragment, places);
                    } else {
                    	ArrayList<index> pastPlaces = data.get(wordFragment);
                    	index newSpot = new index(lineNumber, wordNumber);
                		pastPlaces.add(newSpot);
                		data.put(wordFragment, pastPlaces);
                    }
            		incomplete = false;
            	} else if (currWord.contains("-")&& currWord.length()==currWord.indexOf("-")+1) {
            		incomplete = true;
            		wordFragment = currWord.replaceAll("-", "");
            	} else if (currWord.toCharArray().length>1) { 
            		if (currWord.contains(".")) {
            			index newSentenceStart;
            			if (wordNumber==maxWordNumber) {
                			 newSentenceStart = new index(lineNumber+1, 1);
            			} else {
                			newSentenceStart = new index(lineNumber, wordNumber + 1);
            			}
            			sentenceStarts.add(newSentenceStart);
            		}
            		currWord = currWord.toLowerCase();
            		if (!data.containsKey(currWord)) {
                    	ArrayList<index> places = new ArrayList<index>();
                    	index newSpot = new index(lineNumber, wordNumber);
                		places.add(newSpot);
                		data.put(currWord, places);
            		} else {
                    	ArrayList<index> pastPlaces = data.get(currWord);
                    	index newSpot = new index(lineNumber, wordNumber);
                		pastPlaces.add(newSpot);
                		data.put(currWord, pastPlaces);
            		}
                }
                wordNumber++;
            }
            lineNumber++;
        }
	}
	
	public static void printAlphabetically() {
        SortedSet<String> keys = new TreeSet<String>(data.keySet());
        for (String key : keys) { 
        	ArrayList<index> value = data.get(key);
        	System.out.println(key + ":" + value);  
        }
	}
	
	public static TreeMap<String,ArrayList<index>> sortByCount(HashMap<String, ArrayList<index>> inputSet) {
        ValueComparator bvc =  new ValueComparator(inputSet);
        TreeMap<String,ArrayList<index>> sorted_map = new TreeMap<String,ArrayList<index>>(bvc);
        sorted_map.putAll(inputSet);
        return sorted_map;
	}	

	public static void printByCount(TreeMap<String, ArrayList<index>> inputSet) {
		System.out.println("\n" + "**Print by count ");	
        SortedSet<String> keys = (SortedSet<String>) inputSet.keySet();
	    for (String key : keys) { 
	    	ArrayList<index> value = data.get(key);
	    	StringBuilder temp = new StringBuilder();
	    	temp.append(key + ": " + value.size() + ": " + value);
	    	System.out.println(temp.toString());  
	    }
    	System.out.println("\n");  

	}
	public static void printDoc() {
		int lineNum = 1;
		for (sentenceObject currSen: doc) {
			System.out.println(lineNum+ ": "+ currSen.line);
			lineNum++;
		}
	}
	
	public static HashMap<String, ArrayList<index>> removeStopWords() {
		HashMap<String, ArrayList<index>> post_data = new HashMap<String, ArrayList<index>>();
		post_data.putAll(data);
		ArrayList<String> stopWords = getStopWords();
        SortedSet<String> keys = (SortedSet<String>) sorted_data.keySet();
	    for (String key : keys) {
	    	if (stopWords.contains(key)) {
	    		post_data.remove(key);
	    	} else {
//	    		System.out.println("ok word: " + key);
	    	}
	    }
	    return post_data;
	}
	
	public static ArrayList<String> getStopWords() {
		BufferedReader br;
		String everything = null;
		try {
			br = new BufferedReader(new FileReader("stopwords.txt"));
			StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        everything = sb.toString();
	        br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> results = new ArrayList<String>();
		for (String junk: everything.split("\\s")) {
			results.add(junk.replace(" ", ""));
		}
		return results;
	}
}

class ValueComparator implements Comparator<String> {

    Map<String, ArrayList<index>> base;
    public ValueComparator(Map<String, ArrayList<index>> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a).size() >= base.get(b).size()) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}

class sentenceComparator implements Comparator<sentenceObject> {

    Map<sentenceObject, Integer> base;
    public sentenceComparator(Map<sentenceObject, Integer> base) {
        this.base = base;
    }

	@Override
	public int compare(sentenceObject o1, sentenceObject o2) {
		// TODO Auto-generated method stub
		if (o1.score > o2.score) {
			return -1;
		} else if (o1.score==o2.score){
			return 1;
		}
		return 0;
	}
}