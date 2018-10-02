package com.example.ahao9.running.database.entity

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import org.jetbrains.anko.doAsync


/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:04 2018/9/3
 * @ Description：Build for Metropolia project
 */
class BMIViewModel (application: Application):
        AndroidViewModel(application) {

    private var bmiDao = BMIDatabase.getInstance(application).bmiDao()

    fun delete(bmiEntity: BMIEntity) {
        doAsync {
            bmiDao.delete(bmiEntity)
        }
    }

    fun insert(bmiEntity: BMIEntity) {
        doAsync {
            bmiDao.insert(bmiEntity)
        }
    }

    fun update(bmiEntity: BMIEntity) {
        doAsync {
            bmiDao.update(bmiEntity)
        }
    }

    fun findAll(): LiveData<List<BMIEntity>> {
        return bmiDao.findAll()
    }

    fun findLatest10(): LiveData<List<BMIEntity>> {
        return bmiDao.findLatest10()
    }

}