package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.any
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk_surface.SidewalkSurface
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.koin.android.ext.android.inject
import kotlin.getValue

class AddSidewalkSurfaceForm : AbstractOsmQuestForm<SidewalkSurfaceAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    override val contentPadding = false

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_sidewalk_answer_different) {
            applyAnswer(SidewalkSurfaceAnswer.SidewalkIsDifferent)
        }
    )

    private lateinit var sidewalkSurfaces: MutableState<Sides<Surface>>
    private var hasSidewalkLeft: Boolean = false
    private var hasSidewalkRight: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sides = parseSidewalkSides(element.tags)
        hasSidewalkLeft = sides?.left == Sidewalk.YES
        hasSidewalkRight = sides?.right == Sidewalk.YES
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lastPicked by lazy {
            if (hasSidewalkLeft && hasSidewalkRight) {
                prefs.getLastPicked<Sides<Surface>>(this::class.simpleName!!)
            } else {
                emptyList()
            }
        }

        binding.composeViewBase.content { Surface {
            sidewalkSurfaces = rememberSerializable { mutableStateOf(Sides(null, null)) }

            SidewalkSurfaceForm(
                value = sidewalkSurfaces.value,
                onValueChanged = {
                    sidewalkSurfaces.value = it
                    checkIsFormComplete()
                },
                geometryRotation = geometryRotation.floatValue,
                mapRotation = mapRotation.floatValue,
                mapTilt = mapTilt.floatValue,
                isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                lastPicked = lastPicked,
                isLeftSideVisible = hasSidewalkLeft,
                isRightSideVisible = hasSidewalkRight,
            )
            checkIsFormComplete()
        } }
    }

    override fun isFormComplete() =
        (!hasSidewalkLeft || sidewalkSurfaces.value.left != null) &&
        (!hasSidewalkRight || sidewalkSurfaces.value.right != null)

    override fun isRejectingClose() =
        sidewalkSurfaces.value.any { it != null }

    override fun onClickOk() {
        applyAnswer(SidewalkSurfaceAnswer.Surfaces(SidewalkSurface(sidewalkSurfaces.value)))
        if (hasSidewalkLeft && hasSidewalkRight) {
            prefs.setLastPicked(this::class.simpleName!!, listOf(sidewalkSurfaces.value))
        }
    }
}
