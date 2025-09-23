package de.westnordost.streetcomplete.overlays.buildings

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.overlays.AGroupedItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.util.getNameAndLocationSpanned
import de.westnordost.streetcomplete.util.ktx.valueOfOrNull
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class BuildingsOverlayForm : AGroupedItemSelectOverlayForm<BuildingTypeCategory, BuildingType>() {

    private val prefs: Preferences by inject()

    override val groups = BuildingTypeCategory.entries

    override val lastPickedItems by lazy {
        prefs.getLastPicked(this::class.simpleName!!)
            .map { valueOfOrNull<BuildingType>(it) }
            .takeFavorites(
                n = 6,
                first = 1,
                pad = BuildingType.topSelectableValues
            )
    }

    private var originalBuilding: BuildingType? = null

    @Composable override fun GroupContent(item: BuildingTypeCategory) {
        ImageWithDescription(
            painter = painterResource(item.icon),
            title = stringResource(item.title),
            description = item.description?.let { stringResource(it) }
        )
    }

    @Composable override fun ItemContent(item: BuildingType) {
        ImageWithDescription(
            painter = painterResource(item.icon),
            title = stringResource(item.title),
            description = item.description?.let { stringResource(it) }
        )
    }

    @Composable override fun LastPickedItemContent(item: BuildingType) {
        Image(painterResource(item.icon), stringResource(item.title), Modifier.height(24.dp))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        originalBuilding = createBuildingType(element!!.tags)
        selectedItem.value = originalBuilding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitleHintLabel(getNameAndLocationSpanned(element!!, resources, null, true))
    }

    override fun hasChanges(): Boolean =
        selectedItem.value != originalBuilding

    override fun onClickOk(selectedItem: BuildingType) {
        prefs.addLastPicked(this::class.simpleName!!, selectedItem.name)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
