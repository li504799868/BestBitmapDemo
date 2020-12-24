package com.lzp.bestbitmapdemo

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred

/**
 * @author li.zhipeng
 *
 *      图片加载任务管理类，防止创建重复任务
 * */
object BitmapTaskManager {

    private val taskSet = HashMap<String, Deferred<Bitmap>>()

    fun contains(key: String) = taskSet.contains(key)

    fun add(key: String, task: Deferred<Bitmap>) {
        taskSet[key] = task
    }

    fun get(key: String) = taskSet[key]

    fun remove(key: String) {
        taskSet.remove(key)
    }
}