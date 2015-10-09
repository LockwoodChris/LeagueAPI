package com.saladmuffin.leagueapi.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.saladmuffin.leagueapi.R;
import com.saladmuffin.leagueapi.databases.ChampionDB;
import com.saladmuffin.leagueapi.databases.ChampionFetcherDbHelper;
import com.saladmuffin.leagueapi.databases.MatchDB;
import com.saladmuffin.leagueapi.databases.MatchFetcherDbHelper;
import com.saladmuffin.leagueapi.databases.MatchToSummonerDB;
import com.saladmuffin.leagueapi.databases.PlayerStatsDB;
import com.saladmuffin.leagueapi.databases.PlayerStatsFetcherDbHelper;
import com.saladmuffin.leagueapi.databases.SummonerSpellDB;
import com.saladmuffin.leagueapi.databases.SummonerSpellFetcherDbHelper;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by SaladMuffin on 04/10/2015.
 */
public class MatchHistoryAdapter extends CursorAdapter {

    private Context context;

    public MatchHistoryAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.context = context;
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
        ImageView ivItem0 = (ImageView) view.findViewById(R.id.matchSummonerItem0);
        ImageView ivItem1 = (ImageView) view.findViewById(R.id.matchSummonerItem1);
        ImageView ivItem2 = (ImageView) view.findViewById(R.id.matchSummonerItem2);
        ImageView ivItem3 = (ImageView) view.findViewById(R.id.matchSummonerItem3);
        ImageView ivItem4 = (ImageView) view.findViewById(R.id.matchSummonerItem4);
        ImageView ivItem5 = (ImageView) view.findViewById(R.id.matchSummonerItem5);
        ImageView ivItem6 = (ImageView) view.findViewById(R.id.matchSummonerItem6);
        ImageView ivSpell1 = (ImageView) view.findViewById(R.id.matchSummonerSpell1);
        ImageView ivSpell2 = (ImageView) view.findViewById(R.id.matchSummonerSpell2);

        PlayerStatsFetcherDbHelper statsHelper = new PlayerStatsFetcherDbHelper(context);
        SQLiteDatabase db = statsHelper.getReadableDatabase();
        Cursor statsCursor = db.rawQuery("SELECT * FROM " + PlayerStatsDB.PlayerStatsEntry.TABLE_NAME +
                " WHERE " + PlayerStatsDB.PlayerStatsEntry._ID + "=" +
                matchCursor.getLong(matchCursor.getColumnIndex(MatchDB.SummonerToMatchEntry.COLUMN_NAME_STATS_ID)), null);
        statsCursor.moveToFirst();
        db.close();

        if (statsCursor.getCount() != 0) {

            long champId = statsCursor.getLong(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_CHAMPION_ID));

            ChampionFetcherDbHelper mDbHelper = new ChampionFetcherDbHelper(context);
            db = mDbHelper.getReadableDatabase();
            Cursor championCursor = db.rawQuery(ChampionDB.queryChampionById(champId), null);
            if (championCursor.getCount() == 0) {
                Downloader.getInstance(context).getChampionInfo((int) champId, this);
            } else if (championCursor != null && championCursor.getCount() > 0) {
                championCursor.moveToFirst();
                String champName = championCursor.getString(championCursor.getColumnIndex(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_NAME));
                tChampionName.setText(champName + ", ");
                String champTitle = championCursor.getString(championCursor.getColumnIndex(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_TITLE));
                tSummonerTitle.setText(champTitle);
                loadChampionImage(champName, iChampionIcon);
            }
            db.close();
            int kills = statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_KILLS));
            int deaths = statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_DEATHS));
            int assists = statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ASSISTS));
            tSummonerScore.setText("" + kills + "/" + deaths + "/" + assists);
            tSummonerGold.setText(statsCursor.getString(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_GOLD)) + "G");
            tSummonerCreeps.setText(statsCursor.getString(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_MINIONS)) + "cs");
            long matchId = matchCursor.getLong(matchCursor.getColumnIndex(MatchDB.SummonerToMatchEntry.COLUMN_NAME_MATCH_ID));

            db = new MatchFetcherDbHelper(context).getReadableDatabase();
            Cursor mCursor = db.rawQuery("SELECT * FROM " + MatchDB.MatchEntry.TABLE_NAME + " WHERE " +
                    MatchDB.MatchEntry.COLUMN_NAME_MATCH_ID + "=" + matchId, null);
            mCursor.moveToFirst();
            db.close();
            tMatchMode.setText(mCursor.getString(mCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_MODE)));
            int win = statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_WINNER));
            if (win == 1) tMatchResult.setText("Victory");
            else tMatchResult.setText("Defeat");
            long createDate = mCursor.getLong(mCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_START_TIME));
            tMatchCreateDate.setText(new SimpleDateFormat("HH:mm, dd/MM/yy").format(new Date(createDate)));
            int duration = mCursor.getInt(mCursor.getColumnIndex(MatchDB.MatchEntry.COLUMN_NAME_MATCH_DURATION));
            tSummonerDuration.setText(duration / 60 + "mins, " + duration % 60 + "secs ");
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_0)), ivItem0);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_1)), ivItem1);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_2)), ivItem2);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_3)), ivItem3);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_4)), ivItem4);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_5)), ivItem5);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_6)), ivItem6);
            loadSummonerSpellIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_SPELL_1)), ivSpell1);
            loadSummonerSpellIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_SPELL_2)), ivSpell2);
        }
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


    private void loadSummonerSpellIcon(int iconId, ImageView view) {
        SummonerSpellFetcherDbHelper mDbHelper = new SummonerSpellFetcherDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(SummonerSpellDB.querySumSpellById(iconId), null);
        if (c.getCount() == 0) {
            Downloader.getInstance(context).getSummonerSpellInfo(iconId, this);
        } else {
            c.moveToFirst();
            String spellName = c.getString(c.getColumnIndex(SummonerSpellDB.SummonerSpellEntry.COLUMN_NAME_NAME));
            Picasso.with(context).load("http://ddragon.leagueoflegends.com/cdn/5.19.1/img/spell/" + spellName).into(view);
        }
        db.close();
    }

}
