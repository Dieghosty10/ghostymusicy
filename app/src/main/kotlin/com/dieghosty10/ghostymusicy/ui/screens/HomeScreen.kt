package com.dieghosty10.ghostymusicy.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.dieghosty10.ghostymusicy.LocalPlayerConnection
import com.dieghosty10.ghostymusicy.db.entities.EventWithSong
import com.dieghosty10.ghostymusicy.innertube.models.AlbumItem
import com.dieghosty10.ghostymusicy.innertube.models.SongItem
import com.dieghosty10.ghostymusicy.innertube.models.ArtistItem
import com.dieghosty10.ghostymusicy.innertube.models.YTItem
import com.dieghosty10.ghostymusicy.ui.navigation.Routes
import com.dieghosty10.ghostymusicy.models.toMediaMetadata
import com.dieghosty10.ghostymusicy.playback.queues.YouTubeQueue
import com.dieghosty10.ghostymusicy.viewmodels.HomeViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import java.util.Calendar

@Composable
fun HomeScreen(
    hazeState: HazeState,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homePage     by viewModel.homePage.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()
    val heroArtist   by viewModel.heroArtist.collectAsState()
    val newReleases  by viewModel.newReleases.collectAsState()
    val isOffline    by viewModel.isOffline.collectAsState()
    val playerConnection = LocalPlayerConnection.current

    var showStatsSheet by remember { mutableStateOf(false) }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11  -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else      -> "Buenas noches"
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().haze(hazeState),
            contentPadding = PaddingValues(
                top    = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            )
        ) {
            // ── Header ─────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(greeting,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable { showStatsSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Assessment, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Tus Estadísticas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { navController.navigate("library") }) {
                                Icon(Icons.Rounded.LibraryMusic, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { navController.navigate("settings") }) {
                                Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // ── Hero Card ────────────────────────────────────────────────────────
            if (heroArtist != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                            .height(220.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = heroArtist!!.artist.thumbnail,
                                contentDescription = heroArtist!!.artist.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(0.9f)),
                                            startY = 100f
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Para ti",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(0.7f)
                                )
                                Text(
                                    text = heroArtist!!.artist.title,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        heroArtist!!.artist.radioEndpoint?.let { endpoint ->
                                            playerConnection?.playQueue(YouTubeQueue(endpoint))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reproducir mix", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }

            // ── Nuevos Lanzamientos ────────────────────────────────────────────────
            if (newReleases != null) {
                item {
                    SectionTitle(newReleases!!.title)
                }
                item {
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(newReleases!!.items) { item ->
                            PremiumYTItemCard(item) {
                                when (item) {
                                    is SongItem  -> playerConnection?.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
                                    is AlbumItem -> navController.navigate(Routes.Album(item.browseId))
                                    is ArtistItem -> navController.navigate(Routes.Artist(item.id))
                                    else         -> {}
                                }
                            }
                        }
                    }
                }
            }

            // ── Modo Offline ───────────────────────────────────────────────
            if (isOffline && homePage == null) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Estás offline",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Conéctate a internet para explorar música, o escucha tus canciones guardadas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = { navController.navigate(Routes.DOWNLOADS) },
                                modifier = Modifier.fillMaxWidth(0.9f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Icon(Icons.Rounded.DownloadDone, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Ver Canciones Descargadas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            } else if (isLoading && homePage == null) {
                items(3) { ShimmerSection() }
            } else {

                // ── Escuchado recientemente (solo si hay historial) ────────
                if (recentEvents.isNotEmpty()) {
                    item {
                        SectionTitle("Escuchado recientemente")
                    }
                    item {
                        LazyRow(
                            contentPadding         = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement  = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recentEvents.distinctBy { it.song.song.id }) { event ->
                                RecentSongCard(event) {
                                    playerConnection?.playQueue(
                                        YouTubeQueue.radio(event.song.toMediaMetadata())
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Secciones personalizadas "Porque te gusta [Artista]" ──────────────────
                if (homePage != null) {
                    homePage!!.sections.forEach { section ->
                        item { SectionTitle(section.title) }
                        item {
                            LazyRow(
                                contentPadding        = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(section.items) { item ->
                                    PremiumYTItemCard(item) {
                                        when (item) {
                                            is SongItem  -> playerConnection?.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
                                            is AlbumItem -> navController.navigate(Routes.Album(item.browseId))
                                            is ArtistItem -> navController.navigate(Routes.Artist(item.id))
                                            else         -> {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (!isLoading) {
                    // ── Estado vacío: sin favoritos configurados ────────────────────────
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Explora música",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Busca artistas y canciones para personalizar tu inicio",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showStatsSheet) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showStatsSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                StatsSheetContent(recentEvents = recentEvents)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color    = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)
    )
}

@Composable
fun RecentSongCard(event: EventWithSong, onClick: () -> Unit) {
    val ia      = remember { MutableInteractionSource() }
    val pressed by ia.collectIsPressedAsState()
    val scale   by animateFloatAsState(if (pressed) 0.93f else 1f, tween(120))

    Column(
        modifier = Modifier
            .width(96.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(ia, null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model            = event.song.song.thumbnailUrl,
                contentDescription = null,
                contentScale     = ContentScale.Crop,
                modifier         = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Box(
                Modifier.size(28.dp).background(Color.Black.copy(0.5f), CircleShape),
                Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text     = event.song.song.title,
            style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color    = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ShimmerSection() {
    val t      = rememberInfiniteTransition(label = "shimmer")
    val offset by t.animateFloat(
        initialValue   = -1f,
        targetValue    = 2f,
        animationSpec  = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label          = "offset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFF27272A), Color(0xFF3F3F46), Color(0xFF27272A)),
        start  = Offset(offset * 600f, 0f),
        end    = Offset(offset * 600f + 600f, 0f)
    )
    Column {
        Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp).width(180.dp).height(22.dp).clip(RoundedCornerShape(6.dp)).background(brush))
        Row(Modifier.padding(horizontal = 20.dp), Arrangement.spacedBy(14.dp)) {
            repeat(3) {
                Box(Modifier.width(160.dp).height(200.dp).clip(RoundedCornerShape(24.dp)).background(brush))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun PremiumYTItemCard(item: YTItem, onClick: () -> Unit) {
    val ia      = remember { MutableInteractionSource() }
    val pressed by ia.collectIsPressedAsState()
    val scale   by animateFloatAsState(if (pressed) 0.94f else 1f, tween(120))

    Card(
        modifier = Modifier
            .width(160.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(ia, null, onClick = onClick),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.height(200.dp)) {
            AsyncImage(
                model = item.thumbnail, contentDescription = item.title,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)), startY = 80f)
                )
            )
            Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                if (item is SongItem) {
                    Text(item.artists.joinToString { it.name },
                        style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun StatsSheetContent(recentEvents: List<EventWithSong>) {
    val topSongs = remember(recentEvents) {
        recentEvents.groupBy { it.song.song.id }
            .map { it.value.first().song.song to it.value.size }
            .sortedByDescending { it.second }
            .take(5)
    }

    val topArtists = remember(recentEvents) {
        recentEvents.flatMap { it.song.artists }
            .groupBy { it.id }
            .map { it.value.first() to it.value.size }
            .sortedByDescending { it.second }
            .take(5)
    }

    val maxSongPlays = topSongs.maxOfOrNull { it.second } ?: 1
    val maxArtistPlays = topArtists.maxOfOrNull { it.second } ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Tus Estadísticas",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tus favoritos de esta semana",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (topSongs.isEmpty()) {
            Text("No hay suficientes datos. ¡Escucha más música!", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("Canciones Top", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(16.dp))
            topSongs.forEachIndexed { index, (song, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "#${index + 1}", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), 
                        color = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.width(32.dp)
                    )
                    AsyncImage(
                        model = song.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(song.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { count.toFloat() / maxSongPlays.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("$count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Artistas Top", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(16.dp))
            topArtists.forEachIndexed { index, (artist, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "#${index + 1}", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), 
                        color = MaterialTheme.colorScheme.secondary, 
                        modifier = Modifier.width(32.dp)
                    )
                    AsyncImage(
                        model = artist.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(artist.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { count.toFloat() / maxArtistPlays.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("$count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
