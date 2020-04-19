package com.example.test_04.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.test_04.ui.fragments.InboxFragment;
import com.example.test_04.ui.fragments.NotificationsFragment;

import java.util.ArrayList;

public class MessagesPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;

    public MessagesPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return fragments.get(0);
            case 1:
                return fragments.get(1);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "NOTIFICATIONS";
            case 1:
                return "INBOX";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}