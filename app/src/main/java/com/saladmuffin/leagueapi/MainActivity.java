package com.saladmuffin.leagueapi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String SUMMONER_NAME = "com.saladmuffin.leagueapi.SUMMONER_NAME";

    private EditText summonerNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openStatistics(View view) {
        // Get summoner name and start options activity
        summonerNameView = (EditText) findViewById(R.id.summonerName);
        Intent intent = new Intent(this, SummonerStatisticsActivity.class);
        String name =  summonerNameView.getText().toString();
        intent.putExtra(SUMMONER_NAME, name);
        startActivity(intent);
    }
}
