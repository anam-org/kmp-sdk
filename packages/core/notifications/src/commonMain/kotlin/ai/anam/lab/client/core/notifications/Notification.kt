package ai.anam.lab.client.core.notifications

/**
 * Represents a notification that can be displayed to the user.
 * Supports multiple types (Error, Warning, Info, Success, Confirmation).
 * Error notifications use error codes that map to localized string resources,
 * with an optional custom message override.
 * Confirmation notifications require explicit user acknowledgment before executing an action.
 */
sealed class Notification {
    abstract val message: String

    data class Error(val errorCode: ErrorCode, val customMessage: String? = null) : Notification() {
        override val message: String = customMessage ?: ""
    }

    data class Warning(override val message: String) : Notification()

    data class Info(override val message: String) : Notification()

    data class Success(override val message: String) : Notification()

    class Confirmation(
        override val message: String,
        val confirmLabel: String,
        val dismissLabel: String,
        val onConfirm: suspend () -> Unit,
    ) : Notification()
}
