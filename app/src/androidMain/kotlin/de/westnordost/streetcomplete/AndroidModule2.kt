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
import de.westnordost.streetcomplete.screens.measure.ArQuestsDisabler
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.screens.measure.ArSupportCheckerImpl
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

    // quest definitions

    single<QuestTypeRegistry> {
        val countryInfos = get<CountryInfos>()
        val countryBoundariesLazy = get<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy"))
        val featureDictionaryLazy = get<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy"))
        questTypeRegistry(
            get(),
            { countryInfos.get(countryBoundariesLazy.value, it) },
            { countryBoundariesLazy.value.getIds(it).firstOrNull() },
            { featureDictionaryLazy.value.getFeature(it) }
        )
    }

    // overlays

    single<OverlayRegistry> {
        overlaysRegistry(
            { location ->
                val countryInfos = get<CountryInfos>()
                val countryBoundaries = get<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")).value
                countryInfos.get(countryBoundaries, location)
            },
            { location ->
                val countryBoundaries = get<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")).value
                countryBoundaries.getIds(location).firstOrNull()
            },
            { element ->
                get<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy")).value.getFeature(element)
            }
        )
    }

    // upload & download

    single { QuestAutoSyncer(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }

    // user statistics

    factory { StatisticsParser(editTypeAliases) }

    // AR

    factory<ArSupportChecker> { ArSupportCheckerImpl(get()) }
    factory { ArQuestsDisabler(get(), get()) }

    // settings screen view models

    viewModel<SettingsViewModel> { SettingsViewModelImpl(get(), get(), get(), get(), get(), get(), get()) }
    viewModel<QuestSelectionViewModel> { QuestSelectionViewModelImpl(get(), get(), get(), get(), get(named("CountryBoundariesLazy")), get()) }
    viewModel<ShowQuestFormsViewModel> { ShowQuestFormsViewModelImpl(get()) }
}
