package de.westnordost.streetcomplete.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class SlidingRelativeLayout extends RelativeLayout
{
	public SlidingRelativeLayout(Context context)
	{
		super(context);
	}

	public SlidingRelativeLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public SlidingRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	public void setYFraction(float fraction)
	{
		/* Here we have to tackle two problems:
		   - the layout may not be measured yet. If it isn't hidden, this will result in an ugly
		     flicker in the first frame of the animation
		   - users can turn animations off (i.e. battery saver mode), which results in that all
		     animations "start" in their final frame immediately. If the view is hidden in the first
		     frame, it will never appear
		 solution: hide it only if it was not measured yet AND it should be translated */
		setVisibility(getHeight() == 0 && fraction != 0 ? View.INVISIBLE : View.VISIBLE);

		float translationY = getHeight() * fraction;
		setTranslationY(translationY);
	}

	public float getYFraction()
	{
		if(getHeight() == 0) return 0;
		return getTranslationY() / getHeight();
	}

	public void setXFraction(float fraction)
	{
		setVisibility(getWidth() == 0 && fraction != 0 ? View.INVISIBLE : View.VISIBLE);

		float translationX = getWidth() * fraction;
		setTranslationX(translationX);
	}

	public float getXFraction()
	{
		if(getWidth() == 0) return 0;
		return getTranslationX() / getWidth();
	}
}
