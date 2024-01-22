package de.westnordost.streetcomplete.overlays.buildings

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.osm.building.asItem
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.osm.building.toItems
import de.westnordost.streetcomplete.overlays.AGroupedImageSelectOverlayForm

class BuildingsOverlayForm : AGroupedImageSelectOverlayForm<BuildingType>() {

    override val allItems = BuildingTypeCategory.entries.toItems()
    override val topItems = BuildingType.topSelectableValues.toItems()

    override val itemsPerRow = 1

    private var originalBuilding: BuildingType? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalBuilding = createBuildingType(element!!.tags)
        selectedItem = originalBuilding?.asItem()
    }

    override fun hasChanges(): Boolean =
        selectedItem?.value != originalBuilding

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem!!.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
