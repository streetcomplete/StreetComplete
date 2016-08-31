package de.westnordost.osmagent.util;

public interface ProgressListener
{
	void onDone();
	void onProgress(float progress, float total);
}
