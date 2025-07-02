package com.mihudev.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import java.io.IOException

class ImageCache {

    private val memoryCache: LruCache<String, Bitmap> = object :
        LruCache<String, Bitmap>(calculateCacheSize()) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024 // KB
        }
    }

    fun getBitmapFromMemory(url: String): Bitmap? {
        return memoryCache.get(url)
    }

    fun getBitmapFromDisk(context: Context, url: String): Bitmap? {
        val file = getCachedFile(context, url)
        if (!file.exists()) return null

        // Invalidate if older than 4 hours
        if (System.currentTimeMillis() - file.lastModified() > FOUR_HOURS_MS) {
            file.delete()
            return null
        }

        return try {
            BitmapFactory.decodeFile(file.absolutePath)?.also {
                memoryCache.put(url, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmap(context: Context, url: String, bitmap: Bitmap) {
        memoryCache.put(url, bitmap)

        val file = getCachedFile(context, url)
        try {
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun clearMemoryCache() {
        memoryCache.evictAll()
    }

    fun clearDiskCache(context: Context) {
        val dir = File(context.cacheDir, CACHE_DIR)
        if (dir.exists()) {
            dir.listFiles()?.forEach {
                try {
                    it.delete()
                } catch (_: Exception) { }
            }
        }
    }

    private fun calculateCacheSize(): Int {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt() // KB
        return maxMemory / 8 // Use 1/8th of available memory for cache
    }

    private fun getCachedFile(context: Context, url: String): File {
        val safeFileName = url.hashCode().toString()
        val dir = File(context.cacheDir, CACHE_DIR)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, safeFileName)
    }

    companion object {
        private const val FOUR_HOURS_MS = 4 * 60 * 60 * 1000
        private const val CACHE_DIR = "image_cache"
    }
}
