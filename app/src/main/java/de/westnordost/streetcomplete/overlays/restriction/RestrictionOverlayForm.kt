package de.westnordost.streetcomplete.overlays.restriction

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.CreateRelationAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeleteRelationAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.createChanges
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.RelationMember
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.databinding.FragmentOverlayRestrictionBinding
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.containsAny
import de.westnordost.streetcomplete.util.ktx.firstAndLast
import de.westnordost.streetcomplete.util.math.distanceToArcs
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.finalBearingTo
import de.westnordost.streetcomplete.util.showAddConditionalDialog
import de.westnordost.streetcomplete.view.ArrayImageAdapter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

// todo:
//  currently adding a relation allows only 2 (different!) ways, and simply sets via node
//   not allowing via ways, and requiring from/to ways to be different is bad for u-turn restrictions
//  which icon to use for unknown restriction values? currently it's the note quest icon
//  any safety measures against users doing stupid things?
//   for now: no. if (expert!) users add insane relations, they would do the same in id/josm
class RestrictionOverlayForm : AbstractOverlayForm() {

    private val mapDataSource: MapDataWithEditsSource by inject()
    private val mapFragment by lazy {
        (parentFragment as? MainFragment)?.childFragmentManager?.fragments?.filterIsInstance<MainMapFragment>()?.singleOrNull()
    }
    override val contentLayoutResId = R.layout.fragment_overlay_restriction
    private val binding by contentViewBinding(FragmentOverlayRestrictionBinding::bind)
    private val restrictions by lazy { mapDataSource.getRelationsForWay(element!!.id).filter { it.tags["type"] == "restriction" } }
    private val newTags = hashMapOf<String, String>()
    private var selectedRelation: Relation? = null
    private var relation: Relation? = null
        set(value) {
            if (field == value) return
            field = value
            checkIsFormComplete()
            if (value == null) return

            // get bearing of last segment of "from" member for via icon
            val members = value.members.map { it.role to mapDataSource.get(it.type, it.ref)!! }
            val viaMembers = members.filter { it.first == "via" }.map { it.second }
            val from = members.singleOrNull { it.first == "from" }?.second as? Way ?: return
            val isFirst = viaMembers.any { it is Node && it.id == from.nodeIds.first() || it is Way && it.nodeIds.firstAndLast().contains(from.nodeIds.first()) }
            val isLast = viaMembers.any { it is Node && it.id == from.nodeIds.last() || it is Way && it.nodeIds.firstAndLast().contains(from.nodeIds.last()) }
            if (isFirst == isLast) return // should not happen
            val nodeIdsForBearing = if (isFirst) from.nodeIds.take(2).reversed() else from.nodeIds.takeLast(2)
            val nodesForBearing = nodeIdsForBearing.map { mapDataSource.getNode(it)!! }
            val bearing = nodesForBearing.first().position.finalBearingTo(nodesForBearing.last().position)
            via = nodesForBearing.last().position to bearing

            showRestriction()
        }

    private var via: Pair<LatLon, Double>? = null
        set(value) {
            field?.first?.let { mapFragment?.deleteMarkerForCurrentHighlighting(ElementPointGeometry(it)) }
            field = value
            val icon = getIconForType(newTags.getShortRestrictionValue() ?: "")
            value?.let { mapFragment?.putMarkerForCurrentHighlighting(ElementPointGeometry(it.first), icon, null, null, it.second) }
        }

    private var selectionMode: Boolean = false
        set(value) {
            field = value
            if (value) {
                mapFragment?.hideOverlay()
                mapFragment?.highlightGeometry(geometry) // highlight initially selected way only
                binding.exceptions.isVisible = true
                binding.exceptions.text = getString(R.string.restriction_overlay_exceptions, getString(R.string.overlay_none))
                binding.addRestriction.isGone = true
                binding.infoText.setText(R.string.restriction_overlay_select_way)
                binding.infoText.isVisible = true
            }
        }

