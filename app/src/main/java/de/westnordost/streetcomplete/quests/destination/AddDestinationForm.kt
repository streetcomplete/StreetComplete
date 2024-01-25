package de.westnordost.streetcomplete.quests.destination

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.databinding.QuestDestinationBinding
import de.westnordost.streetcomplete.databinding.QuestDestinationLaneBinding
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.lanes.LineStyle
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.SearchAdapter
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.enlargedBy
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees
import de.westnordost.streetcomplete.util.mostCommonWithin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.PI

class AddDestinationForm : AbstractOsmQuestForm<Pair<DestinationLanes?, DestinationLanes?>>() {

    private val mapDataSource: MapDataWithEditsSource by inject()
//    private val questTypeRegistry: QuestTypeRegistry by inject()

    override val contentLayoutResId = R.layout.quest_destination
    private val binding by contentViewBinding(QuestDestinationBinding::bind)

    /* // todo: add later, once more lanes are allowed
    override val otherAnswers get() = listOf(AnswerItem(R.string.quest_lanes_title) { // todo: text
        // show lanes quest, because just removing lanes doesn't necessarily show lanes quest!
        (parentFragment as? MainFragment)
        val lanesQuestType = questTypeRegistry.getByName("AddLanes")!!
        val key = (questKey as OsmQuestKey).copy(questTypeName = lanesQuestType.name)
        val f = AddLanesForm()
        f.arguments = createArguments(key, lanesQuestType, geometry, 0f, 0f) // looks like lanes form gets correct orientation anyway
        val osmArgs = createArguments(element)
        f.requireArguments().putAll(osmArgs)
        parentFragmentManager.commit {
            replace(id, f, "bottom_sheet")
            addToBackStack("bottom_sheet")
        }
    })
*/
    private var currentLane = 0
    private var currentIsBackward = false
    private var forward: DestinationLanes? = null
    private var backward: DestinationLanes? = null
    private val currentDestinations: MutableSet<String> get() {
        val lanes = if (currentIsBackward) backward else forward
        return lanes!!.get(currentLane) // should never be null, as it's set in showInput
    }

    private var wayRotation: Float = 0f

    private val destination get() = binding.destinationInput.text?.toString().orEmpty().trim()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wayRotation = (geometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.destinationInput.setAdapter(SearchAdapter(requireContext(), { getSuggestions(it) }, { it }))
        binding.destinationInput.onItemClickListener = AdapterView.OnItemClickListener { _, t, _, _ ->
            val destination = (t as? TextView)?.text?.toString() ?: return@OnItemClickListener
            if (!currentDestinations.add(destination)) return@OnItemClickListener // we don't want duplicates
            onAddedDestination()
        }

        binding.destinationInput.doAfterTextChanged {
            if (it.toString().endsWith("\n"))
                finishCurrentDestination()
            checkIsFormComplete()
        }
        binding.destinationInput.doOnLayout { binding.destinationInput.dropDownWidth = binding.destinationInput.width - requireContext().dpToPx(60).toInt() }

        binding.addDestination.setOnClickListener {
            if (binding.destinationInput.text.isBlank()) return@setOnClickListener
            onAddedDestination()
        }

        if (isOneway(element.tags)) {
            currentIsBackward = false
            showInput(getLaneCountInCurrentDirection())
        } else {
            // show side selector
            binding.destinationInput.isGone = true
            binding.addDestination.isGone = true
            binding.lanesContainer.isGone = true
            binding.sideSelect.root.isVisible = true

            // and make it work, this is essentially a condensed copy of AddLanesForm.setStreetSideLayout
            val puzzleView = binding.sideSelect.puzzleView
            lifecycle.addObserver(puzzleView)
            puzzleView.isShowingLaneMarkings = true
            puzzleView.isShowingBothSides = true
            puzzleView.isForwardTraffic = !countryInfo.isLeftHandTraffic
            val edgeLine = countryInfo.edgeLineStyle
            puzzleView.edgeLineColor =
                if (edgeLine.contains("yellow")) Color.YELLOW else Color.WHITE
            puzzleView.edgeLineStyle = when {
                !edgeLine.contains("dashes") -> LineStyle.CONTINUOUS
                edgeLine.contains("short") -> LineStyle.SHORT_DASHES
                else -> LineStyle.DASHES
            }
            puzzleView.centerLineColor = if (countryInfo.centerLineStyle.contains("yellow")) Color.YELLOW else Color.WHITE
            val forwardLanes = getLaneCountInCurrentDirection()
            currentIsBackward = true
            val backwardLanes = getLaneCountInCurrentDirection()
            if (countryInfo.isLeftHandTraffic)
                puzzleView.setLaneCounts(forwardLanes, backwardLanes, false)
            else
                puzzleView.setLaneCounts(backwardLanes, forwardLanes, false)
            // and set the click listener
            puzzleView.onClickListener = null
            puzzleView.onClickSideListener = { isRight ->
                currentIsBackward = !isRight
                if (countryInfo.isLeftHandTraffic)
                    currentIsBackward = !currentIsBackward
                showInput(getLaneCountInCurrentDirection())
                // maybe: hide side selector, and have a button to show it again?
            }
        }

        // start loading the lazy thing now that everything else is done
        viewLifecycleScope.launch(Dispatchers.IO) { suggestions }
    }

