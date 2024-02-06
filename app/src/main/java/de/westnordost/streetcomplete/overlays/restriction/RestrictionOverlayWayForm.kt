package de.westnordost.streetcomplete.overlays.restriction

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.CreateRelationAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeleteRelationAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.createChanges
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.RelationMember
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.databinding.FragmentOverlayRestrictionWayBinding
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.quests.max_weight.ImperialPounds
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSign
import de.westnordost.streetcomplete.quests.max_weight.MetricTons
import de.westnordost.streetcomplete.quests.max_weight.ShortTons
import de.westnordost.streetcomplete.quests.max_weight.asItem
import de.westnordost.streetcomplete.quests.max_weight.getLayoutResourceId
import de.westnordost.streetcomplete.quests.max_weight.osmKey
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.containsAny
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.firstAndLast
import de.westnordost.streetcomplete.util.ktx.showKeyboard
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.distanceToArcs
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.finalBearingTo
import de.westnordost.streetcomplete.util.dialogs.showAddConditionalDialog
import de.westnordost.streetcomplete.util.dialogs.showOtherConditionalDialog
import de.westnordost.streetcomplete.view.ArrayImageAdapter
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item2
import de.westnordost.streetcomplete.view.inputfilter.acceptDecimalDigits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

// todo
//  save instance state
//   save selection mode, selected restriction, current restriction
//  show anything if there is no restriction? looks awfully empty
//  more restriction types like oneway, length, height
//  don't allow adding turn restriction if none can be added (much work for little gain)
//  allow setting via ways, and allow choosing via node if from and to are the same
//   often needed for no_u_turn, but might be much work
//  allow adding conditional-only restriction for weight (works for turn only)
//  form grows too high with many restrictions (maybe scrollview?)
class RestrictionOverlayWayForm : AbstractOverlayForm() {

    private val mapDataSource: MapDataWithEditsSource by inject()
    private val mapFragment by lazy {
        (parentFragment as? MainFragment)?.childFragmentManager?.fragments?.filterIsInstance<MainMapFragment>()?.singleOrNull()
    }
    override val contentLayoutResId = R.layout.fragment_overlay_restriction_way
    private val binding by contentViewBinding(FragmentOverlayRestrictionWayBinding::bind)
    private val maxWeightInput: EditText? get() = binding.maxWeightContainer.findViewById(R.id.maxWeightInput)
    private val weightUnitSelect: Spinner? get() = binding.maxWeightContainer.findViewById(R.id.weightUnitSelect)

    private val originalRestrictions by lazy {
        val turnRestrictions = mapDataSource.getRelationsForWay(element!!.id).filter { it.tags["type"] == "restriction" }
            .map { TurnRestriction(it) }
        val weightRestrictions = getWeightRestrictions(element as Way)
        turnRestrictions + weightRestrictions
    }

    // unchanged restriction from originalRestrictions
    private var selectedRestriction: Restriction? = null
        set(value) {
            field = value
            showOtherRestrictionsList()
            if (value != null)
                currentRestriction = value
        }

    // (currently) can't be set to null
    private var currentRestriction: Restriction? = null
        set(value) {
            if (field == value) return
            val oldValue = field
            field = value
            checkIsFormComplete()
            // can't add restriction if sth is changed
            if (field != selectedRestriction)
                binding.addRestriction.isGone = true
            when (value) {
                is WeightRestriction -> {
                    if (oldValue is WeightRestriction && oldValue.way == value.way && oldValue.sign == value.sign)
                        // no need to change form if only weight changed
                        // especially reloading the input while typing is annoying!
                        return
                    showWeightRestrictionUi(value)
                    via = null
                    mapFragment?.highlightGeometry(geometry)
                }
                is TurnRestriction -> {
                    if (value.relation.isSupportedTurnRestriction()) {
                        showFullTurnRestrictionUi(value)
                        showTurnRestrictionOnMap(value)
                    } else {
                        showUnsupportedTurnRestriction(value)
                    }
                    getGeometry(value.relation)?.let { mapFragment?.highlightGeometry(it) }
                }
                null -> { } // should not happen
            }
            binding.conditionalButton.isVisible = true
            if (value is TurnRestriction && value.relation.id == 0L)
                binding.removeRestriction.isInvisible = true
            else binding.removeRestriction.isVisible = true
        }

