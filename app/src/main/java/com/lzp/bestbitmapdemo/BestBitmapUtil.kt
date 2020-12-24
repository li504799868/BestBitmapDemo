package com.lzp.bestbitmapdemo

import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import kotlinx.coroutines.*

/**
 * @author li.zhipeng
 *
 *      图片加载工具类
 * */
object BestBitmapUtil {

    /**
     * 加载图片
     * */
    fun loadBitmapToImageView(imageView: ImageView, @DrawableRes id: Int) {

        val coroutineScope = getCoroutineScope(imageView.context) ?: return
        val taskKey = BitmapCachePool.generateKey(id)
        imageView.tag = taskKey
        coroutineScope.launch {

            // 优先从缓存中找
            var result = BitmapCachePool.get(taskKey)

            if (result == null) {
                // 在IO线程中做图片的加载缩放处理
                withContext(Dispatchers.IO) {
                    // 已经有相同的图片正在加载，等待任务结果返回
                    result = if (BitmapTaskManager.contains(taskKey)) {
                        Log.i("BestBitmapUtil", "wait task result")
                        BitmapTaskManager.get(taskKey)!!.await()
                    } else {
                        // 创建新的异步任务
                        val task = async {
                            loadResource(imageView, id)
                                .apply {
                                    // 加入缓存
                                    BitmapCachePool.put(taskKey, this)
                                }
                        }
                        // 加入任务队列中
                        BitmapTaskManager.add(taskKey, task)
                        task.await()
                    }
                    //任务结束，移除管理栈
                    BitmapTaskManager.remove(taskKey)
                }
            } else {
                Log.i("BestBitmapUtil", "load from cache")
            }

            Log.i("BestBitmapUtil", "setImageBitmap: $imageView")
            if (imageView.tag == taskKey) {
                imageView.setImageBitmap(result)
            }
        }

    }

    private suspend fun loadResource(imageView: ImageView, @DrawableRes id: Int) = coroutineScope {

//                Log.i("BestBitmapUtil", "delay 2000ms")
        // 模拟加载时间超过2s
//                delay(2_000)

        // 获取图片的原始尺寸
        val options = getOriginalSizeOption(imageView.context, id)
        Log.i("BestBitmapUtil", "original width:${options.outWidth}")
        Log.i("BestBitmapUtil", "original width:${options.outHeight}")

        // 计算图片的缩放比例
        val layoutPrams = imageView.layoutParams
        val inSampleSize =
            calculateInSampleSize(options, layoutPrams.width, layoutPrams.height)
        Log.i("BestBitmapUtil", "inSampleSize:${inSampleSize}")

        // 最终加载图片
        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false

        // 设置可以复用的Bitmap
        BitmapCachePool.getReusableBitmap(options)

        // 禁止系统根据屏幕密度进行尺寸换算
        // 否则会与option.outWidth的大小不一致，例如在xxhdpi的设备中option.outWidth=300，但是bitmap.width=900，设置为false，bitmap.width=300
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(imageView.resources, id, options)
        Log.i("BestBitmapUtil", "result width:${bitmap.width}")
        Log.i("BestBitmapUtil", "result height:${bitmap.height}")

        return@coroutineScope bitmap
    }

    private fun getOriginalSizeOption(
        context: Context,
        @DrawableRes id: Int
    ): BitmapFactory.Options {
        return BitmapFactory.Options().apply {
            this.inJustDecodeBounds = true
            BitmapFactory.decodeResource(context.resources, id, this)
        }
    }

    private fun calculateInSampleSize(
        option: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {

        val (width: Int, height: Int) = option.run { outWidth to outHeight }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfWidth = height / 2
            val halfHeight = width / 2

            while (halfHeight / inSampleSize > reqHeight || halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 获取协程的上下文
     * */
    private fun getCoroutineScope(context: Context?): CoroutineScope? {
        var contextTemp = context
        if (null != contextTemp) {
            while (contextTemp is ContextWrapper) {
                if (contextTemp is CoroutineScope) {
                    return contextTemp
                }
                contextTemp = contextTemp.baseContext
            }
        }
        return null
    }

}