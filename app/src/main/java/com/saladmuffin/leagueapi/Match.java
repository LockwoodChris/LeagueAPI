package com.saladmuffin.leagueapi;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by SaladMuffin on 20/09/2015.
 */
public class Match {
    private String summonerName;
    private int summonerId;
    private String queueType;
    private int matchDuration;
    private int matchId;
    private int mapId;
    private HashMap<Integer, Integer> masteries =  new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> runes =  new HashMap<Integer, Integer>();
    private JSONObject stats;
    private int championId;
    private int participantId;
    private int teamId;
    private String highestTier;
    private int spell1;
    private int spell2;

    public Match(JSONObject jObject) {
        try {
            matchDuration = jObject.getInt("matchDuration");
            matchId = jObject.getInt("matchId");
            mapId = jObject.getInt("mapId");
            queueType = jObject.getString("queueType");
            JSONObject participants = jObject.getJSONArray("participants").getJSONObject(0);
            championId = participants.getInt("championId");
            highestTier = participants.getString("highestAchievedSeasonTier");
            spell1 = participants.getInt("spell1Id");
            spell2 = participants.getInt("spell2Id");
            teamId = participants.getInt("teamId");
            participantId = participants.getInt("participantId");
            masteriesInit(participants.getJSONArray("masteries"));
            runesInit(participants.getJSONArray("runes"));
            stats = participants.getJSONObject("stats");
            JSONObject playerInfo = jObject.getJSONArray("participantIdentities").getJSONObject(0).getJSONObject("player");
            summonerName = playerInfo.getString("summonerName");
            summonerId = playerInfo.getInt("summonerId");
        } catch (JSONException e) {
            Log.d("JSON_EXCEPTION", e.getMessage());
        }
    }

    private void masteriesInit(JSONArray jArray) {
        try {
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                masteries.put(jObject.getInt("masteryId"),jObject.getInt("rank"));
            }
        } catch (JSONException e) {
            Log.d("JSON_EXCEPTION", e.getLocalizedMessage());
        }
    }

    private void runesInit(JSONArray jArray) {
        try {
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                runes.put(jObject.getInt("runeId"),jObject.getInt("rank"));
            }
        } catch (JSONException e) {
            Log.d("JSON_EXCEPTION", e.getLocalizedMessage());
        }
    }
}
