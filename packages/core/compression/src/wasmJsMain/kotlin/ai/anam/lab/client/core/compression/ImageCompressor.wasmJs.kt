package ai.anam.lab.client.core.compression

import kotlin.coroutines.resume
import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.khronos.webgl.toInt8Array

actual suspend fun compressImage(imageData: ByteArray, maxDimension: Int, quality: Int): ByteArray =
    suspendCancellableCoroutine { cont ->
        val jsImageData = imageData.toInt8Array()
        compressImageJs(jsImageData, maxDimension, quality) { result ->
            @Suppress("UNCHECKED_CAST")
            val bytes = Int8Array(result as ArrayBuffer).toByteArray()
            cont.resume(bytes)
        }
    }

@OptIn(ExperimentalWasmJsInterop::class)
private fun compressImageJs(imageData: JsAny, maxDimension: Int, quality: Int, callback: (JsAny) -> Unit): Unit = js(
    """{
    var blob = new Blob([imageData]);
    var url = URL.createObjectURL(blob);
    var img = new Image();
    img.onload = function() {
        URL.revokeObjectURL(url);
        var w = img.width;
        var h = img.height;
        var scale = 1;
        if (w > maxDimension || h > maxDimension) {
            scale = maxDimension / Math.max(w, h);
        }
        var nw = Math.round(w * scale);
        var nh = Math.round(h * scale);
        var canvas = document.createElement('canvas');
        canvas.width = nw;
        canvas.height = nh;
        var ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, nw, nh);
        canvas.toBlob(function(b) {
            var reader = new FileReader();
            reader.onload = function(ev) {
                callback(ev.target.result);
            };
            reader.readAsArrayBuffer(b);
        }, 'image/jpeg', quality / 100.0);
    };
    img.src = url;
}""",
)
