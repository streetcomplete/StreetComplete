package de.westnordost.streetcomplete.overlays.mtb_scale

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.databinding.FragmentOverlayMtbScaleSelectBinding
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.valueOfOrNull
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.view.setImage
import org.koin.android.ext.android.inject

class MtbScaleOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_mtb_scale_select
    private val binding by contentViewBinding(FragmentOverlayMtbScaleSelectBinding::bind)

    private var originalMtbScale: MtbScale? = null

    private lateinit var mtbScaleCtrl: MtbScaleViewController
    private val prefs: ObservableSettings by inject()
    private lateinit var favs: LastPickedValuesStore<MtbScale>
    private val lastPickedMtbScale: MtbScale?
        get() = favs.get().firstOrNull()

    override fun hasChanges(): Boolean = mtbScaleCtrl.value != originalMtbScale

    override fun isFormComplete(): Boolean = mtbScaleCtrl.value != null

    override fun onClickOk() {
        val changesBuilder = StringMapChangesBuilder(element!!.tags)

        if (mtbScaleCtrl.value != null) {
            favs.add(mtbScaleCtrl.value!!)
        }
        changesBuilder["mtb:scale"] = mtbScaleCtrl.value!!.osmValue

        applyEdit(UpdateElementTagsAction(element!!, changesBuilder.create()))
    }


    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            prefs,
            key = javaClass.simpleName,
            serialize = { it.name },
            deserialize = { valueOfOrNull<MtbScale>(it) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        originalMtbScale = MtbScale.entries.find { it.osmValue == element!!.tags["mtb:scale"]?.take(1) }
        Log.e("TEST","originalMtbScale $originalMtbScale ${element!!.tags["mtb:scale"]}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mtbScaleCtrl = MtbScaleViewController(
            selectButton = binding.selectButton.root,
            selectedCellView = binding.selectButton.selectedCellView,
            selectTextView = binding.selectButton.selectTextView,
        )
        mtbScaleCtrl.onInputChanged = { checkIsFormComplete() }

        binding.lastPickedButton.isGone = lastPickedMtbScale == null
        binding.lastPickedButton.setImage(lastPickedMtbScale?.asItem()?.image)
        binding.lastPickedButton.setOnClickListener {
            mtbScaleCtrl.value = lastPickedMtbScale
            binding.lastPickedButton.isGone = true
            checkIsFormComplete()
        }

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        } else {
            initStateFromTags()
        }
        checkIsFormComplete()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(MTB_SCALE, mtbScaleCtrl.value?.osmValue)

    }

    private fun onLoadInstanceState(inState: Bundle) {
        mtbScaleCtrl.value = MtbScale.entries.find { it.osmValue == inState.getString(MTB_SCALE) }
    }

    private fun initStateFromTags() {
        mtbScaleCtrl.value = originalMtbScale
    }

    companion object {
        private const val MTB_SCALE = "selected_mtb_scale"
    }
}
