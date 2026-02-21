package ai.anam.lab.metrics

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.test.Test

class RtpStatsParserTest {

    // -- Video inbound --------------------------------------------------------

    @Test
    fun `parses video inbound stats`() {
        val stats = listOf(
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01V1",
                members = mapOf(
                    "kind" to "video",
                    "framesReceived" to 120L,
                    "framesDropped" to 0L,
                    "framesPerSecond" to 30.0,
                    "packetsReceived" to 500L,
                    "packetsLost" to 0L,
                    "frameWidth" to 1920L,
                    "frameHeight" to 1080L,
                    "jitter" to 0.01,
                ),
            ),
        )

        val report = parseRtpStats(stats)

        assertThat(report.personaVideoStream).isNotNull().hasSize(1)
        val video = report.personaVideoStream!!.first()
        assertThat(video.framesReceived).isEqualTo(120L)
        assertThat(video.framesDropped).isEqualTo(0L)
        assertThat(video.framesPerSecond).isEqualTo(30.0)
        assertThat(video.packetsReceived).isEqualTo(500L)
        assertThat(video.packetsLost).isEqualTo(0L)
        assertThat(video.resolution).isEqualTo("1920x1080")
        assertThat(video.jitter).isEqualTo(0.01)
    }

    @Test
    fun `resolution is null when frame dimensions are missing`() {
        val stats = listOf(
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01V1",
                members = mapOf("kind" to "video"),
            ),
        )

        val report = parseRtpStats(stats)
        assertThat(report.personaVideoStream!!.first().resolution).isNull()
    }

    // -- Audio inbound --------------------------------------------------------

    @Test
    fun `parses audio inbound stats`() {
        val stats = listOf(
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01A1",
                members = mapOf(
                    "kind" to "audio",
                    "packetsReceived" to 1000L,
                    "packetsLost" to 2L,
                    "audioLevel" to 0.75,
                    "jitter" to 0.02,
                    "totalAudioEnergy" to 1.5,
                ),
            ),
        )

        val report = parseRtpStats(stats)

        assertThat(report.personaAudioStream).isNotNull().hasSize(1)
        val audio = report.personaAudioStream!!.first()
        assertThat(audio.packetsReceived).isEqualTo(1000L)
        assertThat(audio.packetsLost).isEqualTo(2L)
        assertThat(audio.audioLevel).isEqualTo(0.75)
        assertThat(audio.jitter).isEqualTo(0.02)
        assertThat(audio.totalAudioEnergy).isEqualTo(1.5)
    }

    // -- Audio outbound -------------------------------------------------------

    @Test
    fun `parses audio outbound stats with avg delay calculation`() {
        val stats = listOf(
            RawRtcStats(
                type = "outbound-rtp",
                id = "OT01A1",
                members = mapOf(
                    "kind" to "audio",
                    "packetsSent" to 200L,
                    "retransmittedPacketsSent" to 3L,
                    "totalPacketSendDelay" to 1.0,
                ),
            ),
        )

        val report = parseRtpStats(stats)

        assertThat(report.userAudioInput).isNotNull().hasSize(1)
        val outbound = report.userAudioInput!!.first()
        assertThat(outbound.packetsSent).isEqualTo(200L)
        assertThat(outbound.retransmittedPackets).isEqualTo(3L)
        assertThat(outbound.avgPacketSendDelay).isEqualTo(1.0 / 200)
    }

    @Test
    fun `avg delay is null when totalPacketSendDelay is missing`() {
        val stats = listOf(
            RawRtcStats(
                type = "outbound-rtp",
                id = "OT01A1",
                members = mapOf("kind" to "audio", "packetsSent" to 100L),
            ),
        )

        val report = parseRtpStats(stats)
        assertThat(report.userAudioInput!!.first().avgPacketSendDelay).isNull()
    }

    @Test
    fun `avg delay is null when packetsSent is zero`() {
        val stats = listOf(
            RawRtcStats(
                type = "outbound-rtp",
                id = "OT01A1",
                members = mapOf(
                    "kind" to "audio",
                    "packetsSent" to 0L,
                    "totalPacketSendDelay" to 1.0,
                ),
            ),
        )

        val report = parseRtpStats(stats)
        assertThat(report.userAudioInput!!.first().avgPacketSendDelay).isNull()
    }

    // -- Codecs ---------------------------------------------------------------

    @Test
    fun `parses codec stats`() {
        val stats = listOf(
            RawRtcStats(
                type = "codec",
                id = "COT01",
                members = mapOf(
                    "mimeType" to "video/VP8",
                    "payloadType" to 96,
                    "clockRate" to 90000,
                    "channels" to 1,
                ),
            ),
        )

        val report = parseRtpStats(stats)

        assertThat(report.codecs).isNotNull().hasSize(1)
        val codec = report.codecs!!.first()
        assertThat(codec.status).isEqualTo("Active")
        assertThat(codec.mimeType).isEqualTo("video/VP8")
        assertThat(codec.payloadType).isEqualTo(96)
        assertThat(codec.clockRate).isEqualTo(90000)
        assertThat(codec.channels).isEqualTo(1)
    }

    @Test
    fun `skips codec when mimeType is missing`() {
        val stats = listOf(
            RawRtcStats(
                type = "codec",
                id = "COT01",
                members = mapOf("payloadType" to 96),
            ),
        )

        val report = parseRtpStats(stats)
        assertThat(report.codecs).isNull()
    }

    // -- Transport ------------------------------------------------------------

    @Test
    fun `parses transport stats`() {
        val stats = listOf(
            RawRtcStats(
                type = "transport",
                id = "T01",
                members = mapOf(
                    "dtlsState" to "connected",
                    "iceState" to "connected",
                    "bytesSent" to 50000L,
                    "bytesReceived" to 120000L,
                ),
            ),
        )

        val report = parseRtpStats(stats)

        assertThat(report.transportLayer).isNotNull().hasSize(1)
        val transport = report.transportLayer!!.first()
        assertThat(transport.dtlsState).isEqualTo("connected")
        assertThat(transport.iceState).isEqualTo("connected")
        assertThat(transport.bytesSent).isEqualTo(50000L)
        assertThat(transport.bytesReceived).isEqualTo(120000L)
    }

    @Test
    fun `skips transport when dtlsState is missing`() {
        val stats = listOf(
            RawRtcStats(
                type = "transport",
                id = "T01",
                members = mapOf("iceState" to "connected"),
            ),
        )

        val report = parseRtpStats(stats)
        assertThat(report.transportLayer).isNull()
    }

    // -- Issue detection ------------------------------------------------------

    @Test
    fun `detects dropped frames`() {
        val stats = listOf(videoInbound(framesDropped = 5L))
        val report = parseRtpStats(stats)
        assertThat(report.issues).containsExactly("Video: 5 frames dropped")
    }

    @Test
    fun `detects video packet loss`() {
        val stats = listOf(videoInbound(packetsLost = 10L))
        val report = parseRtpStats(stats)
        assertThat(report.issues).containsExactly("Video: 10 packets lost")
    }

    @Test
    fun `detects low frame rate`() {
        val stats = listOf(videoInbound(framesPerSecond = 10.0))
        val report = parseRtpStats(stats)
        assertThat(report.issues).containsExactly("Video: Low frame rate (10.0 fps)")
    }

    @Test
    fun `does not flag frame rate at threshold`() {
        val stats = listOf(videoInbound(framesPerSecond = 23.0))
        val report = parseRtpStats(stats)
        assertThat(report.issues).isEmpty()
    }

    @Test
    fun `does not flag frame rate when zero`() {
        val stats = listOf(videoInbound(framesPerSecond = 0.0))
        val report = parseRtpStats(stats)
        assertThat(report.issues).isEmpty()
    }

    @Test
    fun `detects audio packet loss`() {
        val stats = listOf(
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01A1",
                members = mapOf("kind" to "audio", "packetsLost" to 7L),
            ),
        )
        val report = parseRtpStats(stats)
        assertThat(report.issues).containsExactly("Audio: 7 packets lost")
    }

    @Test
    fun `does not flag jitter at threshold`() {
        val stats = listOf(
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01A1",
                members = mapOf("kind" to "audio", "jitter" to 0.1),
            ),
        )
        val report = parseRtpStats(stats)
        assertThat(report.issues).isEmpty()
    }

    @Test
    fun `detects high audio jitter in milliseconds`() {
        val stats = listOf(
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01A1",
                members = mapOf("kind" to "audio", "jitter" to 0.15),
            ),
        )
        val report = parseRtpStats(stats)
        assertThat(report.issues).containsExactly("Audio: High jitter (150.0ms)")
    }

    @Test
    fun `no issues when all values are healthy`() {
        val stats = listOf(
            videoInbound(framesDropped = 0L, packetsLost = 0L, framesPerSecond = 30.0),
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01A1",
                members = mapOf("kind" to "audio", "packetsLost" to 0L, "jitter" to 0.01),
            ),
        )
        val report = parseRtpStats(stats)
        assertThat(report.issues).isEmpty()
    }

    // -- Edge cases -----------------------------------------------------------

    @Test
    fun `empty stats list produces empty report`() {
        val report = parseRtpStats(emptyList())
        assertThat(report.personaVideoStream).isNull()
        assertThat(report.personaAudioStream).isNull()
        assertThat(report.userAudioInput).isNull()
        assertThat(report.codecs).isNull()
        assertThat(report.transportLayer).isNull()
        assertThat(report.issues).isEmpty()
    }

    @Test
    fun `ignores unknown stat types`() {
        val stats = listOf(
            RawRtcStats(type = "candidate-pair", id = "CP01", members = mapOf("state" to "succeeded")),
            RawRtcStats(type = "remote-inbound-rtp", id = "RI01", members = mapOf("kind" to "video")),
        )
        val report = parseRtpStats(stats)
        assertThat(report.personaVideoStream).isNull()
    }

    @Test
    fun `handles missing kind in inbound-rtp`() {
        val stats = listOf(
            RawRtcStats(type = "inbound-rtp", id = "IT01", members = emptyMap()),
        )
        val report = parseRtpStats(stats)
        assertThat(report.personaVideoStream).isNull()
        assertThat(report.personaAudioStream).isNull()
    }

    @Test
    fun `handles numeric values as strings`() {
        val stats = listOf(
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01V1",
                members = mapOf(
                    "kind" to "video",
                    "framesReceived" to "120",
                    "packetsLost" to "5",
                    "jitter" to "0.02",
                ),
            ),
        )
        val report = parseRtpStats(stats)
        val video = report.personaVideoStream!!.first()
        assertThat(video.framesReceived).isEqualTo(120L)
        assertThat(video.packetsLost).isEqualTo(5L)
        assertThat(video.jitter).isEqualTo(0.02)
    }

    @Test
    fun `handles multiple stats of the same type`() {
        val stats = listOf(
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01V1",
                members = mapOf("kind" to "video", "framesReceived" to 100L),
            ),
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01V2",
                members = mapOf("kind" to "video", "framesReceived" to 200L),
            ),
        )
        val report = parseRtpStats(stats)
        assertThat(report.personaVideoStream).isNotNull().hasSize(2)
        assertThat(report.personaVideoStream!![0].framesReceived).isEqualTo(100L)
        assertThat(report.personaVideoStream!![1].framesReceived).isEqualTo(200L)
    }

    // -- Full report ----------------------------------------------------------

    @Test
    fun `full report with all stat types populated`() {
        val stats = listOf(
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01V1",
                members = mapOf(
                    "kind" to "video",
                    "framesReceived" to 120L,
                    "framesDropped" to 1L,
                    "framesPerSecond" to 30.0,
                    "packetsReceived" to 500L,
                    "packetsLost" to 0L,
                    "frameWidth" to 1280L,
                    "frameHeight" to 720L,
                    "jitter" to 0.01,
                ),
            ),
            RawRtcStats(
                type = "inbound-rtp",
                id = "IT01A1",
                members = mapOf(
                    "kind" to "audio",
                    "packetsReceived" to 1000L,
                    "packetsLost" to 0L,
                    "audioLevel" to 0.6,
                    "jitter" to 0.02,
                    "totalAudioEnergy" to 1.2,
                ),
            ),
            RawRtcStats(
                type = "outbound-rtp",
                id = "OT01A1",
                members = mapOf(
                    "kind" to "audio",
                    "packetsSent" to 800L,
                    "retransmittedPacketsSent" to 2L,
                    "totalPacketSendDelay" to 0.4,
                ),
            ),
            RawRtcStats(
                type = "codec",
                id = "COT01",
                members = mapOf("mimeType" to "video/VP8", "payloadType" to 96, "clockRate" to 90000),
            ),
            RawRtcStats(
                type = "codec",
                id = "COT02",
                members = mapOf("mimeType" to "audio/opus", "payloadType" to 111, "clockRate" to 48000),
            ),
            RawRtcStats(
                type = "transport",
                id = "T01",
                members = mapOf(
                    "dtlsState" to "connected",
                    "iceState" to "connected",
                    "bytesSent" to 50000L,
                    "bytesReceived" to 120000L,
                ),
            ),
        )

        val report = parseRtpStats(stats)

        // Video
        assertThat(report.personaVideoStream).isNotNull().hasSize(1)
        assertThat(report.personaVideoStream!!.first().resolution).isEqualTo("1280x720")

        // Audio inbound
        assertThat(report.personaAudioStream).isNotNull().hasSize(1)
        assertThat(report.personaAudioStream!!.first().audioLevel).isEqualTo(0.6)

        // Audio outbound
        assertThat(report.userAudioInput).isNotNull().hasSize(1)
        assertThat(report.userAudioInput!!.first().avgPacketSendDelay).isEqualTo(0.4 / 800)

        // Codecs — all marked Active (matching JS SDK behaviour)
        assertThat(report.codecs).isNotNull().hasSize(2)
        report.codecs!!.forEach { assertThat(it.status).isEqualTo("Active") }

        // Transport
        assertThat(report.transportLayer).isNotNull().hasSize(1)

        // Issues — only the dropped frame should be flagged
        assertThat(report.issues).containsExactly("Video: 1 frames dropped")
    }

    // -- Helpers --------------------------------------------------------------

    private fun videoInbound(
        framesReceived: Long = 100L,
        framesDropped: Long = 0L,
        framesPerSecond: Double = 30.0,
        packetsReceived: Long = 500L,
        packetsLost: Long = 0L,
        jitter: Double = 0.01,
    ): RawRtcStats = RawRtcStats(
        type = "inbound-rtp",
        id = "IT01V1",
        members = mapOf(
            "kind" to "video",
            "framesReceived" to framesReceived,
            "framesDropped" to framesDropped,
            "framesPerSecond" to framesPerSecond,
            "packetsReceived" to packetsReceived,
            "packetsLost" to packetsLost,
            "jitter" to jitter,
        ),
    )
}
