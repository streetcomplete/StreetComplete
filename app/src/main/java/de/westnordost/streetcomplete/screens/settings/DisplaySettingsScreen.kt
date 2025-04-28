package de.westnordost.streetcomplete.screens.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadWorker
import de.westnordost.streetcomplete.data.importGpx
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleListPickerDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference
import de.westnordost.streetcomplete.util.ktx.getActivity
import de.westnordost.streetcomplete.util.ktx.toast
import io.ticofab.androidgpxparser.parser.GPXParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.File
import java.io.IOException

@Composable
fun DisplaySettingsScreen(
    onClickBack: () -> Unit,
) {
    val visibleEditTypeController: VisibleEditTypeController = koinInject()
    val prefs: Preferences = koinInject()
    val scope = rememberCoroutineScope()
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showGpxDialog by remember { mutableStateOf(false) }
    var showGeometryDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.pref_screen_display)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    )
                )
        ) {
            SwitchPreference(
                name = stringResource(R.string.pref_way_direction),
                description = stringResource(R.string.pref_way_direction_summary),
                default = false,
                pref = Prefs.SHOW_WAY_DIRECTION
            )
            SwitchPreference(
                name = stringResource(R.string.pref_quest_geometries_title),
                description = stringResource(R.string.pref_quest_geometries_summary),
                default = false,
                pref = Prefs.QUEST_GEOMETRIES,
                onCheckedChange = { visibleEditTypeController.onVisibilitiesChanged() }
            )
            SwitchPreference(
                name = stringResource(R.string.pref_offset_fix_title2),
                description = stringResource(R.string.pref_offset_fix_summary),
                default = false,
                pref = Prefs.OFFSET_FIX,
                onCheckedChange = {
                    // trigger map update by switching background twice
                    val old = prefs.getString(Prefs.THEME_BACKGROUND, "MAP")
                    val new = if (old == "MAP") "AERIAL" else "MAP"
                    prefs.putString(Prefs.THEME_BACKGROUND, new)
                    scope.launch {
                        delay(100)
                        prefs.putString(Prefs.THEME_BACKGROUND, old)
                    }
                }
            )
            SwitchPreference(
                name = stringResource(R.string.pref_show_solved_animation),
                description = stringResource(R.string.pref_show_solved_animation_summary),
                default = true,
                pref = Prefs.SHOW_SOLVED_ANIMATION
            )
            Preference(
                name = stringResource(R.string.pref_background_type_select),
                description = if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") == "MAP")
                        stringResource(R.string.background_type_map)
                    else stringResource(R.string.background_type_aerial_esri),
                onClick = { showBackgroundDialog = true }
            )
            Preference(
                name = stringResource(R.string.pref_gpx_track_title),
                onClick = { showGpxDialog = true },
            )
            Preference(
                name = stringResource(R.string.pref_custom_geometry_title),
                onClick = { showGeometryDialog = true },
            )
            if (showBackgroundDialog)
                SimpleListPickerDialog(
                    onDismissRequest = { showBackgroundDialog = false },
                    items = listOf("MAP", "AERIAL"),
                    onItemSelected = { prefs.putString(Prefs.THEME_BACKGROUND, it) },
                    getItemName = {
                        if (it == "MAP") stringResource(R.string.background_type_map)
                        else stringResource(R.string.background_type_aerial_esri)
                    },
                    selectedItem = prefs.getString(Prefs.THEME_BACKGROUND, "MAP")
                )
            if (showGpxDialog) {
                val ctx = LocalContext.current
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    val uri = it.data?.data
                    if (it.resultCode != Activity.RESULT_OK || uri == null) {
                        ctx.toast(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
                        return@rememberLauncherForActivityResult
                    }
                    ctx.getActivity()?.contentResolver?.query(uri, null, null, null, null).use {
                        if (it != null && it.moveToFirst()) {
                            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (idx >= 0 && !it.getString(idx).endsWith(".gpx")) {
                                ctx.toast(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
                                return@rememberLauncherForActivityResult
                            }
                        }
                    }
                    try {
                        ctx.getActivity()?.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { reader ->
                            File(ctx.getExternalFilesDir(null), GPX_TRACK_FILE).writeText(reader.readText())
                        } }
                        gpx_track_changed = true
                        showGpxDialog = false
                        showGpxDialog = true
                    } catch (e: IOException) {
                        ctx.toast(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
                    }
                }
                val gpxFileExists = ctx.getExternalFilesDir(null)?.let { File(it, GPX_TRACK_FILE) }?.exists() == true
                val downloadController: DownloadController = koinInject()
                AlertDialog(
                    onDismissRequest = { showGpxDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showGpxDialog = false }) { Text(stringResource(R.string.close)) }
                    },
                    title = { Text(stringResource(R.string.pref_gpx_track_title))},
                    text = {
                        Column {
                            Button(
                                onClick = {
                                    val points = loadGpxTrackPoints(ctx, true) ?: return@Button
                                    GlobalScope.launch {
                                        val import = importGpx(points, true, 10.0).getOrNull()
                                        import?.downloadBBoxes?.let {
                                            if (it.isEmpty()) return@launch
                                            DownloadWorker.enqueuedDownloads.addAll(it.drop(1))
                                            downloadController.download(it.first(), false, true)
                                        }
                                    }
                                },
                                enabled = gpxFileExists
                            ) { Text(stringResource(R.string.pref_gpx_track_download), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        // actually the type should be application/gpx+xml, but often doesn't work
                                        // for some phones only application/octet-stream works, for others it doesn't, so just allow everything
                                        type = "*/*"
                                    }
                                    launcher.launch(intent)
                                }
                            ) { Text(stringResource(R.string.pref_gpx_track_provide), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                            if (gpxFileExists)
                                SwitchPreference(
                                    name = stringResource(R.string.pref_gpx_track_enable),
                                    default = false,
                                    pref = Prefs.SHOW_GPX_TRACK,
                                )
                        }
                    },
                )
            }
            if (showGeometryDialog) {
                val ctx = LocalContext.current
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    val uri = it.data?.data
                    if (it.resultCode != Activity.RESULT_OK || uri == null) {
                        ctx.toast(R.string.file_loading_error, Toast.LENGTH_LONG)
                        return@rememberLauncherForActivityResult
                    }
                    try {
                        ctx.getActivity()?.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { reader ->
                            File(ctx.getExternalFilesDir(null), CUSTOM_GEOMETRY_FILE).writeText(reader.readText())
                        } }
                        custom_geometry_changed = true
                        showGeometryDialog = false
                        showGeometryDialog = true
                    } catch (e: IOException) {
                        ctx.toast(R.string.file_loading_error, Toast.LENGTH_LONG)
                    }
                }
                val fileExists = ctx.getExternalFilesDir(null)?.let { File(it, CUSTOM_GEOMETRY_FILE) }?.exists() == true
                AlertDialog(
                    onDismissRequest = { showGeometryDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showGeometryDialog = false }) { Text(stringResource(R.string.close)) }
                    },
                    title = { Text(stringResource(R.string.pref_custom_geometry_title))},
                    text = {
                        Column {
                            Text(stringResource(R.string.pref_custom_geometry_info))
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "*/*"
                                    }
                                    launcher.launch(intent)
                                }
                            ) { Text(stringResource(R.string.file_provide), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                            if (fileExists)
                                SwitchPreference(
                                    name = stringResource(R.string.quest_enabled),
                                    default = false,
                                    pref = Prefs.SHOW_CUSTOM_GEOMETRY,
                                )
                        }
                    },
                )
            }
        }
    }
}

