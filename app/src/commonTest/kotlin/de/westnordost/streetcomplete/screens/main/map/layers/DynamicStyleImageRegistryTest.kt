package de.westnordost.streetcomplete.screens.main.map.layers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield

class DynamicStyleImageRegistryTest {
    @Test
    fun installedImagesAreRetainedUntilTheNextStyleLoads() = runTest {
        val registry = DynamicStyleImageRegistry()

        registry.recordInstalled("first", generation = 0)
        assertEquals(setOf("second"), registry.pendingImageIds(setOf("first", "second")))

        // Removing an owner must not make its image eligible for reinstallation in this style.
        registry.remove("unused-owner")
        assertEquals(emptySet(), registry.pendingImageIds(setOf("first")))

        registry.onStyleLoaded()
        assertEquals(setOf("first", "second"), registry.pendingImageIds(setOf("first", "second")))
    }

    @Test
    fun anOldGenerationCannotMarkAnImageInstalledInTheNewStyle() {
        val registry = DynamicStyleImageRegistry()

        registry.onStyleLoaded()
        registry.recordInstalled("icon", generation = 0)

        assertEquals(setOf("icon"), registry.pendingImageIds(setOf("icon")))
    }

    @Test
    fun imagePublicationWaitsForEveryRequiredImage() = runTest {
        val registry = DynamicStyleImageRegistry()
        val waiting = async {
            registry.installedImages.first { it.containsAll(setOf("first", "second")) }
        }

        registry.recordInstalled("first", generation = 0)
        yield()
        assertEquals(false, waiting.isCompleted)

        registry.recordInstalled("second", generation = 0)
        waiting.await()
        registry.onStyleLoaded()
        assertEquals(emptySet(), registry.installedImages.value)
    }
}
