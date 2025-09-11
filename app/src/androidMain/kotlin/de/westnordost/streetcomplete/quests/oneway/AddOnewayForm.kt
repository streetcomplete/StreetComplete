package de.westnordost.streetcomplete.quests.oneway

import android.os.Bundle
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.util.CircularClippedPainter
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddOnewayForm : AImageListQuestForm<OnewayAnswer, OnewayAnswer>() {

    override val items = OnewayAnswer.entries
    override val itemsPerRow = 3

    private var mapRotation: MutableFloatState = mutableFloatStateOf(0f)
    private var wayRotation: MutableFloatState = mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wayRotation.floatValue = (geometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
    }

    override fun onMapOrientation(rotation: Double, tilt: Double) {
        mapRotation.floatValue = rotation.toFloat()
    }

    @Composable override fun BoxScope.ItemContent(item: OnewayAnswer) {
        val painter = painterResource(item.icon)
        ImageWithLabel(
            painter = remember(painter) { CircularClippedPainter(painter) },
            label = stringResource(item.title),
            imageRotation = wayRotation.floatValue - mapRotation.floatValue
        )
    }

    override fun onClickOk(selectedItems: List<OnewayAnswer>) {
        applyAnswer(selectedItems.first())
    }
}
