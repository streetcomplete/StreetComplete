package de.westnordost.streetcomplete.overlays.custom

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentOverlayCustomBinding
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm

class CustomOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_custom
    private val binding by contentViewBinding(FragmentOverlayCustomBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editButton.setOnClickListener { element?.let { editTags(it, "CustomOverlay") } }
    }

    override fun hasChanges() = false

    override fun isFormComplete() = false

    override fun onClickOk() {}
}
