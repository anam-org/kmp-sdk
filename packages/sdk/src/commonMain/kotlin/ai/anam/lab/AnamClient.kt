package ai.anam.lab

import ai.anam.lab.api.ApiResult
import ai.anam.lab.api.ClientMetadata
import ai.anam.lab.api.SessionBody
import ai.anam.lab.api.SessionConfig
import ai.anam.lab.api.VoiceDetectionOptions
import ai.anam.lab.api.apiCall
import ai.anam.lab.api.buildApiHttpClient
import ai.anam.lab.api.buildMetricsHttpClient
import ai.anam.lab.api.buildWebSocketClient
import ai.anam.lab.api.cause
import ai.anam.lab.api.createSessionApi
import ai.anam.lab.api.message
import ai.anam.lab.metrics.ClientMetric
import ai.anam.lab.metrics.ClientTags
import ai.anam.lab.metrics.MetricsClient
import ai.anam.lab.metrics.MetricsClientImpl
import ai.anam.lab.metrics.MetricsContext
import ai.anam.lab.metrics.NoOpMetricsClient
import ai.anam.lab.utils.KermitLogger
import ai.anam.lab.utils.Logger
import ai.anam.lab.utils.cancellableRunCatching
import ai.anam.lab.webrtc.MediaStreamManagerImpl
import ai.anam.lab.webrtc.MessagingClientImpl
import ai.anam.lab.webrtc.ReasoningClientImpl
import ai.anam.lab.webrtc.SignallingClientImpl
import ai.anam.lab.webrtc.StreamingClientImpl
import ai.anam.lab.webrtc.ToolCallClientImpl
import com.appstractive.jwt.JWT
import com.appstractive.jwt.from
import de.jensklingenberg.ktorfit.Ktorfit
import kotlinx.coroutines.CoroutineScope

/**
 * The result from [AnamClient] when attempting to create a new [Session].
 */
public sealed interface SessionResult {

    /**
     * The [Session] was created successfully. It will not have been started until [Session.start] is called.
     */
    public data class Success(val session: Session) : SessionResult

    /**
     * An error occurred when attempting to create the new [Session]. The [message] provided is not suitable to display
     * to the user.
     */
    public data class Error(val message: String, val cause: Throwable? = null) : SessionResult
}

public class AnamClient(internal val options: AnamClientOptions) {

    // Check to see if the consumer has specified their own logger, otherwise we'll fall back to our own instance.
    internal val logger: Logger = options.logger ?: KermitLogger()

    /**
     * Create's a new Persona session, using the provided token (for authentication) and [SessionOptions].
     *
     * The returned [Session] will not be started until the [Session.start] method is called. The lifecycle of the
     * [Session] is tied to the associated [CoroutineScope].
     */
    public suspend fun createSession(
        sessionToken: String,
        sessionOptions: SessionOptions = SessionOptions(),
    ): SessionResult {
        logger.i(TAG) { "Creating new session..." }

        // Parse the JWT to verify that it's valid.
        val jwt = cancellableRunCatching { JWT.from(sessionToken) }.getOrNull()
            ?: return SessionResult.Error(message = "Invalid SessionToken")
        logger.i(TAG) { "Claims: ${jwt.claims}" }

        // When making requests to the Session API, we need to configure our HttpClient to support authentication (using
        // the provide token), as well as content negotiation. This is a different configuration to that later used by
        // our signalling infrastructure.
        val httpClient = buildApiHttpClient(sessionToken)

        // Build a metrics client with its own HttpClient (no auth, no expectSuccess) so that metric calls are
        // independent of the session API client lifecycle and credentials.
        val metricsClient = buildMetricsClient(sessionOptions)
        metricsClient.send(ClientMetric.SessionAttempt)

        val ktorfit = Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl(options.environment.baseUrl)
            .build()

        val sessionApi = ktorfit.createSessionApi()
        val result = apiCall {
            // Build our configuration for the Session.
            val options = SessionBody(
                clientMetadata = ClientMetadata(
                    client = AnamClientMetadata.getName(),
                    version = AnamClientMetadata.getVersion(),
                ),
                voiceDetection = sessionOptions.endOfSpeechSensitivity?.let {
                    VoiceDetectionOptions(endOfSpeechSensitivity = it)
                },
            )

            sessionApi.startSession(options)
        }

        return when (result) {
            is ApiResult.Success<SessionConfig> -> {
                val session = result.data
                logger.i(TAG) { "Session started successfully: $session" }
                metricsClient.updateContext(MetricsContext(sessionId = session.sessionId))

                val signallingClient = SignallingClientImpl(
                    config = result.data,
                    apiGateway = sessionOptions.apiGateway,
                    httpClient = buildWebSocketClient(),
                    logger = logger,
                )

                val mediaStreamManager = MediaStreamManagerImpl(logger = logger)
                val streamingClient = StreamingClientImpl(
                    config = result.data,
                    isLocalAudioEnabled = sessionOptions.isLocalAudioEnabled,
                    isStatsCollectionEnabled = !sessionOptions.isMetricsDisabled,
                    mediaStreamManager = mediaStreamManager,
                    signallingClient = signallingClient,
                    logger = logger,
                )

                // Return the newly configured Session. The caller will need to manually start it.
                SessionResult.Success(
                    Session(
                        id = result.data.sessionId,
                        signallingClient = signallingClient,
                        streamingClient = streamingClient,
                        mediaStreamManager = mediaStreamManager,
                        messagingClient = MessagingClientImpl(streamingClient, logger),
                        reasoningClient = ReasoningClientImpl(streamingClient, logger),
                        toolCallClient = ToolCallClientImpl(streamingClient.dataChannelMessages, logger),
                        sessionManager = createPlatformSessionManager(options.context, logger),
                        metricsClient = metricsClient,
                        logger = logger,
                    ),
                )
            }

            // Unfortunately, the API failed to create the require Session.
            is ApiResult.Error -> {
                logger.e(TAG) { "Failed to start session: $result" }
                metricsClient.send(ClientMetric.Error, tags = mapOf(ClientTags.ERROR to result.message))
                SessionResult.Error(
                    message = result.message,
                    cause = result.cause,
                )
            }
        }
    }

    private fun buildMetricsClient(sessionOptions: SessionOptions): MetricsClient {
        logger.i(TAG) { "Metrics ${if (sessionOptions.isMetricsDisabled) "disabled" else "enabled"}" }
        return if (sessionOptions.isMetricsDisabled) {
            NoOpMetricsClient
        } else {
            MetricsClientImpl(
                baseUrl = options.environment.baseUrl,
                apiGateway = sessionOptions.apiGateway,
                httpClient = buildMetricsHttpClient(),
                logger = logger,
            )
        }
    }

    internal companion object {
        const val TAG = "AnamClient"
    }
}
