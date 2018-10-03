package com.example.ahao9.running.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:46 2018/10/3
 * @ Description：Build for Metropolia project
 */
class MyPaletteTabAdapter(fragmentManager: FragmentManager,
                          val fragments: List<Fragment>,
                          val titles: Array<String>) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}