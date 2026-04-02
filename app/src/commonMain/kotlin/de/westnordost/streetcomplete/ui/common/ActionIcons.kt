package de.westnordost.streetcomplete.ui.common

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BackIcon() {
    Icon(painterResource(Res.drawable.ic_arrow_back_24), stringResource(Res.string.action_back))
}

@Composable
fun ClearIcon() {
    Icon(painterResource(Res.drawable.ic_clear_24), stringResource(Res.string.action_clear))
}

@Composable
fun MoreIcon() {
    Icon(painterResource(Res.drawable.ic_more_24), stringResource(Res.string.action_more))
}

@Composable
fun SearchIcon() {
    Icon(painterResource(Res.drawable.ic_search_24), stringResource(Res.string.action_search))
}

@Composable
fun CopyIcon() {
    Icon(painterResource(Res.drawable.ic_content_copy_24), stringResource(Res.string.action_copy))
}

@Composable
fun OpenInBrowserIcon() {
    Icon(painterResource(Res.drawable.ic_open_in_browser_24), stringResource(Res.string.action_open_in_browser))
}

@Composable
fun NextScreenIcon() {
    Icon(painterResource(Res.drawable.ic_chevron_next_24), null)
}

@Composable
fun UndoIcon() {
    Icon(painterResource(Res.drawable.ic_undo_24), stringResource(Res.string.action_undo))
}

@Composable
fun OverlaysIcon() {
    Icon(painterResource(Res.drawable.ic_overlay_24), stringResource(Res.string.action_overlays))
}

@Composable
fun UploadIcon() {
    Icon(painterResource(Res.drawable.ic_file_upload_24), stringResource(Res.string.action_upload))
}

@Composable
fun DownloadIcon() {
    Icon(painterResource(Res.drawable.ic_file_download_24), stringResource(Res.string.action_download))
}

@Composable
fun TeamModeIcon() {
    Icon(painterResource(Res.drawable.ic_team_mode_24), stringResource(Res.string.team_mode))
}

@Composable
fun MessagesIcon() {
    Icon(painterResource(Res.drawable.ic_email_24), stringResource(Res.string.action_messages))
}

@Composable
fun MenuIcon() {
    Icon(painterResource(Res.drawable.ic_menu_24), stringResource(Res.string.map_btn_menu))
}

@Composable
fun ZoomInIcon() {
    Icon(painterResource(Res.drawable.ic_add_24), stringResource(Res.string.map_btn_zoom_in))
}

@Composable
fun ZoomOutIcon() {
    Icon(painterResource(Res.drawable.ic_subtract_24), stringResource(Res.string.map_btn_zoom_out))
}

@Composable
fun StopRecordingIcon() {
    Icon(painterResource(Res.drawable.ic_stop_recording_24), stringResource(Res.string.map_btn_stop_track))
}

@Composable
fun LargeCreateIcon() {
    Icon(painterResource(Res.drawable.ic_crosshair_32), stringResource(Res.string.action_create_new_poi))
}

@Composable
fun MeasurementIcon() {
    Icon(painterResource(Res.drawable.ic_camera_measure_24), stringResource(Res.string.ar_measure))
}

@Composable
fun InfoOutlineIcon() {
    Icon(painterResource(Res.drawable.ic_info_outline_24), stringResource(Res.string.info))
}

@Composable
fun InfoFilledIcon() {
    Icon(painterResource(Res.drawable.ic_info_filled_24), stringResource(Res.string.info))
}
