package de.westnordost.streetcomplete.quests.building_levels

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.QuestBuildingLevelsBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.takeFavourites
import org.koin.android.ext.android.inject

class AddBuildingLevelsForm : AbstractOsmQuestForm<BuildingLevels>() {

    override val contentLayoutResId = R.layout.quest_building_levels
    private val binding by contentViewBinding(QuestBuildingLevelsBinding::bind)

    private val prefs: Preferences by inject()
    private lateinit var levels: MutableState<String>
    private lateinit var roofLevels: MutableState<String>
    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_buildingLevels_answer_multipleLevels) { showMultipleLevelsHint() }
    )

    private val lastPickedAnswers by lazy {
        prefs.getLastPicked(this::class.simpleName!!)
            .map { value ->
                value.split("#")
                    .let { BuildingLevels(it[0].toInt(), it.getOrNull(1)?.toInt()) }
            }
            .takeFavourites(n = 5, history = 15, first = 1)
            .sortedWith(compareBy<BuildingLevels> { it.levels }.thenBy { it.roofLevels })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.questBuildingLevelsBase.content {
            levels = rememberSaveable { mutableStateOf(element.tags["building:levels"] ?: "") }
            roofLevels = rememberSaveable { mutableStateOf(element.tags["roof:levels"] ?: "") }
            BuildingLevelsForm(
                levels = levels.value,
                onLevelsChange = {
                    levels.value = it
                    checkIsFormComplete()
                },
                roofLevels = roofLevels.value,
                onRoofLevelsChange = {
                    roofLevels.value = it
                    checkIsFormComplete()
                },
                previousBuildingLevels = lastPickedAnswers
            )
        }
    }

    override fun onClickOk() {
        val answer = BuildingLevels(
            levels.value.toInt(),
            roofLevels.value.takeIf { it.isNotEmpty() }?.toInt()
        )
        prefs.addLastPicked(
            this::class.simpleName!!,
            listOfNotNull(answer.levels, answer.roofLevels).joinToString("#")
        )
        applyAnswer(answer)
    }

    private fun showMultipleLevelsHint() {
        activity?.let {
            AlertDialog.Builder(it)
                .setMessage(R.string.quest_buildingLevels_answer_description)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    override fun isFormComplete(): Boolean {
        val roofShape = element.tags["roof:shape"]
        val hasNonFlatRoofShape = roofShape != null && roofShape != "flat"
        val roofLevelsAreOptional = countryInfo.roofsAreUsuallyFlat && !hasNonFlatRoofShape

        return levels.value.isValidLevel()
            && (
                roofLevelsAreOptional && roofLevels.value.isEmpty()
                || roofLevels.value.isValidLevel()
            )
    }

    private fun String.isValidLevel(): Boolean =
        toIntOrNull()?.takeIf { it >= 0 } != null
}
