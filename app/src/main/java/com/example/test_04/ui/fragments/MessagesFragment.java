package com.example.test_04.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.test_04.R;
import com.example.test_04.adapters.MessagesPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    private ViewPager vpMain;
    private TabLayout tlTabs;

    private MessagesPagerAdapter pagerAdapter;
    private ArrayList<Fragment> fragments = new ArrayList<>();

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        vpMain = view.findViewById(R.id.vp_main);
        tlTabs = view.findViewById(R.id.tl_tabs);

        if (pagerAdapter == null) {
            fragments.add(new NotificationsFragment());
            fragments.add(new InboxFragment());
        }

        pagerAdapter = new MessagesPagerAdapter(getChildFragmentManager(), fragments);
        vpMain.setAdapter(pagerAdapter);
        tlTabs.setupWithViewPager(vpMain);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((TextView)getActivity().findViewById(R.id.tv_title)).setText("Messages");
//        ((ImageView)getActivity().findViewById(R.id.iv_messages)).setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
    }
}
