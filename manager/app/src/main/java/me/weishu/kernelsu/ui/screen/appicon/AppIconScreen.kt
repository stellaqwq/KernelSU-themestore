package me.weishu.kernelsu.ui.screen.appicon

import me.weishu.kernelsu.ui.util.customText
import me.weishu.kernelsu.ui.util.CustomTextKey
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.TonalCard
import me.weishu.kernelsu.ui.component.material.TopBarBackButton
import me.weishu.kernelsu.ui.component.material.expressiveTopAppBarColors
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.util.AppIconEntry
import me.weishu.kernelsu.ui.util.AppIconPack
import me.weishu.kernelsu.ui.util.AppIconPackStorage
import me.weishu.kernelsu.ui.util.BuiltinAppIcon
import me.weishu.kernelsu.ui.util.BuiltinAppIcons
import me.weishu.kernelsu.ui.util.module.Shortcut
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel

@Immutable
data class AppIconUiState(
    val selectedBuiltinId: String,
    val dynamicColor: Boolean,
    val packs: List<AppIconPack> = emptyList(),
    val loading: Boolean = true,
    val importing: Boolean = false,
    val pendingDelete: AppIconPack? = null,
)

@Immutable
data class AppIconActions(
    val onBack: () -> Unit,
    val onSelectBuiltin: (BuiltinAppIcon) -> Unit,
    val onSetDynamicColor: (Boolean) -> Unit,
    val onImportPack: () -> Unit,
    val onPinCustomIcon: (AppIconPack, AppIconEntry) -> Unit,
    val onRequestDeletePack: (AppIconPack) -> Unit,
    val onDismissDeletePack: () -> Unit,
    val onConfirmDeletePack: (AppIconPack) -> Unit,
)

