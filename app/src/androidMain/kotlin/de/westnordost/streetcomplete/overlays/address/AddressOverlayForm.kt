package de.westnordost.streetcomplete.overlays.address

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.doOnLayout
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.create.createNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.address.AddressNumber
import de.westnordost.streetcomplete.osm.address.BlockAndHouseNumber
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameForm
import de.westnordost.streetcomplete.osm.address.applyTo
import de.westnordost.streetcomplete.osm.address.parseAddressNumber
import de.westnordost.streetcomplete.osm.address.streetHouseNumber
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.quests.address.AddressNumberAndName
import de.westnordost.streetcomplete.quests.address.AddressNumberAndNameForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.getNameAndLocationSpanned
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.isArea
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import org.koin.android.ext.android.inject

class AddressOverlayForm : AbstractOverlayForm(), IsMapPositionAware {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val nameSuggestionsSource: NameSuggestionsSource by inject()

    private val roadsWithNamesFilter =
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()

    private lateinit var originalAddressNumberAndName: AddressNumberAndName
    private lateinit var originalStreetOrPlaceName: StreetOrPlaceName

    private lateinit var addressNumberAndName: MutableState<AddressNumberAndName>
    private lateinit var streetOrPlaceName: MutableState<StreetOrPlaceName>
    private lateinit var showSelect: MutableState<Boolean>

    private var addEntrance: Boolean = true
        set(value) {
            field = value
            updateMarker()
        }

