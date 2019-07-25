package de.westnordost.streetcomplete.data.osm.upload

import android.os.CancellationSignal
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.UndoOsmQuest
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*

class UndoOsmQuestsUploadTest {
    private lateinit var undoQuestDB: UndoOsmQuestDao
    private lateinit var elementDB: MergedElementDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var singleChangeUpload: SingleOsmElementTagChangesUpload
    private lateinit var uploader: UndoOsmQuestsUpload

    @Before fun setUp() {
        undoQuestDB = mock(UndoOsmQuestDao::class.java)
        elementDB = mock(MergedElementDao::class.java)
        changesetManager = mock(OpenQuestChangesetsManager::class.java)
        singleChangeUpload = mock(SingleOsmElementTagChangesUpload::class.java)
        uploader = UndoOsmQuestsUpload(undoQuestDB, elementDB, changesetManager, singleChangeUpload)
    }

    @Test fun `cancel upload works`() {
        val signal = CancellationSignal()
        signal.cancel()
        uploader.upload(signal)
        verifyZeroInteractions(changesetManager, singleChangeUpload, elementDB, undoQuestDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(mock(UndoOsmQuest::class.java)))
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())

        uploader.upload(CancellationSignal())

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(mock(UndoOsmQuest::class.java)))
        on(elementDB.get(any(), anyLong())).thenReturn(null)

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(CancellationSignal())

        verify(uploader.uploadedChangeListener)?.onDiscarded()
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(
            mock(UndoOsmQuest::class.java)
        ))
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(mock(Element::class.java))

        uploader.upload(CancellationSignal())

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(singleChangeUpload, times(2)).upload(anyLong(), any(), any())
    }

    @Test fun `delete each uploaded quest from local DB and calls listener`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(
            mock(UndoOsmQuest::class.java),
            mock(UndoOsmQuest::class.java)
        ))
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())
            .thenReturn(mock(Element::class.java))

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(CancellationSignal())

        verify(undoQuestDB, times(2)).delete(0L)
        verify(uploader.uploadedChangeListener)?.onUploaded()
        verify(uploader.uploadedChangeListener)?.onDiscarded()
    }
}
