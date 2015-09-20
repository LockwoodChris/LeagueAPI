package com.saladmuffin.leagueapi;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SaladMuffin on 20/09/2015.
 */
public class MatchHistory {

    private int summonerId;
    private ArrayList<Match> matchList;

    public MatchHistory(int summonerId) {
        this.summonerId = summonerId;
        matchList = new ArrayList<>();
    }

    public void parseResponse(String responseStr) {
        try {
            JSONObject response = new JSONObject(responseStr);
            JSONArray jArray = response.getJSONArray("matches");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                matchList.add(new Match(jObject));
            }
        } catch (JSONException e) {
            Log.d("JSON_EXCEPTION", e.getMessage());
        }
    }
}
