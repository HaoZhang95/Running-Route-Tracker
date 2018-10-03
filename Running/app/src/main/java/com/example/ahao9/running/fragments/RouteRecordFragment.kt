package com.example.ahao9.running.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.ahao9.running.R
import com.example.ahao9.running.activities.RouteRecordActivity
import com.example.ahao9.running.adapters.RunningRecordAdapter
import com.example.ahao9.running.model.RunningRecordBean
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import java.util.ArrayList

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 20:44 2018/10/3
 * @ Description：Build for Metropolia project
 */
class RouteRecordFragment: Fragment() {

    private val fakeDataList = ArrayList<RunningRecordBean>()
    private lateinit var mListAdapter: RunningRecordAdapter
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var rvRecords: RecyclerView
    private var type: Int = 1
    private val mHandler = Handler()

    companion object {
        const val ID = "type"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        type = arguments!![ID] as Int
        return inflater.inflate(R.layout.route_record_recycle_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshLayout = view.findViewById(R.id.refreshLayout) as SwipeRefreshLayout
        rvRecords = view.findViewById(R.id.rvRecords) as RecyclerView

        setUpFakeData()

        setUpListeners()

        setUpRecycleView()
    }

    private fun setUpRecycleView() {
        val layoutManger = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        rvRecords.layoutManager = layoutManger
        rvRecords.adapter = mListAdapter
    }

    private fun setUpListeners() {
        refreshLayout.setColorSchemeColors(Color.RED, Color.BLUE)
        refreshLayout.setOnRefreshListener {
            mHandler.postDelayed({
                refreshLayout.isRefreshing = false
                toast("Refresh not implement yet")
            }, 1000)
        }

        mListAdapter.setOnItemClickListener(object : RunningRecordAdapter.MyItemClickListener {
            override fun onItemClick(view: View, postion: Int) {
                toast("Type: ${this@RouteRecordFragment.type} ---  Distance: ${fakeDataList[postion].distance}")
                startActivity<RouteRecordActivity>()
            }
        })
    }

    private fun setUpFakeData() {

        val runningRecordBean1 = RunningRecordBean(1,"2.4", "0:00:12")
        val runningRecordBean2 = RunningRecordBean(1,"2.4", "0:00:12")
        val runningRecordBean3 = RunningRecordBean(1,"2.4", "0:00:12")
        val runningRecordBean4 = RunningRecordBean(2,"2.4", "0:00:12")
        val runningRecordBean5 = RunningRecordBean(2,"2.4", "0:00:12")
        val runningRecordBean6 = RunningRecordBean(2,"2.4", "0:00:12")

        if (type == 1) {
            fakeDataList.add(runningRecordBean1)
            fakeDataList.add(runningRecordBean2)
            fakeDataList.add(runningRecordBean3)
        } else {
            fakeDataList.add(runningRecordBean4)
            fakeDataList.add(runningRecordBean5)
            fakeDataList.add(runningRecordBean6)
        }

        mListAdapter = RunningRecordAdapter(fakeDataList, context!!)
    }

}