    // only used for turn restriction
    private var via: Pair<LatLon, Double>? = null
        set(value) {
            field?.first?.let { mapFragment?.deleteMarkerForCurrentHighlighting(ElementPointGeometry(it)) }
            field = value
            val tags = (currentRestriction as? TurnRestriction)?.relation?.tags ?: emptyMap()
            val icon = getIconForTurnRestriction(tags.getShortRestrictionValue() ?: "")
            value?.let { mapFragment?.putMarkerForCurrentHighlighting(ElementPointGeometry(it.first), icon, null, null, it.second) }
        }

    // enabled when adding turn restriction, cannot be disabled
    private var turnRestrictionSelectionMode: Boolean = false
        set(value) {
            field = value
            if (value) {
                mapFragment?.hideOverlay()
                mapFragment?.highlightGeometry(geometry) // highlight initially selected way only
                binding.turnRestrictionContainer.isVisible = true
                binding.maxWeightContainer.isGone = true
                binding.exceptions.text = getString(R.string.restriction_overlay_exceptions, getString(R.string.overlay_none))
                binding.infoText.setText(R.string.restriction_overlay_select_way)
                binding.infoText.isVisible = true
                binding.addRestriction.isGone = true
            }
        }

    override val otherAnswers get() = listOfNotNull(
        relationDetailsAnswer(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { originalRestrictions } // load restrictions in background, so ui thread needs to wait less
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (originalRestrictions.isNotEmpty() && selectedRestriction == null) {
            selectedRestriction = getInitialRestriction()
        }
        binding.addRestriction.setOnClickListener { onClickAddRestriction() }

        binding.turnRestrictionTypeSpinner.adapter = ArrayImageAdapter(requireContext(), turnRestrictionTypeList.map { getIconForTurnRestriction(it) }, 80)
        binding.turnRestrictionTypeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val oldRestriction = currentRestriction as? TurnRestriction ?: return
                val newTags = oldRestriction.relation.tags.toMutableMap()
                val conditionalKey = if (newTags.containsKey("restriction:conditional")) "restriction:conditional"
                    else newTags.keys.firstOrNull { it.startsWith("restriction:") && it.endsWith(":conditional") }
                if (conditionalKey != null && !newTags.containsKey(conditionalKey.substringBefore(":conditional"))) {
                    val old = newTags[conditionalKey]!!.substringBefore("@").trim()
                    newTags[conditionalKey] = newTags[conditionalKey]!!.replace(old, turnRestrictionTypeList[p2])
                } else {
                    if (newTags.containsKey("restriction"))
                        newTags["restriction"] = turnRestrictionTypeList[p2]
                    else {
                        val k = newTags.keys.firstOrNull { key -> key.startsWith("restriction:") && onlyTurnRestriction.any { key.endsWith(it) } }
                            ?: "restriction"
                        newTags[k] = turnRestrictionTypeList[p2]
                    }
                }
                if (newTags != oldRestriction.relation.tags)
                    currentRestriction = TurnRestriction(oldRestriction.relation.copy(tags = newTags))
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }
        binding.implicitSwitch.isChecked = true
        binding.removeRestriction.setOnClickListener { onClickedDelete() }
    }

    private fun getInitialRestriction(): Restriction? {
        // prefer supported and complete turn restrictions
        originalRestrictions
            .firstOrNull { it is TurnRestriction && it.relation.isSupportedTurnRestriction() && it.relation.isRelationComplete() }
            ?.let { return it }

        // then weight restriction
        originalRestrictions.firstOrNull { it is WeightRestriction }?.let { return it }

        // just take the first one
        return originalRestrictions.firstOrNull()
    }

