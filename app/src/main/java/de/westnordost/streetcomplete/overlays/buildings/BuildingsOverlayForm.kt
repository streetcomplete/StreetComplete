package de.westnordost.streetcomplete.overlays.buildings

import android.content.Context
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
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.mostCommonWithin
import de.westnordost.streetcomplete.util.padWith
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import org.koin.android.ext.android.inject

class BuildingsOverlayForm : AGroupedImageSelectOverlayForm<BuildingType>() {

    private val prefs: Preferences by inject()
    private lateinit var favs: LastPickedValuesStore<GroupableDisplayItem<BuildingType>>

    override val allItems = BuildingTypeCategory.entries.toItems()

    override val itemsPerRow = 1

    private val maxLastPickedBuildings = 6

    override val lastPickedItems by lazy {
        favs.get()
            .mostCommonWithin(maxLastPickedBuildings, historyCount = 50, first = 1)
            .padWith(BuildingType.topSelectableValues.take(maxLastPickedBuildings).toItems())
            .toList()
    }

    private var originalBuilding: BuildingType? = null

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            prefs,
            key = javaClass.simpleName,
            serialize = { it.value!!.name },
            deserialize = { BuildingType.valueOf(it).asItem() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalBuilding = createBuildingType(element!!.tags)
        selectedItem = originalBuilding?.asItem()

        setTitleHintLabel(getNameAndLocationLabel(element!!, resources, null, true))
    }

    override fun hasChanges(): Boolean =
        selectedItem?.value != originalBuilding

    override fun onClickOk() {
        favs.add(selectedItem!!)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem!!.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
