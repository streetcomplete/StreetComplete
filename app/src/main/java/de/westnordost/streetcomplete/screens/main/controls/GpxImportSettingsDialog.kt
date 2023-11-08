package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.import.GpxImporter
import de.westnordost.streetcomplete.databinding.DialogGpxImportSettingsBinding
import de.westnordost.streetcomplete.util.ktx.spToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

        binding.minDownloadDistancePicker.wrapSelectorWheel = false
        binding.minDownloadDistancePicker.displayedValues =
            minDownloadDistanceOptions.map { it.roundToInt().toString() }.toTypedArray()
        binding.minDownloadDistancePicker.minValue = 0
        binding.minDownloadDistancePicker.maxValue = minDownloadDistanceOptions.size - 1
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            binding.minDownloadDistancePicker.textSize = requireContext().spToPx(32)
        }
        binding.minDownloadDistancePicker.value = 1
        // do not allow keyboard input
        binding.minDownloadDistancePicker.disableEditTextsFocus()

        binding.okButton.setOnClickListener {
            viewLifecycleScope.launch { callback(processGpxFile()) }
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

    private suspend fun processGpxFile(): Result<GpxImporter.GpxImportData> {
        binding.okButton.isEnabled = false

        worker = viewLifecycleScope.async {
            return@async gpxImporter.processGpxFile(
                inputStream,
                minDownloadDistanceOptions[binding.minDownloadDistancePicker.value],
                binding.downloadCheckBox.isChecked
            ) { p -> withContext(Dispatchers.Main) { binding.importProgress.progress = p } }
        }
        val importData = worker!!.await()

        worker = null

        dismiss()
        return importData
    }

    private fun ViewGroup.disableEditTextsFocus() {
        for (child in children) {
            if (child is ViewGroup) {
                child.disableEditTextsFocus()
            } else if (child is EditText) {
                child.isFocusable = false
            }
        }
    }
}
