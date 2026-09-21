package com.ahmed.tsoup

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmed.tsoup.scrapers.get1337x
import com.ahmed.tsoup.scrapers.getBitSearch
import com.ahmed.tsoup.scrapers.getYts
import com.ahmed.tsoup.scrapers.getKnaben
import com.ahmed.tsoup.scrapers.getTorrentGalaxy
import com.ahmed.tsoup.scrapers.getTorrentQuest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.milliseconds

data class TorrentVM(
    var title: String,
    var size: Float,
    var seeds: Int,
    var leeches: Int,
    var uploader: String,
    var magnet: String,
    var date: String
)

@Keep
class TorrentItems : ViewModel() {
    private val _torrentItems = mutableStateListOf<TorrentVM>()
    val torrentItems: SnapshotStateList<TorrentVM> = _torrentItems

    // Hardcoded comparator to sort by seeds descending

    fun loadItems(domains: List<String>, query: String, context: Context) {
        viewModelScope.launch {
            coroutineScope {
                val results = formatURL(domains, query)

                domains.forEachIndexed { index, domain ->
                    async {
                        when (domain) {
                            "https://1337x.to" -> processDomain(
                                results[index], ::get1337x,
                            )

                            "https://bitsearch.eu" -> processDomain(
                                results[index], ::getBitSearch,
                            )

                            "https://yts.gg" -> processDomain(
                                results[index], ::getYts,
                            )

                            "https://knaben.eu" -> processDomain(
                                results[index], ::getKnaben,
                            )

                            "https://torrentgalaxy.to" -> processDomain(
                                results[index], ::getTorrentGalaxy,
                            )

                            "https://torrentquest.com" -> processDomain(
                                results[index], ::getTorrentQuest
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun processDomain(
        urls: List<String>,
        collector: (String) -> Flow<TorrentVM>,
    ) {
        var exit = false
        for (url in urls) {
            delay(1000L.milliseconds) // Slightly reduced delay
            collector(url).onEach { item ->
                if ((item.title == "None" || item.title == "empty") && _torrentItems.isNotEmpty()) {
                    exit = true
                    return@onEach
                }

                val isDuplicate = _torrentItems.any {
                    it.title == item.title && it.seeds == item.seeds && it.size == item.size
                }

                if (!isDuplicate && item.title != "None" && item.title != "empty") {
                    addSorted(item, compareByDescending { it.seeds })
                }
            }.launchIn(viewModelScope)

            if (exit) break
        }
        Log.d("TorrentItems", "processDomain finished for one domain")
    }

    private val lock = Mutex()

    private suspend fun addSorted(item: TorrentVM, comparator: Comparator<TorrentVM>) {
        lock.withLock {
            val index = _torrentItems.binarySearch(item, comparator)
            if (index < 0) {
                _torrentItems.add(-index - 1, item)
            }
        }
    }
}

fun formatURL(domains: List<String>, query: String): List<List<String>> {
    val result = mutableListOf<List<String>>()
    val encodedQuery = query.replace(" ", "%20")
    val hyphenQuery = query.replace(" ", "-")

    domains.forEach { domain ->
        when (domain) {
            "https://1337x.to" -> result.add(
                listOf(
                    "$domain/search/$encodedQuery/1/", "$domain/search/$encodedQuery/2/"
                )
            )

            "https://torrentgalaxy.to" -> result.add(
                listOf(
                    "$domain/torrents.php?search=$encodedQuery&sort=seeders&order=desc&page=0",
                    "$domain/torrents.php?search=$encodedQuery&sort=seeders&order=desc&page=1"
                )
            )

            "https://torrentquest.com" -> result.add(
                listOf(
                    "$domain/${query.first().lowercase()}/$hyphenQuery/se/desc/1/",
                    "$domain/${query.first().lowercase()}/$hyphenQuery/se/desc/2/"
                )
            )

            "https://knaben.eu" -> result.add(
                listOf(
                    "$domain/search/$encodedQuery/0/1/seeders",
                    "$domain/search/$encodedQuery/0/2/seeders"
                )
            )

            "https://yts.gg" -> result.add(
                listOf(
                    "$domain/api/v2/list_movies.json?query_term=$encodedQuery&sort_by=seeds&quality=1080p&limit=15"
                )
            )

            "https://bitsearch.eu" -> result.add(
                listOf(
                    "$domain/api/v1/search?q=$encodedQuery&sort=seeders&limit=40"
                )
            )

            else -> result.add(listOf(""))
        }
    }
    return result
}

fun sizeFormatter(size: String): Float {
    val cleanSize = size.uppercase()
    return try {
        when {
            cleanSize.contains("G") -> cleanSize.substringBefore("G").trim()
                .toFloat() * 1024 * 1024 * 1024

            cleanSize.contains("M") -> cleanSize.substringBefore("M").trim().toFloat() * 1024 * 1024
            cleanSize.contains("K") -> cleanSize.substringBefore("K").trim().toFloat() * 1024
            else -> cleanSize.filter { it.isDigit() || it == '.' }.toFloat()
        }
    } catch (e: Exception) {
        0f
    }
}
