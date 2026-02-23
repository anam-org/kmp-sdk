package ai.anam.lab.client.core.ui.core

import androidx.compose.runtime.Composable

/**
 * Hides system UI (status bar, navigation bar) for an immersive fullscreen experience.
 *
 * When [enabled] is `true`, system bars are hidden. When `false` or when the composable
 * leaves the composition, system bars are restored.
 *
 * - **Android**: uses [WindowInsetsControllerCompat][androidx.core.view.WindowInsetsControllerCompat]
 *   with swipe-to-reveal behavior.
 * - **iOS**: no-op — the status bar is not visible in landscape on most devices.
 * - **wasmJs**: no-op.
 */
@Composable
expect fun ImmersiveMode(enabled: Boolean)