    private fun showOtherRestrictionsList() {
        val restrictions = originalRestrictions.filterNot { it == selectedRestriction }
        if (restrictions.isEmpty()) return // if we show it once, no need to hide again
        binding.otherRestrictions.isVisible = true
        binding.otherRestrictions.removeAllViews()
        binding.otherRestrictions.addView(TextView(requireContext()).apply {
            setText(R.string.restriction_overlay_other_restrictions)
        })
        for (restriction in restrictions) {
            binding.otherRestrictions.addView(Button(requireContext()).apply {
                text = when (restriction) {
                    is TurnRestriction -> restriction.relation.members.filter { it.type == ElementType.WAY && it.ref == element!!.id }
                        .joinToString(", ") { it.role }
                    is WeightRestriction -> restriction.weight
                }
                val drawable = restriction.getDrawable(layoutInflater, countryInfo)
                val height = context.dpToPx(56).toInt()
                val resizedDrawable = drawable
                    ?.createBitmap(height, drawable.intrinsicWidth * height / drawable.intrinsicHeight)
                    ?.toDrawable(context.resources)

                setCompoundDrawablesWithIntrinsicBounds(resizedDrawable, null, null, null)
                setOnClickListener { selectedRestriction = restriction }
            })
        }
    }

    override fun hasChanges(): Boolean =
        currentRestriction != null && currentRestriction != selectedRestriction

    override fun isFormComplete(): Boolean {
        val restriction = currentRestriction ?: return false
        if (!hasChanges()) return false
        if (restriction is WeightRestriction && restriction.weight.isBlank()) return false
        return true
    }

    override fun onClickOk() {
        val restriction = currentRestriction ?: return
        when (restriction) {
            is WeightRestriction -> {
                val input = restriction.weight.toDouble()
                val weight = when (countryInfo.weightLimitUnits[weightUnitSelect?.selectedItemPosition ?: 0]) {
                    WeightMeasurementUnit.SHORT_TON  -> ShortTons(input)
                    WeightMeasurementUnit.POUND      -> ImperialPounds(input.toInt())
                    WeightMeasurementUnit.METRIC_TON -> MetricTons(input)
                }
                val changes = restriction.way.tags.createChanges(element!!.tags)
                changes[restriction.sign.osmKey] = weight.toString()
                applyEdit(UpdateElementTagsAction(restriction.way, changes.create()))
            }
            is TurnRestriction -> {
                val rel = restriction.relation
                val geometry = getGeometry(rel) ?: geometry
                if (rel.id == 0L) {
                    applyEdit(CreateRelationAction(rel.tags, rel.members), geometry)
                } else {
                    val oldRelation = (selectedRestriction as? TurnRestriction)?.relation ?: return
                    applyEdit(UpdateElementTagsAction(oldRelation, rel.tags.createChanges(oldRelation.tags).create()), geometry)
                }
            }
        }
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        if (!turnRestrictionSelectionMode) return false
        val bbox = position.enclosingBoundingBox(clickAreaSizeInMeters.coerceAtLeast(10.0))
        val data = mapDataSource.getMapDataWithGeometry(bbox)
        val initialWay = element as Way // this must be correct

        // first and last nodes, but only if they are shared by at least 3 roads (otherwise a restriction doesn't make sense)
        val firstAndLastNodes = initialWay.nodeIds.firstAndLast().sorted().filter { mapDataSource.getWaysForNode(it).count { it.tags["highway"] in ALL_ROADS } > 2 }
        if (firstAndLastNodes.isEmpty()) return true
        val eligibleWays = data.ways.mapNotNull {
            if (it.id == initialWay.id || it.isClosed) return@mapNotNull null
            if (it.tags["highway"] !in ALL_ROADS) return@mapNotNull null
            val fl = it.nodeIds.firstAndLast() // exactly one of first and last nodes need to be shared
            if (!fl.containsAny(firstAndLastNodes)) return@mapNotNull null
            val geometry = data.getWayGeometry(it.id) as? ElementPolylinesGeometry ?: return@mapNotNull null
            it to geometry
        }

        val otherWay = eligibleWays.minByOrNull { position.distanceToArcs(it.second.polylines.single()) }?.first ?: return true
        val initialWayAsMember = RelationMember(initialWay.type, initialWay.id, "from")
        val otherWayAsMember = RelationMember(otherWay.type, otherWay.id, "to")
        // ignore ways that have same start and end points
        val viaNode = otherWay.nodeIds.firstAndLast().singleOrNull { it in firstAndLastNodes }?.let { mapDataSource.getNode(it) } ?: return true
        val viaNodeAsMember = RelationMember(viaNode.type, viaNode.id, "via")
        val newTags = (currentRestriction as? TurnRestriction)?.relation?.tags?.toMutableMap() ?: mutableMapOf()
        newTags["type"] = "restriction"
        if (!binding.implicitSwitch.isChecked)
            newTags["explicit"] = "yes"
        newTags["restriction"] = turnRestrictionTypeList[binding.turnRestrictionTypeSpinner.selectedItemPosition]
        val newRelation = Relation(0L, listOf(initialWayAsMember, otherWayAsMember, viaNodeAsMember), newTags)
        binding.swapFromToRoles.isVisible = true
        currentRestriction = TurnRestriction(newRelation)
        return true
    }