fun loadGpxTrackPoints(context: Context, complain: Boolean = false): List<LatLon>? {
    // load gpx file as one long track, no matter how it's stored internally (for now)
    // <trkpt lat="..." lon="..."><ele>...</ele></trkpt>
    // <wpt lon="..." lat="...">
    val gpxFile = context.getExternalFilesDir(null)?.let { File(it, GPX_TRACK_FILE) }
    if (gpxFile?.exists() != true) {
        if (complain)
            context.toast(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
        return null
    }

    val gpxPoints = runCatching {
        GPXParser().parse(gpxFile.inputStream()).tracks.map { track ->
            track.trackSegments.map { segment ->
                segment.trackPoints
            }
        }.flatten().flatten()
            .map { trackPoint ->
                LatLon(
                    latitude = trackPoint.latitude,
                    longitude = trackPoint.longitude
                )
            }
    }.getOrNull()

    if ((gpxPoints?.size ?: 0) < 2) {
        context.toast(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
        return null
    }
    return gpxPoints
}

fun loadCustomGeometryText(context: Context): String? {
    val file = context.getExternalFilesDir(null)?.let { File(it, CUSTOM_GEOMETRY_FILE) }
    if (file?.exists() != true) return null
    return file.readText()
}

private const val GPX_TRACK_FILE = "display_track.gpx"
private const val CUSTOM_GEOMETRY_FILE = "customGeometry.geojson"

var gpx_track_changed = false
var custom_geometry_changed = false
