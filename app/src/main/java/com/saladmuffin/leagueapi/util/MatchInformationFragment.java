package com.saladmuffin.leagueapi.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.saladmuffin.leagueapi.databases.PlayerStatsDB;
import com.saladmuffin.leagueapi.databases.PlayerStatsFetcherDbHelper;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MatchInformationFragment extends Fragment {

    private int matchHistoryListID;

    private OnFragmentInteractionListener mListener;

    public static MatchInformationFragment newInstance() {
        MatchInformationFragment fragment = new MatchInformationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MatchInformationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            matchHistoryListID = getArguments().getInt("matchHistoryListID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_match_information, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant1), 1);
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant2), 2);
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant3), 3);
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant4), 4);
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant5), 5);
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant6), 6);
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant7), 7);
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant8), 8);
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant9), 9);
        setParticipantInformation(view.findViewById(R.id.match_info_container_participant10), 10);
    }

    private void setParticipantInformation(View view, int partNum) {
        TextView tSummonerName = (TextView) view.findViewById(R.id.match_info_name);
        TextView tSummonerScore = (TextView) view.findViewById(R.id.match_info_kda);
        TextView tSummonerGold = (TextView) view.findViewById(R.id.match_info_gold);
        TextView tSummonerCreeps = (TextView) view.findViewById(R.id.match_info_minions);
        TextView tSummonerDamage = (TextView) view.findViewById(R.id.match_info_damage);
        TextView tSummonerWards = (TextView) view.findViewById(R.id.match_info_wards);
        ImageView iChampionIcon = (ImageView) view.findViewById(R.id.match_info_champ_icon);
        ImageView ivItem0 = (ImageView) view.findViewById(R.id.match_info_item0);
        ImageView ivItem1 = (ImageView) view.findViewById(R.id.match_info_item1);
        ImageView ivItem2 = (ImageView) view.findViewById(R.id.match_info_item2);
        ImageView ivItem3 = (ImageView) view.findViewById(R.id.match_info_item3);
        ImageView ivItem4 = (ImageView) view.findViewById(R.id.match_info_item4);
        ImageView ivItem5 = (ImageView) view.findViewById(R.id.match_info_item5);
        ImageView ivItem6 = (ImageView) view.findViewById(R.id.match_info_item6);
        ImageView ivSpell1 = (ImageView) view.findViewById(R.id.matchSummonerSpell1);
        ImageView ivSpell2 = (ImageView) view.findViewById(R.id.matchSummonerSpell2);

        MatchFetcherDbHelper dbHelper = new MatchFetcherDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT " + MatchDB.getColNameStatId(partNum) + " FROM " +
                MatchDB.MatchEntry.TABLE_NAME +
                " WHERE " + MatchDB.MatchEntry._ID + "=" + getArguments().getLong("id"), null);
        mCursor.moveToFirst();
        Log.d("ParticipantLoad", "count = " + mCursor.getCount());
        db.close();
        db = new PlayerStatsFetcherDbHelper(getContext()).getReadableDatabase();
        Cursor statsCursor = db.rawQuery(PlayerStatsDB.queryByRow(
                mCursor.getLong(mCursor.getColumnIndex(MatchDB.getColNameStatId(partNum)))
        ), null);
        statsCursor.moveToFirst();
        db.close();
        if (statsCursor.getCount() != 0) {
            String summonerName = statsCursor.getString(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_SUMMONER_NAME));
            long champId = statsCursor.getLong(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_CHAMPION_ID));

            ChampionFetcherDbHelper mDbHelper = new ChampionFetcherDbHelper(getContext());
            db = mDbHelper.getReadableDatabase();
            Cursor championCursor = db.rawQuery(ChampionDB.queryChampionById(champId), null);
            if (championCursor.getCount() == 0) {
                Downloader.getInstance(getContext()).getChampionInfo((int) champId, null);
            } else if (championCursor.getCount() > 0) {
                championCursor.moveToFirst();
                if (summonerName == null)
                    summonerName = championCursor.getString(championCursor.getColumnIndex(ChampionDB.ChampionEntry.COLUMN_NAME_CHAMPION_NAME));
                loadChampionImage(summonerName, iChampionIcon);
            }
            db.close();
            tSummonerName.setText(summonerName);
            int kills = statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_KILLS));
            int deaths = statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_DEATHS));
            int assists = statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ASSISTS));
            tSummonerScore.setText(" " + kills + "/" + deaths + "/" + assists);
            tSummonerGold.setText(" "+statsCursor.getString(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_GOLD)));
            tSummonerCreeps.setText(statsCursor.getString(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_MINIONS)));
            //tSummonerDamage.setText(statsCursor.getLong(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_DAMAGE)));
            //tSummonerWards.setText(statsCursor.getLong(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_WARDS)));
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_0)), ivItem0);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_1)), ivItem1);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_2)), ivItem2);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_3)), ivItem3);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_4)), ivItem4);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_5)), ivItem5);
            loadItemIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_ITEM_6)), ivItem6);
            //loadSummonerSpellIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_SPELL_1)), ivSpell1);
            //loadSummonerSpellIcon(statsCursor.getInt(statsCursor.getColumnIndex(PlayerStatsDB.PlayerStatsEntry.COLUMN_NAME_SPELL_2)), ivSpell2);
        }
    }

    private void loadChampionImage(String champName, ImageView championImage) {
        Picasso p = Picasso.with(getContext());
        //p.setIndicatorsEnabled(true);
        p.load("http://ddragon.leagueoflegends.com/cdn/5.18.1/img/champion/" + nameForUrl(champName) + ".png").into(championImage);
    }

    private void loadItemIcon(int iconId, ImageView view) {
        Picasso p = Picasso.with(getContext());
        //p.setIndicatorsEnabled(true);
        p.load("http://ddragon.leagueoflegends.com/cdn/5.19.1/img/item/" + iconId + ".png").
                placeholder(getContext().getDrawable(R.drawable.item_placeholder)).into(view);
    }

    private String nameForUrl(String name) {
        return name.replaceAll("[^A-Za-z]","");
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
        getActivity().findViewById(matchHistoryListID).setVisibility(View.VISIBLE);
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

}
