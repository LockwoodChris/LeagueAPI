package com.saladmuffin.leagueapi;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.saladmuffin.leagueapi.databases.SummonerDB;
import com.saladmuffin.leagueapi.databases.SummonerFetcherDbHelper;
/*
TODO:
 - View match details class
 - add different stats
 */

public class MainActivity extends AppCompatActivity {

    public static final String SUMMONER_NAME = "com.saladmuffin.leagueapi.SUMMONER_NAME";

    private EditText summonerNameView;
    private SummonerListCursorAdapter adapter;
    private SummonerFetcherDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new SummonerFetcherDbHelper(this);
        setContentView(R.layout.activity_main);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSummoners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        Log.d("SummonerDB", "getting Summoners");
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void clearSummonerDatabase() {
        Log.d("SummonderDB", "clearing Summoner Database");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(SummonerDB.SummonerEntry.TABLE_NAME, null, null);
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void initialiseSummonerList() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor sumListCursor = db.rawQuery(SummonerDB.queryAllSummoners(), null);
        adapter = new SummonerListCursorAdapter(this, sumListCursor);
        ListView listView = (ListView) findViewById(R.id.summonerList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MY_ERRORS", "before openSummonerStatistics");
                openSummonerStatistics(((TextView) view.findViewById(R.id.summonerListItemName)).getText().toString());
                Log.d("MY_ERRORS", "after openSummonerStatistics");
            }
        });
    }

    private class SummonerListCursorAdapter extends CursorAdapter {
        private SummonerListCursorAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.summoner_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView tvName = (TextView) view.findViewById(R.id.summonerListItemName);
            // Extract properties from cursor
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            // Populate fields with extracted properties
            tvName.setText(String.valueOf(name));
        }
    }

}
