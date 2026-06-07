package com.dieghosty10.ghostymusicy.ui.screens

import android.text.format.DateUtils
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil3.asDrawable
import coil3.compose.AsyncImage
import androidx.media3.exoplayer.offline.Download
import com.dieghosty10.ghostymusicy.LocalPlayerConnection
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import com.dieghosty10.ghostymusicy.viewmodels.LyricsViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(navController: androidx.navigation.NavHostController) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val shuffleEnabled by playerConnection.shuffleModeEnabled.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()

    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    val dominantColor = com.dieghosty10.ghostymusicy.utils.rememberDominantColor(model = mediaMetadata?.thumbnailUrl)
    var isLiked by remember { mutableStateOf(false) }
    
    val videoId = mediaMetadata?.id ?: ""
    val download by playerConnection.getDownload(videoId).collectAsState(initial = null)

    var showQueue by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val queueItems by playerConnection.queueItems.collectAsState()
    val currentMediaItemIndex by playerConnection.currentMediaItemIndex.collectAsState()

    var showLyrics by remember { mutableStateOf(false) }
    val lyricsViewModel = hiltViewModel<LyricsViewModel>()
    val lyrics by lyricsViewModel.lyrics.collectAsState()
    val isLyricsLoading by lyricsViewModel.isLoading.collectAsState()

    // Auto-fetch lyrics when song changes
    LaunchedEffect(mediaMetadata?.id) {
        mediaMetadata?.let { meta ->
            lyricsViewModel.fetchLyrics(meta)
        }
    }

    // Observe download state
    val context = LocalContext.current

    val coverScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.88f,
        animationSpec = tween(durationMillis = 600)
    )

    // Animación infinita de pulso cuando reproduce
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playerConnection.player.currentPosition
            duration = playerConnection.player.duration.coerceAtLeast(0)
            delay(500)
        }
        // También actualizar cuando se pausa (para mostrar posición correcta)
        currentPosition = playerConnection.player.currentPosition
        duration = playerConnection.player.duration.coerceAtLeast(0)
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            dominantColor?.copy(alpha = 0.5f) ?: Color.Transparent,
            Color(0xFF09090B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        // Halo de color detrás de la portada cuando reproduce
        if (isPlaying && dominantColor != null) {
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 80.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                dominantColor!!.copy(alpha = pulseAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                    start = 28.dp,
                    end = 28.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fuente de la cola y botón de Queue
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reproduciendo",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
                IconButton(
                    onClick = { showQueue = true },
                    modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Cola de reproducción",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Portada inmersiva con sombra de color
            val context = LocalContext.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = coverScale
                        scaleY = coverScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Sombra coloreada
                if (dominantColor != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(
                                elevation = if (isPlaying) 48.dp else 24.dp,
                                shape = RoundedCornerShape(32.dp),
                                ambientColor = dominantColor!!,
                                spotColor = dominantColor!!
                            )
                    )
                }
                AnimatedContent(
                    targetState = mediaMetadata?.thumbnailUrl,
                    transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                    label = "cover"
                ) { thumbnailUrl ->
                    AsyncImage(
                        model = thumbnailUrl ?: "https://via.placeholder.com/500",
                        contentDescription = "Cover Art",
                        contentScale = ContentScale.Crop,
                        onSuccess = { state ->
                            // Palette is handled by rememberDominantColor
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF27272A))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Título + artista + like
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = mediaMetadata?.title ?: "Sin reproducción",
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                        label = "title"
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mediaMetadata?.artists?.joinToString { it.name } ?: "Selecciona una canción",
                        style = MaterialTheme.typography.bodyMedium,
                        color = dominantColor?.copy(alpha = 0.9f) ?: MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Botón de Like
                IconButton(
                    onClick = {
                        playerConnection.toggleLike()
                        isLiked = !isLiked
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Me gusta",
                        tint = if (isLiked) Color(0xFFEF4444) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Slider de progreso
            val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()) else 0f
            Slider(
                value = progress,
                onValueChange = { value ->
                    val newPos = (value * duration).toLong()
                    playerConnection.player.seekTo(newPos)
                    currentPosition = newPos
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = dominantColor ?: MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    DateUtils.formatElapsedTime(currentPosition / 1000),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    DateUtils.formatElapsedTime(duration / 1000),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controles principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = {
                        playerConnection.player.shuffleModeEnabled = !shuffleEnabled
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = if (shuffleEnabled) (dominantColor ?: MaterialTheme.colorScheme.primary) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Anterior
                IconButton(
                    onClick = { playerConnection.seekToPrevious() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play/Pause (botón grande)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            dominantColor?.copy(alpha = 0.9f) ?: MaterialTheme.colorScheme.primary
                        )
                        .clickable {
                            if (isPlaying) playerConnection.player.pause()
                            else playerConnection.player.play()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Siguiente
                IconButton(
                    onClick = { playerConnection.seekToNext() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        contentDescription = "Siguiente",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat
                IconButton(
                    onClick = {
                        val next = when (repeatMode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                        playerConnection.player.repeatMode = next
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    val (icon, tint) = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne to (dominantColor ?: MaterialTheme.colorScheme.primary)
                        Player.REPEAT_MODE_ALL -> Icons.Rounded.Repeat to (dominantColor ?: MaterialTheme.colorScheme.primary)
                        else -> Icons.Rounded.Repeat to Color.White.copy(alpha = 0.5f)
                    }
                    Icon(
                        icon,
                        contentDescription = "Repetir",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Barra de opciones extra
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Descargar
                val isDownloaded = download?.state == Download.STATE_COMPLETED
                val isDownloading = download?.state == Download.STATE_DOWNLOADING
                IconButton(onClick = {
                    val vId = mediaMetadata?.id ?: return@IconButton
                    val title = mediaMetadata?.title ?: "Song"
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
                    android.widget.Toast.makeText(context, "Descargando...", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        if (isDownloaded) Icons.Rounded.DownloadDone
                        else if (isDownloading) Icons.Rounded.Downloading
                        else Icons.Rounded.Download,
                        contentDescription = "Descargar",
                        tint = if (isDownloaded) (dominantColor ?: MaterialTheme.colorScheme.primary)
                               else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Letras
                IconButton(onClick = { showLyrics = true }) {
                    Icon(
                        Icons.Rounded.Lyrics,
                        contentDescription = "Letra",
                        tint = if (showLyrics) (dominantColor ?: MaterialTheme.colorScheme.primary)
                               else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Guardar
                IconButton(onClick = {
                    playerConnection.toggleLike()
                    isLiked = !isLiked
                    android.widget.Toast.makeText(context, if (isLiked) "Añadido a la biblioteca" else "Removido de la biblioteca", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        Icons.Rounded.LibraryAdd,
                        contentDescription = "Guardar",
                        tint = if (isLiked) (dominantColor ?: MaterialTheme.colorScheme.primary)
                               else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Menú de opciones (3 puntos)
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "Más opciones",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF141414))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver artista", color = Color.White) },
                            onClick = { 
                                showMenu = false
                                val artistId = mediaMetadata?.artists?.firstOrNull()?.id ?: return@DropdownMenuItem
                                navController.navigate(com.dieghosty10.ghostymusicy.ui.navigation.Routes.Artist(artistId))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ver álbum", color = Color.White) },
                            onClick = { 
                                showMenu = false
                                val albumId = mediaMetadata?.album?.id ?: return@DropdownMenuItem
                                navController.navigate(com.dieghosty10.ghostymusicy.ui.navigation.Routes.Album(albumId))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Compartir", color = Color.White) },
                            onClick = { 
                                showMenu = false
                                mediaMetadata?.id?.let { videoId ->
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=$videoId")
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "Compartir canción"))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showLyrics) {
        ModalBottomSheet(
            onDismissRequest = { showLyrics = false },
            containerColor = dominantColor?.copy(alpha = 0.15f)?.let {
                androidx.compose.ui.graphics.lerp(Color(0xFF09090B), it, 0.6f)
            } ?: Color(0xFF141414),
            scrimColor = Color.Black.copy(alpha = 0.6f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mediaMetadata?.title ?: "Letra",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mediaMetadata?.artists?.joinToString { it.name } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(24.dp))
                if (isLyricsLoading) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = dominantColor ?: MaterialTheme.colorScheme.primary)
                    }
                } else if (lyrics == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = "Sin letra",
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Esta canción no tiene letra disponible.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Intenta buscar una versión diferente de la canción.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = lyrics!!,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 32.sp,
                            letterSpacing = 0.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }

    if (showQueue) {
        ModalBottomSheet(
            onDismissRequest = { showQueue = false },
            containerColor = dominantColor?.copy(alpha = 0.15f)?.let {
                androidx.compose.ui.graphics.lerp(Color(0xFF09090B), it, 0.6f)
            } ?: Color(0xFF141414),
            scrimColor = Color.Black.copy(alpha = 0.6f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Text(
                    text = "Cola de reproducción",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    contentPadding = PaddingValues(bottom = 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    items(queueItems.size) { index ->
                        val item = queueItems[index]
                        val isCurrent = index == currentMediaItemIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val windowIndex = (0 until playerConnection.player.currentTimeline.windowCount).firstOrNull {
                                        val window = androidx.media3.common.Timeline.Window()
                                        playerConnection.player.currentTimeline.getWindow(it, window)
                                        window.mediaItem.mediaId == item.mediaId
                                    } ?: index
                                    playerConnection.player.seekToDefaultPosition(windowIndex)
                                    showQueue = false
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.mediaMetadata.artworkUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF27272A))
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.mediaMetadata.title?.toString() ?: "Desconocido",
                                    color = if (isCurrent) (dominantColor ?: MaterialTheme.colorScheme.primary) else Color.White,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.mediaMetadata.artist?.toString() ?: "",
                                    color = if (isCurrent) (dominantColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isCurrent) {
                                Icon(
                                    Icons.Rounded.VolumeUp,
                                    contentDescription = "Reproduciendo",
                                    tint = dominantColor ?: MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
