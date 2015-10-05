package com.saladmuffin.leagueapi.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MatchListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MatchListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MatchListFragment extends Fragment {

    private static final String ARG_PAGE = "ARG_PAGE";
    private static final String SUMMONER_NAME = "SUMMONER_NAME";
    private int mPage;
    private OnFragmentInteractionListener mListener;
    private ListView matchHistoryList;

    public static MatchListFragment newInstance(int position, String name) {
        MatchListFragment fragment = new MatchListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        args.putString(SUMMONER_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    public MatchListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_list, container, false);
        matchHistoryList = (ListView) view.findViewById(R.id.matchHistoryList);
        String name = getArguments().getString(SUMMONER_NAME);
        int currId = getSummonerId(name);
        if (currId != -1) Downloader.getInstance(getActivity()).getMatchHistory(name, currId, matchHistoryList);
        else Downloader.getInstance(getActivity()).getSummonerInfo(name,matchHistoryList);;
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
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
