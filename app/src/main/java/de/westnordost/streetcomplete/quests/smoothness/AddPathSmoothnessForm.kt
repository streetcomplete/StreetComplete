package de.westnordost.streetcomplete.quests.smoothness

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.surface.GROUND_SURFACES
import de.westnordost.streetcomplete.quests.surface.PAVED_SURFACES
import de.westnordost.streetcomplete.quests.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.quests.surface.toItems
import de.westnordost.streetcomplete.view.Item
import kotlinx.android.synthetic.main.fragment_quest_answer.*
import kotlinx.android.synthetic.main.quest_smoothness.*

class AddPathSmoothnessForm : AbstractQuestFormAnswerFragment<String>() {
    override val contentLayoutResId = R.layout.quest_smoothness

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val values = listOf(
        Item("paved", R.drawable.panorama_path_surface_paved, R.string.quest_surface_value_paved, null, PAVED_SURFACES.toItems()),
        Item("unpaved", R.drawable.panorama_path_surface_unpaved, R.string.quest_surface_value_unpaved, null, UNPAVED_SURFACES.toItems()),
        Item("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, null, GROUND_SURFACES.toItems()))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSlider()
        checkIsFormComplete()

        listener?.onHighlightSidewalkSide(questId, questGroup, Listener.SidewalkSide.LEFT)
    }

    private fun initSlider() {
        valueSlider.apply {
            valueFrom = 0f
            valueTo = values.size.toFloat() - 1
            value = 1f
        }

        setValueInformation(values[1])
        valueSlider.addOnChangeListener { _, value, _ ->
            val item = values[value.toInt()]
            setValueInformation(item)
        }
    }

    private fun setValueInformation(item: Item<String>) {
        valueName.text = item.value
        valueDescription.text = resources.getText(item.titleId!!)

        val exampleImage: Drawable? = ContextCompat.getDrawable(requireContext(), item.drawableId!!)
        valueExampleImage.setImageDrawable(exampleImage)
    }

    override fun onClickOk() {
        bottomSheetContainer.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.enter_from_right)
        )
        listener?.onHighlightSidewalkSide(questId, questGroup, Listener.SidewalkSide.RIGHT)
    }

    override fun isFormComplete(): Boolean {
        return true
    }

    override fun isRejectingClose(): Boolean {
        return false
    }
}
