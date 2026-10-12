package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun SnowhiteNetworkImage(
    url: String?,
    productName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = url, key2 = productName) {
        value = withContext(Dispatchers.IO) {
            val resolvedUrl = resolveImageUrl(url, productName)
            downloadBitmap(resolvedUrl) ?: downloadBitmap(getUnsplashFallbackUrl(productName))
        }
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = productName,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF0061E0),
                strokeWidth = 2.dp
            )
        }
    }
}

private fun resolveImageUrl(url: String?, productName: String): String {
    if (url.isNullOrBlank() || url == "null") {
        return getUnsplashFallbackUrl(productName)
    }
    return com.example.config.AppConfig.resolveImageUrl(url)
}

private fun downloadBitmap(urlString: String): ImageBitmap? {
    try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.connect()

        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            conn.inputStream.use { stream ->
                val bmp = BitmapFactory.decodeStream(stream)
                return bmp?.asImageBitmap()
            }
        }
    } catch (_: Exception) {
    }
    return null
}

private fun getUnsplashFallbackUrl(productName: String): String {
    val nameLower = productName.lowercase()
    return when {
        nameLower.contains("suit") -> "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&q=80"
        nameLower.contains("shirt") -> "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600&q=80"
        nameLower.contains("kurta") || nameLower.contains("shalwar") -> "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&q=80"
        nameLower.contains("sherwani") || nameLower.contains("achkan") -> "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?w=600&q=80"
        nameLower.contains("dress") || nameLower.contains("gown") || nameLower.contains("saree") -> "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=600&q=80"
        nameLower.contains("blanket") || nameLower.contains("comforter") || nameLower.contains("duvet") -> "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=600&q=80"
        nameLower.contains("curtain") -> "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600&q=80"
        nameLower.contains("powder") || nameLower.contains("detergent") || nameLower.contains("softener") || nameLower.contains("liquid") -> "https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=600&q=80"
        else -> "https://images.unsplash.com/photo-1585421514738-01798e348b17?w=600&q=80"
    }
}