    private var positionOnWay: PositionOnWay? = null
        set(value) {
            field = value
            updateMarker()
        }
    private var buildings: Collection<Pair<Way, List<LatLon>>>? = null
    private val allBuildingsFilter = "ways, relations with building".toElementFilterExpression()

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

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_address_answer_house_name2) { showHouseName() },
        AnswerItem(R.string.quest_address_street_no_named_streets) { showPlaceName() },
        createBlockAnswerItem(),
        if (element != null) AnswerItem(R.string.quest_address_answer_no_address) { confirmRemoveAddress() } else null,
        if (element == null && addEntrance) AnswerItem(R.string.overlay_addresses_no_entrance) { addEntrance = false } else null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        originalAddressNumberAndName = AddressNumberAndName(
            number = element?.tags?.let { parseAddressNumber(it) },
            name = element?.tags?.get("addr:housename")
        )
        originalStreetOrPlaceName =
            element?.tags?.get("addr:street")?.let { StreetName(it) }
            ?: element?.tags?.get("addr:place")?.let { PlaceName(it) }
            ?: if (lastWasPlaceName) PlaceName("") else StreetName("")
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

        if (element != null) {
            setTitleHintLabel(getNameAndLocationSpanned(
                element, resources, featureDictionary,
                showHouseNumber = false
            ))
        }
        setMarkerIcon(R.drawable.quest_housenumber)

        binding.composeViewBase.content { Surface(Modifier.padding(bottom = 48.dp)) {
            addressNumberAndName = rememberSerializable { mutableStateOf(originalAddressNumberAndName) }
            streetOrPlaceName = rememberSerializable { mutableStateOf(originalStreetOrPlaceName) }
            showSelect = rememberSaveable { mutableStateOf(lastWasPlaceName) }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StreetOrPlaceNameForm(
                    value = streetOrPlaceName.value,
                    onValueChange = {
                        streetOrPlaceName.value = it
                        checkIsFormComplete()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    streetNameSuggestion = lastStreetName,
                    placeNameSuggestion = lastPlaceName,
                    showSelect = showSelect.value
                )
                AddressNumberAndNameForm(
                    value = addressNumberAndName.value,
                    onValueChange = {
                        addressNumberAndName.value = it

                        // apply suggestion
                        if (streetOrPlaceName.value.name.isEmpty()) {
                            when (streetOrPlaceName.value) {
                                is PlaceName -> {
                                    val name = lastPlaceName
                                    if (!name.isNullOrEmpty()) {
                                        streetOrPlaceName.value = PlaceName(name)
                                    }
                                }
                                is StreetName -> {
                                    val name = lastStreetName
                                    if (!name.isNullOrEmpty()) {
                                        streetOrPlaceName.value = StreetName(name)
                                    }
                                }
                            }
                        }
                        checkIsFormComplete()
                    },
                    countryCode = countryInfo.countryCode,
                    modifier = Modifier.fillMaxWidth(),
                    houseNumberSuggestion = lastHouseNumber,
                    blockSuggestion = lastBlock,
                )
            }
        } }
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkCurrentCursorPosition()
    }

    override fun onMapMoved(position: LatLon) {
        if (element != null) return
        checkCurrentCursorPosition()
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
        checkIsFormComplete()
        return true
    }

    override fun hasChanges(): Boolean =
        originalStreetOrPlaceName != streetOrPlaceName.value
        || originalAddressNumberAndName != addressNumberAndName.value

    override fun isFormComplete(): Boolean =
        addressNumberAndName.value.isComplete()
        // street is optional as in new developments sometimes the street names are not posted yet,
        // or it is not clear on-site, see #6528

    override fun onClickOk() {
        val number = addressNumberAndName.value.number
        val name = addressNumberAndName.value.name
        val streetOrPlaceName = streetOrPlaceName.value

        lastWasPlaceName = streetOrPlaceName is PlaceName
        number?.streetHouseNumber?.let { lastHouseNumber = it }
        lastBlock = if (number is BlockAndHouseNumber) number.block else null
        lastPlaceName = if (streetOrPlaceName is PlaceName) streetOrPlaceName.name else null
        lastStreetName = if (streetOrPlaceName is StreetName) streetOrPlaceName.name else null

        val element = element
        val positionOnWay = positionOnWay

        if (element != null) {
            val tagChanges = StringMapChangesBuilder(element.tags)
            addChanges(tagChanges, number, name, streetOrPlaceName)
            applyEdit(UpdateElementTagsAction(element, tagChanges.create()))
        } else if (positionOnWay != null) {
            val geometry = ElementPointGeometry(positionOnWay.position)
            val action = createNodeAction(positionOnWay, mapDataWithEditsSource) { tagChanges ->
                addChanges(tagChanges, number, name, streetOrPlaceName)
                if (addEntrance && !tagChanges.containsKey("entrance")) tagChanges["entrance"] = "yes"
            } ?: return

            applyEdit(action, geometry)
        } else {
            val tagChanges = StringMapChangesBuilder(mapOf())
            addChanges(tagChanges, number, name, streetOrPlaceName)
            applyEdit(CreateNodeAction(geometry.center, tagChanges))
        }
    }

    private fun addChanges(
        tagChanges: StringMapChangesBuilder,
        number: AddressNumber?,
        name: String?,
        streetOrPlaceName: StreetOrPlaceName?
    ) {
        number?.applyTo(tagChanges, countryInfo.countryCode)
        if (!name.isNullOrEmpty()) {
            tagChanges["addr:housename"] = name
        }
        streetOrPlaceName?.applyTo(tagChanges)
        tagChanges.remove("noaddress")
        tagChanges.remove("nohousenumber")
    }

    /* --------------------------------- Show/Toggle block input -------------------------------- */

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

    /* ------------------------------ Show house name / place name ------------------------------ */

    private fun showHouseName() {
        addressNumberAndName.value = AddressNumberAndName(
            name = "",
            number = addressNumberAndName.value.number?.takeIf { !it.isEmpty() }
        )
    }

    private fun showPlaceName() {
        streetOrPlaceName.value = PlaceName("")
        showSelect.value = true
    }

    /* -------------------------------------- Remove address ------------------------------------ */

    private fun confirmRemoveAddress() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                applyEdit(createRemoveAddressElementEditAction(element!!))
            }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    companion object {
        private var lastBlock: String? = null
        private var lastHouseNumber: String? = null

        private var lastPlaceName: String? = null
        private var lastStreetName: String? = null

        private var lastWasPlaceName: Boolean = false
    }
}

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
