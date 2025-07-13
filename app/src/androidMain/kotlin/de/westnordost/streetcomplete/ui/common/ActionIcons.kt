package de.westnordost.streetcomplete.ui.common

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_back_24
import de.westnordost.streetcomplete.resources.ic_camera_measure_24
import de.westnordost.streetcomplete.resources.ic_chevron_next_24
import de.westnordost.streetcomplete.resources.ic_clear_24
import de.westnordost.streetcomplete.resources.ic_content_copy_24
import de.westnordost.streetcomplete.resources.ic_crosshair_32
import de.westnordost.streetcomplete.resources.ic_email_24
import de.westnordost.streetcomplete.resources.ic_file_download_24
import de.westnordost.streetcomplete.resources.ic_file_upload_24
import de.westnordost.streetcomplete.resources.ic_menu_24
import de.westnordost.streetcomplete.resources.ic_open_in_browser_24
import de.westnordost.streetcomplete.resources.ic_overlay_24
import de.westnordost.streetcomplete.resources.ic_stop_recording_24
import de.westnordost.streetcomplete.resources.ic_team_mode_24
import org.jetbrains.compose.resources.painterResource

@Composable
fun BackIcon() {
    Icon(painterResource(Res.drawable.ic_arrow_back_24), stringResource(R.string.action_back))
}

@Composable
fun ClearIcon() {
    Icon(painterResource(Res.drawable.ic_clear_24), stringResource(R.string.action_clear))
}

@Composable
fun MoreIcon() {
    Icon(painterResource(R.drawable.ic_more_24dp), stringResource(R.string.action_more))
}

@Composable
fun SearchIcon() {
    Icon(painterResource(R.drawable.ic_search_24dp), stringResource(R.string.action_search))
}

@Composable
fun CopyIcon() {
    Icon(painterResource(Res.drawable.ic_content_copy_24), stringResource(android.R.string.copy))
}

@Composable
fun OpenInBrowserIcon() {
    Icon(painterResource(Res.drawable.ic_open_in_browser_24), stringResource(R.string.action_open_in_browser))
}

@Composable
fun NextScreenIcon() {
    Icon(painterResource(Res.drawable.ic_chevron_next_24), null)
}

@Composable
fun UndoIcon() {
    Icon(painterResource(R.drawable.ic_undo_24dp), stringResource(R.string.action_undo))
}

@Composable
fun OverlaysIcon() {
    Icon(painterResource(Res.drawable.ic_overlay_24), stringResource(R.string.action_overlays))
}

@Composable
fun UploadIcon() {
    Icon(painterResource(Res.drawable.ic_file_upload_24), stringResource(R.string.action_upload))
}

@Composable
fun DownloadIcon() {
    Icon(painterResource(Res.drawable.ic_file_download_24), stringResource(R.string.action_download))
}

@Composable
fun TeamModeIcon() {
    Icon(painterResource(Res.drawable.ic_team_mode_24), stringResource(R.string.team_mode))
}

@Composable
fun MessagesIcon() {
    Icon(painterResource(Res.drawable.ic_email_24), stringResource(R.string.action_messages))
}

@Composable
fun MenuIcon() {
    Icon(painterResource(Res.drawable.ic_menu_24), stringResource(R.string.map_btn_menu))
}

@Composable
fun ZoomInIcon() {
    Icon(painterResource(R.drawable.ic_add_24dp), stringResource(R.string.map_btn_zoom_in))
}

@Composable
fun ZoomOutIcon() {
    Icon(painterResource(R.drawable.ic_subtract_24dp), stringResource(R.string.map_btn_zoom_out))
}

@Composable
fun StopRecordingIcon() {
    Icon(painterResource(Res.drawable.ic_stop_recording_24), stringResource(R.string.map_btn_stop_track))
}

@Composable
fun LargeCreateIcon() {
    Icon(painterResource(Res.drawable.ic_crosshair_32), stringResource(R.string.action_create_new_poi))
}

@Composable
fun MeasurementIcon() {
    Icon(painterResource(Res.drawable.ic_camera_measure_24), stringResource(R.string.ar_measure))
}
