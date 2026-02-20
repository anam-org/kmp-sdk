package ai.anam.lab.client.core.compression

/**
 * Compresses and resizes an image to reduce file size for upload.
 *
 * Scales the image so the longest side does not exceed [maxDimension] while preserving aspect ratio,
 * then encodes it as JPEG at the given [quality] (0–100).
 *
 * @param imageData Raw image bytes (any format decodable by the platform).
 * @param maxDimension Maximum pixel size for the longest side. Defaults to 1280.
 * @param quality JPEG compression quality (0–100). Defaults to 80.
 * @return Compressed JPEG image as a [ByteArray].
 */
expect suspend fun compressImage(imageData: ByteArray, maxDimension: Int = 1280, quality: Int = 80): ByteArray
