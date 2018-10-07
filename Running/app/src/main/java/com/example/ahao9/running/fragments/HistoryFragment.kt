package com.example.ahao9.running.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ahao9.running.R
import com.example.ahao9.running.adapters.MyPaletteTabAdapter
import kotlinx.android.synthetic.main.history_layout.*
import java.util.*

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class HistoryFragment: Fragment() {

    private val titlesArray = arrayOf("Running", "Cycling")
    private val fragmentsArray = ArrayList<RouteRecordFragment>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.history_layout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragments()

        initListener()
    }

    private fun initListener() {

        val tabAdapter = MyPaletteTabAdapter(fragmentManager!!, fragmentsArray, titlesArray)
        vpContent.adapter = tabAdapter
        tabLayout.setupWithViewPager(vpContent)
    }

    private fun setUpFragments() {

        val runningTypeFragment = RouteRecordFragment()
        val bundle1 = Bundle()
        bundle1.putInt(RouteRecordFragment.ID, 1)
        runningTypeFragment.arguments = bundle1
        fragmentsArray.add(runningTypeFragment)

        val cyclingingTypeFragment = RouteRecordFragment()
        val bundle2 = Bundle()
        bundle2.putInt(RouteRecordFragment.ID, 2)
        cyclingingTypeFragment.arguments = bundle2
        fragmentsArray.add(cyclingingTypeFragment)
    }
}