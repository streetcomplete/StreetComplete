package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.AnnotatedString
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.viewbinding.ViewBinding
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.screens.main.bottom_sheet.AbstractBottomSheetFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapOrientationAware
import de.westnordost.streetcomplete.ui.common.quest.QuestHeader
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.FragmentViewBindingPropertyDelegate
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

/** abstract base class for the form that is shown to answer a quest. I.e., it is...
 *  - a bottom sheet that can be pulled up to fill the screen (see AbstractBottomSheetFragment)
 *  - displays the quest title, has (an optional) content area, the floating OK button and a
 *    button bar
 *  - and more...
 */
abstract class AbstractQuestForm :
    AbstractBottomSheetFragment(), IsShowingQuestDetails, IsMapOrientationAware {

    // dependencies
    private val countryInfos: CountryInfos by inject()
    private val countryBoundaries: Lazy<CountryBoundaries> by inject(named("CountryBoundariesLazy"))
    private val questTypeRegistry: QuestTypeRegistry by inject()

    private var startedOnce = false

    private var _countryInfo: CountryInfo? = null // lazy but resettable because based on lateinit var
        get() {
            if (field == null) {
                field = countryInfos.get(countryBoundaries.value, geometry.center)
            }
            return field
        }
    protected val countryInfo get() = _countryInfo!!

    /** either DE or US-NY (or null), depending on what countryBoundaries returns */
    protected val countryOrSubdivisionCode: String? get() {
        val latLon = geometry.center
        return countryBoundaries.value.getIds(latLon.longitude, latLon.latitude).firstOrNull()
    }

    // passed in parameters
    override lateinit var questKey: QuestKey
    protected lateinit var questType: QuestType
    protected lateinit var geometry: ElementGeometry private set
    private var initialMapRotation = 0.0
    private var initialMapTilt = 0.0

    protected val geometryRotation: MutableFloatState = mutableFloatStateOf(0f)
    protected val mapRotation: MutableFloatState = mutableFloatStateOf(0f)
    protected val mapTilt: MutableFloatState = mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        questKey = Json.decodeFromString(args.getString(ARG_QUEST_KEY)!!)
        questType = questTypeRegistry.getByName(args.getString(ARG_QUESTTYPE)!!)!!
        geometry = Json.decodeFromString(args.getString(ARG_GEOMETRY)!!)
        initialMapRotation = args.getDouble(ARG_MAP_ROTATION)
        initialMapTilt = args.getDouble(ARG_MAP_TILT)

        geometryRotation.floatValue =
            (geometry as? ElementPolylinesGeometry)?.getOrientationAtCenterLineInDegrees() ?: 0f

        // reset lazy field
        _countryInfo = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = ComposeView(inflater.context)
        view.content {
            Content()
        }
        return view
    }

    @Composable
    abstract fun Content()

    override fun onStart() {
        super.onStart()

        if (!startedOnce) {
            onMapOrientation(initialMapRotation, initialMapTilt)
            startedOnce = true
        }
    }

    override fun isRejectingClose() = isFormComplete()

    override fun onMapOrientation(rotation: Double, tilt: Double) {
        mapRotation.floatValue = rotation.toFloat()
        mapTilt.floatValue = tilt.toFloat()
    }

    companion object {
        private const val ARG_QUEST_KEY = "quest_key"
        private const val ARG_GEOMETRY = "geometry"
        private const val ARG_QUESTTYPE = "quest_type"
        private const val ARG_MAP_ROTATION = "map_rotation"
        private const val ARG_MAP_TILT = "map_tilt"

        fun createArguments(questKey: QuestKey, questType: QuestType, geometry: ElementGeometry, rotation: Double, tilt: Double) = bundleOf(
            ARG_QUEST_KEY to Json.encodeToString(questKey),
            ARG_GEOMETRY to Json.encodeToString(geometry),
            ARG_QUESTTYPE to questType.name,
            ARG_MAP_ROTATION to rotation,
            ARG_MAP_TILT to tilt
        )
    }
}