    private fun onClickAddRestriction() {
        val res = listOf(
            Item2(RestrictionType.TURN, ResImage(R.drawable.ic_overlay_restriction)),
            Item2(RestrictionType.WEIGHT, ResImage(R.drawable.ic_quest_max_weight)),
        )
        ImageListPickerDialog(requireContext(), res) {
            when (it.value) {
                RestrictionType.TURN -> turnRestrictionSelectionMode = true
                RestrictionType.WEIGHT -> {
                    val items = MaxWeightSign.values().mapNotNull { sign ->
                        if (originalRestrictions.any { it is WeightRestriction && it.sign == sign })
                                null
                            else sign.asItem(layoutInflater, countryInfo.countryCode)
                    }
                    ImageListPickerDialog(requireContext(), items) { sign ->
                        currentRestriction = WeightRestriction(element as Way, sign.value!!, "")
                        val units = countryInfo.weightLimitUnits.map { it.displayString }
                        weightUnitSelect?.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_centered, units)
                        weightUnitSelect?.setSelection(0)

                        viewLifecycleScope.launch {
                            delay(20)
                            maxWeightInput?.requestFocus()
                            maxWeightInput?.showKeyboard()
                        }
                    }.show()
                }
                null -> { }
            }
        }.show()
    }

    private fun displayConditionalRestrictions(text: String) {
        if (text.isNotBlank()) {
            binding.infoText.text = text
            binding.infoText.isVisible = true
            binding.conditionalButton.setText(R.string.restriction_overlay_remove_conditional_restrictions)
            binding.conditionalButton.setOnClickListener { onClickRemoveConditional() }
        } else {
            binding.infoText.isGone = true
            binding.conditionalButton.setOnClickListener { onClickAddConditional() }
            binding.conditionalButton.setText(R.string.access_manager_button_add_conditional)
        }
    }

    private fun onClickRemoveConditional() {
        val restriction = currentRestriction ?: return
        when (restriction) {
            is TurnRestriction -> {
                val newTags = restriction.relation.tags.toMutableMap()
                val oldConditionalKey = if (newTags.containsKey("restriction:conditional")) "restriction:conditional"
                    else newTags.keys.firstOrNull { it.startsWith("restriction:") && it.endsWith(":conditional") } ?: "restriction:conditional"
                if (!newTags.containsKey(oldConditionalKey.substringBefore(":conditional")))
                    newTags[oldConditionalKey.substringBefore(":conditional")] = newTags.getShortRestrictionValue()!!
                newTags.remove(oldConditionalKey)
                currentRestriction = TurnRestriction(restriction.relation.copy(tags = newTags))
            }
            is WeightRestriction -> {
                val newTags = restriction.way.tags.toMutableMap()
                val weightKey = restriction.sign.osmKey
                newTags.remove("$weightKey:conditional")
                currentRestriction = WeightRestriction(restriction.way.copy(tags = newTags), restriction.sign, restriction.weight)
            }
        }
    }

    private fun onClickAddConditional() {
        val restriction = currentRestriction ?: return
        when (restriction) {
            is TurnRestriction -> {
                val newTags = restriction.relation.tags.toMutableMap()
                // either it's only conditional, then value is same as restriction, or it's an exception then it's "none"
                val restrictionKey = if (newTags.containsKey("restriction")) "restriction"
                    else newTags.keys.firstOrNull { key -> onlyTurnRestriction.any { key =="restriction:$it" } } ?: "restriction"
                val values = newTags[restrictionKey]?.let { listOf(it, "none") }
                    ?: listOf(turnRestrictionTypeList[binding.turnRestrictionTypeSpinner.selectedItemPosition])
                showAddConditionalDialog(requireContext(), listOf("$restrictionKey:conditional"), values, null) { _, v ->
                    newTags["$restrictionKey:conditional"] = v
                    if (!v.startsWith("none")) newTags.remove(restrictionKey)
                    currentRestriction = TurnRestriction(restriction.relation.copy(tags = newTags))
                }
            }
            is WeightRestriction -> {
                val weightKey = restriction.sign.osmKey
                val newTags = restriction.way.tags.toMutableMap()
                // no values because it needs to be free-form (enter weight, maybe unit for some country, also none
                // -> can't set number-only input type
                showOtherConditionalDialog(requireContext(), listOf("$weightKey:conditional"), null, null) { _, v ->
                    newTags["$weightKey:conditional"] = v
                    if (!v.startsWith("none")) newTags.remove(weightKey)
                    currentRestriction = WeightRestriction(restriction.way.copy(tags = newTags), restriction.sign, restriction.weight)
                }
            }
        }
    }

    private fun onClickedDelete() {
        val restriction = currentRestriction ?: return
        when (restriction) {
            is TurnRestriction -> {
                if (restriction.relation.id == 0L) return
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.quest_generic_confirmation_title)
                    .setPositiveButton(R.string.osm_element_gone_confirmation) { _, _ ->
                        applyEdit(DeleteRelationAction(restriction.relation), getGeometry(restriction.relation) ?: geometry)
                    }
                    .setNeutralButton(R.string.leave_note) { _, _ -> composeNote(restriction.relation) }
                    .show()
            }
            is WeightRestriction -> {
                // delete this and conditional tags, but only apply if there are actually changes
                val changes = restriction.way.tags.createChanges(element!!.tags)
                changes.remove(restriction.sign.osmKey)
                changes.keys.filter { it.startsWith("${restriction.sign.osmKey}:") }.forEach {
                    changes.remove(it)
                }
                if (!changes.hasChanges) return

                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.quest_generic_confirmation_title)
                    .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                        applyEdit(UpdateElementTagsAction(restriction.way, changes.create()))
                    }
                    .setNeutralButton(R.string.leave_note) { _, _ -> composeNote(restriction.way) }
                    .show()
            }
        }
    }

    // ---------------- weight restriction ----------------------

    private fun showWeightRestrictionUi(restriction: WeightRestriction) {
        binding.turnRestrictionContainer.isGone = true
        binding.maxWeightContainer.isVisible = true
        binding.maxWeightContainer.removeAllViews()
        val item = restriction.sign.asItem(layoutInflater, countryInfo.countryCode)
        layoutInflater.inflate(item.value!!.getLayoutResourceId(countryInfo.countryCode), binding.maxWeightContainer)
        val units = countryInfo.weightLimitUnits
        weightUnitSelect?.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_centered, units.map { it.displayString })
        weightUnitSelect?.setSelection(0)
        if (restriction.weight.toDoubleOrNull() == null) {
            when {
                restriction.weight.replace(',', '.').toDoubleOrNull() != null ->
                    maxWeightInput?.setText(restriction.weight.replace(',', '.'))
                restriction.weight.endsWith("lbs") -> {
                    val w = restriction.weight.substringBefore("lbs").trim()
                    if (w.toDoubleOrNull() != null) {
                        maxWeightInput?.setText(w)
                        val idx = units.indexOfFirst { it == WeightMeasurementUnit.POUND }
                        if (idx != -1)
                            weightUnitSelect?.setSelection(idx)
                    }
                }
                restriction.weight.endsWith("st") -> {
                    val w = restriction.weight.substringBefore("st").trim()
                    if (w.toDoubleOrNull() != null) {
                        maxWeightInput?.setText(w)
                        val idx = units.indexOfFirst { it == WeightMeasurementUnit.SHORT_TON }
                        if (idx != -1)
                            weightUnitSelect?.setSelection(idx)
                    }
                }
                restriction.weight.endsWith("t") -> {
                    val w = restriction.weight.substringBefore("t").trim()
                    if (w.toDoubleOrNull() != null)
                        maxWeightInput?.setText(w)
                }
                else -> { } // don't fill unrecognized values
            }
        } else maxWeightInput?.setText(restriction.weight)
        maxWeightInput?.filters = arrayOf(acceptDecimalDigits(6, 2))
        binding.maxWeightContainer.setOnClickListener {
            maxWeightInput?.requestFocus()
            maxWeightInput?.showKeyboard()
        }
        maxWeightInput?.doAfterTextChanged {
            currentRestriction = WeightRestriction(restriction.way, restriction.sign, it.toString())
        }

        // show other restrictions based on this maxweight key
        val restrictionInfo = restriction.way.tags
            .filterKeys { it.startsWith("${restriction.sign.osmKey}:") }
            .map { "${it.key} = ${it.value}" }
            .joinToString("\n")
        displayConditionalRestrictions(restrictionInfo)

        // todo: only-button for maxweight:hgv and stuff
        //  but needs to work a bit different than for turn because of the maxweight keys
    }

    // ---------------- turn restriction ----------------------

    private fun showFullTurnRestrictionUi(restriction: TurnRestriction) {
        binding.turnRestrictionContainer.isVisible = true
        binding.maxWeightContainer.isGone = true

        // set up switch
        binding.implicitSwitch.isChecked = restriction.relation.tags["implicit"] != "yes"
        binding.implicitSwitch.setOnCheckedChangeListener { _, b ->
            val oldRestriction = currentRestriction as? TurnRestriction ?: return@setOnCheckedChangeListener
            val newTags = oldRestriction.relation.tags.toMutableMap()
            if (b)
                newTags.remove("implicit")
            else newTags["implicit"] = "yes"
            currentRestriction = TurnRestriction(oldRestriction.relation.copy(tags = newTags))
        }

        // set spinner value
        val idx = turnRestrictionTypeList.indexOf(restriction.relation.tags.getShortRestrictionValue())
        if (idx != binding.turnRestrictionTypeSpinner.selectedItemPosition) {
            // if -1 selected (unknown restriction), view gets very small... not nice, but not worth the work
            // without the post it doesn't work... though it used to work in a previous version of the form, wtf?
            binding.turnRestrictionTypeSpinner.post { binding.turnRestrictionTypeSpinner.setSelection(idx) }
        }

        // todo: switching roles for an existing relation requires another new action...
        //  maybe do that later, but currently this is only allowed when adding a new relation
        binding.swapFromToRoles.setOnClickListener {
            val rel = (currentRestriction as? TurnRestriction)?.relation ?: return@setOnClickListener
            val newMembers = rel.members.map { when (it.role) {
                "from" -> it.copy(role = "to")
                "to" -> it.copy(role = "from")
                else -> it
            } }
            currentRestriction = TurnRestriction(rel.copy(members = newMembers))
        }
        binding.exceptions.setOnClickListener { showTurnRestrictionExceptionsDialog() }

        // set exceptions
        val args = restriction.relation.tags["except"]?.replace(";", ", ") ?: getString(R.string.overlay_none)
        binding.exceptions.text = getString(R.string.restriction_overlay_exceptions, args)

        // show other restriction parts like conditional or unknown values
        val restrictionInfo = restriction.relation.tags
            .filterKeys { key -> key.startsWith("restriction:") && onlyTurnRestriction.none { key.endsWith(it) } }
            .map { "${it.key} = ${it.value}" }
            .joinToString("\n")
        displayConditionalRestrictions(restrictionInfo)

        // show only-restriction (restriction:hgv and similar)
        binding.onlyButton.isVisible = true
        val onlyFor = restriction.relation.tags.keys
            .firstOrNull { it.startsWith("restriction") && it.substringAfter("restriction:").substringBefore(":conditional") in onlyTurnRestriction }
        val onlyForText = onlyFor?.substringAfter("restriction:")?.substringBefore(":conditional") ?: "-"
        binding.onlyButton.text = getString(R.string.restriction_overlay_only_for, onlyForText)
        binding.onlyButton.setOnClickListener {
            // move restriction and restriction:conditional
            val d = AlertDialog.Builder(requireContext())
                .setSingleChoiceItems(onlyTurnRestriction.toTypedArray(), onlyTurnRestriction.indexOf(onlyFor)) { d, i ->
                    // using tags may not have been the best decision here... but whatever
                    val newOnly = onlyTurnRestriction[i]
                    val switchFrom = onlyFor?.let { ":$it" } ?: ""
                    val newTags = restriction.relation.tags.toMutableMap()
                    newTags.remove("restriction$switchFrom")?.let { newTags["restriction:$newOnly"] = it }
                    newTags.remove("restriction$switchFrom:conditional")?.let { newTags["restriction:$newOnly:conditional"] = it }
                    d.dismiss()
                    currentRestriction = TurnRestriction(restriction.relation.copy(tags = newTags))
                }
                .setNegativeButton(android.R.string.cancel, null)
            if (onlyFor != null)
                d.setNeutralButton(R.string.delete_confirmation) { _, _ ->
                    val newTags = restriction.relation.tags.toMutableMap()
                    newTags.remove("restriction:$onlyFor")?.let { newTags["restriction"] = it }
                    newTags.remove("restriction:$onlyFor:conditional")?.let { newTags["restriction:conditional"] = it }
                    currentRestriction = TurnRestriction(restriction.relation.copy(tags = newTags))
                }
            d.show()
        }
    }

    // get bearing of last segment of "from" member for via icon
    private fun showTurnRestrictionOnMap(restriction: TurnRestriction) {
        val members = restriction.relation.members.map { it.role to mapDataSource.get(it.type, it.ref)!! }
        val viaMembers = members.filter { it.first == "via" }.map { it.second }
        val from = members.singleOrNull { it.first == "from" }?.second as? Way ?: return // only one from member supported
        val isFirst = viaMembers.any { it is Node && it.id == from.nodeIds.first() || it is Way && it.nodeIds.firstAndLast().contains(from.nodeIds.first()) }
        val isLast = viaMembers.any { it is Node && it.id == from.nodeIds.last() || it is Way && it.nodeIds.firstAndLast().contains(from.nodeIds.last()) }
        if (isFirst == isLast) return // should not happen
        val nodeIdsForBearing = if (isFirst) from.nodeIds.take(2).reversed() else from.nodeIds.takeLast(2)
        val nodesForBearing = nodeIdsForBearing.map { mapDataSource.getNode(it)!! }
        val bearing = nodesForBearing.first().position.finalBearingTo(nodesForBearing.last().position)
        via = nodesForBearing.last().position to bearing
    }

    private fun showUnsupportedTurnRestriction(restriction: TurnRestriction) {
        val rel = restriction.relation
        binding.turnRestrictionContainer.isVisible = true
        binding.maxWeightContainer.isGone = true
        binding.infoText.isVisible = true

        if (rel.isRelationComplete()) {
            binding.infoText.text = getString(R.string.restriction_overlay_relation_unsupported, rel.getDetailsText())
        } else {
            binding.infoText.setText(R.string.restriction_overlay_relation_incomplete)
        }
    }

    private fun showTurnRestrictionExceptionsDialog() {
        val restriction = currentRestriction as? TurnRestriction ?: return
        val selectedExceptions = restriction.relation.tags["except"]?.split(";").orEmpty()
        val selected = exceptions.map { it in selectedExceptions }
        val newSelected = selected.toMutableList()
        AlertDialog.Builder(requireContext())
            .setMultiChoiceItems(exceptions.toTypedArray(), selected.toBooleanArray()) { _, i, s ->
                newSelected[i] = s
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (selected == newSelected) return@setPositiveButton
                val newTags = restriction.relation.tags.toMutableMap()
                newTags["except"] = newSelected.zip(exceptions).mapNotNull { if (it.first) it.second else null }.joinToString(";")
                currentRestriction = TurnRestriction(restriction.relation.copy(tags = newTags))
            }
            .show()
    }

    // ---------------- other answers ----------------------

    private fun relationDetailsAnswer(): AnswerItem? {
        val restriction = currentRestriction as? TurnRestriction ?: return null
        return if (restriction.relation.id == 0L) null
        else AnswerItem(R.string.restriction_overlay_show_details) {
            AlertDialog.Builder(requireContext())
                .setMessage(restriction.relation.getDetailsText())
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.quest_generic_answer_show_edit_tags) { _, _ ->
                    editTags(restriction.relation, elementGeometry = getGeometry(restriction.relation), editTypeName = overlay.name)
                }
                .show()
        }
    }

    // ---------------- relation stuff used for turn restriction ----------------------

    private fun getGeometry(rel: Relation): ElementGeometry? {
        if (rel.id != 0L)
            return mapDataSource.getGeometry(rel.type, rel.id)
        val ways = rel.members.mapNotNull { if (it.type == ElementType.WAY) it.key else null }
        return ElementGeometryCreator().create(rel, mapDataSource.getGeometries(ways).associate { it.elementId to (it.geometry as ElementPolylinesGeometry).polylines.single() })
    }

    private fun Relation.isRelationComplete(): Boolean =
        members.all { mapDataSource.get(it.type, it.ref) != null }

    private fun Relation.getDetailsText(): String {
        val tagsText = tags.entries.sortedBy { it.key }.joinToString("\n") { "${it.key} = ${it.value}" }
        val membersText = members.joinToString("\n") { member ->
            val element = mapDataSource.get(member.type, member.ref)!!
            val memberDetails = getNameAndLocationLabel(element, resources, featureDictionary, false)
                ?.let { "${element.key}: $it" } ?: element.key.toString()
            "${member.role}: $memberDetails"
        }
        return "$tagsText\n\n$membersText"
    }
}

