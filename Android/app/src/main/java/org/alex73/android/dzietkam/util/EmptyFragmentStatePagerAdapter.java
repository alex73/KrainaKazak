package org.alex73.android.dzietkam.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
