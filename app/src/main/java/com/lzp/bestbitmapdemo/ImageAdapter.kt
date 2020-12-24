package com.lzp.bestbitmapdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

/**
 * @author li.zhipeng
 * */
class ImageAdapter(
    private val data: List<Int>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun newInstance(itemView: View): ImageViewHolder {
                return ImageViewHolder(itemView)
            }
        }

        private val imageView = itemView.findViewById<ImageView>(R.id.item)

        fun bindData(id: Int) {
            BestBitmapUtil.loadBitmapToImageView(imageView, id)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder.newInstance(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int = data.size
}