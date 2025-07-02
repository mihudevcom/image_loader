package com.mihudev.imageloader

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ImageAdapter(
    private val context: Context,
    private val items: List<ImageItem>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    // Preload drawables once
    private val placeholder = ContextCompat.getDrawable(context, R.drawable.placeholder)
    private val errorDrawable = ContextCompat.getDrawable(context, R.drawable.error_placeholder)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = "ID: ${item.id}"

        ImageLoader.loadImage(
            context = context,
            url = item.imageUrl,
            imageView = holder.imageView,
            placeholder = placeholder,
            errorDrawable = errorDrawable
        )
    }

    override fun getItemCount(): Int = items.size

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textView: TextView = view.findViewById(R.id.imageIdTextView)
    }
}
