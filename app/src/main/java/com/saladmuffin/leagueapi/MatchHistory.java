package com.saladmuffin.leagueapi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SaladMuffin on 20/09/2015.
 */
public class MatchHistory {

    private int summonerId;
    private MatchHistoryAdapter adapter;
    private Context context;
    private ListView matchHistoryList;

    public MatchHistory(int summonerId, Context context, ListView matchHistoryList) {
        this.context = context;
        this.summonerId = summonerId;
        this.matchHistoryList = matchHistoryList;
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

    public ListView getListView() {
        return matchHistoryList;
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
            ImageView ivItem7 = (ImageView) view.findViewById(R.id.matchSummonerItem7);
            ImageView ivSpell1 = (ImageView) view.findViewById(R.id.matchSummonerSpell1);
            ImageView ivSpell2 = (ImageView) view.findViewById(R.id.matchSummonerSpell2);


            int champId = matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_CHAMPION_ID));

            ChampionFetcherDbHelper mDbHelper = new ChampionFetcherDbHelper(context);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor championCursor = db.rawQuery("SELECT * FROM " + ChampionDB.ChampionEntry.TABLE_NAME + " WHERE " + ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_ID + "='" + champId + "'", null);
            if (championCursor.getCount() == 0) {
                new RiotAPIPuller(context).getChampionInfo(champId, this);
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
            tSummonerGold.setText(matchCursor.getString(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_GOLD)) + "G");
            tSummonerCreeps.setText(matchCursor.getString(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MINIONS)) + "cs");
            tMatchMode.setText(matchCursor.getString(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_MODE)));
            int win = matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_RESULT));
            if (win == 1) tMatchResult.setText("Victory");
            else tMatchResult.setText("Defeat");
            long createDate = matchCursor.getLong(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_START_TIME));
            tMatchCreateDate.setText(new SimpleDateFormat("HH:mm, dd/MM/yy").format(new Date(createDate)));
            int duration = matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_DURATION));
            tSummonerDuration.setText(duration / 60 + "mins, " + duration % 60 + "secs ");
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_1)), ivItem1);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_2)), ivItem2);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_3)), ivItem3);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_4)), ivItem4);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_5)), ivItem5);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_6)), ivItem6);
            loadItemIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_ITEM_7)), ivItem7);
            loadSummonerSpellIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_SPELL_1)), ivSpell1);
            loadSummonerSpellIcon(matchCursor.getInt(matchCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_SPELL_2)), ivSpell2);
        }

        private void loadChampionImage(String champName, ImageView championImage) {
            Picasso p = Picasso.with(context);
            //p.setIndicatorsEnabled(true);
            p.load("http://ddragon.leagueoflegends.com/cdn/5.18.1/img/champion/" + nameForUrl(champName) + ".png").into(championImage);
        }

        private void loadItemIcon(int iconId, ImageView view) {
            Picasso p = Picasso.with(context);
            //p.setIndicatorsEnabled(true);
            p.load("http://ddragon.leagueoflegends.com/cdn/5.19.1/img/item/" + iconId + ".png").
                    placeholder(context.getDrawable(R.drawable.item_placeholder)).into(view);
        }


        private String nameForUrl(String name) {
            return name.replaceAll("[^A-Za-z]","");
        }

    }

    private void loadSummonerSpellIcon(int iconId, ImageView view) {
        SummonerSpellFetcherDbHelper mDbHelper = new SummonerSpellFetcherDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SummonerSpellDB.SummonerSpellEntry.TABLE_NAME + " WHERE " + SummonerSpellDB.SummonerSpellEntry.COLUMN_NAME_SPELL_ID + "=" + iconId, null);
        if (c.getCount() == 0) {
            new RiotAPIPuller(context).downloadSummonerSpellIcon(iconId,this);
        } else {
            c.moveToFirst();
            String spellName = c.getString(c.getColumnIndex(SummonerSpellDB.SummonerSpellEntry.COLUMN_NAME_NAME));
            Picasso.with(context).load("http://ddragon.leagueoflegends.com/cdn/5.19.1/img/spell/" + spellName).into(view);
        }
        db.close();
    }
}
