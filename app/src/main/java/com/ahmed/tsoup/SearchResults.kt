package com.ahmed.tsoup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmed.tsoup.ui.theme.TSOUPTheme

class SearchResults : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TSOUPTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 35.dp),
                ) { innerPadding ->
                    val domain = "https://cloudtorrents.com"

                    val config = LocalConfiguration.current
                    val width = LocalWindowInfo.current.containerSize.width
                    val height = LocalWindowInfo.current.containerSize.height
                    Column {
                        Results(
                            modifier = Modifier.padding(innerPadding),
                            domain = domain,
                            height = height.dp,
                            width.dp,
                            query = intent.getStringExtra("url").toString(),
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun Results(
    modifier: Modifier,
    domain: String,
    height: Dp,
    width: Dp,
    viewModel: TorrentItems = viewModel(),
    query: String
) {
    val context = LocalContext.current
    val domains = loadAddress(context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE))
    val domainList = remember { domains.filter { it.enabled }.map { it.domain } }


    val items = viewModel.torrentItems

    LaunchedEffect(query, domainList) {
        if (items.isEmpty()) viewModel.loadItems(domainList, query, context)
    }

    val isListEmpty = items.isEmpty()

    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isListEmpty) {
            Row {
                Text("Loading results for $query")
                Icon(
                    imageVector = Icons.Outlined.Refresh, null, Modifier.size(25.dp)
                )
            }
        } else if (items.size == 6 && items[1].title == "None" && items[2].title == "None" && items[3].title == "None" && items[4].title == "None" && items[5].title == "None" && items[0].title == "None") {
            val context = LocalContext.current

            Toast.makeText(
                context, "No Items Found", Toast.LENGTH_SHORT
            ).show()

            Text("NO RESULTS FOUND")

            Text("CHECK YOUR CONNECTION / QUERY")

            Row {
                TextButton(onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.putExtra("url", query)
                    context.startActivity(intent)
                }) {
                    Text("Go Back")
                }
            }
        } else Column(
            Modifier.width(width), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val boxWidth = width * 0.88f
            val textWidth = width * 0.7f
            val iconWidth = width * 0.2f
            val boxPadding = height * 0.01f

            Text(
                "Showing ${items.size} results for $query",
                fontSize = 20.sp,
                fontStyle = FontStyle.Italic
            )
            LazyColumn(
                Modifier
                    .padding(vertical = boxPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(items, key = null) { item ->
                    val context = LocalContext.current
                    if (item.title == "None")
                    else Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surfaceBright
                            )
                            .padding(10.dp, 5.dp)
                            .width(boxWidth),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Absolute.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(verticalArrangement = Arrangement.Center) {

                                Text(item.title, Modifier.width(textWidth))

                                Spacer(Modifier.height(15.dp))

                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    Text(
                                        text = "Seeds: ${item.seeds}", color = Color.Green
                                    )

                                    Spacer(Modifier.width(20.dp))

                                    Text("Leeches: ${item.leeches}", color = Color.Red)
                                }
                                Spacer(Modifier.height(15.dp))

                                Row {
                                    Text(item.date, color = Color(0xFF5A9BD8))

                                    Text(" From ", color = Color.White)

                                    Text(item.uploader, color = Color(0xFF5A9BD8))
                                }
                            }
                            Column(Modifier.fillMaxHeight(), Arrangement.Center) {

                                IconButton(onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW, Uri.parse(item.magnet)
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "YOU DON'T HAVE A TORRENT CLIENT INSTALLED",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }, Modifier.padding()) {

                                    Icon(
                                        painterResource(R.drawable.ic_launcher_foreground),
                                        "downloader",
                                        Modifier
                                            .width(iconWidth)
                                            .rotate(-45f)
                                    )
                                }
                                Text(
                                    text = if ((item.size / 1024) < 1f) item.size.toString() + " K"
                                    else if (((item.size / 1024) / 1024) < 1f) (item.size / 1024).toString() + " M"
                                    else ((item.size / 1024) / 1024).toString() + " G",
                                    color = Color.Green
                                )
                            }
                        }
                    }
                    Spacer(Modifier.padding(vertical = height * 0.01f))
                }
            }
        }
    }
}
