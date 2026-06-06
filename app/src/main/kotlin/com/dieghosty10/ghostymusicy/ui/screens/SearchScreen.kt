package com.dieghosty10.ghostymusicy.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.dieghosty10.ghostymusicy.LocalPlayerConnection
import com.dieghosty10.ghostymusicy.innertube.models.*
import com.dieghosty10.ghostymusicy.ui.navigation.Routes
import com.dieghosty10.ghostymusicy.models.toMediaMetadata
import com.dieghosty10.ghostymusicy.playback.queues.YouTubeQueue
import com.dieghosty10.ghostymusicy.viewmodels.SearchTab
import com.dieghosty10.ghostymusicy.viewmodels.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query          by viewModel.query.collectAsState()
    val suggestions    by viewModel.suggestions.collectAsState()
    val results        by viewModel.results.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()
    val activeTab      by viewModel.activeTab.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val playerConnection = LocalPlayerConnection.current
    val focusRequester   = remember { FocusRequester() }
    var isSearchActive   by remember { mutableStateOf(false) }

    val hasResults = results.values.any { it != null && it.items.isNotEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        // ── Barra de búsqueda ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearchActive) {
                IconButton(onClick = {
                    isSearchActive = false
                    viewModel.updateQuery("")
                }) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground)
                }
            }
            OutlinedTextField(
                value         = query,
                onValueChange = { viewModel.updateQuery(it); isSearchActive = true },
                modifier      = Modifier.weight(1f).focusRequester(focusRequester),
                placeholder   = { Text("Buscar canciones, artistas...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon   = {
                    Icon(Icons.Rounded.Search, null,
                        tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon  = {
                    if (query.isNotEmpty()) IconButton(onClick = { viewModel.updateQuery("") }) {
                        Icon(Icons.Rounded.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                shape         = RoundedCornerShape(28.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (query.isNotBlank()) { viewModel.performSearch(query); isSearchActive = false }
                }),
            )
        }

        // ── Filtros (Chips) SIEMPRE visibles al escribir ────────────────
        if (query.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SearchTab.entries.toTypedArray()) { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { viewModel.setActiveTab(tab) },
                        label = { Text(tab.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = activeTab == tab,
                            borderColor = if (activeTab == tab) Color.Transparent else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        // ── Contenido dinámico ─────────────────────────────────────────────
        when {
            // Sugerencias mientras escribe
            isSearchActive && query.isNotEmpty() && !hasResults && !isLoading -> {
                LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                    suggestions?.queries?.let { queries ->
                        items(queries) { s ->
                            SuggestionRow(
                                text        = s,
                                onClick     = { viewModel.performSearch(s); isSearchActive = false },
                                onFillQuery = { viewModel.updateQuery(s) }
                            )
                        }
                    }
                }
            }

            // Cargando
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // Resultados por tab
            hasResults -> {
                val currentResults = results[activeTab]?.items ?: emptyList()
                if (currentResults.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "No encontramos nada",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No se encontraron resultados para tu búsqueda en ${activeTab.label}. Intenta con otras palabras clave.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(), top = 4.dp)) {
                        val topArtist = currentResults.firstOrNull { it is ArtistItem } as? ArtistItem
                        val showTopArtist = topArtist != null && activeTab != SearchTab.ARTISTS && activeTab != SearchTab.ALBUMS

                        if (showTopArtist && topArtist != null) {
                            item {
                                TopArtistResultCard(item = topArtist) {
                                    navController.navigate(Routes.Artist(topArtist.id))
                                }
                            }
                        }

                        val itemsList = if (showTopArtist) currentResults.filter { it != topArtist } else currentResults

                        items(itemsList) { item ->
                            when (activeTab) {
                                SearchTab.ARTISTS -> ArtistResultRow(item) {
                                    if (item is ArtistItem) navController.navigate(Routes.Artist(item.id))
                                }
                                SearchTab.ALBUMS  -> AlbumResultRow(item) {
                                    if (item is AlbumItem) navController.navigate(Routes.Album(item.browseId))
                                }
                                else              -> SongResultRow(item) {
                                    if (item is SongItem) {
                                        playerConnection?.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Estado inicial — historial + géneros
            else -> {
                LazyColumn(contentPadding = PaddingValues(bottom = 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
                    if (recentSearches.isNotEmpty()) {
                        item {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                                Arrangement.SpaceBetween, Alignment.CenterVertically
                            ) {
                                Text("Búsquedas recientes",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground)
                                TextButton(onClick = { viewModel.clearRecentSearches() }) {
                                    Text("Borrar todo", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        item {
                            androidx.compose.foundation.lazy.LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recentSearches) { recent ->
                                    SuggestionChip(
                                        onClick = { viewModel.performSearch(recent) },
                                        label = { Text(recent) },
                                        icon = { Icon(Icons.Rounded.History, null, modifier = Modifier.size(16.dp)) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = SuggestionChipDefaults.suggestionChipBorder(
                                            enabled = true,
                                            borderColor = Color.Transparent
                                        )
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

// ── Filas de resultados ────────────────────────────────────────────────────────

@Composable
fun SongResultRow(item: YTItem, onClick: () -> Unit) {
    val ia = remember { MutableInteractionSource() }
    val pressed by ia.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(ia, androidx.compose.foundation.LocalIndication.current, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = item.thumbnail, contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Box(Modifier.size(26.dp).background(Color.Black.copy(0.55f), CircleShape), Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item is SongItem) {
                Text(item.artists.joinToString { it.name }, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (item is SongItem) {
            item.duration?.let { duration ->
                Text(com.dieghosty10.ghostymusicy.utils.makeTimeString(duration.toLong() * 1000L), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ArtistResultRow(item: YTItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.thumbnail, contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Artista", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AlbumResultRow(item: YTItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.thumbnail, contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item is AlbumItem) {
                Text(
                    listOfNotNull(item.artists?.joinToString { it.name }, item.year?.toString()).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SuggestionRow(text: String, onClick: () -> Unit, onFillQuery: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 20.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
        IconButton(onClick = onFillQuery, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.NorthWest, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

// Compatibilidad con HomeScreen
@Composable
fun PremiumSearchResultItem(item: YTItem, onClick: () -> Unit) = SongResultRow(item, onClick)

@Composable
fun TopArtistResultCard(item: ArtistItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Mejor Resultado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Artista",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
