package com.saladmuffin.leagueapi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by SaladMuffin on 20/09/2015.
 */
public class MatchHistory {

    private int summonerId;
    private MatchHistoryAdapter adapter;
    private Context context;

    public MatchHistory(int summonerId, Context context) {
        this.context = context;
        this.summonerId = summonerId;
    }

    public void setAdapter(ListView matchHistoryList) {
        MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT   * FROM " + MatchDB.MatchEntry.TABLE_NAME, null);
        adapter = new MatchHistoryAdapter(context, c);
        matchHistoryList.setAdapter(adapter);
        db.close();
    }

    public MatchHistoryAdapter getAdapter() {
        return adapter;
    }

    public void parseMatchResponse(String responseStr, String summonerName) {
        try {
            Log.e("MatchHistory", "Response: " + responseStr);
            JSONObject response = new JSONObject(responseStr);
            JSONArray jArray = response.getJSONArray("games");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                new Match(jObject, context, adapter, summonerName, summonerId);
            }
        } catch (JSONException e) {
            Log.d("MatchHistory", e.getLocalizedMessage());
        }
    }

    public class MatchHistoryAdapter extends CursorAdapter {

        public MatchHistoryAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.match_history_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            TextView tChampionName = (TextView) view.findViewById(R.id.matchChampionName);
            TextView tSummonerTitle = (TextView) view.findViewById(R.id.matchSummonerTitle);
            TextView tSummonerScore = (TextView) view.findViewById(R.id.matchSummonerScore);
            TextView tSummonerGold = (TextView) view.findViewById(R.id.matchSummonerGold);
            TextView tSummonerCreeps = (TextView) view.findViewById(R.id.matchSummonerCreeps);
            TextView tSummonerDuration = (TextView) view.findViewById(R.id.matchSummonerDuration);
            ImageView championImage = (ImageView) view.findViewById((R.id.matchImage));

            int id = cursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_NAME);
            if (id != -1) {
                String champName = cursor.getString(id);
                if (champName != null) {
                    tChampionName.setText(champName + ", ");
                    new DownloadChampionImage(championImage, champName).execute();
                }
                tSummonerTitle.setText(cursor.getString(cursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_TITLE)));
            }
            int kills = cursor.getInt(cursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_KILLS));
            int deaths = cursor.getInt(cursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_DEATHS));
            int assists = cursor.getInt(cursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ASSISTS));
            tSummonerScore.setText("" + kills +"/"+ deaths +"/"+ assists);
            tSummonerGold.setText(", "+ cursor.getInt(cursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_GOLD)) +"G");
            tSummonerCreeps.setText(", "+ cursor.getInt(cursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MINIONS)) + "cs");
            int duration = cursor.getInt(cursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_DURATION));
            tSummonerDuration.setText(", " + duration / 60 + "mins, " + duration % 60 + "secs ");
        }

        private class DownloadChampionImage extends AsyncTask<Void,Void,Void> {

            private ImageView view;
            private String name;
            private Bitmap bmp;

            private DownloadChampionImage(ImageView view, String name) {
                this.view = view;
                this.name = name;
            }

            @Override
            protected Void doInBackground(Void... urls) {
                // params comes from the execute() call: params[0] is the url.
                try {
                    URL url = new URL("http://ddragon.leagueoflegends.com/cdn/5.18.1/img/champion/"+ name +".png");
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (IOException e) {
                    Log.d("IOException", e.getLocalizedMessage());
                }
                return null;
            }
            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(Void v) {
                view.setImageBitmap(bmp);
            }
        }

    }
}
