package ai.anam.lab

/**
 * This class represents a single message, either from the User or Persona.
 */
public data class Message(
    val id: String,
    val content: String,
    val role: MessageRole,
    val endOfSpeech: Boolean,
    val interrupted: Boolean,
    val correlationId: String? = null,

    /**
     * Since a Message with the same [id] may be mutated, due to additional [content] being added, the [version]
     * property can be used to track whether something has changed.
     */
    val version: Int = 1,
)

/**
 * The role associated with a [Message].
 */
public enum class MessageRole {
    User,
    Persona,
    ;

    internal companion object
}
