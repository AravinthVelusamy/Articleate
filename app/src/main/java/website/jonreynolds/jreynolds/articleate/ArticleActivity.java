package website.jonreynolds.jreynolds.articleate;

import android.content.Intent;
import android.media.session.MediaSession;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apmem.tools.layouts.FlowLayout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import textrank.TextRank;

public class ArticleActivity extends AppCompatActivity {
    private final String TAG = "ArticleActivity";
    private WebView webview;
    private String url;
    private String articleText;
    private static TextRank tr;

    /**
     * Run TextRank algorithms on the article
     */
    private void summarizeArticle(){
        TextView summaryText = (TextView)findViewById(R.id.summary);
        if(articleText != null && articleText != ""){
            ArrayList<TextRank.SentenceVertex> rankedSentences = tr.sentenceExtraction(articleText);
            ArrayList<TextRank.TokenVertex> rankedTokens = tr.keywordExtraction(articleText);
            String summary = rankedSentences.get(0).getSentence();
            summaryText.setText(summary);
            populateKeywordsContainer(rankedTokens);
        }
        else{
            summaryText.setText("Unable to process this article");
        }
    }

    /**
     * Get top 8 keywords and careate buttons for them
     */
    private void populateKeywordsContainer(ArrayList<TextRank.TokenVertex> rankedTokens){
        FlowLayout keywordsContainer = (FlowLayout)findViewById(R.id.keywords);
        keywordsContainer.removeAllViews();
        for(int i = 0; i < rankedTokens.size() && i < 8; i++){
            TextRank.TokenVertex tv = rankedTokens.get(i);
            TextView newButton = new Button(this);
            newButton.setText(tv.getToken());
            keywordsContainer.addView(newButton);
        }
    }

    /**
     * Perform needed information scraping with a JSoup Document
     * @param doc JSoup document initialized on current page
     */
    private void jsoupAnalysis(Document doc){
        //Connection succeeded
        if(doc != null) {
            //Try different methods of article retrieval
            Element article = doc.select("main").select("article").first();
            if(article == null)
                article = doc.select("article").first();
            //If there exists an article tag in the document
            if(article!=null) {
                createArticleText(article);
                createAuthorText(article);
            }
        }
        else {
            TextView authorTextView = (TextView) findViewById(R.id.authors);
            authorTextView.setText("Couldn't extract author data.");
        }
        summarizeArticle();
    }

    /**
     * Preprocess and correct the article text extracted via JSoup.
     * @param article JSoup article element
     */
    private void createArticleText(Element article){
        articleText = "";
        Elements articleParagraphs = article.select("p");
        for(int i = 0; i < articleParagraphs.size(); i++){
            Element paragraph = articleParagraphs.get(i);
            //End all sentences with periods so as to ensure sentence separation
            //Otherwise, sentences will appear concatenated
            String pText = paragraph.text();
            if(pText.length()>0 && pText.charAt(pText.length()-1) != ' '){
                if(pText.charAt(pText.length()-1)== '.')
                    pText += " ";
                else
                    pText += ". ";
            }
            articleText += pText;
        }
    }

    private void createAuthorText(Element article){
        Element authorContainer = article.getElementsByAttributeValueContaining("class", "author").first();
        if(authorContainer == null)
            authorContainer =article.getElementsByAttributeValueContaining("class", "byline").first();
        TextView authorTextView = (TextView) findViewById(R.id.authors);
        if(authorContainer!=null) {
            authorTextView.setText(authorContainer.text());
        }
        else {
            authorTextView.setText("Couldn't extract author data.");
        }
    }

    /**
     * Initialize the WebView with the url passed to the ArticleActivity
     */
    private void initializeWebView(){
        Intent intent = getIntent();
        Uri data = intent.getData();
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
        //In case users didn't include http://
        if(url.indexOf("http://") == -1 && url.indexOf("https://") == -1){
            url = "http://"+url;
        }
        webview = (WebView)findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient(){

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
        initializeWebView();
    }

    /**
     * An AsyncTask to fetch a page and its information with JSoup
     */
    private class FetchPageTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... url) {
            Document doc = null;
            try {
                doc = Jsoup.connect(url[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return doc;
        }

        protected void onPostExecute(Document doc) {
            //Call jsoupAnalysis
            jsoupAnalysis(doc);
        }
    }


}
