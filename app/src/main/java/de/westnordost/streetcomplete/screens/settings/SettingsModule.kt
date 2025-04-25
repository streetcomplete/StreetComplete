package de.westnordost.streetcomplete.screens.settings

import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsViewModel
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsViewModelImpl
import de.westnordost.streetcomplete.screens.settings.overlay_selection.OverlaySelectionViewModel
import de.westnordost.streetcomplete.screens.settings.overlay_selection.OverlaySelectionViewModelImpl
import de.westnordost.streetcomplete.screens.settings.presets.EditTypePresetsViewModel
import de.westnordost.streetcomplete.screens.settings.presets.EditTypePresetsViewModelImpl
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionViewModel
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionViewModelImpl
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val settingsModule = module {
    viewModel<SettingsViewModel> { SettingsViewModelImpl(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel<QuestSelectionViewModel> { QuestSelectionViewModelImpl(get(), get(), get(), get(), get(), get(named("CountryBoundariesLazy")), get()) }
    viewModel<OverlaySelectionViewModel> { OverlaySelectionViewModelImpl(get(), get(), get()) }
    viewModel<EditTypePresetsViewModel> { EditTypePresetsViewModelImpl(get(), get(), get(), get()) }
    viewModel<ShowQuestFormsViewModel> { ShowQuestFormsViewModelImpl(get(), get()) }
}
