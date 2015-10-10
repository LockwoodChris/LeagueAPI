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
import com.saladmuffin.leagueapi.databases.PlayerStatsDB;
import com.saladmuffin.leagueapi.databases.PlayerStatsFetcherDbHelper;
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
        getMatchHistory(id, matchHistoryList);
    }

    private String matchHistoryUrl(Integer summonerId) {
        return "https://euw.api.pvp.net/api/lol/euw/v1.3/game/by-summoner/" + summonerId + "/recent?api_key=fba4693e-ec41-4629-901e-e246d32cfd15";
    }

    public void getMatchHistory(final int summonerId, final ListView matchHistoryList) {
        String url = matchHistoryUrl(summonerId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseMatches(response, summonerId);
                MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(context);
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                String query = "SELECT DISTINCT H.* FROM " + MatchDB.MatchEntry.TABLE_NAME
                        + " P INNER JOIN " + MatchDB.SummonerToMatchEntry.TABLE_NAME + " H ON (H." +
                        MatchDB.SummonerToMatchEntry.COLUMN_NAME_SUMMONER_ID + "=" + summonerId + ")";
                Cursor matchCursor = db.rawQuery(query + " ORDER BY " + MatchDB.MatchEntry._ID + " ASC", null);
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

    private void parseMatches(JSONObject jObject, int summonerId) {
        MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(context);

        try {
            JSONArray jArray = jObject.getJSONArray("games");
            Log.d("MatchHstory","Parsing " + jArray.length() + " matches");
            for (int i = 0; i < jArray.length(); i++) {
                Log.d("Match", "Parsing match " + i);
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                JSONObject jMatch = jArray.getJSONObject(i);
                String matchId = jMatch.getString("gameId");
                int champId = jMatch.getInt("championId");
                Cursor mCursor = db.rawQuery(MatchDB.queryMatchIdAndSummId(matchId, summonerId), null);
                if (mCursor.getCount() == 0) {
                    ContentValues values = new ContentValues();

                    values.put(MatchDB.SummonerToMatchEntry.COLUMN_NAME_MATCH_ID, matchId);
                    values.put(MatchDB.SummonerToMatchEntry.COLUMN_NAME_SUMMONER_ID, summonerId);

                    db.insert(MatchDB.SummonerToMatchEntry.TABLE_NAME,
                                "null",
                                values);
                    db.close();
                    Log.d("MatchSummDB", "Adding matchId:" + matchId + ", summonerId:" + summonerId);
                    checkMatch(matchId, summonerId, champId);
                } else db.close();
            }
        } catch (JSONException e) {
            Log.e("DownloadError", "Summoner Info Parsing + " + e.getLocalizedMessage());
        }

    }

    private String matchUrl(String matchId) {
        return "https://euw.api.pvp.net/api/lol/euw/v2.2/match/" + matchId + "?api_key=817c2c76-73f9-4c53-801f-d4e06c88768f";
    }

    private void parseMatch(final String matchId, final int summonerId, final int champId) {
        String url = matchUrl(matchId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(context);
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();

                    values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_ID, matchId);
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_TYPE, response.getString("matchType"));
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_MODE, response.getString("matchMode"));
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_QUEUE_TYPE, response.getString("queueType"));
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_DURATION, response.getLong("matchDuration"));
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_MAP_ID, response.getInt("mapId"));
                    values.put(MatchDB.MatchEntry.COLUMN_NAME_MATCH_START_TIME, response.getLong("matchCreation"));

                    JSONArray pIdentities = response.getJSONArray("participantIdentities");
                    JSONArray participants = response.getJSONArray("participants");
                    long pullerStatRow = -1;
                    for (int i = 0; i < pIdentities.length(); i++) {
                        boolean containsPlayer = pIdentities.getJSONObject(i).has("player");
                        String summonerName = "";
                        if (containsPlayer) {
                            JSONObject player = pIdentities.getJSONObject(i).getJSONObject("player");
                            values.put(MatchDB.getColNameSumId(i + 1), player.getLong("summonerId"));
                            summonerName = player.getString("summonerName");
                        }
                        long statRow = parsePlayerStats(participants.getJSONObject(i), summonerName);
                        if (champId == participants.getJSONObject(i).getInt("championId")) pullerStatRow = statRow;
                        values.put(MatchDB.getColNameStatId(i + 1), statRow);
                    }
                    db.insert(MatchDB.MatchEntry.TABLE_NAME,
                                "null",
                                values);
                    db.close();
                    if (pullerStatRow != -1) {
                        db = new MatchFetcherDbHelper(context).getWritableDatabase();
                        Cursor mCursor = db.rawQuery(MatchDB.queryMatchIdAndSummId(matchId, summonerId), null);
                        if (mCursor.getCount() > 0) {
                            values = new ContentValues();
                            values.put(MatchDB.SummonerToMatchEntry.COLUMN_NAME_MATCH_ID, matchId);
                            values.put(MatchDB.SummonerToMatchEntry.COLUMN_NAME_SUMMONER_ID, summonerId);
                            values.put(MatchDB.SummonerToMatchEntry.COLUMN_NAME_STATS_ID, pullerStatRow);
                            db.update(MatchDB.SummonerToMatchEntry.TABLE_NAME,
                                        values,
                                        "matchId ='" + matchId + "' AND summonerId='" + summonerId + "'",
                                        null);
                        }
                        db.close();
                    }
                } catch (JSONException e) {
                    Log.e("DownloadError","Match Parsing + " + e.getLocalizedMessage());
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

    private long parsePlayerStats(JSONObject response, String playerName) {
        long statRow = -1;
        try {
            PlayerStatsFetcherDbHelper mDbHelper = new PlayerStatsFetcherDbHelper(context);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();


            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_CHAMPION_ID, response.getInt("championId"));
            if (!playerName.equals("")) values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_SUMMONER_NAME, playerName);
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_SPELL_1, response.getInt("spell1Id"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_SPELL_2, response.getInt("spell2Id"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_TEAM_ID, response.getInt("teamId"));

            // PlayerStats
            response = response.getJSONObject("stats");

            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_DEATHS, response.getLong("deaths"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_KILLS, response.getLong("kills"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ASSISTS, response.getLong("assists"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_GOLD, response.getLong("goldEarned"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_MINIONS, response.getLong("minionsKilled"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_CHAMPION_LEVEL, response.getLong("champLevel"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_0, response.getLong("item0"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_1, response.getLong("item1"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_2, response.getLong("item2"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_3, response.getLong("item3"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_4, response.getLong("item4"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_5, response.getLong("item5"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_6, response.getLong("item6"));
            values.put(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_WINNER, response.getBoolean("winner"));

            statRow = db.insert(PlayerStatsDB.PlayerStatsEntry.TABLE_NAME,
                                "null",
                                values);

            db.close();
        } catch (JSONException e) {
            Log.e("DownloadError","PlayerStats Parsing + " + e.getLocalizedMessage());
        }
        return statRow;
    }

    private void checkMatch(String matchId, int summonerId, int champId) {
        MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor mCursor = db.rawQuery(MatchDB.queryMatchById(matchId), null);

        if (mCursor.getCount() == 0) {
            db.close();
            Log.d("MatchDB","Checking matchDB for " + matchId);
            parseMatch(matchId, summonerId, champId);
        } else db.close();
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


    private String championNameUrl(long id) {
        return "https://global.api.pvp.net/api/lol/static-data/euw/v1.2/champion/" + id + "?&api_key=fba4693e-ec41-4629-901e-e246d32cfd15";
    }

    public void getChampionInfo(final long id, final MatchHistoryAdapter adapter) {
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
                    if (adapter != null) adapter.notifyDataSetChanged();
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
