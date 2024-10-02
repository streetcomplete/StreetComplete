package de.westnordost.streetcomplete.screens.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.StreetCompleteApplication
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadWorker
import de.westnordost.streetcomplete.data.importGpx
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon
import de.westnordost.streetcomplete.util.ktx.toast
import io.ticofab.androidgpxparser.parser.GPXParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.io.IOException

class DisplaySettingsFragment :
    PreferenceFragmentCompat(),
    HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: ObservableSettings by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val downloadController: DownloadController by inject()

    override val title: String get() = getString(R.string.pref_screen_display)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.rootView.findViewById<Toolbar>(R.id.toolbar)?.apply {
            setUpToolbarTitleAndIcon(this)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences_ee_display, false)
        addPreferencesFromResource(R.xml.preferences_ee_display)

        findPreference<Preference>("display_gpx_track")?.setOnPreferenceClickListener {
            onClickDisplayGpxTrack()
            true
        }
    }

    private fun onClickDisplayGpxTrack() {
        val gpxFileExists = context?.getExternalFilesDir(null)?.let { File(it, GPX_TRACK_FILE) }?.exists() == true
        var d: AlertDialog? = null
        val selectFileButton = Button(context).apply {
            setText(R.string.pref_gpx_track_provide)
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                // actually the type should be application/gpx+xml, but often doesn't work
                // for some phones only application/octet-stream works, for others it doesn't, so just allow everything
                type = "*/*"
            }
            setOnClickListener {
                d?.dismiss()
                startActivityForResult(intent, GPX_TRACK_CODE)
            }
        }
        val enableSwitch = SwitchCompat(requireContext()).apply {
            setText(R.string.pref_gpx_track_enable)
            isChecked = prefs.getBoolean(Prefs.SHOW_GPX_TRACK, false)
            isEnabled = gpxFileExists
            setOnCheckedChangeListener { _, _ ->
                prefs.putBoolean(Prefs.SHOW_GPX_TRACK, isChecked)
                gpx_track_changed = true
            }
        }
        val downloadButton = Button(context).apply {
            setText(R.string.pref_gpx_track_download)
            isEnabled = gpxFileExists
            setOnClickListener {
                val points = loadGpxTrackPoints(requireContext(), true) ?: return@setOnClickListener
                GlobalScope.launch {
                    val import = importGpx(points, true, 10.0).getOrNull()
                    import?.downloadBBoxes?.let {
                        if (it.isEmpty()) return@launch
                        DownloadWorker.enqueuedDownloads.addAll(it.drop(1))
                        downloadController.download(it.first(), false, true)
                    }
                }
            }
        }
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            addView(downloadButton)
            addView(selectFileButton)
            addView(enableSwitch)
        }
        d = AlertDialog.Builder(requireContext())
            .setTitle(R.string.pref_gpx_track_title)
            .setViewWithDefaultPadding(layout)
            .setPositiveButton(R.string.close, null)
            .create()
        d.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri = data?.data
        if (resultCode != Activity.RESULT_OK || requestCode != GPX_TRACK_CODE || uri == null) {
            context?.toast(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
            return
        }
        // fail if file doesn't have gpx ending
        activity?.contentResolver?.query(uri, null, null, null, null).use {
            if (it != null && it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && !it.getString(idx).endsWith(".gpx")) {
                    context?.toast(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
                    return
                }
            }
        }
        try {
            activity?.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { reader ->
                File(context?.getExternalFilesDir(null), GPX_TRACK_FILE).writeText(reader.readText())
            } }
            gpx_track_changed = true
            onClickDisplayGpxTrack()
        } catch (e: IOException) {
            context?.toast(R.string.pref_gpx_track_loading_error, Toast.LENGTH_LONG)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == Prefs.QUEST_GEOMETRIES)
            visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
        if (key == Prefs.OFFSET_FIX) {
            // trigger map update
            val old = prefs.getString(Prefs.THEME_BACKGROUND, "MAP")
            val new = if (old == "MAP") "AERIAL" else "MAP"
            sharedPreferences.edit().putString(Prefs.THEME_BACKGROUND, new).commit()
            prefs.putString(Prefs.THEME_BACKGROUND, old)
        }
    }

    override fun onResume() {
        super.onResume()
        StreetCompleteApplication.preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        StreetCompleteApplication.preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {
        var gpx_track_changed = false
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

private const val GPX_TRACK_CODE = 56327
private const val GPX_TRACK_FILE = "display_track.gpx"
