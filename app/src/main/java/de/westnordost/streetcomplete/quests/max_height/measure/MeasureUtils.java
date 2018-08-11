package de.westnordost.streetcomplete.quests.max_height.measure;

public class MeasureUtils
{
	private float bottomAngle;
	private float topAngle;

	private int userHeight = -1;

	public void setBottomAngle(float angle)
	{
		bottomAngle = angle;
	}

	public void setTopAngle(float angle)
	{
		topAngle = angle;
	}

	public void setUserHeight(int height)
	{
		userHeight = height;
	}

	public int getUserHeight()
	{
		return userHeight;
	}

	private double getDistance(float cameraHeight)
	{
		return cameraHeight * Math.tan(bottomAngle);
	}

	public float getHeight()
	{
		float cameraHeight =  userHeight / 100f;
		double distance = getDistance(cameraHeight);

		double angle = Math.PI/2.0 - Math.abs(topAngle);
		double height = distance * Math.tan(angle);

		float result =  cameraHeight + (float) height * (-1)/Math.signum(topAngle);

		//return the absolute height because the user might have marked the top of the object before the bottom
		return Math.abs(result);
	}
}
