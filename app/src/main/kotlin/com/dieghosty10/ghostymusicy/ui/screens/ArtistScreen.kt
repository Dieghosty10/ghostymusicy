package com.dieghosty10.ghostymusicy.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val isFollowing by viewModel.isFollowing.collectAsState()
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
                        bottom = 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                ) {
                    item {
                        ArtistHeader(
                            page = page,
                            isFollowing = isFollowing,
                            onFollow = { viewModel.toggleFollow() },
                            onRadio = {
                                val radioEndpoint = page.artist.radioEndpoint
                                if (radioEndpoint != null) {
                                    playerConnection?.playQueue(YouTubeQueue(radioEndpoint))
                                } else {
                                    val topSongs = page.sections.flatMap { it.items }.filterIsInstance<SongItem>()
                                    if (topSongs.isNotEmpty()) {
                                        playerConnection?.playQueue(
                                            YouTubeQueue.radio(topSongs.first().toMediaMetadata())
                                        )
                                    }
                                }
                            },
                            onShuffle = {
                                playerConnection?.player?.shuffleModeEnabled = true
                                val shuffleEndpoint = page.artist.shuffleEndpoint
                                if (shuffleEndpoint != null) {
                                    playerConnection?.playQueue(YouTubeQueue(shuffleEndpoint))
                                } else {
                                    val topSongs = page.sections.flatMap { it.items }.filterIsInstance<SongItem>()
                                    if (topSongs.isNotEmpty()) {
                                        playerConnection?.playQueue(
                                            YouTubeQueue.radio(topSongs.first().toMediaMetadata())
                                        )
                                    }
                                }
                            }
                        )
                    }

                    page.sections.forEach { section ->
                        if (section.items.isNotEmpty()) {
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
}

@Composable
fun ArtistHeader(
    page: com.dieghosty10.ghostymusicy.innertube.pages.ArtistPage,
    isFollowing: Boolean,
    onFollow: () -> Unit,
    onRadio: () -> Unit,
    onShuffle: () -> Unit
) {
    val totalSongs = page.sections.sumOf { it.items.filterIsInstance<SongItem>().size }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = page.artist.thumbnail,
            contentDescription = page.artist.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = page.artist.title,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (totalSongs > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$totalSongs canciones",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onFollow,
                shape = CircleShape,
                border = BorderStroke(
                    1.dp,
                    if (isFollowing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isFollowing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = if (isFollowing) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isFollowing) "Siguiendo" else "Seguir",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Button(
                onClick = onRadio,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.Radio, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Radio", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            }

            Button(
                onClick = onShuffle,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Aleatorio", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}
