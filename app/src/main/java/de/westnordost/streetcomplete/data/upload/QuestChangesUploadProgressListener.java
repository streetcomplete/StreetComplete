package de.westnordost.streetcomplete.data.upload;

public interface QuestChangesUploadProgressListener
{
	void onError(Exception e);
	void onFinished();
}
