package com.ahmed.tsoup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.ahmed.tsoup.ui.theme.TSOUPTheme
import com.ahmed.tsoup.ui.theme.TorrentGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TSOUPTheme {
                val context = LocalContext.current
                val prefs = context.getSharedPreferences("app_preferences", MODE_PRIVATE)
                
                // Initialize default domains on first run
                if (prefs.getBoolean("first_run", true)) {
                    saveAddress(
                        listOf(
                            DomainItem("https://1337x.to", true),
                            DomainItem("https://bitsearch.eu", true),
                            DomainItem("https://yts.gg", true),
                            DomainItem("https://knaben.eu", true),
                            DomainItem("https://torrentgalaxy.to", true),
                            DomainItem("https://torrentquest.com", true),
                        ), prefs
                    )
                    prefs.edit { putBoolean("first_run", false) }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp, end = 16.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            IconButton(onClick = {
                                val intent = Intent(context, Settings::class.java)
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val urlOld = intent.getStringExtra("url") ?: ""
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        TSoupLogo()
                        Spacer(modifier = Modifier.height(48.dp))
                        SearchBar(initialUrl = urlOld)
                    }
                }
            }
        }
    }
}

@Composable
fun TSoupLogo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "T",
            color = TorrentGreen,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "SOUP",
            fontSize = 64.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@SuppressLint("NewApi")
@Composable
fun SearchBar(initialUrl: String) {
    val context = LocalContext.current
    val url = remember { mutableStateOf(initialUrl) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = url.value,
            onValueChange = { url.value = it },
            label = { Text("Search for torrents...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val intent = Intent(context, SearchResults::class.java)
                intent.putExtra("url", url.value)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp),
            enabled = url.value.trim().length >= 2,
            shape = MaterialTheme.shapes.large
        ) {
            Text("Search", fontSize = 18.sp)
        }
    }
}
