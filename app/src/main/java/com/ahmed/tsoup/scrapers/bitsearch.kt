package com.ahmed.tsoup.scrapers


import com.ahmed.tsoup.TorrentVM
import com.ahmed.tsoup.sizeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun getBitSearch(url: String): Flow<TorrentVM> = flow {
    try {
        val doc: Document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .timeout(10000).get()

        println(url)

        val links = doc.select("li")

        if (doc.select("li").isEmpty() && doc.text().isNotEmpty()) emit(
            TorrentVM(
                "None", 0f, 0, 0, "2", "", ""
            )
        )


        val newLinks = links.subList(0, links.lastIndex + 1)

        for (link in newLinks) {
            val stats = link.select(".stats")

            val magnet = link.select("a[href*=magnet]").first()?.absUrl("href")

            val currentItem = TorrentVM(
                title = link.select("h5").text(),
                size = sizeFormatter(
                    stats.select("div:has(img[alt=Size])").text().split(" ")
                        .getOrNull(1) + stats.select("div:has(img[alt=Size])").text().split(" ")
                        .getOrNull(2)
                ),
                seeds = 0,
                leeches = 0,
                uploader = "BitSearch",
                magnet = magnet.toString(),
                date = (stats.select("div:has(img[alt=Seeder])").text().split(" ")
                    .getOrNull(5) + " " + stats.select("div:has(img[alt=Seeder])").text().split(" ")
                    .getOrNull(6) + " " + stats.select("div:has(img[alt=Seeder])").text().split(" ")
                    .getOrNull(7))
            )
            if (magnet != null) {
                val seedLeeches = listOf(
                    stats.select("div:has(img[alt=Seeder])").text().split(" ").getOrNull(3)!!,
                    stats.select("div:has(img[alt=Seeder])").text().split(" ").getOrNull(4)!!
                )
                val intSeedLeeches = seedLeechFormatter(seedLeeches)
                currentItem.seeds = intSeedLeeches.first
                currentItem.leeches = intSeedLeeches.second
                emit(currentItem)
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
        emit(TorrentVM("None", 0f, 0, 0, "", "", ""))

    }
}.flowOn(Dispatchers.IO)


fun seedLeechFormatter(values: List<String>): Pair<Int, Int> {

    //converts x.yK to x000 + y00 for both seeds and leeches


    val seeds = if (values[0].contains('K') || values[0].contains('B')) {
        val splits = values[0].split(".")
        if (splits.size == 1) {
            splits[0][0].digitToInt() * 1000
        } else {
            val thousands = splits[0].toInt() * 1000
            val hundreds = splits[1][0].digitToInt() * 100
            thousands + hundreds
        }
    } else values[0].toInt()

    val leeches = if (values[1].contains('K') || values[1].contains('B')) {
        val splits = values[1].split(".")
        if (splits.size == 1) {
            splits[0][0].digitToInt() * 1000
        } else {
            val thousands = splits[0].toInt() * 1000
            val hundreds = splits[1][0].digitToInt() * 100
            thousands + hundreds
        }
    } else values[1].toInt()

    return Pair(seeds, leeches)
}