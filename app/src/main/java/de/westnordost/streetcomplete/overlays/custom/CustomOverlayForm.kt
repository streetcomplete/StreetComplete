package de.westnordost.streetcomplete.overlays.custom

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentOverlayCustomBinding
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import org.koin.android.ext.android.inject

class CustomOverlayForm : AbstractOverlayForm() {
    private val prefs: ObservableSettings by inject()

    override val contentLayoutResId = R.layout.fragment_overlay_custom
    private val binding by contentViewBinding(FragmentOverlayCustomBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val colorKeyPref = prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, prefs), "")
        val colorKeySelector = try {
            val actualColorKeyPref = if (colorKeyPref.startsWith("!"))
                    colorKeyPref.substringAfter("!")
                else colorKeyPref
            actualColorKeyPref.takeIf { it.isNotEmpty() }?.toRegex()
        } catch (_: Exception) { null }
        val colorTags = if (colorKeySelector != null)
            element?.tags?.filter { it.key.matches(colorKeySelector) }
        else null
        if (colorTags != null)
            binding.text.text = colorTags.entries.sortedBy { it.key }.joinToString("\n") { "${it.key} = ${it.value}" }
        else
            binding.text.isGone = true
        binding.editButton.setOnClickListener {
            if (colorKeyPref.startsWith("!") && !colorKeyPref.contains(' '))
                focusKey = colorKeyPref
            element?.let { editTags(it, editTypeName = overlay.name) }
        }
    }

    override fun hasChanges() = false

    override fun isFormComplete() = false

    override fun onClickOk() {}

    companion object {
        var focusKey: String? = null
    }
}
