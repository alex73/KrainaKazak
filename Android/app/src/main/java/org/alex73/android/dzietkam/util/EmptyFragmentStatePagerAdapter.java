package org.alex73.android.dzietkam.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class EmptyFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    public EmptyFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Fragment getItem(int arg0) {
        return null;
    }
}
