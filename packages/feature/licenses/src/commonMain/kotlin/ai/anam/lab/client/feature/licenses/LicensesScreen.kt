package ai.anam.lab.client.feature.licenses

import ai.anam.lab.client.core.navigation.SharedTransitionKeys
import ai.anam.lab.client.core.navigation.sharedBoundsIfAvailable
import ai.anam.lab.client.core.ui.components.Preference
import ai.anam.lab.client.core.ui.components.PreferenceHeader
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.license_artifact_version_format
import ai.anam.lab.client.core.ui.resources.generated.resources.licenses_title
import ai.anam.lab.client.core.viewmodel.metroViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

@Composable
fun LicensesScreen(modifier: Modifier = Modifier, viewModel: LicensesViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    LicensesScreen(
        viewState = viewState,
        onNavigateBack = viewModel::navigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(viewState: LicensesViewState, onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                modifier = Modifier.sharedBoundsIfAvailable(key = SharedTransitionKeys.TOP_BAR),
                title = {
                    Text(
                        text = stringResource(Res.string.licenses_title),
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
        val uriHandler = LocalUriHandler.current

        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            viewState.licenses.forEach { group ->
                stickyHeader {
                    PreferenceHeader(
                        title = group.id,
                        modifier = Modifier.fillMaxSize(),
                        tonalElevation = 1.dp,
                    )
                }

                items(group.artifacts) { artifact ->
                    val linkUrl = artifact.scm?.url
                    Preference(
                        title = (artifact.name ?: artifact.artifactId),
                        summary = {
                            Column {
                                Text(
                                    stringResource(
                                        Res.string.license_artifact_version_format,
                                        artifact.artifactId,
                                        artifact.version,
                                    ),
                                )

                                artifact.spdxLicenses?.forEach { license ->
                                    Text(license.name)
                                }
                            }
                        },
                        onClick = { linkUrl?.let { uriHandler.openUri(it) } },
                    )
                }
            }
        }
    }
}
