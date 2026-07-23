package de.westnordost.streetcomplete.overlays.address

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.create.createNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.address.Address
import de.westnordost.streetcomplete.osm.address.AddressForm
import de.westnordost.streetcomplete.osm.address.BlockAndHouseNumber
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.applyTo
import de.westnordost.streetcomplete.osm.address.parseAddressNumber
import de.westnordost.streetcomplete.osm.address.streetHouseNumber
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalLastMapClick
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerPixel
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.isArea
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AddressOverlayForm(
    on: (OverlayAction) -> Unit,
    element: Element?,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    onPinPosition: (icon: DrawableResource, position: LatLon?) -> Unit,
    mapDataWithEditsSource: MapDataWithEditsSource = koinInject(),
    nameSuggestionsSource: NameSuggestionsSource = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
) {
    // previous address and current address being input
    val originalAddress = remember(element) {
        Address(
            streetOrPlace =
                element?.tags?.get("addr:street")?.let { StreetName(it) }
                ?: element?.tags?.get("addr:place")?.let { PlaceName(it) }
                ?: if (lastWasPlaceName) PlaceName("") else StreetName(""),
            number = element?.tags?.let { parseAddressNumber(it) },
            name = element?.tags?.get("addr:housename")
        )
    }
    var address by rememberSerializable(originalAddress) {
        mutableStateOf(originalAddress)
    }
    // whether the button to switch between street name and place name is shown at all
    var showStreetOrPlaceSelect by rememberSaveable { mutableStateOf(lastWasPlaceName) }

    // adding an address at new node or (new) vertex of way. Get the building outlines only *once*
    // once the position is non-null
    val position = if (element == null) geometry.center else null
    val buildingOutlines = remember<Collection<Pair<Way, List<LatLon>>>?>(position != null) {
        position?.let {
            mapDataWithEditsSource.getBuildingOutlines(position.enclosingBoundingBox(100.0))
        }
    }
    val metersPerPixel = LocalMapMetersPerPixel.current
    val maxDistanceToCrosshair = metersPerPixel * 24.dp.toPx()
    val snapToVertexDistance = metersPerPixel * 12.dp.toPx()

    val positionOnWay = remember(position, buildingOutlines) {
        if (position == null) return@remember null
        if (buildingOutlines == null) return@remember null

        position.getPositionOnWays(
            ways = buildingOutlines,
            maxDistance = maxDistanceToCrosshair,
            snapToVertexDistance = snapToVertexDistance
        )
    }

    var addEntrance by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(positionOnWay, addEntrance) {
        onPinPosition(
            if (addEntrance) Res.drawable.quest_door else Res.drawable.quest_housenumber,
            positionOnWay?.position
        )
    }

    var confirmRemoveAddress by remember { mutableStateOf(false) }

    val mapClick = LocalLastMapClick.current
    LaunchedEffect(mapClick) {
        if (mapClick != null) {
            // only allow selection of street when that field is actually displayed
            if (address.streetOrPlace !is StreetName) return@LaunchedEffect

            val name = nameSuggestionsSource
                .getNames(mapClick.position, mapClick.clickAreaSizeInMeters, roadsWithNamesFilter)
                .firstOrNull()
                ?.find { it.languageTag.isEmpty() }
                ?.name
                ?.let { address = address.copy(streetOrPlace = StreetName(it)) }
        }
    }

    fun applyChanges(tagChanges: StringMapChangesBuilder) {
        address.applyTo(tagChanges, countryInfo.countryCode)
        tagChanges.remove("noaddress")
        tagChanges.remove("nohousenumber")
    }

    @Composable
    fun createOtherAnswers(): List<AnswerItem> {
        val result = ArrayList<AnswerItem>()

        result.add(AnswerItem(stringResource(Res.string.quest_address_answer_house_name2)) {
            address = address.copy(
                name = "",
                number = address.number?.takeIf { !it.isEmpty() }
            )
        })

        result.add(AnswerItem(stringResource(Res.string.quest_address_street_no_named_streets)) {
            address = address.copy(streetOrPlace = PlaceName(""))
            showStreetOrPlaceSelect = true
        })

        if (countryInfo.countryCode !in listOf("JP", "CZ", "SK")) {
            if (address.number is BlockAndHouseNumber) {
                result.add(AnswerItem(stringResource(Res.string.quest_address_answer_no_block2)) {
                    address = address.copy(number = HouseNumber(""))
                })
            } else {
                result.add(AnswerItem(stringResource(Res.string.quest_address_answer_block2)) {
                    address = address.copy(number = BlockAndHouseNumber("", ""))
                })
            }
        }

        if (element != null) {
            result.add(AnswerItem(stringResource(Res.string.quest_address_answer_no_address)) {
                confirmRemoveAddress = true
            })
        }

        if (element == null && addEntrance) {
            result.add(AnswerItem(stringResource(Res.string.overlay_addresses_no_entrance)) {
                addEntrance = false
            })
        }

        return result
    }

    OverlayForm(
        on = on,
        isComplete =
            // street is optional as in new developments sometimes the street names are not
            // posted yet, or it is not clear on-site, see #6528
            address.number?.isComplete() == true
            || address.name?.isNotEmpty() == true && address.number?.isEmpty() != false,
        hasChanges =
            originalAddress != address,
        onClickOk = {
            val number = address.number
            val name = address.name
            val streetOrPlace = address.streetOrPlace

            lastWasPlaceName = address.streetOrPlace is PlaceName
            number?.streetHouseNumber?.let { lastHouseNumber = it }
            lastBlock = if (number is BlockAndHouseNumber) number.block else null
            lastPlaceName = if (streetOrPlace is PlaceName) streetOrPlace.name else null
            lastStreetName = if (streetOrPlace is StreetName) streetOrPlace.name else null

            val tagChanges = StringMapChangesBuilder(element?.tags.orEmpty())
            val positionOnWay = positionOnWay

            // add/change address of existing element
            if (element != null) {
                applyChanges(tagChanges)
                on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
            }
            // add address to vertex or new vertex on way
            else if (positionOnWay != null) {
                val geometry = ElementPointGeometry(positionOnWay.position)
                val action = createNodeAction(positionOnWay, mapDataWithEditsSource) { tagChanges ->
                    applyChanges(tagChanges)
                    if (addEntrance && !tagChanges.containsKey("entrance")) {
                        tagChanges["entrance"] = "yes"
                    }
                }
                if (action != null) {
                    on(Edit(action))
                }
            }
            // add new address node
            else if (position != null) {
                applyChanges(tagChanges)
                on(Edit(CreateNodeAction(position, tagChanges)))
            }
        },
        label =
            // never show house number, as it already is shown in the form
            element?.let { nameAndLocationLabel(it, featureDictionary, showHouseNumber = false) },
        otherAnswers = ::createOtherAnswers
    ) {
        AddressForm(
            value = address,
            onValueChange = { address = it },
            countryCode = countryInfo.countryCode,
            showStreetOrPlaceSelect = showStreetOrPlaceSelect,
            streetNameSuggestion = lastStreetName,
            placeNameSuggestion = lastPlaceName,
            houseNumberSuggestion = lastHouseNumber,
            blockSuggestion = lastBlock,
        )
    }

    if (confirmRemoveAddress) {
        AreYouSureDialog(
            onDismissRequest = { confirmRemoveAddress = false },
            onConfirmed = { on(Edit(createRemoveAddressElementEditAction(element!!))) }
        )
    }
}

