package de.westnordost.streetcomplete.data.osm.osmquests

import dagger.Module
import dagger.Provides

@Module object OsmQuestModule {
    @Provides fun osmQuestsSource(osmQuestController: OsmQuestController): OsmQuestSource = osmQuestController
}
