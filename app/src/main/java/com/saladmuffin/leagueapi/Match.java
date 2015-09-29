package com.saladmuffin.leagueapi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    private String matchType;
    private String matchMode;
    private String matchSubType;
    private int matchDuration;
    private String matchId;
    private int mapId;
    private JSONObject stats;
    private int championId;
    private int teamId;
    private int spell1;
    private int spell2;
    private JSONArray participants;
    private String summonerScore;
    private String championName;
    private String championTitle;
    private Context context;
    private RiotAPIPuller api;
    private MatchHistory.MatchHistoryAdapter adapter;
    private int gold;
    private int cs;
    private int deaths;
    private int kills;
    private int assists;

    public Match(JSONObject jObject, Context context, MatchHistory.MatchHistoryAdapter adapter,
                 String summonerName, int summonerId) {
        this.adapter = adapter;
        this.context = context;
        this.summonerName = summonerName;
        this.summonerId = summonerId;
        api = new RiotAPIPuller(context);
        try {
            Log.d("MatchCreation", "starting match parsing");
            matchId = jObject.getString("gameId");
            mapId = jObject.getInt("mapId");
            matchMode = jObject.getString("gameMode");
            matchSubType = jObject.getString("subType");
            matchType = jObject.getString("gameType");
            championId = jObject.getInt("championId");
            api.getChampionInfo(championId, this);
            spell1 = jObject.getInt("spell1");
            spell2 = jObject.getInt("spell2");
            teamId = jObject.getInt("teamId");
            participants = jObject.getJSONArray("fellowPlayers");
            stats = jObject.getJSONObject("stats");
            parseStats();
            addMatch();
        } catch (JSONException e) {
            Log.d("JSON_EXCEPTION", e.getMessage());
        }
}

    private void parseStats() {
        try {
            if (stats.has("championsKilled")) kills = stats.getInt("championsKilled");
            if (stats.has("assists")) assists = stats.getInt("assists");
            if (stats.has("numDeaths")) deaths = stats.getInt("numDeaths");
            if (stats.has("goldEarned")) gold = stats.getInt("goldEarned");
            if (stats.has("minionsKilled")) cs = stats.getInt("minionsKilled");
            if (stats.has("timePlayed")) matchDuration = stats.getInt("timePlayed");
            summonerScore = "" + kills +"/"+ deaths +"/"+ assists ;
        } catch (JSONException e) {
            Log.d("JSON_EXCEPTION", e.getMessage());
        }
    }
    public void parseChampionResponse(String response) {
        try {
            Log.e("Match", "Parsing champion response: " + response);
            JSONObject jObject = new JSONObject(response);
            championName = jObject.getString("name");
            championTitle = jObject.getString("title");
            addMatch();
        } catch (JSONException e) {
            Log.d("JSON_EXCEPTION", e.getLocalizedMessage());
        }
    }

    /* Accessor Methods */
    public String getSummonerName() {
        return summonerName;
    }

    public String getSummonerScore() {
        return summonerScore;
    }

    public String getChampionName() {
        return championName;
    }

    public String getChampionTitle() {
        return championTitle;
    }

    public int getSummonerGold() { return gold; }

    public int getSummonerCreeps() { return cs; }

    public String getSummonerDuration() { return ""+matchDuration/60+" mins " + matchDuration%60 + " secs"; }

    /* Database Methods */

    public void addMatch() {
        MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Cursor mCursor = db.rawQuery("SELECT * FROM " + MatchDB.MatchEntry.TABLE_NAME + " WHERE   " + MatchDB.MatchEntry.COLUMN_NAME_MATCH_ID + "='" + matchId + "'", null);

        if (mCursor.getCount() == 0) {
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_ID, matchId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SUMMONER_NAME, championName);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SUMMONER_ID, summonerId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_TYPE, matchType);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_SUB_TYPE, matchSubType);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_MODE, matchMode);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_DURATION, matchDuration);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MAP_ID, mapId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_ID, championId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_TEAM_ID, teamId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SPELL_1, spell1);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SPELL_2, spell2);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_DEATHS, deaths);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_KILLS, kills);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_ASSISTS, assists);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_GOLD, gold);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MINIONS, cs);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_NAME, championName);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_TITLE, championTitle);

            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.insert(
                    MatchDB.MatchEntry.TABLE_NAME,
                    "null",
                    values);
            Log.d("MY_ERRORS", "added Match at row " + newRowId);
        } else {
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_ID, matchId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SUMMONER_NAME, championName);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SUMMONER_ID, summonerId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_TYPE, matchType);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_SUB_TYPE, matchSubType);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_MODE, matchMode);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_DURATION, matchDuration);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MAP_ID, mapId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_ID, championId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_TEAM_ID, teamId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SPELL_1, spell1);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SPELL_2, spell2);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_DEATHS, deaths);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_KILLS, kills);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_ASSISTS, assists);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_GOLD, gold);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MINIONS, cs);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_NAME, championName);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_TITLE, championTitle);

            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.update(
                    MatchDB.MatchEntry.TABLE_NAME,
                    values,
                    "matchId="+matchId,
                    null);
            Log.d("MY_ERRORS", "updated Match at row " + newRowId);
        }
        adapter.notifyDataSetChanged();
        db.close();
    }

}
