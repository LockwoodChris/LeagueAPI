package com.saladmuffin.leagueapi.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.saladmuffin.leagueapi.MainActivity;
import com.saladmuffin.leagueapi.R;
import com.saladmuffin.leagueapi.databases.MatchDB;
import com.saladmuffin.leagueapi.databases.MatchFetcherDbHelper;
import com.saladmuffin.leagueapi.databases.SummonerDB;
import com.saladmuffin.leagueapi.databases.SummonerFetcherDbHelper;

public class MatchListFragment extends ListFragment {

    private static final String ARG_PAGE = "ARG_PAGE";
    private static String summonerName;
    private OnFragmentInteractionListener mListener;
    private ListView matchHistoryList;

    public static MatchListFragment newInstance(int position) {
        MatchListFragment fragment = new MatchListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        fragment.setArguments(args);
        return fragment;
    }

    public MatchListFragment() {
        if (getArguments() != null) {
            summonerName = getArguments().getString(MainActivity.SUMMONER_NAME);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            summonerName = getArguments().getString(MainActivity.SUMMONER_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        matchHistoryList = getListView();
        matchHistoryList.setVisibility(View.VISIBLE);
        int currId = getSummonerId(summonerName);
        if (currId != -1) Downloader.getInstance(getActivity()).getMatchHistory(currId, matchHistoryList);
        else Downloader.getInstance(getActivity()).getSummonerInfo(summonerName,matchHistoryList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        matchHistoryList.setVisibility(View.GONE);
        MatchInformationFragment fragment = MatchInformationFragment.newInstance();
        Bundle args = new Bundle();
        args.putInt("position",position);
        args.putLong("id",id);
        args.putInt("matchHistoryListID",matchHistoryList.getId());
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private int getSummonerId(String name) {

        int id = -1;

        SummonerFetcherDbHelper mDbHelper = new SummonerFetcherDbHelper(getActivity());

        // Gets the data repository in read mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();


        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                SummonerDB.SummonerEntry._ID,
                SummonerDB.SummonerEntry.COLUMN_NAME_NAME,
                SummonerDB.SummonerEntry.COLUMN_NAME_SUMMONER_ID
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                SummonerDB.SummonerEntry.COLUMN_NAME_NAME + " DESC";

        Cursor c = db.query(
                SummonerDB.SummonerEntry.TABLE_NAME,      // The table to query
                projection,                               // The columns to return
                SummonerDB.SummonerEntry.COLUMN_NAME_NAME + "='" + name + "'",// The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        boolean found = c.moveToFirst();
        if (found) {
            id = c.getInt(c.getColumnIndex(SummonerDB.SummonerEntry.COLUMN_NAME_SUMMONER_ID));
        }
        db.close();
        return id;
    }

    private void clearMatchDatabase() {
        Log.d("MatchDB_CLEARING", "clearing Match Database");
        MatchFetcherDbHelper mDbHelper = new MatchFetcherDbHelper(getActivity());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(MatchDB.MatchEntry.TABLE_NAME, null, null);
        db.close();
        ((MatchHistoryAdapter) matchHistoryList.getAdapter()).notifyDataSetChanged();
    }



}
