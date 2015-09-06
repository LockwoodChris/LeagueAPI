package com.saladmuffin.leagueapi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by SaladMuffin on 06/09/2015.
 */
public class RiotAPIPuller {

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private TextView resultView;

    public RiotAPIPuller (Context context) {
        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
    }

    public void getSummonerInfo(String name, TextView view) {
        resultView = view;
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(summonerNameUrl(name));
        } else {
            resultView.setText("No network connection available");
        }
    }

    public void getMatchHistory(Integer summonerId, TextView view) {
        resultView = view;
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(matchHistoryUrl(summonerId));
        } else {
            resultView.setText("No network connection available");
        }
    }


    /* PRIVATE METHODS */

    private String summonerNameUrl(String name) {
        return "https://euw.api.pvp.net/api/lol/euw/v1.4/summoner/by-name/" + name + "?api_key=817c2c76-73f9-4c53-801f-d4e06c88768f";
    }

    private String matchHistoryUrl(Integer summonerId) {
        return "https://euw.api.pvp.net/api/lol/euw/v2.2/matchhistory/" + summonerId + "?api_key=817c2c76-73f9-4c53-801f-d4e06c88768f";
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            resultView.setText(result);
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

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
            String contentAsString = readIt(is, len);
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
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


}
