package com.dieghosty10.ghostymusicy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.dieghosty10.ghostymusicy.LocalPlayerConnection
import com.dieghosty10.ghostymusicy.innertube.models.AlbumItem
import com.dieghosty10.ghostymusicy.innertube.models.SongItem
import com.dieghosty10.ghostymusicy.models.toMediaMetadata
import com.dieghosty10.ghostymusicy.playback.queues.YouTubeQueue
import com.dieghosty10.ghostymusicy.ui.navigation.Routes
import com.dieghosty10.ghostymusicy.viewmodels.ArtistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    navController: NavController,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val artistPage by viewModel.artistPage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val playerConnection = LocalPlayerConnection.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artistPage?.artist?.title ?: "") },
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
                    Text("Error al cargar el artista", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchArtist() }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Reintentar")
                    }
                }
            }
        } else {
            artistPage?.let { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = 120.dp
                    )
                ) {
                    item {
                        ArtistHeader(page) {
                            // Reproducir la primera lista de canciones disponible (generalmente "Canciones")
                            val topSongs = page.sections.firstOrNull { it.items.any { item -> item is SongItem } }?.items?.filterIsInstance<SongItem>()
                            if (!topSongs.isNullOrEmpty()) {
                                playerConnection?.playQueue(
                                    YouTubeQueue.radio(
                                        topSongs.first().toMediaMetadata()
                                    )
                                )
                            }
                        }
                    }

                    page.sections.forEach { section ->
                        item {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                            )
                        }
                        
                        if (section.items.firstOrNull() is SongItem) {
                            items(section.items.filterIsInstance<SongItem>()) { song ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            playerConnection?.playQueue(
                                                YouTubeQueue.radio(song.toMediaMetadata())
                                            )
                                        }
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = song.thumbnail,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    Spacer(Modifier.width(12.dp))
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
                                }
                            }
                        } else if (section.items.firstOrNull() is AlbumItem) {
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(section.items.filterIsInstance<AlbumItem>()) { album ->
                                        Column(
                                            modifier = Modifier
                                                .width(140.dp)
                                                .clickable {
                                                    navController.navigate(Routes.Album(album.browseId))
                                                }
                                        ) {
                                            AsyncImage(
                                                model = album.thumbnail,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(140.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = album.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = album.year?.toString() ?: "",
                                                style = MaterialTheme.typography.bodySmall,
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
        }
    }
}

@Composable
fun ArtistHeader(page: com.dieghosty10.ghostymusicy.innertube.pages.ArtistPage, onPlay: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = page.artist.thumbnail,
            contentDescription = page.artist.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = page.artist.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onPlay,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reproducir Radio", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}
