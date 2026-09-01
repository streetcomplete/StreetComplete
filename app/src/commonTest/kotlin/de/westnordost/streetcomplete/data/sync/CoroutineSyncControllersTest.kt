package de.westnordost.streetcomplete.data.sync

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineSyncControllersTest {

    @Test
    fun uploadKeepsActiveWork() = runTest {
        val started = CompletableDeferred<Unit>()
        val finish = CompletableDeferred<Unit>()
        var uploads = 0
        val controller = CoroutineUploadController(this) {
            uploads++
            started.complete(Unit)
            finish.await()
        }

        controller.upload(isUserInitiated = false)
        started.await()
        controller.upload(isUserInitiated = true)
        runCurrent()

        assertEquals(1, uploads)
        finish.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun uploadStartsAgainAfterWorkFinishes() = runTest {
        var uploads = 0
        val controller = CoroutineUploadController(this) { uploads++ }

        controller.upload(isUserInitiated = false)
        advanceUntilIdle()
        controller.upload(isUserInitiated = false)
        advanceUntilIdle()

        assertEquals(2, uploads)
    }

    @Test
    fun automaticDownloadKeepsActiveWorkButUserDownloadReplacesIt() = runTest {
        val first = BoundingBox(0.0, 0.0, 1.0, 1.0)
        val second = BoundingBox(1.0, 1.0, 2.0, 2.0)
        var firstCancelled = false
        val downloads = mutableListOf<Pair<BoundingBox, Boolean>>()
        val controller = CoroutineDownloadController(this) { bbox, userInitiated ->
            downloads += bbox to userInitiated
            if (bbox == first) {
                try {
                    awaitCancellation()
                } finally {
                    firstCancelled = true
                }
            }
        }

        controller.download(first, isUserInitiated = false)
        runCurrent()
        controller.download(second, isUserInitiated = false)
        runCurrent()
        assertEquals(listOf(first to false), downloads)
        assertFalse(firstCancelled)

        controller.download(second, isUserInitiated = true)
        runCurrent()
        assertEquals(listOf(first to false, second to true), downloads)
        assertTrue(firstCancelled)
    }

    @Test
    fun changesetCloseIsDelayedAndRescheduled() = runTest {
        var closes = 0
        val controller = CoroutineChangesetAutoCloser(this) { closes++ }

        controller.enqueue(delayInMilliseconds = 1_000)
        advanceTimeBy(750)
        controller.enqueue(delayInMilliseconds = 1_000)
        advanceTimeBy(999)
        runCurrent()
        assertEquals(0, closes)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, closes)
    }
}