    private fun getLaneCountInCurrentDirection(): Int {
        if (currentIsBackward)
            element.tags["lanes:backward"]?.toIntOrNull()?.let { return it }
        else
            element.tags["lanes:forward"]?.toIntOrNull()?.let { return it }
        val lanes = element.tags["lanes"]?.toIntOrNull()
        if (isOneway(element.tags)) return lanes ?: 1
        return ((lanes ?: 2) / 2).coerceAtLeast(1)
    }

    // todo: two lanes in one direction is not yet working properly
    //  orientation is confusing
    //  when selecting other side and going back the marks are missing and "all lanes" is showing again
    //  and sometimes the current destination view stays when switching sides
    private fun showInput(laneCount: Int) {
        // initialize forward/backward if necessary
        if (currentIsBackward) {
            if (backward?.count != laneCount) backward = DestinationLanes(laneCount)
        } else {
            if (forward?.count != laneCount) forward = DestinationLanes(laneCount)
        }

        if (laneCount == 1) {
            currentLane = 1
            binding.destinationInput.isVisible = true
            binding.addDestination.isVisible = true
            binding.lanesContainer.isGone = true
            binding.currentDestinations.text = currentDestinations.joinToString(", ")
            binding.destinationInput.requestFocus()
            viewLifecycleScope.launch {
                delay(30)
//                binding.destinationInput.showDropDown() // working in cuisine form, but not here?
                binding.destinationInput.setText("") // but this works
            }
            return
        }

        binding.destinationInput.isGone = true
        binding.addDestination.isGone = true
        binding.lanesContainer.isVisible = true
        binding.lanesContainer.removeAllViews()
        // hide the whole container after selecting all lanes
        binding.lanesContainer.addView(Button(requireContext()).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setText(R.string.quest_destination_all_lanes_button)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.button_bar_button_text))
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background))
            setOnClickListener { showInput(1) }
            tag = 0
        })

        repeat(laneCount) { idx ->
            val lane = idx + 1
            val b = QuestDestinationLaneBinding.inflate(layoutInflater)
            b.lane.setOnClickListener {
                // remove "all lanes" button if lane tapped
                // and show the lanes input
                binding.lanesContainer.findViewWithTag<View>(0)?.let {
                    binding.destinationInput.isVisible = true
                    binding.addDestination.isVisible = true
                    binding.lanesContainer.removeView(it)
                }
                if (currentLane == lane) return@setOnClickListener

                b.lane.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(requireContext(), R.color.accent), PorterDuff.Mode.MULTIPLY)
                val previousView: View? = binding.lanesContainer.findViewWithTag(currentLane)
                previousView?.findViewById<ImageView>(R.id.lane)?.colorFilter = null

                if (currentLane != 0) {
                    finishCurrentDestination()
                    // set remove / checkmark
                    if (currentDestinations.isEmpty())
                        previousView?.findViewById<ImageView>(R.id.check)?.isGone = true
                    else
                        previousView?.findViewById<ImageView>(R.id.check)?.isVisible = true
                }

                currentLane = lane

                binding.currentDestinations.text = currentDestinations.joinToString(", ")
                binding.destinationInput.requestFocus()
            }
            b.root.tag = lane
            binding.lanesContainer.addView(b.root)
        }
    }

    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
        val mapRotation = (rotation * 180 / PI).toFloat()
        val mapTilt = (tilt * 180 / PI).toFloat()

        binding.sideSelect.puzzleViewRotateContainer.streetRotation = wayRotation + mapRotation
        binding.sideSelect.littleCompass.root.rotation = mapRotation
        binding.sideSelect.littleCompass.root.rotationX = mapTilt
    }

    override fun onClickOk() {
        finishCurrentDestination()
        // one side is complete, but the other may not be, e.g. if the user clicked the wrong side
        if (forward?.isComplete == false) forward = null
        if (backward?.isComplete == false) backward = null
        if (forward == null && backward == null) return // should never happen
        applyAnswer(forward to backward)

        favs.add(getAllCurrentDestinations())
    }

    override fun isFormComplete(): Boolean {
        val forwardComplete = forward?.isComplete ?: false
        val backwardComplete = backward?.isComplete ?: false
        val forwardEmpty = forward?.isEmpty ?: true
        val backwardEmpty = backward?.isEmpty ?: true

        if ((forwardComplete && backwardComplete)
            || (forwardComplete && backwardEmpty)
            || (forwardEmpty && backwardComplete)
        ) return true

        if (binding.destinationInput.text.isNullOrBlank()) return false
        return if (currentIsBackward)
            backward?.isCompleteExcept(currentLane) == true && (forwardComplete || forwardEmpty)
        else
            forward?.isCompleteExcept(currentLane) == true && (backwardComplete || backwardEmpty)
    }

    override fun isRejectingClose() = isFormComplete() || binding.destinationInput.text.isNotBlank() || backward?.isEmpty == false || forward?.isEmpty == false

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            prefs,
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
        )
    }

    private fun finishCurrentDestination() {
        currentDestinations.removeAll { it.isBlank() }
        if (destination.isNotBlank()) currentDestinations.add(destination)
        binding.destinationInput.text.clear()
        setCurrentDestinationsView()
        checkIsFormComplete()
    }

    private fun onAddedDestination() {
        finishCurrentDestination()
        if (binding.lanesContainer.isGone)
            viewLifecycleScope.launch {
                delay(30)
                binding.destinationInput.showDropDown()
            }
    }

    private fun setCurrentDestinationsView() {
        binding.currentDestinations.text = currentDestinations.joinToString(", ")
    }

    private fun getAllCurrentDestinations(): Set<String> {
        val destinations = hashSetOf<String>()
        forward?.let { destinations.addAll(it.getDestinations()) }
        backward?.let { destinations.addAll(it.getDestinations()) }
        return destinations
    }

    private fun getSuggestions(search: String) = (getAllCurrentDestinations() + suggestions)
        .filter { it.startsWith(search, true) && it !in currentDestinations }

    private val suggestions by lazy {
        val data = mapDataSource.getMapDataWithGeometry(geometry.getBounds().enlargedBy(100.0))
        val suggestions = hashSetOf<String>()
        data.filter("ways, relations with destination or destination:forward or destination:backward or destination:lanes")
            .forEach {
                it.tags["destination"]?.let { suggestions.addAll(it.split(";")) }
                it.tags["destination:forward"]?.let { suggestions.addAll(it.split(";")) }
                it.tags["destination:backward"]?.let { suggestions.addAll(it.split(";")) }
                it.tags["destination:lanes"]?.let { suggestions.addAll(it.split(";", "|")) }
            }
        (suggestions + lastPickedAnswers).distinct()
    }

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 20, historyCount = 50, first = 1)
            .toList()
    }
}

