package com.saladmuffin.leagueapi;

import android.provider.BaseColumns;

/**
 * Created by SaladMuffin on 28/09/2015.
 */
public class MatchDB {

    public MatchDB() {}

    /* Inner class that defines table contents */
    public static abstract class MatchEntry implements BaseColumns {
        public static final String TABLE_NAME = "summoners";
        public static final String COLUMN_NAME_SUMMONER_NAME = "name";
        public static final String COLUMN_NAME_SUMMONER_ID = "summonerId";
        public static final String COLUMN_NAME_MATCH_TYPE =  "matchType";
        public static final String COLUMN_NAME_MATCH_MODE = "matchMode";
        public static final String COLUMN_NAME_MATCH_SUB_TYPE = "matchSubType";
        public static final String COLUMN_NAME_MATCH_DURATION = "matchDuration";
        public static final String COLUMN_NAME_MAP_ID = "mapId";
        public static final String COLUMN_NAME_CHAMPION_ID = "championId";
        public static final String COLUMN_NAME_TEAM_ID = "teamId";
        public static final String COLUMN_NAME_SPELL_1 = "spell1Id";
        public static final String COLUMN_NAME_SPELL_2 = "spell2Id";
        public static final String COLUMN_NAME_DEATHS = "deaths";
        public static final String COLUMN_NAME_KILLS = "kills";
        public static final String COLUMN_NAME_ASSISTS = "assists";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_GOLD = "gold";
        public static final String COLUMN_NAME_MINIONS = "minions";
        public static final String COLUMN_NAME_MATCH_ID = "matchId";
        public static final String COLUMN_NAME_CHAMPION_NAME = "championName";
        public static final String COLUMN_NAME_CHAMPION_TITLE = "championTitle";
    }


    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MatchEntry.TABLE_NAME + " (" +
                    MatchEntry._ID + " INTEGER PRIMARY KEY," +
                    MatchEntry.COLUMN_NAME_SUMMONER_NAME + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_SUMMONER_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_TYPE + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_MODE + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_SUB_TYPE + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_DURATION + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MAP_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_CHAMPION_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_TEAM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_SPELL_1 + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_SPELL_2 + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_DEATHS + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_KILLS + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_ASSISTS + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_GOLD + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MINIONS + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_CHAMPION_NAME + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_CHAMPION_TITLE + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MatchEntry.TABLE_NAME;

}
