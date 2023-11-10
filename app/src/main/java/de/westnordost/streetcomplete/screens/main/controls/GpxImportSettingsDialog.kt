package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.import.GpxImporter
import de.westnordost.streetcomplete.databinding.DialogGpxImportSettingsBinding
import de.westnordost.streetcomplete.util.ktx.spToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.InputStream
import kotlin.math.roundToInt

/** A dialog to specify GPX import settings */
class GpxImportSettingsDialog(
    private val inputStream: InputStream,
    private val callback: (result: Result<GpxImporter.GpxImportData>) -> Unit,
) : DialogFragment(R.layout.dialog_gpx_import_settings) {
    private val gpxImporter: GpxImporter by inject()
    private val binding by viewBinding(DialogGpxImportSettingsBinding::bind)
    private var worker: Deferred<Result<GpxImporter.GpxImportData>>? = null

    private val minDownloadDistanceOptions: List<Double> = listOf(10.0, 100.0, 250.0, 500.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_DialogFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.minDownloadDistanceSlider.setLabelFormatter {
            getString(R.string.gpx_distance_formatter, minDownloadDistanceOptions[it.toInt()])
        }
        binding.minDownloadDistanceSlider.addOnChangeListener { _, value, _ ->
            updateDownloadCheckboxLabel(value.toInt())
        }
        binding.minDownloadDistanceSlider.value = INITIAL_MIN_DOWNLOAD_DISTANCE_INDEX.toFloat()
        updateDownloadCheckboxLabel(INITIAL_MIN_DOWNLOAD_DISTANCE_INDEX)

        binding.downloadCheckBox.setOnClickListener {
            if (binding.downloadCheckBox.isChecked) {
                binding.minDownloadDistanceSlider.visibility = View.VISIBLE
                binding.minDownloadDistanceSlider.labelBehavior = LabelFormatter.LABEL_VISIBLE
            } else {
                binding.minDownloadDistanceSlider.visibility = View.GONE
                binding.minDownloadDistanceSlider.labelBehavior = LabelFormatter.LABEL_GONE
            }
            updateOkButtonState()
        }

        binding.displayTrackCheckBox.setOnClickListener {
            updateOkButtonState()
        }
        binding.okButton.setOnClickListener {
            viewLifecycleScope.launch {
                callback(processGpxFile())
            }
        }
        binding.cancelButton.setOnClickListener {
            worker?.cancel()
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        worker?.cancel()
    }

    private fun updateDownloadCheckboxLabel(index: Int) {
        binding.downloadCheckBox.text =
            getString(R.string.gpx_import_download_along_track, minDownloadDistanceOptions[index])
    }

    private fun updateOkButtonState() {
        binding.okButton.isEnabled =
            binding.displayTrackCheckBox.isChecked || binding.downloadCheckBox.isChecked
    }

    private suspend fun processGpxFile(): Result<GpxImporter.GpxImportData> {
        binding.okButton.isEnabled = false

        worker = viewLifecycleScope.async {
            return@async gpxImporter.processGpxFile(
                inputStream,
                binding.displayTrackCheckBox.isChecked,
                binding.downloadCheckBox.isChecked,
                minDownloadDistanceOptions[binding.minDownloadDistanceSlider.value.toInt()]
            ) { p -> withContext(Dispatchers.Main) { binding.importProgress.progress = p } }
        }
        val importData = worker!!.await()

        worker = null

        dismiss()
        return importData
    }

    companion object {
        private const val INITIAL_MIN_DOWNLOAD_DISTANCE_INDEX = 1
    }
}
