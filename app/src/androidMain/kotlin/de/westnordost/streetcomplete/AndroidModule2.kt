package de.westnordost.streetcomplete

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestAutoSyncer
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.achievements.editTypeAliases
import de.westnordost.streetcomplete.data.user.statistics.StatisticsParser
import de.westnordost.streetcomplete.overlays.overlaysRegistry
import de.westnordost.streetcomplete.quests.questTypeRegistry
import de.westnordost.streetcomplete.screens.settings.SettingsViewModel
import de.westnordost.streetcomplete.screens.settings.SettingsViewModelImpl
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsViewModel
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsViewModelImpl
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionViewModel
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionViewModelImpl
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.util.ktx.getFeature
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

// stuff that should go into commonModule soon
val androidModule2 = module {

    // upload & download

    single { QuestAutoSyncer(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
