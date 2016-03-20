package website.jonreynolds.jreynolds.articleate;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.*;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.Properties;
import java.util.List;

/**
 * Created by jonathanreynolds on 3/19/16.
 */
public class TextRankDemo {
    public static void main(String[] args){

        SimpleDirectedWeightedGraph<SentenceVertex, DefaultWeightedEdge> graph =  new SimpleDirectedWeightedGraph<SentenceVertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text in the text variable
        String text = "The sharply worded response came Friday evening after Trump took his attacks against Kelly to a new level, referring to her as \"sick\" and \"overrated.\" Trump also called for his supporters to boycott her Fox News show.\n" +
                "\"Donald Trump's vitriolic attacks against Megyn Kelly and his extreme, sick obsession with her is beneath the dignity of a presidential candidate who wants to occupy the highest office in the land,\" the network said in its statement.\n" +
                "\"Megyn is an exemplary journalist and one of the leading anchors in America — we're extremely proud of her phenomenal work and continue to fully support her throughout every day of Trump's endless barrage of crude and sexist verbal assaults,\" the statement continued. \"As the mother of three young children, with a successful law career and the second highest rated show in cable news, it's especially deplorable for her to be repeatedly abused just for doing her job.\"\n" +
                "Related: Megyn Kelly says Bill O'Reilly didn't have her back on Trump attacks\n" +
                "Trump, who for seven months has tried to impugn the credibility of the Fox News host, had raised the stakes in a tweet on Friday.\n" +
                "\"Everybody should boycott the @megynkelly show. Never worth watching. Always a hit on Trump!\" the GOP frontrunner tweeted. \"She is sick, & the most overrated person on tv.\"\n" +
                "\n" +
                "Trump's campaign kept up the barrage Friday night, putting out a statement that said in part, \"Fox News has begged Mr. Trump to do a prime time special to be broadcast on the Fox Network, not cable, with Megyn Kelly. He has turned them down.\"\n" +
                "Fox recently announced plans for Kelly to do a special on the broadcast network in May, but a Fox News spokesperson said Friday night, \"No one associated with Megyn Kelly's upcoming Fox Broadcasting special reached out to Donald Trump for an interview.\"\n" +
                "A Fox representative made clear that neither Kelly nor Bill Geddie, the executive producer of the Fox program airing in May, ever reached out to Trump for an interview.\n" +
                "Kelly has kept quiet throughout the many attacks by Trump, a fact the Trump campaign noted in its Friday night statement.\n" +
                "\"Unlike Megyn Kelly, who resorts to putting out statements via Fox News, Mr. Trump will continue to defend himself against the inordinate amount of unfair and inaccurate coverage he receives on her second-rate show each night,\" the campaign said.\n" +
                "This was the most intense attack in a campaign against Kelly that Trump began last August, when he complained that she had treated him unfairly during the first Republican presidential debate.\n" +
                "Since then, Trump has frequently accused Kelly of bias and unfair coverage, often with little to no evidence of any specific offense.\n" +
                "In the run-up to what would have been their second meeting, at the Fox News debate on January 28, Trump said that Kelly shouldn't be allowed to serve as moderator. Fox News shot back a tongue-in-cheek response in which it accused Trump of being afraid of Kelly, and therefore unfit to deal with foreign leaders. Trump decided to skip the debate.\n" +
                "Trump and Kelly were reunited at the third Fox News debate, on March 3, where Trump struck a much more convivial tone. Kelly once again proved to be Trump's toughest adversary that night, asking tough and well-researched questions that won her the praise of journalists and observers on both sides of the aisle.\n" +
                "It's not exactly clear what precipitated Trump's latest round of name-calling, nor his call for a boycott. Throughout the week, Trump has been trying to brand Kelly as \"crazy\" and obsessed with criticizing him.\n" +
                "Related: Donald Trump says he's 'ready' for Megyn Kelly\n" +
                "In the eyes of many Trump critics, it is he who appears to be obsessed with Kelly. And while it's hard to see how such attacks could have much success with the American public, it's also true that a plurality of the country's Republicans have so far supported the man making them.";


                // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
                System.out.println(sentence);
                // traversing the words in the current sentence
                // a CoreLabel is a CoreMap with additional token-specific methods
                for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                        // this is the text of the token
                        String word = token.get(TextAnnotation.class);
                        // this is the POS tag of the token
                        String pos = token.get(PartOfSpeechAnnotation.class);
                        // this is the NER label of the token
                        String ne = token.get(NamedEntityTagAnnotation.class);
                }

        }

    }
}
