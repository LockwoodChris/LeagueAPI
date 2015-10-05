package com.saladmuffin.leagueapi.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by SaladMuffin on 05/10/2015.
 */
public class MatchListFragmentPageAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String name;
    private String tabTitles[] = new String[] { "Matches", "Ranked", "Other" };
    private Context context;

    public MatchListFragmentPageAdapter(FragmentManager fm, Context context, String name) {
        super(fm);
        this.context = context;
        this.name = name;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return MatchListFragment.newInstance(position, name);
            case 1:
                return PlaceholderFragment.newInstance("2", "2");
            case 2:
                return PlaceholderFragment.newInstance("3", "3");
            default:
                return PlaceholderFragment.newInstance("messed", "up");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
