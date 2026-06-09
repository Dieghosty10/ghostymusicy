package com.dieghosty10.ghostymusicy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.dieghosty10.ghostymusicy.LocalPlayerConnection
import com.dieghosty10.ghostymusicy.extensions.toMediaItem
import com.dieghosty10.ghostymusicy.models.MediaMetadata
import com.dieghosty10.ghostymusicy.playback.queues.ListQueue
import com.dieghosty10.ghostymusicy.ui.components.EmptyStateMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(navController: NavController) {
    val playerConnection = LocalPlayerConnection.current
    val downloadsMap by playerConnection?.downloads?.collectAsState(initial = emptyMap()) ?: remember { mutableStateOf(emptyMap()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Descargas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        val downloadsList = downloadsMap.values.toList()
        
        if (downloadsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyStateMessage("No tienes descargas aún")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 120.dp)
            ) {
                itemsIndexed(downloadsList) { index, download ->
                    val dataStr = try { String(download.request.data) } catch (e: Exception) { "" }
                    var title = "Desconocido"
                    var artists = ""
                    var thumbnail = ""
                    
                    try {
                        if (dataStr.startsWith("{")) {
                            val json = org.json.JSONObject(dataStr)
                            title = json.optString("title", "Desconocido")
                            artists = json.optString("artists", "")
                            thumbnail = json.optString("thumbnail", "")
                        } else {
                            title = dataStr
                        }
                    } catch (e: Exception) {
                        title = dataStr
                    }
                    val isCompleted = download.state == Download.STATE_COMPLETED
                    val isDownloading = download.state == Download.STATE_DOWNLOADING

                    ListItem(
                        headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) },
                        supportingContent = { 
                            val desc = if (isCompleted) "Descargado" else if (isDownloading) "Descargando... ${download.percentDownloaded.toInt()}%" else "En espera"
                            Text(if (artists.isNotEmpty()) "$artists • $desc" else desc, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (thumbnail.isNotEmpty()) {
                                    AsyncImage(
                                        model = thumbnail,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Rounded.Check else if (isDownloading) Icons.Rounded.Downloading else Icons.Rounded.Download,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            if (isCompleted) {
                                // Create a queue of all completed downloads
                                val completedDownloads = downloadsList.filter { it.state == Download.STATE_COMPLETED }
                                val clickedIndex = completedDownloads.indexOf(download)
                                if (clickedIndex >= 0) {
                                    val mediaItems = completedDownloads.map { d ->
                                        val dStr = try { String(d.request.data) } catch (e: Exception) { "" }
                                        var mTitle = "Desconocido"
                                        var mArtists = emptyList<com.dieghosty10.ghostymusicy.models.MediaMetadata.Artist>()
                                        var mThumbnail = ""
                                        try {
                                            if (dStr.startsWith("{")) {
                                                val json = org.json.JSONObject(dStr)
                                                mTitle = json.optString("title", "Desconocido")
                                                mThumbnail = json.optString("thumbnail", "")
                                                val arts = json.optString("artists", "")
                                                if (arts.isNotEmpty()) {
                                                    mArtists = listOf(com.dieghosty10.ghostymusicy.models.MediaMetadata.Artist(id = "", name = arts))
                                                }
                                            } else {
                                                mTitle = dStr
                                            }
                                        } catch (e: Exception) { mTitle = dStr }
                                        
                                        MediaMetadata(
                                            id = d.request.id,
                                            title = mTitle,
                                            artists = mArtists,
                                            thumbnailUrl = mThumbnail,
                                            duration = MediaMetadata.UNKNOWN_DURATION
                                        ).toMediaItem()
                                    }
                                    playerConnection?.playQueue(ListQueue(
                                        title = "Descargas",
                                        items = mediaItems,
                                        startIndex = clickedIndex
                                    ))
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
