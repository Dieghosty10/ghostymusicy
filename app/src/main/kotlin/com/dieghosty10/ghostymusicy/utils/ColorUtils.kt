package com.dieghosty10.ghostymusicy.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberDominantColor(model: Any?): Color? {
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(model) {
        if (model == null) {
            dominantColor = null
            return@LaunchedEffect
        }
        val request = ImageRequest.Builder(context)
            .data(model)
            .build()
        
        val result = ImageLoader(context).execute(request)
        if (result is SuccessResult) {
            val bitmap = result.image.toBitmap()
            withContext(Dispatchers.Default) {
                val palette = Palette.from(bitmap).generate()
                dominantColor = palette.dominantSwatch?.rgb?.let { Color(it) }
                    ?: palette.mutedSwatch?.rgb?.let { Color(it) }
            }
        }
    }

    return dominantColor
}
