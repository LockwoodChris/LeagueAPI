package com.saladmuffin.leagueapi.databases;

import android.provider.BaseColumns;

/**
 * Created by SaladMuffin on 28/09/2015.
 */
public class MatchDB {

    public MatchDB() {}

    /* Inner class that defines table contents */
    public static abstract class MatchEntry implements BaseColumns {
        public static final String TABLE_NAME = "matches";
        public static final String COLUMN_NAME_MATCH_ID = "matchId";
        public static final String COLUMN_NAME_MATCH_TYPE =  "matchType";
        public static final String COLUMN_NAME_MATCH_MODE = "matchMode";
        public static final String COLUMN_NAME_MATCH_QUEUE_TYPE = "matchQueueType";
        public static final String COLUMN_NAME_MATCH_DURATION = "matchDuration";
        public static final String COLUMN_NAME_MAP_ID = "mapId";
        public static final String COLUMN_NAME_MATCH_START_TIME = "matchStartTime";

        public static final String COLUMN_NAME_P1_SUMM_ID = "summ_p1";
        public static final String COLUMN_NAME_P2_SUMM_ID = "summ_p2";
        public static final String COLUMN_NAME_P3_SUMM_ID = "summ_p3";
        public static final String COLUMN_NAME_P4_SUMM_ID = "summ_p4";
        public static final String COLUMN_NAME_P5_SUMM_ID = "summ_p5";
        public static final String COLUMN_NAME_P6_SUMM_ID = "summ_p6";
        public static final String COLUMN_NAME_P7_SUMM_ID = "summ_p7";
        public static final String COLUMN_NAME_P8_SUMM_ID = "summ_p8";
        public static final String COLUMN_NAME_P9_SUMM_ID = "summ_p9";
        public static final String COLUMN_NAME_P10_SUMM_ID = "summ_p10";

        public static final String COLUMN_NAME_P1_STAT_ID = "stat_p1";
        public static final String COLUMN_NAME_P2_STAT_ID = "stat_p2";
        public static final String COLUMN_NAME_P3_STAT_ID = "stat_p3";
        public static final String COLUMN_NAME_P4_STAT_ID = "stat_p4";
        public static final String COLUMN_NAME_P5_STAT_ID = "stat_p5";
        public static final String COLUMN_NAME_P6_STAT_ID = "stat_p6";
        public static final String COLUMN_NAME_P7_STAT_ID = "stat_p7";
        public static final String COLUMN_NAME_P8_STAT_ID = "stat_p8";
        public static final String COLUMN_NAME_P9_STAT_ID = "stat_p9";
        public static final String COLUMN_NAME_P10_STAT_ID = "stat_p10";
    }


    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MatchEntry.TABLE_NAME + " (" +
                    MatchEntry._ID + " INTEGER PRIMARY KEY," +
                    MatchEntry.COLUMN_NAME_MATCH_TYPE + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_MODE + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_QUEUE_TYPE + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_DURATION + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MAP_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P1_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P2_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P3_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P4_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P5_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P6_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P7_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P8_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P9_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P10_SUMM_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_MATCH_START_TIME + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P1_STAT_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P2_STAT_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P3_STAT_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P4_STAT_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P5_STAT_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P6_STAT_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P7_STAT_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P8_STAT_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P9_STAT_ID + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NAME_P10_STAT_ID + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MatchEntry.TABLE_NAME;

    public static String queryMatchById(String matchId) {
        return "SELECT * FROM " + MatchEntry.TABLE_NAME + " WHERE "
                + MatchEntry.COLUMN_NAME_MATCH_ID + "='" + matchId + "'";
    }

    public static String queryMatchIdAndSummId(String matchId, int summonerId) {
        return "SELECT * FROM " + MatchDB.SummonerToMatchEntry.TABLE_NAME +
                " WHERE matchId ='" + matchId + "' AND summonerId='" + summonerId + "'";
    }

    public static String getColNameSumId(int i) {
        return "summ_p" + (i );
    }

    public static String getColNameStatId(int i) {
        return "stat_p" + (i );
    }

    /*
    public static String queryMatchesBySummonerId(int summonerId) {
        return "SELECT * FROM " + MatchEntry.TABLE_NAME + " WHERE "
        + MatchEntry + "=" + summonerId;
    }
    */

    /* Inner class that defines table contents */
    public static abstract class SummonerToMatchEntry implements BaseColumns {
        public static final String TABLE_NAME = "matchToSummoner";
        public static final String COLUMN_NAME_SUMMONER_ID = "summonerId";
        public static final String COLUMN_NAME_MATCH_ID = "matchId";
        public static final String COLUMN_NAME_STATS_ID = "statId";
    }


    public static final String SUMM_SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SummonerToMatchEntry.TABLE_NAME + " (" +
                    SummonerToMatchEntry._ID + " INTEGER PRIMARY KEY," +
                    SummonerToMatchEntry.COLUMN_NAME_SUMMONER_ID + TEXT_TYPE + COMMA_SEP +
                    SummonerToMatchEntry.COLUMN_NAME_MATCH_ID + TEXT_TYPE + COMMA_SEP +
                    SummonerToMatchEntry.COLUMN_NAME_STATS_ID + TEXT_TYPE +
                    " )";

    public static final String SUMM_SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SummonerToMatchEntry.TABLE_NAME;

    public static String queryAllMatchesBySummId(int summonerId) {
        return "SELECT * FROM " + SummonerToMatchEntry.TABLE_NAME + " WHERE "
                + SummonerToMatchEntry.COLUMN_NAME_SUMMONER_ID + "='" + summonerId + "'";
    }

}
