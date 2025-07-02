package com.mihudev.imageloader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageDownloader {

    fun downloadImage(urlString: String): Bitmap? {
        var connection: HttpURLConnection? = null
        var input: InputStream? = null

        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000 // ms
            connection.readTimeout = 5000 // ms
            connection.instanceFollowRedirects = true
            connection.doInput = true
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                println("ImageDownloader: Non-200 response: ${connection.responseCode}")
                return null
            }

            input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                input?.close()
            } catch (_: Exception) { }

            connection?.disconnect()
        }
    }
}
