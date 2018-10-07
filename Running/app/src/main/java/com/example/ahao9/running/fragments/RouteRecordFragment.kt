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
import com.example.ahao9.running.model.RunningRecordEntity
import com.google.firebase.database.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 20:44 2018/10/3
 * @ Description：Build for Metropolia project
 */
class RouteRecordFragment: Fragment() {
    private lateinit var mListAdapter: RunningRecordAdapter
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var rvRecords: RecyclerView
    private var type: Int = 1
    private val mHandler = Handler()
    private lateinit var mDatabaseRef: DatabaseReference
    private var mDBListener: ValueEventListener? = null
    private var routeRecordsList: MutableList<RunningRecordEntity> = mutableListOf()

    companion object {
        const val ID = "type"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        type = arguments!![ID] as Int
        return inflater.inflate(R.layout.route_record_recycle_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("RunningPath")

        mDBListener = mDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                toast(databaseError.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                routeRecordsList.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val record = postSnapshot.getValue(RunningRecordEntity::class.java)
                    if (record != null) {
                        if (record.itemType == type) {
                            record.setKey(postSnapshot.key)
                            routeRecordsList.add(record)
                        }
                    }
                }
                mListAdapter.notifyDataSetChanged()
            }
        })

        refreshLayout = view.findViewById(R.id.refreshLayout) as SwipeRefreshLayout
        rvRecords = view.findViewById(R.id.rvRecords) as RecyclerView

        mListAdapter = RunningRecordAdapter(routeRecordsList, context!!)
        setUpRecycleView()
        setUpListeners()
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
            }, 1000)
        }

        mListAdapter.setOnItemClickListener(object : RunningRecordAdapter.MyItemClickListener {
            override fun onItemClick(view: View, postion: Int) {
                startActivity<RouteRecordActivity>("path" to routeRecordsList[postion] )
            }
        })
    }
}