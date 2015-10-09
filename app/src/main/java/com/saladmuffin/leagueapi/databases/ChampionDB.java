package com.saladmuffin.leagueapi.databases;

import android.provider.BaseColumns;

/**
 * Created by SaladMuffin on 30/09/2015.
 */
public class ChampionDB {

    public ChampionDB() {}

    public static abstract class ChampionEntry implements BaseColumns {
        public static final String TABLE_NAME = "champions";
        public static final String COLUMN_NAME_CHAMPION_NAME = "name";
        public static final String COLUMN_NAME_CHAMPION_ID = "champId";
        public static final String COLUMN_NAME_CHAMPION_TITLE = "title";
    }

    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ChampionEntry.TABLE_NAME + " (" +
                    ChampionEntry._ID + " INTEGER PRIMARY KEY," +
                    ChampionEntry.COLUMN_NAME_CHAMPION_NAME + TEXT_TYPE + COMMA_SEP +
                    ChampionEntry.COLUMN_NAME_CHAMPION_ID + TEXT_TYPE + COMMA_SEP +
                    ChampionEntry.COLUMN_NAME_CHAMPION_TITLE + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ChampionEntry.TABLE_NAME;

    public static String queryChampionById(long id) {
        return "SELECT * FROM " + ChampionEntry.TABLE_NAME + " WHERE "
                + ChampionEntry.COLUMN_NAME_CHAMPION_ID + "=" + id;
    }
}
