package com.example.ahao9.running.database.entity

import com.google.firebase.database.Exclude

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 17:41 2018/10/6
 * @ Description：Build for Metropolia project
 */
data class TestUpload(var mName:String = "No Name", var mImageUrl:String) {

    private lateinit var mKey: String

    constructor() :this("No Name",""){
    }

    @Exclude
    fun getKey(): String {
        return mKey
    }

    @Exclude
    fun setKey(key: String) {
        mKey = key
    }
}