package de.westnordost.streetcomplete.overlays.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.osm.address.AddressNumberAndNameForm
import de.westnordost.streetcomplete.osm.address.BlockAndHouseNumber
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameForm
import de.westnordost.streetcomplete.osm.address.applyTo
import de.westnordost.streetcomplete.osm.address.parseAddressNumber
import de.westnordost.streetcomplete.osm.address.streetHouseNumber
import de.westnordost.streetcomplete.quests.address.AddressNumberAndName
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.isArea
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.collections.iterator

@Composable
fun AddressOverlayForm(
    on: (OverlayAction) -> Unit,
    element: Element?,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    mapDataWithEditsSource: MapDataWithEditsSource = koinInject(),
    nameSuggestionsSource: NameSuggestionsSource = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
) {
    val originalStreetOrPlaceName = remember(element) {
        element?.tags?.get("addr:street")?.let { StreetName(it) }
            ?: element?.tags?.get("addr:place")?.let { PlaceName(it) }
            ?: if (lastWasPlaceName) PlaceName("") else StreetName("")
    }

    val originalAddressNumberAndName = remember(element) {
        AddressNumberAndName(
            number = element?.tags?.let { parseAddressNumber(it) },
            name = element?.tags?.get("addr:housename")
        )
    }

    var addressNumberAndName by rememberSerializable(originalAddressNumberAndName) {
        mutableStateOf(originalAddressNumberAndName)
    }
    var streetOrPlaceName by rememberSerializable(originalStreetOrPlaceName) {
        mutableStateOf(originalStreetOrPlaceName)
    }
    var showSelect by rememberSaveable { mutableStateOf(lastWasPlaceName) }

    var confirmRemoveAddress by remember { mutableStateOf(false) }

    fun applyChanges(tagChanges: StringMapChangesBuilder) {
        addressNumberAndName.number?.applyTo(tagChanges, countryInfo.countryCode)
        val name = addressNumberAndName.name
        if (!name.isNullOrEmpty()) {
            tagChanges["addr:housename"] = name
        }
        streetOrPlaceName.applyTo(tagChanges)
        tagChanges.remove("noaddress")
        tagChanges.remove("nohousenumber")
    }

    @Composable
    fun createOtherAnswers(): List<AnswerItem> {
        val result = ArrayList<AnswerItem>()

        result.add(AnswerItem(stringResource(Res.string.quest_address_answer_house_name2)) {
            addressNumberAndName = AddressNumberAndName(
                name = "",
                number = addressNumberAndName.number?.takeIf { !it.isEmpty() }
            )
        })

        result.add(AnswerItem(stringResource(Res.string.quest_address_street_no_named_streets)) {
            streetOrPlaceName = PlaceName("")
            showSelect = true
        })

        if (countryInfo.countryCode !in listOf("JP", "CZ", "SK")) {
            if (addressNumberAndName.number is BlockAndHouseNumber) {
                result.add(AnswerItem(stringResource(Res.string.quest_address_answer_no_block2)) {
                    addressNumberAndName = addressNumberAndName.copy(number = HouseNumber(""))
                })
            } else {
                result.add(AnswerItem(stringResource(Res.string.quest_address_answer_block2)) {
                    addressNumberAndName =
                        addressNumberAndName.copy(number = BlockAndHouseNumber("", ""))
                })
            }
        }

        if (element != null) {
            result.add(AnswerItem(stringResource(Res.string.quest_address_answer_no_address)) {
                confirmRemoveAddress = true
            })
        }

        // TODO compose-quest-form position on way stuff
        /*
        if (element == null && addEntrance) {
            result.add(Answer(stringResource(Res.string.overlay_addresses_no_entrance)) {
                addEntrance = false
            })
        }
        */

        return result
    }

    OverlayForm(
        isComplete =
            // street is optional as in new developments sometimes the street names are not
            // posted yet, or it is not clear on-site, see #6528
            addressNumberAndName.isComplete(),
        hasChanges =
            originalStreetOrPlaceName != streetOrPlaceName
            || originalAddressNumberAndName != addressNumberAndName,
        onClickOk = {
            val number = addressNumberAndName.number
            val name = addressNumberAndName.name

            lastWasPlaceName = streetOrPlaceName is PlaceName
            number?.streetHouseNumber?.let { lastHouseNumber = it }
            lastBlock = if (number is BlockAndHouseNumber) number.block else null
            lastPlaceName = if (streetOrPlaceName is PlaceName) streetOrPlaceName.name else null
            lastStreetName = if (streetOrPlaceName is StreetName) streetOrPlaceName.name else null

            if (element != null) {
                val tagChanges = StringMapChangesBuilder(element.tags)
                applyChanges(tagChanges)
                on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
            }
            // TODO compose-quest-form position on way stuff
            /*
            else if (positionOnWay != null) {
                val geometry = ElementPointGeometry(positionOnWay.position)
                val action = createNodeAction(positionOnWay, mapDataWithEditsSource) { tagChanges ->
                    addChanges(tagChanges)
                    if (addEntrance && !tagChanges.containsKey("entrance")) {
                        tagChanges["entrance"] = "yes"
                    }
                }
                if (action != null) {
                    on(Edit(action))
                }
            }
             */
            else {
                val tagChanges = StringMapChangesBuilder(mapOf())
                applyChanges(tagChanges)
                on(Edit(CreateNodeAction(geometry.center, tagChanges)))
            }
        },
        on = on,
        label =
            // never show house number, as it already is shown in the form
            element?.let { nameAndLocationLabel(it, featureDictionary, showHouseNumber = false) },
        otherAnswers = ::createOtherAnswers
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StreetOrPlaceNameForm(
                value = streetOrPlaceName,
                onValueChange = { streetOrPlaceName = it },
                modifier = Modifier.fillMaxWidth(),
                streetNameSuggestion = lastStreetName,
                placeNameSuggestion = lastPlaceName,
                showSelect = showSelect
            )
            AddressNumberAndNameForm(
                value = addressNumberAndName,
                onValueChange = {
                    addressNumberAndName = it

                    // apply suggestion
                    if (streetOrPlaceName.name.isEmpty()) {
                        when (streetOrPlaceName) {
                            is PlaceName -> {
                                val name = lastPlaceName
                                if (!name.isNullOrEmpty()) {
                                    streetOrPlaceName = PlaceName(name)
                                }
                            }

                            is StreetName -> {
                                val name = lastStreetName
                                if (!name.isNullOrEmpty()) {
                                    streetOrPlaceName = StreetName(name)
                                }
                            }
                        }
                    }
                },
                countryCode = countryInfo.countryCode,
                modifier = Modifier.fillMaxWidth(),
                houseNumberSuggestion = lastHouseNumber,
                blockSuggestion = lastBlock,
            )
        }
    }

    if (confirmRemoveAddress) {
        QuestConfirmationDialog(
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


// TODO compose-quest-form position on way stuff
/*
    private var buildings: Collection<Pair<Way, List<LatLon>>>? = null

    private val roadsWithNamesFilter =
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()

    private val allBuildingsFilter = "ways, relations with building".toElementFilterExpression()


    private var positionOnWay: PositionOnWay? = null
        set(value) {
            field = value
            updateMarker()
        }

    private var addEntrance: Boolean = true
        set(value) {
            field = value
            updateMarker()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val element = element
        if (element == null) {
            view.doOnLayout {
                initCreatingPointOnWay()
                checkCurrentCursorPosition()
            }
        }

        setMarkerIcon(R.drawable.quest_housenumber)
    }

    private fun initCreatingPointOnWay() {
        val data = mapDataWithEditsSource.getMapDataWithGeometry(geometry.center.enclosingBoundingBox(100.0))
        buildings = data
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

    private fun updateMarker() {
        val positionOnWay = positionOnWay
        if (positionOnWay != null) {
            setMarkerPosition(positionOnWay.position)
            setMarkerIcon(if (addEntrance) R.drawable.quest_door else R.drawable.quest_housenumber)
        } else {
            setMarkerIcon(R.drawable.quest_housenumber)
            setMarkerPosition(null)
        }
    }

    private fun checkCurrentCursorPosition() {
        val buildings = buildings ?: return
        val metersPerPixel = metersPerPixel ?: return
        val maxDistance = metersPerPixel * resources.dpToPx(12)
        val snapToVertexDistance = metersPerPixel * resources.dpToPx(8)
        positionOnWay = geometry.center.getPositionOnWays(buildings, maxDistance, snapToVertexDistance)
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        if (streetOrPlaceName.value !is StreetName) return false

        val name = nameSuggestionsSource
            .getNames(position, clickAreaSizeInMeters, roadsWithNamesFilter)
            .firstOrNull()
            ?.find { it.languageTag.isEmpty() }
            ?.name
            // still consume event even when there is no named road at this position
            ?: return true

        streetOrPlaceName.value = StreetName(name)
        return true
    }

 */
