package ai.anam.lab.fakes

import ai.anam.lab.webrtc.MediaStreamManager
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.VideoTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fake implementation of [MediaStreamManager] designed for unit tests. Allows tests to manually set values to the flows
 * for testing purposes.
 */
internal class FakeMediaStreamManager : MediaStreamManager {
    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    override val remoteVideoTrack: Flow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    override var isLocalAudioMuted: Boolean = false

    /**
     * Set the remote video track. This allows tests to simulate a video track being available.
     */
    fun setRemoteVideoTrack(track: VideoTrack?) {
        _remoteVideoTrack.value = track
    }

    override suspend fun initializeLocalAudio(): MediaStream {
        // No-Op for testing
        throw UnsupportedOperationException("Not implemented in fake")
    }

    override suspend fun setRemoteStream(stream: MediaStream) {
        // No-Op for testing
    }

    internal var isReleased = false

    override fun release() {
        isReleased = true
    }
}