    override val otherAnswers get() = listOfNotNull(
        createDoesntExistAnswer(),
        createConditionalAnswer(),
        relationDetailsAnswer(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { restrictions } // load restrictions in background, so ui thread needs to wait less
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.restrictionTypeSpinner.adapter = ArrayImageAdapter(requireContext(), restrictionTypes.map { getIconForType(it) }, 80)
        binding.restrictionTypeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (newTags.containsKey("restriction:conditional") && !newTags.containsKey("restriction")) {
                    val old = newTags["restriction:conditional"]!!.substringBefore("@").trim()
                    newTags["restriction:conditional"] = newTags["restriction:conditional"]!!.replace(old, restrictionTypeList[p2])
                    showRestriction() // reloads text
                } else
                    newTags["restriction"] = restrictionTypeList[p2]
                via = via // reload icon
                checkIsFormComplete()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }

        // switching roles for an existing relation requires another new action...
        // maybe do that later, but currently this is not allowed
        binding.swapFromToRoles.setOnClickListener {
            val rel = relation ?: return@setOnClickListener
            val newMembers = rel.members.map { when (it.role) {
                "from" -> it.copy(role = "to")
                "to" -> it.copy(role = "from")
                else -> it
            } }
            relation = rel.copy(members = newMembers)
        }
        binding.implicitSwitch.isGone = true
        binding.implicitSwitch.setOnCheckedChangeListener { _, b ->
            if (b)
                newTags.remove("implicit")
            else newTags["implicit"] = "yes"
            checkIsFormComplete()
        }
        binding.addRestriction.setOnClickListener { selectionMode = true }
        binding.exceptions.setOnClickListener { showExceptionsDialog() }

        getInitialRestrictionRelation()?.let { setRestrictionRelation(it) }
    }

    private fun getInitialRestrictionRelation(): Relation? {
        if (restrictions.isEmpty()) return null
        // prefer supported and complete relations
        return restrictions.firstOrNull { it.isSupportedRestrictionRelation() && it.isRelationComplete() }
            ?: restrictions.first()
    }

    private fun setRestrictionRelation(rel: Relation) {
        val isComplete = rel.isRelationComplete()
        if (restrictions.size > 1)
            showOtherRestrictions(restrictions.filterNot { it == rel })
        if (isComplete) {
            if (rel.isSupportedRestrictionRelation()) {
                binding.infoText.isGone = true
            } else {
                binding.infoText.text = getString(R.string.restriction_overlay_relation_unsupported, rel.getDetailsText())
                binding.infoText.isVisible = true
            }
            newTags.putAll(rel.tags)
            selectedRelation = rel
            relation = rel
            return
        }
        binding.infoText.isVisible = true
        binding.infoText.setText(R.string.restriction_overlay_relation_incomplete)
        getGeometry(rel)?.let { mapFragment?.highlightGeometry(it) }
    }

    private fun showOtherRestrictions(restrictions: List<Relation>) {
        if (restrictions.isEmpty()) return
        binding.otherRestrictions.isVisible = true
        binding.otherRestrictions.removeAllViews()
        binding.otherRestrictions.addView(TextView(requireContext()).apply {
            setText(R.string.restriction_overlay_other_restrictions)
        })
        for (restriction in restrictions) {
            binding.otherRestrictions.addView(Button(requireContext()).apply {
                text = restriction.members.filter { it.type == ElementType.WAY && it.ref == element!!.id }
                    .joinToString(", ") { it.role }
                setCompoundDrawablesWithIntrinsicBounds(getIconForType(restriction.tags.getShortRestrictionValue()), 0, 0, 0)
                setOnClickListener { setRestrictionRelation(restriction) }
            })
        }
    }

    private fun showExceptionsDialog() {
        val selectedExceptions = newTags["except"]?.split(";").orEmpty()
        val selected = exceptions.map { it in selectedExceptions }
        val newSelected = selected.toMutableList()
        AlertDialog.Builder(requireContext())
            .setMultiChoiceItems(exceptions.toTypedArray(), selected.toBooleanArray()) { _, i, s ->
                newSelected[i] = s
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (selected == newSelected) return@setPositiveButton
                newTags["except"] = newSelected.zip(exceptions).mapNotNull { if (it.first) it.second else null }.joinToString(";")
                checkIsFormComplete()
                showRestriction()
            }
            .show()
    }

    private fun showRestriction() {
        val rel = relation ?: return // should not happen
        val idx = restrictionTypes.indexOf(newTags.getShortRestrictionValue())
        if (idx != binding.restrictionTypeSpinner.selectedItemPosition) {
            // if -1 selected (unknown restriction), view gets very small... not nice, but not worth the work
            binding.restrictionTypeSpinner.setSelection(idx)
        }
        getGeometry(rel)?.let { mapFragment?.highlightGeometry(it) }

        // set info
        binding.implicitSwitch.isVisible = true
        binding.implicitSwitch.isChecked = newTags["implicit"] != "yes"
        binding.exceptions.isVisible = true
        binding.exceptions.text = getString(R.string.restriction_overlay_exceptions, newTags["except"]?.replace(";", ", ") ?: getString(R.string.overlay_none))

        // show other restrictions, but they can be modified only using the tag editor
        val restrictionInfo = newTags
            .filterKeys { it.startsWith("restriction:") }
            .map { "${it.key} = ${it.value}" }
            .joinToString("\n")
        if (restrictionInfo.isNotBlank()) {
            binding.infoText.text = restrictionInfo
            binding.infoText.isVisible = true
        } else {
            binding.infoText.isGone = true
        }
    }

