package ai.anam.lab.client.core.permissions

import androidx.compose.runtime.Composable

/**
 * On wasmJs, there is no Activity lifecycle to bind to.
 */
@Composable
actual fun BindEffect(permissionsManager: PermissionsManager) = Unit