@Composable
fun AppIconScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val viewModel = viewModel<SettingsViewModel>()
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var packs by remember { mutableStateOf<List<AppIconPack>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var importing by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<AppIconPack?>(null) }

    suspend fun reloadPacks() {
        packs = AppIconPackStorage.load(context)
        loading = false
    }

    LaunchedEffect(Unit) { reloadPacks() }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            importing = true
            scope.launch {
                try {
                    AppIconPackStorage.import(context, uri)
                        .onSuccess { count ->
                            Toast.makeText(
                                context,
                                context.getString(R.string.app_icon_import_success, count),
                                Toast.LENGTH_SHORT,
                            ).show()
                            reloadPacks()
                        }
                        .onFailure {
                            Toast.makeText(
                                context,
                                R.string.app_icon_import_failed,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                } finally {
                    importing = false
                }
            }
        }
    }

    val state = AppIconUiState(
        selectedBuiltinId = settings.appIconId,
        dynamicColor = settings.appIconDynamicColor,
        packs = packs,
        loading = loading,
        importing = importing,
        pendingDelete = pendingDelete,
    )
    val actions = AppIconActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onSelectBuiltin = { icon ->
            // Re-enabling the active alias can restart the process on some ROMs.
            if (icon.id != settings.appIconId) {
                viewModel.setAppIcon(context, icon.id)
                Toast.makeText(context, R.string.app_icon_switch_hint, Toast.LENGTH_SHORT).show()
            }
        },
        onSetDynamicColor = { viewModel.setAppIconDynamicColor(context, it) },
        onImportPack = {
            if (!importing) {
                importLauncher.launch(
                    arrayOf(
                        "application/zip",
                        "application/x-zip",
                        "application/x-zip-compressed",
                        "application/octet-stream",
                    )
                )
            }
        },
        onPinCustomIcon = { pack, entry ->
            scope.launch {
                val bitmap = AppIconPackStorage.loadBitmap(context, pack, entry)
                if (bitmap == null) {
                    Toast.makeText(context, R.string.app_icon_pin_failed, Toast.LENGTH_SHORT).show()
                } else {
                    Shortcut.createAppIconShortcut(
                        context = context,
                        key = "${pack.id}_${entry.id}",
                        label = context.getString(R.string.app_name),
                        bitmap = bitmap,
                    )
                }
            }
        },
        onRequestDeletePack = { pendingDelete = it },
        onDismissDeletePack = { pendingDelete = null },
        onConfirmDeletePack = { pack ->
            pendingDelete = null
            scope.launch {
                AppIconPackStorage.delete(context, pack.id)
                    .onSuccess { reloadPacks() }
                    .onFailure {
                        Toast.makeText(
                            context,
                            R.string.app_icon_delete_pack_failed,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
            }
        },
    )

    AppIconScreenContent(state = state, actions = actions)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppIconScreenContent(
    state: AppIconUiState,
    actions: AppIconActions,
) {
    val deleteDialog = rememberConfirmDialog(onConfirm = {
        state.pendingDelete?.let(actions.onConfirmDeletePack)
    }, onDismiss = actions.onDismissDeletePack)
    // LaunchedEffect's body is a suspend lambda, not a composable one: resolve the strings here.
    val deleteTitle = stringResource(R.string.app_icon_delete_pack_title)
    val deleteMessage = state.pendingDelete?.let {
        stringResource(R.string.app_icon_delete_pack_message, it.name)
    }.orEmpty()
    LaunchedEffect(state.pendingDelete) {
        if (state.pendingDelete == null) return@LaunchedEffect
        deleteDialog.showConfirm(title = deleteTitle, content = deleteMessage)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    ExpressiveScaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                navigationIcon = { TopBarBackButton(onClick = actions.onBack) },
                title = { Text(customText(CustomTextKey.PAGE_APP_ICON)) },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.app_icon_builtin_section),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BuiltinAppIcons.all.forEach { icon ->
                                val selected = icon.id == state.selectedBuiltinId
                                Column(
                                    modifier = Modifier
                                        .width(84.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (selected) {
                                                MaterialTheme.colorScheme.secondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainer
                                            }
                                        )
                                        .clickable { actions.onSelectBuiltin(icon) }
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(modifier = Modifier.size(56.dp)) {
                                        Image(
                                            painter = painterResource(id = icon.iconRes),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                        )
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = stringResource(R.string.app_icon_selected),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .align(Alignment.BottomEnd),
                                            )
                                        }
                                    }
                                    Text(
                                        text = stringResource(id = icon.nameRes),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.app_icon_dynamic_color_title),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = stringResource(R.string.app_icon_dynamic_color_summary),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = state.dynamicColor,
                                onCheckedChange = actions.onSetDynamicColor,
                            )
                        }
                    }
                }
            }

            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.app_icon_import_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.app_icon_import_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = actions.onImportPack,
                            enabled = !state.importing && !state.loading,
                        ) {
                            Icon(Icons.Rounded.FileUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(
                                    if (state.importing) {
                                        R.string.app_icon_importing
                                    } else {
                                        R.string.app_icon_import_button
                                    }
                                )
                            )
                        }
                        Text(
                            text = stringResource(R.string.app_icon_custom_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (state.packs.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.app_icon_empty_custom_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }

            state.packs.forEach { pack ->
                item(key = pack.id) {
                    IconPackCard(
                        pack = pack,
                        onDelete = { actions.onRequestDeletePack(pack) },
                        onPin = { entry -> actions.onPinCustomIcon(pack, entry) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun IconPackCard(
    pack: AppIconPack,
    onDelete: () -> Unit,
    onPin: (AppIconEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    TonalCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.app_icon_delete_pack_title),
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(pack.icons, key = { it.id }) { entry ->
                    Column(
                        modifier = Modifier
                            .width(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPin(entry) }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(modifier = Modifier.size(64.dp)) {
                            AsyncImage(
                                model = AppIconPackStorage.iconFile(context, pack, entry),
                                contentDescription = entry.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.AddToHomeScreen,
                                contentDescription = stringResource(R.string.app_icon_pin_badge),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.BottomEnd),
                            )
                        }
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
