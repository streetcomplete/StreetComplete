package de.westnordost.streetcomplete.quests.building_levels

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.LastPickedValuesStore
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher

import kotlinx.android.synthetic.main.quest_building_levels.*

class AddBuildingLevelsForm : AbstractQuestFormAnswerFragment<BuildingLevelsAnswer>() {

    override val contentLayoutResId = R.layout.quest_building_levels

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_buildingLevels_answer_multipleLevels) { showMultipleLevelsHint() }
    )

    private val levels get() = levelsInput?.text?.toString().orEmpty().trim()
    private val roofLevels get() = roofLevelsInput?.text?.toString().orEmpty().trim()

    @Inject internal lateinit var favs: LastPickedValuesStore<String>

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onTextChangedListener = TextChangedWatcher { checkIsFormComplete() }

        levelsInput.requestFocus()
        levelsInput.addTextChangedListener(onTextChangedListener)
        roofLevelsInput.addTextChangedListener(onTextChangedListener)

        val lastPickedStrings = favs.get(javaClass.simpleName)
        if (lastPickedStrings.isEmpty()) {
            pickLastButton.visibility = View.GONE
        } else {
            pickLastButton.visibility = View.VISIBLE

            val favValues = lastPickedStrings.first.split("#")

            lastLevelsLabel.text = favValues[0]
            lastRoofLevelsLabel.text = if (favValues.size > 1) favValues[1] else " "

            pickLastButton.setOnClickListener {
                levelsInput.setText(lastLevelsLabel.text)
                roofLevelsInput.setText(lastRoofLevelsLabel.text)
                pickLastButton.visibility = View.GONE
            }
        }
    }

    override fun onClickOk() {
        val buildingLevels = levels.toInt()
        val roofLevels = if(!roofLevels.isEmpty()) roofLevels.toInt() else null

        favs.add(javaClass.simpleName,
            listOfNotNull(buildingLevels, roofLevels).joinToString("#"), max = 1)
        applyAnswer(BuildingLevelsAnswer(buildingLevels, roofLevels))
    }

    private fun showMultipleLevelsHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_buildingLevels_answer_description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    override fun isFormComplete() = !levels.isEmpty()
}
