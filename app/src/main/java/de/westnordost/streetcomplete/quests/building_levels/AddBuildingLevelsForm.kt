package de.westnordost.streetcomplete.quests.building_levels

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.LastPickedValuesStore
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher

import kotlinx.android.synthetic.main.quest_building_levels.*

class AddBuildingLevelsForm : AbstractQuestFormAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_building_levels

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_buildingLevels_answer_multipleLevels) { showMultipleLevelsHint() }
    )

    private val levels get() = levelsInput.text.toString().trim()
    private val roofLevels get() = roofLevelsInput.text.toString().trim()

    @Inject internal lateinit var favs: LastPickedValuesStore

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onTextChangedListener = TextChangedWatcher { checkIsFormComplete() }

        levelsInput.requestFocus()
        levelsInput.addTextChangedListener(onTextChangedListener)
        roofLevelsInput.addTextChangedListener(onTextChangedListener)

        val lastPicked = favs.get(javaClass.simpleName)
        if (lastPicked.isEmpty()) {
            pickLastButton.visibility = View.GONE
        } else {
            pickLastButton.visibility = View.VISIBLE

            val favValues = lastPicked.first.split("#")

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
        val favValues = mutableListOf<Int>()
        val answer = Bundle()

        val buildingLevels = levels.toInt()
        answer.putInt(BUILDING_LEVELS, buildingLevels)
        favValues.add(buildingLevels)

        if (!roofLevels.isEmpty()) {
            val roofLevels = roofLevels.toInt()
            answer.putInt(ROOF_LEVELS, roofLevels)
            favValues.add(roofLevels)
        }
        favs.add(javaClass.simpleName, favValues.joinToString("#"), MAX_FAVS)
        applyAnswer(answer)
    }

    private fun showMultipleLevelsHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_buildingLevels_answer_description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    override fun isFormComplete() = !levels.isEmpty()

    companion object {
        const val BUILDING_LEVELS = "building_levels"
        const val ROOF_LEVELS = "roof_levels"

        private const val MAX_FAVS = 1
    }
}
