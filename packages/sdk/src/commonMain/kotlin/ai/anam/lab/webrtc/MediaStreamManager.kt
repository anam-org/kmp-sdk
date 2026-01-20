package ai.anam.lab.webrtc

import ai.anam.lab.utils.Logger
import ai.anam.lab.webrtc.MediaStreamManagerImpl.MediaAccessException
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.MediaStreamTrack
import com.shepeliev.webrtckmp.MediaStreamTrackKind
import com.shepeliev.webrtckmp.VideoTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * This class provides access to [MediaStream]s, both locally and any obtained remotely.
 */
internal interface MediaStreamManager {
    /**
     * Attempts to initialise the [MediaStream] for local audio.
     *
     * @throws MediaAccessException if the microphone access is denied, due to missing permissions.
     */
    suspend fun initializeLocalAudio(): MediaStream

    /**
     * Flag to mute or unmute the local audio stream (microphone).
     * Can be set before [initializeLocalAudio].
     */
    var isLocalAudioMuted: Boolean

    /**
     * Set's the [MediaStream] from the remote source.
     */
    suspend fun setRemoteStream(stream: MediaStream)

    /**
     * A flow which provides access to the [MediaStreamTrack] which represents the remote video.
     */
    val remoteVideoTrack: Flow<VideoTrack?>

    /**
     * Releases any held resources (local or remote streams).
     */
    fun release()
}

internal class MediaStreamManagerImpl(
    private val logger: Logger,
    private val getLocalAudioMediaStream: suspend () -> MediaStream = { MediaDevices.getUserMedia(audio = true) },
) : MediaStreamManager {

    // Local media stream (user's microphone)
    private var localStream: MediaStream? = null

    // Remote media stream (persona's audio/video)
    private val _remoteStream = MutableStateFlow<MediaStream?>(null)
    val remoteStream = _remoteStream.asStateFlow()

    override val remoteVideoTrack = _remoteStream
        .mapNotNull { stream -> stream?.tracks }
        .map { tracks -> tracks.firstOrNull { it.kind == MediaStreamTrackKind.Video } }
        .map { it as? VideoTrack }

    override suspend fun initializeLocalAudio(): MediaStream {
        logger.i(TAG) { "Initializing local audio stream..." }
        return try {
            val stream = getLocalAudioMediaStream()
            localStream = stream

            // Apply muted state if set before initialization
            if (isLocalAudioMuted) {
                logger.i(TAG) { "Applying pre-set mute state to local audio stream" }
                setLocalAudioTrackState(enabled = false)
            }

            logger.i(TAG) { "Local audio stream initialized successfully. Track count: ${stream.tracks.size}" }
            stream
        } catch (e: Exception) {
            logger.e(TAG) { "Failed to initialize local audio stream: ${e.message}" }
            throw MediaAccessException("Failed to access microphone: ${e.message}", e)
        }
    }

    override var isLocalAudioMuted: Boolean = false
        set(value) {
            field = value
            logger.i(TAG) { "Setting local audio mute state to: $value" }
            setLocalAudioTrackState(enabled = !value)
        }

    /**
     * Sets the remote media stream (from the persona).
     *
     * @param stream The remote [MediaStream] received from the peer.
     */
    override suspend fun setRemoteStream(stream: MediaStream) {
        logger.i(TAG) { "Setting remote stream. Track count: ${stream.tracks.size}" }

        _remoteStream.value?.release()
        _remoteStream.value = stream
    }

    override fun release() {
        logger.i(TAG) { "Releasing media streams..." }
        localStream?.release()
        localStream = null
    }

    /**
     * Disables all audio tracks associated with the local stream.
     */
    private fun setLocalAudioTrackState(enabled: Boolean) {
        localStream?.tracks
            ?.filter { it.kind == MediaStreamTrackKind.Audio }
            ?.forEach { it.enabled = enabled }
    }

    /**
     * Exception thrown when media access fails (e.g., microphone permission denied).
     */
    class MediaAccessException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

    private companion object {
        const val TAG = "MediaStreamManager"
    }
}
