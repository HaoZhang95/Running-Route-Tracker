package com.example.ahao9.running.fragments

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ahao9.running.R

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class HistoryFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.history_layout,container,false)
    }
}