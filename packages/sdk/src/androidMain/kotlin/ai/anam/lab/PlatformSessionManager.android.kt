package ai.anam.lab

import ai.anam.lab.utils.Logger
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.awaitCancellation

internal actual fun createPlatformSessionManager(context: PlatformContext, logger: Logger): PlatformSessionManager {
    return PlatformSessionManagerImpl(
        context = context,
        logger = logger,
    )
}

/**
 * This Android specific [PlatformSessionManager] is responsible for managing any platform specific integrations during
 * the lifetime of the [Session]. For example, since the session is effectively making a VOIP call, we need to obtain
 * the relevant audio focus via Android's `AudioManager`.
 */
internal class PlatformSessionManagerImpl(private val context: Context, private val logger: Logger) :
    PlatformSessionManager {

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override suspend fun start() {
        logger.i(TAG) { "Starting..." }

        // Flag to track whether the speakerphone was enabled when we started. This is only used for older API versions
        // (pre-S) where we manually toggle the speakerphone.
        var originalSpeakerphoneOn = false

        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            originalSpeakerphoneOn = enableSpeakerphone()

            // Request, and hold, audio focus until we are cancelled.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                holdAudioFocusApi26()
            } else {
                holdAudioFocus()
            }
        } finally {
            restoreSpeakerphone(originalSpeakerphoneOn)
            audioManager.mode = AudioManager.MODE_NORMAL
        }
    }

    /**
     * Attempts to route audio to the built-in speaker, but only if a headset is not currently connected.
     *
     * @return The original state of the speakerphone (if applicable), or false.
     */
    private fun enableSpeakerphone(): Boolean {
        if (hasHeadset()) {
            logger.i(TAG) { "Headset detected, skipping speakerphone." }
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager.availableCommunicationDevices
            val speaker = devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }

            if (speaker != null) {
                val result = audioManager.setCommunicationDevice(speaker)
                logger.i(TAG) { "setCommunicationDevice(speaker): $result" }
            } else {
                logger.w(TAG) { "No built-in speaker found." }
            }
            return false
        } else {
            @Suppress("DEPRECATION")
            val original = audioManager.isSpeakerphoneOn
            if (!original) {
                logger.i(TAG) { "Enabling speakerphone..." }
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = true
            }
            return original
        }
    }

    /**
     * Checks if a headset is connected (wired or bluetooth).
     */
    private fun hasHeadset(): Boolean {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        }
    }

    /**
     * Restores the audio routing to its previous state.
     *
     * @param originalSpeakerphoneOn The original state of the speakerphone (returned by [enableSpeakerphone]).
     */
    private fun restoreSpeakerphone(originalSpeakerphoneOn: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            logger.i(TAG) { "Clearing communication device..." }
            audioManager.clearCommunicationDevice()
        } else {
            @Suppress("DEPRECATION")
            if (!originalSpeakerphoneOn) {
                logger.i(TAG) { "Disabling speakerphone..." }
                audioManager.isSpeakerphoneOn = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun holdAudioFocusApi26() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(false)
            .build()

        try {
            val result = audioManager.requestAudioFocus(focusRequest)
            logger.i(TAG) { "Audio focus requested: $result" }

            awaitCancellation()
        } finally {
            logger.i(TAG) { "Abandoning audio focus" }
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun holdAudioFocus() {
        try {
            val result = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN,
            )
            logger.i(TAG) { "Audio focus requested: $result" }

            awaitCancellation()
        } finally {
            logger.i(TAG) { "Abandoning audio focus" }
            audioManager.abandonAudioFocus(null)
        }
    }

    private companion object {
        const val TAG = "PlatformSessionManager"
    }
}
