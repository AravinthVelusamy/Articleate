package website.jonreynolds.jreynolds.articleate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private final int MAX_NUM_SUMMARIES = 20;
    private final int LINES_PER_SUMMARY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Allow people to use keyboard search key to initialize the summarization Activity
        final EditText urlInput = (EditText) findViewById(R.id.url_bar);
        urlInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String url = urlInput.getText().toString();
                    Intent articleIntent = new Intent(MainActivity.this, ArticleActivity.class);
                    articleIntent.putExtra("url", url);
                    startActivity(articleIntent);
                    return true;
                }
                return false;
            }
        });

        try {
            loadCachedSummaries();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads saved summaries (summaries that were completed succesfully)
     * @throws IOException
     */
    private void loadCachedSummaries() throws IOException {
        File cachedSummaries = new File(getCacheDir(), "summaries.txt");

        //Count number of lines
        LineNumberReader  lnr = new LineNumberReader(new FileReader(cachedSummaries));
        lnr.skip(Long.MAX_VALUE);
        int numLines = lnr.getLineNumber() + 1; //Add 1 because line index starts at 0
        // Finally, the LineNumberReader object should be closed to prevent resource leak
        lnr.close();

        //Create ArrayList to hold summaries
        ArrayList<SpannableStringBuilder> summaries = new ArrayList<>();

        //Maintain cache to last MAX_NUM_SUMMARIES summaries and add those to ArrayList
        File tempFile = new File(getCacheDir(), "myTempFile.txt");
        BufferedReader reader = new BufferedReader(new FileReader(cachedSummaries));
        PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

        String currentLine;
        int currentLineNum = 1;
        int i = 0;
        SpannableStringBuilder current = new SpannableStringBuilder();
        while ((currentLine = reader.readLine()) != null) {
            if(numLines - currentLineNum <= MAX_NUM_SUMMARIES*LINES_PER_SUMMARY){
                writer.println(currentLine);
                if(i%3==0) {
                    current.append(currentLine + "\n");
                    current.setSpan(new android.text.style.StyleSpan(Typeface.BOLD_ITALIC), 0, current.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                else if(i%3 == 1)
                    current.append(currentLine + "\n");
                if(i%3 == 2) {
                    current.append(currentLine);
                    current.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.ITALIC), current.length()-currentLine.length(), current.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    summaries.add(current);
                    current= new SpannableStringBuilder();
                }
                i++;
            }
            currentLineNum+=1;
        }
        writer.close();
        reader.close();
        boolean successful = tempFile.renameTo(cachedSummaries);

        //Make newest lines first
        Collections.reverse(summaries);
        ArrayAdapter<SpannableStringBuilder> summaryListViewAdapter = new ArrayAdapter<SpannableStringBuilder>(this, R.layout.list_item_summary, R.id.list_item_summary_textview, summaries){
            @Override
            public View getView (int position, View convertView, ViewGroup parent){
                if(convertView == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = vi.inflate(R.layout.list_item_summary, null);
                }
                TextView tv = (TextView)convertView.findViewById(R.id.list_item_summary_textview);
                tv.setText(getItem(position));
                Log.v("item", getItem(position).toString());
                return convertView;
            }
        };
        ListView summaryListView = (ListView)findViewById(R.id.listView);
        summaryListView.setAdapter(summaryListViewAdapter);
    }

    /**
     * Allow people to use the button to perform the search and launch the
     * Articleate Summarization activity.
     * @param view
     */
    public void handleButtonClick(View view){
        EditText urlInput = (EditText)findViewById(R.id.url_bar);
        String url = urlInput.getText().toString();
        Intent articleIntent = new Intent(this, ArticleActivity.class);
        articleIntent.putExtra("url", url);
        startActivity(articleIntent);
    }

}
