package ai.anam.lab.metrics

import com.shepeliev.webrtckmp.RtcStatsReport

/**
 * Lightweight snapshot of a single WebRTC stats entry, used as the parser input so that the parsing logic is decoupled
 * from the platform-specific `RtcStats` expect class and can be unit-tested in common code.
 */
internal data class RawRtcStats(val type: String, val id: String, val members: Map<String, Any>)

/**
 * Converts a webrtc-kmp [RtcStatsReport] into an [RtpStatsReport] suitable for sending via the data channel.
 */
internal fun RtcStatsReport.toRtpStatsReport(): RtpStatsReport {
    val raw = stats.values.map { RawRtcStats(type = it.type, id = it.id, members = it.members) }
    return parseRtpStats(raw)
}

/**
 * Parses a list of raw WebRTC stats entries into a structured [RtpStatsReport]. This mirrors the JS SDK's
 * `createRTCStatsReport()` function.
 */
internal fun parseRtpStats(stats: List<RawRtcStats>): RtpStatsReport {
    val videoInbound = mutableListOf<VideoStreamStats>()
    val audioInbound = mutableListOf<AudioInboundStats>()
    val audioOutbound = mutableListOf<AudioOutboundStats>()
    val codecs = mutableListOf<CodecStats>()
    val transport = mutableListOf<TransportStats>()
    val issues = mutableListOf<String>()

    for (stat in stats) {
        when (stat.type) {
            "inbound-rtp" -> {
                when (stat.members.stringOrNull("kind")) {
                    "video" -> parseVideoInbound(stat.members).let { (entry, entryIssues) ->
                        videoInbound += entry
                        issues += entryIssues
                    }
                    "audio" -> parseAudioInbound(stat.members).let { (entry, entryIssues) ->
                        audioInbound += entry
                        issues += entryIssues
                    }
                }
            }
            "outbound-rtp" -> {
                if (stat.members.stringOrNull("kind") == "audio") {
                    audioOutbound += parseAudioOutbound(stat.members)
                }
            }
            "codec" -> parseCodec(stat.members)?.let { codecs += it }
            "transport" -> parseTransport(stat.members)?.let { transport += it }
        }
    }

    return RtpStatsReport(
        personaVideoStream = videoInbound.ifEmpty { null },
        personaAudioStream = audioInbound.ifEmpty { null },
        userAudioInput = audioOutbound.ifEmpty { null },
        codecs = codecs.ifEmpty { null },
        transportLayer = transport.ifEmpty { null },
        issues = issues,
    )
}

// -- Individual stat parsers --------------------------------------------------

private fun parseVideoInbound(m: Map<String, Any>): Pair<VideoStreamStats, List<String>> {
    val framesReceived = m.long("framesReceived")
    val framesDropped = m.long("framesDropped")
    val framesPerSecond = m.double("framesPerSecond")
    val packetsReceived = m.long("packetsReceived")
    val packetsLost = m.long("packetsLost")
    val jitter = m.doubleOrNull("jitter")
    val width = m.longOrNull("frameWidth")
    val height = m.longOrNull("frameHeight")
    val resolution = if (width != null && height != null) "${width}x$height" else null

    val issues = buildList {
        if (framesDropped > 0) add("Video: $framesDropped frames dropped")
        if (packetsLost > 0) add("Video: $packetsLost packets lost")
        if (framesPerSecond > 0 && framesPerSecond < LOW_FRAME_RATE_THRESHOLD) {
            add("Video: Low frame rate ($framesPerSecond fps)")
        }
    }

    return VideoStreamStats(
        framesReceived = framesReceived,
        framesDropped = framesDropped,
        framesPerSecond = framesPerSecond,
        packetsReceived = packetsReceived,
        packetsLost = packetsLost,
        resolution = resolution,
        jitter = jitter,
    ) to issues
}

private fun parseAudioInbound(m: Map<String, Any>): Pair<AudioInboundStats, List<String>> {
    val packetsLost = m.long("packetsLost")
    val jitter = m.doubleOrNull("jitter")

    val issues = buildList {
        if (packetsLost > 0) add("Audio: $packetsLost packets lost")
        if (jitter != null && jitter > HIGH_JITTER_THRESHOLD) {
            val jitterMs = kotlin.math.round(jitter * 10000) / 10.0
            add("Audio: High jitter (${jitterMs}ms)")
        }
    }

    return AudioInboundStats(
        packetsReceived = m.long("packetsReceived"),
        packetsLost = packetsLost,
        audioLevel = m.double("audioLevel"),
        jitter = jitter,
        totalAudioEnergy = m.doubleOrNull("totalAudioEnergy"),
    ) to issues
}

private fun parseAudioOutbound(m: Map<String, Any>): AudioOutboundStats {
    val packetsSent = m.long("packetsSent")
    val retransmitted = m.longOrNull("retransmittedPacketsSent")
    val totalDelay = m.doubleOrNull("totalPacketSendDelay")
    val avgDelay = if (totalDelay != null && packetsSent > 0) totalDelay / packetsSent else null

    return AudioOutboundStats(
        packetsSent = packetsSent,
        retransmittedPackets = retransmitted,
        avgPacketSendDelay = avgDelay,
    )
}

private fun parseCodec(m: Map<String, Any>): CodecStats? {
    val mimeType = m.stringOrNull("mimeType") ?: return null
    val payloadType = m.intOrNull("payloadType") ?: return null

    return CodecStats(
        status = "Active",
        mimeType = mimeType,
        payloadType = payloadType,
        clockRate = m.intOrNull("clockRate"),
        channels = m.intOrNull("channels"),
    )
}

private fun parseTransport(m: Map<String, Any>): TransportStats? {
    val dtlsState = m.stringOrNull("dtlsState") ?: return null
    val iceState = m.stringOrNull("iceState") ?: m.stringOrNull("iceLocalCandidateId")?.let { "unknown" } ?: return null

    return TransportStats(
        dtlsState = dtlsState,
        iceState = iceState,
        bytesSent = m.longOrNull("bytesSent"),
        bytesReceived = m.longOrNull("bytesReceived"),
    )
}

// -- Safe member accessors ----------------------------------------------------

private fun Map<String, Any>.stringOrNull(key: String): String? = get(key)?.toString()

private fun Map<String, Any>.long(key: String): Long = when (val v = get(key)) {
    is Number -> v.toLong()
    is String -> v.toLongOrNull() ?: 0L
    else -> 0L
}

private fun Map<String, Any>.longOrNull(key: String): Long? = when (val v = get(key)) {
    is Number -> v.toLong()
    is String -> v.toLongOrNull()
    else -> null
}

private fun Map<String, Any>.double(key: String): Double = when (val v = get(key)) {
    is Number -> v.toDouble()
    is String -> v.toDoubleOrNull() ?: 0.0
    else -> 0.0
}

private fun Map<String, Any>.doubleOrNull(key: String): Double? = when (val v = get(key)) {
    is Number -> v.toDouble()
    is String -> v.toDoubleOrNull()
    else -> null
}

private fun Map<String, Any>.intOrNull(key: String): Int? = when (val v = get(key)) {
    is Number -> v.toInt()
    is String -> v.toIntOrNull()
    else -> null
}

// -- Thresholds ---------------------------------------------------------------

private const val LOW_FRAME_RATE_THRESHOLD = 23.0
private const val HIGH_JITTER_THRESHOLD = 0.1
