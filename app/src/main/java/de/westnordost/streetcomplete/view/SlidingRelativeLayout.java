package de.westnordost.streetcomplete.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

public class SlidingRelativeLayout extends RelativeLayout
{
	private float yFraction;
	private float xFraction;

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
		this.yFraction = fraction;
		postAfterViewMeasured(() -> setTranslationY(getHeight() * yFraction));
	}

	public float getYFraction()
	{
		return yFraction;
	}

	public void setXFraction(float fraction)
	{
		this.xFraction = fraction;
		postAfterViewMeasured(() -> setTranslationX(getWidth() * xFraction));
	}

	public float getXFraction()
	{
		return xFraction;
	}

	private void postAfterViewMeasured(Runnable runnable)
	{
		if (getWidth() != 0 || getHeight() != 0)
		{
			runnable.run();
		}
		else
		{
			if(getViewTreeObserver().isAlive())
			{
				getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
				{
					@Override public boolean onPreDraw()
					{
						runnable.run();
						if(getViewTreeObserver().isAlive())
						{
							getViewTreeObserver().removeOnPreDrawListener(this);
						}
						return true;
					}
				});
			}
		}
	}
}
