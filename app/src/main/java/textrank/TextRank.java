package textrank;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
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
    private POSTaggerME tagger;
    private Tokenizer tokenizer;
    private final double convergenceThreshold = 0.0001;
    private final double probability = 0.85;
    private static TextRank instance;
    static {
        try {
            instance = new TextRank();
        } catch (IOException e){
        }
    }


    /**
     * Initialize TextRank
     */
    private TextRank() throws IOException {
        init();
    }

    /**
     * Get TextRank singleton
     * @return the singleton TextRank instance
     */
    public static TextRank getInstance(){
        return instance;
    }

    /**
     * Initialization method. Creates a new graph and initializes the StanfordNLPCore pipeline if needed
     */
    private void init() throws IOException {
        // creates a new SentenceDetector, POSTagger, and Tokenizer
        if(sdetector == null || tagger == null || tokenizer == null) {
            System.out.println("Working Directory = " +
                    System.getProperty("user.dir"));
            InputStream is = new FileInputStream("app/src/main/assets/en-sent.bin");
            SentenceModel sentModel = new SentenceModel(is);
            is.close();
            sdetector = new SentenceDetectorME(sentModel);
            is = new FileInputStream("app/src/main/assets/en-pos-maxent.bin");
            POSModel posModel = new POSModel(is);
            is.close();
            tagger = new POSTaggerME(posModel);
            is = new FileInputStream("app/src/main/assets/en-token.bin");
            TokenizerModel tokenModel = new TokenizerModel(is);
            tokenizer = new TokenizerME(tokenModel);
            is.close();
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
            SentenceVertex sv = new SentenceVertex(sentence);
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
//                        System.out.println(v1.getSentence());
//                        System.out.println(v2.getSentence());
//                        System.out.println(weight);
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
        String[] tags1 = v1.getTags();
        String[] tokens2 = v2.getTokens();
        //Loop through tokens in first sentence
        double similarities = 0;
        for (int i = 0; i < tokens1.length; i++) {
            String word1 = tokens1[i];
            String pos1 =  tags1[i];

            //Loop through tokens in second sentence
            for (int j = 0; j < tokens2.length; j++) {
                String word2 = tokens2[j];

                if(word1.equals(word2) && (pos1.indexOf("JJ")==0 || pos1.indexOf("NN")==0 || pos1.indexOf("VB")==0)){
                    similarities += 1;
//                    System.out.println(pos1 + " " + word1);
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
    }

    /**
     * Client method that returns the TextRank-processed sentence list.
     * @param text Text to be processed
     * @return Ordered ArrayList of sentence Strings

     */
    public ArrayList<String> sentenceExtraction(String text){
        createGraph(text);
        convergeScores();
        ArrayList<SentenceVertex> sorted = new ArrayList<SentenceVertex>();
        sorted.addAll(graph.vertexSet());
        Collections.sort(sorted, new SentenceVertexComparator());
        ArrayList<String> ret = new ArrayList<String>();
        for(SentenceVertex v: sorted){
            ret.add(v.getSentence());
        }
        return ret;

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

    public static void main(String[] args){
        //tests
        String text = "Scrambling to respond to the success of Google DeepMind’s world-beating Go program AlphaGo, South Korea announced on 17 March that it would invest $863 million (1 trillion won) in artificial-intelligence (AI) research over the next five years. It is not immediately clear whether the cash represents new funding, or had been previously allocated to AI efforts. But it does include the founding of a high-profile, public–private research centre with participation from several Korean conglomerates, including Samsung, LG Electronics and Hyundai Motor, as well as the technology firm Naver, based near Seoul.\n" +
                "“Thanks to the ‘AlphaGo shock’, we have learned the importance of AI before it is too late”\n" +
                "The timing of the announcement indicates the impact in South Korea of AlphaGo, which two days earlier wrapped up a 4–1 victory over grandmaster Lee Sedol in an exhibition match in Seoul. The feat was hailed as a milestone for AI research. But it also shocked the Korean public, stoking widespread concern over the capabilities of AI, as well as a spate of newspaper headlines worrying that South Korea was falling behind in a crucial growth industry.\n" +
                "South Korean President Park Geun-hye has also announced the formation of a council that will provide recommendations to overhaul the nation’s research and development process to enhance productivity. In her 17 March speech, she emphasized that “artificial intelligence can be a blessing for human society” and called it “the fourth industrial revolution”. She added, “Above all, Korean society is ironically lucky, that thanks to the ‘AlphaGo shock’, we have learned the importance of AI before it is too late.”\n" +
                "Korean scientists told Nature that the AI research institute was already in the planning stages, and was originally intended to open in 2017. However, AlphaGo’s success prompted the government to accelerate plans for the institute. Science ministry official Kim Yong-soo said that they hope it will open as soon as possible, but that the exact date depends on the companies involved. The institute is likely to be located in Pangyo, a technology city just south of Seoul.\n" +
                "Korean AI researchers who spoke to Nature expressed concern that the new initiative is short-sighted, and a knee-jerk reaction. “It will help, but more consistent support is required,” said Kwon Hyuk-chul, an AI researcher at Pusan National University in Busan.\n" +
                "“I’m very sorry to hear that the government is interested in investing a lot of money in mostly industry, not universities,” said one machine-learning professor at a leading Korean university, who requested anonymity in order to talk openly about the policy. “Industry will probably get some useful applications for making some product, but they are basically not interested in the research itself.”\n" +
                "South Korea already funds two high-profile AI projects: Exobrain, which is intended to compete with IBM’s Watson computer, and Deep View, a computer vision project. The Korean science ministry has not yet responded to queries from Nature about how much of the pledged 1 trillion won is already allocated to those projects.\n" +
                "The international Go community has probably not seen the last Go showdown between a human and a machine. Two days before the AlphaGo match began, deep-learning start-up firm NovuMind challenged the Chinese professional Go player Ke Jie to a computer match. Based in California's Silicon Valley, NovuMind was founded in 2015 by Ren Wu, a former deep-learning researcher at Baidu, the Chinese web-services giant.\n" +
                "The terms of the match have yet to be formalized, says Wu, but he is confident that they will be signed soon. The 18-year-old Ke Jie defeated Lee Sedol in an international tournament held in China in January, and is currently the world’s top-ranked player at GoRatings.org — just ahead of AlphaGo.\n" +
                "The developers of two other Go-playing programs, called Zen and CrazyStone, are also working on enhanced deep-learning versions.";
        String text2 = "Tanvir Hassan Zoha, 34, security researcher, has gone missing just days after accusing Bangladesh's central bank officials of negligence, which facilitated the theft of over $81 million from the country's oversea accounts.\n" +
                "On February 5, 2016, hackers accessed the accounts of Bangladesh's central bank at the US Federal Reserve Bank in New York and tried to steal $1 billion dollars. Their attempt to transfer the money was thwarted by a simple typo, but not before managing to take $81 million.\n" +
                "In the investigation that followed, security researchers blamed malware and a faulty printer but at the same time said that the Bangladesh central bank officials were also to blame because of weak security procedures. The bank's governor and two deputy governors had to quit their jobs after the scandal.\n" +
                "In a weird turn of events, one of the security researchers who voiced their criticism at the central bank’s security measures disappeared on Wednesday night.\n" +
                "Family members are saying that Zoha met with a friend at 11:30 PM on Wednesday night, March 16. While coming home, a jeep pulled in front of their auto-rickshaw, and men separated the two, putting them in two different cars.\n" +
                "Zoha's friend was dumped somewhere in the city (Dhaka) and was able to get home by 02:00 AM, the next day. He then contacted Zoha's family, who said the security researcher never came home.\n" +
                "The next day, family members tried to report the researcher missing, but police officers just kept redirecting them from one police station to another until the family gave up and contacted the media for help.\n" +
                "The media's reports angered Bangladesh's population, who was already annoyed by the fact that government officials almost lost $1 billion of their money. The following day, government officials put out a statement on Zoha's disappearance but did not say much outside the fact that they opened an investigation.\n" +
                "According to BDNews24, Zoha was a former collaborator of Bangladesh's ICT (Information and Communication Technology) Division and worked with various government agencies in the past. It appears that his comments about the Bangladesh central bank cyber-heist were made working as a \"shadow investigator\" for a security company that family members declined to name.\n" +
                "Answering questions about his own investigation into the central bank's cyber-heist, Zoha said that the \"database administrator of the [Bangladesh Bank] server cannot avoid responsibility for such hacking\" and that he \"noticed apathy about the [server's] security system.\"\n" +
                "Family members suspect that these comments Zoha made to the press on March 11 are the cause of his disappearance.\n" +
                "UPDATE: On March 19, three days after Zoha was kidnapped, police still have no clues. The Bangla Tribune has also found an interview (in Bengali) in which Zoha had criticized bank officials.";
        String text3 = "Amish scholar Don Kraybill calls it a riddle, or a paradox.\n" +
                "How can the Amish be such successful entrepreneurs today, when they end their formal education at eighth grade and forswear so much of the paraphernalia of modern life?\n" +
                "That they succeed is indisputable: The failure rate of Amish startups in the first five years is less than 10 percent, versus 65 percent for businesses in North America overall.\n" +
                "Many Amish retailers cater to mainstream customers, and do so with sophistication. Kraybill likes to cite Emma’s Gourmet Popcorn, which pegs promotions to popular holidays and offers online ordering on a modern, well-designed website.\n" +
                "Bowls of the flavored treat were part of a buffet preceding a talk on Amish business that Kraybill gave recently at Elizabethtown College. Kraybill, who retired from teaching at Elizabethtown last year, remains an active scholar at the college’s Young Center for Anabaptist and Pietist Studies.\n" +
                "2,000-plus Amish firms\n" +
                "Over the past few decades, Lancaster County’s Amish have undergone a “mini-Industrial Revolution,” Kraybill said. High land prices plus a population explosion limited farming opportunities for rising generations, fueling a turn to carpentry, small manufacturing and other enterprises.\n" +
                "Today, there are more than 2,000 Amish businesses in the Lancaster area, Kraybill said. Fewer than one-third of local Amish households still rely on farming as the primary source of income.\n" +
                "Alan Dakey is president of the Bank of Bird-in-Hand. Its single branch sits at the corner of North Ronks Road and Route 340, and a majority of its clientele are Plain-sect members.\n" +
                "Many of the bank’s customers farm but also operate nonfarm side businesses, Dakey said.\n" +
                "Remarkably, the bank has yet to record a single 30-day delinquency on a loan since its December 2013 opening — a tribute to its customers’ frugality and money-management capabilities.\n" +
                "Amish aren’t opposed to borrowing per se, but “they want to use it constructively,” Dakey said.\n" +
                "In his talk, Kraybill identified 12 factors he sees contributing to Amish business success.\n" +
                "While some are integral to the culture, many, in principle, could be adopted by anyone.\n" +
                "Here they are:\n" +
                "1. Apprenticeship: Apprenticeship is a training system that mainstream society has largely abandoned, Kraybill said. But in Amish society, teens learn trades by working alongside their parents or other adults. Kraybill described once watching a 13-year-old fix a piece of hydraulic machinery. He had already spent years in his father’s shop and knew what he was doing. “That’s apprenticeship,” Kraybill said.\n" +
                "2. Limited education: Because Amish finish school with eighth grade, they can’t be drawn off into law, medicine or other professions that require extended formal education. The two basic Amish career tracks are farming and small business, so that’s where the best and brightest end up, bringing their ingenuity and drive with them.\n" +
                "3. Work ethic: Amish are brought up in a culture that values hard work. It’s seen as integral to life, and children are brought up from an early age to pitch in to help their family and community.\n" +
                "4. Smallness: “Bigness spoils everything,” Kraybill said an Amishman once told him. With many small companies instead of a few dominant ones, individual Amish have scope to express their entrepreneurial spirit. There’s little social distance between business owners and employees, and owners stay personally invested in their enterprises.\n" +
                "5. Low overhead: Amish businesses don’t have air conditioning or luxurious offices. If the business has an office, Kraybill said he usually finds it empty, because the owner is out working on the shop floor.\n" +
                "6. Social capital: Information propagates rapidly through Amish communities’ social networks. Job seekers and companies with vacancies can put the word out and find each other easily. Transaction costs are low because everyone shares the same values and trust is high.\n" +
                "7. The paradox of technology: The Amish taboos on technology stimulate innovation and “hacking” as entrepreneurs find workarounds, Kraybill said. The culture distinguishes between using and owning technology — that’s why it’s OK for a business like Emma’s Gourmet Popcorn to contract with a website developer, or for Amish carpenters to journey to job sites in “Amish taxis” driven by their neighbors.\n" +
                "8. Infrastructure: New Amish companies operate within a framework created by their fellow businesspeople. They enjoy access to a well-established network of products and services tailored to the culture and its unique needs and restrictions.\n" +
                "9. Regional markets: The tens of millions of people in the mid-Atlantic region comprise a “phenomenal external market” for the Amish, Kraybill said. There are more than 50 Amish markets between Annapolis and New York City, many catering to urban dwellers hungering for a taste of rural life. Ben Riehl, who owns a stand at the Markets at Shrewsbury in southern York County, said half of his Saturday customers drive up from Maryland, and he estimates they account for half his weekly sales.\n" +
                "10. Niche markets: Gourmet popcorn is a niche product. So are dried flower arrangements, carriage restoration, handmade furniture and horse-drawn farm machinery. Many Amish specialize in organic or free-range farming, Dakey said. Kraybill said he knows an Amish farmer who raises camels, having discovered camel milk commands a premium price.\n" +
                "11. Amish “branding”: For many Americans, the term “Amish” has strong positive associations: honesty, simplicity, old-fashioned virtue. Businesses can partake in those associations simply by being Amish. For Riehl, there's a big difference between overt image-building and the kind of trust that accrues when Amish business owners serve their customers with integrity: The latter “is a reputation that was earned, not a brand that was bought.” \n" +
                "12. Payroll costs: Amish employees in Amish businesses are exempt from mainstream companies’ Social Security, health insurance and pension mandates. Though that keeps costs down, the impact is often exaggerated, Amish business owners say. They say they still have to pay into Amish Aid, the community’s mutual-aid fund, and they have responsibility for payroll taxes and benefits for non-Amish employees, so the difference isn’t all that great.";

        TextRank tr = TextRank.getInstance();
        ArrayList<String> sent1 = tr.sentenceExtraction(text);
        ArrayList<String> sent2 = tr.sentenceExtraction(text2);
        ArrayList<String> sent3 = tr.sentenceExtraction(text3);
        System.out.println(sent1.get(0));
        System.out.println(sent2.get(0));
        System.out.println(sent3.get(0));
    }
    /**
     * A node containing a sentence and its information, such as score, tokens, parts-of-speech, etc.
     * Created by jonathanreynolds on 3/19/16.
     */
    private class SentenceVertex {

        private String sentence;
        private String[] tokens;
        private String[] tags;
        private double score;

        /**
         * Constructor for SentenceVertex. Initializes fields.
         * @param s String sentence
         */
        public SentenceVertex(String s){
            sentence = s;
            tokens = tokenizer.tokenize(s);
            tags = tagger.tag(tokens);
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
         * Returns the tags of the tokens of this sentence
         * @return Array of POS tags
         */
        public String[] getTags() {
            return tags;
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
