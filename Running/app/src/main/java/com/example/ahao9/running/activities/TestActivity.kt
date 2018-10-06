package com.example.ahao9.running.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.example.ahao9.running.R
import com.example.ahao9.running.adapters.TestActivityAdapter
import com.example.ahao9.running.database.entity.TestUpload
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_test.*
import org.jetbrains.anko.toast


class TestActivity : AppCompatActivity(), TestActivityAdapter.MyOnItemClickListener {

    private lateinit var mAdapter: TestActivityAdapter
    private lateinit var mContext:Context

    private var mUploads: ArrayList<TestUpload> = ArrayList()
    private var mStorage: FirebaseStorage? = null
    private var mDatabaseRef: DatabaseReference? = null
    private var mDBListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(this)

        mContext = this
        mAdapter = TestActivityAdapter(mContext, mUploads)
        recycler_view.adapter = mAdapter
        mAdapter.setOnItemClickListener(this)

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")

        mDBListener = mDatabaseRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                toast(databaseError.message)
                progress_circle.visibility = View.INVISIBLE;
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                mUploads.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val upload = postSnapshot.getValue(TestUpload::class.java)
                    if (upload != null) {
                        upload.setKey(postSnapshot.key)
                        mUploads.add(upload)
                    }
                }
                mAdapter.notifyDataSetChanged()
                progress_circle.visibility = View.INVISIBLE
            }
        })
    }

    override fun onDeleteClick(position: Int) {

        val selectedItem = mUploads[position]
        val selectedKey = selectedItem.getKey()

        val imageRef = mStorage?.getReferenceFromUrl(selectedItem.mImageUrl)
        imageRef?.delete()?.addOnSuccessListener {
            mDatabaseRef?.child(selectedKey)?.removeValue()
            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this, "Normal click at position: $position", Toast.LENGTH_SHORT).show()
    }

    override fun onWhatEverClick(position: Int) {
        Toast.makeText(this, "Whatever click at position: $position", Toast.LENGTH_SHORT).show();
    }

    override fun onDestroy() {
        super.onDestroy()
        mDatabaseRef?.removeEventListener(mDBListener)
    }
}
