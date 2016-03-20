package website.jonreynolds.jreynolds.articleate;

import java.util.Random;

import edu.stanford.nlp.util.CoreMap;

/**
 * A node containing a sentence and its information, such as score, tokens, parts-of-speech, etc.
 * Created by jonathanreynolds on 3/19/16.
 */
public class SentenceVertex {

    private String sentence;
    private CoreMap annotatedSentence;
    private double score;

    /**
     * Constructor for SentenceVertex. Initializes fields.
     * @param c
     */
    public SentenceVertex(CoreMap c){
        annotatedSentence = c;
        sentence = c.toString();
        //Initialize to a random score between 1 and 10, as stated by Mihalcea in the TextRank paper
        score = new Random().nextDouble()*10.0;
    }


    /**
     * Getter for sentence field
     * @return sentence stored in this vertex
     */
    public String getSentence() {
        return sentence;
    }

    /**
     * Getter for the annotatedSentence field
     * @return annotatedSentence
     */
    public CoreMap getAnnotatedSentence() {
        return annotatedSentence;
    }

    /**
     * Setter for the annotatedSentence field
     * Note: this also updates the sentence field
     * @param annotatedSentence the annotatedSentence to update the field
     */
    public void setAnnotatedSentence(CoreMap annotatedSentence) {
        this.annotatedSentence = annotatedSentence;
        this.sentence = annotatedSentence.toString();
    }

    /**
     * Getter for the score field
     * @return the score for this vertex
     */
    public double getScore() {
        return score;
    }

    /**
     * Setter for the score field of this vertex
     * @param score the score to be used to update the score field
     */
    public void setScore(double score) {
        this.score = score;
    }
}
