package ai.anam.lab

import ai.anam.lab.utils.Logger

/**
 * The options for the [AnamClient].
 */
public data class AnamClientOptions(
    val context: PlatformContext,
    val environment: Environment = Environment.Production,
    val logger: Logger? = null,
)

/**
 * Represents a platform-specific context that acts as an interface to global information about an application
 * environment.
 */
public expect abstract class PlatformContext

/**
 * This type defines which [Environment] to use for the [AnamClient]. In normal scenarios, this will be
 * [Environment.Production]. However, for local testing, it's possible to specify a custom base url.
 */
public sealed interface Environment {
    public val baseUrl: String

    /**
     * The Production [Environment].
     */
    public data object Production : Environment {
        override val baseUrl: String = "https://api.anam.ai/"
    }

    /**
     * A custom [Environment] that can be used for test purposes.
     */
    public data class Custom(public override val baseUrl: String) : Environment
}
