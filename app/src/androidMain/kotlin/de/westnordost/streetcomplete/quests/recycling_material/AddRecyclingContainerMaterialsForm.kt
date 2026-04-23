package de.westnordost.streetcomplete.quests.recycling_material

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.takeFavorites
import org.koin.android.ext.android.inject
import kotlin.getValue

class AddRecyclingContainerMaterialsForm : AbstractOsmQuestForm<RecyclingContainerMaterialsAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    private val prefs: Preferences by inject()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_recycling_materials_answer_waste) { confirmJustTrash() }
    )

    private lateinit var reorderedItems: List<RecyclingMaterial>
    private val selectedItems = mutableStateOf(emptySet<RecyclingMaterial>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reorderedItems = moveFavouritesToFront(RecyclingMaterial.entries)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            RecyclingContainerMaterialsForm(
                items = reorderedItems,
                tree = RecyclingMaterial.tree,
                selectedItems = selectedItems.value,
                onSelectedItems = {
                    selectedItems.value = it
                    checkIsFormComplete()
                },
            )
        } }
    }

    override fun onClickOk() {
        prefs.addLastPicked(this::class.simpleName!!, selectedItems.value.toList())
        applyAnswer(RecyclingMaterials(selectedItems.value))
    }

    override fun isFormComplete() = selectedItems.value.isNotEmpty()

    private fun confirmJustTrash() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_recycling_materials_answer_waste_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(IsWasteContainer) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }

    private fun moveFavouritesToFront(originalList: List<RecyclingMaterial>): List<RecyclingMaterial> {
        val favourites = prefs
            .getLastPicked<RecyclingMaterial>(this::class.simpleName!!)
            .takeFavorites(4)
        return (favourites + originalList).distinct()
    }
}
