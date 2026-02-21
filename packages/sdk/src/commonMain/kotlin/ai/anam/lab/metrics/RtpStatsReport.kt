@file:OptIn(ExperimentalSerializationApi::class)

package ai.anam.lab.metrics

import ai.anam.lab.api.defaultJsonConfiguration
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A structured report of WebRTC connection statistics, matching the JS SDK's `RTCStatsJsonReport` format.
 * Sent periodically via the data channel as `remote_rtp_stats`.
 */
@Serializable
internal data class RtpStatsReport(
    val personaVideoStream: List<VideoStreamStats>? = null,
    val personaAudioStream: List<AudioInboundStats>? = null,
    val userAudioInput: List<AudioOutboundStats>? = null,
    val codecs: List<CodecStats>? = null,
    val transportLayer: List<TransportStats>? = null,
    val issues: List<String> = emptyList(),
)

@Serializable
internal data class VideoStreamStats(
    val framesReceived: Long = 0,
    val framesDropped: Long = 0,
    val framesPerSecond: Double = 0.0,
    val packetsReceived: Long = 0,
    val packetsLost: Long = 0,
    val resolution: String? = null,
    val jitter: Double? = null,
)

@Serializable
internal data class AudioInboundStats(
    val packetsReceived: Long = 0,
    val packetsLost: Long = 0,
    val audioLevel: Double = 0.0,
    val jitter: Double? = null,
    val totalAudioEnergy: Double? = null,
)

@Serializable
internal data class AudioOutboundStats(
    val packetsSent: Long = 0,
    val retransmittedPackets: Long? = null,
    val avgPacketSendDelay: Double? = null,
)

@Serializable
internal data class CodecStats(
    val status: String,
    val mimeType: String,
    val payloadType: Int,
    val clockRate: Int? = null,
    val channels: Int? = null,
)

@Serializable
internal data class TransportStats(
    val dtlsState: String,
    val iceState: String,
    val bytesSent: Long? = null,
    val bytesReceived: Long? = null,
)

/**
 * The envelope sent over the data channel. Serialised to JSON and passed directly to `DataChannel.send()`.
 */
@Serializable
internal data class RemoteRtpStatsMessage(
    @EncodeDefault
    @SerialName("message_type")
    val messageType: String = "remote_rtp_stats",
    val data: RtpStatsReport,
)

/**
 * [Json] instance used exclusively for serialising [RemoteRtpStatsMessage]. Unlike [defaultJsonConfiguration], this
 * enables [encodeDefaults][Json.encodeDefaults] so that zero-valued fields (e.g. `framesReceived = 0`) and empty
 * collections (e.g. `issues = []`) are always present in the output, matching the JS SDK's format.
 */
internal val statsJson = Json(from = defaultJsonConfiguration) {
    encodeDefaults = true
    explicitNulls = false
    prettyPrint = false
}
