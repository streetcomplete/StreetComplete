package de.westnordost.osmagent.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmagent.data.osm.changes.StringMapChanges;
import de.westnordost.osmagent.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.osmagent.data.osm.persist.ElementGeometryDao;
import de.westnordost.osmagent.data.osm.persist.MergedElementDao;
import de.westnordost.osmagent.data.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.data.osmnotes.CreateNote;
import de.westnordost.osmagent.data.osmnotes.CreateNoteDao;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuest;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.osmagent.quests.note_discussion.NoteDiscussionForm;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

import static android.content.Context.BIND_AUTO_CREATE;

public class QuestController
{
	private final OsmQuestDao osmQuestDB;
	private final MergedElementDao osmElementDB;
	private final ElementGeometryDao geometryDB;
	private final OsmNoteQuestDao osmNoteQuestDB;
	private final CreateNoteDao createNoteDB;
	private final Context context;

	private VisibleQuestRelay relay;

	private boolean downloadServiceIsBound;
	private QuestDownloadService.Interface downloadService;
	private ServiceConnection downloadServiceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			downloadService = ((QuestDownloadService.Interface)service);
			downloadService.setQuestListener(relay);
		}

		public void onServiceDisconnected(ComponentName className)
		{
			downloadService = null;
		}
	};

	@Inject public QuestController(OsmQuestDao osmQuestDB, MergedElementDao osmElementDB,
								   ElementGeometryDao geometryDB, OsmNoteQuestDao osmNoteQuestDB,
								   CreateNoteDao createNoteDB, Context context)
	{
		this.osmQuestDB = osmQuestDB;
		this.osmElementDB = osmElementDB;
		this.geometryDB = geometryDB;
		this.osmNoteQuestDB = osmNoteQuestDB;
		this.createNoteDB = createNoteDB;
		this.context = context;
		this.relay = new VisibleQuestRelay();
	}

	public void onStart(VisibleQuestListener questListener)
	{
		relay.setListener(questListener);
		downloadServiceIsBound = context.bindService(
				new Intent(context, QuestDownloadService.class),
				downloadServiceConnection, BIND_AUTO_CREATE);
	}

	public void onStop()
	{
		relay.setListener(null);
		if(downloadServiceIsBound) context.unbindService(downloadServiceConnection);
		if(downloadService != null) downloadService.setQuestListener(null);
	}

	/** Create a note for the given OSM Quest instead of answering it. The quest will turn
	 *  invisible. */
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
					relay.onOsmQuestRemoved(quest.getId());
				}
				osmElementDB.deleteUnreferenced();
				geometryDB.deleteUnreferenced();
			}
		}.start();
	}

	/** Apply the user's answer to the given quest. (The quest will turn invisible.) */
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
					StringMapChanges changes = changesBuilder.create();
					if(!changes.isEmpty())
					{
						q.setChanges(changes);
						q.setStatus(QuestStatus.ANSWERED);
						osmQuestDB.update(q);
						relay.onOsmQuestRemoved(q.getId());
					}
					else
					{
						throw new RuntimeException(
								"OsmQuest " + questId + " (" + q.getType().getClass().getSimpleName() +
								") has been answered by the user but the changeset is empty!");
					}
				}
				else if (group == QuestGroup.OSM_NOTE)
				{
					OsmNoteQuest q = osmNoteQuestDB.get(questId);
					String comment = answer.getString(NoteDiscussionForm.TEXT);
					if(comment != null && comment.isEmpty())
					{
						q.setComment(comment);
						q.setStatus(QuestStatus.ANSWERED);
						osmNoteQuestDB.update(q);
						relay.onNoteQuestRemoved(q.getId());
					}
					else
					{
						throw new RuntimeException(
								"NoteQuest has been answered with an empty comment!");
					}
				}
			}
		}.start();
	}

	/** Make the given quest invisible asynchronously. */
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
					relay.onOsmQuestRemoved(q.getId());
				}
				else if(group == QuestGroup.OSM_NOTE)
				{
					OsmNoteQuest q = osmNoteQuestDB.get(questId);
					q.setStatus(QuestStatus.HIDDEN);
					osmNoteQuestDB.update(q);
					relay.onNoteQuestRemoved(q.getId());
				}
			}
		}.start();
	}

	/** Retrieve the given quest from local database asynchronously, including the element / note. */
	public void retrieve(final QuestGroup group, final long questId)
	{
		new Thread()
		{
			@Override public void run()
			{
				switch (group)
				{
					case OSM:
						OsmQuest osmQuest = osmQuestDB.get(questId);
						Element element = osmElementDB.get(osmQuest.getElementType(), osmQuest.getElementId());
						relay.onQuestCreated(osmQuest, element);
						break;
					case OSM_NOTE:
						OsmNoteQuest osmNoteQuest = osmNoteQuestDB.get(questId);
						relay.onQuestCreated(osmNoteQuest);
						break;
				}
			}
		}.start();
	}

	/** Retrieve all visible (=new) quests in the given bounding box from local database
	 *  asynchronously. */
	public void retrieve(final BoundingBox bbox)
	{
		new Thread()
		{
			@Override public void run()
			{
				for (OsmQuest q : osmQuestDB.getAll(bbox, QuestStatus.NEW))
				{
					relay.onQuestCreated(q, null);
				}
				for (OsmNoteQuest q : osmNoteQuestDB.getAll(bbox, QuestStatus.NEW))
				{
					relay.onQuestCreated(q);
				}
			}
		}.start();
	}

	/** Download quests in the given bounding box asynchronously. maxVisibleQuests = null for no
	 *  limit. Multiple calls to this method will cancel the previous download job. */
	public void download(BoundingBox bbox, Integer maxVisibleQuests, boolean manualStart)
	{
		Intent intent = new Intent(context, QuestDownloadService.class);
		QuestDownloadService.putBBox(bbox, intent);
		if(maxVisibleQuests != null)
		{
			intent.putExtra(QuestDownloadService.ARG_MAX_VISIBLE_QUESTS, maxVisibleQuests);
		}
		if(manualStart)
		{
			intent.putExtra(QuestDownloadService.ARG_MANUAL, true);
		}
		context.startService(intent);
	}

	/** @return true if a quest download triggered by the user is running */
	public boolean isManualDownloadRunning()
	{
		return downloadService != null && downloadService.isManualDownloadRunning();
	}

	/** Collect and upload all changes made by the user */
	public void upload()
	{
		context.startService(new Intent(context, QuestChangesUploadService.class));
	}
}