// accessing restrictionTypes by index is absurdly complicated... the set is ordered, so wtf?
private val turnRestrictionTypeList = turnRestrictionTypes.toList()

// most used according to taginfo
private val exceptions = listOf(
    "bicycle", "psv", "bus", "emergency", "agricultural", "hgv", "moped", "destination", "motorcar"
)

// restriction:* list from wiki
private val onlyTurnRestriction = listOf(
    "hgv", "caravan", "motorcar", "bus", "agricultural", "motorcycle", "bicycle", "hazmat"
)

val onlyTurnRestrictionSet = onlyTurnRestriction.toHashSet()

// actually this may be country specific!
private fun getIconForTurnRestriction(type: String?) = when(type) {
    "no_right_turn" -> R.drawable.ic_restriction_no_right_turn
    "no_left_turn" -> R.drawable.ic_restriction_no_left_turn
    "no_u_turn" -> R.drawable.ic_restriction_no_u_turn
    "no_straight_on" -> R.drawable.ic_restriction_no_straight_on
    "only_right_turn" -> R.drawable.ic_restriction_only_right_turn
    "only_left_turn" -> R.drawable.ic_restriction_only_left_turn
    "only_straight_on" -> R.drawable.ic_restriction_only_straight_on
    else -> R.drawable.ic_restriction_unknown // currently the note icon, but half size
}

