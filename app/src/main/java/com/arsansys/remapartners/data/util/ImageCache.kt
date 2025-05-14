package com.arsansys.remapartners.data.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.arsansys.remapartners.data.repository.ImageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

class ImageCache(private val imageRepository: ImageRepository) {

    // Caché en memoria para las imágenes ya descargadas
    private val memoryCache = LruCache<String, ByteArray>(
        (Runtime.getRuntime().maxMemory() / 8).toInt()
    )

    // Registro de tareas en curso para evitar solicitudes duplicadas
    private val ongoingRequests = ConcurrentHashMap<String, Boolean>()

    // Obtener imagen desde la caché o descargarla
    suspend fun getImage(imageUrl: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            // Extraer el nombre del archivo
            val fileName = extractFileName(imageUrl)

            // Comprobar si ya está en la caché
            val cachedImage = memoryCache.get(fileName)
            if (cachedImage != null) {
                return@withContext cachedImage
            }

            // Evitar solicitudes duplicadas
            if (ongoingRequests[fileName] == true) {
                // Esperar un poco y volver a comprobar la caché
                var attempts = 0
                while (attempts < 5) {
                    kotlinx.coroutines.delay(300)
                    memoryCache.get(fileName)?.let { return@withContext it }
                    attempts++
                }
                return@withContext null
            }

            // Marcar como en curso
            ongoingRequests[fileName] = true

            try {
                // Descargar la imagen
                val imageBytes = imageRepository.getImageBytes(fileName)

                // Optimizar el tamaño si es necesario
                val optimizedBytes = imageBytes?.let { optimizeImageBytes(it) }

                // Guardar en caché
                optimizedBytes?.let { memoryCache.put(fileName, it) }

                return@withContext optimizedBytes
            } finally {
                // Marcar como completada incluso si hay error
                ongoingRequests.remove(fileName)
            }
        } catch (e: CancellationException) {
            throw e // Permitir la propagación de cancelaciones
        } catch (e: Exception) {
            return@withContext null
        }
    }

    // Cargar múltiples imágenes de manera eficiente
    suspend fun preloadImages(imageUrls: List<String>, maxConcurrent: Int = 3) =
        withContext(Dispatchers.IO) {
            imageUrls.chunked(maxConcurrent).forEach { chunk ->
                val deferreds = chunk.map { url ->
                    async { getImage(extractFileName(url)) }
                }
                deferreds.awaitAll()
            }
        }

    // Optimizar el tamaño de la imagen para miniaturas
    private fun optimizeImageBytes(bytes: ByteArray, maxSize: Int = 600): ByteArray {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        // Si la imagen es pequeña, no es necesario reescalarla
        if (options.outWidth <= maxSize && options.outHeight <= maxSize) {
            return bytes
        }

        // Calcular factor de escala
        val scale = Math.max(
            options.outWidth.toFloat() / maxSize,
            options.outHeight.toFloat() / maxSize
        ).toInt()

        // Decodificar con el factor de escala
        val bitmap =
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply {
                inSampleSize = scale
            })

        // Convertir a ByteArray
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val result = outputStream.toByteArray()
        bitmap.recycle()

        return result
    }

    // Extraer el nombre del archivo de la URL
    private fun extractFileName(imageUrl: String): String {
        return if (imageUrl.contains("/api/images/")) {
            imageUrl.substringAfterLast("/api/images/")
        } else {
            imageUrl.substringAfterLast("/")
        }
    }

    // Limpiar caché cuando sea necesario
    fun clearCache() {
        memoryCache.evictAll()
    }
}