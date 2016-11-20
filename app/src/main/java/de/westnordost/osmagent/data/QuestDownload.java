package de.westnordost.osmagent.data;

import android.content.SharedPreferences;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmagent.Prefs;
import de.westnordost.osmagent.data.osm.OverpassQuestType;
import de.westnordost.osmagent.data.osm.download.OsmQuestDownload;
import de.westnordost.osmagent.data.osmnotes.OsmNotesDownload;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;

public class QuestDownload
{
	private final Provider<OsmNotesDownload> notesDownloadProvider;
	private final Provider<OsmQuestDownload> questDownloadProvider;
	private final QuestTypes questTypeList;
	private final SharedPreferences prefs;

	private BoundingBox bbox;
	private Integer maxVisibleQuests;
	private AtomicBoolean cancelState;
	private boolean isStartedByUser;

	// listeners
	private VisibleQuestListener questListener;
	private QuestDownloadProgressListener progressListener;

	// state
	private int downloadedQuestTypes = 0;
	private int totalQuestTypes;
	private int visibleQuests = 0;
	private boolean finished = false;

	@Inject public QuestDownload(Provider<OsmNotesDownload> notesDownloadProvider,
								 Provider<OsmQuestDownload> questDownloadProvider,
								 QuestTypes questTypeList, SharedPreferences prefs)
	{
		this.notesDownloadProvider = notesDownloadProvider;
		this.questDownloadProvider = questDownloadProvider;
		this.questTypeList = questTypeList;
		this.prefs = prefs;
	}

	public void setQuestTypeListener(VisibleQuestListener questListener)
	{
		this.questListener = questListener;
	}

	public void setProgressListener(QuestDownloadProgressListener progressListener)
	{
		this.progressListener = progressListener;
	}

	public void init(BoundingBox bbox, Integer maxVisibleQuests, boolean isStartedByUser,
					 AtomicBoolean cancel)
	{
		this.bbox = bbox;
		this.maxVisibleQuests = maxVisibleQuests;
		this.isStartedByUser = isStartedByUser;
		this.cancelState = cancel;
	}

	public void download()
	{
		if(cancelState.get()) return;

		try
		{
			progressListener.onStarted();

			List<QuestType> questTypes = questTypeList.getQuestTypesSortedByImportance();
			totalQuestTypes = questTypes.size() + 1; // +1 because of the notes quest type

			Set<LatLon> notesPositions = downloadNotes();
			downloadedQuestTypes++;
			dispatchProgress();

			downloadQuestTypes(questTypes, notesPositions);
		}
		finally
		{
			finished = true;
			progressListener.onFinished();
		}
	}

	private Set<LatLon> downloadNotes()
	{
		OsmNotesDownload notesDownload = notesDownloadProvider.get();
		notesDownload.setQuestListener(questListener);

		Long userId = prefs.getLong(Prefs.OSM_USER_ID, -1);
		if(userId == -1) userId = null;

		int maxNotes = maxVisibleQuests != null ? maxVisibleQuests : 10000;
		return notesDownload.download(bbox, userId, maxNotes);
	}

	private void downloadQuestTypes(List<QuestType> questTypes, Set<LatLon> notesPositions)
	{
		for (QuestType questType : questTypes)
		{
			if (!(questType instanceof OverpassQuestType)) continue;
			if (cancelState.get()) break;
			if (maxVisibleQuests != null && visibleQuests >= maxVisibleQuests) break;

			OsmQuestDownload questDownload = questDownloadProvider.get();
			questDownload.setQuestListener(questListener);

			visibleQuests += questDownload.download((OverpassQuestType) questType, bbox, notesPositions);

			downloadedQuestTypes++;
			dispatchProgress();
		}
	}

	public float getProgress()
	{
		float progressByQuestTypes = (float) downloadedQuestTypes / totalQuestTypes;
		float progressByMaxVisible = 0;
		if(maxVisibleQuests != null)
		{
			progressByMaxVisible = (float) visibleQuests / maxVisibleQuests;
		}
		return Math.min(1f, Math.max(progressByQuestTypes, progressByMaxVisible));
	}

	public boolean isStartedByUser()
	{
		return isStartedByUser;
	}

	public boolean isFinished()
	{
		return finished;
	}

	private void dispatchProgress()
	{
		progressListener.onProgress(getProgress());
	}
}
