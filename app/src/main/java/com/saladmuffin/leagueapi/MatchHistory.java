package com.saladmuffin.leagueapi;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by SaladMuffin on 20/09/2015.
 */
public class MatchHistory {

    private int summonerId;
    private MatchHistoryAdapter adapter;
    private Context context;

    public MatchHistory(int summonerId, Context context) {
        this.context = context;
        this.summonerId = summonerId;
    }

    public void setAdapter(ListView matchHistoryList) {
        MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor matchCursor = db.rawQuery("SELECT * FROM " + MatchDB.MatchEntry.TABLE_NAME + " WHERE summonerId=" + summonerId, null);
        adapter = new MatchHistoryAdapter(context, matchCursor);
        matchHistoryList.setAdapter(adapter);
        db.close();
    }

    public MatchHistoryAdapter getAdapter() {
        return adapter;
    }

    public void parseMatchResponse(String responseStr, String summonerName) {
        try {
            Log.e("MatchHistory", "Response: " + responseStr);
            JSONObject response = new JSONObject(responseStr);
            JSONArray jArray = response.getJSONArray("games");
            Log.d("MatchHistory", "Number of matches = " + jArray.length());
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                new Match(jObject, context, adapter, summonerName, summonerId);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.d("MatchHistory", e.getLocalizedMessage());
        }
    }

    public class MatchHistoryAdapter extends CursorAdapter {

