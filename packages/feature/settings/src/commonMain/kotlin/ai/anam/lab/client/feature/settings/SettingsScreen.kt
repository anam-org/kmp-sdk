package ai.anam.lab.client.feature.settings

import ai.anam.lab.client.core.navigation.SharedTransitionKeys
import ai.anam.lab.client.core.navigation.sharedBoundsIfAvailable
import ai.anam.lab.client.core.settings.Theme
import ai.anam.lab.client.core.ui.components.ApiKeyDescription
import ai.anam.lab.client.core.ui.components.Preference
import ai.anam.lab.client.core.ui.components.PreferenceDivider
import ai.anam.lab.client.core.ui.components.PreferenceHeader
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.app_name
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_api_key_dialog_cancel
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_api_key_dialog_hint
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_api_key_dialog_save
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_api_key_summary_empty
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_api_key_title
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_header_api
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_header_ui
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_licenses_summary
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_licenses_title
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_theme_title
import ai.anam.lab.client.core.viewmodel.metroViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    SettingsScreen(
        viewState = viewState,
        onNavigateBack = viewModel::navigateBack,
        onThemeSelect = viewModel::selectTheme,
        onLicensesSelect = viewModel::selectLicenses,
        onApiKeyClick = viewModel::showApiKeyDialog,
        onApiKeyDialogDismiss = viewModel::dismissApiKeyDialog,
        onApiKeySave = viewModel::saveApiKey,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewState: SettingsViewState,
    onNavigateBack: () -> Unit,
    onThemeSelect: (Theme) -> Unit,
    onLicensesSelect: () -> Unit,
    onApiKeyClick: () -> Unit,
    onApiKeyDialogDismiss: () -> Unit,
    onApiKeySave: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                modifier = Modifier.sharedBoundsIfAvailable(key = SharedTransitionKeys.TOP_BAR),
                title = {
                    Text(
                        text = stringResource(Res.string.app_name),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            stickyHeader {
                PreferenceHeader(stringResource(Res.string.settings_header_ui))
            }

            item {
                ThemePreference(
                    title = stringResource(Res.string.settings_theme_title),
                    selected = viewState.theme,
                    onThemeSelect = onThemeSelect,
                )
            }

            item { PreferenceDivider() }

            stickyHeader {
                PreferenceHeader(stringResource(Res.string.settings_header_api))
            }

            item {
                Preference(
                    title = stringResource(Res.string.settings_api_key_title),
                    summary = {
                        Text(
                            viewState.displayApiKey
                                ?: stringResource(Res.string.settings_api_key_summary_empty),
                        )
                    },
                    onClick = onApiKeyClick,
                )
            }

            item { PreferenceDivider() }

            item {
                Preference(
                    title = stringResource(Res.string.settings_licenses_title),
                    summary = {
                        Text(stringResource(Res.string.settings_licenses_summary))
                    },
                    onClick = onLicensesSelect,
                )
            }
        }
    }

    if (viewState.showApiKeyDialog) {
        ApiKeyDialog(
            initialKey = viewState.apiKey,
            onDismiss = onApiKeyDialogDismiss,
            onSave = onApiKeySave,
        )
    }
}

@Composable
private fun ApiKeyDialog(initialKey: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember(initialKey) { mutableStateOf(initialKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.settings_api_key_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column {
                ApiKeyDescription()

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(Res.string.settings_api_key_dialog_hint)) },
                    singleLine = true,
                    trailingIcon = {
                        if (text.isNotEmpty()) {
                            IconButton(onClick = { text = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text(stringResource(Res.string.settings_api_key_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.settings_api_key_dialog_cancel))
            }
        },
    )
}

@Composable
private fun ThemePreference(
    selected: Theme,
    onThemeSelect: (Theme) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
) {
    Preference(
        title = title,
        control = {
            Row(Modifier.selectableGroup()) {
                ThemeButton(
                    icon = Icons.Default.AutoMode,
                    onClick = { onThemeSelect(Theme.SYSTEM) },
                    isSelected = selected == Theme.SYSTEM,
                )

                ThemeButton(
                    icon = Icons.Default.LightMode,
                    onClick = { onThemeSelect(Theme.LIGHT) },
                    isSelected = selected == Theme.LIGHT,
                )

                ThemeButton(
                    icon = Icons.Default.DarkMode,
                    onClick = { onThemeSelect(Theme.DARK) },
                    isSelected = selected == Theme.DARK,
                )
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ThemeButton(isSelected: Boolean, icon: ImageVector, onClick: () -> Unit) {
    FilledIconToggleButton(
        checked = isSelected,
        onCheckedChange = { onClick() },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
    }
}
