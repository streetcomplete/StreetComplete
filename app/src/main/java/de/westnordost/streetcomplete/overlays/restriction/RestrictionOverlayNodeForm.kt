package de.westnordost.streetcomplete.overlays.restriction

import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.createNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.databinding.FragmentOverlayRestrictionNodeBinding
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isNotOnewayForCyclists
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapOrientationAware
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.firstAndLast
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.PositionOnWaySegment
import de.westnordost.streetcomplete.util.math.VertexOfWay
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item2
import de.westnordost.streetcomplete.view.setImage
import de.westnordost.streetcomplete.view.setText
import org.koin.android.ext.android.inject

// some stuff taken from LaneNarrowingTrafficCalmingForm
class RestrictionOverlayNodeForm : AbstractOverlayForm(), IsMapPositionAware, IsMapOrientationAware {

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    override val contentLayoutResId = R.layout.fragment_overlay_restriction_node
    private val binding by contentViewBinding(FragmentOverlayRestrictionNodeBinding::bind)
    private val items = listOf(
        Item2(Type.GIVE_WAY, ResImage(R.drawable.ic_restriction_give_way), ResText(R.string.restriction_overlay_sign_give_way)),
        Item2(Type.STOP, ResImage(R.drawable.ic_restriction_stop), ResText(R.string.restriction_overlay_sign_stop)),
        Item2(Type.ALL_WAY_STOP, ResImage(R.drawable.ic_restriction_stop), ResText(R.string.restriction_overlay_sign_stop_all_way)),
    )
    private val selectableItems = items.filterNot { it.value == Type.ALL_WAY_STOP }

    private var positionOnWay: PositionOnWay? = null
        set(value) {
            field = value
            if (value != null) {
                setMarkerPosition(value.position)
                setMarkerVisibility(true)
            } else {
                setMarkerVisibility(false)
                setMarkerPosition(null)
            }
        }
    private var roads: Collection<Pair<Way, List<LatLon>>>? = null
    private val waysFilter = """
        ways with
          area != yes
          and (
            highway ~ ${ALL_ROADS.joinToString("|")}|cycleway
            or (
              highway ~ path|footpath|bridleway
              and bicycle ~ yes|designated
            )
          )
    """.toElementFilterExpression()
    private var mapRotation = 0.0

    private var data: MapDataWithGeometry? = null
    private var direction: Direction? = null
    private var type: Type? = null
        set(value) {
            if (field == value) return
            field = value
            checkCurrentCursorPosition()
            if (element == null) {
                if (type == Type.GIVE_WAY) setMarkerIcon(R.drawable.ic_restriction_give_way)
                    else setMarkerIcon(R.drawable.ic_restriction_stop)
            }
            checkIsFormComplete()
            updateForm()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectedCellView.setOnClickListener {
            ImageListPickerDialog(requireContext(), selectableItems) { item ->
                type = item.value
            }.show()
        }
        binding.selectTextView.setOnClickListener {
            ImageListPickerDialog(requireContext(), selectableItems) { item ->
                type = item.value
            }.show()
        }
        binding.dropDownArrowImageView.setOnClickListener {
            ImageListPickerDialog(requireContext(), selectableItems) { item ->
                type = item.value
            }.show()
        }
        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

        if (element == null) {
            view.doOnLayout {
                initCreatingPointOnWay()
                checkCurrentCursorPosition()
            }
            setMarkerIcon(R.drawable.ic_restriction_stop)
            setMarkerVisibility(false)
        } else {
            val td = getTypeAndDirection(element!!.tags)
            type = td.first
            direction = td.second
            updateForm()
        }
    }

    private fun initCreatingPointOnWay() {
        data = mapDataWithEditsSource.getMapDataWithGeometry(geometry.center.enclosingBoundingBox(100.0))
        val data = data ?: return
        roads = data
            .filter(waysFilter)
            .filterIsInstance<Way>()
            .map { way ->
                val positions = way.nodeIds.map { data.getNode(it)!!.position }
                way to positions
            }.toList()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkCurrentCursorPosition()
    }

    override fun onMapMoved(position: LatLon) {
        if (element != null) return
        checkCurrentCursorPosition()
    }

