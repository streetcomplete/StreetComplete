package de.westnordost.streetcomplete.osm.cycleway

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.oneway.Direction
import de.westnordost.streetcomplete.osm.oneway.Direction.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.advisoryCycleLaneDrawable
import de.westnordost.streetcomplete.util.ktx.advisoryCycleLaneMirroredDrawable
import de.westnordost.streetcomplete.util.ktx.dualCycleLaneDrawable
import de.westnordost.streetcomplete.util.ktx.dualCycleLaneMirroredDrawable
import de.westnordost.streetcomplete.util.ktx.exclusiveCycleLaneDrawable
import de.westnordost.streetcomplete.util.ktx.exclusiveCycleLaneMirroredDrawable
import de.westnordost.streetcomplete.util.ktx.noEntrySignDrawable
import de.westnordost.streetcomplete.util.ktx.pictogramCycleLaneDrawable
import de.westnordost.streetcomplete.util.ktx.pictogramCycleLaneMirroredDrawable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CyclewayItem(
    cycleway: CyclewayAndDirection,
    isRight: Boolean,
    roadDirection: Direction,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
) {
    val icon = cycleway.getIcon(isRight, countryInfo, roadDirection) ?: return
    val floatingIcon = cycleway.getFloatingIcon(roadDirection, countryInfo.noEntrySignDrawable)
    val title = cycleway.getTitle(roadDirection) ?: return
    val imageRotation = if (countryInfo.isLeftHandTraffic) 180f else 0f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .rotate(imageRotation),
            )
            if (floatingIcon != null) {
                Image(
                    painter = painterResource(floatingIcon),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
        }
        Text(
            text = stringResource(title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption.copy(hyphens = Hyphens.Auto),
            modifier = Modifier.padding(2.dp)
        )
    }
}

fun CyclewayAndDirection.getFloatingIcon(
    roadDirection: Direction,
    noEntrySignDrawable: DrawableResource
): DrawableResource? =
    when (cycleway) {
        NONE ->
            if (direction.isReverseOf(roadDirection)) noEntrySignDrawable
            else null
        SEPARATE -> Res.drawable.floating_separate
        SIDEWALK_OK ->
            if (direction == BOTH) Res.drawable.cycleway_sign_sidewalk_ok_dual
            else Res.drawable.cycleway_sign_sidewalk_ok
        else ->     null
    }

fun CyclewayAndDirection.getIcon(
    isRight: Boolean,
    countryInfo: CountryInfo,
    roadDirection: Direction,
): DrawableResource? =
    when (direction) {
        BOTH -> cycleway.getDualTrafficIcon(countryInfo)
        else -> {
            val isForward = (direction == FORWARD)
            val showMirrored = isForward xor isRight
            val isContraflowInOneway = direction.isReverseOf(roadDirection)
            if (showMirrored) {
                cycleway.getLeftHandTrafficIcon(countryInfo, isContraflowInOneway)
            } else {
                cycleway.getRightHandTrafficIcon(countryInfo, isContraflowInOneway)
            }
        }
    }

private fun Cycleway.getDualTrafficIcon(countryInfo: CountryInfo): DrawableResource? =
    when (this) {
        UNSPECIFIED_LANE, EXCLUSIVE_LANE ->
            if (countryInfo.isLeftHandTraffic) {
                countryInfo.dualCycleLaneMirroredDrawable
            } else {
                countryInfo.dualCycleLaneDrawable
            }
        TRACK ->
            if (countryInfo.isLeftHandTraffic) {
                Res.drawable.cycleway_track_dual_l
            } else {
                Res.drawable.cycleway_track_dual
            }
        SIDEWALK_EXPLICIT -> Res.drawable.cycleway_sidewalk_explicit_dual
        SIDEWALK_OK ->       Res.drawable.cycleway_sidewalk
        else ->              null
    }

