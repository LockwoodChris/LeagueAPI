package com.saladmuffin.leagueapi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String SUMMONER_NAME = "com.saladmuffin.leagueapi.SUMMONER_NAME";

    private EditText summonerNameView;
    private ArrayList<String> summonerNames;
    private ArrayAdapter<String> adapter;
    private SummonerFetcherDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        summonerNames = new ArrayList<String>();
        mDbHelper = new SummonerFetcherDbHelper(this);
        setContentView(R.layout.activity_main);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, summonerNames);
        adapter.notifyDataSetChanged();
        initialiseSummonerList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_clear_summoners:
                clearSummonerDatabase();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MY_ERRORS", "in onStop");
        /*
        try {
            FileOutputStream outputStream = openFileOutput("tempstorage", Context.MODE_PRIVATE);
            for (int i = 0; i < summonerNames.size(); i++) {
                outputStream.write((summonerNames.get(i) + "\n").getBytes());
                Log.d("MY_ERRORS", "writing " + summonerNames.get(i));
            }
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.d("MY_ERRORS", "could not open file in onPause");
        } catch (IOException e) {
            Log.d("MY_ERRORS", "IOException in onPause");
        }
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MY_ERRORS", "in onResume");
        getSummoners();
        /*
        String pathName = "/data/user/0/com.saladmuffin.leagueapi/files/tempstorage";
        try {
            BufferedReader br = new BufferedReader(new FileReader(pathName));
            String line;

            while ((line = br.readLine()) != null) {
                summonerNames.add(line);
                Log.d("MY_ERRORS", "reading " + line);
            }
            adapter.notifyDataSetChanged();
            br.close();

        } catch (FileNotFoundException e) {
            Log.d("MY_ERRORS", "could not open file in onResume");
        } catch (IOException e) {
            Log.d("MY_ERRORS", "IOException in onResume");
        }
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MY_ERRORS", "ON DESTROY CALLED");
    }


    public void openStatistics(View view) {
        // Get summoner name and start options activity
        summonerNameView = (EditText) findViewById(R.id.summonerName);
        String name = summonerNameView.getText().toString();
        adapter.notifyDataSetChanged();
        openSummonerStatistics(name);
    }

    private void openSummonerStatistics(String name) {
        Intent intent = new Intent(this, SummonerStatisticsActivity.class);
        intent.putExtra(SUMMONER_NAME, name);
        startActivity(intent);
    }

    private void getSummoners() {

        // Gets the data repository in read mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();


        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                SummonerDB.SummonerEntry._ID,
                SummonerDB.SummonerEntry.COLUMN_NAME_NAME,
                SummonerDB.SummonerEntry.COLUMN_NAME_SUMMONER_ID
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                SummonerDB.SummonerEntry.COLUMN_NAME_NAME + " DESC";

        Cursor c = db.query(
                SummonerDB.SummonerEntry.TABLE_NAME,      // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        boolean isNext = c.moveToFirst();
        String name = "";
        Log.d("MY_ERRORS", "currId in getSummoners");
        while (isNext) {
            int id = c.getColumnIndex(SummonerDB.SummonerEntry.COLUMN_NAME_NAME);
            if (id != -1) {
                name = c.getString(id);
                summonerNames.add(name);
            }
            Log.d("MY_ERRORS", "adding " + name);
            isNext = c.moveToNext();
        }
        adapter.notifyDataSetChanged();
    }

    private void clearSummonerDatabase() {
        Log.d("MY_ERRORS", "clearing Summoner Database");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(SummonerDB.SummonerEntry.TABLE_NAME, null, null);
        summonerNames.clear();
        adapter.notifyDataSetChanged();
    }

    private void initialiseSummonerList() {
        ListView listView = (ListView) findViewById(R.id.summonerList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MY_ERRORS", "before openSummonerStatistics");
                openSummonerStatistics(((TextView) view).getText().toString());
                Log.d("MY_ERRORS", "after openSummonerStatistics");
            }
        });
    }

}