    private fun updateForm() {
        // show correct icon
        val item = items.firstOrNull { it.value == type }
        if (item != null) {
            binding.selectedCellView.isVisible = true
            binding.selectedCellView.setImage(item.image)
            binding.selectTextView.setText(item.title)
        } else {
            binding.selectedCellView.isGone = true
        }
        // and direction
        if (type == Type.ALL_WAY_STOP) {
            binding.directionText.isGone = true
            binding.directionContainer.isGone = true
        } else if (type != null) {
            setDirectionImages()
        }
    }

    private fun setDirectionImages() {
        val element = element
        val pow = positionOnWay
        val way = when {
            element != null -> mapDataWithEditsSource.getWaysForNode(element.id).firstOrNull { waysFilter.matches(it) }
            pow is VertexOfWay -> mapDataWithEditsSource.getWay(pow.wayIds.first())
            pow is PositionOnWaySegment -> mapDataWithEditsSource.getWay(pow.wayId)
            else -> null
        }
        if (way?.tags?.let {
                if (it["highway"] in ALL_ROADS)
                    isOneway(it) && !isNotOnewayForCyclists(it, countryInfo.isLeftHandTraffic)
                else // cycleways, though doesn't catch oneway = yes and oneway:bicycle = no
                    isOneway(it) || it["oneway:bicycle"] in listOf("yes", "-1")
        } != false) {
            binding.directionText.isGone = true
            binding.directionContainer.isGone = true
            return
        }
        val wayRotation = getWayRotation()
        binding.directionText.isVisible = true
        binding.directionContainer.isVisible = true
        binding.directionContainer.removeAllViews()
        binding.directionContainer.addView(ImageView(requireContext()).apply {
            // forward
            val drawable = RotatedCircleDrawable(context.getDrawable(R.drawable.ic_oneway_yes)!!)
            drawable.rotation = (mapRotation + wayRotation).toFloat()
            setImageDrawable(drawable)
            if (direction == Direction.FORWARD)
                setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent))
            else colorFilter = null
            setOnClickListener {
                direction = Direction.FORWARD
                binding.directionContainer.children.forEach { (it as ImageView).colorFilter = null }
                setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent))
                checkIsFormComplete()
            }
        })
        binding.directionContainer.addView(ImageView(requireContext()).apply {
            // backward
            val drawable = RotatedCircleDrawable(context.getDrawable(R.drawable.ic_oneway_yes_reverse)!!)
            drawable.rotation = (mapRotation + wayRotation).toFloat()
            setImageDrawable(drawable)
            if (direction == Direction.BACKWARD)
                setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent))
            else colorFilter = null
            setOnClickListener {
                direction = Direction.BACKWARD
                binding.directionContainer.children.forEach { (it as ImageView).colorFilter = null }
                setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent))
                checkIsFormComplete()
            }
        })
    }

    private fun checkCurrentCursorPosition() {
        val roads = roads ?: return
        val metersPerPixel = metersPerPixel ?: return
        val maxDistance = metersPerPixel * requireContext().resources.dpToPx(24)
        val snapToVertexDistance = metersPerPixel * requireContext().resources.dpToPx(12)
        val pos = geometry.center.getPositionOnWays(roads, maxDistance, snapToVertexDistance)
        if (pos is VertexOfWay) {
            val node = mapDataWithEditsSource.getNode(pos.nodeId)!!
            if (node.tags.containsKey("highway") || node.tags.containsKey("crossing"))
                return
        }
        // get number of roads on this vertex
        // but count only 1 road if count is 2 and it's an end node of both
        val wayCountOnVertex = if (pos !is VertexOfWay) null
        else {
            val r = roads.filter { it.first.nodeIds.contains(pos.nodeId) }
            if (r.size == 2 && r.all { it.first.nodeIds.firstAndLast().contains(pos.nodeId) }) 1
            else r.size
        }
        positionOnWay = when (type) {
            Type.GIVE_WAY -> {
                if (wayCountOnVertex != null && wayCountOnVertex > 1)
                    null // don't allow on more than a single way
                else pos
            }
            Type.STOP -> {
                if (wayCountOnVertex != null && wayCountOnVertex > 1)
                    type = Type.ALL_WAY_STOP // no normal stop if there is more than one way
                pos
            }
            Type.ALL_WAY_STOP -> {
                if (wayCountOnVertex == null || wayCountOnVertex == 1)
                    type = Type.STOP // normal stop if there is only one way
                pos
            }
            else -> pos
        }
        checkIsFormComplete()
        updateForm()
    }

    override fun hasChanges(): Boolean {
        val td = element?.let { getTypeAndDirection(it.tags) }
        return td?.first != type || td?.second != direction
    }

    override fun isFormComplete(): Boolean = type != null && hasChanges() && (element != null || positionOnWay != null)

    override fun onClickOk() {
        val element = element
        val positionOnWay = positionOnWay
        val direction = direction
        val type = type ?: return
        val editAction = if (element != null) {
            val tagChanges = StringMapChangesBuilder(element.tags)
            applyTo(tagChanges, type, direction)
            UpdateElementTagsAction(element, tagChanges.create())
        } else if (positionOnWay != null) {
            createNodeAction(positionOnWay, mapDataWithEditsSource) { applyTo(it, type, direction) }
        } else null
        if (editAction != null)
            applyEdit(editAction)
    }

    private fun onLoadInstanceState(inState: Bundle) {
        val selectedIndex = inState.getInt(SELECTED_INDEX)
        type = if (selectedIndex != -1) items[selectedIndex].value else null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_INDEX, items.indexOfFirst { it.value == type })
    }

    override fun onMapOrientation(rotation: Double, tilt: Double) {
        mapRotation = -rotation
    }

    private fun getWayRotation(): Double {
        val element = element as? Node
        if (element != null) {
            val way = mapDataWithEditsSource.getWaysForNode(element.id).firstOrNull { waysFilter.matches(it) } ?: return 0.0
            val index = way.nodeIds.indexOf(element.id)
            return if (index != way.nodeIds.lastIndex)
                element.position.initialBearingTo(mapDataWithEditsSource.getNode(way.nodeIds[index + 1])!!.position)
            else
                mapDataWithEditsSource.getNode(way.nodeIds[index - 1])!!.position.initialBearingTo(element.position)
        } else {
            val pow = positionOnWay ?: return 0.0
            if (pow is PositionOnWaySegment) return pow.segment.first.initialBearingTo(pow.segment.second)
            else if (pow is VertexOfWay) {
                val way = mapDataWithEditsSource.getWay(pow.wayIds.first())!!
                val index = way.nodeIds.indexOf(pow.nodeId)
                return if (index != way.nodeIds.lastIndex)
                    pow.position.initialBearingTo(mapDataWithEditsSource.getNode(way.nodeIds[index + 1])!!.position)
                else
                    mapDataWithEditsSource.getNode(way.nodeIds[index - 1])!!.position.initialBearingTo(pow.position)
            }
        }
        return 0.0
    }

    companion object {
        private const val SELECTED_INDEX = "selected_index"
    }
}

