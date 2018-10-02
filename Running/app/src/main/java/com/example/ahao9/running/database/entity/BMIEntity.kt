package com.example.ahao9.running.database.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:35 2018/10/1
 * @ Description：Build for Metropolia project
 */

@Entity(tableName = "BMI")
data class BMIEntity(

        @PrimaryKey(autoGenerate = true)
        val id: Long,

        val weight: String,
        val height: String,
        val bmi: String,
        val time: String
)