package com.mihudev.imageloader

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object ImageLoader {

    private val cache = ImageCache()
    private val downloader = ImageDownloader()
    private val inProgress = ConcurrentHashMap<String, MutableList<ImageView>>()
    private val downloadDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
    private val mainScope = CoroutineScope(Dispatchers.Main)

    @JvmStatic
    fun loadImage(
        context: Context,
        url: String,
        imageView: ImageView,
        placeholder: Drawable? = null,
        errorDrawable: Drawable? = null
    ) {
        imageView.setImageDrawable(placeholder)
        imageView.tag = url

        val cachedBitmap = cache.getBitmapFromMemory(url)
        if (cachedBitmap != null) {
            if (imageView.tag == url) {
                imageView.setImageBitmap(cachedBitmap)
            }
            return
        }

        val existing = inProgress.putIfAbsent(url, mutableListOf(imageView))
        if (existing != null) {
            existing.add(imageView)
            return
        }

        mainScope.launch {
            val bitmap = withContext(downloadDispatcher) {
                cache.getBitmapFromDisk(context, url) ?: downloader.downloadImage(url)?.also {
                    cache.saveBitmap(context, url, it)
                }
            }

            val viewsToUpdate = inProgress.remove(url) ?: emptyList()


            if (bitmap != null) {
                viewsToUpdate.forEach { iv ->
                    if (iv.tag == url) {
                        iv.setImageBitmap(bitmap)
                    }
                }
            } else {
                Log.w("ImageLoader", "Failed to load image: $url")
                viewsToUpdate.forEach { iv ->
                    if (iv.tag == url) {
                        iv.setImageDrawable(errorDrawable ?: placeholder)
                    }
                }
            }
        }
    }

    @JvmStatic
    fun invalidateCache(context: Context) {
        cache.clearMemoryCache()
        cache.clearDiskCache(context)
    }
}