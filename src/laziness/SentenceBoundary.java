package laziness;

import java.io.*;
import java.util.*;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;

public class SentenceBoundary {

    static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL  = new MedlineSentenceModel();
    static final ArrayList<sentenceObject> sentenceList = new ArrayList<sentenceObject>();

    public static ArrayList<sentenceObject> makeSentences(String input) {
    	File file = new File(input);
    	String text = null;
		try {
			text = Files.readFromFile(file,"ISO-8859-1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	List<String> tokenList = new ArrayList<String>();
    	List<String> whiteList = new ArrayList<String>();
    	Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(),0,text.length());
    	tokenizer.tokenize(tokenList,whiteList);

    	String[] tokens = new String[tokenList.size()];
    	String[] whites = new String[whiteList.size()];
    	tokenList.toArray(tokens);
    	whiteList.toArray(whites);
    	int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens,whites);

    	
    	if (sentenceBoundaries.length < 1) {
    	    System.out.println("No sentence boundaries found.");
    	    return null;
    	}
    	int sentStartTok = 0;
    	int sentEndTok = 0;
    	for (int i = 0; i < sentenceBoundaries.length; ++i) {
    	    sentEndTok = sentenceBoundaries[i];
    	    StringBuilder currSen = new StringBuilder();
    	    for (int j=sentStartTok; j<=sentEndTok; j++) {
    	    	currSen.append(tokens[j] + whites[j+1]);
    	    }
        	currSen.append("\n");

        	sentenceObject currSentenceObject = new sentenceObject(currSen.toString(), i);
           	sentenceList.add(currSentenceObject);
    	    sentStartTok = sentEndTok+1;
    	}    
    return sentenceList;
    }
}