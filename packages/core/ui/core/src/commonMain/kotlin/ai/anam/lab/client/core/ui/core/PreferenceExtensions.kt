package ai.anam.lab.client.core.ui.core

import ai.anam.lab.client.core.settings.Preference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

/**
 * Extension function to collect a [Preference] as a [State].
 */
@Composable
fun <T> Preference<T>.collectAsState() = flow.collectAsState(defaultValue)
