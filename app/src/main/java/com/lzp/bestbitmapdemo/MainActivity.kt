package com.lzp.bestbitmapdemo

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

//    private lateinit var imageView: ImageView

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        imageView = findViewById(R.id.image)

        // 直接设置Resource使用的是图片的原始尺寸, 默认使用RGB_8888
//        if (imageView.drawable is BitmapDrawable) {

//            Log.i(
//                "lzp",
//                "drawable size: ${(imageView.drawable as BitmapDrawable).bitmap.allocationByteCount}"
//            )
//            Log.i("lzp", "drawable width: ${(imageView.drawable as BitmapDrawable).bitmap.width}")
//            Log.i("lzp", "drawable width: ${(imageView.drawable as BitmapDrawable).bitmap.height}")
//        }

//        BestBitmapUtil.loadBitmapToImageView(imageView, R.drawable.cat)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ImageAdapter(
            arrayListOf(
                R.drawable.cat,
                R.drawable.cat2,
                R.drawable.cat3,
                R.drawable.cat4,
                R.drawable.cat5,
                R.drawable.cat6,
                R.drawable.cat7,
                R.drawable.cat8,
                R.drawable.cat9,
                R.drawable.cat10,
                R.drawable.cat,
                R.drawable.cat2,
                R.drawable.cat3,
                R.drawable.cat4,
                R.drawable.cat5,
                R.drawable.cat6,
                R.drawable.cat7,
                R.drawable.cat8,
                R.drawable.cat9,
                R.drawable.cat10
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}