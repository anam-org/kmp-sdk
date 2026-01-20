package ai.anam.lab.client.core.permissions

import androidx.compose.runtime.Composable

/**
 * On iOS, there isn't any additional work required.
 */
@Composable
actual fun BindEffect(permissionsManager: PermissionsManager) = Unit