fun Map<String, String>.getShortRestrictionValue(): String? {
    get("restriction")?.let { return it }
    get("restriction:conditional")?.let { return it.substringBefore("@").trim() }
    entries.firstOrNull { it.key.startsWith("restriction:") }?.let { return it.value.substringBefore("@").trim() } // restriction:hgv and similar, may be conditional
    return null
}

// todo: switch form changing tags to sth else, this is getting way too complicated to handle
private sealed interface Restriction {
    val type: RestrictionType
    val element: Element
    fun getDrawable(inflater: LayoutInflater, countryInfo: CountryInfo): Drawable?
}

private data class TurnRestriction(val relation: Relation) : Restriction {
    override val type = RestrictionType.TURN
    override val element get () = relation
    override fun getDrawable(inflater: LayoutInflater, countryInfo: CountryInfo) =
        ContextCompat.getDrawable(inflater.context, getIconForTurnRestriction(relation.tags.getShortRestrictionValue()))
}

private data class WeightRestriction(val way: Way, val sign: MaxWeightSign, val weight: String) : Restriction {
    override val type = RestrictionType.WEIGHT
    override val element get () = way
    override fun getDrawable(inflater: LayoutInflater, countryInfo: CountryInfo) =
        (sign.asItem(inflater, countryInfo.countryCode).image as? DrawableImage)?.drawable
}

private enum class RestrictionType { TURN, WEIGHT }

private fun getWeightRestrictions(way: Way): List<WeightRestriction> {
    val restrictions = mutableListOf<WeightRestriction>()
    for (sign in MaxWeightSign.values()) {
        val key = if (way.tags.containsKey(sign.osmKey)) sign.osmKey
            else way.tags.keys.firstOrNull { it == "${sign.osmKey}:conditional" } ?: continue
        val weight = way.tags[key]!!
        restrictions.add(WeightRestriction(way, sign, weight))
    }
    return restrictions
}