class DestinationLanes(val count: Int) {
    init { require(count > 0) { "count $count must be positive" } }
    private val destinationsByLane = hashMapOf<Int, MutableSet<String>>()
    fun set(lane: Int, destinations: MutableSet<String>) {
        checkLane(lane)
        destinationsByLane[lane] = destinations
    }
    fun get(lane: Int): MutableSet<String> {
        checkLane(lane)
        return destinationsByLane.getOrPut(lane) { mutableSetOf() }
    }
    fun getDestinations() = destinationsByLane.values.flatten()

    val isEmpty get() = (1..count).all { destinationsByLane[it].isNullOrEmpty() }
    val isComplete get() = (1..count).none { destinationsByLane[it].isNullOrEmpty() }
    fun isCompleteExcept(lane: Int) = (1..count).filterNot { it == lane }.none { destinationsByLane[it].isNullOrEmpty() }

    private fun laneString(): String? {
        if (!isComplete) return null
        return destinationsByLane.entries.sortedBy { it.key }
            .map { it.value }.joinToString("|") { it.joinToString(";") }
    }

    private fun checkLane(lane: Int) = require(lane in 1..count) {"tried to access lane $lane outside laneCount $count" }

    // todo: for lane count also cycleways need to be considered
    //  but careful about sides!
    // anyway, currently such cases are simply ignored by the filter
    fun applyTo(tags: Tags, isBackward: Boolean) {
        if (!isComplete) throw (IllegalStateException("cannot apply an incomplete destination answer"))
        val tag = if (count > 1) "destination:lanes" else "destination"
        if (isOneway(tags)) {
            tags[tag] = laneString()!!
            return
        }
        val forwardBackward = if (isBackward) ":backward" else ":forward"
        tags[tag + forwardBackward] = laneString()!!
    }
}