    override fun hasChanges(): Boolean = relation != null
        && (relation?.id == 0L || relation != selectedRelation || selectedRelation?.tags != newTags)

    override fun isFormComplete(): Boolean = hasChanges() && newTags.keys.any { it.startsWith("restriction") }

    override fun onClickOk() {
        val rel = relation ?: return
        val geometry = getGeometry(rel) ?: geometry
        if (rel.id == 0L)
            applyEdit(CreateRelationAction(newTags, rel.members), geometry)
        else
            applyEdit(UpdateElementTagsAction(selectedRelation!!, newTags.createChanges(selectedRelation!!.tags).create()), geometry)
    }

    private fun getGeometry(rel: Relation): ElementGeometry? {
        if (rel.id != 0L)
            return mapDataSource.getGeometry(rel.type, rel.id)
        val ways = rel.members.mapNotNull { if (it.type == ElementType.WAY) it.key else null }
        return ElementGeometryCreator().create(rel, mapDataSource.getGeometries(ways).associate { it.elementId to (it.geometry as ElementPolylinesGeometry).polylines.single() })
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        if (!selectionMode) return false
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
        newTags["type"] = "restriction"
        val newRelation = Relation(0L, listOf(initialWayAsMember, otherWayAsMember, viaNodeAsMember), newTags)
        binding.swapFromToRoles.isVisible = true
        relation = newRelation
        return true
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

    private fun relationDetailsAnswer() = if ((relation?.id ?: 0L) == 0L) null
        else AnswerItem(R.string.restriction_overlay_show_details) {
            val rel = relation ?: return@AnswerItem
            AlertDialog.Builder(requireContext())
                .setMessage(rel.getDetailsText())
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.quest_generic_answer_show_edit_tags) { _, _ ->
                    editTags(rel, elementGeometry = getGeometry(rel), editTypeName = overlay.name)
                }
                .show()
        }

    private fun createDoesntExistAnswer() = if ((relation?.id ?: 0L) == 0L) null
        else AnswerItem(R.string.quest_generic_answer_does_not_exist) {
                val rel = relation ?: return@AnswerItem
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.osm_element_gone_description)
                    .setPositiveButton(R.string.osm_element_gone_confirmation) { _, _ ->
                        applyEdit(DeleteRelationAction(rel), getGeometry(rel) ?: geometry)
                    }
                    .setNeutralButton(R.string.leave_note) { _, _ -> composeNote(rel) }
                    .show()
            }

    private fun createConditionalAnswer() = relation?.let {
        if (newTags["restriction:conditional"] != null)
            AnswerItem(R.string.restriction_overlay_remove_conditional_restrictions) {
                if (!newTags.containsKey("restriction")) newTags["restriction"] = newTags.getShortRestrictionValue()!!
                newTags.remove("restriction:conditional")
                checkIsFormComplete()
                showRestriction()
            }
        else
            AnswerItem(R.string.access_manager_button_add_conditional) {
                // either it's only conditional, then value is same as restriction, or it's an exception then it's "none"
                val values = newTags["restriction"]?.let { listOf(it, "none") }
                    ?: listOf(restrictionTypeList[binding.restrictionTypeSpinner.selectedItemPosition])
                showAddConditionalDialog(requireContext(), listOf("restriction:conditional"), values, null) { _, v ->
                    newTags["restriction:conditional"] = v
                    if (!v.startsWith("none")) newTags.remove("restriction")
                    checkIsFormComplete()
                    showRestriction()
                }
            }
    }
}

val restrictionTypes = linkedSetOf(
    "no_right_turn",
    "no_left_turn",
    "no_u_turn",
    "no_straight_on",
    "only_right_turn",
    "only_left_turn",
    "only_straight_on",
)

// accessing restrictionTypes by index is absurdly complicated... the set is ordered, so wtf?
val restrictionTypeList = restrictionTypes.toList()

// most used according to taginfo
val exceptions = listOf(
    "bicycle", "psv", "bus", "emergency", "agricultural", "hgv", "moped", "destination", "motorcar"
)

// actually this may be country specific!
private fun getIconForType(type: String?) = when(type) {
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
    entries.firstOrNull { it.key.startsWith("restriction:") }?.let { return it.value } // restriction:hgv and similar
    return null
}
