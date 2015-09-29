package com.saladmuffin.leagueapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
    private ArrayList<Match> matchList;
    private MatchHistoryAdapter adapter;
    private Context context;

    public MatchHistory(int summonerId, Context context) {
        this.context = context;
        this.summonerId = summonerId;
        matchList = new ArrayList<>();
    }

    public void setAdapter(ListView matchHistoryList) {
        adapter = new MatchHistoryAdapter(context, matchList);
        matchHistoryList.setAdapter(adapter);
    }

    public void parseMatchResponse(String responseStr, String summonerName) {
        try {
            Log.e("MatchHistory", "Response: " + responseStr);
            JSONObject response = new JSONObject(responseStr);
            JSONArray jArray = response.getJSONArray("games");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                Match match = new Match(jObject, context, adapter, summonerName, summonerId);
                adapter.add(match);
            }
        } catch (JSONException e) {
            Log.d("MatchHistory", e.getLocalizedMessage());
        }
    }

    public class MatchHistoryAdapter extends ArrayAdapter<Match> {

        public MatchHistoryAdapter(Context context, ArrayList<Match> matches) {
            super(context, 0, matches);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Match match = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.match_history_list_item, parent, false);
            }

            TextView tChampionName = (TextView) convertView.findViewById(R.id.matchChampionName);
            TextView tSummonerTitle = (TextView) convertView.findViewById(R.id.matchSummonerTitle);
            TextView tSummonerScore = (TextView) convertView.findViewById(R.id.matchSummonerScore);
            TextView tSummonerGold = (TextView) convertView.findViewById(R.id.matchSummonerGold);
            TextView tSummonerCreeps = (TextView) convertView.findViewById(R.id.matchSummonerCreeps);
            TextView tSummonerDuration = (TextView) convertView.findViewById(R.id.matchSummonerDuration);
            ImageView championImage = (ImageView) convertView.findViewById((R.id.matchImage));

            String champName = match.getChampionName();
            if (champName != null) {
                tChampionName.setText(champName + ", ");
                new DownloadChampionImage(championImage, champName).execute();
            }
            tSummonerTitle.setText(match.getChampionTitle());
            tSummonerScore.setText(match.getSummonerScore());
            tSummonerGold.setText(", "+match.getSummonerGold()+"G");
            tSummonerCreeps.setText(", "+match.getSummonerCreeps()+ "cs");
            tSummonerDuration.setText(", "+match.getSummonerDuration());
            return convertView;
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
