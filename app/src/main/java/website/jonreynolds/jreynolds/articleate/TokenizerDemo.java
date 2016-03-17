package website.jonreynolds.jreynolds.articleate;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;

public class TokenizerDemo {

    public static void main(String[] args) throws IOException {
        String[] examples = {"Bla bla bla bla"};
        for (String s : examples) {
            Reader reader = new StringReader(s);
            // option #1: By sentence.
            DocumentPreprocessor dp = new DocumentPreprocessor(reader);
            for (List<HasWord> sentence : dp) {
                System.out.println(sentence);
            }
            // option #2: By token
            PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(s),
                    new CoreLabelTokenFactory(), "");
            while (ptbt.hasNext()) {
                CoreLabel label = ptbt.next();
                System.out.println(label);
            }
        }
    }
}