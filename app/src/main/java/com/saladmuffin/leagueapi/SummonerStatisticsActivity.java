package com.saladmuffin.leagueapi;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.saladmuffin.leagueapi.databases.MatchDB;
import com.saladmuffin.leagueapi.databases.MatchFetcherDbHelper;
import com.saladmuffin.leagueapi.databases.SummonerDB;
import com.saladmuffin.leagueapi.databases.SummonerFetcherDbHelper;
import com.saladmuffin.leagueapi.util.MatchHistoryAdapter;
import com.saladmuffin.leagueapi.util.MatchListFragmentPageAdapter;

public class SummonerStatisticsActivity extends FragmentActivity {

    private String name;
    private SummonerFetcherDbHelper mDbHelper;
    private int currId;
    private ListView matchHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner_statistics);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new MatchListFragmentPageAdapter(
                getSupportFragmentManager(), SummonerStatisticsActivity.this,
                getIntent().getStringExtra(MainActivity.SUMMONER_NAME)));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
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
}
