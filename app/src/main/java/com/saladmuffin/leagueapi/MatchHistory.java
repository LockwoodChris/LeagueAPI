package com.saladmuffin.leagueapi;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public void parseMatchResponse(String responseStr) {
        try {
            JSONObject response = new JSONObject(responseStr);
            JSONArray jArray = response.getJSONArray("matches");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                Match match = new Match(jObject, context, adapter);
                matchList.add(match);
                adapter.add(match);
            }
        } catch (JSONException e) {
            Log.d("JSON_EXCEPTION", e.getMessage());
        }
    }

    public class MatchHistoryAdapter extends ArrayAdapter<Match> {

        public MatchHistoryAdapter(Context context, ArrayList<Match> matches) {
            super(context, 0, matches);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Match match = getItem(position);

            //if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.match_history_list_item, parent, false);
            //}

            TextView tSummonerName = (TextView) convertView.findViewById(R.id.matchSummonerName);
            TextView tSummonerTitle = (TextView) convertView.findViewById(R.id.matchSummonerTitle);
            TextView tSummonerScore = (TextView) convertView.findViewById(R.id.matchSummonerScore);

            String champName = match.getChampionName();
            if (champName != null) tSummonerName.setText(champName + ", ");
            tSummonerTitle.setText(match.getChampionTitle());
            tSummonerScore.setText(match.getSummonerScore());

            return convertView;
        }

    }
}
