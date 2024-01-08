package de.westnordost.streetcomplete.overlays.address

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
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
import de.westnordost.streetcomplete.databinding.FragmentOverlayAddressBinding
import de.westnordost.streetcomplete.osm.address.AddressNumber
import de.westnordost.streetcomplete.osm.address.AddressNumberAndNameInputViewController
import de.westnordost.streetcomplete.osm.address.HouseAndBlockNumber
import de.westnordost.streetcomplete.osm.address.HouseNumberAndBlock
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameViewController
import de.westnordost.streetcomplete.osm.address.applyTo
import de.westnordost.streetcomplete.osm.address.createAddressNumber
import de.westnordost.streetcomplete.osm.address.streetHouseNumber
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.quests.road_name.RoadNameSuggestionsSource
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.isArea
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import org.koin.android.ext.android.inject

class AddressOverlayForm : AbstractOverlayForm(), IsMapPositionAware {

    override val contentLayoutResId = R.layout.fragment_overlay_address
    private val binding by contentViewBinding(FragmentOverlayAddressBinding::bind)

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val abbreviationsByLocale: AbbreviationsByLocale by inject()
    private val roadNameSuggestionsSource: RoadNameSuggestionsSource by inject()

    private lateinit var numberOrNameInputCtrl: AddressNumberAndNameInputViewController
    private lateinit var streetOrPlaceCtrl: StreetOrPlaceNameViewController

    private var addressNumber: AddressNumber? = null
    private var houseName: String? = null
    private var streetOrPlaceName: StreetOrPlaceName? = null

