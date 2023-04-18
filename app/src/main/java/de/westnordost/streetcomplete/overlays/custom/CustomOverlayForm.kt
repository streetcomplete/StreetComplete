package de.westnordost.streetcomplete.overlays.custom

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentOverlayCustomBinding
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import org.koin.android.ext.android.inject

class CustomOverlayForm : AbstractOverlayForm() {
    private val prefs: SharedPreferences by inject()

    override val contentLayoutResId = R.layout.fragment_overlay_custom
    private val binding by contentViewBinding(FragmentOverlayCustomBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val colorKeySelector = try {
            prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, prefs), "")?.takeIf { it.isNotEmpty() }?.toRegex()
        } catch (_: Exception) { null }
        val colorTags = if (colorKeySelector != null)
            element?.tags?.filter { it.key.matches(colorKeySelector) }
        else null
        if (colorTags != null)
            binding.text.text = colorTags.entries.sortedBy { it.key }.joinToString("\n") { "${it.key} = ${it.value}" }
        else
            binding.text.isGone = true
        binding.editButton.setOnClickListener { element?.let { editTags(it, "CustomOverlay") } }
    }

    override fun hasChanges() = false

    override fun isFormComplete() = false

    override fun onClickOk() {}
}
