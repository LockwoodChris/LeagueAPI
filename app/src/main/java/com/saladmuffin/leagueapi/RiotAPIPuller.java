package com.saladmuffin.leagueapi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by SaladMuffin on 06/09/2015.
 */
public class RiotAPIPuller {

    private ConnectivityManager connMgr;
    private Context currContext;

    public RiotAPIPuller (Context context) {
        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        currContext = context;
    }

    public void getSummonerInfo(String name) {
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadSummonerDetails().execute(name);
        }
    }

    public void getMatchHistory(Integer summonerId, MatchHistory history, String name) {
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadMatchHistory(history).execute(matchHistoryUrl(summonerId), name);
        }
    }

    public void getChampionInfo(Integer id) {
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadChampionDetails().execute(championNameUrl(id));
        }
    }

    public void downloadSummonerSpellIcon(int iconId, ImageView view) {
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadSummonerSpellIcon(view,iconId).execute();
        }
    }

    /* PRIVATE METHODS */

    private String summonerNameUrl(String name) {
        return "https://euw.api.pvp.net/api/lol/euw/v1.4/summoner/by-name/" + name + "?api_key=817c2c76-73f9-4c53-801f-d4e06c88768f";
    }

    private String matchHistoryUrl(Integer summonerId) {
        return "https://euw.api.pvp.net/api/lol/euw/v1.3/game/by-summoner/" + summonerId + "/recent?api_key=fba4693e-ec41-4629-901e-e246d32cfd15";
    }

    private String championNameUrl(Integer id) {
        return "https://global.api.pvp.net/api/lol/static-data/euw/v1.2/champion/" + id + "?&api_key=fba4693e-ec41-4629-901e-e246d32cfd15";
    }

    private class DownloadSummonerDetails extends AsyncTask<String, Void, String> {

        private String name;

        @Override
        protected String doInBackground(String... urls) {
            name = urls[0];
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(summonerNameUrl(name));
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid. IN DownloadSummonerDetails";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObject = new JSONObject(result);
                jObject = jObject.getJSONObject(name.toLowerCase());
                int id = jObject.getInt("id");
                String name = jObject.getString("name");
                addSummoner(name, id);
            } catch (JSONException e) {
                Log.e("MY_ERRORS", "JSON Exception in OnPostExecute: " + e.getLocalizedMessage());
            }
        }
    }


    private class DownloadSummonerSpellIcon extends AsyncTask<Void,Void,String> {

        private ImageView view;
        private int id;
        private Bitmap bmp;

        private DownloadSummonerSpellIcon(ImageView view, int id) {
            this.view = view;
            this.id = id;
        }

        @Override
        protected String doInBackground(Void... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                String result = downloadUrl("https://global.api.pvp.net/api/lol/static-data/euw/v1.2/summoner-spell/" + id + "?spellData=image&api_key=fba4693e-ec41-4629-901e-e246d32cfd15");
                JSONObject jObject = new JSONObject(result).getJSONObject("image");
                result = jObject.getString("full");
                Log.d("ASDSAD", result);
                URL url = new URL("http://ddragon.leagueoflegends.com/cdn/5.19.1/img/spell/" + result);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                Log.e("IOException", e.getLocalizedMessage() + ", icon with id " + id);
            } catch (JSONException e) {
                Log.e("JSONException", e.getLocalizedMessage());
            }
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                File file = currContext.getFilesDir();
                file = new File(file.getPath() + "/summonerspells/icons/");
                file.mkdirs();
                file = new File(file.getPath() + "/" + id + ".png");
                FileOutputStream fos = new FileOutputStream(file);
                //FileOutputStream fos = context.openFileOutput("/champions/icons/" + name + ".png", Context.MODE_PRIVATE);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                Log.d("MatchHistory", "Downloading summoner spell icon for " + id);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Log.e("IOException", e.getLocalizedMessage() + ", icon with id " + id);
            }
            view.setImageBitmap(bmp);
        }
    }


    private class DownloadMatchHistory extends AsyncTask<String, Void, String> {
        MatchHistory history;
        String name;

        private DownloadMatchHistory(MatchHistory history) {
            this.history = history;
        }

        @Override
        protected String doInBackground(String... urls) {
            name = urls[1];
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
            history.parseMatchResponse(result, name);
        }
    }

    private class DownloadChampionDetails extends AsyncTask<String, Void, String> {

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
            addChampion(result);
        }
    }

    public void addChampion(String jsonResult) {
        try {
            JSONObject jObject = new JSONObject(jsonResult);
            String championName = jObject.getString("name");
            String championTitle = jObject.getString("title");
            int championId = jObject.getInt("id");

            ChampionFetcherDbHelper mDbHelper = new ChampionFetcherDbHelper(currContext);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            Cursor mCursor = db.rawQuery("SELECT * FROM " + ChampionDB.ChampionEntry.TABLE_NAME + " WHERE   " + ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_ID + "=" + championId, null);

            if (mCursor.getCount() == 0) {
                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_ID, championId);
                values.put(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_NAME, championName);
                values.put(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_TITLE, championTitle);

                // Insert the new row, returning the primary key value of the new row
                long newRowId;
                newRowId = db.insert(
                        ChampionDB.ChampionEntry.TABLE_NAME,
                        "null",
                        values);
                Log.d("ChampionDB", "added " + championName + " at row " + newRowId);
            }
            db.close();
        } catch (JSONException e) {
            Log.e("JSON_EXCEPTION", e.getLocalizedMessage());
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
            Log.d("SummonerDB", "added Summoner at row " + newRowId);
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