        public MatchHistoryAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.match_history_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor matchCursor) {

            TextView tChampionName = (TextView) view.findViewById(R.id.matchChampionName);
            TextView tSummonerTitle = (TextView) view.findViewById(R.id.matchSummonerTitle);
            TextView tSummonerScore = (TextView) view.findViewById(R.id.matchSummonerScore);
            TextView tSummonerGold = (TextView) view.findViewById(R.id.matchSummonerGold);
            TextView tSummonerCreeps = (TextView) view.findViewById(R.id.matchSummonerCreeps);
            TextView tSummonerDuration = (TextView) view.findViewById(R.id.matchSummonerDuration);
            TextView tMatchMode = (TextView) view.findViewById(R.id.matchMode);
            TextView tMatchResult = (TextView) view.findViewById(R.id.matchResult);
            TextView tMatchCreateDate = (TextView) view.findViewById(R.id.matchCreateDate);
            ImageView iChampionIcon = (ImageView) view.findViewById(R.id.matchImage);
            ImageView ivItem1 = (ImageView) view.findViewById(R.id.matchSummonerItem1);
            ImageView ivItem2 = (ImageView) view.findViewById(R.id.matchSummonerItem2);
            ImageView ivItem3 = (ImageView) view.findViewById(R.id.matchSummonerItem3);
            ImageView ivItem4 = (ImageView) view.findViewById(R.id.matchSummonerItem4);
            ImageView ivItem5 = (ImageView) view.findViewById(R.id.matchSummonerItem5);
            ImageView ivItem6 = (ImageView) view.findViewById(R.id.matchSummonerItem6);
            ImageView ivSpell1 = (ImageView) view.findViewById(R.id.matchSummonerSpell1);
            ImageView ivSpell2 = (ImageView) view.findViewById(R.id.matchSummonerSpell2);


            int champId = matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_ID));

            ChampionFetcherDbHelper mDbHelper = new ChampionFetcherDbHelper(context);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor championCursor = db.rawQuery("SELECT * FROM " + ChampionDB.ChampionEntry.TABLE_NAME + " WHERE " + ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_ID + "='" + champId + "'", null);
            if (championCursor.getCount() == 0) {
                new RiotAPIPuller(context).getChampionInfo(champId);
            } else if (championCursor != null && championCursor.getCount()>0){
                championCursor.moveToFirst();
                String champName = championCursor.getString(championCursor.getColumnIndex(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_NAME));
                tChampionName.setText(champName + ", ");
                String champTitle = championCursor.getString(championCursor.getColumnIndex(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_TITLE));
                tSummonerTitle.setText(champTitle);
                loadChampionImage(champName, iChampionIcon);
            }
            db.close();
            int kills = matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_KILLS));
            int deaths = matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_DEATHS));
            int assists = matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ASSISTS));
            tSummonerScore.setText("" + kills +"/"+ deaths +"/"+ assists);
            tSummonerGold.setText(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_GOLD)) +"G");
            tSummonerCreeps.setText(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MINIONS)) + "cs");
            tMatchMode.setText(matchCursor.getString(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_MODE)));
            int win = matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_RESULT));
            if (win == 1) tMatchResult.setText("Victory");
            else tMatchResult.setText("Defeat");
            long createDate = matchCursor.getLong(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_START_TIME));
            tMatchCreateDate.setText(new SimpleDateFormat("HH:mm:ss, dd/MM/yy").format(new Date(createDate)));
            int duration = matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_DURATION));
            tSummonerDuration.setText(duration / 60 + "mins, " + duration % 60 + "secs ");
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_1)), ivItem1);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_2)), ivItem2);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_3)), ivItem3);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_4)), ivItem4);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_5)), ivItem5);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_6)), ivItem6);
            loadSummonerSpellIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_SPELL_1)), ivSpell1);
            loadSummonerSpellIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_SPELL_2)), ivSpell2);
        }

        private void loadSummonerSpellIcon(int iconId, ImageView view) {
            if (iconId != 0) {
                File dir = context.getFilesDir();
                File newFile = new File(dir, "/summonerspells/icons/" + iconId + ".png");
                try {
                    if (newFile.isFile()) {
                        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(newFile));
                        view.setImageBitmap(b);
                    } else {
                        new RiotAPIPuller(context).downloadSummonerSpellIcon(iconId, view);
                    }
                } catch (IOException e) {
                    Log.e("MatchHistory", "IOException while loading summoner spell icon");
                }
            }
        }

        private void loadChampionImage(String champName, ImageView championImage) {
            champName = nameForUrl(champName);
            File dir = context.getFilesDir();
            File newFile = new File(dir,"/champions/icons/" + champName +".png");
            try {
                if (newFile.isFile()) {
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(newFile));
                    championImage.setImageBitmap(b);
                } else {
                    new DownloadChampionImage(championImage, champName).execute();
                }
            } catch (IOException e ) {
                Log.e("MatchHistory","IOException while loading championImage");
            }
        }

        private void loadItemIcon(int iconId, ImageView view) {
            if (iconId != 0) {
                File dir = context.getFilesDir();
                File newFile = new File(dir, "/items/icons/" + iconId + ".png");
                try {
                    if (newFile.isFile()) {
                        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(newFile));
                        view.setImageBitmap(b);
                    } else {
                        new DownloadItemIcon(view, iconId).execute();
                    }
                } catch (IOException e) {
                    Log.e("MatchHistory", "IOException while loading championImage");
                }
            }
        }

        private class DownloadItemIcon extends AsyncTask<Void,Void,Void> {

            private ImageView view;
            private int id;
            private Bitmap bmp;

            private DownloadItemIcon(ImageView view, int id) {
                this.view = view;
                this.id = id;
            }

            @Override
            protected Void doInBackground(Void... urls) {
                // params comes from the execute() call: params[0] is the url.
                try {
                    URL url = new URL("http://ddragon.leagueoflegends.com/cdn/5.19.1/img/item/"+ id +".png");
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    File file = context.getFilesDir();
                    file = new File(file.getPath() + "/items/icons/");
                    file.mkdirs();
                    file = new File(file.getPath() + "/" + id + ".png");
                    FileOutputStream fos = new FileOutputStream(file);
                    //FileOutputStream fos = context.openFileOutput("/champions/icons/" + name + ".png", Context.MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Log.d("MatchHistory", "Downloading item icon for " + id);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    Log.e("IOException", e.getLocalizedMessage() + ", icon with id " + id);
                }
                return null;
            }
            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(Void v) {
                loadItemIcon(id, view);
            }
        }

        private class DownloadChampionImage extends AsyncTask<Void,Void,Void> {

            private ImageView view;
            private String name;
            private Bitmap bmp;

            private DownloadChampionImage(ImageView view, String name) {
                this.view = view;
                this.name = name;
            }

            @Override
            protected Void doInBackground(Void... urls) {
                // params comes from the execute() call: params[0] is the url.
                try {
                    URL url = new URL("http://ddragon.leagueoflegends.com/cdn/5.18.1/img/champion/"+ name +".png");
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    File file = context.getFilesDir();
                    file = new File(file.getPath() + "/champions/icons/");
                    file.mkdirs();
                    file = new File(file.getPath() + "/" + name + ".png");
                    FileOutputStream fos = new FileOutputStream(file);
                    //FileOutputStream fos = context.openFileOutput("/champions/icons/" + name + ".png", Context.MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Log.d("MatchHistory", "Downloading champion icon for " + name);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    Log.e("IOException", e.getLocalizedMessage() + ", champion with name " + name);
                }
                return null;
            }
            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(Void v) {
                view.setImageBitmap(bmp);
            }
        }

        private String nameForUrl(String name) {
            return name.replaceAll("[^A-Za-z]","");
        }

    }
}
