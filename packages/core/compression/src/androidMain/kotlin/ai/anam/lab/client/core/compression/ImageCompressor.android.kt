package ai.anam.lab.client.core.compression

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun compressImage(imageData: ByteArray, maxDimension: Int, quality: Int): ByteArray =
    withContext(Dispatchers.Default) {
        // Decode the raw bytes into a Bitmap; return the original data if decoding fails
        val original = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            ?: return@withContext imageData

        val width = original.width
        val height = original.height

        // If the image already fits within maxDimension, just re-encode as JPEG
        if (width <= maxDimension && height <= maxDimension) {
            val output = ByteArrayOutputStream()
            original.compress(Bitmap.CompressFormat.JPEG, quality, output)
            original.recycle()
            return@withContext output.toByteArray()
        }

        // Calculate a uniform scale factor so the longest side equals maxDimension
        val scale = maxDimension.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        // Resize the bitmap using bilinear filtering (filter = true)
        val scaled = Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
        original.recycle()

        // Encode the resized bitmap as JPEG and free native memory
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
        scaled.recycle()

        output.toByteArray()
    }
