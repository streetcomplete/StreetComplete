package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FavoriteQuestTypeControllerTest {

    private lateinit var favoriteQuestTypeDao: FavoriteQuestTypeDao
    private lateinit var editTypePresetsSource: EditTypePresetsSource
    private lateinit var ctrl: FavoriteQuestTypeController
    private lateinit var listener: FavoriteQuestTypeSource.Listener

    private lateinit var editTypePresetsListener: EditTypePresetsSource.Listener

    private val quest1 = TestQuestTypeA()
    private val quest2 = TestQuestTypeB()

    @BeforeTest fun setUp() {
        favoriteQuestTypeDao = mock()

        editTypePresetsSource = mock {
            every { addListener(any()) } calls { (listener: EditTypePresetsSource.Listener) ->
                editTypePresetsListener = listener
            }
            every { selectedId } returns 0
        }

        ctrl = FavoriteQuestTypeController(
            favoriteQuestTypeDao,
            editTypePresetsSource
        )

        listener = mock()
        ctrl.addListener(listener)
    }

    @Test fun `get favorites`() {
        every { favoriteQuestTypeDao.getAll(0) } returns listOf(
            quest1.name,
            quest2.name
        )

        assertEquals(
            listOf(quest1.name, quest2.name),
            ctrl.getFavorites()
        )
    }

    @Test fun `is favorite`() {
        every { favoriteQuestTypeDao.get(0, quest1.name) } returns quest1.name
        every { favoriteQuestTypeDao.get(0, quest2.name) } returns null

        assertTrue(ctrl.isFavorite(quest1.name, 0))
        assertFalse(ctrl.isFavorite(quest2.name, 0))
    }

    @Test fun `set favorite`() {
        ctrl.setFavorite(quest1, true)

        verify { favoriteQuestTypeDao.put(0, quest1.name) }
        verify { listener.onFavoriteChanged(quest1, true) }
    }

    @Test fun `unset favorite`() {
        ctrl.setFavorite(quest1, false)

        verify { favoriteQuestTypeDao.remove(0, quest1.name) }
        verify { listener.onFavoriteChanged(quest1, false) }
    }

    @Test fun `set favorite in non selected preset`() {
        ctrl.setFavorite(quest1, true, 1)

        verify { favoriteQuestTypeDao.put(1, quest1.name) }
        verifyNoMoreCalls(listener)
    }

    @Test fun `unset favorite in non selected preset`() {
        ctrl.setFavorite(quest1, false, 1)

        verify { favoriteQuestTypeDao.remove(1, quest1.name) }
        verifyNoMoreCalls(listener)
    }

    @Test fun `copy favorites`() {
        every { favoriteQuestTypeDao.getAll(0) } returns listOf(
            quest1.name,
            quest2.name
        )

        ctrl.copyFavorites(0, 1)

        verify {
            favoriteQuestTypeDao.putAll(
                1,
                listOf(quest1.name, quest2.name)
            )
        }
    }

    @Test fun `clear favorites`() {
        ctrl.clear()

        verify { favoriteQuestTypeDao.clear(0) }
        verify { listener.onFavoritesChanged() }
    }

    @Test fun `clear favorites in non selected preset`() {
        ctrl.clear(1)

        verify { favoriteQuestTypeDao.clear(1) }
        verifyNoMoreCalls(listener)
    }

    @Test fun `notifies listener when changing preset`() {
        editTypePresetsListener.onSelectionChanged()

        verify { listener.onFavoritesChanged() }
    }

    @Test fun `clears favorites when preset is deleted`() {
        editTypePresetsListener.onDeleted(1)

        verify { favoriteQuestTypeDao.clear(1) }
        verifyNoMoreCalls(listener)
    }
}
