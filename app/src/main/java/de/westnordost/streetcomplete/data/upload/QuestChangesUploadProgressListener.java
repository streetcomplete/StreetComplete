package de.westnordost.streetcomplete.data.upload;

public interface QuestChangesUploadProgressListener
{
	void onStarted();
	void onProgress(boolean success);
	void onError(Exception e);
	void onFinished();
}
