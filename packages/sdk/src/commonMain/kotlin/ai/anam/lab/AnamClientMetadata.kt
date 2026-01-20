package ai.anam.lab

/**
 * This object provides access to SDK constants that can be used for metrics/metadata.
 */
internal object AnamClientMetadata {

    /**
     * The name of the Client SDK.
     */
    fun getName(): String = "kmp-sdk"

    /**
     * The current version of the Client SDK.
     */
    fun getVersion(): String = BuildConfig.VERSION
}