// only saved per application start
private var lastBlock: String? = null
private var lastHouseNumber: String? = null

private var lastPlaceName: String? = null
private var lastStreetName: String? = null

private var lastWasPlaceName: Boolean = false

private fun createRemoveAddressElementEditAction(element: Element): ElementEditAction {
    if (element is Node && element.tags.all { isAddressTag(it.key, it.value) }) {
        return DeletePoiNodeAction(element)
    }
    val tagChanges = StringMapChangesBuilder(element.tags)
    for (tag in tagChanges) {
        if (isAddressTag(tag.key, tag.value)) {
            tagChanges.remove(tag.key)
        }
    }
    // only add noaddress for areas (=buildings) because that's how it is defined in the wiki.
    // Address nodes will be deleted or the address removed (see above)
    if (element.isArea()) {
        tagChanges["noaddress"] = "yes"
    }

    return UpdateElementTagsAction(element, tagChanges.create())
}

private fun isAddressTag(key: String, value: String): Boolean =
    key.startsWith("addr:") ||
    key.startsWith("source:addr:") ||
    key.startsWith("note:addr:") ||
    key == "noaddress" ||
    key == "nohousenumber"


private val roadsWithNamesFilter by lazy {
    "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
        .toElementFilterExpression()
}

private val allBuildingsFilter by lazy {
    "ways, relations with building".toElementFilterExpression()
}

private fun MapDataWithEditsSource.getBuildingOutlines(
    bbox: BoundingBox,
): Collection<Pair<Way, List<LatLon>>> {
    val data = getMapDataWithGeometry(bbox)

    return data
        .filter(allBuildingsFilter)
        // we want the ways of the building relations, not the building relation itself
        .flatMap { element ->
            when (element) {
                is Relation -> {
                    element.members.asSequence()
                        .filter { it.type == ElementType.WAY }
                        .mapNotNull { data.getWay(it.ref) }
                }
                is Way -> sequenceOf(element)
                else -> sequenceOf()
            }
        }
        .map { way ->
            val positions = way.nodeIds.map { data.getNode(it)!!.position }
            way to positions
        }
        .toList()
}
