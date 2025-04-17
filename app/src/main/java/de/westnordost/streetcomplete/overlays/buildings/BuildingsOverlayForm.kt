package de.westnordost.streetcomplete.overlays.buildings

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.osm.building.asItem
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.osm.building.toItems
import de.westnordost.streetcomplete.overlays.AGroupedImageSelectOverlayForm
import de.westnordost.streetcomplete.util.getNameAndLocationSpanned
import de.westnordost.streetcomplete.util.ktx.valueOfOrNull
import de.westnordost.streetcomplete.util.takeFavourites
import org.koin.android.ext.android.inject

class BuildingsOverlayForm : AGroupedImageSelectOverlayForm<BuildingType>() {

    private val prefs: Preferences by inject()

    override val allItems = BuildingTypeCategory.entries.toItems()

    override val itemsPerRow = 1

    override val lastPickedItems by lazy {
        prefs.getLastPicked(this::class.simpleName!!)
            .map { valueOfOrNull<BuildingType>(it)?.asItem() }
            .takeFavourites(
                n = 6,
                history = 50,
                first = 1,
                pad = BuildingType.topSelectableValues.toItems()
            )
    }

    private var originalBuilding: BuildingType? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalBuilding = createBuildingType(element!!.tags)
        selectedItem = originalBuilding?.asItem()

        setTitleHintLabel(getNameAndLocationSpanned(element!!, resources, null, true))
    }

    override fun hasChanges(): Boolean =
        selectedItem?.value != originalBuilding

    override fun onClickOk() {
        prefs.addLastPicked(this::class.simpleName!!, selectedItem!!.value!!.name)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem!!.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
