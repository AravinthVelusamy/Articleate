package website.jonreynolds.jreynolds.articleate;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import org.apmem.tools.layouts.FlowLayout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import textrank.TextRank;

public class ArticleActivity extends AppCompatActivity {
    private final String TAG = "ArticleActivity";
    private WebView webview;
    private TextView authorText;
    private TextView summaryText;
    FlowLayout keywordsContainer;
    private Document document;
    private String summary;
    private String author;
    private String[] keywords;
    private static TextRank tr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webview = (WebView)findViewById(R.id.webView);
        summaryText = (TextView)findViewById(R.id.summary);
        authorText = (TextView) findViewById(R.id.authors);
        keywordsContainer = (FlowLayout)findViewById(R.id.keywords);

        initializeTextRank();
        initializeWebView();
    }

    private void runAnalysis(){
        createAuthorText();
        Element article = getArticleFromDocument();
        if(article!=null){
            summarizeArticle(article);
        }
        else{
            summary = "No article could be extracted.";
        }

        //Show results in-app
        summaryText.setText(summary);
        authorText.setText(author);
        keywordsContainer.removeAllViews();
        if(keywords != null) {
            for (String s : keywords) {
                TextView newButton = new Button(this);
                newButton.setText(s);
                newButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        DialogFragment newFragment = new ResearchDialogFragment();
                        newFragment.show(getFragmentManager(), "research");
                    }
                });
                keywordsContainer.addView(newButton);
            }
        }

    }



    /**
     * Check if TextRank instance exists. If not, create it.
     */
    private void initializeTextRank(){
        //Open raw resources to initialize OpenNLP tools for TextRank
        if(tr==null){
            InputStream sent = getResources().openRawResource(R.raw.en_sent);
            InputStream token = getResources().openRawResource(R.raw.en_token);
            InputStream stop = getResources().openRawResource(R.raw.stopwords);
            InputStream exstop = getResources().openRawResource(R.raw.extended_stopwords);
            try {
                tr = new TextRank(sent, token, stop, exstop);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initialize the WebView with the url passed to the ArticleActivity
     */
    private void initializeWebView(){
        Intent intent = getIntent();
        Uri data = intent.getData();
        String url;
        //If there's data, the intent was from outside of the app
        if(data!=null) {
            String host = data.getHost();
            String path = data.getPath();
            url = host+path;
        }
        //Otherwise it was initialized from the MainActivity
        else {
            url = intent.getStringExtra("url");
        }
        //In case users didn't include http:// or https://
        if(url.indexOf("http://") == -1 && url.indexOf("https://") == -1){
            url = "http://"+url;
        }
        webview.setWebViewClient(new WebViewClient() {
            @Override
            /**
             * Run AsyncTask to fetch page with JSoup
             */
            public void onPageFinished(WebView view, String url) {
                FetchPageTask fetch = new FetchPageTask();
                fetch.execute(url);
            }

        });
        webview.loadUrl(url);
    }

    /**
     * An AsyncTask to fetch a page and its information with JSoup
     */
    private class FetchPageTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... url) {
            Document doc = null;
            try {
                Log.v("JSoup", "Attempting to connect to "+ url[0]);
                doc = Jsoup.connect(url[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return doc;
        }

        protected void onPostExecute(Document doc) {
            //New page, clear out past keywords
            keywords=null;
            //Call jsoupAnalysis
            if(doc!=null) {
                Log.v(TAG, "Loaded the page.");
                ArticleActivity.this.document = doc;
                runAnalysis();

            }
            else{
                Log.v(TAG, "Couldn't load page.");
                summary = "Page couldn't be loaded for info extraction.";
            }
        }
    }


    /**
     * Perform needed information scraping with the JSoup Document
     */
    private Element getArticleFromDocument(){
        //Start specific. Some websites have smaller psuedo articles at the top of the page
        Element article = document.select("main").select("article").first();
        if(article == null)
            article = document.select("article").first();
        if(article == null)
            article = document.getElementsByAttributeValueContaining("class", "article").first();
        if(article == null)
            article = document.getElementsByAttributeValueContaining("class", "story").first();
        return article;
    }

    /**
     * Find author text based on common class names for authors
     */
    private void createAuthorText(){
        Element authorContainer = document.getElementsByAttributeValueContaining("class", "byline").first();
        if(authorContainer == null) {
            authorContainer = document.getElementsByAttributeValueContaining("class", "author").first();
        }
        if(authorContainer!=null) {
            Log.v("Author Information", authorContainer.text());
            author = authorContainer.text();
        }
        else {
            author = "Couldn't extract author data.";
        }
    }

    /**
     * Run TextRank algorithms on the article
     */
    private void summarizeArticle(Element article){
        String articleText = createArticleText(article);
        TextView summaryText = (TextView)findViewById(R.id.summary);
        if(articleText != null && articleText != ""){
            ArrayList<TextRank.SentenceVertex> rankedSentences = tr.sentenceExtraction(articleText);
            ArrayList<TextRank.TokenVertex> rankedTokens = tr.keywordExtraction(articleText);
            summary = rankedSentences.get(0).getSentence();
            for(int i = 0; i < 5 && i < rankedSentences.size(); i ++){
                Log.v("Ranked Sentence #" + (i+1), rankedSentences.get(i).getSentence());
            }
            keywords = new String[8];
            for(int i = 0; i < rankedTokens.size() && i < keywords.length; i++){
                TextRank.TokenVertex tv = rankedTokens.get(i);
                keywords[i] = tv.getToken();
            }
            //Save to cache
            try {
                File cachedSummaries= new File(getCacheDir(), "summaries.txt");
                //Open PrintWriter in append mode.
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(cachedSummaries, true)));
                pw.println(summary);
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            summary = "Unable to process this article";
        }
    }

    /**
     * Preprocess and correct the article text extracted via JSoup.
     * @param article JSoup article element
     */
    private String createArticleText(Element article){
        String articleText = "";
        Elements articleParagraphs = article.select("p");
        String articleParagraphText = articleParagraphs.text();
        //If the article <p> elements don't make up most of the text in the article
        if((double)article.text().length()*0.4 >= articleParagraphText.length()){
            //Hope that they're under the tag paragraph (prime example, CNN, who just changed their article HTML layout this week)
            articleParagraphs = article.getElementsByAttributeValueContaining("class", "paragraph");
        }
        for(int i = 0; i < articleParagraphs.size(); i++){
            Element paragraph = articleParagraphs.get(i);
            //End all sentences with periods so as to ensure sentence separation
            //Otherwise, sentences will appear concatenated
            String pText = paragraph.text();
            if(pText.length()>0 && pText.charAt(pText.length()-1) != ' '){
                if(pText.charAt(pText.length()-1)== '.' || pText.charAt(pText.length()-1)=='"')
                    pText += " ";
                else
                    pText += ". ";
            }
            articleText += pText;
        }
        return articleText;
    }

}
