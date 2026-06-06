package com.dieghosty10.ghostymusicy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.dieghosty10.ghostymusicy.LocalPlayerConnection
import com.dieghosty10.ghostymusicy.models.toMediaMetadata
import com.dieghosty10.ghostymusicy.playback.queues.YouTubeQueue
import com.dieghosty10.ghostymusicy.utils.makeTimeString
import com.dieghosty10.ghostymusicy.viewmodels.AlbumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    navController: NavController,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val albumPage by viewModel.albumPage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val downloads by viewModel.downloadUtil.downloads.collectAsState()
    val playerConnection = LocalPlayerConnection.current

    val isDownloaded = remember(albumPage, downloads) {
        albumPage?.songs?.isNotEmpty() == true && albumPage!!.songs.all {
            downloads[it.id]?.state == androidx.media3.exoplayer.offline.Download.STATE_COMPLETED
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(albumPage?.album?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error al cargar el álbum", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchAlbum() }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Reintentar")
                    }
                }
            }
        } else {
            albumPage?.let { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                ) {
                    item {
                        AlbumHeader(
                            page = page,
                            isSaved = isSaved,
                            isDownloaded = isDownloaded,
                            onSave = {
                                viewModel.toggleSave()
                                val msg = if (!isSaved) "Álbum Guardado" else "Álbum Eliminado"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onDownload = {
                                if (isDownloaded) return@AlbumHeader

                                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                                val activeNetwork = connectivityManager.activeNetwork
                                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                                val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

                                if (!isWifi) {
                                    Toast.makeText(context, "Descargando con datos móviles", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Descargando álbum...", Toast.LENGTH_SHORT).show()
                                }

                                page.songs.forEach { song ->
                                    val vId = song.id
                                    val title = song.title
                                    val downloadRequest = androidx.media3.exoplayer.offline.DownloadRequest
                                        .Builder(vId, vId.toUri())
                                        .setCustomCacheKey(vId)
                                        .setData(title.toByteArray())
                                        .build()
                                    androidx.media3.exoplayer.offline.DownloadService.sendAddDownload(
                                        context,
                                        com.dieghosty10.ghostymusicy.playback.ExoDownloadService::class.java,
                                        downloadRequest,
                                        false
                                    )
                                }
                                Toast.makeText(context, "Descargando álbum...", Toast.LENGTH_SHORT).show()
                            },
                            onPlay = { startIndex ->
                                val song = page.songs.getOrNull(startIndex)
                                playerConnection?.playQueue(
                                    YouTubeQueue.playlist(
                                        playlistId = page.album.playlistId,
                                        videoId = song?.id,
                                        preloadItem = song?.toMediaMetadata(),
                                        startIndex = startIndex
                                    )
                                )
                            }
                        )
                    }

                    itemsIndexed(page.songs) { index, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playerConnection?.playQueue(
                                        YouTubeQueue.playlist(
                                            playlistId = page.album.playlistId,
                                            videoId = song.id,
                                            preloadItem = song.toMediaMetadata(),
                                            startIndex = index
                                        )
                                    )
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artists.joinToString { it.name },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            song.duration?.let { dur ->
                                Text(
                                    text = makeTimeString(dur.toLong() * 1000L),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumHeader(
    page: com.dieghosty10.ghostymusicy.innertube.pages.AlbumPage,
    isSaved: Boolean,
    isDownloaded: Boolean,
    onSave: () -> Unit,
    onDownload: () -> Unit,
    onPlay: (Int) -> Unit
) {
    val playerConnection = LocalPlayerConnection.current
    val totalDurationMillis = page.songs.sumOf { (it.duration ?: 0).toLong() * 1000L }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = page.album.thumbnail,
            contentDescription = page.album.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = page.album.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${page.album.artists?.joinToString { it.name } ?: "Varios Artistas"} • ${page.album.year ?: ""} • ${page.songs.size} canciones • ${makeTimeString(totalDurationMillis)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onPlay(0) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reproducir", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Guardar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSave() }
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Guardar",
                    modifier = Modifier.size(28.dp),
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isSaved) "Guardado" else "Guardar",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Aleatorio
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        playerConnection?.player?.shuffleModeEnabled = true
                        val randomStartIndex = if (page.songs.size > 1) (0 until page.songs.size).random() else 0
                        onPlay(randomStartIndex)
                    }
                    .padding(12.dp)
            ) {
                Icon(Icons.Rounded.Shuffle, contentDescription = "Aleatorio", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("Aleatorio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            }

            // Descargar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDownload() }
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = if (isDownloaded) Icons.Rounded.Check else Icons.Rounded.Download,
                    contentDescription = "Descargar",
                    modifier = Modifier.size(28.dp),
                    tint = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isDownloaded) "Descargado" else "Descargar",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
