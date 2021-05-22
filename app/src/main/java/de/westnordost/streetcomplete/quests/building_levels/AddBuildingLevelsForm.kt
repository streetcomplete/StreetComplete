package de.westnordost.streetcomplete.quests.building_levels

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import androidx.core.view.isGone

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestBuildingLevelsBinding
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.LastPickedValuesStore
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddBuildingLevelsForm : AbstractQuestFormAnswerFragment<BuildingLevelsAnswer>() {

    override val contentLayoutResId = R.layout.quest_building_levels

    private val binding by viewBinding(QuestBuildingLevelsBinding::bind)

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_buildingLevels_answer_multipleLevels) { showMultipleLevelsHint() }
    )

    private val levels get() = binding.levelsInput?.text?.toString().orEmpty().trim()
    private val roofLevels get() = binding.roofLevelsInput?.text?.toString().orEmpty().trim()

    @Inject internal lateinit var favs: LastPickedValuesStore<String>

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onTextChangedListener = TextChangedWatcher {
            checkIsFormComplete()
            if (isFormComplete()) binding.pickLastButton.visibility = View.GONE
        }

        binding.levelsInput.requestFocus()
        binding.levelsInput.addTextChangedListener(onTextChangedListener)
        binding.roofLevelsInput.addTextChangedListener(onTextChangedListener)

        val lastPickedStrings = favs.get(javaClass.simpleName)
        val isLastPickedStringsEmpty = lastPickedStrings.isEmpty()
        binding.pickLastButton.isGone = isLastPickedStringsEmpty
        if (!isLastPickedStringsEmpty) {
            val favValues = lastPickedStrings.first.split("#")

            binding.lastLevelsLabel.text = favValues[0]
            binding.lastRoofLevelsLabel.text = if (favValues.size > 1) favValues[1] else " "

            binding.pickLastButton.setOnClickListener {
                binding.levelsInput.setText(binding.lastLevelsLabel.text)
                binding.roofLevelsInput.setText(binding.lastRoofLevelsLabel.text)
                binding.pickLastButton.visibility = View.GONE
            }
        }
    }

    override fun onClickOk() {
        val buildingLevels = levels.toInt()
        val roofLevels = if(roofLevels.isNotEmpty()) roofLevels.toInt() else null

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

    override fun isFormComplete() = levels.isNotEmpty()
}
