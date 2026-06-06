package com.dieghosty10.ghostymusicy.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
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

private val colorCache = LruCache<String, Color>(100)

@Composable
fun rememberDominantColor(model: Any?): Color? {
    val context = LocalContext.current
    val key = model?.toString()
    
    var dominantColor by remember(key) { 
        mutableStateOf(if (key != null) colorCache.get(key) else null) 
    }

    LaunchedEffect(key) {
        if (key == null || dominantColor != null) {
            return@LaunchedEffect
        }
        
        val request = ImageRequest.Builder(context)
            .data(model)
            .build()
        
        val result = ImageLoader(context).execute(request)
        if (result is SuccessResult) {
            val bitmap = result.image.toBitmap()
            
            withContext(Dispatchers.Default) {
                try {
                    val swBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                        bitmap.copy(Bitmap.Config.ARGB_8888, false)
                    } else bitmap
                    
                    val palette = Palette.from(swBitmap).generate()
                    val extractedColor = palette.dominantSwatch?.rgb?.let { Color(it) }
                        ?: palette.mutedSwatch?.rgb?.let { Color(it) }
                        
                    if (extractedColor != null) {
                        colorCache.put(key, extractedColor)
                        dominantColor = extractedColor
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    return dominantColor
}
