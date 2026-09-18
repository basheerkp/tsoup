package com.ahmed.tsoup.scrapers

import android.util.Log
import com.ahmed.tsoup.TorrentVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

fun getBitSearch(url: String): Flow<TorrentVM> = flow {
    println("getBitSearch started for URL: $url")
    try {
        val response = URL(url).readText()
        val results = JSONObject(response).getJSONArray("results")

        if (results.length() == 0) {
            emit(TorrentVM("empty", 0f, 0, 0, "2", "", ""))
            return@flow
        }
        
        for (i in 0 until results.length()) {
            val item = results.getJSONObject(i)
            val infohash = item.getString("infohash")
            val magnet = "magnet:?xt=urn:btih:$infohash"

            emit(
                TorrentVM(
                    title = item.getString("title"),
                    size = item.getLong("size").toFloat(),  // already in bytes
                    seeds = item.getInt("seeders"),
                    leeches = item.getInt("leechers"),
                    uploader = "BitSearch",
                    magnet = magnet,
                    date = item.getString("updatedAt").substring(0, 10)
                )
            )
        }
    } catch (e: Exception) {
        println("EXCEPTION: ${e.javaClass.simpleName} - ${e.message}")
        e.printStackTrace()
        emit(TorrentVM("None", 0f, 0, 0, "2", "", ""))
    }
}.flowOn(Dispatchers.IO)