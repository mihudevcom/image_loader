package com.mihudev.imageloader

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val imageList = mutableListOf<ImageItem>()
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.imageRecyclerView)
        val clearCacheButton = findViewById<Button>(R.id.clearCacheButton)

        adapter = ImageAdapter(this, imageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        clearCacheButton.setOnClickListener {
            ImageLoader.invalidateCache(this)
            Toast.makeText(this, "Cache cleared!", Toast.LENGTH_SHORT).show()
        }

        fetchImages()
    }

    private fun fetchImages() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { downloadJson() }

            if (result.isEmpty()) {
                Toast.makeText(this@MainActivity, "Failed to load images", Toast.LENGTH_LONG).show()
                return@launch
            }

            imageList.clear()
            imageList.addAll(result)
            adapter.notifyDataSetChanged()
        }
    }

    private fun downloadJson(): List<ImageItem> {
        val urlString = "https://zipoapps-storage-test.nyc3.digitaloceanspaces.com/image_list.json"
        val list = mutableListOf<ImageItem>()

        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(json)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getInt("id")
                val imageUrl = obj.getString("imageUrl")
                list.add(ImageItem(id, imageUrl))
            }

            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
