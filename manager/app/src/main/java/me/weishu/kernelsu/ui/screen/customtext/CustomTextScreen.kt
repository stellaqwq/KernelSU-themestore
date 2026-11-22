package me.weishu.kernelsu.ui.screen.customtext

import me.weishu.kernelsu.ui.util.customText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.SegmentedColumn
import me.weishu.kernelsu.ui.component.material.SegmentedListItem
import me.weishu.kernelsu.ui.component.material.TopBarBackButton
import me.weishu.kernelsu.ui.component.material.expressiveTopAppBarColors
import me.weishu.kernelsu.ui.util.CustomTextGroup
import me.weishu.kernelsu.ui.util.CustomTextKey
import me.weishu.kernelsu.ui.util.customTextSummary
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel

/**
 * Flagship-style editor for the interface texts a theme can rename.
 *
 * One list per [CustomTextGroup]; tapping a row opens a small dialog. An empty value is stored as
 * "no override", which is also what the "restore default" button does, so every row can always fall
 * back to the packaged string.
 */
@Composable
fun CustomTextScreen(onBack: () -> Unit) {
    val viewModel = viewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var editingKey by remember { mutableStateOf<CustomTextKey?>(null) }

    ExpressiveScaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                navigationIcon = { TopBarBackButton(onClick = onBack) },
                title = { Text(customText(CustomTextKey.PAGE_CUSTOM_TEXTS)) },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            CustomTextGroup.entries.forEach { group ->
                val keys = CustomTextKey.entries.filter { it.group == group }
                if (keys.isEmpty()) return@forEach
                item(key = "header_${group.name}") {
                    Text(
                        text = stringResource(group.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 32.dp, end = 16.dp),
                    )
                }
                item(key = "group_${group.name}") {
                    SegmentedColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        content = buildList {
                            keys.forEach { key ->
                                add {
                                    SegmentedListItem(
                                        onClick = { editingKey = key },
                                        headlineContent = {
                                            Text(
                                                text = stringResource(key.nameRes),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                text = customTextSummary(key, uiState.customTexts[key.key]),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                imageVector = Icons.Filled.TextFields,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        },
                                        trailingContent = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = null,
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }

            item(key = "reset_all") {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = buildList {
                        add {
                            SegmentedListItem(
                                onClick = viewModel::resetCustomTexts,
                                headlineContent = { Text(stringResource(R.string.custom_text_restore_all)) },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Filled.RestartAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    headlineColor = MaterialTheme.colorScheme.error,
                                ),
                            )
                        }
                    }
                )
            }

            item(key = "bottom") {
                val navBars = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                Text(
                    text = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = navBars + 12.dp),
                )
            }
        }
    }

    editingKey?.let { key ->
        CustomTextEditDialog(
            key = key,
            current = uiState.customTexts[key.key].orEmpty(),
            onDismiss = { editingKey = null },
            onReset = {
                viewModel.setCustomText(key.key, "")
                editingKey = null
            },
            onConfirm = { value ->
                viewModel.setCustomText(key.key, value)
                editingKey = null
            },
        )
    }
}

@Composable
private fun CustomTextEditDialog(
    key: CustomTextKey,
    current: String,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(key, current) { mutableStateOf(current) }
    val name = stringResource(key.nameRes)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_text_edit_title, name)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(customTextSummary(key, null)) },
                supportingText = { Text(stringResource(R.string.custom_text_edit_hint)) },
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text(stringResource(R.string.custom_text_restore))
            }
        },
    )
}
