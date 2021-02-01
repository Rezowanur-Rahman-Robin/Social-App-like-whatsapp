package com.robin.robchat;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabAccessAdaptor extends FragmentPagerAdapter {


    public TabAccessAdaptor(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:
                ChatFragment chatFragment=new ChatFragment();
                return chatFragment;

            case 1:
                GroupFragment groupFragment =new GroupFragment();
                return groupFragment;

            case 2:
                ContextFragment contextFragment=new ContextFragment();
                return contextFragment;

            case 3:
                RequestFragment requestFragment=new RequestFragment();
                return requestFragment;


            default:
                return null;

        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){

            case 0:
                return "Chats";


            case 1:
                return "Groups";


            case 2:
                return "Contacts";

            case 3:
                return "Requests";


            default:
                return null;

        }

    }
}
