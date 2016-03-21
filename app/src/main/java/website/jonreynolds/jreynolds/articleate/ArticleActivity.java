package website.jonreynolds.jreynolds.articleate;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import textrank.TextRank;

public class ArticleActivity extends AppCompatActivity {

    private WebView webview;
    private String url;
    private String articleText;
    private static TextRank tr;

    private void summarizeArticle(){
        TextView textView = (TextView)findViewById(R.id.textView);
        if(articleText != null && articleText!= ""){
            ArrayList<String> rankedSentences = tr.sentenceExtraction(articleText);
            textView.setText(rankedSentences.get(0));
        }
        else{
            textView.setText("There was an error processing your request.");
        }
    }
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
        if(url.indexOf("http://") == -1){
            url = "http://"+url;
        }
        webview = (WebView)findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl(url);
        FetchPageTask fetch = new FetchPageTask();
        fetch.execute(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(tr==null){
            InputStream sent = getResources().openRawResource(R.raw.en_sent);
            InputStream token = getResources().openRawResource(R.raw.en_token);
            try {
                tr = new TextRank(sent, token);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        initializeWebView();
    }

    private class FetchPageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... url) {
            Document doc = null;
            String html = "";
            try {
                doc = Jsoup.connect(url[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(doc != null) {
                html = doc.html();
                Element article = doc.select("article").first();
                if(article!=null) {
                    Elements articleParagraphs = article.select("p");
                    articleText = articleParagraphs.text();
                }
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            summarizeArticle();
        }
    }


}
