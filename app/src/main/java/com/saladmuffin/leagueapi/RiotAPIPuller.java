package com.saladmuffin.leagueapi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by SaladMuffin on 06/09/2015.
 */
public class RiotAPIPuller {

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private String currName;
    private Context currContext;

    public RiotAPIPuller (Context context) {
        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        currContext = context;
    }

    public void getSummonerInfo(String name) {
        currName = name;
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadSummonerDetails().execute(summonerNameUrl(name));
        }
    }

    public void getMatchHistory(Integer summonerId, MatchHistory history) {
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadMatchHistory(history).execute(matchHistoryUrl(summonerId));
        }
    }

    public void getChampionInfo(Integer id, Match match) {
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadChampionDetails(match).execute(championNameUrl(id));
        }
    }



    /* PRIVATE METHODS */

    private String summonerNameUrl(String name) {
        return "https://euw.api.pvp.net/api/lol/euw/v1.4/summoner/by-name/" + name + "?api_key=817c2c76-73f9-4c53-801f-d4e06c88768f";
    }

    private String matchHistoryUrl(Integer summonerId) {
        Log.e("ASDASDASD", ""+summonerId);
        return "https://euw.api.pvp.net/api/lol/euw/v1.3/game/by-summoner/" + summonerId + "/recent?api_key=fba4693e-ec41-4629-901e-e246d32cfd15";
    }

    private String championNameUrl(Integer id) {
        return "https://global.api.pvp.net/api/lol/static-data/euw/v1.2/champion/" + id + "?&api_key=fba4693e-ec41-4629-901e-e246d32cfd15";
    }

    private class DownloadSummonerDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid. IN DownloadSummonerDetails";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObject = new JSONObject(result);
                jObject = jObject.getJSONObject(currName.toLowerCase());
                int id = jObject.getInt("id");
                String name = jObject.getString("name");
                addSummoner(name, id);
            } catch (JSONException e) {
                Log.e("MY_ERRORS", "JSON Exception in OnPostExecute: " + e.getLocalizedMessage());
            }
        }
    }

    private class DownloadMatchHistory extends AsyncTask<String, Void, String> {
        MatchHistory history;

        private DownloadMatchHistory(MatchHistory history) {
            this.history = history;
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid. IN DownloadMatchHistory";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            history.parseMatchResponse(result, currName);
        }
    }

    private class DownloadChampionDetails extends AsyncTask<String, Void, String> {
        Match match;

        private DownloadChampionDetails(Match match) {
            this.match = match;
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid. IN DownloadChampionsDetails";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            match.parseChampionResponse(result);
        }
    }

    public void addSummoner(String name, Integer id) {

        SummonerFetcherDbHelper mDbHelper = new SummonerFetcherDbHelper(currContext);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Cursor mCursor = db.rawQuery("SELECT * FROM " + SummonerDB.SummonerEntry.TABLE_NAME + " WHERE   " + SummonerDB.SummonerEntry.COLUMN_NAME_NAME + "='" + name + "'", null);

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
            Log.d("MY_ERRORS", "added Summoner at row " + newRowId);
        }
        db.close();
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buf = new byte[4096];
        while(true) {
            int n = stream.read(buf);
            if( n < 0 ) break;
            outputStream.write(buf, 0, n);
        }

        return outputStream.toString();
    }

}
