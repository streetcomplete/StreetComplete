package de.westnordost.osmagent.data;

import android.os.Bundle;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmagent.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.osmagent.data.osm.persist.ElementGeometryDao;
import de.westnordost.osmagent.data.osm.persist.MergedElementDao;
import de.westnordost.osmagent.data.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.data.osmnotes.CreateNote;
import de.westnordost.osmagent.data.osmnotes.CreateNoteDao;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuest;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.osmagent.dialogs.NoteDiscussionForm;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

public class QuestController
{
	private QuestDispatcher questDispatcher;

	private final OsmQuestDao osmQuestDB;
	private final MergedElementDao osmElementDB;
	private final ElementGeometryDao geometryDB;
	private final OsmNoteQuestDao osmNoteQuestDB;
	private final CreateNoteDao createNoteDB;

	private final QuestDownloader questDownloader;

	@Inject public QuestController(OsmQuestDao osmQuestDB, MergedElementDao osmElementDB,
								   ElementGeometryDao geometryDB, OsmNoteQuestDao osmNoteQuestDB,
								   CreateNoteDao createNoteDB, QuestDownloader questDownloader)
	{
		this.osmQuestDB = osmQuestDB;
		this.osmElementDB = osmElementDB;
		this.geometryDB = geometryDB;
		this.osmNoteQuestDB = osmNoteQuestDB;
		this.createNoteDB = createNoteDB;
		this.questDownloader = questDownloader;

		questDispatcher = new QuestDispatcher();
		questDownloader.setQuestListener(questDispatcher);
	}

	public void createNote(final long osmQuestId, final String text)
	{
		new Thread()
		{
			@Override public void run()
			{
				OsmQuest q = osmQuestDB.get(osmQuestId);

				CreateNote createNote = new CreateNote();
				createNote.position = q.getMarkerLocation();
				createNote.text = text;
				createNote.elementType = q.getElementType();
				createNote.elementId = q.getElementId();
				createNoteDB.add(createNote);

				/** The quests that reference the same element for which the user was not able to
				 *  answer the question are removed because the to-be-created note blocks quest
				 *  creation for other users, so those quests should be removed from the user's
				 *  own display as well. As soon as the note is resolved, the quests will be re-
				 *  created next time they are downloaded */
				List<OsmQuest> quests = osmQuestDB.getAll(null, QuestStatus.NEW, null,
						q.getElementType(), q.getElementId());
				for(OsmQuest quest : quests)
				{
					osmQuestDB.delete(quest.getId());
					questDispatcher.onQuestRemoved(quest, QuestGroup.OSM);
				}
				osmElementDB.deleteUnreferenced();
				geometryDB.deleteUnreferenced();
			}
		}.start();
	}

	public void solveQuest(final long questId, final QuestGroup group, final Bundle answer)
	{
		new Thread()
		{
			@Override public void run()
			{
				if (group == QuestGroup.OSM)
				{
					OsmQuest q = osmQuestDB.get(questId);
					Element e = osmElementDB.get(q.getElementType(), q.getElementId());
					StringMapChangesBuilder changesBuilder = new StringMapChangesBuilder(e.getTags());
					q.getOsmElementQuestType().applyAnswerTo(answer, changesBuilder);
					q.setChanges(changesBuilder.create());
					q.setStatus(QuestStatus.ANSWERED);
					osmQuestDB.update(q);
					questDispatcher.onQuestRemoved(q, QuestGroup.OSM);
				}
				else if (group == QuestGroup.OSM_NOTE)
				{
					OsmNoteQuest q = osmNoteQuestDB.get(questId);
					q.setComment(answer.getString(NoteDiscussionForm.TEXT));
					q.setStatus(QuestStatus.ANSWERED);
					osmNoteQuestDB.update(q);
					questDispatcher.onQuestRemoved(q, QuestGroup.OSM_NOTE);
				}
			}
		}.start();
	}

	public void hideQuest(final long questId, final QuestGroup group)
	{
		new Thread()
		{
			@Override public void run()
			{
				if(group == QuestGroup.OSM)
				{
					OsmQuest q = osmQuestDB.get(questId);
					q.setStatus(QuestStatus.HIDDEN);
					osmQuestDB.update(q);
					questDispatcher.onQuestRemoved(q, QuestGroup.OSM);
				}
				else if(group == QuestGroup.OSM_NOTE)
				{
					OsmNoteQuest q = osmNoteQuestDB.get(questId);
					q.setStatus(QuestStatus.HIDDEN);
					osmNoteQuestDB.update(q);
					questDispatcher.onQuestRemoved(q, QuestGroup.OSM_NOTE);
				}
			}
		}.start();
	}

	public void retrieve(final long questId, final QuestGroup group)
	{
		new Thread()
		{
			@Override public void run()
			{
				Quest q;
				switch (group)
				{
					case OSM:
						q = osmQuestDB.get(questId);
						questDispatcher.onQuestCreated(q, QuestGroup.OSM);
						break;
					case OSM_NOTE:
						q = osmNoteQuestDB.get(questId);
						questDispatcher.onQuestCreated(q, QuestGroup.OSM_NOTE);
						break;
				}
			}
		}.start();
	}

	public void retrieve(final BoundingBox bbox)
	{
		new Thread()
		{
			@Override public void run()
			{
				for (Quest q : osmQuestDB.getAll(bbox, QuestStatus.NEW))
				{
					questDispatcher.onQuestCreated(q, QuestGroup.OSM);
				}
				for (Quest q : osmNoteQuestDB.getAll(bbox, QuestStatus.NEW))
				{
					questDispatcher.onQuestCreated(q, QuestGroup.OSM_NOTE);
				}
			}
		}.start();
	}

	public void download(BoundingBox bbox, Integer maxVisibleQuests)
	{
		questDownloader.download(bbox, maxVisibleQuests);
	}

	public void shutdown()
	{
		questDownloader.shutdown();
	}

	public void addQuestListener(VisibleQuestListener listener)
	{
		questDispatcher.addListener(listener);
	}

	public void removeQuestListener(VisibleQuestListener listener)
	{
		questDispatcher.removeListener(listener);
	}

	private class QuestDispatcher implements VisibleQuestListener
	{
		List<VisibleQuestListener> questListeners = new LinkedList<>();

		public void addListener(VisibleQuestListener listener)
		{
			questListeners.add(listener);
		}

		public void removeListener(VisibleQuestListener listener)
		{
			questListeners.remove(listener);
		}

		@Override public void onQuestCreated(Quest quest, QuestGroup group)
		{
			for(VisibleQuestListener listener : questListeners)
			{
				listener.onQuestCreated(quest, group);
			}
		}

		@Override public void onQuestRemoved(Quest quest, QuestGroup group)
		{
			for(VisibleQuestListener listener : questListeners)
			{
				listener.onQuestRemoved(quest, group);
			}
		}
	}
}
