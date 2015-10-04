package com.saladmuffin.leagueapi.databases;

import android.provider.BaseColumns;

/**
 * Created by SaladMuffin on 02/10/2015.
 */
public class SummonerSpellDB {
    public SummonerSpellDB() {}

    /* Inner class that defines table contents */
    public static abstract class SummonerSpellEntry implements BaseColumns {
        public static final String TABLE_NAME = "summonerSpells";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SPELL_ID = "spellid";
    }


    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SummonerSpellEntry.TABLE_NAME + " (" +
                    SummonerSpellEntry._ID + " INTEGER PRIMARY KEY," +
                    SummonerSpellEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    SummonerSpellEntry.COLUMN_NAME_SPELL_ID + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SummonerSpellEntry.TABLE_NAME;

    public static String querySumSpellById(int id) {
        return "SELECT * FROM " + SummonerSpellEntry.TABLE_NAME + " WHERE "
                + SummonerSpellEntry.COLUMN_NAME_SPELL_ID + "=" + id;
    }

}
