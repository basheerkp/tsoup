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
import com.ahmed.tsoup.scrapers.getCloudTorrents
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

    fun loadItems(domains: List<String>, query: String, context: Context) {
        viewModelScope.launch {
            val sorter = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                .getString("sorter", "seeds")
            val comparator = if (sorter == "Sizeasc") compareBy<TorrentVM> {
                println(sorter)
                it.size
            }
            else compareByDescending<TorrentVM> {
                println(sorter)
                when (sorter) {
                    "Seeds" -> it.seeds
                    "Leeches" -> it.leeches
                    "Sizedesc" -> it.size
                    else -> it.seeds
                }
            }
            coroutineScope {
                val results = formatURL(domains, query)

                domains.forEach { domain ->
                    async {
                        when (domain) {
                            "https://1337x.to" -> processDomain(
                                results[domains.indexOf(domain)], ::get1337x, comparator
                            )

                            "https://bitsearch.to" -> processDomain(
                                results[domains.indexOf(domain)], ::getBitSearch, comparator
                            )

                            "https://cloudtorrents.com" -> processDomain(
                                results[domains.indexOf(domain)], ::getCloudTorrents, comparator
                            )

                            "https://knaben.eu" -> processDomain(
                                results[domains.indexOf(domain)], ::getKnaben, comparator
                            )

                            "https://torrentgalaxy.to" -> processDomain(
                                results[domains.indexOf(domain)], ::getTorrentGalaxy, comparator
                            )

                            "https://torrentquest.com" -> processDomain(
                                results[domains.indexOf(domain)], ::getTorrentQuest, comparator
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
        comparator: Comparator<TorrentVM>
    ) {
        var exit = false
        for (url in urls) {
            delay(1500L.milliseconds)
            collector(url).onEach { item ->
                if (item.title == "None" && _torrentItems.isNotEmpty()) {
                    exit = true
                    return@onEach
                }
                if (_torrentItems.none { it.seeds == item.seeds && it.leeches == item.leeches && it.title == item.title && it.size == item.size }) addSorted(
                    item, comparator
                )
            }.launchIn(viewModelScope)
            if (exit) break
        Log.d("TorrentItems", "processDomain: $_torrentItems")
        }
        return
    }

    fun addSorted(item: TorrentVM, comparator: Comparator<TorrentVM>) {
        val index = _torrentItems.binarySearch(item, comparator)
        if (index < 0) {
            if (_torrentItems.isEmpty()) _torrentItems.add(item)
            else _torrentItems.add(-index - 1, item)
        }
    }

}

fun formatURL(domains: List<String>, query: String): List<List<String>> {
    val result = mutableListOf<List<String>>()
    domains.forEach { domain ->
        when (domain) {

            "https://1337x.to" -> result.add(
                listOf(
                    "$domain/search/$query/1/",
                    "$domain/search/$query/2/",
                    "$domain/search/$query/3/",
                    "$domain/search/$query/4/"
                )
            )

            "https://torrentgalaxy.to" -> result.add(
                listOf(
                    "$domain/torrents.php?search=$query&sort=id&page=0&sort=seeders&order=desc",
                    "$domain/torrents.php?search=$query&sort=id&page=1&sort=seeders&order=desc",
                    "$domain/torrents.php?search=$query&sort=id&page=2&sort=seeders&order=desc",
                    "$domain/torrents.php?search=$query&sort=id&page=3&sort=seeders&order=desc"
                )
            )

            "https://torrentquest.com" -> result.add(
                listOf(
                    "$domain/${query[0]}/${query.replace(" ", "-")}/se/desc/1/",
                    "$domain/${query[0]}/${query.replace(" ", "-")}/se/desc/2/",
                    "$domain/${query[0]}/${query.replace(" ", "-")}/se/desc/3/",
                    "$domain/${query[0]}/${query.replace(" ", "-")}/se/desc/4/"
                )
            )

            "https://knaben.eu" -> result.add(
                listOf(
                    "$domain/search/${query.replace(" ", "%20")}/0/1/seeders",
                    "$domain/search/${query.replace(" ", "%20")}/0/2/seeders",
                    "$domain/search/${query.replace(" ", "%20")}/0/3/seeders",
                    "$domain/search/${query.replace(" ", "%20")}/0/4/seeders"
                )
            )

            "https://cloudtorrents.com" -> result.add(
                listOf(
                    "$domain/search?offset=0&query=$query&ordering=-se",
                    "$domain/search?offset=50&query=$query&ordering=-se",
                    "$domain/search?offset=100&query=$query&ordering=-se",
                    "$domain/search?offset=150&query=$query&ordering=-se"
                )
            )

            "https://bitsearch.to" -> result.add(
                listOf(
                    "$domain/search?q=$query&page=1&sort=seeders",
                    "$domain/search?q=$query&page=2&sort=seeders",
                    "$domain/search?q=$query&page=3&sort=seeders",
                    "$domain/search?q=$query&page=4&sort=seeders"
                )
            )

            else -> result.add(listOf(""))
        }
    }
    return result
}


fun sizeFormatter(size: String): Float {
    if (size.contains("G")) {
        return size.slice(0..size.length - 4).toFloat() * 1024 * 1024
    } else if (size.contains("M")) {
        return size.slice(0..size.length - 4).toFloat() * 1024
    } else if (size.contains("K")) {
        return size.slice(0..size.length - 4).toFloat()
    }
    return 0f
}