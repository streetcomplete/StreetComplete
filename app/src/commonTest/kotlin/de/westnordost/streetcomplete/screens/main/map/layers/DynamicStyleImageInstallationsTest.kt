package de.westnordost.streetcomplete.screens.main.map.layers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.maplibre.compose.map.StyleLoadState

class DynamicStyleImageInstallationsTest {
    @Test
    fun `installed images survive publisher recreation while the style remains loaded`() {
        val installations = DynamicStyleImageInstallations()
        val style = Any()

        assertEquals(
            setOf("quest", "overlay"),
            installations.pendingImageIds(
                style,
                StyleLoadState.Ready,
                setOf("quest", "overlay"),
            ),
        )
        installations.recordInstalled("quest")
        installations.recordInstalled("overlay")

        assertEquals(
            emptySet(),
            installations.pendingImageIds(
                style,
                StyleLoadState.Ready,
                setOf("quest", "overlay"),
            ),
        )
    }

    @Test
    fun `installed images reset after the loaded style is replaced`() {
        val installations = DynamicStyleImageInstallations()
        val style = Any()
        val ids = setOf("quest")

        assertEquals(ids, installations.pendingImageIds(style, StyleLoadState.Ready, ids))
        installations.recordInstalled("quest")
        assertEquals(emptySet(), installations.pendingImageIds(style, StyleLoadState.Pending, ids))
        assertEquals(ids, installations.pendingImageIds(style, StyleLoadState.Ready, ids))
    }

    @Test
    fun `installed images reset when the base style changes`() {
        val installations = DynamicStyleImageInstallations()
        val ids = setOf("quest")

        assertEquals(ids, installations.pendingImageIds("first", StyleLoadState.Ready, ids))
        installations.recordInstalled("quest")

        assertEquals(ids, installations.pendingImageIds("second", StyleLoadState.Ready, ids))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `data publication waits until every required image is installed`() = runTest {
        val registry = DynamicStyleImageRegistry()
        val ids = setOf("first", "second")
        assertEquals(ids, registry.pendingImageIds(Any(), StyleLoadState.Ready, ids))

        val waiting = launch { registry.awaitInstalled(ids) }
        runCurrent()
        assertFalse(waiting.isCompleted)

        registry.recordInstalled("first")
        runCurrent()
        assertFalse(waiting.isCompleted)

        registry.recordInstalled("second")
        advanceUntilIdle()
        assertTrue(waiting.isCompleted)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `leaving ready clears the observable installed set`() = runTest {
        val registry = DynamicStyleImageRegistry()
        val style = Any()
        val ids = setOf("quest")
        registry.pendingImageIds(style, StyleLoadState.Ready, ids)
        registry.recordInstalled("quest")
        registry.awaitInstalled(ids)

        registry.pendingImageIds(style, StyleLoadState.Loading, ids)
        val waiting = launch { registry.awaitInstalled(ids) }
        runCurrent()
        assertFalse(waiting.isCompleted)

        assertEquals(ids, registry.pendingImageIds(style, StyleLoadState.Ready, ids))
        registry.recordInstalled("quest")
        advanceUntilIdle()
        assertTrue(waiting.isCompleted)
    }
}
