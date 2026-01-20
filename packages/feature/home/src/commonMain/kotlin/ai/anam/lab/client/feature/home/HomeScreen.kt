package ai.anam.lab.client.feature.home

import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.app_name
import ai.anam.lab.client.core.ui.resources.generated.resources.settings_content_description
import ai.anam.lab.client.core.viewmodel.metroViewModel
import ai.anam.lab.client.feature.avatars.AvatarsView
import ai.anam.lab.client.feature.llms.LlmsView
import ai.anam.lab.client.feature.messages.MessagesView
import ai.anam.lab.client.feature.session.SessionView
import ai.anam.lab.client.feature.voices.VoicesView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: HomeViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    HomeScreen(
        viewState = viewState,
        onTabSelect = viewModel::selectTab,
        onSettingsClick = viewModel::selectSettings,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewState: HomeViewState,
    onTabSelect: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(Res.string.app_name),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.align(Alignment.Center),
                        )

                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.align(Alignment.CenterEnd),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(Res.string.settings_content_description),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        HomeView(
            viewState = viewState,
            onTabSelect = onTabSelect,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
fun HomeView(viewState: HomeViewState, onTabSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SessionView(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary),
        )

        SecondaryScrollableTabRow(
            selectedTabIndex = viewState.selectedIndex,
            modifier = Modifier.fillMaxWidth(),
        ) {
            viewState.tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = viewState.selectedIndex == index,
                    onClick = { onTabSelect(index) },
                    modifier = Modifier.padding(8.dp),
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(tab.name),
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                    },
                )
            }
        }

        Box(modifier = Modifier.padding(top = 16.dp)) {
            val selected = viewState.tabs[viewState.selectedIndex]
            when (selected) {
                Tab.Avatar -> AvatarsView(modifier = Modifier.fillMaxWidth())
                Tab.Messages -> MessagesView(modifier = Modifier.fillMaxWidth())
                Tab.Voices -> VoicesView(modifier = Modifier.fillMaxWidth())
                Tab.Llms -> LlmsView(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
