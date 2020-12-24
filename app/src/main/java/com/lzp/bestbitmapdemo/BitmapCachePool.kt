package com.lzp.bestbitmapdemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.util.lruCache
import java.lang.ref.SoftReference
import java.util.*
import kotlin.collections.HashMap

/**
 * @author li.zhipeng
 *
 *      图片缓存池
 * */
object BitmapCachePool {

    private val memoryCache = lruCache<String, Bitmap>(
        maxSize = 4 * 1024 * 1024,  // 缓存4M的图片
        sizeOf = { _, value ->
            value.byteCount
        },
        onEntryRemoved = { _, key, oldValue, _ ->
            // 放入软引用复用池
            if (oldValue.isMutable) {
                bitmapRecyclerPool?.put(key, SoftReference(oldValue))
            }
        }
    )

    /**
     * 软引用池
     * */
    private var bitmapRecyclerPool: MutableMap<String, SoftReference<Bitmap>>? = null

    /**
     * 位图复用只支持Android 3.0 及以上
     * */
    private fun hasHoneycomb() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB

    init {
        if (hasHoneycomb()) {
            bitmapRecyclerPool =
                Collections.synchronizedMap(HashMap<String, SoftReference<Bitmap>>())
        }
    }


    fun put(key: String, bitmap: Bitmap) {
        memoryCache.put(key, bitmap)
    }

    fun get(key: String): Bitmap? {
        var result = memoryCache[key]
        if (result == null) {
            bitmapRecyclerPool?.remove(key)?.let {
                result = it.get()?.apply {
                    // 从softReference中移出，加入LruCache
                    memoryCache.put(key, this)
                }
            }
        }
        return result
    }

    fun generateKey(id: Int): String {
        return id.toString()
    }

    fun getReusableBitmap(options: BitmapFactory.Options) {
        bitmapRecyclerPool?.let {
            options.inMutable = true
            val iterator = it.values.iterator()
            while (iterator.hasNext()) {
                val bitmap = iterator.next().get()
                // 已经被回收或不可复用
                if (bitmap == null || !bitmap.isMutable) {
                    iterator.remove()
                }
                // 找到合适的位图
                else if (canUseInBitmap(bitmap, options)) {
                    Log.i("BitmapCachePool", "find reusable bitmap")
                    options.inBitmap = bitmap
                }
            }

        }
    }

    private fun canUseInBitmap(bitmap: Bitmap, options: BitmapFactory.Options): Boolean {
        // 4.4以上需要bitmap的native内存大于等于需要的内存
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val width = options.outWidth / options.inSampleSize
            val height = options.outHeight / options.inSampleSize
            val byteCount = width * height * getBytesPerPixel(bitmap.config)
            byteCount <= bitmap.allocationByteCount
        }
        // Android 3.0 到 Android 4.4 版本之间需要必须宽高要完全匹配
        else {
            bitmap.width == options.outWidth && bitmap.height == options.outHeight && options.inSampleSize == 1
        }
    }

    private fun getBytesPerPixel(config: Bitmap.Config): Int {
        return when (config) {
            Bitmap.Config.ARGB_8888 -> 4
            Bitmap.Config.ARGB_4444, Bitmap.Config.RGB_565 -> 2
            Bitmap.Config.ALPHA_8 -> 1
            else -> 1
        }
    }

}