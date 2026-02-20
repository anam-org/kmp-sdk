package ai.anam.lab.client.core.compression

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun compressImage(imageData: ByteArray, maxDimension: Int, quality: Int): ByteArray =
    withContext(Dispatchers.Default) {
        // Convert Kotlin ByteArray to NSData by pinning memory and copying bytes
        val nsData = imageData.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = imageData.size.toULong())
        }

        // Decode NSData into a UIImage; return the original data if decoding fails
        val original = UIImage.imageWithData(nsData) ?: return@withContext imageData

        // Extract pixel dimensions from the CGSize struct
        val (width, height) = original.size.useContents { width to height }

        // Calculate a uniform scale factor so the longest side equals maxDimension
        val scale = if (width > maxDimension || height > maxDimension) {
            maxDimension.toDouble() / maxOf(width, height)
        } else {
            1.0
        }

        val newWidth = width * scale
        val newHeight = height * scale

        // Draw the image into a bitmap context at the new size (scale=1.0 for exact pixel dimensions)
        val newSize = CGSizeMake(newWidth, newHeight)
        UIGraphicsBeginImageContextWithOptions(newSize, true, 1.0)
        original.drawInRect(CGRectMake(0.0, 0.0, newWidth, newHeight))
        val resized = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        // Encode the resized image as JPEG; return the original data if encoding fails
        val imageToCompress = resized ?: original
        val jpegData = UIImageJPEGRepresentation(imageToCompress, quality / 100.0)
            ?: return@withContext imageData

        // Copy the JPEG NSData back into a Kotlin ByteArray
        val bytes = ByteArray(jpegData.length.toInt())
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), jpegData.bytes, jpegData.length)
        }
        bytes
    }
