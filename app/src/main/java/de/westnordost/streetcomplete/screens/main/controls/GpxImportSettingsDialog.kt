package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.LabelFormatter
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.databinding.DialogGpxImportSettingsBinding
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch

data class GpxImportSettings(
    val displayTrack: Boolean,
    val downloadAlongTrack: Boolean,
    val minDownloadDistance: Double,
)

/** A dialog to specify GPX import settings */
class GpxImportSettingsDialog(
    private val lengthUnit: LengthUnit,
    private val callback: (GpxImportSettings) -> Unit,
) : DialogFragment(R.layout.dialog_gpx_import_settings) {
    private val binding by viewBinding(DialogGpxImportSettingsBinding::bind)
    private var worker: Deferred<Result<GpxImportSettings>>? = null

    private val minDownloadDistanceOptions: List<Double> = listOf(10.0, 100.0, 250.0, 500.0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.minDownloadDistanceSlider.setLabelFormatter {
            formatMinDownloadDistance(it.toInt())
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
                callback(
                    GpxImportSettings(
                        binding.displayTrackCheckBox.isChecked,
                        binding.downloadCheckBox.isChecked,
                        minDownloadDistanceInMeters()
                    )
                )
                dismiss()
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
            getString(
                R.string.gpx_import_download_along_track,
                formatMinDownloadDistance(index)
            )
    }

    private fun updateOkButtonState() {
        binding.okButton.isEnabled =
            binding.displayTrackCheckBox.isChecked || binding.downloadCheckBox.isChecked
    }

    private fun formatMinDownloadDistance(index: Int): String {
        val minDownloadDistance = minDownloadDistanceOptions[index].toInt()
        return when (lengthUnit) {
            LengthUnit.FOOT_AND_INCH -> "${minDownloadDistance}yd"
            else -> "${minDownloadDistance}m"
        }
    }

    private fun minDownloadDistanceInMeters(): Double {
        val minDownloadDistance =
            minDownloadDistanceOptions[binding.minDownloadDistanceSlider.value.toInt()]
        return when (lengthUnit) {
            LengthUnit.FOOT_AND_INCH -> minDownloadDistance * YARDS_IN_METER
            else -> minDownloadDistance
        }
    }

    companion object {
        private const val INITIAL_MIN_DOWNLOAD_DISTANCE_INDEX = 1
        private const val YARDS_IN_METER = 0.9144
    }
}
