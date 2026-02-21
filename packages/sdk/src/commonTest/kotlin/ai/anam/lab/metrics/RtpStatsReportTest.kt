package ai.anam.lab.metrics

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RtpStatsReportTest {

    private val json = statsJson

    @Test
    fun `RtpStatsReport round-trip preserves all fields`() {
        val report = RtpStatsReport(
            personaVideoStream = listOf(
                VideoStreamStats(
                    framesReceived = 120,
                    framesDropped = 2,
                    framesPerSecond = 29.5,
                    packetsReceived = 500,
                    packetsLost = 1,
                    resolution = "1920x1080",
                    jitter = 0.01,
                ),
            ),
            personaAudioStream = listOf(
                AudioInboundStats(
                    packetsReceived = 1000,
                    packetsLost = 3,
                    audioLevel = 0.8,
                    jitter = 0.02,
                    totalAudioEnergy = 1.5,
                ),
            ),
            userAudioInput = listOf(
                AudioOutboundStats(
                    packetsSent = 200,
                    retransmittedPackets = 1,
                    avgPacketSendDelay = 0.005,
                ),
            ),
            codecs = listOf(
                CodecStats(
                    status = "Active",
                    mimeType = "video/VP8",
                    payloadType = 96,
                    clockRate = 90000,
                    channels = null,
                ),
            ),
            transportLayer = listOf(
                TransportStats(
                    dtlsState = "connected",
                    iceState = "connected",
                    bytesSent = 50000,
                    bytesReceived = 120000,
                ),
            ),
            issues = listOf("Video: 2 frames dropped"),
        )

        val encoded = json.encodeToString(RtpStatsReport.serializer(), report)
        val decoded = json.decodeFromString<RtpStatsReport>(encoded)

        assertThat(decoded).isEqualTo(report)
    }

    @Test
    fun `null lists are omitted but empty issues is always present`() {
        val report = RtpStatsReport(issues = emptyList())

        val encoded = json.encodeToString(RtpStatsReport.serializer(), report)
        val obj = json.parseToJsonElement(encoded).jsonObject

        assertThat(obj.containsKey("personaVideoStream")).isEqualTo(false)
        assertThat(obj.containsKey("personaAudioStream")).isEqualTo(false)
        assertThat(obj.containsKey("userAudioInput")).isEqualTo(false)
        assertThat(obj.containsKey("codecs")).isEqualTo(false)
        assertThat(obj.containsKey("transportLayer")).isEqualTo(false)

        // issues must always be present, even when empty
        assertThat(obj.containsKey("issues")).isEqualTo(true)
        assertThat(obj["issues"]!!.jsonArray).isEqualTo(json.parseToJsonElement("[]").jsonArray)
    }

    @Test
    fun `zero-valued stats fields are included in JSON`() {
        val report = RtpStatsReport(
            personaVideoStream = listOf(VideoStreamStats()),
            issues = emptyList(),
        )

        val encoded = json.encodeToString(RtpStatsReport.serializer(), report)
        val video = json.parseToJsonElement(encoded).jsonObject["personaVideoStream"]!!
            .jsonArray[0].jsonObject

        // All non-nullable fields must be present even at their default (zero) values.
        assertThat(video.containsKey("framesReceived")).isEqualTo(true)
        assertThat(video.containsKey("framesDropped")).isEqualTo(true)
        assertThat(video.containsKey("framesPerSecond")).isEqualTo(true)
        assertThat(video.containsKey("packetsReceived")).isEqualTo(true)
        assertThat(video.containsKey("packetsLost")).isEqualTo(true)

        // Nullable fields remain absent when null.
        assertThat(video.containsKey("resolution")).isEqualTo(false)
        assertThat(video.containsKey("jitter")).isEqualTo(false)
    }

    @Test
    fun `RemoteRtpStatsMessage has correct message_type`() {
        val report = RtpStatsReport(issues = emptyList())
        val message = RemoteRtpStatsMessage(data = report)

        val encoded = json.encodeToString(RemoteRtpStatsMessage.serializer(), message)
        val obj = json.parseToJsonElement(encoded).jsonObject

        assertThat(obj["message_type"]!!.jsonPrimitive.content).isEqualTo("remote_rtp_stats")
        assertThat(obj.containsKey("data")).isEqualTo(true)
    }

    @Test
    fun `RemoteRtpStatsMessage round-trip preserves data`() {
        val report = RtpStatsReport(
            personaVideoStream = listOf(
                VideoStreamStats(framesReceived = 60, framesPerSecond = 25.0),
            ),
            issues = listOf("Video: Low frame rate (10.0 fps)"),
        )
        val message = RemoteRtpStatsMessage(data = report)

        val encoded = json.encodeToString(RemoteRtpStatsMessage.serializer(), message)
        val decoded = json.decodeFromString<RemoteRtpStatsMessage>(encoded)

        assertThat(decoded.messageType).isEqualTo("remote_rtp_stats")
        assertThat(decoded.data).isEqualTo(report)
    }

    @Test
    fun `JSON structure matches JS SDK format`() {
        val report = RtpStatsReport(
            personaVideoStream = listOf(
                VideoStreamStats(
                    framesReceived = 100,
                    resolution = "640x480",
                ),
            ),
            issues = listOf("test issue"),
        )
        val message = RemoteRtpStatsMessage(data = report)

        val encoded = json.encodeToString(RemoteRtpStatsMessage.serializer(), message)
        val root = json.parseToJsonElement(encoded).jsonObject

        // Top-level keys match JS SDK
        assertThat(root["message_type"]!!.jsonPrimitive.content).isEqualTo("remote_rtp_stats")

        val data = root["data"]!!.jsonObject
        assertThat(data.containsKey("personaVideoStream")).isEqualTo(true)
        assertThat(data.containsKey("issues")).isEqualTo(true)

        // Video stream fields use camelCase
        val video = data["personaVideoStream"]!!.jsonArray[0].jsonObject
        assertThat(video.containsKey("framesReceived")).isEqualTo(true)
        assertThat(video.containsKey("resolution")).isEqualTo(true)

        // Issues is a string array
        val issues = data["issues"]!!.jsonArray
        assertThat(issues[0].jsonPrimitive.content).isEqualTo("test issue")
    }

    @Test
    fun `statsJson does not use pretty print`() {
        val message = RemoteRtpStatsMessage(data = RtpStatsReport())
        val encoded = json.encodeToString(RemoteRtpStatsMessage.serializer(), message)
        assertThat(encoded.contains("\n")).isEqualTo(false)
    }
}
