package de.westnordost.streetcomplete.screens

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** All screens of the app. The back stack is saved, so routes need to be serializable. */
@Serializable
sealed interface Route : NavKey {
    @Serializable data object Main : Route
    @Serializable data object TeamModeWizard : Route

    @Serializable data class IntroTutorial(val onboarding: Boolean = false) : Route
    @Serializable data object OverlaysTutorial : Route

    @Serializable data class User(val launchAuth: Boolean = false) : Route

    @Serializable data object Settings : Route
    @Serializable data object EditTypePresets : Route
    @Serializable data object QuestSelection : Route
    @Serializable data object OverlaySelection : Route
    @Serializable data object LocaleSelection : Route
    @Serializable data object MessagesSelection : Route
    @Serializable data object ShowQuestForms : Route

    @Serializable data object About : Route
    @Serializable data object Changelog : Route
    @Serializable data object Credits : Route
    @Serializable data object PrivacyStatement : Route
    @Serializable data object Logs : Route
    @Serializable data object LogsFilters : Route
}

/** Go back to the previous screen. The main screen is never removed, as the back stack must not
 *  be empty (e.g. when the back button is clicked twice in quick succession). */
fun NavBackStack<Route>.goBack() {
    if (size > 1) removeAt(lastIndex)
}
