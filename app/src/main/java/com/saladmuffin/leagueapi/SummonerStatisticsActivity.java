package com.saladmuffin.leagueapi;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class SummonerStatisticsActivity extends AppCompatActivity {

    private TextView summonerNameView;
    private TextView showJSONView;
    private String name;
    private RiotAPIPuller api;
    private SummonerFetcherDbHelper mDbHelper;
    private int currId;
    private MatchHistory matchHistory;
    private ListView matchHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new SummonerFetcherDbHelper(this);
        Intent intent = getIntent();
        name = intent.getStringExtra(MainActivity.SUMMONER_NAME);
        api = new RiotAPIPuller(this);
        setTitle(name);
        setContentView(R.layout.activity_summoner_statistics);
        matchHistory = new MatchHistory(currId, this);
        matchHistoryList = (ListView) findViewById(R.id.matchHistoryList);
        matchHistory.setAdapter(matchHistoryList);
        summonerNameView = (TextView) findViewById(R.id.summonerName);
        summonerNameView.setText(name);
        currId = getSummonerId(name);
        api.getMatchHistory(currId, matchHistory);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_summoner_statistics, menu);
        return true;
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
        if (id == R.id.action_clear_matches) {
            clearMatchDatabase();
        }

        return super.onOptionsItemSelected(item);
    }

    private int getSummonerId(String name) {

        int id = -1;

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
                SummonerDB.SummonerEntry.COLUMN_NAME_NAME + "='" + name + "'",// The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        boolean found = c.moveToFirst();
        if (found) {
            id = c.getInt(c.getColumnIndex(SummonerDB.SummonerEntry.COLUMN_NAME_SUMMONER_ID));
        }
        db.close();
        return id;
    }

    private void clearMatchDatabase() {
        Log.d("MY_ERRORS", "clearing Match Database");
        MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(MatchDB.MatchEntry.TABLE_NAME, null, null);
        db.close();
        matchHistory.getAdapter().notifyDataSetChanged();
    }


}
