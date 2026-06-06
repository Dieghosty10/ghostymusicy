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
    val playerConnection = LocalPlayerConnection.current

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11  -> "Buenos días ☀️"
        in 12..17 -> "Buenas tardes 🎵"
        else      -> "Buenas noches 🌙"
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().haze(hazeState),
            contentPadding = PaddingValues(
                top    = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = 140.dp,
            )
        ) {
            // ── Header (Search & Chips) ─────────────────────────────────────────────────────
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Falso Search Bar que redirige a la pantalla de búsqueda
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { navController.navigate(Routes.SEARCH) }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Buscar canciones, álbumes, artistas...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Chips de filtrado visuales
                    var selectedChip by remember { mutableStateOf("Todo") }
                    val chips = listOf("Todo", "Mixes", "Canciones", "Álbumes", "Artistas")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chips) { chip ->
                            val isSelected = selectedChip == chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedChip = chip }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = chip,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Shimmer si carga ───────────────────────────────────────────
            if (isLoading && homePage == null) {
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
