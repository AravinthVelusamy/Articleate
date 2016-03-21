package textrank;

import android.util.Log;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * Singleton to perform a modified TextRank-algorithm on a body of text
 * Created by jonathanreynolds on 3/19/16.
 */
public class TextRank {
    private SimpleWeightedGraph<SentenceVertex, DefaultWeightedEdge> graph;
    private SentenceDetectorME sdetector;
    private Tokenizer tokenizer;
    private final double convergenceThreshold = 0.0001;
    private final double probability = 0.85;
    private final HashSet<String> stopwords = new HashSet<String>();


    /**
     * Initialize TextRank with three inputstreams corresponsing to training information
     * for Sentence extraction, Tokenization, and Part of Speech recognition, respectively.
     */
    public TextRank(InputStream sent, InputStream token, InputStream stop) throws IOException {
        init(sent, token, stop);
    }

    /**
     * Initialization method. Creates a new graph and initializes the StanfordNLPCore pipeline if needed
     * @param sent
     * @param token
     */
    private void init(InputStream sent, InputStream token, InputStream stop) throws IOException {
        // creates a new SentenceDetector, POSTagger, and Tokenizer
        SentenceModel sentModel = new SentenceModel(sent);
        sent.close();
        sdetector = new SentenceDetectorME(sentModel);
        TokenizerModel tokenModel = new TokenizerModel(token);
        token.close();
        tokenizer = new TokenizerME(tokenModel);
        BufferedReader br = new BufferedReader(new InputStreamReader(stop));
        String line;
        while ((line = br.readLine()) != null) {
            stopwords.add(line);
        }
    }

    /**
     * Creates a TextRank graph from an article
     * @param article a String argument containing a body of text
     */
    private void createGraph(String article){
        graph =  new SimpleWeightedGraph<SentenceVertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        String[] sentences = sdetector.sentDetect(article);
        //Initialize graph with a vertex for each sentence
        for(String sentence: sentences){
            SentenceVertex sv = new SentenceVertex(sentence, tokenizer.tokenize(sentence));
            graph.addVertex(sv);
        }
        //Create edges
        for(SentenceVertex v1: graph.vertexSet()){
            for(SentenceVertex v2: graph.vertexSet()){
                if(v1 != v2){
                    DefaultWeightedEdge dwe = graph.addEdge(v1, v2);
                    //If the edge hasn't yet been added
                    if(dwe != null) {
                        double weight = calculateSimilarity(v1, v2);
                        if(weight > 0.0) {
                            graph.setEdgeWeight(dwe, weight);
                        }
                        else{
                            graph.removeEdge(dwe);
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculates the edge-weight between two vertices according to the algorithm
     * given in section 4.1 of the TextRank paper to find similarities between sentences.
     * @param v1 the first vertex
     * @param v2 the vertex to compare for similarities
     * @return a similarity score
     */
    private double calculateSimilarity(SentenceVertex v1, SentenceVertex v2){
        String[] tokens1 = v1.getTokens();
        String[] tokens2 = v2.getTokens();
        //Loop through tokens in first sentence
        double similarities = 0;
        for (int i = 0; i < tokens1.length; i++) {
            String word1 = tokens1[i];
//            String pos1 =  tags1[i];

            //Loop through tokens in second sentence
            for (int j = 0; j < tokens2.length; j++) {
                String word2 = tokens2[j];

                if(word1.equals(word2) && !stopwords.contains(word1.toLowerCase())){
                    similarities += 1;
                }
            }
        }
        int numWordsInSentence1 = tokens1.length;
        int numWordsInSentence2 = tokens2.length;
        double similarity = similarities/(Math.log(numWordsInSentence1)+Math.log(numWordsInSentence2));
        return similarity;
    }

    /**
     * Calculate a ranking score for a given vertex according to the algorithm in
     * section 2.2 of the PageRank paper
     * @param vi vertex to calculate score for
     * @return calculated score
     */
    private double calculateScore(SentenceVertex vi){
        double scorei = (1.0 - probability);
        double sum = 0;
        //Iterate over edges of vi
        for(DefaultWeightedEdge eij: graph.edgesOf(vi)){
            double numerator = graph.getEdgeWeight(eij);
            //Get other vertex
            SentenceVertex vj = graph.getEdgeSource(eij);
            if(vi == vj){
                vj = graph.getEdgeTarget(eij);
            }
            //Sum the denominator
            double denominator = 0;
            for(DefaultWeightedEdge ejk: graph.edgesOf(vj)){
                denominator += graph.getEdgeWeight(ejk);
            }
            double scorej = vj.getScore();
            sum += (numerator/denominator)*scorej;
        }
        scorei += probability*sum;
        return scorei;
    }

    /**
     * Method that repeatedly calculates scores until error is below the threshold
     * recommended in the PageRank paper, 0.001 (Supposing that this is percent error of 0.1%)
     */
    private void convergeScores(){
        double error = 1;
        int iterations = 0;
        while(error > convergenceThreshold){
            for(SentenceVertex v : graph.vertexSet()){
                double newScore = calculateScore(v);
                double lastScore = v.getScore();
                double scoreError = Math.abs(lastScore - newScore)/newScore;
                error += scoreError;
                v.setScore(newScore);
            }
            error = error/(double)(graph.vertexSet().size());
            iterations +=1;
        }
        Log.v("TextRank", iterations+"");
    }

    /**
     * Client method that returns the TextRank-processed sentence list.
     * @param text Text to be processed
     * @return Ordered ArrayList of sentence Strings

     */
    public ArrayList<SentenceVertex> sentenceExtraction(String text){
        createGraph(text);
        convergeScores();
        ArrayList<SentenceVertex> sorted = new ArrayList<SentenceVertex>();
        sorted.addAll(graph.vertexSet());
        Collections.sort(sorted, new SentenceVertexComparator());
        return sorted;
    }

    /**
     * Custom comparator for sorting SentenceVertices
     */
    private static class SentenceVertexComparator implements Comparator<SentenceVertex> {
        @Override
        public int compare(SentenceVertex lhs, SentenceVertex rhs) {
            return Double.compare(rhs.getScore(), lhs.getScore());
        }
    }

    /**
     * A node containing a sentence and its information, such as score, tokens, parts-of-speech, etc.
     * Created by jonathanreynolds on 3/19/16.
     */
    public class SentenceVertex {

        private String sentence;
        private String[] tokens;
        private double score;

        /**
         * Constructor for SentenceVertex. Initializes fields.
         * @param s String sentence
         */
        public SentenceVertex(String s, String[] t){
            sentence = s;
            tokens = t;
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
         * Returns the tokens of this sentence
         * @return Array of token strings
         */
        public String[] getTokens() {
            return tokens;
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

}
