package com.saladmuffin.leagueapi.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.saladmuffin.leagueapi.databases.ChampionDB;
import com.saladmuffin.leagueapi.databases.ChampionFetcherDbHelper;
import com.saladmuffin.leagueapi.databases.MatchDB;
import com.saladmuffin.leagueapi.databases.MatchFetcherDbHelper;
import com.saladmuffin.leagueapi.databases.SummonerDB;
import com.saladmuffin.leagueapi.databases.SummonerFetcherDbHelper;
import com.saladmuffin.leagueapi.databases.SummonerSpellDB;
import com.saladmuffin.leagueapi.databases.SummonerSpellFetcherDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SaladMuffin on 03/10/2015.
 */
public class Downloader {

    private static Downloader singletonInstance;
    private RequestQueue requestQueue;
    private static Context context;

    private Downloader(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized Downloader getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new Downloader(context);
        }
        return singletonInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    private String summonerNameUrl(String name) {
        return "https://euw.api.pvp.net/api/lol/euw/v1.4/summoner/by-name/" + name + "?api_key=817c2c76-73f9-4c53-801f-d4e06c88768f";
    }

    public void getSummonerInfo(final String name, final ListView matchHistoryList)  {
        String url = summonerNameUrl(name);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jObject = response.getJSONObject(name.toLowerCase());
                    int id = jObject.getInt("id");
                    String name = jObject.getString("name");
                    addSummoner(name, id, matchHistoryList);
                } catch (JSONException e) {
                    Log.e("DownloadError", "Summoner Info Parsing + " + e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley",error.getLocalizedMessage());
            }
        }
        );
        Downloader.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void addSummoner(String name, Integer id, ListView matchHistoryList) {

        SummonerFetcherDbHelper mDbHelper = new SummonerFetcherDbHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Cursor mCursor = db.rawQuery(SummonerDB.querySummonerByName(name), null);

        if (mCursor.getCount() == 0) {
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(SummonerDB.SummonerEntry.COLUMN_NAME_SUMMONER_ID, id);
            values.put(SummonerDB.SummonerEntry.COLUMN_NAME_NAME, name);

            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.insert(
                    SummonerDB.SummonerEntry.TABLE_NAME,
                    "null",
                    values);
            Log.d("SummonerDB", "added Summoner at row " + newRowId);
        }
        db.close();
        getMatchHistory(name, id, matchHistoryList);
    }

    private String matchHistoryUrl(Integer summonerId) {
        return "https://euw.api.pvp.net/api/lol/euw/v1.3/game/by-summoner/" + summonerId + "/recent?api_key=fba4693e-ec41-4629-901e-e246d32cfd15";
    }

    public void getMatchHistory(final String summonerName, final int summonerId, final ListView matchHistoryList) {
        String url = matchHistoryUrl(summonerId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jArray = response.getJSONArray("games");
                    Log.d("MatchHstory","Parsing " + jArray.length() + " matches");
                    for (int i = 0; i < jArray.length(); i++) {
                        Log.d("Match", "Parsing match " + i);
                        parseMatch(jArray.getJSONObject(i), summonerName, summonerId);
                    }
                } catch (JSONException e) {
                    Log.e("DownloadError", "Summoner Info Parsing + " + e.getLocalizedMessage());
                }
                MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(context);
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                Cursor matchCursor = db.rawQuery(MatchDB.queryMatchesBySummonerId(summonerId), null);
                MatchHistoryAdapter adapter = new MatchHistoryAdapter(context, matchCursor);
                matchHistoryList.setAdapter(adapter);
                db.close();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley",error.getLocalizedMessage());
            }
        }
        );
        Downloader.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void parseMatch(JSONObject jObject, String summonerName, int summonerId) {
        MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(context);

        String matchId = "";
        try {
            matchId = jObject.getString("gameId");
        } catch (JSONException e) {
            Log.e("MatchDB", "Error parsing JSON +" + e.getLocalizedMessage());
        }

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Cursor mCursor = db.rawQuery(MatchDB.queryMatchById(matchId), null);

        if (mCursor.getCount() == 0) {
            // Create a new map of values, where column names are the keys
            try {
                ContentValues values = new ContentValues();
                values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_ID, matchId);
                values.put(MatchDB.MatchEntry.COLUMN_NAME_SUMMONER_NAME, summonerName);
                values.put(MatchDB.MatchEntry.COLUMN_NAME_SUMMONER_ID, summonerId);
                values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_TYPE, jObject.getString("gameType"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_SUB_TYPE, jObject.getString("subType"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_MODE, jObject.getString("gameMode"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_MAP_ID, jObject.getInt("mapId"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_ID, jObject.getInt("championId"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_TEAM_ID, jObject.getInt("teamId"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_SPELL_1, jObject.getInt("spell1"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_SPELL_2, jObject.getInt("spell2"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_START_TIME, jObject.getLong("createDate"));
                // getting inner JSON object
                jObject = jObject.getJSONObject("stats");
                if (jObject.has("numDeaths"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_DEATHS, jObject.getInt("numDeaths"));
                if (jObject.has("championsKilled"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_KILLS, jObject.getInt("championsKilled"));
                if (jObject.has("assists"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_ASSISTS, jObject.getInt("assists"));
                if (jObject.has("minionsKilled"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_MINIONS, jObject.getInt("minionsKilled"));
                if (jObject.has("item0"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_1, jObject.getInt("item0"));
                if (jObject.has("item1"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_2, jObject.getInt("item1"));
                if (jObject.has("item2"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_3, jObject.getInt("item2"));
                if (jObject.has("item3"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_4, jObject.getInt("item3"));
                if (jObject.has("item4"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_5, jObject.getInt("item4"));
                if (jObject.has("item5"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_6, jObject.getInt("item5"));
                if (jObject.has("item6"))
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_ITEM_7, jObject.getInt("item6"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_GOLD, jObject.getInt("goldEarned"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_RESULT, jObject.getBoolean("win"));
                values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_DURATION, jObject.getInt("timePlayed"));

                // Insert the new row, returning the primary key value of the new row
                long newRowId;
                newRowId = db.insert(
                        MatchDB.MatchEntry.TABLE_NAME,
                        "null",
                        values);
                Log.d("MatchDB", "added Match at row " + newRowId + ", for summoner " + summonerName + "(" + summonerId + ")");
            } catch (JSONException e) {
                Log.e("MatchDB", "Error parsing JSON +" + e.getLocalizedMessage());
            }
        }
        db.close();
    }

    private String summonerSpellUrl(int id) {
        return "https://global.api.pvp.net/api/lol/static-data/euw/v1.2/summoner-spell/" + id + "?spellData=image&api_key=fba4693e-ec41-4629-901e-e246d32cfd15";
    }

    public void getSummonerSpellInfo(final int id, final MatchHistoryAdapter matchHistoryAdapter) {
        String url = summonerSpellUrl(id);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    SummonerSpellFetcherDbHelper mDbHelper = new SummonerSpellFetcherDbHelper(context);
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    Cursor mCursor = db.rawQuery(SummonerSpellDB.querySumSpellById(id), null);

                    if (mCursor.getCount() == 0) {
                        String imageName = response.getJSONObject("image").getString("full");
                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(SummonerSpellDB.SummonerSpellEntry.COLUMN_NAME_SPELL_ID, id);
                        values.put(SummonerSpellDB.SummonerSpellEntry.COLUMN_NAME_NAME, imageName);

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId;
                        newRowId = db.insert(
                                SummonerSpellDB.SummonerSpellEntry.TABLE_NAME,
                                "null",
                                values);
                        Log.d("SummonerSpellDB", "added " + imageName + " at row " + newRowId);
                    }
                    db.close();
                    matchHistoryAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("DownloadError", "Summoner Info Parsing + " + e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley",error.getLocalizedMessage());
            }
        }
        );
        Downloader.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }


    private String championNameUrl(Integer id) {
        return "https://global.api.pvp.net/api/lol/static-data/euw/v1.2/champion/" + id + "?&api_key=fba4693e-ec41-4629-901e-e246d32cfd15";
    }

    public void getChampionInfo(final int id, final MatchHistoryAdapter adapter) {
        String url = championNameUrl(id);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    ChampionFetcherDbHelper mDbHelper = new ChampionFetcherDbHelper(context);
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    Cursor mCursor = db.rawQuery(ChampionDB.queryChampionById(id), null);

                    if (mCursor.getCount() == 0) {
                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_ID, id);
                        values.put(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_NAME, response.getString("name"));
                        values.put(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_TITLE, response.getString("title"));

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId;
                        newRowId = db.insert(
                                ChampionDB.ChampionEntry.TABLE_NAME,
                                "null",
                                values);
                        Log.d("ChampionDB", "added " + response.getString("name") + " at row " + newRowId);
                    }
                    db.close();
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("DownloadError", "Summoner Info Parsing + " + e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley",error.getLocalizedMessage());
            }
        }
        );
        Downloader.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

}
