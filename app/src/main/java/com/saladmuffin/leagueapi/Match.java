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
    private Context context;
    private RiotAPIPuller api;
    private MatchHistory.MatchHistoryAdapter adapter;
    private int gold;
    private int cs;
    private int deaths;
    private int kills;
    private int assists;
    private int item1;
    private int item2;
    private int item3;
    private int item4;
    private int item5;
    private int item6;
    private int item7;
    private boolean win;
    private long createDate;

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
            spell1 = jObject.getInt("spell1");
            spell2 = jObject.getInt("spell2");
            teamId = jObject.getInt("teamId");
            participants = jObject.getJSONArray("fellowPlayers");
            stats = jObject.getJSONObject("stats");
            createDate = jObject.getLong("createDate");
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
            if (stats.has("item0")) item1 = stats.getInt("item0");
            if (stats.has("item1")) item2 = stats.getInt("item1");
            if (stats.has("item2")) item3 = stats.getInt("item2");
            if (stats.has("item3")) item4 = stats.getInt("item3");
            if (stats.has("item4")) item5 = stats.getInt("item4");
            if (stats.has("item5")) item6 = stats.getInt("item5");
            if (stats.has("item6")) item7 = stats.getInt("item6");
            if (stats.has("win")) win = stats.getBoolean("win");
            summonerScore = "" + kills +"/"+ deaths +"/"+ assists ;
        } catch (JSONException e) {
            Log.d("JSON_EXCEPTION", e.getMessage());
        }
    }
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
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SUMMONER_NAME, summonerName);
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
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_RESULT, win);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_START_TIME, createDate);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_1, item1);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_2, item2);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_3, item3);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_4, item4);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_5, item5);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_6, item6);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_7, item7);

            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.insert(
                    MatchDB.MatchEntry.TABLE_NAME,
                    "null",
                    values);
            Log.d("MatchDB", "added Match at row " + newRowId + ", for summoner " + summonerName + "(" + summonerId + ")");
        } /*else {
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_ID, matchId);
            values.put(MatchDB.MatchEntry.COLUMN_NAME_SUMMONER_NAME, summonerName);
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

            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.update(
                    MatchDB.MatchEntry.TABLE_NAME,
                    values,
                    "matchId="+matchId,
                    null);
            Log.d("MatchDB", "updated Match at row " + newRowId + ", for summoner " + summonerName + "(" + summonerId + ")");
        }
        */
        db.close();
        adapter.notifyDataSetChanged();
    }

}
