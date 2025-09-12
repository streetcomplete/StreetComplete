package de.westnordost.streetcomplete.quests.incline_direction

import android.os.Bundle
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_steps_incline_up
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddInclineForm : AImageListQuestForm<Incline, Incline>() {
    override val items = Incline.entries
    override val itemsPerRow = 2

    private var mapRotation: MutableFloatState = mutableFloatStateOf(0f)
    private var wayRotation: MutableFloatState = mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wayRotation.floatValue = (geometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
    }

    override fun onMapOrientation(rotation: Double, tilt: Double) {
        mapRotation.floatValue = rotation.toFloat()
    }

    @Composable override fun BoxScope.ItemContent(item: Incline) {
        ImageWithLabel(
            painter = painterResource(item.icon),
            label = stringResource(Res.string.quest_steps_incline_up),
            imageRotation = wayRotation.floatValue - mapRotation.floatValue
        )
    }

    override fun onClickOk(selectedItems: List<Incline>) {
        applyAnswer(selectedItems.first())
    }
}