    private var isShowingHouseName: Boolean = false
    private var isShowingPlaceName: Boolean = false
    private var isShowingBlock: Boolean = false

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
            setMarkerIcon(if (addEntrance) R.drawable.ic_quest_door else R.drawable.ic_quest_housenumber)
        } else {
            setMarkerIcon(R.drawable.ic_quest_housenumber)
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

        addressNumber = element?.tags?.let { createAddressNumber(it) }
        houseName = element?.tags?.get("addr:housename")
        val placeName = element?.tags?.get("addr:place")
        val streetName = element?.tags?.get("addr:street")
        streetOrPlaceName = streetName?.let { StreetName(it) } ?: placeName?.let { PlaceName(it) }

        isShowingPlaceName = savedInstanceState?.getBoolean(SHOW_PLACE_NAME)
            ?: if (streetOrPlaceName == null) {
                lastWasPlace
            } else {
                placeName != null
            }
        isShowingHouseName = savedInstanceState?.getBoolean(SHOW_HOUSE_NAME) ?: (houseName != null)
        isShowingBlock = savedInstanceState?.getBoolean(SHOW_BLOCK)
            ?: addressNumber?.let { it is HouseNumberAndBlock } ?: (lastBlock != null)
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
            setTitleHintLabel(getNameAndLocationLabel(
                element, resources, featureDictionary,
                showHouseNumber = false
            ))
        }
        setMarkerIcon(R.drawable.ic_quest_housenumber)

        val streetOrPlaceBinding = binding.streetOrPlaceNameContainer
        streetOrPlaceCtrl = StreetOrPlaceNameViewController(
            select = streetOrPlaceBinding.streetOrPlaceSelect,
            placeNameInputContainer = streetOrPlaceBinding.placeNameInputContainer,
            placeNameInput = streetOrPlaceBinding.placeNameInput.apply { hint = lastPlaceName },
            streetNameInputContainer = streetOrPlaceBinding.streetNameInputContainer,
            streetNameInput = streetOrPlaceBinding.streetNameInput.apply { hint = lastStreetName },
            roadNameSuggestionsSource = roadNameSuggestionsSource,
            abbreviationsByLocale = abbreviationsByLocale,
            countryLocale = countryInfo.locale,
            startWithPlace = isShowingPlaceName
        )
        if (streetOrPlaceName != null) { // this changes back to street if it's null
            streetOrPlaceCtrl.streetOrPlaceName = streetOrPlaceName
        }
        streetOrPlaceCtrl.onInputChanged = { checkIsFormComplete() }

        // initially do not show the select for place name
        if (!isShowingPlaceName) {
            streetOrPlaceBinding.streetOrPlaceSelect.isGone = true
        }

        val layoutResId = getCountrySpecificAddressNumberLayoutResId(countryInfo.countryCode)
            ?: if (isShowingBlock) R.layout.view_house_number_and_block else R.layout.view_house_number
        showNumberOrNameInput(layoutResId)

        addEntrance = savedInstanceState?.getBoolean(ADD_ENTRANCE) ?: true
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
        val maxDistance = metersPerPixel * requireContext().dpToPx(12)
        val snapToVertexDistance = metersPerPixel * requireContext().dpToPx(8)
        positionOnWay = geometry.center.getPositionOnWays(buildings, maxDistance, snapToVertexDistance)
    }

    private fun showNumberOrNameInput(layoutResId: Int) {
        binding.addressNumberOrNameContainer.countrySpecificContainer.removeAllViews() // need to remove previous view
        val numberOrNameBinding = binding.addressNumberOrNameContainer
        val numberView = layoutInflater.inflate(
            layoutResId,
            numberOrNameBinding.countrySpecificContainer
        )
        val blockInput = numberView.findViewById<EditText?>(R.id.blockInput)

        numberOrNameInputCtrl = AddressNumberAndNameInputViewController(
            toggleHouseNameButton = numberOrNameBinding.toggleHouseNameButton,
            houseNameInput = numberOrNameBinding.houseNameInput,
            toggleAddressNumberButton = numberOrNameBinding.toggleAddressNumberButton,
            addressNumberContainer = numberOrNameBinding.addressNumberContainer,
            activity = requireActivity(),
            houseNumberInput = numberView.findViewById<EditText?>(R.id.houseNumberInput)?.apply { hint = lastHouseNumber },
            blockNumberInput = numberView.findViewById<EditText?>(R.id.blockNumberInput)?.apply { hint = lastBlockNumber },
            blockInput = blockInput?.apply { hint = lastBlock },
            conscriptionNumberInput = numberView.findViewById(R.id.conscriptionNumberInput),
            streetNumberInput = numberView.findViewById(R.id.streetNumberInput),
            toggleKeyboardButton = numberOrNameBinding.toggleKeyboardButton,
            addButton = numberView.findViewById(R.id.addButton),
            subtractButton = numberView.findViewById(R.id.subtractButton),
        )
        numberOrNameInputCtrl.addressNumber = addressNumber
        numberOrNameInputCtrl.houseName = houseName
        numberOrNameInputCtrl.onInputChanged = {
            streetOrPlaceCtrl.applyHint()
            checkIsFormComplete()
        }

        // initially do not show any house number / house name UI
        if (!isShowingHouseName) {
            numberOrNameBinding.toggleAddressNumberButton.isGone = true
            numberOrNameBinding.toggleHouseNameButton.isGone = true
        }
        isShowingBlock = blockInput != null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_PLACE_NAME, isShowingPlaceName)
        outState.putBoolean(SHOW_HOUSE_NAME, isShowingHouseName)
        outState.putBoolean(SHOW_BLOCK, isShowingBlock)
        outState.putBoolean(ADD_ENTRANCE, addEntrance)
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        return streetOrPlaceCtrl.selectStreetAt(position, clickAreaSizeInMeters)
    }

    override fun hasChanges(): Boolean =
        numberOrNameInputCtrl.addressNumber != addressNumber
        || numberOrNameInputCtrl.houseName != houseName
        || streetOrPlaceCtrl.streetOrPlaceName != streetOrPlaceName

    override fun isFormComplete(): Boolean =
        numberOrNameInputCtrl.isComplete && streetOrPlaceCtrl.streetOrPlaceName != null

    override fun onClickOk() {
        val number = numberOrNameInputCtrl.addressNumber
        val name = numberOrNameInputCtrl.houseName
        val streetOrPlaceName = streetOrPlaceCtrl.streetOrPlaceName
        lastWasPlace = streetOrPlaceName is PlaceName

        number?.streetHouseNumber?.let { lastHouseNumber = it }
        lastBlockNumber = if (number is HouseAndBlockNumber) number.blockNumber else null
        lastBlock = if (number is HouseNumberAndBlock) number.block else null
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

    /* --------------------------------- Show/Toggle block input -------------------------------- */

    private fun createBlockAnswerItem(): IAnswerItem? =
        if (getCountrySpecificAddressNumberLayoutResId(countryInfo.countryCode) == null) {
            if (isShowingBlock) {
                AnswerItem(R.string.quest_address_answer_no_block) { showNumberOrNameInput(R.layout.view_house_number) }
            } else {
                AnswerItem(R.string.quest_address_answer_block) { showNumberOrNameInput(R.layout.view_house_number_and_block) }
            }
        } else {
            null
        }

    /* ------------------------------ Show house name / place name ------------------------------ */

    private fun showHouseName() {
        isShowingHouseName = true
        binding.addressNumberOrNameContainer.toggleAddressNumberButton.isGone = false
        binding.addressNumberOrNameContainer.toggleHouseNameButton.isGone = false
        numberOrNameInputCtrl.setHouseNameViewExpanded(true)
        binding.addressNumberOrNameContainer.houseNameInput.requestFocus()
    }

    private fun showPlaceName() {
        isShowingPlaceName = true
        binding.streetOrPlaceNameContainer.streetOrPlaceSelect.isGone = false
        streetOrPlaceCtrl.selectPlaceName()
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
        private var lastBlockNumber: String? = null
        private var lastBlock: String? = null
        private var lastHouseNumber: String? = null
        private var lastPlaceName: String? = null
        private var lastStreetName: String? = null
        private var lastWasPlace: Boolean = false

        private const val SHOW_PLACE_NAME = "show_place_name"
        private const val SHOW_HOUSE_NAME = "show_house_name"
        private const val SHOW_BLOCK = "show_block_number"
        private const val ADD_ENTRANCE = "add_entrance"
    }
}

private fun addChanges(
    tagChanges: StringMapChangesBuilder,
    number: AddressNumber?,
    name: String?,
    streetOrPlaceName: StreetOrPlaceName?
) {
    number?.applyTo(tagChanges)
    name?.let { tagChanges["addr:housename"] = it }
    streetOrPlaceName?.applyTo(tagChanges)
    tagChanges.remove("noaddress")
    tagChanges.remove("nohousenumber")
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

private fun getCountrySpecificAddressNumberLayoutResId(countryCode: String): Int? = when (countryCode) {
    "JP" -> R.layout.view_house_number_japan
    "CZ" -> R.layout.view_house_number_czechia
    "SK" -> R.layout.view_house_number_slovakia
    else -> null
}
