package com.example.ahao9.running.database.entity

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context


/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 10:16 2018/9/3
 * @ Description：Build for Metropolia project
 */

@Database(entities = [(BMIEntity::class)],
        version = 2)
abstract class BMIDatabase: RoomDatabase() {
    abstract fun bmiDao(): BMIDao

    /* singleton - one and only one instance */
    companion object {
        private var INSTANCE: BMIDatabase? = null
        @Synchronized
        fun getInstance(context: Context): BMIDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                        BMIDatabase::class.java,
                        "bmi.db")
                        .build()
            }
            return INSTANCE!!
        }
    }
}