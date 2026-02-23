package ai.anam.lab.client.core.ui.core

import androidx.compose.runtime.Composable

/**
 * No-op on iOS. In landscape fullscreen mode, Compose content fills the available
 * space via layout modifiers, and the status bar is not visible in landscape on
 * most iOS devices.
 */
@Composable
actual fun ImmersiveMode(enabled: Boolean) = Unit
