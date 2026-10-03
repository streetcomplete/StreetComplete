package de.westnordost.streetcomplete.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.savedstate.compose.LocalSavedStateRegistryOwner

/* Navigation 3 has no nested graphs to scope a view model shared by several screens to. Instead,
 * an entry can declare a parent entry whose view models it can then access, e.g. a screen and a
 * sub-screen that edits its state. Adapted from the shared view model recipe of nav3-recipes. */

/** The view model store of the entry declared as parent with [parentViewModelStore] */
val LocalParentViewModelStoreOwner = staticCompositionLocalOf<ViewModelStoreOwner> {
    error("No parent declared for this entry")
}

/** Entry metadata to declare the entry with the given [contentKey] as parent, see
 *  [LocalParentViewModelStoreOwner] */
fun parentViewModelStore(contentKey: Any) = metadata { put(ParentContentKey, contentKey) }

private data object ParentContentKey : NavMetadataKey<Any>

/** Provides [LocalParentViewModelStoreOwner] to entries that declared a parent. [provider] must be
 *  the same one that the view model store decorator uses. */
@Composable
fun <T : Any> rememberParentViewModelStoreNavEntryDecorator(
    provider: ViewModelStoreProvider
): NavEntryDecorator<T> = remember(provider) {
    NavEntryDecorator { entry ->
        val parentContentKey = entry.metadata[ParentContentKey]
        if (parentContentKey == null) {
            entry.Content()
        } else {
            val parentOwner = rememberViewModelStoreOwner(
                key = parentContentKey,
                provider = provider,
                savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
            )
            CompositionLocalProvider(LocalParentViewModelStoreOwner provides parentOwner) {
                entry.Content()
            }
        }
    }
}
