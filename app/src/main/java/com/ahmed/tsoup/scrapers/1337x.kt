package com.ahmed.tsoup.scrapers

import com.ahmed.tsoup.TorrentVM
import com.ahmed.tsoup.sizeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException

fun get1337x(url: String): Flow<TorrentVM> = flow {
    try {
        println(url)
        val doc: Document = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(5000).get()
        val links = doc.select("a[href^=/torrent]")
        println("running scraper")
        if (doc.select("td").isEmpty() && doc.text().isNotEmpty()) emit(
            TorrentVM(
                "None", 0f, 0, 0, "1", "", ""
            )
        )
        links.forEach { link ->
            val currentItem = TorrentVM(
                title = link.text(),
                size = 0f,
                seeds = 0,
                leeches = 0,
                uploader = "",
                magnet = "",
                date = ""
            )

            val linkUrl = link.absUrl("href")

            try {
                val detailsPage =
                    Jsoup.connect(linkUrl).userAgent("Mozilla/5.0").timeout(15000).get()
                currentItem.magnet =
                    detailsPage.select("a[href*=magnet]").first()?.absUrl("href").toString()


                val listItems = detailsPage.select("li")
                for (item: Element in listItems) {
                    when (item.select("strong").text()) {

                        "Total size" -> currentItem.size = sizeFormatter(item.select("span").text())
                        "Seeders" -> currentItem.seeds = item.select("span").text().toInt()
                        "Leechers" -> currentItem.leeches = item.select("span").text().toInt()

                        "Uploaded By" -> currentItem.uploader = "1337x"
                        "Date uploaded" -> currentItem.date = item.select("span").text()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            emit(currentItem)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        emit(TorrentVM("None", 0f, 0, 0, "1", "", ""))

    }
}.flowOn(Dispatchers.IO)