package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeAndStatus
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Dialog in which the user is asked whether the given [buildingTypeAndStatus] is correct, in the
 *  context of that he answered before that the building doesn't have a house number. So, either it
 *  actually doesn't have a house number, or maybe the building type was mistagged instead. */
@Composable
fun NoHouseNumberDialog(
    onDismissRequest: () -> Unit,
    onNoHouseNumber: () -> Unit,
    onWrongBuildingType: () -> Unit,
    buildingTypeAndStatus: BuildingTypeAndStatus,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onNoHouseNumber()
                onDismissRequest()
            }) { Text(stringResource(Res.string.quest_generic_hasFeature_yes)) }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = {
                onWrongBuildingType()
                onDismissRequest()
            }) { Text(stringResource(Res.string.quest_generic_hasFeature_no)) }
        },
        title = {
            Text(stringResource(Res.string.quest_address_answer_no_housenumber_message2b))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(Res.string.quest_address_answer_no_housenumber_message1))
                ImageWithDescription(
                    painter =
                        (buildingTypeAndStatus.status?.icon ?: buildingTypeAndStatus.type?.icon)
                        ?.let { painterResource(it) },
                    title =
                        (buildingTypeAndStatus.status?.title ?: buildingTypeAndStatus.type?.title)
                        ?.let { stringResource(it) },
                    description =
                        (buildingTypeAndStatus.status?.description ?: buildingTypeAndStatus.type?.description)
                        ?.let { stringResource(it) },
                    imageSize = DpSize(48.dp, 48.dp),
                )
            }
        }
    )
}
