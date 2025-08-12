package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.databinding.DialogQuestAddressNoHousenumberBinding
import de.westnordost.streetcomplete.osm.address.BlockAndHouseNumber
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.osm.address.looksInvalid
import de.westnordost.streetcomplete.osm.address.streetHouseNumber
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.asItem
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.IAnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class AddHousenumberForm : AbstractOsmQuestForm<HouseNumberAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers get() =
        listOfNotNull(
            AnswerItem(R.string.quest_address_answer_no_housenumber) { onNoHouseNumber() },
            AnswerItem(R.string.quest_address_answer_house_name2) { showHouseName() },
            createBlockAnswerItem(),
            AnswerItem(R.string.quest_housenumber_multiple_numbers) { showMultipleNumbersHint() }
        )

    private lateinit var address: MutableState<AddressNumberOrName>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            address = rememberSerializable { mutableStateOf(AddressNumberOrName(null, null)) }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (address.value.name != null) {
                    AddressNumberOrNameForm(
                        value = address.value,
                        onValueChange = {
                            address.value = it
                            checkIsFormComplete()
                        },
                        countryCode = countryInfo.countryCode,
                        modifier = Modifier.fillMaxWidth(),
                        houseNumberSuggestion = lastHouseNumber,
                        blockSuggestion = lastBlock,
                    )
                } else {
                    AddressNumberForm(
                        value = address.value.number ?: HouseNumber(""),
                        onValueChange = {
                            address.value = address.value.copy(number = it)
                            checkIsFormComplete()
                        },
                        countryCode = countryInfo.countryCode,
                        houseNumberSuggestion = lastHouseNumber,
                        blockSuggestion = lastBlock,
                    )
                }
            }
        } }
    }

    /* ------------------------------------- Other answers -------------------------------------- */

    private fun createBlockAnswerItem(): IAnswerItem? {
        if (countryInfo.countryCode in listOf("JP", "CZ", "SK")) return null
        return when (address.value.number) {
            is BlockAndHouseNumber ->
                AnswerItem(R.string.quest_address_answer_no_block) {
                    address.value = address.value.copy(number = HouseNumber(""))
                }
            else ->
                AnswerItem(R.string.quest_address_answer_block) {
                    address.value = address.value.copy(number = BlockAndHouseNumber("", ""))
                }
        }
    }

    private fun showMultipleNumbersHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_housenumber_multiple_numbers_description2)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    private fun onNoHouseNumber() {
        val buildingType = createBuildingType(element.tags)
        if (buildingType != null) {
            showNoHouseNumberDialog(buildingType.asItem())
        } else {
            // fallback in case the type of building is known by Housenumber quest but not by
            // building type quest
            onClickCantSay()
        }
    }

    private fun showNoHouseNumberDialog(buildingType: DisplayItem<BuildingType>) {
        val dialogBinding = DialogQuestAddressNoHousenumberBinding.inflate(layoutInflater)
        ItemViewHolder(dialogBinding.root).bind(buildingType)

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(AddressNumberOrName(null, null)) }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(HouseNumberAnswer.WrongBuildingType) }
            .show()
    }

    /* ----------------------------------- Show house name -------------------------------------- */

    private fun showHouseName() {
        address.value = AddressNumberOrName(
            name = "",
            number = address.value.number?.takeIf { !it.isEmpty() }
        )
    }

    /* ----------------------------------- Commit answer ---------------------------------------- */

    override fun onClickOk() {
        val number = address.value.number
        val isUnusual = number?.looksInvalid(countryInfo.additionalValidHousenumberRegex) == true
        confirmHouseNumber(isUnusual) {
            applyAnswer(address.value)
            lastBlock = (number as? BlockAndHouseNumber)?.block
            number?.streetHouseNumber?.let { lastHouseNumber = it }
        }
    }

    private fun confirmHouseNumber(isUnusual: Boolean, onConfirmed: () -> Unit) {
        if (isUnusual) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_generic_confirmation_title)
                .setMessage(R.string.quest_address_unusualHousenumber_confirmation_description)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> onConfirmed() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        } else {
            onConfirmed()
        }
    }
    override fun isFormComplete(): Boolean =
        address.value.number?.isComplete() == true ||
        address.value.name?.isNotEmpty() == true

    override fun isRejectingClose(): Boolean =
        address.value.number?.isEmpty() == false ||
        address.value.name?.isEmpty() == false

    companion object {
        private var lastBlock: String? = null
        private var lastHouseNumber: String? = null
    }
}
