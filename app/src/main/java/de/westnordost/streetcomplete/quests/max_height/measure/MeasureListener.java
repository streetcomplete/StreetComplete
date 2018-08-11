package de.westnordost.streetcomplete.quests.max_height.measure;

public interface MeasureListener
{
	void onMeasured(String meters);
	void onMeasured(String feet, String inches);
}
