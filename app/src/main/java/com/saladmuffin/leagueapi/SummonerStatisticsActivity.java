package com.saladmuffin.leagueapi;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SummonerStatisticsActivity extends AppCompatActivity {

    private TextView summonerNameView;
    private TextView showJSONView;
    private String name;
    private RiotAPIPuller api;
    private SummonerFetcherDbHelper mDbHelper;
    private int currId;
    private MatchHistory matchHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new SummonerFetcherDbHelper(this);
        Intent intent = getIntent();
        name = intent.getStringExtra(MainActivity.SUMMONER_NAME);
        setTitle(name);
        setContentView(R.layout.activity_summoner_statistics);
        showJSONView = (TextView) findViewById(R.id.showJSON);
        summonerNameView = (TextView) findViewById(R.id.summonerName);
        summonerNameView.setText(name);
        api = new RiotAPIPuller(this);
        api.getSummonerInfo(name);
        currId = getSummonerId(name);
        matchHistory = new MatchHistory(currId);
        api.getMatchHistory(currId,showJSONView,matchHistory);
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
        return id;
    }

}
