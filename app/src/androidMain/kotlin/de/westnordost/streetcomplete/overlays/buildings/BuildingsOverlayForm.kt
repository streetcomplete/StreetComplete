package de.westnordost.streetcomplete.overlays.buildings

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
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
import de.westnordost.streetcomplete.overlays.AGroupedImageSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithDescription
import de.westnordost.streetcomplete.util.getNameAndLocationSpanned
import de.westnordost.streetcomplete.util.ktx.valueOfOrNull
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class BuildingsOverlayForm : AGroupedImageSelectOverlayForm<BuildingTypeCategory, BuildingType>() {

    private val prefs: Preferences by inject()

    override val allItems = BuildingTypeCategory.entries

    override val itemsPerRow = 1

    override val lastPickedItems by lazy {
        prefs.getLastPicked(this::class.simpleName!!)
            .map { valueOfOrNull<BuildingType>(it) }
            .takeFavorites(
                n = 6,
                history = 50,
                first = 1,
                pad = BuildingType.topSelectableValues
            )
    }

    private var originalBuilding: BuildingType? = null

    @Composable override fun BoxScope.GroupContent(item: BuildingTypeCategory) {
        ImageWithDescription(
            painter = painterResource(item.icon),
            title = stringResource(item.title),
            description = item.description?.let { stringResource(it) }
        )
    }

    @Composable override fun BoxScope.ItemContent(item: BuildingType) {
        ImageWithDescription(
            painter = painterResource(item.icon),
            title = stringResource(item.title),
            description = item.description?.let { stringResource(it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalBuilding = createBuildingType(element!!.tags)
        selectedItem = originalBuilding

        setTitleHintLabel(getNameAndLocationSpanned(element!!, resources, null, true))
    }

    override fun hasChanges(): Boolean =
        selectedItem != originalBuilding

    override fun onClickOk() {
        prefs.addLastPicked(this::class.simpleName!!, selectedItem!!.name)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
