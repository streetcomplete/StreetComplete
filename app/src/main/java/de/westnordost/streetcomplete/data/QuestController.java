package de.westnordost.streetcomplete.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.download.QuestDownloadService;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osmnotes.CreateNote;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuest;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm;
import de.westnordost.streetcomplete.util.SlippyMapMath;
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
	private final OpenChangesetsDao openChangesetsDao;
	private final Context context;
	private final VisibleQuestRelay relay;

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

	private Handler workerHandler;
	private HandlerThread worker;

	@Inject public QuestController(OsmQuestDao osmQuestDB, MergedElementDao osmElementDB,
								   ElementGeometryDao geometryDB, OsmNoteQuestDao osmNoteQuestDB,
								   CreateNoteDao createNoteDB, OpenChangesetsDao openChangesetsDao,
								   Context context)
	{
		this.osmQuestDB = osmQuestDB;
		this.osmElementDB = osmElementDB;
		this.geometryDB = geometryDB;
		this.osmNoteQuestDB = osmNoteQuestDB;
		this.createNoteDB = createNoteDB;
		this.openChangesetsDao = openChangesetsDao;
		this.context = context;
		this.relay = new VisibleQuestRelay();
	}

	public void onCreate()
	{
		worker = new HandlerThread("QuestControllerThread");
		worker.start();
		workerHandler = new Handler(worker.getLooper());
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

	public void onDestroy()
	{
		worker.quit();
	}

	/** Create a note for the given OSM Quest instead of answering it. The quest will turn
	 *  invisible. */
	public void createNote(final long osmQuestId, final String questTitle, final String text)
	{
		workerHandler.post(new Runnable() { @Override public void run()
		{
			OsmQuest q = osmQuestDB.get(osmQuestId);
			// race condition: another thread may have removed the element already (#288)
			if(q == null) return;

			CreateNote createNote = new CreateNote();
			createNote.position = q.getMarkerLocation();
			createNote.text = text;
			createNote.questTitle = questTitle;
			createNote.elementType = q.getElementType();
			createNote.elementId = q.getElementId();
			createNoteDB.add(createNote);

			/* The quests that reference the same element for which the user was not able to
			   answer the question are removed because the to-be-created note blocks quest
			   creation for other users, so those quests should be removed from the user's
			   own display as well. As soon as the note is resolved, the quests will be re-
			   created next time they are downloaded */
			List<OsmQuest> questsForThisOsmElement = osmQuestDB.getAll(null, QuestStatus.NEW, null,
					q.getElementType(), q.getElementId());
			List<Long> otherQuestIdsForThisOsmElement = new ArrayList<>(questsForThisOsmElement.size());
			for(OsmQuest quest : questsForThisOsmElement)
			{
				if(quest.getId() != osmQuestId) otherQuestIdsForThisOsmElement.add(quest.getId());
			}

			/* creating a note instead of "really" solving the quest still counts as solving in
			   regards of the listener because the user answered to the quest and thats something
			 that needs to be uploaded */
			osmQuestDB.delete(osmQuestId);
			relay.onQuestSolved(osmQuestId, QuestGroup.OSM);

			osmQuestDB.deleteAll(otherQuestIdsForThisOsmElement);
			relay.onQuestsRemoved(otherQuestIdsForThisOsmElement, QuestGroup.OSM);

			osmElementDB.deleteUnreferenced();
			geometryDB.deleteUnreferenced();
		}});
	}

	/** Apply the user's answer to the given quest. (The quest will turn invisible.) */
	public void solveQuest(final long questId, final QuestGroup group, final Bundle answer,
						   final String source)
	{
		workerHandler.post(new Runnable() { @Override public void run()
		{
			boolean success = false;
			if (group == QuestGroup.OSM)
			{
				success = solveOsmQuest(questId, answer, source);
			}
			else if (group == QuestGroup.OSM_NOTE)
			{
				success = solveOsmNoteQuest(questId, answer);
			}
			if(success) relay.onQuestSolved(questId, group);
		}});
	}

	private boolean solveOsmNoteQuest(long questId, Bundle answer)
	{
		OsmNoteQuest q = osmNoteQuestDB.get(questId);
		String comment = answer.getString(NoteDiscussionForm.TEXT);
		if(comment != null && !comment.isEmpty())
		{
			q.setComment(comment);
			q.setStatus(QuestStatus.ANSWERED);
			osmNoteQuestDB.update(q);
			return true;
		}
		else
		{
			throw new RuntimeException(
					"NoteQuest has been answered with an empty comment!");
		}
	}

	private boolean solveOsmQuest(long questId, Bundle answer, String source)
	{
		// race condition: another thread (i.e. quest download thread) may have removed the
		// element already (#282). So in this case, just ignore
		OsmQuest q = osmQuestDB.get(questId);
		if(q == null) return false;
		Element ele = osmElementDB.get(q.getElementType(), q.getElementId());
		if(ele == null) return false;

		StringMapChangesBuilder changesBuilder = new StringMapChangesBuilder(ele.getTags());
		try
		{
			q.getOsmElementQuestType().applyAnswerTo(answer, changesBuilder);
		}
		catch (IllegalArgumentException e)
		{
			// if applying the changes results in an error (=a conflict), the data the quest(ion)
			// was based on is not valid anymore -> like with other conflicts, silently drop the
			// user's change (#289)
			return false;
		}
		StringMapChanges changes = changesBuilder.create();
		if(!changes.isEmpty())
		{
			q.setChanges(changes, source);
			q.setStatus(QuestStatus.ANSWERED);
			osmQuestDB.update(q);
			openChangesetsDao.setLastQuestSolvedTimeToNow();
			return true;
		}
		else
		{
			throw new RuntimeException(
					"OsmQuest " + questId + " (" + q.getType().getClass().getSimpleName() +
							") has been answered by the user but the changeset is empty!");
		}
	}

	/** Make the given quest invisible asynchronously (per user interaction). */
	public void hideQuest(final long questId, final QuestGroup group)
	{
		workerHandler.post(new Runnable() { @Override public void run()
		{
			if(group == QuestGroup.OSM)
			{
				OsmQuest q = osmQuestDB.get(questId);
				if(q == null) return;
				q.setStatus(QuestStatus.HIDDEN);
				osmQuestDB.update(q);
				relay.onQuestRemoved(q.getId(), group);
			}
			else if(group == QuestGroup.OSM_NOTE)
			{
				OsmNoteQuest q = osmNoteQuestDB.get(questId);
				if(q == null) return;
				q.setStatus(QuestStatus.HIDDEN);
				osmNoteQuestDB.update(q);
				relay.onQuestRemoved(q.getId(), group);
			}
		}});
	}

	/** Retrieve the given quest from local database asynchronously, including the element / note. */
	public void retrieve(final QuestGroup group, final long questId)
	{
		workerHandler.post(new Runnable() { @Override public void run()
		{
			switch (group)
			{
				case OSM:
					// race condition: another thread may have removed the element already (#288)
					OsmQuest osmQuest = osmQuestDB.get(questId);
					if(osmQuest == null) return;
					Element element = osmElementDB.get(osmQuest.getElementType(), osmQuest.getElementId());
					if(element == null) return;
					relay.onQuestCreated(osmQuest, group, element);
					break;
				case OSM_NOTE:
					OsmNoteQuest osmNoteQuest = osmNoteQuestDB.get(questId);
					if(osmNoteQuest == null) return;
					relay.onQuestCreated(osmNoteQuest, group, null);
					break;
			}
		}});
	}

	/** Retrieve all visible (=new) quests in the given bounding box from local database
	 *  asynchronously. */
	public void retrieve(final BoundingBox bbox)
	{
		workerHandler.post(new Runnable() { @Override public void run()
		{
			relay.onQuestsCreated(osmQuestDB.getAll(bbox, QuestStatus.NEW), QuestGroup.OSM);
			relay.onQuestsCreated(osmNoteQuestDB.getAll(bbox, QuestStatus.NEW), QuestGroup.OSM_NOTE);
		}});
	}

	/** Download quests in at least the given bounding box asynchronously. The next-bigger rectangle
	 *  in a (z14) tiles grid that encloses the given bounding box will be downloaded.
	 *
	 *  @param bbox the minimum area to download
	 *  @param maxQuestTypesToDownload download at most the given number of quest types. null for
	 *                                 unlimited
	 *  @param isPriority whether this shall be a priority download (cancels previous downloads and
	 *                    puts itself in the front)
	 */
	public void download(BoundingBox bbox, Integer maxQuestTypesToDownload, boolean isPriority)
	{
		Rect tilesRect = SlippyMapMath.enclosingTiles(bbox, ApplicationConstants.QUEST_TILE_ZOOM);

		Intent intent = new Intent(context, QuestDownloadService.class);
		intent.putExtra(QuestDownloadService.ARG_TILES_RECT, tilesRect);

		if(maxQuestTypesToDownload != null)
		{
			intent.putExtra(QuestDownloadService.ARG_MAX_QUEST_TYPES, maxQuestTypesToDownload);
		}
		if(isPriority)
		{
			intent.putExtra(QuestDownloadService.ARG_IS_PRIORITY, true);
		}
		context.startService(intent);

	}

	/** @return true if a quest download triggered by the user is running */
	public boolean isPriorityDownloadRunning()
	{
		return downloadService != null &&
		       downloadService.isDownloading() &&
		       downloadService.currentDownloadHasPriority();
	}

	/** Collect and upload all changes made by the user */
	public void upload()
	{
		context.startService(new Intent(context, QuestChangesUploadService.class));
	}
}
