package com.saladmuffin.leagueapi.databases;

import android.provider.BaseColumns;

/**
 * Created by SaladMuffin on 08/10/2015.
 */
public class PlayerStatsDB {

    public PlayerStatsDB() {}

    public static abstract class PlayerStatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "player_stats";
        public static final String COLUMN_NAME_SUMMONER_NAME = "name";
        public static final String COLUMN_NAME_MATCH_ID = "matchId";
        public static final String COLUMN_NAME_CHAMPION_ID = "champId";
        public static final String COLUMN_NAME_SPELL_1 = "spell1";
        public static final String COLUMN_NAME_SPELL_2 = "spell2";
        public static final String COLUMN_NAME_DEATHS = "deaths";
        public static final String COLUMN_NAME_ASSISTS = "assists";
        public static final String COLUMN_NAME_KILLS = "kills";
        public static final String COLUMN_NAME_GOLD = "gold";
        public static final String COLUMN_NAME_MINIONS = "minions";
        public static final String COLUMN_NAME_CHAMPION_LEVEL = "champLevel";
        public static final String COLUMN_NAME_TEAM_ID = "teamId";
        public static final String COLUMN_NAME_WINNER = "winner";
        public static final String COLUMN_NAME_ITEM_0 = "matchItem0";
        public static final String COLUMN_NAME_ITEM_1 = "matchItem1";
        public static final String COLUMN_NAME_ITEM_2 = "matchItem2";
        public static final String COLUMN_NAME_ITEM_3 = "matchItem3";
        public static final String COLUMN_NAME_ITEM_4 = "matchItem4";
        public static final String COLUMN_NAME_ITEM_5 = "matchItem5";
        public static final String COLUMN_NAME_ITEM_6 = "matchItem6";
    }

    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlayerStatsEntry.TABLE_NAME + " (" +
                    PlayerStatsEntry._ID + " INTEGER PRIMARY KEY," +
                    PlayerStatsEntry.COLUMN_NAME_SUMMONER_NAME + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_CHAMPION_ID + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_SPELL_1 + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_SPELL_2 + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_DEATHS + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_ASSISTS + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_KILLS + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_GOLD + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_MINIONS + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_MATCH_ID + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_CHAMPION_LEVEL + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_TEAM_ID + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_WINNER + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_ITEM_0 + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_ITEM_1 + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_ITEM_2 + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_ITEM_3 + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_ITEM_4 + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_ITEM_5 + TEXT_TYPE + COMMA_SEP +
                    PlayerStatsEntry.COLUMN_NAME_ITEM_6 + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlayerStatsEntry.TABLE_NAME;

    public static String queryByRow(int row) {
        return "SELECT * FROM " + PlayerStatsEntry.TABLE_NAME + " WHERE "
                + PlayerStatsEntry._ID + "=" + row;
    }
}
