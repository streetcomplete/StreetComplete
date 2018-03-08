package de.westnordost.streetcomplete.data.osm;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OsmQuestUnlockerTest extends TestCase
{
	private static LatLon POS = new OsmLatLon(10,10);
	private static Node NODE = new OsmNode(1, 0, POS, null, null, null);

	private OsmNoteQuestDao osmNoteQuestDao;
	private OsmQuestDao osmQuestDao;
	private OsmQuestUnlocker osmQuestUnlocker;
	private OsmElementQuestType questType;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		ElementGeometryDao elementGeometryDao = mock(ElementGeometryDao.class);
		when(elementGeometryDao.get(Element.Type.NODE, 1)).thenReturn(new ElementGeometry(POS));

		osmNoteQuestDao = mock(OsmNoteQuestDao.class);
		when(osmNoteQuestDao.getAllPositions(any(BoundingBox.class)))
				.thenReturn(Collections.emptyList());

		osmQuestDao = mock(OsmQuestDao.class);
		when(osmQuestDao.getAll(null, null, null, Element.Type.NODE, 1L))
				.thenReturn(Collections.emptyList());

		questType = mock(OsmElementQuestType.class);
		final List<QuestType> questTypes = Collections.singletonList(questType);
		osmQuestUnlocker = new OsmQuestUnlocker(osmNoteQuestDao, osmQuestDao, elementGeometryDao,
				() -> questTypes);
	}

	public void testNoteBlocksNewQuests()
	{
		when(osmNoteQuestDao.getAllPositions(any(BoundingBox.class)))
				.thenReturn(Collections.singletonList(POS));

		assertTrue(osmQuestUnlocker.unlockNewQuests(NODE).isEmpty());
	}

	public void testPreviousQuestBlocksNewQuest()
	{
		OsmQuest q = new OsmQuest(questType, Element.Type.NODE, 1, new ElementGeometry(POS));
		when(osmQuestDao.getAll(null, null, null, Element.Type.NODE, 1L))
				.thenReturn(Collections.singletonList(q));

		assertTrue(osmQuestUnlocker.unlockNewQuests(NODE).isEmpty());
	}

	public void testQuestDoesNotApplyToElement()
	{
		when(questType.isApplicableTo(NODE)).thenReturn(false);

		assertTrue(osmQuestUnlocker.unlockNewQuests(NODE).isEmpty());
	}

	public void testAddsNewQuest()
	{
		when(questType.isApplicableTo(NODE)).thenReturn(true);
		List<OsmQuest> quests = osmQuestUnlocker.unlockNewQuests(NODE);

		assertEquals(1, quests.size());
		assertEquals(1,quests.get(0).getElementId());
		assertEquals(Element.Type.NODE,quests.get(0).getElementType());
		assertEquals(questType,quests.get(0).getType());
	}
}
