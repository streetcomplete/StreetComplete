package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.elementIdProvider
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ElementEditUploaderTest {

    @Mock private lateinit var changesetManager: OpenChangesetsManager
    @Mock private lateinit var mapDataApi: MapDataApi
    @Mock private lateinit var mapDataController: MapDataController
    private lateinit var uploader: ElementEditUploader

    // dummy
    @Mock val action: ElementEditAction = mock(classOf<ElementEditAction>())

    @BeforeTest fun setUp() {
        changesetManager = mock(classOf<OpenChangesetsManager>())
        mapDataApi = mock(classOf<MapDataApi>())
        mapDataController = mock(classOf<MapDataController>())

        uploader = ElementEditUploader(changesetManager, mapDataApi, mapDataController)
    }

    @Test
    fun `passes on conflict exception`() {
        val action: ElementEditAction = mock(classOf<ElementEditAction>())
        val edit: ElementEdit = edit(action = action)
        every { changesetManager.getOrCreateChangeset(any(), any(), any(), any()) }.returns(1)
        every { changesetManager.createChangeset(any(), any(), any()) }.returns(1)
        every { action.createUpdates(any(), any()) }.returns(MapDataChanges())
        every { mapDataApi.uploadChanges(any(), any(), any()) }.throws(ConflictException())

        assertFailsWith<ConflictException> {
            uploader.upload(edit, { elementIdProvider() })
        }
    }

    @Test
    fun `passes on element conflict exception`() {
        val action: ElementEditAction = mock(classOf<ElementEditAction>())
        val edit: ElementEdit = edit(action = action)
        every { action.createUpdates(any(), any()) }.returns(MapDataChanges())

        every { changesetManager.getOrCreateChangeset(any(), any(), any(), any()) }.returns(1)
        every { changesetManager.createChangeset(any(), any(), any()) }.returns(1)
        every { mapDataApi.uploadChanges(any(), any(), any()) }
            .throws(ConflictException())

        assertFailsWith<ConflictException> {
            uploader.upload(edit, { elementIdProvider() })
        }
    }

    @Test fun `handles changeset conflict exception`() {

        val action: ElementEditAction = mock(classOf<ElementEditAction>())
        val edit: ElementEdit = edit(action = action)
        every { action.createUpdates(any(), any()) }.returns(MapDataChanges())

        every { changesetManager.getOrCreateChangeset(any(), any(), any(), any()) }.returns(1)
        every { changesetManager.createChangeset(any(), any(), any()) }.returns(1)
        every { mapDataApi.uploadChanges(any(), any(), any()) }.throwsMany(ConflictException(), ConflictException())
        every { mapDataApi.uploadChanges(any(), any(), any()) }.returns(MapDataUpdates())

        uploader.upload(edit, { elementIdProvider() })
    }
}
