package de.westnordost.osmagent.data.download;

public interface QuestDownloadProgressListener
{
	void onStarted();
	void onProgress(float progress);
	void onError(Exception e);
	void onFinished();
}
