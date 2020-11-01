package de.westnordost.streetcomplete.quests.smoothness

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.surface.PAVED_SURFACES
import de.westnordost.streetcomplete.quests.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.quests.surface.toItems
import de.westnordost.streetcomplete.view.image_select.Item
import kotlinx.android.synthetic.main.quest_smoothness.*

class AddPathSmoothnessForm : AbstractQuestFormAnswerFragment<String>() {
    override val contentLayoutResId = R.layout.quest_smoothness

    private val values = listOf(
        Item("impassable", R.drawable.smoothness_impassable, R.string.quest_smoothness_impassable, R.string.quest_smoothness_impassable_description, UNPAVED_SURFACES.toItems()),
        Item("very_horrible", R.drawable.smoothness_very_horrible, R.string.quest_smoothness_very_horrible, R.string.quest_smoothness_very_horrible_description, UNPAVED_SURFACES.toItems()),
        Item("horrible", R.drawable.smoothness_horrible, R.string.quest_smoothness_horrible, R.string.quest_smoothness_horrible_description, UNPAVED_SURFACES.toItems()),
        Item("very_bad", R.drawable.smoothness_very_bad, R.string.quest_smoothness_very_bad, R.string.quest_smoothness_very_bad_description, UNPAVED_SURFACES.toItems()),
        Item("bad", R.drawable.smoothness_bad, R.string.quest_smoothness_bad, R.string.quest_smoothness_bad_description, UNPAVED_SURFACES.toItems()),
        Item("intermediate", R.drawable.smoothness_intermediate, R.string.quest_smoothness_intermediate, R.string.quest_smoothness_intermediate_description, UNPAVED_SURFACES.toItems()),
        Item("good", R.drawable.smoothness_good, R.string.quest_smoothness_good, R.string.quest_smoothness_good_description, UNPAVED_SURFACES.toItems()),
        Item("excellent", R.drawable.smoothness_excellent, R.string.quest_smoothness_excellent, R.string.quest_smoothness_excellent_description, PAVED_SURFACES.toItems()))
    private val initialValueIndex = 5

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSlider()
        checkIsFormComplete()
    }

    private fun initSlider() {
        valueSlider.apply {
            valueFrom = 0f
            valueTo = values.size.toFloat() - 1
            value = initialValueIndex.toFloat()
        }

        setValueInformation(values[initialValueIndex])
        valueSlider.addOnChangeListener { _, value, _ ->
            val item = values[value.toInt()]
            setValueInformation(item)
        }
    }

    private fun setValueInformation(item: Item<String>) {
        valueName.text = resources.getText(item.titleId!!)
        valueDescription.text = resources.getText(item.descriptionId!!)

        val exampleImage: Drawable? = ContextCompat.getDrawable(requireContext(), item.drawableId!!)
        valueExampleImage.setImageDrawable(exampleImage)
    }

    override fun onClickOk() {
        applyAnswer("test")
    }

    override fun isFormComplete(): Boolean {
        return true
    }

    override fun isRejectingClose(): Boolean {
        return false
    }
}
