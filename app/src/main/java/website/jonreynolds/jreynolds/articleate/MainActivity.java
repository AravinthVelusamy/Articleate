package website.jonreynolds.jreynolds.articleate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        loadCachedSummaries();
    }

    private void loadCachedSummaries(){
        File cachedSummaries = new File(getCacheDir(), "summaries.txt");
        ArrayList<String> summaries = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(cachedSummaries));
            String line;
            while ((line = br.readLine()) != null) {
                summaries.add(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Make newest lines first
        Collections.reverse(summaries);
        ArrayAdapter<String> summaryListViewAdapter = new ArrayAdapter<String>(this, R.layout.list_item_summary, R.id.list_item_summary_textview, summaries);
        ListView summaryListView = (ListView)findViewById(R.id.listView);
        summaryListView.setAdapter(summaryListViewAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void handleButtonClick(View view){
        EditText urlInput = (EditText)findViewById(R.id.editText);
        String url = urlInput.getText().toString();
        Intent articleIntent = new Intent(this, ArticleActivity.class);
        articleIntent.putExtra("url", url);
        startActivity(articleIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
