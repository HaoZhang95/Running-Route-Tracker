package com.example.ahao9.running.database.entity

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 10:09 2018/9/3
 * @ Description：Build for Metropolia project
 */

@Dao
interface BMIDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bmiEntity: BMIEntity): Long

    @Update
    fun update(bmiEntity: BMIEntity)

    @Delete
    fun delete(bmiEntity: BMIEntity)

    @Query("SELECT * FROM BMI")
    fun findAll(): LiveData<List<BMIEntity>>

    @Query("select * from BMI order by time desc limit 10")
    fun findLatest10(): LiveData<List<BMIEntity>>
}