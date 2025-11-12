package de.westnordost.streetcomplete.quests.recycling_material

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content

class AddRecyclingContainerMaterialsForm : AbstractOsmQuestForm<RecyclingContainerMaterialsAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_recycling_materials_answer_waste) { confirmJustTrash() }
    )

    private val selectedItems = mutableStateOf(emptySet<RecyclingMaterial>())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            RecyclingContainerMaterialsForm(
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
}
