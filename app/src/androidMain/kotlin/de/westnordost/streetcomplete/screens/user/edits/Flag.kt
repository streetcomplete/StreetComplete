package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.exists
import de.westnordost.streetcomplete.ui.ktx.innerBorder
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.stretcomplete.resources.Res
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceItem
import org.jetbrains.compose.resources.painterResource

/** Flag image with a thin border around it so that a white flag color can be distinguished from the
 *  background */
@Composable
fun Flag(
    countryCode: String,
    modifier: Modifier = Modifier,
) {
    val resource = Res.flags.get(countryCode) ?: return
    val color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    Image(
        painter = painterResource(resource),
        contentDescription = countryCode,
        modifier = modifier.innerBorder(1.dp, color)
    )
}

/** Circular flag image with a thin border around it so that a white flag color can be distinguished
 *  from the background */
@Composable
fun CircularFlag(
    countryCode: String,
    modifier: Modifier = Modifier,
    flagAlignment: FlagAlignment = FlagAlignment.Center
) {
    val resource = Res.flags.get(countryCode) ?: return
    val painter = painterResource(resource)
    val color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)

    Image(
        painter = painter,
        contentDescription = countryCode,
        alignment = flagAlignment.alignment,
        contentScale = flagAlignment.contentScale,
        modifier = modifier
            .size(painter.intrinsicSize.minDimension.toInt().pxToDp())
            .innerBorder(1.dp, color, CircleShape)
            .clip(CircleShape)
    )
}

enum class FlagAlignment {
    Left,
    CenterLeft,
    Center,
    CenterRight,
    Right,
    Stretch;

    val alignment: Alignment get() = when (this) {
        Left ->        AbsoluteAlignment.CenterLeft
        CenterLeft ->  BiasAbsoluteAlignment(-0.5f, 0f)
        Center ->      Alignment.Center
        CenterRight -> BiasAbsoluteAlignment(+0.5f, 0f)
        Right ->       AbsoluteAlignment.CenterRight
        Stretch ->     Alignment.Center
    }

    val contentScale: ContentScale get() = when (this) {
        Stretch -> ContentScale.FillBounds
        else -> ContentScale.Crop
    }
}

@OptIn(InternalResourceApi::class)
private object Flags {
    private const val DIR = "composeResources/de.westnordost.stretcomplete.resources/files/flags/"
    private val flags = HashMap<String, Lazy<DrawableResource>?>()

    fun get(id: String): DrawableResource? {
        val cc = id.lowercase().replace('-', '_')
        if (cc !in flags) {
            flags[cc] = if (Res.exists("files/flags/cc.xml")) {
                createFlagDrawable(cc)
            } else {
                null
            }
        }
        return flags[cc]?.value
    }

    private fun createFlagDrawable(cc: String) = lazy {
        DrawableResource("flag:$cc", setOf(ResourceItem(setOf(), "${DIR}$cc.xml", -1, -1)))
    }
}

private val Res.flags get() = Flags
