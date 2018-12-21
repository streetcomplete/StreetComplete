package de.westnordost.streetcomplete.quests.building_levels

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.LastPickedValuesStore
import de.westnordost.streetcomplete.util.TextChangedWatcher

import kotlinx.android.synthetic.main.quest_building_levels.*

class AddBuildingLevelsForm : AbstractQuestFormAnswerFragment() {

    @Inject internal lateinit var favs: LastPickedValuesStore

    private val levels = levelsInput.text.toString().trim()
    private val roofLevels = roofLevelsInput.text.toString().trim()

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        setContentView(R.layout.quest_building_levels)

        val onTextChangedListener = TextChangedWatcher { checkIsFormComplete() }

        levelsInput.requestFocus()
        levelsInput.addTextChangedListener(onTextChangedListener)
        roofLevelsInput.addTextChangedListener(onTextChangedListener)

        addOtherAnswer(R.string.quest_buildingLevels_answer_multipleLevels) {
            AlertDialog.Builder(activity!!)
                .setMessage(R.string.quest_buildingLevels_answer_description)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        val lastPicked = favs.getLastPicked(javaClass.simpleName)
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

        return view
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
        favs.addLastPicked(javaClass.simpleName, favValues.joinToString("#"), MAX_FAVS)
        applyAnswer(answer)
    }

    override fun isFormComplete() = !levels.isEmpty()

    companion object {
        const val BUILDING_LEVELS = "building_levels"
        const val ROOF_LEVELS = "roof_levels"

        private const val MAX_FAVS = 1
    }
}
