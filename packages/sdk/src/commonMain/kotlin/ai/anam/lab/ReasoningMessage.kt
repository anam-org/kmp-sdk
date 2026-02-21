package ai.anam.lab

/**
 * This class represents a single reasoning message from the Persona's chain-of-thought.
 */
public data class ReasoningMessage(
    val id: String,
    val content: String,
    val role: MessageRole,
    val endOfThought: Boolean,

    /**
     * Since a ReasoningMessage with the same [id] may be mutated, due to additional [content] being added, the [version]
     * property can be used to track whether something has changed.
     */
    val version: Int = 1,
)
