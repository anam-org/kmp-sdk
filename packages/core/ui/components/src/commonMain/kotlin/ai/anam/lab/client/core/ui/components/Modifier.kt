package ai.anam.lab.client.core.ui.components

import androidx.compose.ui.Modifier

/**
 * Extension function to conditionally mutate a [Modifier] when a condition is true.
 */
inline fun Modifier.thenIf(
    condition: Boolean,
    whenFalse: Modifier.() -> Modifier = { this },
    whenTrue: Modifier.() -> Modifier,
): Modifier = if (condition) whenTrue() else whenFalse()
