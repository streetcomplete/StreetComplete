package de.westnordost.osmagent.quests;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.quests.osmnotes.NotesQuestDownloadTask;
import de.westnordost.osmagent.quests.osm.download.OverpassQuestDownloadTask;
import de.westnordost.osmagent.quests.osm.download.ReflectionQuestTypeListCreator;
import de.westnordost.osmagent.quests.osm.types.OverpassQuestType;
import de.westnordost.osmagent.quests.osmnotes.OsmNoteQuestChangesUploadTask;
import de.westnordost.osmagent.quests.osm.upload.OsmQuestChangesUploadTask;
import de.westnordost.osmagent.quests.osmnotes.OsmNoteQuestDao;
import de.westnordost.osmagent.util.SerialExecutorService;
import de.westnordost.osmapi.map.data.BoundingBox;


public class QuestController
{
	public static final String OSM_QUEST_PACKAGE = "de.westnordost.osmagent.quests.osm.types";

	@Inject OsmNoteQuestDao osmNoteQuestDB;
	@Inject OsmQuestDao osmQuestDB;
	@Inject SharedPreferences prefs;

	private SerialExecutorService downloader;
	private SerialExecutorService uploader;

	private Long osmUserId;

	public QuestController()
	{
		downloader = new SerialExecutorService();
		uploader = new SerialExecutorService();
	}

	public void init()
	{
		downloader.init();
		uploader.init();
	}

	public void download(BoundingBox bbox)
	{
		downloader.replace(createQuestDownloadTasks(bbox));
	}

	public void shutdown()
	{
		downloader.shutdown();
		uploader.shutdown();
	}

	public void uploadChanges()
	{
		uploader.add(createQuestChangesUploadTasks());
	}

	private List<Runnable> createQuestChangesUploadTasks()
	{
		List<Runnable> tasks = new ArrayList<>();

		List<Long> noteQuestIds = osmNoteQuestDB.getIdsByStatus(QuestStatus.ANSWERED);
		for(Long questId : noteQuestIds)
		{
			OsmNoteQuestChangesUploadTask task = new OsmNoteQuestChangesUploadTask();
			task.questId = questId;
			task.osmUserId = null; // TODO
			tasks.add(task);
		}

		List<Long> osmQuestIds = osmQuestDB.getIdsByStatus(QuestStatus.ANSWERED);
		for(Long questId : osmQuestIds)
		{
			OsmQuestChangesUploadTask task = new OsmQuestChangesUploadTask();
			task.questId = questId;
			tasks.add(task);
		}

		return tasks;
	}

	private List<Runnable> createQuestDownloadTasks(BoundingBox bbox)
	{
		List<Runnable> tasks = new ArrayList<>();
		NotesQuestDownloadTask noteTask = new NotesQuestDownloadTask();
		noteTask.bbox = bbox;
		noteTask.osmUserId = null; // TODO
		tasks.add(noteTask);

		List<OverpassQuestType> questTypes = ReflectionQuestTypeListCreator
				.create(OverpassQuestType.class, OSM_QUEST_PACKAGE);
		for(OverpassQuestType questType : questTypes)
		{
			OverpassQuestDownloadTask task = new OverpassQuestDownloadTask();
			task.bbox = bbox;
			task.questType = questType;
			tasks.add(task);
		}

		return tasks;
	}
}