private fun applyTo(tagChanges: StringMapChangesBuilder, type: Type, direction: Direction?) {
    val newDirection = direction?.takeIf { type != Type.ALL_WAY_STOP }?.osmValue
    if (tagChanges["direction"] != newDirection) {
        if (newDirection == null)
            tagChanges.remove("direction")
        else tagChanges["direction"] = newDirection
    }
    val newHighway = if (type == Type.GIVE_WAY) "give_way"
        else "stop"
    if (tagChanges["highway"] != newHighway)
        tagChanges["highway"] = newHighway
    if (type == Type.ALL_WAY_STOP) tagChanges["stop"] = "all" // according to wiki, also minor is possible, but it seems that it's not used on intersection nodes
    else if (type == Type.GIVE_WAY) tagChanges.remove("stop")
}

private fun getTypeAndDirection(tags: Map<String, String>): Pair<Type?, Direction?> {
    val type = when {
        tags["highway"] == "give_way" -> Type.GIVE_WAY
        // direction = both seems to be used like stop = all
        tags["highway"] == "stop" && (tags["stop"] == "all" || tags["direction"] == "both") -> Type.ALL_WAY_STOP
        tags["highway"] == "stop" -> Type.STOP
        else -> null
    }
    val direction = when (type) {
        Type.GIVE_WAY, Type.STOP -> tags["direction"]?.let { dir -> Direction.values().firstOrNull { it.osmValue == dir } }
        else -> null
    }
    return type to direction
}

private enum class Type { GIVE_WAY, STOP, ALL_WAY_STOP }
private enum class Direction(val osmValue: String) { FORWARD("forward"), BACKWARD("backward") }
