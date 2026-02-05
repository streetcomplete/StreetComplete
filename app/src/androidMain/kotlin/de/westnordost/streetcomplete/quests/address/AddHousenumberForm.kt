package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.address.BlockAndHouseNumber
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.osm.address.looksInvalid
import de.westnordost.streetcomplete.osm.address.streetHouseNumber
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.IAnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_address_answer_no_housenumber_message1
import de.westnordost.streetcomplete.resources.quest_address_answer_no_housenumber_message2b
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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

    private lateinit var addressNumberAndName: MutableState<AddressNumberAndName>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            addressNumberAndName = rememberSerializable {
                val number = if (lastWasBlock) BlockAndHouseNumber("", "") else HouseNumber("")
                mutableStateOf(AddressNumberAndName(number, null))
            }

            AddressNumberAndNameForm(
                value = addressNumberAndName.value,
                onValueChange = {
                    addressNumberAndName.value = it
                    checkIsFormComplete()
                },
                countryCode = countryInfo.countryCode,
                modifier = Modifier.fillMaxWidth(),
                houseNumberSuggestion = lastHouseNumber,
                blockSuggestion = lastBlock,
            )
        } }
    }

    /* ------------------------------------- Other answers -------------------------------------- */

    private fun createBlockAnswerItem(): IAnswerItem? {
        if (countryInfo.countryCode in listOf("JP", "CZ", "SK")) return null
        return when (addressNumberAndName.value.number) {
            is BlockAndHouseNumber ->
                AnswerItem(R.string.quest_address_answer_no_block2) {
                    addressNumberAndName.value = addressNumberAndName.value.copy(number = HouseNumber(""))
                }
            else ->
                AnswerItem(R.string.quest_address_answer_block2) {
                    addressNumberAndName.value = addressNumberAndName.value.copy(number = BlockAndHouseNumber("", ""))
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
            showNoHouseNumberDialog(buildingType)
        } else {
            // fallback in case the type of building is known by Housenumber quest but not by
            // building type quest
            onClickCantSay()
        }
    }

    private fun showNoHouseNumberDialog(buildingType: BuildingType) {
        val dialogBinding = ComposeViewBinding.inflate(layoutInflater)
        dialogBinding.composeViewBase.content { Surface(Modifier.padding(24.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(Res.string.quest_address_answer_no_housenumber_message1))
                ImageWithDescription(
                    painter = painterResource(buildingType.icon),
                    title = stringResource(buildingType.title),
                    description = buildingType.description?.let { stringResource(it) },
                    imageSize = DpSize(48.dp, 48.dp),
                )
                Text(stringResource(Res.string.quest_address_answer_no_housenumber_message2b))
            }
        } }

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(AddressNumberAndName(null, null)) }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(HouseNumberAnswer.WrongBuildingType) }
            .show()
    }

    /* ----------------------------------- Show house name -------------------------------------- */

    private fun showHouseName() {
        addressNumberAndName.value = AddressNumberAndName(
            name = "",
            number = addressNumberAndName.value.number?.takeIf { !it.isEmpty() }
        )
    }

    /* ----------------------------------- Commit answer ---------------------------------------- */

    override fun onClickOk() {
        val number = addressNumberAndName.value.number?.takeIf { !it.isEmpty() }
        val isUnusual = number?.looksInvalid(countryInfo.additionalValidHousenumberRegex) == true
        confirmHouseNumber(isUnusual) {
            applyAnswer(addressNumberAndName.value)
            lastBlock = (number as? BlockAndHouseNumber)?.block
            lastWasBlock = number is BlockAndHouseNumber
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
        addressNumberAndName.value.isComplete()

    override fun isRejectingClose(): Boolean =
        !addressNumberAndName.value.isEmpty()

    companion object {
        private var lastBlock: String? = null
        private var lastHouseNumber: String? = null
        private var lastWasBlock: Boolean = false
    }
}
