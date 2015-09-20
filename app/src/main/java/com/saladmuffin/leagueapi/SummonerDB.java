package com.saladmuffin.leagueapi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by SaladMuffin on 19/09/2015.
 */
public final class SummonerDB {

    public SummonerDB() {}

    /* Inner class that defines table contents */
    public static abstract class SummonerEntry implements BaseColumns {
        public static final String TABLE_NAME = "summoners";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SUMMONER_ID = "summonerid";
    }


    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SummonerEntry.TABLE_NAME + " (" +
                    SummonerEntry._ID + " INTEGER PRIMARY KEY," +
                    SummonerEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    SummonerEntry.COLUMN_NAME_SUMMONER_ID + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SummonerEntry.TABLE_NAME;

}