private fun Cycleway.getRightHandTrafficIcon(
    countryInfo: CountryInfo,
    isContraflowInOneway: Boolean
): DrawableResource? =
    when (this) {
        UNSPECIFIED_LANE ->  countryInfo.exclusiveCycleLaneDrawable
        EXCLUSIVE_LANE ->    countryInfo.exclusiveCycleLaneDrawable
        ADVISORY_LANE ->     countryInfo.advisoryCycleLaneDrawable
        SUGGESTION_LANE ->   countryInfo.advisoryCycleLaneDrawable
        TRACK ->             Res.drawable.cycleway_track
        NONE ->              Res.drawable.cycleway_none
        NONE_NO_ONEWAY -> {
            if (isContraflowInOneway) {
                Res.drawable.cycleway_none_no_oneway
            } else {
                Res.drawable.cycleway_none
            }
        }
        PICTOGRAMS ->        countryInfo.pictogramCycleLaneDrawable
        SIDEWALK_EXPLICIT -> Res.drawable.cycleway_sidewalk_explicit
        SIDEWALK_OK ->       Res.drawable.cycleway_sidewalk
        BUSWAY ->            Res.drawable.cycleway_bus_lane
        SEPARATE ->          Res.drawable.cycleway_none
        SHOULDER ->          Res.drawable.cycleway_shoulder
        else -> null
    }

private fun Cycleway.getLeftHandTrafficIcon(
    countryInfo: CountryInfo,
    isContraflowInOneway: Boolean
): DrawableResource? =
    when (this) {
        UNSPECIFIED_LANE ->  countryInfo.exclusiveCycleLaneMirroredDrawable
        EXCLUSIVE_LANE ->    countryInfo.exclusiveCycleLaneMirroredDrawable
        ADVISORY_LANE ->     countryInfo.advisoryCycleLaneMirroredDrawable
        SUGGESTION_LANE ->   countryInfo.advisoryCycleLaneMirroredDrawable
        TRACK ->             Res.drawable.cycleway_track_l
        NONE ->              Res.drawable.cycleway_none
        NONE_NO_ONEWAY -> {
            if (isContraflowInOneway) {
                Res.drawable.cycleway_none_no_oneway_l
            } else {
                Res.drawable.cycleway_none
            }
        }
        PICTOGRAMS ->        countryInfo.pictogramCycleLaneMirroredDrawable
        SIDEWALK_EXPLICIT -> Res.drawable.cycleway_sidewalk_explicit_l
        SIDEWALK_OK ->       Res.drawable.cycleway_sidewalk
        BUSWAY ->            Res.drawable.cycleway_bus_lane_l
        SEPARATE ->          Res.drawable.cycleway_none
        SHOULDER ->          Res.drawable.cycleway_shoulder
        else -> null
    }

fun CyclewayAndDirection.getTitle(roadDirection: Direction): StringResource? =
    when (cycleway) {
        UNSPECIFIED_LANE, EXCLUSIVE_LANE -> {
            if (direction == BOTH) {
                Res.string.quest_cycleway_value_lane_dual
            } else {
                Res.string.quest_cycleway_value_lane
            }
        }
        TRACK -> {
            if (direction == BOTH) {
                Res.string.quest_cycleway_value_track_dual
            } else {
                Res.string.quest_cycleway_value_track
            }
        }
        SIDEWALK_EXPLICIT -> {
            if (direction == BOTH) {
                Res.string.quest_cycleway_value_sidewalk_dual2
            } else {
                Res.string.quest_cycleway_value_sidewalk2
            }
        }
        NONE -> {
            if (direction.isReverseOf(roadDirection)) {
                Res.string.quest_cycleway_value_none_and_oneway
            } else {
                Res.string.quest_cycleway_value_none
            }
        }
        ADVISORY_LANE,
        SUGGESTION_LANE ->   Res.string.quest_cycleway_value_advisory_lane
        NONE_NO_ONEWAY -> {
            if (direction.isReverseOf(roadDirection)) {
                Res.string.quest_cycleway_value_none_but_no_oneway
            } else {
                Res.string.quest_cycleway_value_none
            }
        }
        PICTOGRAMS ->        Res.string.quest_cycleway_value_shared
        BUSWAY ->            Res.string.quest_cycleway_value_bus_lane
        SEPARATE ->          Res.string.quest_cycleway_value_separate
        SHOULDER ->          Res.string.quest_cycleway_value_shoulder
        SIDEWALK_OK ->       {
            if (direction == BOTH) {
                Res.string.quest_cycleway_value_sidewalk_ok_dual
            } else {
                Res.string.quest_cycleway_value_sidewalk_ok
            }
        }
        else -> null
    }
